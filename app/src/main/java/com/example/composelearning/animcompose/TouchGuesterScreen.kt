package com.example.composelearning.animcompose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

@Composable
fun TouchGesturesScreen() {
    var selectedDemo by remember { mutableIntStateOf(0) }
    val demos: List<Pair<String, @Composable () -> Unit>> = listOf(
        "Basic Touch" to { BasicTouchDemo() },
        "Simple Drag" to { SimpleDragDemo() },
        "Multi-Element" to { MultiElementDragDemo() },
        "Pinch Zoom" to { PinchZoomDemo() },
        "Gesture Priority" to { GesturePriorityDemo() }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyRow(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(demos.size) { index ->
                FilterChip(
                    onClick = { selectedDemo = index },
                    label = { Text(demos[index].first) },
                    selected = selectedDemo == index
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            demos[selectedDemo].second()
        }
    }
}

@Composable
fun BasicTouchDemo() {
    var touchInfo by remember { mutableStateOf("Tap, Long Press, or Double Tap anywhere") }
    var tapPoint by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        tapPoint = offset
                        touchInfo = "Single Tap at: ${offset.x.roundToInt()}, ${offset.y.roundToInt()}"
                    },
                    onDoubleTap = { offset ->
                        tapPoint = offset
                        touchInfo = "Double Tap at: ${offset.x.roundToInt()}, ${offset.y.roundToInt()}"
                    },
                    onLongPress = { offset ->
                        tapPoint = offset
                        touchInfo = "Long Press at: ${offset.x.roundToInt()}, ${offset.y.roundToInt()}"
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        if (tapPoint != Offset.Zero) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = Color.Red.copy(alpha = 0.5f),
                    radius = 40.dp.toPx(),
                    center = tapPoint,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
        Text(text = touchInfo, modifier = Modifier.padding(16.dp))
    }
}

@Composable
fun SimpleDragDemo() {
    var offset by remember { mutableStateOf(Offset(100f, 100f)) }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .offset(
                    x = (offset.x / 2.5).dp, // Crude conversion for demo
                    y = (offset.y / 2.5).dp
                )
                .background(Color.Blue, RoundedCornerShape(8.dp))
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offset += dragAmount
                    }
                }
        )
    }
}

@Composable
fun MultiElementDragDemo() {
    var box1Offset by remember { mutableStateOf(Offset(50f, 50f)) }
    var box2Offset by remember { mutableStateOf(Offset(200f, 200f)) }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .graphicsLayer {
                    translationX = box1Offset.x
                    translationY = box1Offset.y
                }
                .background(Color.Green, RoundedCornerShape(8.dp))
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        box1Offset += dragAmount
                    }
                }
        )
        Box(
            modifier = Modifier
                .size(80.dp)
                .graphicsLayer {
                    translationX = box2Offset.x
                    translationY = box2Offset.y
                }
                .background(Color.Magenta, RoundedCornerShape(8.dp))
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        box2Offset += dragAmount
                    }
                }
        )
    }
}

@Composable
fun PinchZoomDemo() {
    var scale by remember { mutableFloatStateOf(1f) }
    var rotation by remember { mutableFloatStateOf(0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, rotate ->
                    scale *= zoom
                    rotation += rotate
                    offset += pan
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    rotationZ = rotation
                    translationX = offset.x
                    translationY = offset.y
                }
                .background(Color.Red, RoundedCornerShape(16.dp))
        )
    }
}

@Composable
fun GesturePriorityDemo() {
    var parentTapCount by remember { mutableIntStateOf(0) }
    var childTapCount by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
            .pointerInput(Unit) {
                detectTapGestures { parentTapCount++ }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Parent taps: $parentTapCount")
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .background(Color.DarkGray)
                    .pointerInput(Unit) {
                        detectTapGestures { childTapCount++ }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("Child taps: $childTapCount", color = Color.White)
            }
        }
    }
}
