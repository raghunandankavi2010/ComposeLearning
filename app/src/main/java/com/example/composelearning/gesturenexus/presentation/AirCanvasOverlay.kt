package com.example.composelearning.gesturenexus.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.composelearning.gesturenexus.data.GestureNexusAnalyzer
import com.example.composelearning.gesturenexus.presentation.GestureNexusContract.HandLandmarkPoint
import com.example.composelearning.gesturenexus.presentation.GestureNexusContract.StrokePath

@Composable
fun AirCanvasOverlay(
    handLandmarks: Map<Int, HandLandmarkPoint>,
    drawnStrokes: List<StrokePath>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // 1. Draw persistent user air-drawn strokes
        for (stroke in drawnStrokes) {
            if (stroke.points.size < 2) continue
            val path = Path().apply {
                moveTo(stroke.points.first().x, stroke.points.first().y)
                for (i in 1 until stroke.points.size) {
                    lineTo(stroke.points[i].x, stroke.points[i].y)
                }
            }

            // Glow under-layer
            drawPath(
                path = path,
                color = stroke.color.copy(alpha = 0.4f),
                style = Stroke(width = stroke.strokeWidth * 2.2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            // Core stroke
            drawPath(
                path = path,
                color = stroke.color,
                style = Stroke(width = stroke.strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }

        if (handLandmarks.isEmpty()) return@Canvas

        fun HandLandmarkPoint.toOffset(): Offset = Offset(x * w, y * h)

        // Hand skeletal connections
        val connections = listOf(
            // Palm
            GestureNexusAnalyzer.WRIST to GestureNexusAnalyzer.THUMB_CMC,
            GestureNexusAnalyzer.WRIST to GestureNexusAnalyzer.INDEX_FINGER_MCP,
            GestureNexusAnalyzer.WRIST to GestureNexusAnalyzer.MIDDLE_FINGER_MCP,
            GestureNexusAnalyzer.WRIST to GestureNexusAnalyzer.RING_FINGER_MCP,
            GestureNexusAnalyzer.WRIST to GestureNexusAnalyzer.PINKY_MCP,
            // Thumb
            GestureNexusAnalyzer.THUMB_CMC to GestureNexusAnalyzer.THUMB_MCP,
            GestureNexusAnalyzer.THUMB_MCP to GestureNexusAnalyzer.THUMB_IP,
            GestureNexusAnalyzer.THUMB_IP to GestureNexusAnalyzer.THUMB_TIP,
            // Index
            GestureNexusAnalyzer.INDEX_FINGER_MCP to GestureNexusAnalyzer.INDEX_FINGER_PIP,
            GestureNexusAnalyzer.INDEX_FINGER_PIP to GestureNexusAnalyzer.INDEX_FINGER_DIP,
            GestureNexusAnalyzer.INDEX_FINGER_DIP to GestureNexusAnalyzer.INDEX_FINGER_TIP,
            // Middle
            GestureNexusAnalyzer.MIDDLE_FINGER_MCP to GestureNexusAnalyzer.MIDDLE_FINGER_PIP,
            GestureNexusAnalyzer.MIDDLE_FINGER_PIP to GestureNexusAnalyzer.MIDDLE_FINGER_DIP,
            GestureNexusAnalyzer.MIDDLE_FINGER_DIP to GestureNexusAnalyzer.MIDDLE_FINGER_TIP,
            // Ring
            GestureNexusAnalyzer.RING_FINGER_MCP to GestureNexusAnalyzer.RING_FINGER_PIP,
            GestureNexusAnalyzer.RING_FINGER_PIP to GestureNexusAnalyzer.RING_FINGER_DIP,
            GestureNexusAnalyzer.RING_FINGER_DIP to GestureNexusAnalyzer.RING_FINGER_TIP,
            // Pinky
            GestureNexusAnalyzer.PINKY_MCP to GestureNexusAnalyzer.PINKY_PIP,
            GestureNexusAnalyzer.PINKY_PIP to GestureNexusAnalyzer.PINKY_DIP,
            GestureNexusAnalyzer.PINKY_DIP to GestureNexusAnalyzer.PINKY_TIP
        )

        // Draw bone links
        for ((startIdx, endIdx) in connections) {
            val p1 = handLandmarks[startIdx]
            val p2 = handLandmarks[endIdx]
            if (p1 != null && p2 != null) {
                drawLine(
                    color = Color(0xFF00E5FF).copy(alpha = 0.6f),
                    start = p1.toOffset(),
                    end = p2.toOffset(),
                    strokeWidth = 4f,
                    cap = StrokeCap.Round
                )
            }
        }

        // Draw 21 hand joint nodes
        handLandmarks.forEach { (idx, pt) ->
            val center = pt.toOffset()
            val isTip = idx in listOf(
                GestureNexusAnalyzer.THUMB_TIP,
                GestureNexusAnalyzer.INDEX_FINGER_TIP,
                GestureNexusAnalyzer.MIDDLE_FINGER_TIP,
                GestureNexusAnalyzer.RING_FINGER_TIP,
                GestureNexusAnalyzer.PINKY_TIP
            )

            val nodeColor = if (isTip) Color(0xFFFF9100) else Color(0xFFE040FB)
            drawCircle(
                color = nodeColor.copy(alpha = 0.3f),
                radius = if (isTip) 14f else 8f,
                center = center
            )
            drawCircle(
                color = nodeColor,
                radius = if (isTip) 7f else 4f,
                center = center
            )
        }

        // Draw glowing brush tip around index fingertip
        val indexTip = handLandmarks[GestureNexusAnalyzer.INDEX_FINGER_TIP]?.toOffset()
        if (indexTip != null) {
            drawCircle(
                color = Color(0xFF00E5FF).copy(alpha = 0.25f),
                radius = 28f,
                center = indexTip
            )
            drawCircle(
                color = Color.White,
                radius = 6f,
                center = indexTip
            )
        }
    }
}
