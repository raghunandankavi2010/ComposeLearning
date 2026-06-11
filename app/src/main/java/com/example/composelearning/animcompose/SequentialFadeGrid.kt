package com.example.composelearning.animcompose

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A 4×4 grid of spaced boxes whose alpha fades out one cell at a time in reading
 * order (cell 1, 2, 3, 4 across row 1, then row 2 … down to cell 16). Once every
 * cell is invisible it resets and runs the whole fade-out again — an infinite loop.
 *
 * ### How the stagger works
 * Each cell has its own [Animatable] for alpha. A single driver coroutine kicks off
 * `animateTo` for cell `i`, waits `STAGGER_MS`, then kicks off cell `i + 1`, etc.
 * Launching each animation in a child coroutine means they run concurrently but
 * *start* offset in time — that offset is the staggered wave.
 *
 * ### Why it doesn't spam recompositions
 * The animated alpha is read inside `Modifier.graphicsLayer { alpha = ... }`, which
 * runs in the **draw phase**. So each frame only re-draws the affected boxes; the
 * composable itself never recomposes for the animation.
 */
@Composable
fun SequentialFadeGrid(modifier: Modifier = Modifier) {
    val rows = 4
    val cols = 4
    val count = rows * cols

    // One alpha channel per cell, all starting fully visible.
    val alphas = remember { List(count) { Animatable(1f) } }

    LaunchedEffectStagger(alphas, count)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        val spacing = 14.dp
        // Largest square cell that lets the 4×4 grid (with gaps) fit the smaller side.
        val side = (minOf(maxWidth, maxHeight) - spacing * (cols - 1)) / cols

        Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
            for (r in 0 until rows) {
                Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                    for (c in 0 until cols) {
                        val i = r * cols + c
                        // Rainbow hue per cell purely for visual distinction.
                        val cellColor = Color.hsv(
                            hue = (i.toFloat() / count) * 320f,
                            saturation = 0.55f,
                            value = 0.95f
                        )
                        Box(
                            modifier = Modifier
                                .size(side)
                                // Draw-phase read -> redraw only, no recomposition.
                                .graphicsLayer { alpha = alphas[i].value }
                                .clip(RoundedCornerShape(18.dp))
                                .background(cellColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${i + 1}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Drives the looping sequential fade-out / fade-in. Extracted so the grid composable
 * stays focused on layout.
 */
@Composable
private fun LaunchedEffectStagger(
    alphas: List<Animatable<Float, *>>,
    count: Int
) {
    androidx.compose.runtime.LaunchedEffect(Unit) {
        while (true) {
            // Sequential FADE OUT: cell 1 → cell 16
            for (i in 0 until count) {
                launch { alphas[i].animateTo(0f, tween(FADE_MS, easing = FastOutSlowInEasing)) }
                delay(STAGGER_MS.milliseconds)
            }
            // Wait for the last cell to finish fading, then hold while fully invisible.
            delay(FADE_MS.milliseconds)
            delay(HOLD_MS.milliseconds)

            // Restart the loop: snap every cell back to visible, pause briefly, then
            // run the whole sequential fade-out again — forever.
            for (a in alphas) a.snapTo(1f)
            delay(HOLD_MS.milliseconds)
        }
    }
}

private const val FADE_MS = 350 // how long a single cell takes to fade
private const val STAGGER_MS = 110L // gap between consecutive cells starting
private const val HOLD_MS = 500L // pause between the out and in passes

@Preview(showBackground = true)
@Composable
private fun SequentialFadeGridPreview() {
    SequentialFadeGrid()
}
