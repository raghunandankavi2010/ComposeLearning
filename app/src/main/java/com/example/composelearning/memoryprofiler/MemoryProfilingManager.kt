package com.example.composelearning.memoryprofiler

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.ComponentCallbacks2
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.CancellationSignal
import android.os.Debug
import android.os.ProfilingManager
import android.os.ProfilingResult
import android.os.ProfilingTrigger
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.composelearning.memoryprofiler.domain.MemorySnapshot
import com.example.composelearning.memoryprofiler.domain.ProfilingArtifact
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong
import java.util.function.Consumer

/**
 * Thin, lifecycle-agnostic wrapper around the platform [ProfilingManager]
 * (`android.os.ProfilingManager`) introduced in Android 15 (API 35) and
 * extended with **trigger-based capture** in Android 16 "Baklava" (API 36).
 *
 * ### Why a wrapper?
 * - Keeps every `@RequiresApi` platform touch-point in one place so the
 *   ViewModel and UI stay API-level agnostic.
 * - Exposes cold [Flow]s ([memorySnapshots], [profilingResults],
 *   [memoryPressure]) whose listeners/executors are torn down in `awaitClose`.
 *   Because the flows are cold, their registration is bound to whatever scope
 *   collects them (here, `viewModelScope`) — there is **no** way to leak a
 *   callback past the collector's lifetime.
 * - Never holds an `Activity`/UI `Context`; only `applicationContext`.
 *
 * ### API naming reality check
 * The platform ships exactly two trigger types today —
 * [ProfilingTrigger.TRIGGER_TYPE_ANR] (fired on an ANR "anomaly") and
 * [ProfilingTrigger.TRIGGER_TYPE_APP_FULLY_DRAWN]. There is **no**
 * `TRIGGER_TYPE_OOM`; the out-of-memory / low-memory story is instead handled
 * reactively via [ComponentCallbacks2.onTrimMemory] plus an on-demand
 * `PROFILING_TYPE_JAVA_HEAP_DUMP` — see [memoryPressure] and
 * [requestJavaHeapDump].
 */
class MemoryProfilingManager(context: Context) {

    /** Application context only — safe to retain for the process lifetime. */
    private val appContext: Context = context.applicationContext

    private val activityManager: ActivityManager? =
        appContext.getSystemService(ActivityManager::class.java)

    /**
     * The platform service, resolved once. Non-null from API 35 upward. The
     * field type is only a lazily-resolved descriptor, so it never forces a
     * class-load of [ProfilingManager] on devices that predate it; the actual
     * `getSystemService` call is behind the [isProfilingRuntimeAvailable] guard.
     */
    @SuppressLint("NewApi") // Guarded by isProfilingRuntimeAvailable (SDK_INT >= 35).
    private val profilingManager: ProfilingManager? =
        if (isProfilingRuntimeAvailable) {
            appContext.getSystemService(ProfilingManager::class.java)
        } else {
            null
        }

    /** Stable, ever-increasing id source for emitted [ProfilingArtifact]s. */
    private val artifactIds = AtomicLong(0L)

    /**
     * FIFO of origins for requests we initiated, awaiting their result callback.
     * Thread-safe because it is written from coroutine dispatchers and read from
     * the background results executor.
     */
    private val pendingOrigins = ConcurrentLinkedQueue<ProfilingArtifact.Origin>()

    /** True when the base `requestProfiling`/result-callback API exists (API 35+). */
    val isProfilingServiceAvailable: Boolean get() = profilingManager != null

    /** True when trigger-based capture (`addProfilingTriggers`) exists (Baklava+). */
    val isTriggerApiAvailable: Boolean get() = isBaklavaOrHigher && profilingManager != null

    // ─────────────────────────────────────────────────────────────────────────
    // Metrics
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Reads the current memory picture. Pure and allocation-light — the single
     * [ActivityManager.MemoryInfo] it needs is stack-local, not cached, so this
     * is safe to call from any thread. Callers should still invoke it off the
     * main thread (see [memorySnapshots]).
     */
    fun readSnapshot(): MemorySnapshot {
        val runtime = Runtime.getRuntime()
        val max = runtime.maxMemory()
        val allocated = runtime.totalMemory() - runtime.freeMemory()

        val info = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(info)

        return MemorySnapshot(
            capturedAtMs = System.currentTimeMillis(),
            heapAllocatedBytes = allocated,
            heapMaxBytes = max,
            heapFreeBytes = (max - allocated).coerceAtLeast(0L),
            nativeAllocatedBytes = Debug.getNativeHeapAllocatedSize(),
            systemAvailableBytes = info.availMem,
            systemThresholdBytes = info.threshold,
            systemLowMemory = info.lowMemory
        )
    }

