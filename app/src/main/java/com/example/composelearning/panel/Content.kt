package com.example.composelearning.panel

import android.annotation.SuppressLint
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.verticalDrag
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun Content(
    boxWidth: Dp,
    boxHeight: Dp,
    state: DragState = rememberDragState(currentHeight = 100f, maxHeight = boxHeight.value),
) {
    Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier.fillMaxSize()) {
        Surface(
            tonalElevation = 5.dp,
            color = Color(0xFF34AB52),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp, max = boxHeight - 20.dp)
                .height(state.currentHeight.dp)
                .clip(RoundedCornerShape(15.dp, 15.dp, 0.dp, 0.dp))
                .drag(state)
        ) {
            val angle = 180 * (state.currentHeight.dp - 120.dp).value / (boxHeight - 120.dp).value
            // consider angle between 0 and 180 cause height can vary depending on offset
            val currAngle = angle.coerceIn(0f, 180f)

            Box(modifier = Modifier
                .size(50.dp)
                .align(alignment = Alignment.TopCenter)) {
                Icon(
                    Icons.Filled.ArrowDropDown,
                    tint = Color.Black,
                    contentDescription = "print",
                    modifier = Modifier
                        .align(alignment = Alignment.TopCenter)
                        .padding(top = 16.dp)
                        .rotate(currAngle)
                )
            }
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

//@SuppressLint("ReturnFromAwaitPointerEventScope", "MultipleAwaitPointerEventScopes")
//private fun Modifier.drag(
//    state: DragState,
//) = pointerInput(Unit) {
//    val decay = splineBasedDecay<Float>(this)
//    coroutineScope {
//        while (true) {
//            val pointerId = awaitPointerEventScope { awaitFirstDown().id }
//            state.stop()
//            val tracker = androidx.compose.ui.input.pointer.util.VelocityTracker()
//            awaitPointerEventScope {
//                verticalDrag(pointerId) { change ->
//                    val verticalDragOffset =
//                        state.currentHeight - change.positionChange().y
//                    launch {
//                        state.snapTo(verticalDragOffset)
//                    }
//                    if (change.positionChange() != Offset.Zero) {
//                        change.consume()
//                    }
//                }
//                val velocity = tracker.calculateVelocity().y
//                val targetValue = decay.calculateTargetValue(state.currentHeight, -velocity)
//                launch {
//                    state.decayTo(velocity, targetValue)
//                }
//            }
//        }
//    }
//}

@SuppressLint("MultipleAwaitPointerEventScopes", "ReturnFromAwaitPointerEventScope")
private fun Modifier.drag(
    state: DragState,
) = pointerInput(Unit) {
    val decay = splineBasedDecay<Float>(this)
    coroutineScope {
        var snapBack = false // Flag to determine whether to snap back after drag stops
        var snapFwd = false
        while (true) {
            val pointerId = awaitPointerEventScope { awaitFirstDown().id }
            state.stop()
            val tracker = androidx.compose.ui.input.pointer.util.VelocityTracker()
            awaitPointerEventScope {
                verticalDrag(pointerId) { change ->
                    val verticalDragOffset =
                        state.currentHeight - change.positionChange().y
                    launch {
                        state.snapTo(verticalDragOffset)
                    }
                    if (change.positionChange() != Offset.Zero) {
                        change.consume()
                    }
                }
                val velocity = tracker.calculateVelocity().y
                val targetValue = decay.calculateTargetValue(state.currentHeight, -velocity)

                // Check if the drag is less than half of the screen height
                if (abs( state.currentHeight) < (state.maxHeight / 2)) {
                    // Set the snapBack flag when dragging stops
                    snapBack = true
                } else if(state.currentHeight > state.maxHeight / 2) {
                    snapFwd = true
                }

                launch {
                    state.decayTo(velocity, targetValue)

                    // Snap back to the original position after drag stops and the drag is less than half of the screen height
                    if (snapBack) {
                        state.snapTo(100f)
                        snapBack = false
                    } else if(snapFwd) {
                        state.snapTo(state.maxHeight)
                        snapFwd = false
                    }
                }
            }
        }
    }
}

