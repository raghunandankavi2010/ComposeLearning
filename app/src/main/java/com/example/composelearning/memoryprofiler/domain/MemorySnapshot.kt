package com.example.composelearning.memoryprofiler.domain

import androidx.compose.runtime.Immutable

/**
 * An immutable point-in-time reading of the process' memory situation.
 *
 * All figures are captured off the main thread (see
 * `MemoryProfilingManager.readSnapshot`) and pushed into Compose as a single
 * value, so the UI only recomposes when the *state object* changes — never
 * because we mutate a field in place.
 *
 * Byte-based fields are stored raw; formatting to MB happens in the UI layer
 * (outside the draw/measure phase) so we don't build `String`s we might throw
 * away during layout.
 */
@Immutable
data class MemorySnapshot(
    /** `SystemClock.elapsedRealtime()`-independent wall clock, for the log. */
    val capturedAtMs: Long,
    /** Bytes currently allocated on the managed (ART) heap: total − free. */
    val heapAllocatedBytes: Long,
    /** The hard ceiling the VM will grow the managed heap to (`Runtime.maxMemory`). */
    val heapMaxBytes: Long,
    /** Headroom left before hitting [heapMaxBytes]: max − allocated. */
    val heapFreeBytes: Long,
    /** Native (C/C++) heap currently allocated — invisible to the ART GC. */
    val nativeAllocatedBytes: Long,
    /** System-wide available RAM (`ActivityManager.MemoryInfo.availMem`). */
    val systemAvailableBytes: Long,
    /** The low-memory threshold below which the system starts killing processes. */
    val systemThresholdBytes: Long,
    /** Whether the system currently considers itself in a low-memory state. */
    val systemLowMemory: Boolean
) {
    /**
     * Fraction of the managed-heap ceiling that is in use, clamped to `0f..1f`.
     * Cheap enough to expose as a property; the UI reads it through a
     * `derivedStateOf` so the gauge only re-derives when the number moves.
     */
    val heapUsedFraction: Float
        get() = if (heapMaxBytes <= 0L) 0f else (heapAllocatedBytes.toFloat() / heapMaxBytes).coerceIn(0f, 1f)

    companion object {
        /** Neutral value used before the first reading lands. */
        val EMPTY = MemorySnapshot(
            capturedAtMs = 0L,
            heapAllocatedBytes = 0L,
            heapMaxBytes = 1L,
            heapFreeBytes = 0L,
            nativeAllocatedBytes = 0L,
            systemAvailableBytes = 0L,
            systemThresholdBytes = 0L,
            systemLowMemory = false
        )
    }
}
