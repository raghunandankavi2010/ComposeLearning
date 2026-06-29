package com.example.composelearning.promotions

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

/**
 * Unit tests for [DealTimerViewModel].
 *
 * The countdown is a cold flow ([DealTimerViewModel.timerFlow]) shared via
 * `stateIn(WhileSubscribed)`, so most tests collect the cold flow directly on virtual time. The
 * injected [TimeProvider] (uptime wired to the test scheduler) and [DealStore] make the logic
 * deterministic without a device clock or disk.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DealTimerViewModelTest {

    @Before
    fun setUp() = Dispatchers.setMain(StandardTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `flow is cold - no work happens until it is collected`() = runTest {
        val store = FakeDealStore()
        val time = FakeTimeProvider(wallClock = 0L, virtualTime = { testScheduler.currentTime })
        val vm = DealTimerViewModel(store, initialTargetEndTimestamp = 10_000L, timeProvider = time)

        // Merely creating the ViewModel must not touch the store or emit anything.
        runCurrent()
        assertEquals(0, store.saveCount)
        assertEquals(DealTimerUiState(), vm.timerState.value)
    }

    @Test
    fun `persists the initial target when nothing is saved`() = runTest {
        val store = FakeDealStore(initial = null)
        val time = FakeTimeProvider(wallClock = 0L, virtualTime = { testScheduler.currentTime })
        val vm = DealTimerViewModel(store, initialTargetEndTimestamp = 30_000L, timeProvider = time)

        val first = vm.timerFlow().first()

        assertEquals(1, store.saveCount)
        assertEquals(30_000L, store.savedValue)
        assertEquals(30_000L, first.remainingMillis) // target(30_000) - wall(0), from elapsed(0)
        assertFalse(first.isExpired)
    }

    @Test
    fun `exposes the resolved target so dependents stay in sync`() = runTest {
        // After process death the seed differs from the persisted deadline; emissions must surface
        // the persisted one, which the notification (and anything else) relies on.
        val store = FakeDealStore(initial = 77_000L)
        val time = FakeTimeProvider(wallClock = 0L, virtualTime = { testScheduler.currentTime })
        val vm = DealTimerViewModel(store, initialTargetEndTimestamp = 5_000L, timeProvider = time)

        val first = vm.timerFlow().first()

        assertEquals(77_000L, first.targetEndTimestamp)
    }

    @Test
    fun `restores a persisted target after process death`() = runTest {
        // A value saved before the process was killed; the initial target must be ignored.
        val store = FakeDealStore(initial = 50_000L)
        val time = FakeTimeProvider(wallClock = 0L, virtualTime = { testScheduler.currentTime })
        val vm = DealTimerViewModel(store, initialTargetEndTimestamp = 999_999L, timeProvider = time)

        val first = vm.timerFlow().first()

        assertEquals("must not overwrite the persisted value", 0, store.saveCount)
        assertEquals(50_000L, store.savedValue)
        assertEquals(50_000L, first.remainingMillis)
    }

    @Test
    fun `counts down as time elapses`() = runTest {
        val store = FakeDealStore()
        val time = FakeTimeProvider(wallClock = 0L, virtualTime = { testScheduler.currentTime })
        val vm = DealTimerViewModel(store, initialTargetEndTimestamp = 10_000L, timeProvider = time)

        val states = mutableListOf<DealTimerUiState>()
        val job = launch { vm.timerFlow().toList(states) }

        runCurrent()
        assertEquals(10_000L, states.last().remainingMillis)

        advanceTimeBy(3_000L.milliseconds)
        runCurrent()
        assertEquals(7_000L, states.last().remainingMillis)
        assertFalse(states.last().isExpired)

        job.cancel()
    }

    @Test
    fun `reaches zero and reports expired`() = runTest {
        val store = FakeDealStore()
        val time = FakeTimeProvider(wallClock = 0L, virtualTime = { testScheduler.currentTime })
        val vm = DealTimerViewModel(store, initialTargetEndTimestamp = 5_000L, timeProvider = time)

        // The flow completes once it expires; runTest auto-advances the delays.
        val states = vm.timerFlow().toList()

        assertEquals(0L, states.last().remainingMillis)
        assertTrue(states.last().isExpired)
    }

    @Test
    fun `target already in the past is immediately expired`() = runTest {
        val store = FakeDealStore()
        val time = FakeTimeProvider(wallClock = 100_000L, virtualTime = { testScheduler.currentTime })
        val vm = DealTimerViewModel(store, initialTargetEndTimestamp = 50_000L, timeProvider = time)

        val first = vm.timerFlow().first()

        assertEquals(0L, first.remainingMillis)
        assertTrue(first.isExpired)
    }

    @Test
    fun `changing the wall clock after start does not change the countdown`() = runTest {
        val store = FakeDealStore()
        val time = FakeTimeProvider(wallClock = 0L, virtualTime = { testScheduler.currentTime })
        val vm = DealTimerViewModel(store, initialTargetEndTimestamp = 20_000L, timeProvider = time)

        val states = mutableListOf<DealTimerUiState>()
        val job = launch { vm.timerFlow().toList(states) }

        runCurrent()
        assertEquals(20_000L, states.last().remainingMillis)

        // User jumps their device clock far forward/back: must not affect the monotonic countdown.
        time.wallClock = 1_000_000L
        advanceTimeBy(2_000L.milliseconds)
        runCurrent()
        assertEquals(18_000L, states.last().remainingMillis)

        time.wallClock = -1_000_000L
        advanceTimeBy(2_000L.milliseconds)
        runCurrent()
        assertEquals(16_000L, states.last().remainingMillis)

        job.cancel()
    }

    @Test
    fun `timerState starts ticking when collected and shares one upstream`() = runTest {
        val store = FakeDealStore()
        val time = FakeTimeProvider(wallClock = 0L, virtualTime = { testScheduler.currentTime })
        val vm = DealTimerViewModel(store, initialTargetEndTimestamp = 10_000L, timeProvider = time)

        // Before anyone subscribes, the WhileSubscribed flow stays at its initial value.
        runCurrent()
        assertEquals(DealTimerUiState(), vm.timerState.value)

        // Two collectors should share a single upstream (one save), proving stateIn is wired up.
        val a = launch { vm.timerState.collect {} }
        val b = launch { vm.timerState.collect {} }

        runCurrent()
        assertEquals(10_000L, vm.timerState.value.remainingMillis)

        advanceTimeBy(4_000L.milliseconds)
        runCurrent()
        assertEquals(6_000L, vm.timerState.value.remainingMillis)
        assertEquals("upstream collected once despite two subscribers", 1, store.saveCount)

        a.cancel()
        b.cancel()
    }

    @Test
    fun `reboot - timer survives elapsedRealtime resetting to zero if wall clock is correct`() = runTest {
        val store = FakeDealStore(initial = 50_000L)
        val time = FakeTimeProvider(wallClock = 20_000L, virtualTime = { testScheduler.currentTime })

        // Before reboot: elapsedRealtime is 100_000
        time.elapsedBase = 100_000L
        val vmBefore = DealTimerViewModel(store, initialTargetEndTimestamp = 0L, timeProvider = time)
        val stateBefore = vmBefore.timerFlow().first()
        assertEquals(30_000L, stateBefore.remainingMillis) // 50k - 20k

        // Simulate Reboot: elapsedRealtime resets to 0. wallClock remains correct (network sync).
        time.elapsedBase = 0L
        val vmAfter = DealTimerViewModel(store, initialTargetEndTimestamp = 0L, timeProvider = time)
        val stateAfter = vmAfter.timerFlow().first()
        assertEquals(30_000L, stateAfter.remainingMillis) // Still 30s remaining
    }

    @Test
    fun `configuration change - flow keeps ticking within subscription timeout`() = runTest {
        val store = FakeDealStore()
        val time = FakeTimeProvider(wallClock = 0L, virtualTime = { testScheduler.currentTime })
        // Use a short subscription timeout for testing if we wanted, but we use the default 5s
        val vm = DealTimerViewModel(store, initialTargetEndTimestamp = 20_000L, timeProvider = time)

        // 1. First collection (Activity starts)
        val states = mutableListOf<DealTimerUiState>()
        val job1 = launch { vm.timerState.toList(states) }
        runCurrent()
        assertEquals(20_000L, states.last().remainingMillis)

        advanceTimeBy(2_000L.milliseconds)
        runCurrent()
        assertEquals(18_000L, states.last().remainingMillis)

        // 2. Unsubscribe (Configuration change starts)
        job1.cancel()
        runCurrent()

        // 3. Advance time while no one is listening (Rotation in progress)
        advanceTimeBy(3_000L.milliseconds)
        runCurrent()
        // Upstream should still be running because 3s < 5s (WhileSubscribed timeout)

        // 4. Resubscribe (New Activity instance attached)
        val job2 = launch { vm.timerState.toList(states) }
        runCurrent()

        // The timer should have kept ticking in the background
        assertEquals(15_000L, states.last().remainingMillis)

        job2.cancel()
    }
}
