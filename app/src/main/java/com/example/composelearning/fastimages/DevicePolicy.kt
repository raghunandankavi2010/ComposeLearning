package com.example.composelearning.fastimages

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import androidx.compose.runtime.Immutable

private const val MB = 1024L * 1024L
private const val GB = 1024L * MB

/**
 * How much the hardware can be asked to do.
 *
 * The reason this exists at all: **screen density is a terrible proxy for
 * device capability.** A budget phone sold in India today ships a 1080x2400
 * panel with 2-3 GB of RAM and eight slow A53 cores. Sizing images from
 * density alone hands that phone the same payload as a flagship and then asks
 * its weaker CPU to decode it. Density tells you how many pixels the screen
 * has; it says nothing about how many the device can afford.
 *
 * Note also that on a low tier the binding constraint is usually **decode CPU
 * and bitmap memory, not bandwidth**. One 1080x1080 ARGB_8888 bitmap is 4.4 MB
 * of Java heap; a dozen of them is an OOM on a 96 MB heap. Bytes-on-the-wire
 * and bytes-in-memory are two separate budgets and each needs its own cap.
 */
enum class DeviceTier { LOW, MID, HIGH }

/**
 * Raw hardware facts, read once at startup. Cheap to collect and completely
 * static, so there is no reason to ever re-measure them.
 */
@Immutable
data class DeviceSignals(
    /** Max Java heap this process can grow to — the real bitmap budget. */
    val heapBytes: Long,
    val totalRamBytes: Long,
    val cores: Int,
    val sdkInt: Int,
    /** The OS's own opinion, and the single strongest signal available. */
    val lowRamFlagged: Boolean
) {
    /**
     * Heuristic tiering. In production these cut-offs belong in remote config
     * and get calibrated against your own crash-free-rate and frame-timing
     * telemetry per device model — you ship a guess, then let the data move it.
     */
    val tier: DeviceTier
        get() = when {
            lowRamFlagged ||
                totalRamBytes < 3 * GB ||
                cores <= 4 ||
                heapBytes < 128 * MB -> DeviceTier.LOW

            totalRamBytes >= 6 * GB &&
                cores >= 8 &&
                heapBytes >= 256 * MB -> DeviceTier.HIGH

            else -> DeviceTier.MID
        }

    val summary: String
        get() = "${totalRamBytes / GB}GB RAM · ${heapBytes / MB}MB heap · ${cores}c · API $sdkInt" +
            if (lowRamFlagged) " · lowRam" else ""

    companion object {
        fun detect(context: Context): DeviceSignals {
            val activityManager = context.getSystemService(ActivityManager::class.java)
            val memoryInfo = ActivityManager.MemoryInfo().also { activityManager?.getMemoryInfo(it) }
            return DeviceSignals(
                heapBytes = Runtime.getRuntime().maxMemory(),
                totalRamBytes = memoryInfo.totalMem,
                cores = Runtime.getRuntime().availableProcessors(),
                sdkInt = Build.VERSION.SDK_INT,
                lowRamFlagged = activityManager?.isLowRamDevice == true
            )
        }
    }
}

/**
 * The static half of the delivery decision — everything that depends only on
 * the hardware and therefore never changes while the app is running.
 *
 * This half configures the `ImageLoader` itself. The dynamic half (network,
 * Save-Data) lives in [DeliveryProfile] and only affects individual requests.
 * Keeping the split explicit matters: you do not want to rebuild a loader,
 * and with it throw away the memory cache, the disk cache and the connection
 * pool, every time the radio changes state.
 */
@Immutable
data class DevicePolicy(
    val tier: DeviceTier,
    val signals: DeviceSignals,
    /** Bitmap cache budget in bytes — a *share of the heap*, not a fixed number. */
    val memoryCacheBytes: Long,
    /** Hard ceiling on the width bucket, regardless of how dense the screen is. */
    val maxBucketWidthPx: Int,
    /** RGB_565 halves bytes-per-pixel. Safe for opaque photos, wrong for alpha. */
    val useRgb565: Boolean,
    /**
     * AVIF is 20-30% smaller than WebP *and* meaningfully slower to decode on
     * weak CPUs, so on a low tier it can cost more in decode time than it saves
     * in transfer. "Fewer bytes" and "faster" are not the same thing — which is
     * why this is gated on tier, not just on OS support.
     */
    val avifCapable: Boolean
) {
    val memoryCachePercent: Int
        get() = ((memoryCacheBytes * 100) / signals.heapBytes.coerceAtLeast(1)).toInt()
}

/**
 * Builds a policy for an explicit [tier], keeping the measured hardware facts.
 * Passing a tier the device does not actually have is how the debug panel lets
 * you experience a cheap phone on expensive hardware.
 */
fun DeviceSignals.policyFor(tier: DeviceTier = this.tier): DevicePolicy = DevicePolicy(
    tier = tier,
    signals = this,
    memoryCacheBytes = (heapBytes * memoryCacheShare(tier)).toLong(),
    maxBucketWidthPx = when (tier) {
        DeviceTier.LOW -> 320
        DeviceTier.MID -> 480
        DeviceTier.HIGH -> 1080
    },
    useRgb565 = tier == DeviceTier.LOW,
    avifCapable = sdkInt >= Build.VERSION_CODES.S && tier != DeviceTier.LOW
)

private fun memoryCacheShare(tier: DeviceTier): Double = when (tier) {
    // 25% of a 256 MB heap is reasonable; 25% of a 96 MB heap is reckless,
    // because everything else in the app has to fit in what is left.
    DeviceTier.LOW -> 0.12
    DeviceTier.MID -> 0.20
    DeviceTier.HIGH -> 0.25
}
