package com.example.composelearning.animcompose

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun PulsatingCircle() {
    val infiniteTransition = rememberInfiniteTransition()
    val pulseMagnitude by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Reverse
        )
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            color = Color.Red.copy(alpha = 0.5f),
            radius = size.minDimension / 2 * pulseMagnitude,
            style = Stroke(width = 10.dp.toPx())
        )
    }
}