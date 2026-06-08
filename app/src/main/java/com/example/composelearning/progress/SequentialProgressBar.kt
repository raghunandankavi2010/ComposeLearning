package com.example.composelearning.progress

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CanvasCircularLoader(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    strokeWidth: Dp = 4.dp,
    color: Color = Color.Blue,
    animationDuration: Int = 1500 // Duration for one full cycle
) {
    val infiniteTransition = rememberInfiniteTransition(label = "CanvasCircularLoader")

    // Animate the sweep angle from 0 to 360 and back to 0
    val animatedSweep by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = animationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "CanvasCircularLoaderSweep"
    )

    // Animate the start angle to create the rotation effect
    val animatedStartAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = animationDuration * 2, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "CanvasCircularLoaderStartAngle"
    )

    Canvas(modifier = modifier.size(size)) {
        val canvasSize = size.toPx()
        val diameter = canvasSize - strokeWidth.toPx()
        val topLeft = Offset((canvasSize - diameter) / 2f, (canvasSize - diameter) / 2f)
        val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)

        // Draw the arc
        drawArc(
            color = color,
            startAngle = animatedStartAngle,
            sweepAngle = animatedSweep,
            useCenter = false,
            topLeft = topLeft,
            size = Size(diameter, diameter),
            style = stroke
        )
    }
}
