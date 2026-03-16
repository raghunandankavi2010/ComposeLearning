package com.example.composelearning.animcompose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CanvasStateScreen() {
    var selectedDemo by remember { mutableIntStateOf(0) }
    val demos = listOf(
        "Save/Restore",
        "Transformations",
        "Clipping",
        "Interactive Canvas"
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
                    label = { Text(demos[index]) },
                    selected = selectedDemo == index
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when (selectedDemo) {
                0 -> SaveRestoreDemo()
                1 -> TransformationsDemo()
                2 -> ClippingDemo()
                3 -> InteractiveCanvasDemo()
            }
        }
    }
}

@Composable
fun SaveRestoreDemo() {
    var rotation by remember { mutableFloatStateOf(0f) }

    Column {
        Slider(
            value = rotation,
            onValueChange = { rotation = it },
            valueRange = 0f..360f,
            modifier = Modifier.padding(16.dp)
        )

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(16.dp)
                .background(Color.White)
        ) {
            // Draw reference axes
            drawLine(Color.Gray, Offset(0f, size.height / 2), Offset(size.width, size.height / 2), 1f)
            drawLine(Color.Gray, Offset(size.width / 2, 0f), Offset(size.width / 2, size.height), 1f)

            // Save state manually is not typically needed in DrawScope as we use transform blocks,
            // but we can demonstrate withTransform
            withTransform({
                translate(size.width / 2, size.height / 2)
                rotate(rotation)
                scale(1.5f)
            }) {
                drawRect(
                    color = Color.Red,
                    topLeft = Offset(-25.dp.toPx(), -25.dp.toPx()),
                    size = Size(50.dp.toPx(), 50.dp.toPx())
                )
            }

            // Draw reference shape (outside transform block, so it's "restored")
            drawRect(
                color = Color.Blue,
                topLeft = Offset(50.dp.toPx(), 50.dp.toPx()),
                size = Size(50.dp.toPx(), 50.dp.toPx())
            )
        }
    }
}

@Composable
fun TransformationsDemo() {
    var scaleX by remember { mutableFloatStateOf(1f) }
    var scaleY by remember { mutableFloatStateOf(1f) }

    Column {
        Text("Scale X: ${String.format("%.2f", scaleX)}", modifier = Modifier.padding(horizontal = 16.dp))
        Slider(
            value = scaleX,
            onValueChange = { scaleX = it },
            valueRange = 0.5f..3f,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Text("Scale Y: ${String.format("%.2f", scaleY)}", modifier = Modifier.padding(horizontal = 16.dp))
        Slider(
            value = scaleY,
            onValueChange = { scaleY = it },
            valueRange = 0.5f..3f,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(16.dp)
                .background(Color.LightGray)
        ) {
            translate(center.x, center.y) {
                // Draw grid
                for (i in -2..2) {
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.3f),
                        start = Offset(i * 40.dp.toPx(), -120.dp.toPx()),
                        end = Offset(i * 40.dp.toPx(), 120.dp.toPx()),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.3f),
                        start = Offset(-120.dp.toPx(), i * 40.dp.toPx()),
                        end = Offset(120.dp.toPx(), i * 40.dp.toPx()),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                scale(scaleX, scaleY) {
                    // Rectangle
                    drawRect(
                        color = Color.Red,
                        topLeft = Offset(-30.dp.toPx(), -20.dp.toPx()),
                        size = Size(60.dp.toPx(), 40.dp.toPx())
                    )
                    // Circle
                    drawCircle(
                        color = Color.Blue,
                        radius = 25.dp.toPx(),
                        center = Offset.Zero
                    )
                }
            }
        }
    }
}

enum class ClipType { RECTANGLE, CIRCLE, STAR, NONE }

