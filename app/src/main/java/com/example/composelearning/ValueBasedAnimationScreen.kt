package com.example.composelearning


import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*
import kotlin.random.Random
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import android.view.animation.OvershootInterpolator
import android.view.animation.AnticipateInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.Interpolator
import androidx.compose.ui.graphics.Path

@Composable
fun ValueBasedAnimationsScreen() {
    var selectedDemo by remember { mutableStateOf(0) }
    val demos = listOf<Pair<String, @Composable () -> Unit>>(
        "Custom Type" to @Composable { CustomTypeAnimationDemo() },
        "Keyframes" to @Composable { KeyframeAnimationDemo() },
        "Custom Curves" to @Composable { CustomCurveAnimationDemo() },
        "Math Functions" to @Composable { MathematicalAnimationDemo() }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyRow(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(demos.size) { index ->
                FilterChip(
                    onClick = { selectedDemo = index },
                    label = { Text(demos[index].first) },
                    selected = selectedDemo == index
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            demos[selectedDemo].second()
        }
    }
}

@Composable
fun CustomTypeAnimationDemo() {
    data class AnimatedPosition(val x: Float, val y: Float, val rotation: Float)

    var targetPosition by remember {
        mutableStateOf(AnimatedPosition(200f, 200f, 0f))
    }

    val animatedPosition by animateValueAsState(
        targetValue = targetPosition,
        typeConverter = TwoWayConverter(
            convertToVector = { AnimationVector3D(it.x, it.y, it.rotation) },
            convertFromVector = { AnimatedPosition(it.v1, it.v2, it.v3) }
        ),
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "custom_position"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = {
                targetPosition = AnimatedPosition(
                    x = Random.nextFloat() * 300f + 50f,
                    y = Random.nextFloat() * 200f + 50f,
                    rotation = Random.nextFloat() * 360f
                )
            }) {
                Text("Random")
            }
            Button(onClick = {
                targetPosition = AnimatedPosition(200f, 200f, 0f)
            }) {
                Text("Reset")
            }
        }

        Canvas(
            modifier = Modifier
                .size(400.dp, 300.dp)
                .background(Color.LightGray)
        ) {
            rotate(animatedPosition.rotation, Offset(animatedPosition.x, animatedPosition.y)) {
                drawRect(
                    Color.Blue,
                    topLeft = Offset(animatedPosition.x - 25.dp.toPx(), animatedPosition.y - 15.dp.toPx()),
                    size = androidx.compose.ui.geometry.Size(50.dp.toPx(), 30.dp.toPx())
                )
                drawLine(
                    Color.Red,
                    Offset(animatedPosition.x, animatedPosition.y),
                    Offset(animatedPosition.x + 30.dp.toPx(), animatedPosition.y),
                    3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Target indicator
            drawCircle(
                Color.Green.copy(alpha = 0.3f),
                20.dp.toPx(),
                Offset(targetPosition.x, targetPosition.y)
            )

            // Info
            drawContext.canvas.nativeCanvas.drawText(
                "Pos: (${animatedPosition.x.toInt()}, ${animatedPosition.y.toInt()}), Rot: ${animatedPosition.rotation.toInt()}°",
                20.dp.toPx(),
                size.height - 20.dp.toPx(),
                android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 12.sp.toPx()
                }
            )
        }
    }
}

@Composable
fun KeyframeAnimationDemo() {
    var isAnimating by remember { mutableStateOf(false) }

    val animatedValue by animateFloatAsState(
        targetValue = if (isAnimating) 1f else 0f,
        animationSpec = keyframes {
            durationMillis = 3000
            0f at 0 with LinearEasing
            0.3f at 500 with FastOutSlowInEasing
            0.8f at 1000 with LinearOutSlowInEasing
            0.6f at 1500 with FastOutSlowInEasing
            1.2f at 2200 with LinearEasing
            1f at 3000 with FastOutSlowInEasing
        },
        label = "keyframe_animation"
    )

    val animatedColor by animateColorAsState(
        targetValue = if (isAnimating) Color.Red else Color.Blue,
        animationSpec = keyframes {
            durationMillis = 3000
            Color.Blue at 0
            Color.Green at 1000
            Color.Yellow at 2000
            Color.Red at 3000
        },
        label = "keyframe_color"
    )

    val animatedRotation by animateFloatAsState(
        targetValue = if (isAnimating) 360f else 0f,
        animationSpec = keyframes {
            durationMillis = 3000
            0f at 0 with LinearEasing
            90f at 800 with OvershootInterpolator().toEasing()
            180f at 1600 with AnticipateInterpolator().toEasing()
            270f at 2400 with BounceInterpolator().toEasing()
            360f at 3000 with LinearEasing
        },
        label = "keyframe_rotation"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = { isAnimating = !isAnimating },
            modifier = Modifier.padding(16.dp)
        ) {
            Text(if (isAnimating) "Reset" else "Start")
        }

        Canvas(
            modifier = Modifier
                .size(400.dp)
                .background(Color.Black)
        ) {
            val s = 0.5f + animatedValue * 0.7f
            val centerOffset = Offset(
                center.x + sin(animatedValue * Math.PI * 4).toFloat() * 50.dp.toPx(),
                center.y + cos(animatedValue * Math.PI * 2).toFloat() * 30.dp.toPx()
            )

            rotate(animatedRotation, centerOffset) {
                this@Canvas.scale(s, centerOffset) {
                    drawCircle(animatedColor, 40.dp.toPx(), centerOffset)
                    drawCircle(Color.White, 40.dp.toPx(), centerOffset, style = Stroke(width = 3.dp.toPx()))
                }
            }

            // Trail effect
            for (i in 1..10) {
                val trailProgress = (animatedValue - i * 0.05f).coerceAtLeast(0f)
                if (trailProgress > 0f) {
                    val trailOffset = Offset(
                        center.x + sin(trailProgress * Math.PI * 4).toFloat() * 50.dp.toPx(),
                        center.y + cos(trailProgress * Math.PI * 2).toFloat() * 30.dp.toPx()
                    )
                    val alpha = (trailProgress * (1f - i * 0.08f)).coerceIn(0f, 1f)
                    drawCircle(
                        animatedColor.copy(alpha = alpha * 0.4f),
                        40.dp.toPx() * (0.3f + trailProgress * 0.4f),
                        trailOffset
                    )
                }
            }

            // Progress bar
            val progressWidth = size.width * 0.8f
            val progressX = (size.width - progressWidth) / 2
            val progressY = size.height - 40.dp.toPx()

            drawRect(Color.Gray.copy(alpha = 0.3f), Offset(progressX, progressY), androidx.compose.ui.geometry.Size(progressWidth, 8.dp.toPx()))
            drawRect(Color.White, Offset(progressX, progressY), androidx.compose.ui.geometry.Size(progressWidth * animatedValue, 8.dp.toPx()))

            // Info
            drawContext.canvas.nativeCanvas.drawText(
                "Progress: ${(animatedValue * 100).toInt()}% | Rotation: ${animatedRotation.toInt()}°",
                20.dp.toPx(),
                30.dp.toPx(),
                android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 14.sp.toPx()
                }
            )
        }
    }
}

