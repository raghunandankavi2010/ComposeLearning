package com.example.composelearning.progress

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Showcase for [PacManLoader] / [PacManCircleLoader]: an auto-running 0 → 100% loop, a
 * manually scrubbed track, the circular ring variant and a few styling combinations.
 */
@Composable
fun PacManLoaderScreen() {
    val autoProgress = rememberAutoProgress()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(text = "Pac-Man Loader", style = MaterialTheme.typography.headlineSmall)
        Text(
            text = "A determinate loader: Pac-Man walks a horizontal row of pellets from 0% to " +
                "100%, chomping each one as the mouth reaches it. Progress is read in the draw " +
                "phase, so the animation never recomposes.",
            style = MaterialTheme.typography.bodyMedium
        )

        PacManSample(title = "Auto 0 → 100%") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PacManLoader(
                    progress = { autoProgress.value },
                    modifier = Modifier.weight(1f)
                )
                PercentLabel(progress = { autoProgress.value })
            }
        }

        PacManSample(title = "Scrub it yourself") {
            var manual by remember { mutableFloatStateOf(0.35f) }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PacManLoader(
                    progress = { manual },
                    modifier = Modifier.weight(1f),
                    dotCount = 10
                )
                PercentLabel(progress = { manual })
            }
            Slider(value = manual, onValueChange = { manual = it })
        }

        PacManSample(title = "Inside a ring (progress arc + percentage)") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                PacManCircleLoader(progress = { autoProgress.value })
            }
        }

        PacManSample(title = "Dense track, 20 pellets, small Pac-Man") {
            PacManLoader(
                progress = { autoProgress.value },
                modifier = Modifier.fillMaxWidth(),
                dotCount = 20,
                pacManSize = 24.dp,
                dotSize = 5.dp
            )
        }

        PacManSample(title = "Ghost colors, chunky, slow chomp") {
            PacManLoader(
                progress = { autoProgress.value },
                modifier = Modifier.fillMaxWidth(),
                dotCount = 8,
                pacManSize = 52.dp,
                dotSize = 12.dp,
                pacManColor = Color(0xFF00E5FF),
                dotColor = Color(0xFFFF6E9C),
                showEye = false,
                chompDurationMillis = 320
            )
        }

        PacManSample(title = "Compact ring, 4 pellets") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                PacManCircleLoader(
                    progress = { autoProgress.value },
                    diameter = 120.dp,
                    dotCount = 4,
                    ringWidth = 5.dp,
                    showPercentage = false
                )
            }
        }
    }
}

/** 0 → 100% over 4s, then a short hold at full before restarting. */
@Composable
private fun rememberAutoProgress(): State<Float> {
    val transition = rememberInfiniteTransition(label = "pacManProgress")
    return transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 5000
                0f at 0 using LinearEasing
                1f at 4000
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )
}

@Composable
private fun PercentLabel(progress: () -> Float, modifier: Modifier = Modifier) {
    val percent by remember(progress) {
        derivedStateOf { (progress().coerceIn(0f, 1f) * 100).roundToInt() }
    }
    Text(
        text = "$percent%",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier.padding(start = 12.dp)
    )
}

@Composable
private fun PacManSample(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleSmall)
        content()
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 900)
@Composable
private fun PacManLoaderScreenPreview() {
    PacManLoaderScreen()
}