@Composable
fun ClippingDemo() {
    var clipType by remember { mutableStateOf(ClipType.RECTANGLE) }

    Column {
        LazyRow(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val values = ClipType.entries.toTypedArray()
            items(values.size) { index ->
                FilterChip(
                    onClick = { clipType = values[index] },
                    label = { Text(values[index].name) },
                    selected = clipType == values[index]
                )
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(16.dp)
                .background(Color.White)
        ) {
            when (clipType) {
                ClipType.RECTANGLE -> {
                    clipRect(50.dp.toPx(), 50.dp.toPx(), 250.dp.toPx(), 250.dp.toPx()) {
                        drawColorfulCircles()
                    }
                    // Draw clip boundary
                    drawRect(
                        Color.Black,
                        topLeft = Offset(50.dp.toPx(), 50.dp.toPx()),
                        size = Size(200.dp.toPx(), 200.dp.toPx()),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
                ClipType.CIRCLE -> {
                    clipPath(
                        Path().apply {
                            addOval(Rect(center.x - 100f, center.y - 100f, center.x + 100f, center.y + 100f))
                        }
                    ) {
                        drawColorfulCircles()
                    }
                    // Draw clip boundary
                    drawCircle(
                        Color.Black,
                        radius = 100f,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
                ClipType.STAR -> {
                    val starPath = createStarPath(center, 100.dp.toPx(), 50.dp.toPx(), 6)
                    clipPath(starPath) {
                        drawColorfulCircles()
                    }
                    // Draw clip boundary
                    drawPath(
                        starPath,
                        Color.Black,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
                ClipType.NONE -> {
                    drawColorfulCircles()
                }
            }
        }
    }
}

fun DrawScope.drawColorfulCircles() {
    for (i in 0..10) {
        val radius = i * 15.dp.toPx()
        val hue = (i * 36f) % 360f
        drawCircle(
            color = Color.hsv(hue, 1f, 1f),
            radius = radius,
            center = center,
            style = Stroke(width = 8.dp.toPx())
        )
    }
}

fun createStarPath(center: Offset, outerRadius: Float, innerRadius: Float, points: Int): Path {
    return Path().apply {
        for (i in 0 until points * 2) {
            val angle = (i * Math.PI / points - Math.PI / 2).toFloat()
            val radius = if (i % 2 == 0) outerRadius else innerRadius
            val x = center.x + cos(angle) * radius
            val y = center.y + sin(angle) * radius
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }
}

data class CanvasState(
    val zoom: Float = 1f,
    val pan: Offset = Offset.Zero,
    val rotation: Float = 0f
)

@Composable
fun InteractiveCanvasDemo() {
    var canvasState by remember { mutableStateOf(CanvasState()) }

    Column {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = { canvasState = canvasState.copy(zoom = canvasState.zoom * 1.2f) }) {
                Text("Zoom In")
            }
            Button(onClick = { canvasState = canvasState.copy(zoom = canvasState.zoom / 1.2f) }) {
                Text("Zoom Out")
            }
            Button(onClick = { canvasState = CanvasState() }) {
                Text("Reset")
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(16.dp)
                .background(Color.White)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        canvasState = canvasState.copy(pan = canvasState.pan + dragAmount)
                    }
                }
        ) {
            translate(canvasState.pan.x, canvasState.pan.y) {
                scale(canvasState.zoom) {
                    rotate(canvasState.rotation) {
                        // Draw grid
                        for (x in 0..size.width.toInt() step 50) {
                            drawLine(
                                Color.Gray.copy(alpha = 0.3f),
                                Offset(x.toFloat(), 0f),
                                Offset(x.toFloat(), size.height),
                                1f
                            )
                        }
                        for (y in 0..size.height.toInt() step 50) {
                            drawLine(
                                Color.Gray.copy(alpha = 0.3f),
                                Offset(0f, y.toFloat()),
                                Offset(size.width, y.toFloat()),
                                1f
                            )
                        }

                        // Draw shapes
                        drawRect(
                            Color.Blue,
                            topLeft = Offset(100f, 100f),
                            size = Size(100.dp.toPx(), 50.dp.toPx())
                        )
                        drawCircle(
                            Color.Red,
                            radius = 30.dp.toPx(),
                            center = Offset(300f, 150f)
                        )
                    }
                }
            }
        }
    }
}
