package com.example.composelearning.memoryprofiler.presentation

import androidx.compose.runtime.Immutable
import com.example.composelearning.memoryprofiler.domain.MemorySnapshot
import com.example.composelearning.memoryprofiler.domain.ProfilingArtifact

/**
 * MVI contract for the Memory Profiler dashboard.
 *
 * Three strictly separated channels of the unidirectional data flow:
 *  - [MemoryProfilerUiState] — the single, immutable snapshot the UI renders.
 *  - [MemoryProfilerIntent]  — user/system inputs travelling *into* the ViewModel.
 *  - [MemoryProfilerEffect]  — one-shot side effects travelling *out* (snackbars).
 */

/** Availability of the Android 16 (Baklava) trigger-profiling subsystem. */
@Immutable
sealed interface GuardStatus {
    /** Still resolving the platform capability (first frame). */
    data object Checking : GuardStatus

    /** Running below Baklava (or the service is missing): show a graceful fallback. */
    @Immutable
    data class Unsupported(val reason: String) : GuardStatus

    /** Trigger API is present and usable. */
    data object Ready : GuardStatus
}

@Immutable
data class MemoryProfilerUiState(
    val guard: GuardStatus = GuardStatus.Checking,
    /** True from API 35+: the base ProfilingManager (and on-demand dumps) exists. */
    val profilingServiceAvailable: Boolean = false,
    /** Whether ANR/fully-drawn triggers are currently registered & "bound". */
    val triggersRegistered: Boolean = false,
    /** Latest off-main-thread memory reading. */
    val snapshot: MemorySnapshot = MemorySnapshot.EMPTY,
    /** Newest-first log of intercepted profiling artifacts. */
    val artifacts: List<ProfilingArtifact> = emptyList(),
    /** True while the controlled high-allocation ballast is held in memory. */
    val isSimulatingSpike: Boolean = false,
    /** Approx. bytes of ballast currently retained by the spike simulation. */
    val spikeHeldBytes: Long = 0L,
    /** Human-readable label of the last `onTrimMemory` level observed, if any. */
    val lastMemoryPressure: String? = null
) {
    /** Convenience: is the "guard service" banner green? */
    val isGuardServiceActive: Boolean
        get() = guard is GuardStatus.Ready && triggersRegistered
}

/** All inputs into the ViewModel. */
@Immutable
sealed interface MemoryProfilerIntent {
    /** Bind + register the event-driven system triggers (the "guard service"). */
    data object RegisterGuardService : MemoryProfilerIntent

    /** Unregister all triggers. */
    data object UnregisterGuardService : MemoryProfilerIntent

    /** Allocate controlled ballast to push heap usage toward the OS limit. */
    data object SimulateMemorySpike : MemoryProfilerIntent

    /** Release the ballast and hint a GC. */
    data object ReleaseMemorySpike : MemoryProfilerIntent

    /** Manually request an immediate Java heap dump. */
    data object TriggerOnDemandDump : MemoryProfilerIntent

    /** Clear the on-screen artifact log. */
    data object ClearLog : MemoryProfilerIntent
}

/** One-shot side effects. Delivered via a Channel so each is consumed once. */
@Immutable
sealed interface MemoryProfilerEffect {
    data class ShowMessage(val text: String) : MemoryProfilerEffect
}
