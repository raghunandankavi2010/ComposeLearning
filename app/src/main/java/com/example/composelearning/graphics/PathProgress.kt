package com.example.composelearning.graphics

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay


@Preview
@Composable
fun AnimatedBorderButton() {
    val startDurationInSeconds = 2 // Reduced duration for testing
    var targetValue by remember { mutableStateOf(0f) }
    var isGrayAnimating by remember { mutableStateOf(true) }
    var isAnimationRunning by remember { mutableStateOf(false) } // New state to track if any animation is running

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val pathWithProgress = remember { Path() }
        val pathMeasure = remember { PathMeasure() }
        val path = remember { Path() }

        val progress by animateFloatAsState(
            targetValue = targetValue,
            animationSpec = tween((startDurationInSeconds * 5000), easing = LinearEasing),
            label = "pathProgress",
            finishedListener = {
                isAnimationRunning = false // Animation has completed
            }
        )

        Box(contentAlignment = Alignment.Center) {
            Canvas(
                modifier = Modifier
                    .height(48.dp)
                    .fillMaxWidth()
            ) {
                if (path.isEmpty) {
                    path.addRoundRect(
                        RoundRect(
                            Rect(offset = Offset.Zero, size),
                            cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                        )
                    )
                }
                pathWithProgress.reset()

                pathMeasure.setPath(path, forceClosed = false)
                if (isGrayAnimating) {
                    // Gray animation: full to empty (right to left)
                    pathMeasure.getSegment(
                        startDistance = 0f,
                        stopDistance = pathMeasure.length * (progress / 100f),
                        pathWithProgress,
                        startWithMoveTo = true
                    )
                } else {
                    // Orange animation: empty to full (left to right)
                    pathMeasure.getSegment(
                        startDistance = pathMeasure.length * (1 - progress / 100f),
                        stopDistance = pathMeasure.length,
                        pathWithProgress,
                        startWithMoveTo = true
                    )
                }

                clipPath(path) {
                    drawRect(Color.White)
                }
                drawPath(
                    path = path,
                    style = Stroke(1.dp.toPx()),
                    color = if (isGrayAnimating) Color(0xFFFA7D19) else Color.Gray
                )

                // Animated path
                drawPath(
                    path = pathWithProgress,
                    style = Stroke(1.dp.toPx()),
                    color = if (isGrayAnimating) Color.Gray else Color(0xFFFA7D19)
                )
            }
        }

        // Manage animation steps
        LaunchedEffect(Unit) {
            // Start gray animation on initial composition
            targetValue = 100f
            isAnimationRunning = true
        }
        // Switch animation
        LaunchedEffect(isAnimationRunning) {
            if (!isAnimationRunning) {
                if (isGrayAnimating) {
                    isGrayAnimating = false
                    targetValue = 100f // Start orange animation after gray finishes
                } else {
                    targetValue = 0f // Reset target value
                }
                isAnimationRunning = true
            }
        }

        Button(onClick = {
            // Reset state to trigger the gray animation
            isGrayAnimating = true
            targetValue = 100f
            isAnimationRunning = true
        }) {
            Text("Restart")
        }
    }
}

@Composable
fun BorderProgressBar() {

    val startDurationInSeconds = 10

    var targetValue by remember {
        mutableFloatStateOf(100f)
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

        var isAnimationRunning by remember { mutableStateOf(false) }

        // using path
        val pathMeasure by remember { mutableStateOf(PathMeasure()) }

        val path = remember {
            Path()
        }

        val progress by animateFloatAsState(
            targetValue = targetValue,
            animationSpec = tween(startDurationInSeconds * 1000, easing = LinearEasing),
            finishedListener = {
                isAnimationRunning = false // Animation has completed
            }
        )

        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(250.dp, 140.dp)) {

                if (path.isEmpty) {
                    path.addRoundRect(
                        RoundRect(
                            Rect(offset = Offset.Zero, size),
                            cornerRadius = CornerRadius(100.dp.toPx(), 100.dp.toPx())
                        )
                    )
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
                        1.dp.toPx()
                    ),
                    color = Color.Gray
                )

                drawPath(
                    path = pathWithProgress,
                    style = Stroke(
                        1.dp.toPx()
                    ),
                    color = Color.Blue
                )
            }


        }

        Spacer(modifier = Modifier.height(20.dp))

        LaunchedEffect(isAnimationRunning) {
            if(!isAnimationRunning){
                targetValue = 0f
            }
        }

    }
}