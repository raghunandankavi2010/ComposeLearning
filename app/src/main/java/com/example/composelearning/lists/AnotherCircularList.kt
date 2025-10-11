package com.example.composelearning.lists

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun CircularList(
    itemCount: Int,
    radius: Dp = 300.dp,
    itemContent: @Composable (index: Int) -> Unit
) {
    val scope = rememberCoroutineScope()
    val radiusPx = with(LocalDensity.current) { radius.toPx() }
    val rotation = remember { Animatable(0f) }
    val velocityTracker = remember { VelocityTracker() }
    val decay: DecayAnimationSpec<Float> = rememberSplineBasedDecay()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        scope.launch { rotation.stop() }
                        velocityTracker.resetTracking()
                    },
                    onDragEnd = {
                        val velocity = velocityTracker.calculateVelocity().x
                        scope.launch {
                            rotation.animateDecay(
                                initialVelocity = velocity * 0.2f,
                                animationSpec = decay
                            )
                        }
                    },
                    onDrag = { change: PointerInputChange, dragAmount: Offset ->
                        velocityTracker.addPosition(change.uptimeMillis, change.position)
                        scope.launch {
                            rotation.snapTo((rotation.value + dragAmount.x * 0.15f) % 360f)
                        }
                        change.consume()
                    }
                )
            }
    ) {
        Layout(
            content = {
                repeat(itemCount) { i -> itemContent(i) }
            },
            modifier = Modifier.fillMaxSize()
        ) { measurables, constraints ->
            val layoutWidth = constraints.maxWidth
            val layoutHeight = constraints.maxHeight
            val centerX = layoutWidth.toFloat()
            val centerY = layoutHeight.toFloat()
            val angleStep = 360f / itemCount

            val placeables = measurables.map { it.measure(constraints) }

            layout(layoutWidth, layoutHeight) {
                placeables.forEachIndexed { index, placeable ->
                    val angle = index * angleStep + rotation.value - 90
                    val angleRad = Math.toRadians(angle.toDouble())
                    val x = centerX + radiusPx * cos(angleRad) - placeable.width / 2
                    val y = centerY + radiusPx * sin(angleRad) - placeable.height / 2
                    placeable.place(x.roundToInt(), y.roundToInt())
                }
            }
        }
    }
}

@Composable
fun CircularListDemo() {
    val itemCount = 15

    CircularList(itemCount = itemCount, radius = 300.dp) { index ->
        val hue = (index * 360f / itemCount) % 360f
        val color = Color.hsv(hue, 0.8f, 0.9f)

        Box(
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = color,
                        shape = CircleShape
                    )
            )
        }
    }
}