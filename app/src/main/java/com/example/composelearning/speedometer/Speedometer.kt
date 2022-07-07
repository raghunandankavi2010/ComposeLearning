package com.example.composelearning.speedometer

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SpeedometerScreen() {
    var targetValue by remember {
        mutableStateOf(0f)
    }
    val progress = remember { mutableStateOf(0f) }
    Column(Modifier.padding(16.dp)) {
        Slider(value = targetValue, onValueChangeFinished = {
        }, onValueChange = { targetValue = it }, valueRange = 0f..100f)

        Text(text = "${targetValue.toInt()}")
        Button(onClick = {
            progress.value = targetValue
        }) {
            Text(text = "Reset")
        }
        Speedometer(progress.value.toInt())
    }
}

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

    // remember is not used so as to restart the animation from start
    val progressAnimation = Animatable(0f)
    val pointerAnimation = Animatable(startAngleRadians.toFloat())

    /** restart @LaunchedEffect
     *  when progress changes
     * **/
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
                    progress < 20 -> // Red
                        Color(0xFFD32F2F) to Color(0xFFFFCDD2)
                    progress < 40 -> // Orange
                        Color(0xFFF57C00) to Color(0xFFFFE0B2)
                    else -> // Green
                        Color(0xFF388E3C) to Color(0xFFC8E6C9)
                }
                val paint = Paint().apply {
                    color = mainColor
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
                val x = r * cos(pointerAnimation.value) + w / 2
                val y = r * sin(pointerAnimation.value) + w / 2

                canvas.drawLine(Offset(w / 2, h / 2), Offset(x, y), paint)
            }
        }
    )
}