package com.example.composelearning.animcompose

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutQuad
import androidx.compose.animation.core.EaseInQuart
import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val NetflixRed = Color(0xFFE50914)
private val NetflixDarkRed = Color(0xFF7A040C)
private val NetflixHighlight = Color(0xFFFF1F2E)

/**
 * Compose approximation of the Netflix "N" logo intro.
 *
 * Disclosures up front:
 *  - The real app intro is a *pre-rendered video* synced to audio; this is a stylised redraw.
 *  - The three bars form a letter "N" via a clip-path parallelogram for the diagonal and two
 *    plain rects for the verticals. Verticals are drawn *over* the diagonal so the diagonal is
 *    only visible in the gap between them, giving clean N geometry.
 *
 * Sequence:
 *  1. Left + right vertical bars sweep down (slightly staggered).
 *  2. The diagonal "ribbon" sweeps top → bottom inside the cutout between the verticals.
 *  3. A camera punch-in (uniform scale) zooms into the centre, fading to black.
 *
 * Tap anywhere to replay.
 */
@Composable
fun NetflixLogoAnimation(modifier: Modifier = Modifier) {
    var replay by remember { mutableIntStateOf(0) }

    val leftFill = remember { Animatable(0f) }
    val rightFill = remember { Animatable(0f) }
    val diagFill = remember { Animatable(0f) }
    val zoom = remember { Animatable(1f) }
    val fade = remember { Animatable(1f) }

    LaunchedEffect(replay) {
        // Reset for replays
        leftFill.snapTo(0f)
        rightFill.snapTo(0f)
        diagFill.snapTo(0f)
        zoom.snapTo(1f)
        fade.snapTo(1f)

        // Phase 1 — verticals sweep down (parallel, with a small stagger).
        launch { leftFill.animateTo(1f, tween(900, easing = EaseOutQuart)) }
        launch {
            delay(140)
            rightFill.animateTo(1f, tween(900, easing = EaseOutQuart))
        }
        delay(1040)

        // Phase 2 — diagonal ribbon sweeps in.
        diagFill.animateTo(1f, tween(700, easing = EaseInOutQuad))
        delay(280)

        // Phase 3 — camera punch-in + fade.
        launch { zoom.animateTo(7f, tween(900, easing = EaseInQuart)) }
        delay(700)
        fade.animateTo(0f, tween(400))
    }

    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = interaction,
                indication = null,
            ) { replay += 1 },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier
                .size(width = 220.dp, height = 280.dp)
                .graphicsLayer {
                    scaleX = zoom.value
                    scaleY = zoom.value
                    alpha = fade.value
                    transformOrigin = TransformOrigin(0.5f, 0.5f)
                },
        ) {
            drawNetflixN(
                leftFill = leftFill.value,
                rightFill = rightFill.value,
                diagFill = diagFill.value,
            )
        }
    }
}

private fun DrawScope.drawNetflixN(
    leftFill: Float,
    rightFill: Float,
    diagFill: Float,
) {
    val w = size.width
    val h = size.height
    val strokeW = w / 5f

    // Diagonal parallelogram — the four corners are the inside-corners of the verticals
    // (TL touches right-top of the left vertical; BR touches left-bottom of the right vertical),
    // plus an offset of strokeW to give the diagonal a consistent thickness at top and bottom.
    val diagPath = Path().apply {
        moveTo(strokeW, 0f)                  // TL
        lineTo(2 * strokeW, 0f)              // TR
        lineTo(w - strokeW, h)               // BR
        lineTo(w - 2 * strokeW, h)           // BL
        close()
    }

    // Clip-revealed diagonal: vertical wipe top→bottom driven by diagFill.
    clipPath(diagPath) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(NetflixHighlight, NetflixRed, NetflixDarkRed),
                startY = 0f,
                endY = h,
            ),
            topLeft = Offset.Zero,
            size = Size(w, h * diagFill),
        )
    }

    // Left vertical — drawn over the diagonal so the diagonal only shows in the gap.
    drawRect(
        brush = Brush.horizontalGradient(
            colors = listOf(NetflixRed, NetflixDarkRed),
            startX = 0f,
            endX = strokeW,
        ),
        topLeft = Offset.Zero,
        size = Size(strokeW, h * leftFill),
    )

    // Right vertical — mirrored gradient for a subtle 3D-edge feel.
    drawRect(
        brush = Brush.horizontalGradient(
            colors = listOf(NetflixDarkRed, NetflixRed),
            startX = w - strokeW,
            endX = w,
        ),
        topLeft = Offset(w - strokeW, 0f),
        size = Size(strokeW, h * rightFill),
    )
}