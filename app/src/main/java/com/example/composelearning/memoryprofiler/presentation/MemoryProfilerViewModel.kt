package com.example.composelearning.memoryprofiler.presentation

import android.content.ComponentCallbacks2
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.composelearning.memoryprofiler.MemoryProfilingManager
import com.example.composelearning.memoryprofiler.domain.ProfilingArtifact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UDF/MVI ViewModel for the Memory Profiler dashboard.
 *
 * Owns exactly one [MemoryProfilerUiState] ([StateFlow]) and one one-shot
 * effect channel. Every source of change — the 1 Hz memory sampler, the
 * persistent profiling-result callback, and framework memory-pressure signals —
 * is collected inside [viewModelScope], so all of them (and their underlying
 * platform listeners/executors) tear down deterministically when the ViewModel
 * is cleared. Nothing is collected in, or leaks into, the composition.
 */
class MemoryProfilerViewModel(
    private val profiling: MemoryProfilingManager
) : ViewModel() {

    private val _state = MutableStateFlow(MemoryProfilerUiState())
    val state: StateFlow<MemoryProfilerUiState> = _state.asStateFlow()

    // Channel (not SharedFlow) so each side effect is delivered exactly once,
    // even across configuration changes / brief UI detachment.
    private val _effects = Channel<MemoryProfilerEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    /**
     * Controlled high-allocation "ballast". Held as a field (not a local) so the
     * GC cannot reclaim it while the spike is active. `@Volatile` because it is
     * written from a background dispatcher and read from [onCleared] on main.
     */
    @Volatile
    private var spikeBallast: MutableList<ByteArray>? = null

    init {
        resolveGuardStatus()
        observeMemory()
        observeProfilingResults()
        observeMemoryPressure()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Intent handling (single entry point)
    // ─────────────────────────────────────────────────────────────────────────

    fun onIntent(intent: MemoryProfilerIntent) {
        when (intent) {
            MemoryProfilerIntent.RegisterGuardService -> registerGuardService()
            MemoryProfilerIntent.UnregisterGuardService -> unregisterGuardService()
            MemoryProfilerIntent.SimulateMemorySpike -> simulateMemorySpike()
            MemoryProfilerIntent.ReleaseMemorySpike -> releaseMemorySpike()
            MemoryProfilerIntent.TriggerOnDemandDump -> triggerOnDemandDump()
            MemoryProfilerIntent.ClearLog -> _state.update { it.copy(artifacts = emptyList()) }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Observers
    // ─────────────────────────────────────────────────────────────────────────

    private fun resolveGuardStatus() {
        val guard = when {
            profiling.isTriggerApiAvailable -> GuardStatus.Ready
            profiling.isProfilingServiceAvailable -> GuardStatus.Unsupported(
                "Trigger-based profiling needs Android 16 (Baklava, API 36)+. " +
                    "On-demand heap dumps are still available on this device."
            )
            else -> GuardStatus.Unsupported(
                "ProfilingManager needs Android 15 (API 35)+. Showing live metrics only."
            )
        }
        _state.update {
            it.copy(guard = guard, profilingServiceAvailable = profiling.isProfilingServiceAvailable)
        }
    }

    /** 1 Hz off-main-thread memory sampler → state.snapshot. */
    private fun observeMemory() {
        profiling.memorySnapshots()
            .onEach { snapshot -> _state.update { it.copy(snapshot = snapshot) } }
            .launchIn(viewModelScope)
    }

    /** Persistent result callback → prepend to the log + notify. */
    private fun observeProfilingResults() {
        profiling.profilingResults()
            .onEach { artifact ->
                _state.update { current ->
                    current.copy(
                        // Newest first; cap the visible history so the list can't grow unbounded.
                        artifacts = (listOf(artifact) + current.artifacts).take(MAX_LOG_ENTRIES)
                    )
                }
                val message = if (artifact.isSuccess) {
                    "Captured ${artifact.origin.label} → ${artifact.fileSizeBytes / 1024} KB"
                } else {
                    "Profiling failed (code ${artifact.errorCode})"
                }
                _effects.send(MemoryProfilerEffect.ShowMessage(message))
            }
            .launchIn(viewModelScope)
    }

    /** Framework memory pressure → label + auto heap dump on critical levels. */
    private fun observeMemoryPressure() {
        profiling.memoryPressure()
            .onEach { level ->
                _state.update { it.copy(lastMemoryPressure = level.toPressureLabel()) }
                // Only the genuinely critical levels warrant a post-mortem dump.
                // (Levels like UI_HIDDEN/BACKGROUND fire on every backgrounding
                // and must NOT trigger a capture.)
                val isCritical = level == ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL ||
                    level == ComponentCallbacks2.TRIM_MEMORY_COMPLETE
                if (isCritical && profiling.isProfilingServiceAvailable) {
                    // The real "OOM post-mortem": grab a heap dump as we near the limit.
                    profiling.requestJavaHeapDump(ProfilingArtifact.Origin.MEMORY_PRESSURE_DUMP)
                }
            }
            .launchIn(viewModelScope)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Actions
    // ─────────────────────────────────────────────────────────────────────────

    private fun registerGuardService() {
        viewModelScope.launch {
            profiling.registerTriggers()
                .onSuccess {
                    _state.update { it.copy(triggersRegistered = true) }
                    _effects.send(MemoryProfilerEffect.ShowMessage("Guard service bound — ANR & fully-drawn triggers active"))
                }
                .onFailure { error ->
                    _effects.send(MemoryProfilerEffect.ShowMessage(error.message ?: "Could not register triggers"))
                }
        }
    }

    private fun unregisterGuardService() {
        profiling.clearTriggers()
        _state.update { it.copy(triggersRegistered = false) }
        viewModelScope.launch {
            _effects.send(MemoryProfilerEffect.ShowMessage("Guard service unbound"))
        }
    }

    private fun triggerOnDemandDump() {
        viewModelScope.launch {
            profiling.requestJavaHeapDump()
                .onSuccess {
                    _effects.send(MemoryProfilerEffect.ShowMessage("On-demand heap dump requested…"))
                }
                .onFailure { error ->
                    _effects.send(MemoryProfilerEffect.ShowMessage(error.message ?: "Dump request failed"))
                }
        }
    }

    /**
     * Allocates ~half of the current managed-heap headroom in 4 MB chunks,
     * stopping early (and gracefully) if we hit [OutOfMemoryError] first. The
     * ballast is retained in [spikeBallast] so the metrics visibly climb until
     * the user releases it.
     */
    private fun simulateMemorySpike() {
        if (spikeBallast != null) {
            viewModelScope.launch {
                _effects.send(MemoryProfilerEffect.ShowMessage("Spike already active — release it first"))
            }
            return
        }
        // Allocation runs off the main thread; retention is what matters, not the thread.
        viewModelScope.launch(Dispatchers.Default) {
            val ballast = ArrayList<ByteArray>()
            val budget = (profiling.readSnapshot().heapFreeBytes * SPIKE_HEADROOM_FRACTION).toLong()
            var held = 0L
            try {
                while (held < budget) {
                    val block = ByteArray(SPIKE_CHUNK_BYTES)
                    block[0] = 1 // touch a page so the allocation is actually committed
                    ballast.add(block)
                    held += SPIKE_CHUNK_BYTES
                }
            } catch (_: OutOfMemoryError) {
                // Hit the hard limit before the budget — exactly the edge case
                // we want to exercise. Keep whatever we managed to hold.
            }
            spikeBallast = ballast
            _state.update { it.copy(isSimulatingSpike = true, spikeHeldBytes = held) }
            _effects.send(
                MemoryProfilerEffect.ShowMessage("Spike active: holding ${held / BYTES_PER_MB} MB")
            )
        }
    }

    private fun releaseMemorySpike() {
        val wasActive = spikeBallast != null
        spikeBallast = null
        @Suppress("ExplicitGarbageCollectionCall") // Deliberate for the demo: make the drop visible immediately.
        System.gc()
        _state.update { it.copy(isSimulatingSpike = false, spikeHeldBytes = 0L) }
        if (wasActive) {
            viewModelScope.launch {
                _effects.send(MemoryProfilerEffect.ShowMessage("Ballast released — GC hinted"))
            }
        }
    }

    override fun onCleared() {
        // Deterministic teardown. Flow collectors are cancelled by viewModelScope,
        // which unregisters the platform listeners/executors via their awaitClose.
        profiling.clearTriggers()
        spikeBallast = null
        super.onCleared()
    }

    /**
     * Builds the ViewModel with a process-scoped [MemoryProfilingManager]. Uses
     * `applicationContext` so the manager never captures an `Activity`.
     */
    class Factory(context: Context) : ViewModelProvider.Factory {
        private val appContext = context.applicationContext

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MemoryProfilerViewModel(MemoryProfilingManager(appContext)) as T
    }

    private companion object {
        const val MAX_LOG_ENTRIES = 50
        const val BYTES_PER_MB = 1024 * 1024
        const val SPIKE_CHUNK_BYTES = 4 * 1024 * 1024 // 4 MB
        const val SPIKE_HEADROOM_FRACTION = 0.5
    }
}

/** Maps an `onTrimMemory` level constant to a short human-readable label. */
private fun Int.toPressureLabel(): String = when (this) {
    ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> "COMPLETE (about to be killed)"
    ComponentCallbacks2.TRIM_MEMORY_MODERATE -> "MODERATE"
    ComponentCallbacks2.TRIM_MEMORY_BACKGROUND -> "BACKGROUND"
    ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> "UI_HIDDEN"
    ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> "RUNNING_CRITICAL"
    ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW -> "RUNNING_LOW"
    ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE -> "RUNNING_MODERATE"
    else -> "level $this"
}
