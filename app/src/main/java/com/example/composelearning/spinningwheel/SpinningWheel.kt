package com.example.composelearning.spinningwheel

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Stateless spinning wheel drawn on a [Canvas].
 *
 * The whole wheel is rotated clockwise by [rotation] degrees; the pointer is
 * drawn afterwards so it stays fixed at the top (12 o'clock). See DESIGN.md for
 * the winner-detection math.
 *
 * @param rotation current rotation of the wheel, in degrees.
 * @param sections slices to render, drawn clockwise starting from 0° (East).
 */
@Composable
fun SpinningWheel(
    rotation: Float,
    modifier: Modifier = Modifier,
    sections: List<WheelSection> = defaultWheelSections
) {
    val sweep = 360f / sections.size

    Canvas(
        modifier = modifier.aspectRatio(1f)
    ) {
        val diameter = min(size.width, size.height)
        val center = Offset(size.width / 2f, size.height / 2f)
        // Leave room for the rim and the pointer above the wheel.
        val wheelRadius = diameter / 2f - 16.dp.toPx()

        drawDropShadow(center, wheelRadius)

        // Everything that belongs to the wheel rotates together.
        rotate(degrees = rotation, pivot = center) {
            drawSlices(center, wheelRadius, sections, sweep)
            drawDividers(center, wheelRadius, sections.size, sweep)
            drawLabels(center, wheelRadius, sections, sweep)
        }

        drawRim(center, wheelRadius)
        drawHub(center, wheelRadius)
        drawPointer(center, wheelRadius)
    }
}

private fun DrawScope.drawDropShadow(center: Offset, radius: Float) {
    drawCircle(
        color = Color.Black.copy(alpha = 0.18f),
        radius = radius + 6.dp.toPx(),
        center = center.copy(y = center.y + 8.dp.toPx())
    )
}

private fun DrawScope.drawSlices(
    center: Offset,
    radius: Float,
    sections: List<WheelSection>,
    sweep: Float
) {
    val topLeft = Offset(center.x - radius, center.y - radius)
    val arcSize = androidx.compose.ui.geometry.Size(radius * 2f, radius * 2f)
    sections.forEachIndexed { index, section ->
        drawArc(
            color = section.color,
            startAngle = index * sweep,
            sweepAngle = sweep,
            useCenter = true,
            topLeft = topLeft,
            size = arcSize
        )
    }
}

private fun DrawScope.drawDividers(
    center: Offset,
    radius: Float,
    count: Int,
    sweep: Float
) {
    repeat(count) { index ->
        val angle = Math.toRadians((index * sweep).toDouble())
        drawLine(
            color = Color.White.copy(alpha = 0.85f),
            start = center,
            end = Offset(
                x = center.x + radius * cos(angle).toFloat(),
                y = center.y + radius * sin(angle).toFloat()
            ),
            strokeWidth = 2.dp.toPx()
        )
    }
}

private fun DrawScope.drawLabels(
    center: Offset,
    radius: Float,
    sections: List<WheelSection>,
    sweep: Float
) {
    val textPaint = Paint().apply {
        color = Color.White.toArgb()
        textAlign = Paint.Align.CENTER
        textSize = (radius * 0.13f)
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
        setShadowLayer(4f, 0f, 1f, Color.Black.copy(alpha = 0.4f).toArgb())
    }

    sections.forEachIndexed { index, section ->
        // Rotate the canvas to the mid-angle of this slice so the label reads
        // outward along the radius.
        val midAngle = index * sweep + sweep / 2f
        rotate(degrees = midAngle, pivot = center) {
            drawIntoCanvas { canvas ->
                val lines = section.label.split("\n")
                val lineHeight = textPaint.textSize * 1.05f
                // Center the block of lines vertically on the radius.
                val startY = center.y - (lines.size - 1) * lineHeight / 2f +
                    textPaint.textSize / 3f
                val x = center.x + radius * 0.62f
                lines.forEachIndexed { line, text ->
                    canvas.nativeCanvas.drawText(
                        text,
                        x,
                        startY + line * lineHeight,
                        textPaint
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawRim(center: Offset, radius: Float) {
    // Outer metallic ring.
    drawCircle(
        color = Color(0xFF3E2723),
        radius = radius + 10.dp.toPx(),
        center = center,
        style = Stroke(width = 14.dp.toPx())
    )
    // Decorative "LED" pegs evenly spaced around the rim.
    val pegCount = 24
    val pegRadius = radius + 10.dp.toPx()
    repeat(pegCount) { i ->
        val angle = Math.toRadians((i * 360f / pegCount).toDouble())
        val on = i % 2 == 0
        drawCircle(
            color = if (on) Color(0xFFFFF59D) else Color(0xFFFFA000),
            radius = 3.dp.toPx(),
            center = Offset(
                x = center.x + pegRadius * cos(angle).toFloat(),
                y = center.y + pegRadius * sin(angle).toFloat()
            )
        )
    }
}

private fun DrawScope.drawHub(center: Offset, radius: Float) {
    drawCircle(color = Color(0xFF3E2723), radius = radius * 0.16f, center = center)
    drawCircle(color = Color(0xFFFFC107), radius = radius * 0.11f, center = center)
    drawCircle(color = Color(0xFFFFF8E1), radius = radius * 0.04f, center = center)
}

private fun DrawScope.drawPointer(center: Offset, radius: Float) {
    // Triangle pointing down into the wheel from above the top edge.
    val tipY = center.y - radius + 6.dp.toPx()
    val topY = center.y - radius - 18.dp.toPx()
    val halfWidth = 14.dp.toPx()
    val path = Path().apply {
        moveTo(center.x, tipY)
        lineTo(center.x - halfWidth, topY)
        lineTo(center.x + halfWidth, topY)
        close()
    }
    // Subtle shadow then the pointer body.
    drawPath(path, color = Color.Black.copy(alpha = 0.25f))
    drawPath(path, color = Color(0xFFD32F2F))
    drawCircle(color = Color(0xFFB71C1C), radius = 6.dp.toPx(), center = Offset(center.x, topY + 4.dp.toPx()))
}

@Preview(showBackground = true)
@Composable
private fun SpinningWheelPreview() {
    SpinningWheel(
        rotation = 12f,
        modifier = Modifier.aspectRatio(1f)
    )
}
