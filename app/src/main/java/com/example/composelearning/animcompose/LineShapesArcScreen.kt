package com.example.composelearning.animcompose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun LinesShapesArcsScreen() {
    var selectedDemo by remember { mutableIntStateOf(0) }
    val demos = listOf(
        "Primitives",
        "Fill vs Stroke",
        "Gradients",
        "Audio Visualizer"
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
                0 -> PrimitivesDemo()
                1 -> FillStrokeDemo()
                2 -> GradientsDemo()
                3 -> AudioVisualizerDemo()
            }
        }
    }
}

@Composable
fun PrimitivesDemo() {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White)
    ) {
        // Line
        drawLine(
            color = Color.Black,
            start = Offset(50.dp.toPx(), 50.dp.toPx()),
            end = Offset(200.dp.toPx(), 100.dp.toPx()),
            strokeWidth = 4.dp.toPx()
        )

        // Circle
        drawCircle(
            color = Color.Red,
            radius = 30.dp.toPx(),
            center = Offset(300.dp.toPx(), 80.dp.toPx())
        )

        // Rectangle
        drawRect(
            color = Color.Blue,
            topLeft = Offset(50.dp.toPx(), 150.dp.toPx()),
            size = Size(80.dp.toPx(), 60.dp.toPx())
        )

        // Rounded Rectangle
        drawRoundRect(
            color = Color.Green,
            topLeft = Offset(200.dp.toPx(), 150.dp.toPx()),
            size = Size(80.dp.toPx(), 60.dp.toPx()),
            cornerRadius = CornerRadius(15.dp.toPx())
        )

        // Oval
        drawOval(
            color = Color.Magenta,
            topLeft = Offset(300.dp.toPx(), 150.dp.toPx()),
            size = Size(80.dp.toPx(), 50.dp.toPx())
        )

        // Arc (sector)
        drawArc(
            color = Color.Yellow,
            startAngle = 0f,
            sweepAngle = 120f,
            useCenter = true,
            topLeft = Offset(50.dp.toPx(), 250.dp.toPx()),
            size = Size(60.dp.toPx(), 60.dp.toPx())
        )

        // Arc (outline)
        drawArc(
            color = Color.Cyan,
            startAngle = 180f,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = Offset(150.dp.toPx(), 250.dp.toPx()),
            size = Size(60.dp.toPx(), 60.dp.toPx()),
            style = Stroke(width = 4.dp.toPx())
        )
    }
}

@Composable
fun FillStrokeDemo() {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.LightGray)
    ) {
        val rectSize = Size(80.dp.toPx(), 60.dp.toPx())
        val strokeWidth = 4.dp.toPx()

        // Filled rectangle
        drawRect(
            color = Color.Blue,
            topLeft = Offset(20.dp.toPx(), 20.dp.toPx()),
            size = rectSize
        )

        // Stroked rectangle
        drawRect(
            color = Color.Red,
            topLeft = Offset(120.dp.toPx(), 20.dp.toPx()),
            size = rectSize,
            style = Stroke(width = strokeWidth)
        )

        // Dashed rectangle
        drawRect(
            color = Color.Green,
            topLeft = Offset(220.dp.toPx(), 20.dp.toPx()),
            size = rectSize,
            style = Stroke(
                width = strokeWidth,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 5f))
            )
        )

        // Fill + Stroke (two overlapping rectangles)
        drawRect(
            color = Color.Yellow,
            topLeft = Offset(20.dp.toPx(), 100.dp.toPx()),
            size = rectSize
        )
        drawRect(
            color = Color.Black,
            topLeft = Offset(20.dp.toPx(), 100.dp.toPx()),
            size = rectSize,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

@Composable
fun GradientsDemo() {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.Black)
    ) {
        val rectSize = Size(100.dp.toPx(), 80.dp.toPx())

        // Linear gradient
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(Color.Red, Color.Yellow),
                start = Offset.Zero,
                end = Offset(rectSize.width, 0f)
            ),
            topLeft = Offset(20.dp.toPx(), 20.dp.toPx()),
            size = rectSize
        )

        // Radial gradient
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, Color.Blue, Color.Black),
                center = Offset(190.dp.toPx(), 60.dp.toPx()),
                radius = 50.dp.toPx()
            ),
            topLeft = Offset(140.dp.toPx(), 20.dp.toPx()),
            size = rectSize
        )

        // Sweep gradient
        drawRect(
            brush = Brush.sweepGradient(
                colors = listOf(Color.Red, Color.Green, Color.Blue, Color.Red),
                center = Offset(290.dp.toPx(), 60.dp.toPx())
            ),
            topLeft = Offset(260.dp.toPx(), 20.dp.toPx()),
            size = rectSize
        )

        // Horizontal gradient
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color.Cyan, Color.Magenta, Color.Yellow)
            ),
            topLeft = Offset(20.dp.toPx(), 120.dp.toPx()),
            size = rectSize
        )

        // Vertical gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Green, Color.White, Color.Red)
            ),
            topLeft = Offset(140.dp.toPx(), 120.dp.toPx()),
            size = rectSize
        )
    }
}

@Composable
fun AudioVisualizerDemo() {
    val random = remember { Random(12345) }
    var barHeights by remember { mutableStateOf(List(12) { 0f }) }

    LaunchedEffect(Unit) {
        while (true) {
            barHeights = List(12) {
                random.nextFloat() * 0.75f + 0.2f
            }
            delay(100)
        }
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.Black)
    ) {
        val barCount = 12
        val spacing = 4.dp.toPx()
        val totalSpacingWidth = spacing * (barCount - 1)
        val barWidth = (size.width - totalSpacingWidth) / barCount

        barHeights.forEachIndexed { index, height ->
            val barLeft = index * (barWidth + spacing)
            val barTop = size.height - (height * size.height * 0.8f)
            val barActualHeight = size.height - barTop

            drawRoundRect(
                color = Color.Cyan,
                topLeft = Offset(barLeft, barTop),
                size = Size(barWidth, barActualHeight),
                cornerRadius = CornerRadius(barWidth / 4)
            )
        }
    }
}
