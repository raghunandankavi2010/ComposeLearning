package com.example.composelearning.fastimages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.max

/**
 * Debug-only forcing of either tier, so the behaviour of a cheap phone on a
 * slow link can be experienced on whatever hardware you happen to hold.
 */
@Immutable
data class PolicyOverrides(
    val deviceTier: DeviceTier? = null,
    val networkTier: NetworkTier? = null,
    val forceSaveData: Boolean = false,
    /**
     * Forces a punitively short read budget so the downgrade ladder can be
     * watched working. Off by default, because against a real backend it
     * produces nothing but failures.
     */
    val tightTimeouts: Boolean = false
)

/**
 * The resolved answer to "what should we actually download right now".
 *
 * This is the whole point of the exercise. Note what is *not* here: no device
 * model, no user agent, no screen size. Capability is resolved **on the
 * device** into a handful of variant parameters, and only those parameters
 * reach the network.
 *
 * That is not a style choice, it is what keeps the CDN cacheable. If the app
 * posted its hardware details and let the server decide, the response would
 * vary per device model: you would need `Vary` headers and your edge hit rate
 * would collapse across tens of thousands of Android SKUs. Resolving locally
 * collapses that population into `buckets x qualities x formats` — a few dozen
 * derivatives, all shared. The URL is the contract.
 */
@Immutable
data class DeliveryProfile(
    val label: String,
    val deviceTier: DeviceTier,
    val networkTier: NetworkTier,
    val saveData: Boolean,
    val metered: Boolean,
    /** Ceiling on the requested width bucket — the `w=` parameter's upper bound. */
    val bucketCapPx: Int,
    /** The `q=` parameter. Cheapest lever there is: near-linear in bytes. */
    val quality: Int,
    /** Whether we would ask a real CDN for AVIF. See [DevicePolicy.avifCapable]. */
    val avifPreferred: Boolean,
    /** Decode into RGB_565 instead of letting Coil use a hardware bitmap. */
    val useRgb565: Boolean,
    val eagerPrefetch: Int,
    val diskPrefetch: Int,
    /** DNS + TCP + TLS. Independent of server think time. */
    val connectTimeoutMs: Int,
    /**
     * Read budget: time to first byte, and between bytes thereafter. On expiry
     * we downgrade rather than retry the same size.
     *
     * This is *not* a total call budget. "Cancel the whole call after 1.5s" is
     * `OkHttpClient.callTimeout` / `Call.timeout()`, which an interceptor cannot
     * reach — interceptors can only shape connect, read and write.
     */
    val firstAttemptTimeoutMs: Int,
    val retryTimeoutMs: Int,
    /** Applied to the live `MemoryCache.maxSize`, no loader rebuild required. */
    val memoryCacheBytes: Long,
    val rationale: String
)

/**
 * `variant = f(layout, density, device tier, network tier, user intent)`.
 *
 * Every number below is a guess that should live in remote config and be moved
 * by telemetry — image p95 latency and decode time segmented by device tier and
 * network tier. Shipping the ladders as constants is how you guarantee you can
 * never fix them without a release.
 */
object DeliveryPolicy {

    /** Quality rungs, low to high. The downgrade ladder walks this backwards. */
    val QUALITY_LADDER = intArrayOf(40, 50, 62, 74)

    /**
     * Connecting is a fixed cost that does not scale with the link's throughput,
     * so it does not belong on the tier ladder.
     */
    private const val CONNECT_TIMEOUT_MS = 5_000

