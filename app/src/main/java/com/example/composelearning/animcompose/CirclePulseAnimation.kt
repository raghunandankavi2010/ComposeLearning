package com.example.composelearning.animcompose

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.dp
import kotlin.math.max

@Composable
fun PulsatingCircle() {
    val strokeWidth by remember { mutableStateOf(10) }
    val infiniteTransition = rememberInfiniteTransition()
    val pulseMagnitude by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000),
            repeatMode = RepeatMode.Restart
        )
    )

    val pulseMagnitude2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            // Use the same duration, but add a delay
            animation = tween(durationMillis = 5200, delayMillis = 100), // <-- Delay of 300ms
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseMagnitude2"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            color = Color.Red.copy(alpha = 1 - pulseMagnitude),
            radius = size.minDimension / 2 * pulseMagnitude - strokeWidth.dp.toPx(),
            style = Fill //Stroke(width = strokeWidth.dp.toPx())
        )

        drawCircle(
            color = Color.Green.copy(alpha =  1f - pulseMagnitude2),
            radius = size.minDimension / 2 * pulseMagnitude2 - strokeWidth.dp.toPx(),
            style = Fill// Stroke(width = strokeWidth.dp.toPx())
        )
    }
}


@Composable
fun PulsatingCircle2() {
    val durationMillis = 2000
    val delayMillis = 800 // The delay for the second circle

    val infiniteTransition = rememberInfiniteTransition(label = "TimeBasedPulsatingCircle")

    // 1. Animate a single 'time' value from 0 to the duration of the animation.
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = durationMillis.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "animationTime"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val maxRadius = size.minDimension / 2f

        // --- Circle 1 Calculations ---
        // 'progress' is a value from 0.0 to 1.0 representing the animation's state.
        val progress1 = time / durationMillis
        val radius1 = maxRadius * progress1
        val alpha1 = 1f - progress1

        // --- Circle 2 Calculations ---
        // Introduce the delay by subtracting it from the main 'time'.
        // Use max(0f, ...) to ensure the time for the second circle doesn't go below zero.
        val time2 = max(0f, time - delayMillis)
        val progress2 = time2 / (durationMillis - delayMillis) // Adjust progress to its own timeline
        val radius2 = maxRadius * progress2
        val alpha2 = 1f - progress2

        // --- Draw the first circle (Green) ---
        drawCircle(
            color = Color.Green.copy(alpha = alpha1),
            radius = radius1,
            style = Fill
        )

        // --- Draw the second circle (Red) ---
        drawCircle(
            color = Color.Red.copy(alpha = 1f),
            radius = radius2,
            style = Fill
        )
    }
}

