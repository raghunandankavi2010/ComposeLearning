package com.example.composelearning.panel

import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.verticalDrag
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Composable
fun Content(
    boxHeight: Dp,
    state: DragState = rememberDragState(currentHeight = 100f, maxHeight = boxHeight.value),
) {
    Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier.fillMaxSize()) {
        Surface(
            elevation = 5.dp,
            color = Color(0xFF34AB52),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp, max = boxHeight - 20.dp)
                .height(state.currentHeight.dp)
                .clip(RoundedCornerShape(15.dp, 15.dp, 0.dp, 0.dp))
                .drag(state)
        ) {
        }
    }
}

@Composable
fun rememberDragState(
    currentHeight: Float = 0f,
    maxHeight: Float,
): DragState {
    val state = rememberSaveable(saver = DragStateImpl.Saver) {
        DragStateImpl(currentHeight, maxHeight)
    }
    LaunchedEffect(key1 = Unit) {
        state.snapTo(state.currentHeight)
    }
    return state
}

private fun Modifier.drag(
    state: DragState,
) = pointerInput(Unit) {
    val decay = splineBasedDecay<Float>(this)
    coroutineScope {
        while (true) {
            val pointerId = awaitPointerEventScope { awaitFirstDown().id }
            state.stop()
            val tracker = androidx.compose.ui.input.pointer.util.VelocityTracker()
            awaitPointerEventScope {
                verticalDrag(pointerId) { change ->
                    val horizontalDragOffset =
                        state.currentHeight - change.positionChange().y
                    launch {
                        state.snapTo(horizontalDragOffset)
                    }
                    if (change.positionChange() != Offset.Zero) {
                        change.consume()
                    }
                }
                val velocity = tracker.calculateVelocity().y
                val targetValue = decay.calculateTargetValue(state.currentHeight, -velocity)
                launch {
                    state.decayTo(velocity, targetValue)
                }
            }
        }
    }
}