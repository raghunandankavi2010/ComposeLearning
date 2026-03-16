package com.example.composelearning.animcompose

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

@Composable
fun TransitionAnimationsScreen() {
    var selectedDemo by remember { mutableIntStateOf(0) }
    val demos = listOf(
        "Basic Transition",
        "Transition Spec",
        "Multi-State",
        "Conditional"
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
                    label = { Text(demos[index]) },
                    selected = selectedDemo == index
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when (selectedDemo) {
                0 -> BasicTransitionDemo()
                1 -> TransitionSpecDemo()
                2 -> MultiStateTransitionDemo()
                3 -> ConditionalTransitionDemo()
            }
        }
    }
}

enum class BoxState { SMALL, MEDIUM, LARGE }

@Composable
fun BasicTransitionDemo() {
    var currentState by remember { mutableStateOf(BoxState.SMALL) }

    val transition = updateTransition(targetState = currentState, label = "box_transition")

    val size by transition.animateDp(
        transitionSpec = { spring(dampingRatio = Spring.DampingRatioMediumBouncy) },
        label = "size"
    ) { state ->
        when (state) {
            BoxState.SMALL -> 60.dp
            BoxState.MEDIUM -> 100.dp
            BoxState.LARGE -> 150.dp
        }
    }

    val color by transition.animateColor(
        transitionSpec = { tween(500) },
        label = "color"
    ) { state ->
        when (state) {
            BoxState.SMALL -> Color.Blue
            BoxState.MEDIUM -> Color.Green
            BoxState.LARGE -> Color.Red
        }
    }

    val cornerRadius by transition.animateDp(
        transitionSpec = { spring(stiffness = Spring.StiffnessMedium) },
        label = "corner_radius"
    ) { state ->
        when (state) {
            BoxState.SMALL -> 8.dp
            BoxState.MEDIUM -> 16.dp
            BoxState.LARGE -> size / 2
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyRow(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val states = BoxState.entries.toTypedArray()
            items(states.size) { index ->
                FilterChip(
                    onClick = { currentState = states[index] },
                    label = { Text(states[index].name) },
                    selected = currentState == states[index]
                )
            }
        }

        Canvas(
            modifier = Modifier
                .size(300.dp)
                .background(Color.LightGray)
        ) {
            drawRoundRect(
                color = color,
                topLeft = Offset(center.x - size.toPx() / 2, center.y - size.toPx() / 2),
                size = Size(size.toPx(), size.toPx()),
                cornerRadius = CornerRadius(cornerRadius.toPx())
            )

            // Info (simplified without nativeCanvas for better stability)
        }
    }
}

@Composable
fun TransitionSpecDemo() {
    var isExpanded by remember { mutableStateOf(false) }

    val transition = updateTransition(targetState = isExpanded, label = "expansion_transition")

    val width by transition.animateDp(
        transitionSpec = {
            if (targetState) {
                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh)
            } else {
                tween(300, easing = FastOutSlowInEasing)
            }
        },
        label = "width"
    ) { expanded ->
        if (expanded) 250.dp else 100.dp
    }

    val height by transition.animateDp(
        transitionSpec = {
            if (targetState) {
                tween(400, delayMillis = 150, easing = FastOutSlowInEasing)
            } else {
                tween(250, easing = FastOutLinearInEasing)
            }
        },
        label = "height"
    ) { expanded ->
        if (expanded) 150.dp else 60.dp
    }

    val backgroundColor by transition.animateColor(
        transitionSpec = { tween(800, easing = LinearEasing) },
        label = "background_color"
    ) { expanded ->
        if (expanded) Color.Green else Color.Blue
    }

    val rotation by transition.animateFloat(
        transitionSpec = { spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessHigh) },
        label = "rotation"
    ) { expanded ->
        if (expanded) 45f else 0f
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = { isExpanded = !isExpanded },
            modifier = Modifier.padding(16.dp)
        ) {
            Text(if (isExpanded) "Collapse" else "Expand")
        }

        Canvas(
            modifier = Modifier
                .size(400.dp, 300.dp)
                .background(Color.White)
        ) {
            rotate(rotation, center) {
                drawRoundRect(
                    color = backgroundColor,
                    topLeft = Offset(center.x - width.toPx() / 2, center.y - height.toPx() / 2),
                    size = Size(width.toPx(), height.toPx()),
                    cornerRadius = CornerRadius(12.dp.toPx())
                )

                // Inner decoration
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.3f),
                    topLeft = Offset(center.x - width.toPx() / 2 + 10.dp.toPx(), center.y - height.toPx() / 2 + 10.dp.toPx()),
                    size = Size(width.toPx() - 20.dp.toPx(), height.toPx() - 20.dp.toPx()),
                    cornerRadius = CornerRadius(8.dp.toPx())
                )
            }

            // Progress bars
            val maxWidth = 250.dp.toPx()
            val maxHeight = 150.dp.toPx()

            drawRect(Color.Gray.copy(alpha = 0.3f), Offset(50.dp.toPx(), size.height - 80.dp.toPx()), Size(maxWidth, 8.dp.toPx()))
            drawRect(Color.Blue, Offset(50.dp.toPx(), size.height - 80.dp.toPx()), Size(width.toPx(), 8.dp.toPx()))

            drawRect(Color.Gray.copy(alpha = 0.3f), Offset(50.dp.toPx(), size.height - 60.dp.toPx()), Size(maxHeight, 8.dp.toPx()))
            drawRect(Color.Green, Offset(50.dp.toPx(), size.height - 60.dp.toPx()), Size(height.toPx(), 8.dp.toPx()))
        }
    }
}

