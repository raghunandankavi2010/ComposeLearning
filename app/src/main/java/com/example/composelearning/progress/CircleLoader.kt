package com.example.composelearning.progress

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun WaveLoadingCircle(
    fillProgress: () -> Float, // Animated from 0f to 1f (controls height)
    wavePhase: () -> Float, // Infinitely animated from 0f to 2*PI (controls wave movement)
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        // State is read here, in the draw phase, so animation frames only
        // invalidate drawing instead of recomposing the composable tree
        val progress = fillProgress()
        val phase = wavePhase()

        val w = size.width
        val h = size.height

        // 1. Define Wave Parameters
        val maxAmplitude = h * 0.05f // Wave height is 5% of canvas height
        val waveCount = 1f // Number of wave crests across the width

        // Dampen amplitude near 0% and 100% so it fills cleanly
        val currentAmplitude = maxAmplitude * kotlin.math.sin(progress * kotlin.math.PI).toFloat()

        // 2. Calculate the baseline vertical position
        val baselineY = h - (progress * h)

        // 3. Build the closed liquid wave path
        val wavePath = Path().apply {
            moveTo(0f, h) // Start at bottom-left

            // Increment across the width to plot the sine wave points
            val stepPx = 2f
            var x = 0f
            while (x <= w) {
                val omega = (2f * kotlin.math.PI * waveCount) / w
                val y = baselineY + (currentAmplitude * kotlin.math.sin(omega * x + phase)).toFloat()
                lineTo(x, y)
                x += stepPx
            }

            lineTo(w, h) // Line to bottom-right
            close() // Close back to bottom-left forming a solid block
        }

        // 4. Clip to a circle and pour the liquid path inside
        val circlePath = Path().apply {
            addOval(Rect(0f, 0f, w, h))
        }
        clipPath(circlePath) {
            drawPath(
                path = wavePath,
                color = Color(0xFF2196F3) // Liquid Color
            )
        }
    }
}

@Composable
fun WaveLoadingCircleScreen(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveLoader")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * kotlin.math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing)
        ),
        label = "wavePhase"
    )
    val fillProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing)
        ),
        label = "fillProgress"
    )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            WaveLoadingCircle(
                fillProgress = { fillProgress },
                wavePhase = { wavePhase },
                modifier = Modifier
                    .size(220.dp)
                    .border(2.dp, Color(0xFF2196F3), CircleShape)
            )
            Spacer(modifier = Modifier.height(24.dp))
            ProgressPercentText(fillProgress = { fillProgress })
        }
    }
}

@Composable
private fun ProgressPercentText(fillProgress: () -> Float) {
    // derivedStateOf limits recomposition to when the displayed integer
    // actually changes, and only this composable recomposes — not the screen
    val percent by remember { derivedStateOf { (fillProgress() * 100).toInt() } }
    Text(
        text = "$percent%",
        style = MaterialTheme.typography.headlineMedium
    )
}

@Preview(showBackground = true)
@Composable
private fun WaveLoadingCircleScreenPreview() {
    WaveLoadingCircleScreen()
}
