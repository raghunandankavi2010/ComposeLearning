package com.example.composelearning.speedometer

import android.annotation.SuppressLint
import android.graphics.Paint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin


@SuppressLint("UnrememberedAnimatable")
@Composable
fun Speedometer(
    progress: Int,
) {
    val arcDegrees = 275
    val startArcAngle = 135f
    val startAngleRadians = startArcAngle * (PI / 180f)
    val progressInRadians = (progress * (arcDegrees) / 100f) * (PI / 180f)
    val endProgressInRadians = startAngleRadians + progressInRadians
    // remember
    val progressAnimation = remember {
        Animatable(0f)
    }
    val pointerAnimation = remember {
        Animatable(startAngleRadians.toFloat())
    }

    LaunchedEffect(progress) {
        launch {
            pointerAnimation.animateTo(
                targetValue = endProgressInRadians.toFloat(),
                animationSpec = tween(durationMillis = 3000, easing = LinearEasing))
        }
        launch {
            progressAnimation.animateTo(
                targetValue = progress.toFloat(),
                animationSpec = tween(durationMillis = 3000, easing = LinearEasing))
        }
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        onDraw = {
            drawIntoCanvas { canvas ->
                val w = drawContext.size.width
                val h = drawContext.size.height
                val centerOffset = Offset(w / 2f, h / 2f)
                val quarterOffset = Offset(w / 4f, h / 4f)
                // Drawing Center Arc background
                val (mainColor, secondaryColor) = when {
                    progressAnimation.value < 100 -> // Red
                        Color(0xFFD32F2F) to Color(0xFFFFCDD2)
                    progressAnimation.value < 200 -> // Orange
                        Color(0xFFF57C00) to Color(0xFFFFE0B2)
                    else -> // Green
                        Color(0xFF388E3C) to Color(0xFFC8E6C9)
                }
                val centerArcSize = Size(w / 2f, h / 2f)
                val centerArcStroke = Stroke(20f, 0f, StrokeCap.Round)
                drawArc(
                    secondaryColor,
                    startArcAngle,
                    arcDegrees.toFloat(),
                    false,
                    topLeft = quarterOffset,
                    size = centerArcSize,
                    style = centerArcStroke
                )
                // Drawing Center Arc progress
                drawArc(
                    mainColor,
                    startArcAngle,
                    progressAnimation.value * (arcDegrees) / 100f,
                    false,
                    topLeft = quarterOffset,
                    size = centerArcSize,
                    style = centerArcStroke
                )
                // Drawing the pointer circle
                drawCircle(mainColor, 20f, centerOffset)

                // draw pointer
                val r = w / 4f
                val x = (r - 30f) * cos(pointerAnimation.value) + w / 2
                val y = (r - 30f) * sin(pointerAnimation.value) + h / 2
                println("Times Called")
                drawLine(mainColor, Offset(w / 2, h / 2), Offset(x, y), 20f, StrokeCap.Round)
                var markerAngleRadians: Double
                var textValue = 0
                var angleStart = 135f
                val angleEnd = 410f
                // draw markers
                while (angleStart <= angleEnd) {
                    markerAngleRadians = angleStart * (PI / 180f)
                    val x1 = (r - 10f) * cos(markerAngleRadians) + w / 2
                    val y1 = (r - 10f) * sin(markerAngleRadians) + h / 2
                    val x2 = (r + 10f) * cos(markerAngleRadians) + w / 2
                    val y2 = (r + 10f) * sin(markerAngleRadians) + h / 2
                    val tx = (r + 40f) * cos(markerAngleRadians) + w / 2
                    val ty = (r + 40f) * sin(markerAngleRadians) + h / 2

                    drawContext.canvas.nativeCanvas.apply {
                        drawText(
                            "$textValue",
                            tx.toFloat(),
                            ty.toFloat(),
                            Paint().apply {
                                textSize = 20f
                                color = android.graphics.Color.BLACK
                                textAlign = Paint.Align.CENTER
                            }
                        )
                    }
                    textValue += 10

                    drawLine(Color.Black,
                        Offset(x1.toFloat(), y1.toFloat()),
                        Offset(x2.toFloat(), y2.toFloat()),
                        5f,
                        StrokeCap.Square)

                    angleStart += 27.5f
                }
            }
        }
    )
}