    fun resolve(
        signals: DeviceSignals,
        status: NetworkStatus,
        overrides: PolicyOverrides = PolicyOverrides()
    ): DeliveryProfile {
        val deviceTier = overrides.deviceTier ?: signals.tier
        val device = signals.policyFor(deviceTier)
        val networkTier = overrides.networkTier ?: status.tier
        val saveData = overrides.forceSaveData || status.saveDataRequested

        val quality = when {
            saveData -> QUALITY_LADDER[0]
            networkTier == NetworkTier.OFFLINE || networkTier == NetworkTier.SLOW -> QUALITY_LADDER[1]
            networkTier == NetworkTier.MODERATE -> QUALITY_LADDER[2]
            else -> QUALITY_LADDER[3]
        }

        // Two independent ceilings, and the tighter one wins: what the *screen*
        // could use, capped by what the *device* can afford to decode, capped
        // again by what the *link* can deliver in time.
        val networkCap = when {
            saveData -> 240
            networkTier == NetworkTier.OFFLINE || networkTier == NetworkTier.SLOW -> 240
            networkTier == NetworkTier.MODERATE -> 400
            else -> 1080
        }
        val bucketCap = minOf(device.maxBucketWidthPx, networkCap)

        var eager = when (networkTier) {
            NetworkTier.OFFLINE -> 0
            NetworkTier.SLOW -> 2
            NetworkTier.MODERATE -> 4
            NetworkTier.FAST -> 6
        }
        var disk = when (networkTier) {
            NetworkTier.OFFLINE -> 0
            NetworkTier.SLOW -> 0
            NetworkTier.MODERATE -> 6
            NetworkTier.FAST -> 12
        }
        // Prefetching is a bet placed with someone else's data allowance. When
        // they have explicitly asked us not to, we stop betting entirely.
        if (saveData) {
            eager = 0
            disk = 0
        } else if (status.metered && networkTier == NetworkTier.FAST) {
            disk = 6
        }
        // On a low tier the limit is usually decode CPU, not bandwidth: every
        // eager prefetch is a full bitmap allocation competing with the frame.
        if (deviceTier == DeviceTier.LOW) {
            eager = max(1, eager / 2)
            disk = minOf(disk, 4)
        }

        // Calibration is the whole game here, and it has to be done against the
        // *actual* backend's time-to-first-byte distribution.
        //
        // A 1.5s read budget is correct for a CDN edge returning a pre-warmed
        // derivative in under 200ms. It is completely wrong for a backend that
        // generates the image on demand — this demo's picsum.photos 302-redirects
        // to Fastly and synthesises the crop, measuring 0.9-1.6s TTFB from a
        // desktop on fibre. Ship the aggressive number against that and every
        // request times out, downgrades twice, and the user sees placeholders
        // forever. Which is exactly what happened the first time this ran.
        //
        // So: generous enough for the real backend, with [PolicyOverrides.tightTimeouts]
        // to demonstrate the fallback path on demand.
        val firstTimeout = when {
            overrides.tightTimeouts -> 1_200
            networkTier == NetworkTier.FAST -> 5_000
            networkTier == NetworkTier.MODERATE -> 8_000
            else -> 12_000
        }

        return DeliveryProfile(
            label = buildString {
                append(deviceTier.name.lowercase())
                append('/')
                append(networkTier.name.lowercase())
                if (saveData) append("+lite")
            },
            deviceTier = deviceTier,
            networkTier = networkTier,
            saveData = saveData,
            metered = status.metered,
            bucketCapPx = bucketCap,
            quality = quality,
            avifPreferred = device.avifCapable && !saveData,
            useRgb565 = device.useRgb565,
            eagerPrefetch = eager,
            diskPrefetch = disk,
            connectTimeoutMs = CONNECT_TIMEOUT_MS,
            firstAttemptTimeoutMs = firstTimeout,
            retryTimeoutMs = firstTimeout * 3 / 2,
            memoryCacheBytes = device.memoryCacheBytes,
            rationale = rationaleFor(device, status, networkTier, saveData)
        )
    }

    fun qualityDown(quality: Int, steps: Int): Int {
        val index = QUALITY_LADDER.indexOfFirst { it >= quality }.takeIf { it >= 0 }
            ?: QUALITY_LADDER.lastIndex
        return QUALITY_LADDER[(index - steps).coerceAtLeast(0)]
    }

    private fun rationaleFor(
        device: DevicePolicy,
        status: NetworkStatus,
        networkTier: NetworkTier,
        saveData: Boolean
    ): String = buildString {
        append(device.signals.summary)
        append(" → cache ").append(device.memoryCachePercent).append("% of heap")
        append(" · ").append(status.transport)
        if (status.metered) append(" (metered)")
        val measured = if (status.measuredKbps > 0) {
            "${status.measuredKbps} kbps measured"
        } else {
            "${status.reportedKbps} kbps reported"
        }
        append(" · ").append(measured).append(" → ").append(networkTier.name.lowercase())
        if (saveData) append(" · Data Saver on: prefetch off, lowest quality")
    }
}

/**
 * Drops [steps] rungs off both ladders.
 *
 * Used when a request times out: retrying the *same* variant on a link that
 * just proved it cannot deliver it is how you turn one slow image into three.
 * Asking for less is the only retry that changes the odds.
 */
fun DeliveryProfile.downgraded(steps: Int): DeliveryProfile {
    if (steps <= 0) return this
    return copy(
        label = "$label-$steps",
        bucketCapPx = ImageCdn.bucketDown(bucketCapPx, steps),
        quality = DeliveryPolicy.qualityDown(quality, steps)
    )
}

/**
 * Read by the OkHttp timeout interceptor, which has no access to composition.
 * A single volatile field is enough: it is written on the main thread whenever
 * the profile changes and read on OkHttp's dispatcher threads.
 */
internal object ActiveProfile {
    @Volatile
    var current: DeliveryProfile? = null
}

@Composable
fun rememberDeliveryProfile(overrides: PolicyOverrides): DeliveryProfile {
    val context = LocalContext.current
    val signals = remember(context) { DeviceSignals.detect(context) }
    val monitor = remember(context) { FeedNetworkMonitors.get(context) }
    val status by monitor.status.collectAsStateWithLifecycle()
    val profile = remember(signals, status, overrides) {
        DeliveryPolicy.resolve(signals, status, overrides)
    }
    // Hand the resolved profile to the network layer, which lives outside
    // composition and needs it to size its per-call timeouts.
    SideEffect { ActiveProfile.current = profile }
    return profile
}
