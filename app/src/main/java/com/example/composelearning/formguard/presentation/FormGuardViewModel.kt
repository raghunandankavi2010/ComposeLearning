package com.example.composelearning.formguard.presentation

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.composelearning.formguard.data.FormGuardAnalyzer
import com.example.composelearning.formguard.domain.SquatFormEvaluator
import com.example.composelearning.formguard.domain.model.PoseFrame
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

/**
 * ViewModel for the FormGuard squat coach. No Hilt (project convention) — see [Factory].
 *
 * Owns two kinds of state, on purpose (the pattern documented in `ArGlassesViewModel`):
 *  - [uiState] — the durable, low-frequency [PoseUiState] the HUD reacts to (rep count, valgus
 *    flag, ~1°-quantised knee angle, feedback). De-duplicated so a steady pose doesn't churn it.
 *  - [poseFrame] — the high-frequency skeleton, held as **Compose snapshot state**. The overlay
 *    reads it in its draw lambda, so a new camera frame invalidates only the draw phase, never
 *    recomposition.
 *
 * Landmark callbacks arrive on MediaPipe's result-listener thread; both `StateFlow.value` writes
 * and snapshot-state writes are safe from any thread.
 */
class FormGuardViewModel(
    appContext: Context,
) : ViewModel() {

    private val evaluator = SquatFormEvaluator()

    private val _uiState = MutableStateFlow<PoseUiState>(PoseUiState.Initializing)
    val uiState: StateFlow<PoseUiState> = _uiState.asStateFlow()

    /** Latest skeleton for the overlay's draw phase; never routed through [uiState]. */
    var poseFrame by mutableStateOf<PoseFrame?>(null)
        private set

    /** Bound to CameraX's `ImageAnalysis`. */
    val analyzer = FormGuardAnalyzer(
        context = appContext.applicationContext,
        onResult = ::onPose,
        onError = ::onError,
    )

    private fun onPose(frame: PoseFrame?) {
        if (frame == null) {
            // No person in frame: drop the skeleton, but keep the rep count visible.
            if (poseFrame != null) poseFrame = null
            val assessment = evaluatorMiss()
            emitTracking(assessment.kneeAngle, assessment.repCount, assessment.feedbackMessage, false)
            return
        }
        poseFrame = frame
        val a = evaluator.evaluate(frame)
        if (!a.personDetected && poseFrame != null) poseFrame = null
        emitTracking(a.kneeAngle, a.repCount, a.feedbackMessage, a.isKneesCaving)
    }

    /** Re-evaluates "no person" through the same path so feedback/rep state stays consistent. */
    private fun evaluatorMiss() = evaluator.evaluate(EMPTY_FRAME)

    /**
     * Emits a new [PoseUiState.Tracking] only when a *displayed* value actually changes — the knee
     * angle is compared at ~1° granularity. This keeps the single `StateFlow` design from
     * recomposing the HUD on every one of the ~30 frames per second.
     */
    private fun emitTracking(
        kneeAngle: Float,
        repCount: Int,
        feedback: String?,
        caving: Boolean,
    ) {
        val current = _uiState.value
        val changed = current !is PoseUiState.Tracking ||
            current.repCount != repCount ||
            current.isKneesCaving != caving ||
            current.feedbackMessage != feedback ||
            abs(current.kneeAngle - kneeAngle) >= 1f
        if (changed) {
            _uiState.value = PoseUiState.Tracking(
                kneeAngle = kneeAngle,
                repCount = repCount,
                feedbackMessage = feedback,
                isKneesCaving = caving,
            )
        }
    }

    private fun onError(error: Throwable) {
        _uiState.value = PoseUiState.Error(error.message ?: "Camera/AI error")
    }

    /** Clears the rep count back to zero (UI "reset" action). */
    fun resetReps() {
        evaluator.reset()
        emitTracking(0f, 0, null, false)
    }

    override fun onCleared() {
        super.onCleared()
        analyzer.release()
    }

    class Factory(private val appContext: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            FormGuardViewModel(appContext.applicationContext) as T
    }

    private companion object {
        /** Reused empty frame so the "no person" path allocates nothing. */
        val EMPTY_FRAME = PoseFrame(emptyList(), 0, 0)
    }
}
