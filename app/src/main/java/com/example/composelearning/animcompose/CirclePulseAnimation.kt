package com.example.composelearning.animcompose

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.composelearning.customshapes.dpToPx

@Composable
fun PulsatingCircle() {
    val strokeWidth by remember { mutableStateOf(10) }
    val infiniteTransition = rememberInfiniteTransition()
    val pulseMagnitude by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000),
            repeatMode = RepeatMode.Reverse
        )
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            color = Color.Red.copy(alpha = 0.2f),
            radius = size.minDimension / 2 * pulseMagnitude - strokeWidth.dp.toPx(),
            style = Fill
        )

        drawCircle(
            color = Color.Green.copy(alpha = 1f),
            radius = size.minDimension / 2 * pulseMagnitude - strokeWidth.dp.toPx(),
            style = Stroke(width = strokeWidth.dp.toPx())
        )
    }
}