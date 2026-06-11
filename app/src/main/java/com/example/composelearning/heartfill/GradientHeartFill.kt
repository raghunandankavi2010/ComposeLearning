/*
 * Copyright 2026 Raghunandan Kavi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.composelearning.heartfill

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * # Gradient heart with a diagonal fill reveal
 *
 * Three independent pieces compose into the effect:
 *
 * 1. **Shape** — [heartPath] builds a heart from four cubic Béziers (two per lobe)
 *    inside an arbitrary bounding box. The path is geometry only; it knows nothing
 *    about color or animation.
 * 2. **Color** — a [Brush.linearGradient] runs bottom-left → top-right, the same
 *    direction the fill travels, so the revealed colors appear in gradient order.
 * 3. **Reveal** — `clipPath(heart) { drawPath(revealMask, gradient) }`. The mask is
 *    a right triangle anchored at the bottom-left corner whose hypotenuse is the
 *    diagonal wavefront. Animating one float (`progress` 0→1) grows the triangle's
 *    legs from 0 to `width + height`, which sweeps the hypotenuse across every
 *    point of the box — only the part of the mask inside the heart ever shows.
 *
 * ## Why a triangle is the right mask
 *
 * "Filled diagonally up to progress p" means the set of points whose perpendicular
 * distance along the diagonal direction d = (1, −1)/√2 from the bottom-left corner
 * is at most p × D, where D is the box's full diagonal extent (width + height when
 * measured as x + (height − y)). That half-plane, intersected with the box, is
 * exactly the triangle with vertices:
 *
 * ```
 * (0, h)             — bottom-left corner (the anchor)
 * (p·(w+h), h)       — along the bottom edge
 * (0, h − p·(w+h))   — up the left edge
 * ```
 *
 * At p = 1 the hypotenuse passes beyond the top-right corner, so the whole heart
 * is covered. Vertices may fall outside the canvas; the heart clip makes that
 * harmless and keeps the math branch-free.
 */
@Composable
fun GradientHeartFill(modifier: Modifier = Modifier) {
    // Animatable (not animateFloatAsState) so the replay button can snap back to 0
    // and re-run the tween from the start instead of animating backwards first.
    val progress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    // Autoplay once on entry. Replays are launched straight from the click handler
    // instead of restarting this effect via a key: composition then never reads any
    // animation-related state, so tapping Replay recomposes nothing. Animatable's
    // internal mutex makes rapid taps safe — a new animateTo cancels the running one.
    LaunchedEffect(Unit) {
        progress.runFillAnimation()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF06070F))
            .systemBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        ) {
            HeartCanvas(
                progressProvider = { progress.value },
                modifier = Modifier.fillMaxSize()
            )
        }
        Text(
            text = "Diagonal gradient fill — bottom-left → top-right",
            color = Color(0xFF9FA8C7),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(top = 16.dp)
        )
        Button(
            onClick = { scope.launch { progress.runFillAnimation() } },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1D2440),
                contentColor = Color(0xFFFF6B9D)
            ),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Replay")
        }
    }
}

private suspend fun Animatable<Float, AnimationVector1D>.runFillAnimation() {
    snapTo(0f)
    animateTo(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 1_800, easing = FastOutSlowInEasing)
    )
}

@Composable
private fun HeartCanvas(
    progressProvider: () -> Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        // Reading progress inside the draw scope keeps the animation in the draw
        // phase: each frame redraws this lambda without recomposing the tree.
        val progress = progressProvider()
        val heart = heartPath(size)

        // Gradient axis matches the fill direction so colors are revealed in order:
        // pink at the bottom-left wavefront start, violet at the top-right finish.
        val gradient = Brush.linearGradient(
            colors = listOf(
                Color(0xFFFF6B9D), // pink
                Color(0xFFE53935), // red
                Color(0xFF8E24AA) // violet
            ),
            start = Offset(0f, size.height),
            end = Offset(size.width, 0f)
        )

        // Always-visible outline so the unfilled remainder of the shape reads
        // as "empty" rather than missing.
        drawPath(path = heart, color = Color(0xFF3A4163), style = Stroke(width = 2.dp.toPx()))

        // Triangle mask: legs grow with progress along the bottom and left edges;
        // the hypotenuse is the diagonal wavefront (see file header for the math).
        val reach = progress * (size.width + size.height)
        val revealMask = Path().apply {
            moveTo(0f, size.height)
            lineTo(reach, size.height)
            lineTo(0f, size.height - reach)
            close()
        }

        clipPath(heart) {
            drawPath(path = revealMask, brush = gradient)
        }
    }
}

/**
 * Heart inside a [size]-sized box, built from four cubic Béziers.
 *
 * Anatomy (fractions of width w and height h):
 * - Start at the **notch** between the lobes: (w/2, h/5).
 * - Left lobe: one curve out to the left edge, one sweeping down to the
 *   **bottom tip** (w/2, h × 0.95).
 * - Right lobe mirrors the left (control points reflected about x = w/2),
 *   returning to the notch. `close()` makes the path fillable and clippable.
 *
 * Control points pull the lobes up past y = 0 slightly (−h/25) so the lobes
 * look round rather than flat-topped.
 */
private fun heartPath(size: Size): Path {
    val w = size.width
    val h = size.height
    return Path().apply {
        moveTo(w / 2f, h / 5f)
        // Left lobe: notch → left edge.
        cubicTo(
            w * 0.36f,
            -h / 25f,
            0f,
            h / 15f,
            w / 28f,
            h * 0.42f
        )
        // Left flank: left edge → bottom tip.
        cubicTo(
            w / 14f,
            h * 0.62f,
            w * 0.36f,
            h * 0.78f,
            w / 2f,
            h * 0.95f
        )
        // Right flank: bottom tip → right edge (mirror of the left flank).
        cubicTo(
            w * 0.64f,
            h * 0.78f,
            w * (13f / 14f),
            h * 0.62f,
            w * (27f / 28f),
            h * 0.42f
        )
        // Right lobe: right edge → notch (mirror of the left lobe).
        cubicTo(
            w,
            h / 15f,
            w * 0.64f,
            -h / 25f,
            w / 2f,
            h / 5f
        )
        close()
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF06070F)
@Composable
private fun GradientHeartFillPreview() {
    GradientHeartFill()
}
