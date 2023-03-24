package com.example.composelearning.animcompose

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun SinWave() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val path = Path()
        val amplitude = 100f
        val frequency = 10f
        val phase = 0f
        val yCenter = size.height / 2f
        path.moveTo(100f, yCenter)
        for (x in 100..size.width.toInt() - 100) {
            val y = amplitude * sin((x * frequency + phase) * PI.toFloat() / 180f) + yCenter
            path.lineTo(x.toFloat(), y)
        }
        drawPath(path, color = Color.Red, style = Stroke(width = 5f))
    }
}


/**
 * Animation of path using segments inspired from beloq
 * https://stackoverflow.com/questions/75745905/rectangle-border-progress-bar/75747893#75747893
 */
@Composable
fun TutorialContent() {

    val startDurationInSeconds = 20
    var currentTime by remember {
        mutableStateOf(startDurationInSeconds)
    }

    var targetValue by remember {
        mutableStateOf(0f)
    }

    var timerStarted by remember {
        mutableStateOf(false)
    }

    val progress by animateFloatAsState(
        targetValue = targetValue,
        animationSpec = tween(startDurationInSeconds * 1000, easing = LinearEasing)
    )

    LaunchedEffect(key1 = timerStarted) {
        if (timerStarted) {
            while (currentTime > 0) {
                delay(1000)
                currentTime--
            }
        }
        timerStarted = false

    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // This is the progress path which wis changed using path measure
        val pathWithProgress by remember {
            mutableStateOf(Path())
        }

        // using path
        val pathMeasure by remember { mutableStateOf(PathMeasure()) }

        val path = remember {
            Path()
        }

        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .clickable {
                    if (currentTime > 0) {
                        targetValue = 100f
                        timerStarted = true
                    } else {
                        currentTime = startDurationInSeconds
                        timerStarted = true
                    }
                }) {

                if (path.isEmpty) {
                    val amplitude = 100f
                    val frequency = 5f
                    val phase = 0f
                    val yCenter = size.height / 2f
                    path.moveTo(0f, yCenter)
                    for (x in 0..size.width.toInt() - 100) {
                        val y =
                            amplitude * sin((x * frequency + phase) * PI.toFloat() / 180f) + yCenter
                        path.lineTo(x.toFloat(), y)
                    }
                }
                pathWithProgress.reset()

                pathMeasure.setPath(path, forceClosed = false)
                pathMeasure.getSegment(
                    startDistance = 0f,
                    stopDistance = pathMeasure.length * progress / 100f,
                    pathWithProgress,
                    startWithMoveTo = true
                )

                drawPath(
                    path = path,
                    style = Stroke(
                        2.dp.toPx()
                    ),
                    color = Color.Gray
                )

                drawPath(
                    path = pathWithProgress,
                    style = Stroke(
                        2.dp.toPx()
                    ),
                    color = Color.Blue
                )
            }

        }
    }

}