@Composable
fun CustomCurveAnimationDemo() {
    var selectedCurve by remember { mutableStateOf(0) }
    var isAnimating by remember { mutableStateOf(false) }
    val density = LocalDensity.current

    val customCurves = listOf(
        "Elastic" to { t: Float ->
            if (t == 0f || t == 1f) t
            else {
                val period = 0.3f
                val amplitude = 1f
                (-amplitude * 2.0.pow(-10.0 * t).toFloat() *
                        sin(((t - period / 4f) * (2f * Math.PI.toFloat()) / period)).toFloat()).toFloat()
            }
        },
        "Bounce In" to { t: Float ->
            1f - bounceOut(1f - t)
        },
        "Sine Wave" to { t: Float ->
            ((sin(t * Math.PI * 2 - Math.PI / 2).toFloat() + 1f) / 2f)
        },
        "Exponential" to { t: Float ->
            if (t == 0f) 0f else 2.0.pow(10.0 * (t - 1)).toFloat()
        },
        "Circular" to { t: Float ->
            1f - sqrt(1f - t * t)
        }
    )

    val customEasing = Easing { fraction ->
        customCurves[selectedCurve].second.invoke(fraction) as Float
    }

    val animatedPosition by animateFloatAsState(
        targetValue = with(density) { if (isAnimating) 300.dp.toPx() else 50.dp.toPx() },
        animationSpec = tween(2000, easing = customEasing),
        label = "custom_curve"
    )

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyRow(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(customCurves.size) { index ->
                FilterChip(
                    onClick = { selectedCurve = index },
                    label = { Text(customCurves[index].first) },
                    selected = selectedCurve == index
                )
            }
        }

        Button(
            onClick = { isAnimating = !isAnimating },
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(if (isAnimating) "Reset" else "Animate")
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .padding(16.dp)
                .background(Color.White)
        ) {
            // Draw curve visualization
            val curveHeight = 100.dp.toPx()
            val curveWidth = size.width - 100.dp.toPx()
            val curveStartX = 50.dp.toPx()
            val curveStartY = 50.dp.toPx()

            // Background
            drawRect(
                Color.LightGray.copy(alpha = 0.3f),
                Offset(curveStartX, curveStartY),
                androidx.compose.ui.geometry.Size(curveWidth, curveHeight)
            )

            // Draw curve
            val curvePath = Path()
            val steps = 100
            for (i in 0..steps) {
                val t = i.toFloat() / steps
                val curveValue = (customCurves[selectedCurve].second.invoke(t) as Float)
                val x = curveStartX + t * curveWidth
                val y = curveStartY + curveHeight - (curveValue * curveHeight)
                if (i == 0) curvePath.moveTo(x, y)
                else curvePath.lineTo(x, y)
            }
            drawPath(curvePath, Color.Blue, style = Stroke(width = 3.dp.toPx()))

            // Current position indicator
            val currentT = if (isAnimating) {
                (animatedPosition - 50.dp.toPx()) / (300.dp.toPx() - 50.dp.toPx())
            } else 0f

            if (currentT > 0f) {
                val curveValue = (customCurves[selectedCurve].second.invoke(currentT) as Float)
                val indicatorX = curveStartX + currentT * curveWidth
                val indicatorY = curveStartY + curveHeight - (curveValue * curveHeight)
                drawCircle(Color.Red, 6.dp.toPx(), Offset(indicatorX, indicatorY))
            }

            // Ball track
            val ballY = 200.dp.toPx()
            drawLine(Color.Gray, Offset(50.dp.toPx(), ballY), Offset(350.dp.toPx(), ballY), 2.dp.toPx())
            drawCircle(Color.Red, 15.dp.toPx(), Offset(animatedPosition, ballY))

            // Labels
            drawContext.canvas.nativeCanvas.drawText(
                "Curve: ${customCurves[selectedCurve].first}",
                20.dp.toPx(),
                size.height - 60.dp.toPx(),
                android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 14.sp.toPx()
                }
            )
            drawContext.canvas.nativeCanvas.drawText(
                "Progress: ${(currentT * 100).toInt()}%",
                20.dp.toPx(),
                size.height - 40.dp.toPx(),
                android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 14.sp.toPx()
                }
            )
        }
    }
}

