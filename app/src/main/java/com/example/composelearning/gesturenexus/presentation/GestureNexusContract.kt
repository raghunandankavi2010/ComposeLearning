package com.example.composelearning.gesturenexus.presentation

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

object GestureNexusContract {

    enum class HandMode {
        AIR_CANVAS_DRAWING,
        ASL_SIGN_TRANSLATOR
    }

    enum class DetectedGesture(val label: String) {
        PINCH_DRAW("Pinch to Draw"),
        OPEN_PALM_ERASE("Open Palm Erase"),
        THUMBS_UP("Thumbs Up!"),
        PEACE_SIGN("Peace / Victory (V)"),
        CLOSED_FIST("Fist / Clear"),
        ASL_LETTER_A("Sign Letter 'A'"),
        ASL_LETTER_B("Sign Letter 'B'"),
        ASL_LETTER_C("Sign Letter 'C'"),
        UNKNOWN("Scanning Hand Gestures...")
    }

    @Immutable
    data class HandLandmarkPoint(
        val x: Float, // 0..1 normalized
        val y: Float, // 0..1 normalized
        val z: Float = 0f
    )

    @Immutable
    data class StrokePath(
        val points: List<Offset>,
        val color: Color,
        val strokeWidth: Float = 12f
    )

    @Immutable
    data class State(
        val isCameraPermissionGranted: Boolean = false,
        val isSimulationMode: Boolean = true,
        val handMode: HandMode = HandMode.AIR_CANVAS_DRAWING,
        val handLandmarks: Map<Int, HandLandmarkPoint> = emptyMap(),
        val currentGesture: DetectedGesture = DetectedGesture.PINCH_DRAW,
        val drawnStrokes: List<StrokePath> = emptyList(),
        val activeColor: Color = Color(0xFF00E5FF),
        val transcribedText: String = "",
        val confidenceScore: Float = 0.94f
    )

    sealed interface Intent {
        data class PermissionResult(val isGranted: Boolean) : Intent
        data class SelectMode(val mode: HandMode) : Intent
        data class SelectColor(val color: Color) : Intent
        data object ClearCanvas : Intent
        data object SpeakTranscribedText : Intent
        data object ToggleSimulationMode : Intent
    }
}
