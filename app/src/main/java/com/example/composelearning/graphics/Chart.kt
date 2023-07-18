package com.example.composelearning.graphics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas

@Composable
fun Chart() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        drawIntoCanvas {
            val width = size.width
            val radius = width / 2f
            val strokeWidth = radius * .3f
            var startAngle = 0f

            val items = listOf(25f, 25f, 25f, 25f, 25f, 25f, 25f, 25f)

            items.forEach {
                val sweepAngle = it.toAngle

                drawArc(
                    color = Color.Gray,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle - 5,
                    useCenter = false,
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                    size = Size(width - strokeWidth, width - strokeWidth),
                    style = Stroke(strokeWidth)
                )

                startAngle += sweepAngle
            }
        }
    }
}

private val Float.toAngle: Float
    get() = this * 180 / 100
