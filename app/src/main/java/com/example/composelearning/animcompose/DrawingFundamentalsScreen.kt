package com.example.composelearning.animcompose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun DrawingFundamentalsScreen() {
    var selectedDemo by remember { mutableIntStateOf(0) }
    val demos: List<Pair<String, @Composable () -> Unit>> = listOf(
        "Simple Drawing" to { SimpleDrawingDemo() },
        "Simple Landscape" to { SimpleLandscapeDemo() },
        "Interactive Shapes" to { InteractiveShapeDrawer() }
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
fun SimpleDrawingDemo() {
    var circleColor by remember { mutableStateOf(Color.Blue) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = { circleColor = Color.Blue }) { Text("Blue") }
            Button(onClick = { circleColor = Color.Red }) { Text("Red") }
            Button(onClick = { circleColor = Color.Green }) { Text("Green") }
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Color.LightGray)
        ) {
            // Draw grid
            val gridSpacing = 20.dp.toPx()
            val gridColor = Color.Gray.copy(alpha = 0.3f)

            for (x in 0 until (size.width / gridSpacing).toInt()) {
                drawLine(
                    color = gridColor,
                    start = Offset(x * gridSpacing, 0f),
                    end = Offset(x * gridSpacing, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }

            for (y in 0 until (size.height / gridSpacing).toInt()) {
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y * gridSpacing),
                    end = Offset(size.width, y * gridSpacing),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Draw circle
            drawCircle(
                color = circleColor,
                radius = 40.dp.toPx(),
                center = center
            )

            // Draw border
            drawCircle(
                color = Color.Black,
                radius = 40.dp.toPx(),
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}

@Composable
fun SimpleLandscapeDemo() {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White)
    ) {
        val skyHeight = size.height * 2f / 3f
        val groundHeight = size.height - skyHeight

        // Sky
        drawRect(
            color = Color(0xFF87CEEB), // Light Sky Blue
            size = Size(size.width, skyHeight)
        )

        // Sun
        val sunRadius = size.width * 0.1f
        drawCircle(
            color = Color.Yellow,
            radius = sunRadius,
            center = Offset(size.width * 0.8f, sunRadius * 1.5f)
        )

        // Ground
        drawRect(
            color = Color(0xFF90EE90), // Light Green
            topLeft = Offset(0f, skyHeight),
            size = Size(size.width, groundHeight)
        )

        // Mountain
        val path = Path().apply {
            val mountainBottom = skyHeight
            val mountainTop = skyHeight - size.height * 0.3f
            val mountainLeft = size.width * 0.3f
            val mountainRight = size.width * 0.7f

            moveTo(mountainLeft, mountainBottom)
            lineTo((mountainLeft + mountainRight) / 2f, mountainTop)
            lineTo(mountainRight, mountainBottom)
            close()
        }

        drawPath(
            path = path,
            color = Color(0xFF8B4513) // Saddle Brown
        )
    }
}

enum class ShapeType { CIRCLE, RECTANGLE, TRIANGLE }

data class DrawnShape(
    val type: ShapeType,
    val position: Offset,
    val color: Color,
    val size: Float
)

@Composable
fun InteractiveShapeDrawer() {
    var shapes by remember { mutableStateOf(listOf<DrawnShape>()) }
    var selectedShape by remember { mutableStateOf(ShapeType.CIRCLE) }
    var selectedColor by remember { mutableStateOf(Color.Blue) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Shape selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ShapeType.entries.forEach { shapeType ->
                FilterChip(
                    onClick = { selectedShape = shapeType },
                    label = { Text(shapeType.name) },
                    selected = selectedShape == shapeType
                )
            }
        }

        // Color selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val colors = listOf(Color.Blue, Color.Red, Color.Green, Color.Yellow, Color.Magenta)
            colors.forEach { color ->
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(color, shape = CutCornerShape(4.dp))
                        .border(
                            2.dp,
                            if (selectedColor == color) Color.Black else Color.Transparent,
                            CutCornerShape(4.dp)
                        )
                        .clickable { selectedColor = color }
                )
            }
        }

        // Clear button
        Button(
            onClick = { shapes = emptyList() },
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text("Clear Canvas")
        }

        // Drawing canvas
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.White)
                .pointerInput(selectedShape, selectedColor) {
                    detectTapGestures { offset ->
                        val newShape = DrawnShape(
                            type = selectedShape,
                            position = offset,
                            color = selectedColor,
                            size = 30f
                        )
                        shapes = shapes + newShape
                    }
                }
        ) {
            shapes.forEach { shape ->
                when (shape.type) {
                    ShapeType.CIRCLE -> {
                        drawCircle(
                            color = shape.color,
                            radius = shape.size,
                            center = shape.position
                        )
                    }
                    ShapeType.RECTANGLE -> {
                        drawRect(
                            color = shape.color,
                            topLeft = Offset(
                                shape.position.x - shape.size,
                                shape.position.y - shape.size
                            ),
                            size = Size(shape.size * 2, shape.size * 2)
                        )
                    }
                    ShapeType.TRIANGLE -> {
                        val path = Path().apply {
                            moveTo(shape.position.x, shape.position.y - shape.size)
                            lineTo(shape.position.x - shape.size, shape.position.y + shape.size)
                            lineTo(shape.position.x + shape.size, shape.position.y + shape.size)
                            close()
                        }
                        drawPath(
                            path = path,
                            color = shape.color
                        )
                    }
                }
            }
        }
    }
}
