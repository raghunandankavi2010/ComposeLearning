package com.example.composelearning.visionguard.presentation

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Rect

object VisionGuardContract {

    enum class HazardLevel {
        SAFE, // Green
        APPROACHING, // Yellow
        CRITICAL_HAZARD // Red
    }

    enum class ObjectCategory(val label: String, val typicalHeightMeters: Float) {
        PERSON("Person", 1.7f),
        CHAIR("Chair / Seat", 0.9f),
        TABLE("Table / Desk", 0.75f),
        STAIRS("Stairs / Steps", 1.2f),
        DOOR("Doorway", 2.0f),
        VEHICLE("Vehicle / Bike", 1.5f),
        OBSTACLE("General Hazard", 0.5f)
    }

    @Immutable
    data class SpatialTarget(
        val id: String,
        val label: String,
        val category: ObjectCategory,
        val distanceMeters: Float, // Estimated distance in meters
        val lateralOffsetMeters: Float, // Negative = left, positive = right
        val closingVelocityMs: Float, // Velocity towards user in m/s
        val hazardLevel: HazardLevel,
        val cameraBoundingBox: Rect, // Normalized 0..1 bounding box
        val confidence: Float = 0.88f
    )

    @Immutable
    data class State(
        val isCameraPermissionGranted: Boolean = false,
        val isSimulationMode: Boolean = true,
        val isTtsEnabled: Boolean = true,
        val targets: List<SpatialTarget> = emptyList(),
        val primaryHazard: SpatialTarget? = null,
        val latestVoiceAlert: String = "Spatial Radar Active. Scanning for hazards.",
        val selectedTargetId: String? = null
    )

    sealed interface Intent {
        data class PermissionResult(val isGranted: Boolean) : Intent
        data object ToggleSimulationMode : Intent
        data object ToggleTts : Intent
        data class SelectTarget(val targetId: String?) : Intent
        data object ClearHazards : Intent
    }
}
