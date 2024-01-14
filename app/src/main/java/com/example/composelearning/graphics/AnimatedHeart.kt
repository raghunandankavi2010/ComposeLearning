package com.example.composelearning.graphics


import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun AnimatedHeartShape() {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        var clicked by remember { mutableStateOf(false) }
        val heartSize = 400.dp
        val heartSizePx = with(LocalDensity.current) { heartSize.toPx() }

        val infiniteTransition = rememberInfiniteTransition(label = "Sample rememberInfiniteTransition")
        val animatedStrokePhase = infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = heartSizePx * 2,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ), label = "Sample AnimatedFloat"
        )

        val continuousScale = infiniteTransition.animateFloat(
            initialValue = 0.8f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(500, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ), label = "Sample AnimatedFloat"
        )

        val disappearScale by animateFloatAsState(
            targetValue = if (clicked) 0f else 1f,
            animationSpec = tween(
                durationMillis = 500,
                easing = LinearOutSlowInEasing
            ), label = "Sample Float As State"
        )

        Canvas(
            modifier = Modifier
                .size(heartSize)
                .clickable { clicked = true }
        ) {
            val path = Path().apply {
                moveTo(heartSizePx / 2, heartSizePx / 5)
                cubicTo(heartSizePx * 3 / 4, 0f, heartSizePx, heartSizePx / 3, heartSizePx / 2, heartSizePx)
                cubicTo(0f, heartSizePx / 3, heartSizePx / 4, 0f, heartSizePx / 2, heartSizePx / 5)
                close()
            }

            val gradient = Brush.linearGradient(
                colors = listOf(Color.Red, Color(0xFF841C26), Color(0xFFBA274A)),
                start = Offset(0f, 0f),
                end = Offset(heartSizePx, heartSizePx)
            )

            val combinedScale = continuousScale.value * disappearScale

            scale(combinedScale, combinedScale, pivot = Offset(heartSizePx / 2, heartSizePx / 2)) {
                drawPath(path, gradient)

                val lineGradient = Brush.linearGradient(
                    colors = listOf(Color(0xFFFFE45E), Color(0xFFFF6392)),
                    start = Offset(0f, 0f),
                    end = Offset(heartSizePx, heartSizePx)
                )

                val pathEffect = PathEffect.dashPathEffect(
                    intervals = floatArrayOf(heartSizePx, heartSizePx),
                    phase = -animatedStrokePhase.value
                )

                drawPath(
                    path = path,
                    brush = lineGradient,
                    style = Stroke(width = 8.dp.toPx(), pathEffect = pathEffect)
                )
            }
        }
    }
}