enum class AnimationState { IDLE, LOADING, SUCCESS, ERROR, WARNING }

@Composable
fun MultiStateTransitionDemo() {
    var currentState by remember { mutableStateOf(AnimationState.IDLE) }

    val transition = updateTransition(targetState = currentState, label = "multi_state_transition")

    val iconRotation by transition.animateFloat(
        transitionSpec = {
            when (targetState) {
                AnimationState.LOADING -> tween(1000, easing = LinearEasing)
                else -> spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            }
        },
        label = "icon_rotation"
    ) { state ->
        when (state) {
            AnimationState.IDLE -> 0f
            AnimationState.LOADING -> 360f
            AnimationState.SUCCESS -> 0f
            AnimationState.ERROR -> 180f
            AnimationState.WARNING -> 45f
        }
    }

    val backgroundColor by transition.animateColor(
        transitionSpec = {
            when (targetState) {
                AnimationState.ERROR -> spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessHigh)
                else -> tween(500)
            }
        },
        label = "background_color"
    ) { state ->
        when (state) {
            AnimationState.IDLE -> Color.Gray
            AnimationState.LOADING -> Color.Blue
            AnimationState.SUCCESS -> Color.Green
            AnimationState.ERROR -> Color.Red
            AnimationState.WARNING -> Color(0xFFFFA500)
        }
    }

    val scaleVal by transition.animateFloat(
        transitionSpec = {
            when (targetState) {
                AnimationState.SUCCESS -> spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium)
                AnimationState.ERROR -> keyframes {
                    durationMillis = 600
                    1f at 0
                    1.2f at 100
                    0.9f at 200
                    1.1f at 300
                    1f at 600
                }
                else -> spring()
            }
        },
        label = "scale"
    ) { state ->
        when (state) {
            AnimationState.IDLE -> 1f
            AnimationState.LOADING -> 0.9f
            AnimationState.SUCCESS -> 1.3f
            AnimationState.ERROR -> 1f
            AnimationState.WARNING -> 1.1f
        }
    }

    val borderWidth by transition.animateDp(
        transitionSpec = { spring() },
        label = "border_width"
    ) { state ->
        when (state) {
            AnimationState.IDLE -> 2.dp
            AnimationState.LOADING -> 4.dp
            AnimationState.SUCCESS -> 6.dp
            AnimationState.ERROR -> 8.dp
            AnimationState.WARNING -> 3.dp
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyRow(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val states = AnimationState.entries.toTypedArray()
            items(states.size) { index ->
                FilterChip(
                    onClick = { currentState = states[index] },
                    label = { Text(states[index].name) },
                    selected = currentState == states[index]
                )
            }
        }

        Canvas(
            modifier = Modifier
                .size(300.dp)
                .background(Color.LightGray)
        ) {
            val radius = 60.dp.toPx()

            scale(scaleVal, center) {
                // Main circle
                drawCircle(backgroundColor, radius, center)

                // Border
                drawCircle(Color.White, radius, center, style = Stroke(width = borderWidth.toPx()))

                // Icon
                rotate(iconRotation, center) {
                    when (currentState) {
                        AnimationState.IDLE -> {
                            drawCircle(Color.White, 15.dp.toPx(), center)
                        }
                        AnimationState.LOADING -> {
                            for (i in 0 until 8) {
                                val angle = i * 45f
                                val lineRadius = 30.dp.toPx()
                                val lineLength = 15.dp.toPx()
                                val alpha = (1f - i * 0.1f).coerceAtLeast(0.2f)
                                rotate(angle, center) {
                                    drawLine(
                                        Color.White.copy(alpha = alpha),
                                        Offset(center.x, center.y - lineRadius),
                                        Offset(center.x, center.y - lineRadius + lineLength),
                                        4.dp.toPx(),
                                        StrokeCap.Round
                                    )
                                }
                            }
                        }
                        AnimationState.SUCCESS -> {
                            val checkPath = Path().apply {
                                moveTo(center.x - 15.dp.toPx(), center.y)
                                lineTo(center.x - 5.dp.toPx(), center.y + 10.dp.toPx())
                                lineTo(center.x + 15.dp.toPx(), center.y - 10.dp.toPx())
                            }
                            drawPath(
                                checkPath,
                                Color.White,
                                style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }
                        AnimationState.ERROR -> {
                            drawLine(Color.White, center + Offset(-15.dp.toPx(), -15.dp.toPx()), center + Offset(15.dp.toPx(), 15.dp.toPx()), 6.dp.toPx(), StrokeCap.Round)
                            drawLine(Color.White, center + Offset(15.dp.toPx(), -15.dp.toPx()), center + Offset(-15.dp.toPx(), 15.dp.toPx()), 6.dp.toPx(), StrokeCap.Round)
                        }
                        AnimationState.WARNING -> {
                            drawLine(Color.White, center + Offset(0f, -15.dp.toPx()), center + Offset(0f, 5.dp.toPx()), 4.dp.toPx(), StrokeCap.Round)
                            drawCircle(Color.White, 3.dp.toPx(), center + Offset(0f, 12.dp.toPx()))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConditionalTransitionDemo() {
    var count by remember { mutableIntStateOf(0) }
    var enableAnimation by remember { mutableStateOf(true) }

    val transition = updateTransition(targetState = count > 5, label = "threshold_transition")

    val animatedCount by animateIntAsState(
        targetValue = count,
        animationSpec = if (enableAnimation && abs(count - 5) > 2) {
            spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        } else {
            snap()
        },
        label = "animated_count"
    )

    val backgroundColor by transition.animateColor(
        transitionSpec = {
            if (targetState && enableAnimation) {
                keyframes {
                    durationMillis = 1000
                    Color.Blue at 0
                    Color.Yellow at 300
                    Color(0xFFFFA500) at 600
                    Color.Red at 1000
                }
            } else {
                tween(300)
            }
        },
        label = "conditional_background"
    ) { overThreshold ->
        if (overThreshold) Color.Red else Color.Blue
    }

    val indicatorSize by transition.animateDp(
        transitionSpec = {
            if (targetState && enableAnimation) {
                spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium)
            } else {
                tween(200)
            }
        },
        label = "conditional_size"
    ) { overThreshold ->
        if (overThreshold) 120.dp else 80.dp
    }

    val pulseScale by transition.animateFloat(
        transitionSpec = {
            if (targetState && enableAnimation) {
                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
            } else {
                spring()
            }
        },
        label = "pulse_scale"
    ) { overThreshold ->
        if (overThreshold) 1.1f else 1f
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = { count++ }) { Text("+") }
            Button(onClick = { count = maxOf(0, count - 1) }) { Text("-") }
            Button(onClick = { count = 0 }) { Text("Reset") }
            Spacer(modifier = Modifier.width(16.dp))
            Checkbox(checked = enableAnimation, onCheckedChange = { enableAnimation = it })
            Text("Animate")
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(16.dp)
                .background(Color.LightGray)
        ) {
            // Draw threshold line
            drawLine(
                Color.Gray,
                Offset(0f, center.y - 50.dp.toPx()),
                Offset(size.width, center.y - 50.dp.toPx()),
                2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 5f))
            )

            // Draw bars
            val barWidth = size.width / 12
            val maxBarHeight = 100.dp.toPx()

            for (i in 0..10) {
                val barHeight = if (i < animatedCount) {
                    (i + 1).toFloat() / 10f * maxBarHeight
                } else 0f
                val x = i * barWidth + barWidth / 2
                val barColor = if (i < 5) Color.Blue else Color.Red
                val alpha = if (i < animatedCount) 1f else 0.3f

                drawRect(
                    barColor.copy(alpha = alpha),
                    Offset(x - barWidth / 3, center.y - barHeight),
                    Size(barWidth * 2 / 3, barHeight)
                )
            }

            // Draw indicator
            val indicatorX = (animatedCount.coerceAtMost(10)) * barWidth + barWidth / 2
            scale(pulseScale, Offset(indicatorX, center.y + 50.dp.toPx())) {
                drawCircle(backgroundColor, indicatorSize.toPx() / 2, Offset(indicatorX, center.y + 50.dp.toPx()))
                drawCircle(Color.White, indicatorSize.toPx() / 2, Offset(indicatorX, center.y + 50.dp.toPx()), style = Stroke(width = 3.dp.toPx()))
            }
        }
    }
}
