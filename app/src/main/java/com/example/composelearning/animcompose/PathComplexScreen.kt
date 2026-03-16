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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.*

@Composable
fun PathsComplexShapesScreen() {
    var selectedDemo by remember { mutableIntStateOf(0) }
    val demos = listOf(
        "Basic Paths",
        "Quadratic Bezier",
        "Cubic Bezier",
        "Heart Shape",
        "Path Operations"
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
                0 -> BasicPathDemo()
                1 -> QuadraticBezierDemo()
                2 -> CubicBezierDemo()
                3 -> HeartShapeDemo()
                4 -> PathOperationsDemo()
            }
        }
    }
}

@Composable
fun BasicPathDemo() {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White)
    ) {
        // Triangle
        val trianglePath = Path().apply {
            moveTo(50.dp.toPx(), 150.dp.toPx())
            lineTo(100.dp.toPx(), 50.dp.toPx())
            lineTo(150.dp.toPx(), 150.dp.toPx())
            close()
        }
        drawPath(
            path = trianglePath,
            color = Color.Blue
        )

        // Star
        val starPath = Path().apply {
            val centerX = 250.dp.toPx()
            val centerY = 100.dp.toPx()
            val outerRadius = 40.dp.toPx()
            val innerRadius = 20.dp.toPx()
            val points = 5

            for (i in 0 until points * 2) {
                val angle = (i * PI / points).toFloat()
                val radius = if (i % 2 == 0) outerRadius else innerRadius
                val x = centerX + (cos(angle) * radius)
                val y = centerY + (sin(angle) * radius)

                if (i == 0) moveTo(x, y)
                else lineTo(x, y)
            }
            close()
        }
        drawPath(
            path = starPath,
            brush = Brush.radialGradient(
                colors = listOf(Color.Yellow, Color.Red),
                center = Offset(250.dp.toPx(), 100.dp.toPx()),
                radius = 40.dp.toPx()
            )
        )

        // Heart
        val heartPath = Path().apply {
            val width = 80.dp.toPx()
            val height = 70.dp.toPx()
            val centerX = 100.dp.toPx()
            val centerY = 200.dp.toPx()

            moveTo(centerX, centerY + height / 4)

            // Left curve
            cubicTo(
                centerX - width / 2, centerY - height / 4,
                centerX - width / 2, centerY + height / 8,
                centerX, centerY + height / 2
            )

            // Right curve
            cubicTo(
                centerX + width / 2, centerY + height / 8,
                centerX + width / 2, centerY - height / 4,
                centerX, centerY + height / 4
            )
        }
        drawPath(
            path = heartPath,
            color = Color.Red
        )
    }
}

@Composable
fun QuadraticBezierDemo() {
    var controlPoint by remember { mutableStateOf(Offset(200f, 150f)) }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.LightGray)
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    controlPoint = change.position
                }
            }
    ) {
        val startPoint = Offset(100f, 200f)
        val endPoint = Offset(300f, 200f)

        // Draw helper lines
        drawLine(
            color = Color.Gray,
            start = startPoint,
            end = controlPoint,
            strokeWidth = 2.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f))
        )
        drawLine(
            color = Color.Gray,
            start = controlPoint,
            end = endPoint,
            strokeWidth = 2.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f))
        )

        // Draw curve
        val path = Path().apply {
            moveTo(startPoint.x, startPoint.y)
            quadraticTo(
                controlPoint.x, controlPoint.y,
                endPoint.x, endPoint.y
            )
        }
        drawPath(
            path = path,
            color = Color.Blue,
            style = Stroke(width = 3.dp.toPx())
        )

        // Draw points
        drawCircle(color = Color.Black, radius = 4.dp.toPx(), center = startPoint)
        drawCircle(color = Color.Black, radius = 4.dp.toPx(), center = endPoint)
        drawCircle(color = Color.Red, radius = 8.dp.toPx(), center = controlPoint)
    }
}

@Composable
fun CubicBezierDemo() {
    var controlPoint1 by remember { mutableStateOf(Offset(150f, 100f)) }
    var controlPoint2 by remember { mutableStateOf(Offset(250f, 200f)) }
    var draggingPoint by remember { mutableStateOf<Int?>(null) }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        draggingPoint = when {
                            (offset - controlPoint1).getDistance() < 30f -> 1
                            (offset - controlPoint2).getDistance() < 30f -> 2
                            else -> null
                        }
                    },
                    onDrag = { change, _ ->
                        when (draggingPoint) {
                            1 -> controlPoint1 = change.position
                            2 -> controlPoint2 = change.position
                        }
                    },
                    onDragEnd = { draggingPoint = null }
                )
            }
    ) {
        val startPoint = Offset(100f, 300f)
        val endPoint = Offset(300f, 300f)

        // Draw helper lines
        drawLine(Color.Gray, startPoint, controlPoint1, 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f)))
        drawLine(Color.Gray, controlPoint2, endPoint, 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f)))

        // Draw curve
        val path = Path().apply {
            moveTo(startPoint.x, startPoint.y)
            cubicTo(
                controlPoint1.x, controlPoint1.y,
                controlPoint2.x, controlPoint2.y,
                endPoint.x, endPoint.y
            )
        }
        drawPath(path, Color.Black, style = Stroke(width = 3.dp.toPx()))

        // Draw points
        drawCircle(Color.Blue, 8f, startPoint)
        drawCircle(Color.Blue, 8f, endPoint)
        drawCircle(Color.Red, 12f, controlPoint1)
        drawCircle(Color.Red, 12f, controlPoint2)
    }
}

@Composable
fun HeartShapeDemo() {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White)
    ) {
        val heartWidth = size.width * 0.8f
        val heartHeight = size.height * 0.7f
        val centerX = size.width / 2
        val centerY = size.height / 2

        val path = Path().apply {
            moveTo(centerX, centerY + heartHeight / 2)

            // Right side
            cubicTo(
                centerX + heartWidth / 2, centerY - heartHeight / 2,
                centerX + heartWidth / 4, centerY - heartHeight,
                centerX, centerY - heartHeight / 4
            )

            // Left side
            cubicTo(
                centerX - heartWidth / 4, centerY - heartHeight,
                centerX - heartWidth / 2, centerY - heartHeight / 2,
                centerX, centerY + heartHeight / 2
            )
            close()
        }

        // Fill
        drawPath(path, Color.Red)

        // Outline
        drawPath(path, Color.Red, style = Stroke(width = 5.dp.toPx()))
    }
}

@Composable
fun PathOperationsDemo() {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White)
    ) {
        val rect1 = Path().apply {
            addRect(Rect(50.dp.toPx(), 50.dp.toPx(), 150.dp.toPx(), 150.dp.toPx()))
        }
        val rect2 = Path().apply {
            addRect(Rect(100.dp.toPx(), 100.dp.toPx(), 200.dp.toPx(), 200.dp.toPx()))
        }

        // Union
        val unionPath = Path().apply {
            op(rect1, rect2, PathOperation.Union)
        }
        drawPath(unionPath, Color.Blue.copy(alpha = 0.7f))

        // Intersection (offset)
        val intersectionPath = Path().apply {
            op(rect1, rect2, PathOperation.Intersect)
        }
        val translateX = 150.dp.toPx()
        withTransform({
            translate(translateX, 0f)
        }) {
            drawPath(intersectionPath, Color.Red)
        }

        // Difference (offset)
        val differencePath = Path().apply {
            op(rect1, rect2, PathOperation.Difference)
        }
        val translateY = 150.dp.toPx()
        withTransform({
            translate(0f, translateY)
        }) {
            drawPath(differencePath, Color.Green)
        }
    }
}
