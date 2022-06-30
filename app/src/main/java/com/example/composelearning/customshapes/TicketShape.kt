package com.example.composelearning.customshapes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


fun drawTicketPath(size: Size, cornerRadius: Float): Path {
    return Path().apply {
        reset()
        // Top left arc
        arcTo(
            rect = Rect(
                left = -cornerRadius,
                top = -cornerRadius,
                right = cornerRadius,
                bottom = cornerRadius
            ),
            startAngleDegrees = 90.0f,
            sweepAngleDegrees = -90.0f,
            forceMoveTo = false
        )
        lineTo(x = size.width - cornerRadius, y = 0f)
        // Top right arc
        arcTo(
            rect = Rect(
                left = size.width - cornerRadius,
                top = -cornerRadius,
                right = size.width + cornerRadius,
                bottom = cornerRadius
            ),
            startAngleDegrees = 180.0f,
            sweepAngleDegrees = -90.0f,
            forceMoveTo = false
        )
        lineTo(x = size.width, y = size.height - cornerRadius)
        // Bottom right arc
        arcTo(
            rect = Rect(
                left = size.width - cornerRadius,
                top = size.height - cornerRadius,
                right = size.width + cornerRadius,
                bottom = size.height + cornerRadius
            ),
            startAngleDegrees = 270.0f,
            sweepAngleDegrees = -90.0f,
            forceMoveTo = false
        )
        lineTo(x = cornerRadius, y = size.height)
        // Bottom left arc
        arcTo(
            rect = Rect(
                left = -cornerRadius,
                top = size.height - cornerRadius,
                right = cornerRadius,
                bottom = size.height + cornerRadius
            ),
            startAngleDegrees = 0.0f,
            sweepAngleDegrees = -90.0f,
            forceMoveTo = false
        )
        lineTo(x = 0f, y = cornerRadius)
        close()
    }
}

@Composable
fun TicketComposable(modifier: Modifier) {
    Text(
        text = "\uD83C\uDF89 CINEMA TICKET \uD83C\uDF89",
        style = TextStyle(
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
        ),
        textAlign = TextAlign.Center,
        modifier = modifier
            .height(200.dp)
            .width(300.dp)
            .graphicsLayer {
                shadowElevation = 8.dp.toPx()
                shape = TicketShape(10.dp.toPx())
                clip = true
            }
            .background(color = Color.Black)
            .drawBehind {
                scale(scale = 0.9f) {
                    drawPath(
                        path = drawTicketPath(size = size, cornerRadius = 25.dp.toPx()),
                        color = Color.Red,
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                        )
                    )
                }
                drawLine(Color.Green,
                    Offset(10.dp.toPx(), size.height / 2),
                    Offset(size.width  - 10.dp.toPx(), size.height / 2),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                )
            }
            .padding(start = 32.dp, top = 64.dp, end = 32.dp, bottom = 64.dp)
    )
}

fun drawTicketPathVariation(size: Size, cornerRadius: Float): Path {
    return Path().apply {
        reset()
        moveTo(0f, 0f)
        lineTo(x = size.width, y = 0f)
        lineTo(x = size.width, y = size.height / 2 - cornerRadius)
        arcTo(
            rect = Rect(
                left = size.width - cornerRadius,
                top = size.height / 2 - cornerRadius,
                right = size.width + cornerRadius,
                bottom = size.height / 2 + cornerRadius
            ),
            startAngleDegrees = 270f,
            sweepAngleDegrees = -180f,
            forceMoveTo = false
        )

        lineTo(x = size.width, y = size.height)
        lineTo(x = 0f, y = size.height)
        lineTo(x = 0f, y = size.height / 2 - cornerRadius)
        arcTo(
            rect = Rect(
                left = -cornerRadius,
                top = size.height / 2 - cornerRadius,
                right = cornerRadius,
                bottom = size.height / 2 + cornerRadius
            ),
            startAngleDegrees = 90f,
            sweepAngleDegrees = -180f,
            forceMoveTo = false
        )

        lineTo(x = 0f, y = 0f)
        close()
    }
}


class TicketShape(private val cornerRadius: Float) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        return Outline.Generic(
            // Draw your custom path here
            path = drawTicketPathVariation(size = size, cornerRadius = cornerRadius)
        )
    }
}