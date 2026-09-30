package com.example.composelearning.visionguard.domain

import androidx.compose.ui.geometry.Rect
import com.example.composelearning.visionguard.presentation.VisionGuardContract.HazardLevel
import com.example.composelearning.visionguard.presentation.VisionGuardContract.ObjectCategory
import com.example.composelearning.visionguard.presentation.VisionGuardContract.SpatialTarget
import kotlin.math.abs
import kotlin.math.sin

object SpatialSonarEngine {

    private const val FOCAL_LENGTH_PIXELS = 800f

    fun estimateDepthAndHazard(
        category: ObjectCategory,
        box: Rect,
        imageHeightPx: Float,
        previousTarget: SpatialTarget? = null,
        deltaTimeSec: Float = 0.1f
    ): SpatialTarget {
        val boxHeightPx = box.height * imageHeightPx
        val estimatedDistanceM = if (boxHeightPx > 10f) {
            (category.typicalHeightMeters * FOCAL_LENGTH_PIXELS / boxHeightPx).coerceIn(0.4f, 10f)
        } else 3.0f

        // Center X = 0.5 is direct center (0m offset)
        val centerX = box.left + box.width / 2f
        val lateralOffsetM = (centerX - 0.5f) * estimatedDistanceM * 1.2f

        // Velocity = delta distance over time
        val velocityMs = if (previousTarget != null && deltaTimeSec > 0.01f) {
            (previousTarget.distanceMeters - estimatedDistanceM) / deltaTimeSec
        } else 0f

        val hazardLevel = when {
            estimatedDistanceM < 1.2f || (estimatedDistanceM < 2.0f && velocityMs > 0.8f) -> HazardLevel.CRITICAL_HAZARD
            estimatedDistanceM < 2.8f || velocityMs > 0.3f -> HazardLevel.APPROACHING
            else -> HazardLevel.SAFE
        }

        return SpatialTarget(
            id = category.name.lowercase(),
            label = category.label,
            category = category,
            distanceMeters = estimatedDistanceM,
            lateralOffsetMeters = lateralOffsetM,
            closingVelocityMs = velocityMs,
            hazardLevel = hazardLevel,
            cameraBoundingBox = box
        )
    }

    fun generateSimulatedTargets(elapsedTimeMs: Long): List<SpatialTarget> {
        val timeSec = elapsedTimeMs / 1000f

        // Simulated approaching chair
        val chairDist = (4.0f - (timeSec % 8f) * 0.45f).coerceAtLeast(0.6f)
        val chairHazard = when {
            chairDist < 1.3f -> HazardLevel.CRITICAL_HAZARD
            chairDist < 2.5f -> HazardLevel.APPROACHING
            else -> HazardLevel.SAFE
        }

        // Simulated person walking across
        val personOffset = sin(timeSec * 0.8f) * 1.5f
        val personDist = 2.4f

        return listOf(
            SpatialTarget(
                id = "chair_1",
                label = "Chair Obstacle",
                category = ObjectCategory.CHAIR,
                distanceMeters = chairDist,
                lateralOffsetMeters = -0.3f,
                closingVelocityMs = 0.45f,
                hazardLevel = chairHazard,
                cameraBoundingBox = Rect(0.35f, 0.45f, 0.65f, 0.80f)
            ),
            SpatialTarget(
                id = "person_1",
                label = "Pedestrian",
                category = ObjectCategory.PERSON,
                distanceMeters = personDist,
                lateralOffsetMeters = personOffset.toFloat(),
                closingVelocityMs = 0.1f,
                hazardLevel = HazardLevel.SAFE,
                cameraBoundingBox = Rect(0.15f, 0.20f, 0.40f, 0.70f)
            ),
            SpatialTarget(
                id = "stairs_1",
                label = "Staircase",
                category = ObjectCategory.STAIRS,
                distanceMeters = 3.8f,
                lateralOffsetMeters = 0.8f,
                closingVelocityMs = 0.0f,
                hazardLevel = HazardLevel.SAFE,
                cameraBoundingBox = Rect(0.60f, 0.30f, 0.90f, 0.65f)
            )
        )
    }
}
