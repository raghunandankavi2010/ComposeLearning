package com.example.composelearning

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun DragGestureTest() {
    val size by remember { mutableStateOf(400.dp) }
    val offsetX = remember { mutableStateOf(0f) }
    val offsetY = remember { mutableStateOf(0f) }
    Box(modifier = Modifier.size(size)){
        Box(
            Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }
                .background(Color.Blue)
                .size(50.dp)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX.value = (offsetX.value + dragAmount.x)
                            .coerceIn(0f, size.value.toFloat() - 50.dp.toPx())

                        offsetY.value = (offsetY.value + dragAmount.y)
                            .coerceIn(0f, size.value.toFloat() - 50.dp.toPx())
                    }
                }
        )
        Text("Drag the box around", Modifier.align(Alignment.Center))
    }
}