    /**
     * Cold flow that samples [readSnapshot] every [intervalMs] on
     * [Dispatchers.Default], so no measurement work ever lands on the main
     * thread. Emission stops automatically when the collector is cancelled.
     */
    fun memorySnapshots(intervalMs: Long = DEFAULT_POLL_INTERVAL_MS): Flow<MemorySnapshot> =
        flow {
            while (currentCoroutineContext().isActive) {
                emit(readSnapshot())
                delay(intervalMs)
            }
        }.flowOn(Dispatchers.Default)

    // ─────────────────────────────────────────────────────────────────────────
    // Profiling results (persistent callback → cold flow)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Streams every [ProfilingResult] the system hands us — whether it came from
     * a registered trigger or an on-demand [requestJavaHeapDump]. Backed by
     * [ProfilingManager.registerForAllProfilingResults].
     *
     * The callback executor is a dedicated single background thread, so the disk
     * probe ([File.length]) that turns a result into a [ProfilingArtifact] never
     * runs on the main thread. Both the listener and the executor are released in
     * [awaitClose]; there is nothing to leak.
     *
     * Guarded to Baklava because the surrounding trigger workflow is; the base
     * callback API itself is API 35.
     */
    @SuppressLint("NewApi") // Guarded by isProfilingServiceAvailable; SDK_INT_FULL check is inside registerResultsInternal.
    fun profilingResults(ioDispatcher: CoroutineDispatcher = Dispatchers.IO): Flow<ProfilingArtifact> {
        if (!isProfilingServiceAvailable) return emptyFlow() // nothing to stream on legacy OS
        return registerResultsInternal().flowOn(ioDispatcher)
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    private fun registerResultsInternal(): Flow<ProfilingArtifact> = callbackFlow {
        val manager = profilingManager ?: run { close(); return@callbackFlow }

        // Dedicated background executor => result post-processing (file I/O) is
        // guaranteed off the main thread.
        val callbackExecutor = Executors.newSingleThreadExecutor { runnable ->
            Thread(runnable, "profiling-results").apply { isDaemon = true }
        }

        val listener = Consumer<ProfilingResult> { result ->
            trySend(result.toArtifact())
        }

        manager.registerForAllProfilingResults(callbackExecutor, listener)

        awaitClose {
            // Strict teardown: unregister first, then stop the executor.
            manager.unregisterForAllProfilingResults(listener)
            callbackExecutor.shutdownNow()
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Memory pressure (ComponentCallbacks2 → cold flow) — our "OOM" substitute
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Emits `onTrimMemory` levels as the framework signals memory pressure. This
     * is the closest real analogue to a "TRIGGER_TYPE_OOM": the ViewModel reacts
     * to a *critical* level by firing an on-demand heap dump, giving you a
     * post-mortem artifact right as the process approaches its memory limit.
     */
    fun memoryPressure(): Flow<Int> = callbackFlow {
        val callbacks = object : ComponentCallbacks2 {
            override fun onTrimMemory(level: Int) {
                trySend(level)
            }

            override fun onConfigurationChanged(newConfig: Configuration) = Unit

            @Deprecated("Deprecated in Java")
            override fun onLowMemory() {
                trySend(ComponentCallbacks2.TRIM_MEMORY_COMPLETE)
            }
        }
        appContext.registerComponentCallbacks(callbacks)
        awaitClose { appContext.unregisterComponentCallbacks(callbacks) }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Trigger registration (Baklava+)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Registers the event-driven system triggers: an ANR "anomaly" trigger and
     * an app-fully-drawn trigger, each rate-limited so the OS won't spam dumps.
     * Runs on [Dispatchers.IO] because binding into the system service can touch
     * disk/IPC. Returns [Result] so the ViewModel can surface success/failure as
     * UI state rather than throwing.
     */
    @SuppressLint("NewApi") // isTriggerApiAvailable performs the SDK_INT_FULL >= BAKLAVA guard.
    suspend fun registerTriggers(): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isTriggerApiAvailable) {
            return@withContext Result.failure(
                UnsupportedOperationException("Trigger-based profiling requires Android 16 (Baklava, API 36)+")
            )
        }
        runCatching { addTriggersInternal() }
    }

    @RequiresApi(Build.VERSION_CODES.BAKLAVA)
    private fun addTriggersInternal() {
        val manager = requireNotNull(profilingManager)
        val triggers = listOf(
            // "Anomaly" → ANR. Fired when the app becomes unresponsive.
            ProfilingTrigger.Builder(ProfilingTrigger.TRIGGER_TYPE_ANR)
                .setRateLimitingPeriodHours(1)
                .build(),
            // Cold-start "fully drawn" checkpoint.
            ProfilingTrigger.Builder(ProfilingTrigger.TRIGGER_TYPE_APP_FULLY_DRAWN)
                .setRateLimitingPeriodHours(1)
                .build()
        )
        manager.addProfilingTriggers(triggers)
        Log.d(TAG, "Registered ${triggers.size} profiling triggers")
    }

    /** Removes all previously registered triggers. Safe to call on any OS. */
    @SuppressLint("NewApi") // isTriggerApiAvailable performs the SDK_INT_FULL >= BAKLAVA guard.
    fun clearTriggers() {
        if (!isTriggerApiAvailable) return
        clearTriggersInternal()
    }

    @RequiresApi(Build.VERSION_CODES.BAKLAVA)
    private fun clearTriggersInternal() {
        profilingManager?.clearProfilingTriggers()
        Log.d(TAG, "Cleared profiling triggers")
    }

    // ─────────────────────────────────────────────────────────────────────────
    // On-demand capture (API 35+)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Fires an immediate `PROFILING_TYPE_JAVA_HEAP_DUMP`. The resulting artifact
     * arrives asynchronously on the [profilingResults] stream (that is where the
     * single, persistent result callback lives), so this only kicks the request.
     *
     * @param origin how the resulting artifact should be labelled once it lands
     * on the [profilingResults] stream. Enqueued FIFO because the platform
     * [ProfilingResult] does not expose the request tag back to us.
     */
    @SuppressLint("NewApi") // Guarded by isProfilingServiceAvailable / isTriggerApiAvailable at the call site.
    suspend fun requestJavaHeapDump(
        origin: ProfilingArtifact.Origin = ProfilingArtifact.Origin.ON_DEMAND_HEAP_DUMP,
        cancellationSignal: CancellationSignal? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val manager = profilingManager
            ?: return@withContext Result.failure(
                UnsupportedOperationException("ProfilingManager requires Android 15 (API 35)+")
            )
        runCatching {
            // Record the expected provenance before firing; the shared result
            // callback dequeues it to label the artifact (best-effort FIFO).
            pendingOrigins.offer(origin)
            manager.requestProfiling(
                ProfilingManager.PROFILING_TYPE_JAVA_HEAP_DUMP,
                /* params = */ null,
                /* tag = */ TAG_ON_DEMAND,
                /* cancellationSignal = */ cancellationSignal,
                // The persistent registerForAllProfilingResults callback receives
                // the result, so no per-request executor/listener is needed here.
                /* executor = */ null,
                /* listener = */ null
            )
            Log.d(TAG, "Requested on-demand Java heap dump (origin=$origin)")
            Unit
        }.onFailure { pendingOrigins.poll() /* roll back on failure to submit */ }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Mapping helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Converts a platform [ProfilingResult] into our immutable domain model.
     * Runs on the background results executor, so the [File.length] disk probe
     * is off the main thread.
     */
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    private fun ProfilingResult.toArtifact(): ProfilingArtifact {
        val path = resultFilePath
        val size = path?.let { runCatching { File(it).length() }.getOrDefault(0L) } ?: 0L
        return ProfilingArtifact(
            id = artifactIds.incrementAndGet(),
            filePath = path,
            fileSizeBytes = size,
            errorCode = errorCode,
            errorMessage = errorMessage,
            receivedAtMs = System.currentTimeMillis(),
            // A queued origin means we requested this; nothing queued => it came
            // from a system trigger (ANR / fully-drawn) we did not initiate.
            origin = pendingOrigins.poll() ?: ProfilingArtifact.Origin.UNKNOWN
        )
    }

    companion object {
        private const val TAG = "MemoryProfiling"

        private const val TAG_ON_DEMAND = "compose-learning-on-demand"

        private const val DEFAULT_POLL_INTERVAL_MS = 1_000L

        /** `ProfilingManager.getSystemService` exists from API 35 (VanillaIceCream). */
        private val isProfilingRuntimeAvailable: Boolean
            get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM

        /**
         * The Android 16 "Baklava" guard, expressed with the minor-version-aware
         * [Build.VERSION.SDK_INT_FULL] as requested. Equivalent to
         * `SDK_INT >= 36` on the current platform, but future-proof against SDK
         * minor bumps.
         */
        val isBaklavaOrHigher: Boolean
            get() = Build.VERSION.SDK_INT_FULL >= Build.VERSION_CODES_FULL.BAKLAVA
    }
}
