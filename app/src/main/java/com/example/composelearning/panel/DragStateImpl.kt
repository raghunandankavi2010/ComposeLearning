package com.example.composelearning.panel

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FloatSpringSpec
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.runtime.saveable.Saver


class DragStateImpl(
    currentHeight: Float,
    override val maxHeight: Float
    ) : DragState {

    private val animatable = Animatable(currentHeight)
    private val decayAnimationSpec = FloatSpringSpec(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow,
    )

    override val currentHeight: Float
        get() = animatable.value


    override suspend fun stop() {
        animatable.stop()
    }

    override suspend fun snapTo(value: Float) {
        animatable.snapTo(value.coerceIn(0f,maxHeight))
    }

    override suspend fun decayTo(velocity: Float, value: Float) {
        val target = value.coerceIn(0f,maxHeight)
        animatable.animateTo(
            targetValue = target,
            initialVelocity = velocity,
            animationSpec = decayAnimationSpec,
        )
    }

    companion object {
        val Saver = Saver<DragStateImpl, List<Any>>(
            save = { listOf(it.currentHeight,it.maxHeight) },
            restore = {
                DragStateImpl(
                    currentHeight = it[0] as Float,
                    maxHeight = it[1] as Float,
                )
            }
        )
    }

}