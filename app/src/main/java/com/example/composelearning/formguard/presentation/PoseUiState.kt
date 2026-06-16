package com.example.composelearning.formguard.presentation

import androidx.compose.runtime.Immutable

/**
 * The single source of UI truth for the FormGuard screen, exposed as `StateFlow<PoseUiState>`.
 *
 * Deliberately **excludes** the raw per-frame skeleton ([com.example.composelearning.formguard.domain.model.PoseFrame]):
 * that arrives ~30×/second and is pure render data, so it lives in
 * [FormGuardViewModel.poseFrame] as Compose snapshot state (read in the overlay's draw phase)
 * instead of here. Routing it through this `StateFlow` would recompose the whole screen on every
 * camera frame. [Tracking] carries only the coarse, de-duplicated values the HUD reacts to (the
 * knee angle is quantised to ~1° before it can trigger a new emission).
 */
sealed interface PoseUiState {

    /** Camera permission granted; landmarker spinning up, no frame analysed yet. */
    @Immutable
    data object Initializing : PoseUiState

    /** The user has not yet granted the camera permission. */
    @Immutable
    data object CameraPermissionRequired : PoseUiState

    /** Live analysis is running. */
    @Immutable
    data class Tracking(
        val kneeAngle: Float,
        val repCount: Int,
        val feedbackMessage: String?,
        val isKneesCaving: Boolean,
    ) : PoseUiState

    /** Unrecoverable setup failure (e.g. the model asset is missing). */
    @Immutable
    data class Error(val errorMsg: String) : PoseUiState
}
