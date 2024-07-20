package com.example.composelearning.progess

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp
import kotlin.math.max

@Composable
fun MultiColorProgress(
    modifier: Modifier = Modifier
) {
    val colors = listOf(
        Color.Gray,
        Color.Yellow,
        Color.Magenta,
        Color.Green
    )

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(Color.Red, Color.Blue, Color.Green)
    )
    val stroke = Stroke(8f)


    val infiniteTransition = rememberInfiniteTransition("PathTransition")

    // Animate path completion with a larger target value
    val animatedProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f, // Double the full circle (360 degrees)
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000), // Adjust duration as needed
            repeatMode = RepeatMode.Restart
        ),
        label = "MultiColorProgress"
    )

    Canvas(modifier = Modifier.size(24.dp)) {
        val size = Size(24.dp.toPx(), 24.dp.toPx())
        val diameter = size.minDimension - stroke.width
        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)

        drawArc(
            color = colors.first(),
            useCenter = true,
            startAngle = 0f,
            sweepAngle = 360f,
            topLeft = topLeft,
            size = Size(diameter, diameter),
            style = stroke
        )

        withTransform({
            rotate(degrees = animatedProgress, pivot = center)
        }) {
            drawArc(
                color = colors[1],
                startAngle = 270f, // Use modulo to keep between 0 and 360
                sweepAngle = 45f,
                useCenter = false,
                topLeft = Offset(stroke.width / 2, stroke.width / 2),
                size = Size(diameter, diameter),
                style = Stroke(8f, cap = StrokeCap.Round)
            )

            drawArc(
                color = colors[2],
                startAngle = 270f + 45f,
                sweepAngle = 45f,
                useCenter = false,
                topLeft = Offset(stroke.width / 2, stroke.width / 2),
                size = Size(diameter, diameter),
                style = Stroke(8f, cap = StrokeCap.Round)
            )

            drawArc(
                color = colors[3],
                startAngle = 270 + 90f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(stroke.width / 2, stroke.width / 2),
                size = Size(diameter, diameter),
                style = Stroke(8f, cap = StrokeCap.Round)
            )
        }
    }
}
