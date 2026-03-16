package com.example.composelearning.animcompose

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.*

@Composable
fun MathBasicsScreen() {
    var selectedDemo by remember { mutableIntStateOf(0) }
    val demos = listOf(
        "Coordinate System",
        "Vector Movement",
        "Circular Motion",
        "Wave Functions"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Demo selector
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

        // Selected demo
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when (selectedDemo) {
                0 -> MathCoordinateSystemDemo()
                1 -> VectorMovementDemo()
                2 -> CircularMotionDemo()
                3 -> WaveFunctionsDemo()
            }
        }
    }
}

@Composable
fun MathCoordinateSystemDemo() {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White)
    ) {
        val strokeWidth = 2.dp.toPx()

        // Draw coordinate axes
        drawLine(
            color = Color.Black,
            start = Offset(0f, size.height / 2),
            end = Offset(size.width, size.height / 2),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = Color.Black,
            start = Offset(size.width / 2, 0f),
            end = Offset(size.width / 2, size.height),
            strokeWidth = strokeWidth
        )

        // Mark origin (top-left)
        drawCircle(
            color = Color.Red,
            radius = 5.dp.toPx(),
            center = Offset.Zero
        )

        // Mark center
        drawCircle(
            color = Color.Blue,
            radius = 5.dp.toPx(),
            center = center
        )
    }
}

@Composable
fun VectorMovementDemo() {
    var position by remember { mutableStateOf(Offset(200f, 200f)) }
    var velocity by remember { mutableStateOf(Offset(5f, -3f)) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(16) // ~60fps
            position += velocity

            // Bounce off walls
            if (position.x < 0 || position.x > 800f) { // Adjusted for canvas size
                velocity = velocity.copy(x = -velocity.x)
            }
            if (position.y < 0 || position.y > 800f) {
                velocity = velocity.copy(y = -velocity.y)
            }
            position = Offset(
                position.x.coerceIn(0f, 800f),
                position.y.coerceIn(0f, 800f)
            )
        }
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White)
    ) {
        // Draw vector arrow
        val arrowEnd = position + velocity * 10f
        drawLine(
            color = Color.Red,
            start = position,
            end = arrowEnd,
            strokeWidth = 3.dp.toPx()
        )

        // Draw moving object
        drawCircle(
            color = Color.Blue,
            radius = 15.dp.toPx(),
            center = position
        )

        // Draw trail
        for (i in 1..5) {
            val trailPos = position - velocity * (i * 2f)
            drawCircle(
                color = Color.Blue.copy(alpha = 0.3f / i),
                radius = 15.dp.toPx() * (1f - i * 0.15f),
                center = trailPos
            )
        }
    }
}

@Composable
fun CircularMotionDemo() {
    var angle by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            angle += 0.02f
            delay(16)
        }
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White)
    ) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val radius = minOf(centerX, centerY) * 0.6f

        // Draw orbit path
        drawCircle(
            color = Color.Gray.copy(alpha = 0.3f),
            radius = radius,
            center = Offset(centerX, centerY),
            style = Stroke(width = 2.dp.toPx())
        )

        // Draw orbiting object
        val x = centerX + cos(angle) * radius
        val y = centerY + sin(angle) * radius

        drawCircle(
            color = Color.Blue,
            radius = 15.dp.toPx(),
            center = Offset(x, y)
        )

        // Draw central point
        drawCircle(
            color = Color.Red,
            radius = 5.dp.toPx(),
            center = Offset(centerX, centerY)
        )
    }
}

@Composable
fun WaveFunctionsDemo() {
    var phase by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            phase += 0.1f
            delay(16)
        }
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White)
    ) {
        val points = mutableListOf<Offset>()
        val amplitude = size.height / 4f
        val frequency = 4f
        val centerY = size.height / 2

        for (x in 0..size.width.toInt() step 5) {
            val normalizedX = x / size.width * frequency * 2 * PI.toFloat()
            val y = centerY + sin(normalizedX + phase) * amplitude
            points.add(Offset(x.toFloat(), y))
        }

        // Draw wave
        for (i in 0 until points.size - 1) {
            drawLine(
                color = Color.Blue,
                start = points[i],
                end = points[i + 1],
                strokeWidth = 3.dp.toPx()
            )
        }

        // Draw axis
        drawLine(
            color = Color.Gray.copy(alpha = 0.3f),
            start = Offset(0f, centerY),
            end = Offset(size.width, centerY),
            strokeWidth = 1.dp.toPx()
        )
    }
}
