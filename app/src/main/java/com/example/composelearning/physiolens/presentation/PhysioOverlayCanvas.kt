package com.example.composelearning.physiolens.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.composelearning.physiolens.domain.BiomechanicalEngine
import com.example.composelearning.physiolens.presentation.PhysioLensContract.BiomechanicalAnalysis
import com.example.composelearning.physiolens.presentation.PhysioLensContract.PosePoint
import com.example.composelearning.physiolens.presentation.PhysioLensContract.StrainStatus

@Composable
fun PhysioOverlayCanvas(
    posePoints: Map<Int, PosePoint>,
    analysis: BiomechanicalAnalysis?,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        if (posePoints.isEmpty()) return@Canvas

        val w = size.width
        val h = size.height

        fun PosePoint.toOffset(): Offset = Offset(x * w, y * h)

        // Bone connections
        val connections = listOf(
            BiomechanicalEngine.LEFT_SHOULDER to BiomechanicalEngine.RIGHT_SHOULDER,
            BiomechanicalEngine.LEFT_SHOULDER to BiomechanicalEngine.LEFT_ELBOW,
            BiomechanicalEngine.LEFT_ELBOW to BiomechanicalEngine.LEFT_WRIST,
            BiomechanicalEngine.RIGHT_SHOULDER to BiomechanicalEngine.RIGHT_ELBOW,
            BiomechanicalEngine.RIGHT_ELBOW to BiomechanicalEngine.RIGHT_WRIST,
            BiomechanicalEngine.LEFT_SHOULDER to BiomechanicalEngine.LEFT_HIP,
            BiomechanicalEngine.RIGHT_SHOULDER to BiomechanicalEngine.RIGHT_HIP,
            BiomechanicalEngine.LEFT_HIP to BiomechanicalEngine.RIGHT_HIP,
            BiomechanicalEngine.LEFT_HIP to BiomechanicalEngine.LEFT_KNEE,
            BiomechanicalEngine.LEFT_KNEE to BiomechanicalEngine.LEFT_ANKLE,
            BiomechanicalEngine.RIGHT_HIP to BiomechanicalEngine.RIGHT_KNEE,
            BiomechanicalEngine.RIGHT_KNEE to BiomechanicalEngine.RIGHT_ANKLE
        )

        // Draw bone glow paths
        for ((startIdx, endIdx) in connections) {
            val p1 = posePoints[startIdx]
            val p2 = posePoints[endIdx]
            if (p1 != null && p2 != null) {
                val o1 = p1.toOffset()
                val o2 = p2.toOffset()

                // Background glow
                drawLine(
                    color = Color(0x6600E5FF),
                    start = o1,
                    end = o2,
                    strokeWidth = 12f,
                    cap = StrokeCap.Round
                )
                // Core bone vector
                drawLine(
                    color = Color(0xFF00E5FF),
                    start = o1,
                    end = o2,
                    strokeWidth = 4f,
                    cap = StrokeCap.Round
                )
            }
        }

        // Draw joint nodes and torque indicators
        posePoints.forEach { (idx, pt) ->
            val center = pt.toOffset()
            val nodeColor = when (idx) {
                BiomechanicalEngine.RIGHT_ELBOW, BiomechanicalEngine.LEFT_ELBOW -> Color(0xFFFF9100)
                BiomechanicalEngine.RIGHT_SHOULDER, BiomechanicalEngine.LEFT_SHOULDER -> Color(0xFFE040FB)
                BiomechanicalEngine.RIGHT_KNEE, BiomechanicalEngine.LEFT_KNEE -> Color(0xFF00E676)
                else -> Color.White
            }

            drawCircle(
                color = nodeColor.copy(alpha = 0.3f),
                radius = 16f,
                center = center
            )
            drawCircle(
                color = nodeColor,
                radius = 8f,
                center = center
            )
        }

        // Draw Biomechanical Torque Arc around Right Elbow if available
        val rElbow = posePoints[BiomechanicalEngine.RIGHT_ELBOW]?.toOffset()
        val rWrist = posePoints[BiomechanicalEngine.RIGHT_WRIST]?.toOffset()
        val rShoulder = posePoints[BiomechanicalEngine.RIGHT_SHOULDER]?.toOffset()

        if (rElbow != null && rWrist != null && rShoulder != null && analysis != null) {
            val bicepData = analysis.muscleStrains.firstOrNull { it.muscleGroup.name.contains("BICEPS") }
            val statusColor = when (bicepData?.status) {
                StrainStatus.OPTIMAL -> Color(0xFF00E676)
                StrainStatus.MODERATE -> Color(0xFFFFD600)
                StrainStatus.HIGH_STRAIN -> Color(0xFFFF9100)
                StrainStatus.OVERLOAD -> Color(0xFFFF1744)
                null -> Color.Cyan
            }

            // Draw Torque vector line from Elbow to Wrist (Moment Arm)
            drawLine(
                color = statusColor,
                start = rElbow,
                end = rWrist,
                strokeWidth = 6f,
                cap = StrokeCap.Round
            )

            // Draw Torque Arc
            val arcPath = Path().apply {
                moveTo(rElbow.x, rElbow.y)
                quadraticTo(
                    (rElbow.x + rWrist.x) / 2f,
                    (rElbow.y + rWrist.y) / 2f - 40f,
                    rWrist.x,
                    rWrist.y
                )
            }
            drawPath(
                path = arcPath,
                color = statusColor.copy(alpha = 0.7f),
                style = Stroke(width = 4f)
            )
        }
    }
}