private fun bounceOut(t: Float): Float {
    return when {
        t < 1f / 2.75f -> 7.5625f * t * t
        t < 2f / 2.75f -> {
            val t2 = t - 1.5f / 2.75f
            7.5625f * t2 * t2 + 0.75f
        }
        t < 2.5f / 2.75f -> {
            val t2 = t - 2.25f / 2.75f
            7.5625f * t2 * t2 + 0.9375f
        }
        else -> {
            val t2 = t - 2.625f / 2.75f
            7.5625f * t2 * t2 + 0.984375f
        }
    }
}

@Composable
fun MathematicalAnimationDemo() {
    var selectedFunction by remember { mutableStateOf(0) }
    var isAnimating by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "math_functions")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    val animationFunctions = listOf(
        "Sine Wave" to { t: Float, scale: Float ->
            Offset(
                sin(t * Math.PI * 4).toFloat() * 80f * scale,
                sin(t * Math.PI * 2).toFloat() * 40f * scale
            )
        },
        "Lissajous" to { t: Float, scale: Float ->
            Offset(
                sin(t * Math.PI * 3).toFloat() * 100f * scale,
                sin(t * Math.PI * 4).toFloat() * 80f * scale
            )
        },
        "Spiral" to { t: Float, scale: Float ->
            val angle = t * Math.PI * 8
            val radius = t * 100f * scale
            Offset(
                cos(angle).toFloat() * radius,
                sin(angle).toFloat() * radius
            )
        },
        "Heart" to { t: Float, scale: Float ->
            val angle = t * Math.PI * 2
            val heartScale = 60f * scale
            val x = 16.0 * sin(angle).pow(3.0)
            val y = -(13.0 * cos(angle) - 5.0 * cos(2.0 * angle) - 2.0 * cos(3.0 * angle) - cos(4.0 * angle))
            Offset((x * heartScale / 16.0).toFloat(), (y * heartScale / 16.0).toFloat())
        },
        "Figure 8" to { t: Float, scale: Float ->
            val angle = t * Math.PI * 2
            val figureScale = 80f * scale
            Offset(
                (figureScale * sin(angle)).toFloat(),
                (figureScale * sin(angle) * cos(angle)).toFloat()
            )
        },
        "Rose Curve" to { t: Float, scale: Float ->
            val angle = t * Math.PI * 8
            val k = 5.0
            val r = cos(k * angle).toFloat() * 80f * scale
            Offset(
                (r * cos(angle)).toFloat(),
                (r * sin(angle)).toFloat()
            )
        }
    )

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyRow(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(animationFunctions.size) { index ->
                FilterChip(
                    onClick = { selectedFunction = index },
                    label = { Text(animationFunctions[index].first) },
                    selected = selectedFunction == index
                )
            }
        }

        Row(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Button(onClick = { isAnimating = !isAnimating }) {
                Text(if (isAnimating) "Pause" else "Play")
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(16.dp)
                .background(Color.Black)
        ) {
            val currentFunction = animationFunctions[selectedFunction].second

            if (isAnimating) {
                // Draw trail
                val trailLength = 50
                for (i in 0 until trailLength) {
                    val trailTime = (time - i * 0.02f + 1f) % 1f
                    val position = currentFunction(trailTime, 1.dp.toPx())
                    val centerPos = center + position
                    val alpha = (1f - i.toFloat() / trailLength) * 0.8f
                    val size = (1f - i.toFloat() / trailLength) * 8.dp.toPx() + 2.dp.toPx()
                    val hue = (trailTime * 360f + i * 5f) % 360f
                    drawCircle(Color.hsv(hue, 0.8f, 0.9f).copy(alpha = alpha), size, centerPos)
                }

                // Current position
                val currentPos = center + currentFunction(time, 1.dp.toPx())
                drawCircle(Color.White, 12.dp.toPx(), currentPos)
                drawCircle(Color.Yellow, 8.dp.toPx(), currentPos)
            } else {
                // Draw static path
                val path = Path()
                val steps = 200
                for (i in 0..steps) {
                    val t = i.toFloat() / steps
                    val position = currentFunction(t, 1.dp.toPx())
                    val pathPos = center + position
                    if (i == 0) path.moveTo(pathPos.x, pathPos.y)
                    else path.lineTo(pathPos.x, pathPos.y)
                }
                drawPath(path, Color.Cyan, style = Stroke(width = 2.dp.toPx()))
            }

            // Center reference
            drawCircle(Color.Gray.copy(alpha = 0.5f), 4.dp.toPx(), center)

            // Info
            drawContext.canvas.nativeCanvas.drawText(
                "Function: ${animationFunctions[selectedFunction].first}",
                20.dp.toPx(),
                size.height - 40.dp.toPx(),
                android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 14.sp.toPx()
                }
            )
            drawContext.canvas.nativeCanvas.drawText(
                "Time: ${String.format("%.2f", time)}",
                20.dp.toPx(),
                size.height - 20.dp.toPx(),
                android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 12.sp.toPx()
                }
            )
        }
    }
}

fun Interpolator.toEasing(): Easing = Easing { fraction -> this.getInterpolation(fraction) }