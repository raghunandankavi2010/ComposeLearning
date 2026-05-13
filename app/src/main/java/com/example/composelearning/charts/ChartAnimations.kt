package com.example.composelearning.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember

/**
 * Returns an animation progress (0f..1f) that runs once whenever [key] changes.
 *
 * Used by charts to drive entrance animations without each chart having to re-implement the
 * Animatable + LaunchedEffect pattern.
 */
@Composable
fun rememberChartProgress(
    key: Any?,
    spec: AnimationSpec<Float> = tween(durationMillis = 900, easing = FastOutSlowInEasing),
    initial: Float = 0f,
): State<Float> {
    val animatable = remember { Animatable(initial) }
    LaunchedEffect(key) {
        animatable.snapTo(initial)
        animatable.animateTo(1f, animationSpec = spec)
    }
    return animatable.asState()
}