package com.example.composelearning.applerings.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.composelearning.applerings.domain.model.RingSpec
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private val StrokeWidth = 34.dp

@Composable
fun ActivityRingsScreen(
    viewModel: ActivityRingsViewModel = viewModel(factory = ActivityRingsViewModel.Factory()),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000001)),
        contentAlignment = Alignment.Center,
    ) {
        when {
            state.isLoading -> CircularProgressIndicator(color = Color.White)
            else -> ActivityRings(state.rings)
        }
        Text(
            "Tap to replay",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 14.sp,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 40.dp),
        )
    }
}

@Composable
private fun ActivityRings(rings: List<RingSpec>) {
    val progress = remember { Animatable(0f) }
    var replay by remember { mutableIntStateOf(0) }

    LaunchedEffect(replay) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(durationMillis = 1800, easing = FastOutSlowInEasing))
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) { detectTapGestures { replay++ } },
    ) {
        val strokePx = StrokeWidth.toPx()
        val center = Offset(size.width / 2f, size.height / 2f)
        val outerRadius = (min(size.width, size.height) - strokePx) / 2f - 8.dp.toPx()

        rings.forEach { ring ->
            val radius = outerRadius - ring.insetSteps * strokePx
            if (radius > strokePx) {
                drawRing(ring, center, radius, strokePx, progress.value)
            }
        }
    }
}

private fun DrawScope.drawRing(
    ring: RingSpec,
    center: Offset,
    radius: Float,
    strokePx: Float,
    progress: Float,
) {
    val topLeft = Offset(center.x - radius, center.y - radius)
    val arcSize = Size(radius * 2, radius * 2)
    val stroke = Stroke(width = strokePx, cap = StrokeCap.Round)

    // Background track (full circle).
    drawArc(
        color = Color(ring.trackColor),
        startAngle = 0f,
        sweepAngle = 360f,
        useCenter = false,
        topLeft = topLeft,
        size = arcSize,
        style = Stroke(width = strokePx),
    )

    val sweep = ring.targetTurns * 360f * progress
    if (sweep <= 0.01f) return

    // Progress arc, gradient running start → end. Rotated so it begins at the top.
    rotate(degrees = -90f, pivot = center) {
        drawArc(
            brush = Brush.sweepGradient(
                0f to Color(ring.startColor),
                0.5f to Color(ring.endColor),
                1f to Color(ring.startColor),
                center = center,
            ),
            startAngle = 0f,
            sweepAngle = sweep,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = stroke,
        )
    }

    // End cap with a drop shadow to sell the overlap when sweep > 360°.
    val endAngleRad = Math.toRadians((-90f + sweep).toDouble())
    val capCenter = Offset(
        center.x + radius * cos(endAngleRad).toFloat(),
        center.y + radius * sin(endAngleRad).toFloat(),
    )
    drawCircle(
        color = Color.Black.copy(alpha = 0.28f),
        radius = strokePx / 2f,
        center = capCenter + Offset(2f, 4f),
    )
    drawCircle(
        color = Color(ring.endColor),
        radius = strokePx / 2f,
        center = capCenter,
    )
}
