package com.example.composelearning.tutorial.ui.overlay

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector4D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

internal val RectToVector: TwoWayConverter<Rect, AnimationVector4D> =
    TwoWayConverter(
        convertToVector = { r -> AnimationVector4D(r.left, r.top, r.right, r.bottom) },
        convertFromVector = { v -> Rect(v.v1, v.v2, v.v3, v.v4) },
    )

@Composable
fun SpotlightScrim(
    targetRect: Rect?,
    cornerRadiusPx: Float,
    scrimColor: Color,
    modifier: Modifier = Modifier,
) {
    val animatable = remember {
        Animatable(
            initialValue = targetRect ?: Rect.Zero,
            typeConverter = RectToVector,
        )
    }

    LaunchedEffect(targetRect) {
        val next = targetRect ?: return@LaunchedEffect
        if (animatable.value == Rect.Zero) {
            // First reveal — snap a slightly larger rect then settle for a soft "drop-in" feel.
            animatable.snapTo(next.inflate(40f))
        }
        animatable.animateTo(
            targetValue = next,
            animationSpec = tween(durationMillis = 480, easing = FastOutSlowInEasing),
        )
    }

    val pulseTransition = rememberInfiniteTransition(label = "spotlightPulse")
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "pulseAlpha",
    )
    val pulseExpand by pulseTransition.animateFloat(
        initialValue = 0f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "pulseExpand",
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val rect = animatable.value
        if (rect.width <= 1f || rect.height <= 1f) {
            drawRect(scrimColor)
            return@Canvas
        }
        drawScrimWithCutout(rect, cornerRadiusPx, scrimColor)
        drawPulseRing(rect, cornerRadiusPx, pulseExpand, pulseAlpha)
    }
}

private fun DrawScope.drawScrimWithCutout(
    rect: Rect,
    cornerRadiusPx: Float,
    scrimColor: Color,
) {
    val path = Path().apply {
        fillType = PathFillType.EvenOdd
        addRect(Rect(Offset.Zero, size))
        addRoundRect(
            RoundRect(rect = rect, cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx))
        )
    }
    drawPath(path = path, color = scrimColor)
}

private fun DrawScope.drawPulseRing(
    rect: Rect,
    cornerRadiusPx: Float,
    expand: Float,
    alpha: Float,
) {
    if (alpha <= 0f) return
    val expanded = rect.inflate(expand)
    val cr = cornerRadiusPx + expand
    drawRoundRect(
        color = Color.White.copy(alpha = alpha * 0.5f),
        topLeft = expanded.topLeft,
        size = Size(expanded.width, expanded.height),
        cornerRadius = CornerRadius(cr, cr),
        style = Stroke(width = 3f),
    )
}