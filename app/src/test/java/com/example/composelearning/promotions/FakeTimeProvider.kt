package com.example.composelearning.promotions

/**
 * Controllable [TimeProvider] for tests.
 *
 * - [wallClock] is the (mutable) value returned by [currentTimeMillis]; tests can change it after
 *   start to prove the countdown is immune to wall-clock tampering.
 * - [elapsedRealtime] is derived as [elapsedBase] + [virtualTime]. Wiring [virtualTime] to the
 *   coroutine test scheduler's virtual clock means uptime advances exactly as `delay()` advances,
 *   so the timer loop terminates naturally under `advanceUntilIdle()`.
 */
class FakeTimeProvider(
    var wallClock: Long = 0L,
    var elapsedBase: Long = 0L,
    private val virtualTime: () -> Long = { 0L }
) : TimeProvider {
    override fun currentTimeMillis(): Long = wallClock
    override fun elapsedRealtime(): Long = elapsedBase + virtualTime()
}
