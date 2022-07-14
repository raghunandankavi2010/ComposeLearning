package com.example.composelearning.panel

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
fun Content(
    boxHeight: Dp,
) {
    val offsetY = remember { mutableStateOf(0f) }
    val height = remember { mutableStateOf(100.dp) }
    val half = (boxHeight - 40.dp)

    Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier.fillMaxSize()) {
        val calHeight = if (height.value + offsetY.value.dp < half) {
            height.value + offsetY.value.dp
        } else {
            half
        }
        Surface(
            color = Color(0xFF34AB52),
            modifier = Modifier
                .fillMaxWidth()
                .height(calHeight)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetY.value = (offsetY.value - dragAmount.y)
                            .coerceIn(0f, boxHeight.toPx())
                    }
                }
        ) {
        }
    }
}