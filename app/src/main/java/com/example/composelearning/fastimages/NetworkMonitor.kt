package com.example.composelearning.fastimages

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.SystemClock
import androidx.compose.runtime.Immutable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Measured downstream throughput, in kbit/s, above which we treat the link as fast. */
private const val FAST_KBPS = 3_000

/** ...and above which we treat it as usable but not generous. */
private const val MODERATE_KBPS = 700

/**
 * Moving *up* a tier needs 25% more headroom than the raw threshold.
 *
 * Without this, a connection parked at 3,000 kbit/s flips FAST/MODERATE every
 * few seconds, and since the tier feeds the variant URL, every flip changes
 * the cache key and re-downloads the visible screen. Tier flapping is a real
 * production bug, not a theoretical one.
 */
private const val UPGRADE_MARGIN = 1.25

/** Minimum time a tier must hold before it is allowed to change again. */
private const val MIN_TIER_DWELL_MS = 10_000L

/** How often the measured throughput is re-classified into a tier. */
private const val TIER_REEVALUATE_MS = 5_000L

/** Samples smaller or faster than this are noise, not bandwidth evidence. */
private const val MIN_SAMPLE_BYTES = 8 * 1024L
private const val MIN_SAMPLE_MS = 10L

/** Weight of the newest sample in the moving average. */
private const val EWMA_ALPHA = 0.3

/**
 * The published throughput is rounded to this granularity.
 *
 * Not cosmetic: [NetworkStatus] feeds the delivery profile, and a raw kbit/s
 * figure that ticks every few seconds would produce a new profile object every
 * few seconds, invalidating everything keyed on it. The debug panel reads the
 * unrounded value separately, so jitter stays in the UI and out of the policy.
 */
private const val KBPS_ROUNDING = 250

enum class NetworkTier { OFFLINE, SLOW, MODERATE, FAST }

@Immutable
data class NetworkStatus(
    val tier: NetworkTier = NetworkTier.MODERATE,
    val metered: Boolean = true,
    /** The OS-level Data Saver switch. A direct statement of user intent. */
    val saveDataRequested: Boolean = false,
    /** Our own measurement. 0 until enough bytes have moved to say anything. */
    val measuredKbps: Int = 0,
    /** What the platform *claims*, kept only to show how optimistic it is. */
    val reportedKbps: Int = 0,
    val transport: String = "unknown"
)

/**
 * Exponentially-weighted moving average of real transfer throughput.
 *
 * Measured from [okhttp3.EventListener.responseBodyStart] to
 * [okhttp3.EventListener.responseBodyEnd], so it covers *only* body transfer —
 * DNS, TCP, TLS and time-to-first-byte are latency, not bandwidth, and folding
 * them in would make a fast link on a far server look slow.
 *
 * This exists because [NetworkCapabilities.getLinkDownstreamBandwidthKbps] is
 * a capability hint from the radio, not a measurement: it happily reports
 * "LTE, 20 Mbps" on a congested cell that is delivering 300 kbit/s. Anything
 * that adapts to bandwidth has to measure its own.
 */
class ThroughputEstimator {

    private val lock = Any()
    private var ewmaKbps = 0.0

    fun sample(bytes: Long, elapsedMs: Long) {
        if (bytes < MIN_SAMPLE_BYTES || elapsedMs < MIN_SAMPLE_MS) return
        // bits per millisecond is numerically identical to kbit per second.
        val kbps = (bytes * 8.0) / elapsedMs
        synchronized(lock) {
            ewmaKbps = if (ewmaKbps == 0.0) kbps else EWMA_ALPHA * kbps + (1 - EWMA_ALPHA) * ewmaKbps
        }
    }

    fun kbps(): Int = synchronized(lock) { ewmaKbps.toInt() }

    fun reset() {
        synchronized(lock) { ewmaKbps = 0.0 }
    }
}

/**
 * The dynamic half of the delivery decision.
 *
 * Publishes a [NetworkStatus] whose **tier changes rarely and deliberately**.
 * Throughput samples arrive with every image, but they only update the average;
 * re-classification happens on a slow timer, behind a dwell time and an upgrade
 * margin. The live kbit/s number is readable separately for display, so the
 * debug panel can show a jittery value without that jitter ever reaching the
 * variant URL.
 */
