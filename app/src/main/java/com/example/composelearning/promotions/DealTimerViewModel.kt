package com.example.composelearning.promotions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.milliseconds

/**
 * UI state for the deal timer.
 * @param remainingMillis Remaining time in milliseconds.
 * @param isExpired True if the deal has ended.
 * @param targetEndTimestamp The resolved absolute deal end time (epoch millis), or 0L before the
 *   timer has resolved it. This is the single source of truth for the deadline — survives
 *   configuration changes (ViewModel) and process death (DataStore) — so dependent features such as
 *   the background notification stay in sync with the on-screen countdown.
 */
data class DealTimerUiState(
    val remainingMillis: Long = 0L,
    val isExpired: Boolean = false,
    val targetEndTimestamp: Long = 0L
)

/**
 * Drives the countdown for a promotional deal.
 *
 * The countdown is modelled as a **cold flow** ([timerFlow]) that is shared as a [StateFlow] with
 * [SharingStarted.WhileSubscribed]. The upstream work therefore starts automatically when the UI
 * begins collecting (via `collectAsStateWithLifecycle`) and is torn down a few seconds after the
 * last collector goes away. This avoids both an `init`-block side effect and a UI-driven `start()`
 * call (which would have to be triggered from a `LaunchedEffect` whose key never meaningfully
 * changes); recomposition and configuration changes no longer risk re-triggering the work.
 *
 * Testability: the two clocks and the persistence layer are injected ([TimeProvider], [DealStore]),
 * and [timerFlow] is exposed so the time-sensitive logic can be collected and asserted directly on
 * virtual time, without an active subscriber or `viewModelScope`. [tickIntervalMillis] is injectable
 * for the same reason.
 */
class DealTimerViewModel(
    private val dealStore: DealStore,
    private val initialTargetEndTimestamp: Long, // Epoch milliseconds from server
    private val timeProvider: TimeProvider = SystemTimeProvider(),
    private val tickIntervalMillis: Long = 1000L
) : ViewModel() {

    val timerState: StateFlow<DealTimerUiState> =
        timerFlow().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = DealTimerUiState()
        )

    /**
     * Cold countdown stream. Each collection resolves the target end time (restoring it after
     * process death when possible), then emits the remaining time once per [tickIntervalMillis]
     * until the deal expires.
     *
     * Visible (not private) so unit tests can collect it directly.
     */
    internal fun timerFlow(): Flow<DealTimerUiState> = flow {
        // Edge Case 2: Process Death. Fetch from DataStore if exists, otherwise save initial.
        val savedTime = dealStore.targetEndTime.first()
        val targetTime = if (savedTime == null) {
            dealStore.saveTargetEndTime(initialTargetEndTimestamp)
            initialTargetEndTimestamp
        } else {
            savedTime
        }

        // Edge Case 1: Immunity to System Clock changes. Compute the remaining duration once against
        // the wall clock, then track it against monotonic uptime from here on.
        val remainingAtStart = (targetTime - timeProvider.currentTimeMillis()).coerceAtLeast(0L)
        val endElapsedRealtime = timeProvider.elapsedRealtime() + remainingAtStart

        while (true) {
            val remaining = (endElapsedRealtime - timeProvider.elapsedRealtime()).coerceAtLeast(0L)
            emit(
                DealTimerUiState(
                    remainingMillis = remaining,
                    isExpired = remaining <= 0L,
                    targetEndTimestamp = targetTime
                )
            )
            if (remaining <= 0L) break
            delay(tickIntervalMillis.milliseconds)
        }
    }
}
