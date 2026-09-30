package com.example.composelearning.visionguard.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.composelearning.visionguard.presentation.VisionGuardContract.HazardLevel
import com.example.composelearning.visionguard.presentation.VisionGuardContract.SpatialTarget
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SpatialSonarCanvas(
    targets: List<SpatialTarget>,
    sweepAngleDeg: Float,
    selectedTargetId: String?,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val originX = w / 2f
        val originY = h * 0.85f // User position at bottom center of radar
        val maxRadiusPx = h * 0.75f // Represents 5 meters

        // Distance Scale: 5 meters = maxRadiusPx
        fun distanceToPx(distM: Float): Float = (distM / 5.0f * maxRadiusPx).coerceAtMost(maxRadiusPx)

        // Concentric Distance Rings (1m, 2m, 3m, 5m)
        val ringsM = listOf(1f, 2f, 3f, 5f)
        for (ringM in ringsM) {
            val r = distanceToPx(ringM)
            drawCircle(
                color = Color(0xFF00E5FF).copy(alpha = 0.25f),
                radius = r,
                center = Offset(originX, originY),
                style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f))
            )
        }

        // Tactical Field Lines (30°, 60°, 90°, 120°, 150°)
        val anglesDeg = listOf(30f, 60f, 90f, 120f, 150f)
        for (angleDeg in anglesDeg) {
            val rad = Math.toRadians(angleDeg.toDouble())
            val endX = originX + maxRadiusPx * cos(rad).toFloat()
            val endY = originY - maxRadiusPx * sin(rad).toFloat()
            drawLine(
                color = Color(0xFF00E5FF).copy(alpha = 0.2f),
                start = Offset(originX, originY),
                end = Offset(endX, endY),
                strokeWidth = 1.5f
            )
        }

        // Sweeping Radar Line
        val sweepRad = Math.toRadians(sweepAngleDeg.toDouble())
        val sweepEndX = originX + maxRadiusPx * cos(sweepRad).toFloat()
        val sweepEndY = originY - maxRadiusPx * sin(sweepRad).toFloat()
        drawLine(
            color = Color(0xFF00E5FF),
            start = Offset(originX, originY),
            end = Offset(sweepEndX, sweepEndY),
            strokeWidth = 3f
        )

        // User Position Center Icon
        drawCircle(
            color = Color(0xFF00E5FF),
            radius = 10f,
            center = Offset(originX, originY)
        )

        // Draw Spatial Target Blips
        for (target in targets) {
            // Lateral offset maps to angle on radar screen
            // Lateral 0 = 90 deg (straight ahead)
            val angleRad = Math.atan2(target.distanceMeters.toDouble(), target.lateralOffsetMeters.toDouble())
            val r = distanceToPx(target.distanceMeters)

            val blipX = originX + r * cos(angleRad).toFloat()
            val blipY = originY - r * sin(angleRad).toFloat()

            val blipColor = when (target.hazardLevel) {
                HazardLevel.SAFE -> Color(0xFF00E676)
                HazardLevel.APPROACHING -> Color(0xFFFFD600)
                HazardLevel.CRITICAL_HAZARD -> Color(0xFFFF1744)
            }

            val isSelected = target.id == selectedTargetId

            // Outer Pulse Ring for hazards
            if (target.hazardLevel == HazardLevel.CRITICAL_HAZARD) {
                drawCircle(
                    color = blipColor.copy(alpha = 0.4f),
                    radius = 24f,
                    center = Offset(blipX, blipY)
                )
            }

            // Blip Core
            drawCircle(
                color = if (isSelected) Color.White else blipColor,
                radius = if (isSelected) 14f else 10f,
                center = Offset(blipX, blipY)
            )
        }
    }
}
