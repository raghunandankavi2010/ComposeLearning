package com.example.composelearning.customshapes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

@Composable
fun DraggableLineDrawing() {
    Box(modifier = Modifier.fillMaxSize()) {
        val buttonSize = 50.dp
        val buttonRadius = buttonSize.value / 2
        val buttonsState = remember { List(10) { mutableStateOf(Offset.Zero) } }
        val explodedState = remember { mutableStateListOf<Boolean>().apply { repeat(10) { add(false) } } }
        val secondaryColor = MaterialTheme.colorScheme.secondary
        val brush = Brush.horizontalGradient(listOf(Color.Red, Color.Blue))
        val brushSecond = Brush.horizontalGradient(listOf(Color.Gray, Color.Black))

        Canvas(modifier = Modifier.fillMaxSize()) {
            for (i in buttonsState.indices) {
                for (j in i + 1 until buttonsState.size) {
                    val pos1 = buttonsState[i].value
                    val pos2 = buttonsState[j].value
                    val distance = sqrt((pos1.x - pos2.x).pow(2) + (pos1.y - pos2.y).pow(2))
                    if (distance <= buttonSize.toPx() * 2) {
                        drawLine(
                            start = pos1,
                            end = pos2,
                            brush = brushSecond,
                            strokeWidth = 10f
                        )
                    }
                }
            }

            buttonsState.forEachIndexed { index, state ->
                val explosionProgress = if (explodedState[index]) 1f else 0f
                val radius = buttonRadius * (1 - explosionProgress)

                drawCircle(
                    brush = brush,
                    radius = radius,
                    center = state.value
                )

                if (explodedState[index]) {
                    repeat(20) {
                        val angle = Random.nextFloat() * 2 * Math.PI
                        val distance = radius * 3 * Random.nextFloat()
                        val x = state.value.x + distance * cos(angle).toFloat()
                        val y = state.value.y + distance * sin(angle).toFloat()

                        drawCircle(
                            color = secondaryColor,
                            radius = 5F,
                            center = Offset(x, y)
                        )
                    }
                }
            }
        }

        buttonsState.forEachIndexed { index, state ->
            var offsetX by remember { mutableStateOf(0f) }
            var offsetY by remember { mutableStateOf(0f) }

            if (state.value == Offset.Zero) {
                val angle = 2 * PI / buttonsState.size * index
                offsetX = (cos(angle) * 200f).toFloat()
                offsetY = (sin(angle) * 200f).toFloat()
                state.value = Offset(offsetX, offsetY)
            }

            DraggableButtonDrawing(
                modifier = Modifier
                    .size(buttonSize)
                    .offset(x = offsetX.dp, y = offsetY.dp)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                            state.value = Offset(offsetX, offsetY)
                        }
                    }
                    .clickable { explodedState[index] = !explodedState[index] },
                explosionProgress = if (explodedState[index]) 1f else 0f
            )
        }
    }
}

@Composable
fun DraggableButtonDrawing(modifier: Modifier = Modifier, explosionProgress: Float) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2 * (1 - explosionProgress)

        drawCircle(
            color = primaryColor,
            radius = radius,
            center = center
        )

        if (explosionProgress > 0) {
            for (i in 0..100) {
                val angle = Math.random() * 2 * Math.PI
                val distance = explosionProgress * radius * 3
                val x = center.x + (distance * cos(angle)).toFloat()
                val y = center.y + (distance * sin(angle)).toFloat()

                drawCircle(
                    color = secondaryColor,
                    radius = 5.dp.toPx(),
                    center = Offset(x, y)
                )
            }
        }
    }
}