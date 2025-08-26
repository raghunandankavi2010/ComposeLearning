package com.example.composelearning.animcompose

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


/**
 * A composable that displays an indeterminate circular progress arc with a gradient.
 * This has the appearance of a standard CircularProgressIndicator but with a gradient color.
 *
 * @param modifier The modifier to be applied to the progress bar.
 * @param strokeWidth The width of the progress bar's stroke.
 * @param colors The list of colors to be used in the gradient.
 * @param durationMillis The duration of one full rotation animation in milliseconds.
 * @param arcAngle The sweep angle of the arc, controlling the size of the gap.
 * A value of 360f would be a full circle. A good default is 270f.
 */
@Composable
fun GradientArcCircularProgressBar(
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 4.dp,
    colors: List<Color> = listOf(Color(0xFF4CAF50),Color(0xFFCDDC39), Color(0xFFFD1D1D)),
    durationMillis: Int = 2000,
    arcAngle: Float = 270f // The angle of the arc
) {
    // Create an infinite transition to drive the animation
    val infiniteTransition = rememberInfiniteTransition(label = "infinite_arc_transition")

    // Animate a rotation angle from 0f to 360f and repeat indefinitely
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation_angle"
    )

    // Create a sweep gradient brush. The gradient will be applied along the arc.
    val brush = remember {
        Brush.linearGradient(colors)
    }

    Canvas(
        modifier = modifier
    ) {
        // The `rotate` block is what makes the progress bar spin
        rotate(degrees = rotationAngle) {
            drawArc(
                brush = brush,
                startAngle = 0f,
                // This is the key change: we draw a partial arc instead of a full circle
                sweepAngle = arcAngle,
                useCenter = false,
                style = Stroke(
                    width = strokeWidth.toPx(),
                    cap = StrokeCap.Round // Rounded edges for a softer look
                )
            )
        }
    }
}


@Composable
fun MyAwesomeLoadingScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Loading...", fontSize = 24.sp, modifier = Modifier.padding(bottom = 32.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Default arc (270 degrees)
                GradientArcCircularProgressBar(
                    modifier = Modifier.size(80.dp),
                    strokeWidth = 6.dp
                )
                Text(
                    text = "Default\n(270° arc)",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.width(40.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Custom arc with a larger gap
                GradientArcCircularProgressBar(
                    modifier = Modifier.size(80.dp),
                    strokeWidth = 10.dp,
                    colors = listOf(Color.Cyan, Color.Green),
                    arcAngle = 180f // A half-circle arc
                )
                Text(
                    text = "Custom\n(180° arc)",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMyAwesomeLoadingScreen() {
    MyAwesomeLoadingScreen()
}