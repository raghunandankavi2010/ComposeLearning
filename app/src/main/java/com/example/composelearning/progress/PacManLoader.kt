package com.example.composelearning.progress

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.composelearning.util.LocalAnimationsEnabled
import kotlin.math.roundToInt

/** Classic Pac-Man yellow. */
val PacManYellow = Color(0xFFFFCE00)

private const val MaxMouthAngle = 62f

/**
 * Determinate Pac-Man loader: a horizontal row of dots with Pac-Man chomping his way
 * from left (0%) to right (100%). Dots shrink away as the mouth reaches them, so the
 * number of pellets left is the remaining progress.
 *
 * [progress] is a lambda on purpose — the value is read inside the draw phase, so an
 * animated progress never recomposes this composable or its caller.
 *
 * @param progress current progress in `0f..1f`, read every frame during draw.
 * @param dotCount how many pellets to lay across the track.
 * @param pacManSize diameter of Pac-Man; also the height of the track.
 * @param dotSize diameter of a pellet.
 * @param chompDurationMillis time for the mouth to go from closed to fully open.
 */
@Composable
fun PacManLoader(
    progress: () -> Float,
    modifier: Modifier = Modifier,
    dotCount: Int = 12,
    pacManSize: Dp = 36.dp,
    dotSize: Dp = 8.dp,
    pacManColor: Color = PacManYellow,
    dotColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
    eyeColor: Color = Color(0xFF1B1B1F),
    showEye: Boolean = true,
    chompDurationMillis: Int = 170
) {
    val mouthAngle = rememberMouthAngle(chompDurationMillis)
    val density = LocalDensity.current
    val pacManRadiusPx = with(density) { pacManSize.toPx() / 2f }
    val dotRadiusPx = with(density) { dotSize.toPx() / 2f }

    Canvas(modifier = modifier.height(pacManSize)) {
        val fraction = progress().coerceIn(0f, 1f)
        val radius = pacManRadiusPx.coerceAtMost(size.height / 2f)
        val centerY = size.height / 2f
        val travel = (size.width - radius * 2f).coerceAtLeast(0f)
        val pacManX = radius + travel * fraction
        // The mouth reaches a bit ahead of the body centre; pellets are consumed over that span.
        val bite = radius * 0.9f
        val mouthTipX = pacManX + bite
        val step = if (dotCount > 0) travel / dotCount else 0f

        for (index in 1..dotCount) {
            val dotX = radius + step * index
            val eaten = ((mouthTipX - dotX) / bite).coerceIn(0f, 1f)
            if (eaten >= 1f) continue
            val scale = 1f - eaten
            drawCircle(
                color = dotColor,
                radius = dotRadiusPx * scale,
                center = Offset(dotX, centerY),
                alpha = scale
            )
        }

        drawPacMan(
            center = Offset(pacManX, centerY),
            radius = radius,
            mouthAngle = mouthAngle.value,
            color = pacManColor,
            eyeColor = eyeColor,
            showEye = showEye
        )
    }
}

/**
 * The same Pac-Man track, horizontally centred inside a circular progress ring, with the
 * percentage under it. The ring sweeps clockwise from 12 o'clock as Pac-Man crosses the row.
 */
@Composable
fun PacManCircleLoader(
    progress: () -> Float,
    modifier: Modifier = Modifier,
    diameter: Dp = 200.dp,
    dotCount: Int = 6,
    ringWidth: Dp = 8.dp,
    pacManColor: Color = PacManYellow,
    ringColor: Color = PacManYellow,
    trackColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.18f),
    dotColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
    showPercentage: Boolean = true
) {
    val ringWidthPx = with(LocalDensity.current) { ringWidth.toPx() }

    Box(modifier = modifier.size(diameter), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val inset = ringWidthPx / 2f
            val arcSize = Size(size.width - ringWidthPx, size.height - ringWidthPx)
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = ringWidthPx)
            )
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = 360f * progress().coerceIn(0f, 1f),
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = ringWidthPx, cap = StrokeCap.Round)
            )
        }

        PacManLoader(
            progress = progress,
            modifier = Modifier.width(diameter * 0.62f),
            dotCount = dotCount,
            pacManSize = diameter * 0.19f,
            dotSize = diameter * 0.045f,
            pacManColor = pacManColor,
            dotColor = dotColor
        )

        if (showPercentage) {
            PacManPercentText(
                progress = progress,
                modifier = Modifier.offset(y = diameter * 0.2f)
            )
        }
    }
}

/**
 * Kept separate so that the once-per-percent recomposition it needs does not drag the
 * Canvas-based loader through composition on every frame.
 */
@Composable
private fun PacManPercentText(progress: () -> Float, modifier: Modifier = Modifier) {
    val percent by remember(progress) {
        derivedStateOf { (progress().coerceIn(0f, 1f) * 100).roundToInt() }
    }
    Text(
        text = "$percent%",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
    )
}

/**
 * Mouth opening angle in degrees, ping-ponging between closed and [MaxMouthAngle].
 * Returned as [State] so callers can read it inside a draw block instead of composition.
 */
@Composable
private fun rememberMouthAngle(chompDurationMillis: Int): State<Float> {
    if (!LocalAnimationsEnabled.current) {
        return remember { mutableFloatStateOf(MaxMouthAngle * 0.7f) }
    }
    val transition = rememberInfiniteTransition(label = "pacManChomp")
    return transition.animateFloat(
        initialValue = 0f,
        targetValue = MaxMouthAngle,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = chompDurationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mouthAngle"
    )
}

/** Pac-Man is a pie slice: a full circle minus a wedge of [mouthAngle] degrees facing right. */
private fun DrawScope.drawPacMan(
    center: Offset,
    radius: Float,
    mouthAngle: Float,
    color: Color,
    eyeColor: Color,
    showEye: Boolean
) {
    drawArc(
        color = color,
        startAngle = mouthAngle / 2f,
        sweepAngle = 360f - mouthAngle,
        useCenter = true,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2f, radius * 2f)
    )
    if (showEye) {
        drawCircle(
            color = eyeColor,
            radius = radius * 0.12f,
            center = Offset(center.x + radius * 0.08f, center.y - radius * 0.45f)
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 120)
@Composable
private fun PacManLoaderPreview() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        PacManLoader(progress = { 0.45f }, modifier = Modifier.width(300.dp))
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 260)
@Composable
private fun PacManCircleLoaderPreview() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        PacManCircleLoader(progress = { 0.6f })
    }
}
