package com.example.composelearning.memoryprofiler.domain

import androidx.compose.runtime.Immutable

/**
 * One intercepted `ProfilingResult`, normalized into a UI-friendly, immutable
 * record. Produced on a background executor (the file-system probe of the
 * artifact path runs off the main thread) and appended to the on-screen log.
 */
@Immutable
data class ProfilingArtifact(
    /** Monotonic id so `LazyColumn` keys stay stable across recompositions. */
    val id: Long,
    /** Where the trace / heap-dump was written, or `null` on failure. */
    val filePath: String?,
    /** Size on disk in bytes (0 if the file is gone or the request failed). */
    val fileSizeBytes: Long,
    /** Raw `ProfilingResult.getErrorCode()`; `0` == `ERROR_NONE`. */
    val errorCode: Int,
    /** Human-readable error message when [errorCode] is non-zero. */
    val errorMessage: String?,
    /** Wall-clock time the callback fired. */
    val receivedAtMs: Long,
    /** What produced this artifact — drives the log row's label & color. */
    val origin: Origin
) {
    val isSuccess: Boolean get() = errorCode == ERROR_NONE

    /** Provenance of a profiling artifact. */
    enum class Origin(val label: String) {
        ON_DEMAND_HEAP_DUMP("On-demand heap dump"),
        MEMORY_PRESSURE_DUMP("Low-memory heap dump"),
        TRIGGER_ANR("ANR anomaly trigger"),
        TRIGGER_FULLY_DRAWN("App-fully-drawn trigger"),
        UNKNOWN("System profiling result")
    }

    companion object {
        const val ERROR_NONE = 0
    }
}
