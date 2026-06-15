package com.example.composelearning.arglasses.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.composelearning.arglasses.data.FaceMeshAnalyzer
import com.example.composelearning.arglasses.domain.ComputeFaceTransformUseCase
import com.example.composelearning.arglasses.domain.model.FaceAnchors
import com.example.composelearning.arglasses.domain.model.FaceTransform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * MVI ViewModel for the AR glasses feature.
 *
 * Owns two distinct kinds of state, on purpose:
 *  - [state] — durable, low-frequency MVI [ArGlassesUiState] (tracking toggle, face-present
 *    flag, errors). Drives the UI chrome.
 *  - [trackedFace] — the high-frequency render transform, held as **Compose snapshot
 *    state**. The overlay reads it in its draw lambda, so a new camera frame invalidates
 *    only the draw phase, never recomposition. (Same rationale the project documents in
 *    `RiveoContract`: ~30/60fps values don't belong in the MVI `StateFlow`.)
 *
 * No Hilt (project convention) — see [Factory].
 */
class ArGlassesViewModel(
    private val computeTransform: ComputeFaceTransformUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(ArGlassesUiState())
    val state: StateFlow<ArGlassesUiState> = _state.asStateFlow()

    var trackedFace by mutableStateOf<FaceTransform?>(null)
        private set

    /** Bound to CameraX's `ImageAnalysis`; callbacks arrive on ML Kit's listener thread. */
    val analyzer = FaceMeshAnalyzer(
        onFace = ::onFaceAnchors,
        onEmpty = ::onFaceLost,
        onError = { error -> _state.update { it.copy(error = error.message ?: "Camera error") } },
    )

    private fun onFaceAnchors(anchors: FaceAnchors) {
        if (!_state.value.trackingEnabled) {
            onFaceLost()
            return
        }
        val transform = computeTransform(anchors)
        trackedFace = transform
        setFaceDetected(transform != null)
    }

    private fun onFaceLost() {
        if (trackedFace != null) trackedFace = null
        setFaceDetected(false)
    }

    /** StateFlow dedups equal values, so this only emits when the flag actually flips. */
    private fun setFaceDetected(detected: Boolean) {
        if (_state.value.faceDetected != detected) {
            _state.update { it.copy(faceDetected = detected) }
        }
    }

    fun onIntent(intent: ArGlassesIntent) {
        when (intent) {
            ArGlassesIntent.ToggleTracking -> {
                val enabled = !_state.value.trackingEnabled
                if (!enabled) trackedFace = null
                _state.update {
                    it.copy(
                        trackingEnabled = enabled,
                        faceDetected = if (enabled) it.faceDetected else false,
                    )
                }
            }
            ArGlassesIntent.ConsumeError -> _state.update { it.copy(error = null) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        analyzer.release()
    }

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ArGlassesViewModel(ComputeFaceTransformUseCase()) as T
    }
}