class NetworkMonitor internal constructor(context: Context) {

    private val connectivity: ConnectivityManager? =
        context.getSystemService(ConnectivityManager::class.java)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val throughput = ThroughputEstimator()

    private val _status = MutableStateFlow(NetworkStatus())
    val status: StateFlow<NetworkStatus> = _status.asStateFlow()

    private var lastTierChangeAt = 0L

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) = reevaluate()
        override fun onLost(network: Network) = reevaluate()
        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) =
            reevaluate()
    }

    internal fun start() {
        // Needs ACCESS_NETWORK_STATE; degrade to the timer alone rather than
        // taking the screen down if a build variant drops the permission.
        runCatching { connectivity?.registerDefaultNetworkCallback(callback) }
        reevaluate()
        scope.launch {
            while (true) {
                delay(TIER_REEVALUATE_MS)
                reevaluate()
            }
        }
    }

    /** Called from the transport listener for every completed image body. */
    fun sampleTransfer(bytes: Long, elapsedMs: Long) = throughput.sample(bytes, elapsedMs)

    fun liveKbps(): Int = throughput.kbps()

    fun resetMeasurements() = throughput.reset()

    private fun reevaluate() {
        val capabilities = activeCapabilities()
        val measured = throughput.kbps()
        val reported = capabilities?.linkDownstreamBandwidthKbps ?: 0
        val current = _status.value

        val tier = when {
            capabilities == null -> NetworkTier.OFFLINE
            // Prefer our own measurement; fall back to the radio's claim, but
            // discount it heavily because it describes the link's best case.
            else -> classify(
                kbps = if (measured > 0) measured else reported / 3,
                current = current.tier
            )
        }

        val settled = tier == current.tier ||
            SystemClock.elapsedRealtime() - lastTierChangeAt >= MIN_TIER_DWELL_MS
        val nextTier = if (settled) tier else current.tier
        if (nextTier != current.tier) lastTierChangeAt = SystemClock.elapsedRealtime()

        _status.value = NetworkStatus(
            tier = nextTier,
            metered = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) != true,
            saveDataRequested = isDataSaverOn(),
            measuredKbps = round(measured),
            reportedKbps = round(reported),
            transport = describeTransport(capabilities)
        )
    }

    private fun classify(kbps: Int, current: NetworkTier): NetworkTier {
        val raw = when {
            kbps >= FAST_KBPS -> NetworkTier.FAST
            kbps >= MODERATE_KBPS -> NetworkTier.MODERATE
            else -> NetworkTier.SLOW
        }
        if (raw.ordinal <= current.ordinal) return raw
        val threshold = if (raw == NetworkTier.FAST) FAST_KBPS else MODERATE_KBPS
        return if (kbps >= threshold * UPGRADE_MARGIN) raw else current
    }

    private fun activeCapabilities(): NetworkCapabilities? {
        val manager = connectivity ?: return null
        return runCatching {
            manager.activeNetwork?.let(manager::getNetworkCapabilities)
        }.getOrNull()
    }

    private fun round(kbps: Int): Int = (kbps / KBPS_ROUNDING) * KBPS_ROUNDING

    private fun isDataSaverOn(): Boolean = runCatching {
        connectivity?.restrictBackgroundStatus ==
            ConnectivityManager.RESTRICT_BACKGROUND_STATUS_ENABLED
    }.getOrDefault(false)

    private fun describeTransport(capabilities: NetworkCapabilities?): String = when {
        capabilities == null -> "offline"
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "wifi"
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "cellular"
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ethernet"
        else -> "other"
    }
}

/**
 * App-scoped, like the loader. Network state is a property of the process, and
 * a per-screen monitor would throw away the throughput history that makes the
 * estimate worth anything.
 */
internal object FeedNetworkMonitors {

    @Volatile
    private var monitor: NetworkMonitor? = null

    fun get(context: Context): NetworkMonitor = monitor ?: synchronized(this) {
        monitor ?: NetworkMonitor(context.applicationContext).also {
            it.start()
            monitor = it
        }
    }
}
