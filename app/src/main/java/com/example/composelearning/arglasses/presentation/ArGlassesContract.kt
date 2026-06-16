package com.example.composelearning.arglasses.presentation

import androidx.compose.runtime.Immutable

/**
 * Durable MVI state for the AR glasses screen.
 *
 * Deliberately **excludes** the per-frame [com.example.composelearning.arglasses.domain.model.FaceTransform]:
 * that arrives ~30 times a second and is pure render data, so it lives in
 * [ArGlassesViewModel.trackedFace] as Compose snapshot state (read in the overlay's draw
 * phase) instead of here. Routing it through this `StateFlow` would recompose the whole
 * screen on every camera frame. This state holds only the coarse, occasionally-changing
 * flags the UI chrome reacts to.
 */
@Immutable
data class ArGlassesUiState(
    val trackingEnabled: Boolean = true,
    val faceDetected: Boolean = false,
    val selectedStyleId: String = GlassesCatalog.default.id,
    val error: String? = null,
)

/** Unidirectional events flowing UI → ViewModel. */
sealed interface ArGlassesIntent {
    data object ToggleTracking : ArGlassesIntent
    data object ConsumeError : ArGlassesIntent
    data class SelectStyle(val styleId: String) : ArGlassesIntent
}
