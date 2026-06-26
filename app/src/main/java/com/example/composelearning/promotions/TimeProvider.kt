package com.example.composelearning.promotions

import android.os.SystemClock

/**
 * Abstraction over the two clocks the deal timer relies on.
 *
 * Pulling these behind an interface keeps [DealTimerViewModel] free of direct Android framework
 * calls, so its time-sensitive logic can be exercised in plain JVM unit tests by supplying a
 * controllable fake instead of waiting on the real wall clock / device uptime.
 *
 * - [currentTimeMillis] is the wall clock and can be changed by the user (used only once, at start,
 *   to derive the remaining duration against the server timestamp).
 * - [elapsedRealtime] is monotonic device uptime and cannot be tampered with; the countdown ticks
 *   against this so manual clock changes can't extend or shorten a deal.
 */
interface TimeProvider {
    /** Wall-clock time, equivalent to [System.currentTimeMillis]. */
    fun currentTimeMillis(): Long

    /** Monotonic device uptime, equivalent to [SystemClock.elapsedRealtime]. */
    fun elapsedRealtime(): Long
}

/** Production [TimeProvider] backed by the real Android clocks. */
class SystemTimeProvider : TimeProvider {
    override fun currentTimeMillis(): Long = System.currentTimeMillis()
    override fun elapsedRealtime(): Long = SystemClock.elapsedRealtime()
}
