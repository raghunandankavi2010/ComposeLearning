package com.example.composelearning.pager

import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

class FlingBehaviourMultiplier(
    private val multiplier: Float,
    private val baseFlingBehavior: FlingBehavior
) : FlingBehavior {
    override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
        return with(baseFlingBehavior) {
            performFling(initialVelocity * multiplier)
        }
    }
}

@Composable
fun rememberFlingBehaviorMultiplier(
    multiplier: Float,
    baseFlingBehavior: FlingBehavior
) = remember(multiplier, baseFlingBehavior) {
    FlingBehaviourMultiplier(multiplier, baseFlingBehavior)
}