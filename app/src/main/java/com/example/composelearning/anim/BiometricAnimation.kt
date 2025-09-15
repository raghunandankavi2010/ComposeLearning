package com.example.composelearning.anim


import androidx.compose.animation.animateColor
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Stable
enum class BiometricState { Idle, Scanning, Success, Error }

@Composable
fun BiometricRecognitionAnimation(
    state: BiometricState,
    modifier: Modifier = Modifier,
    size: Dp = 180.dp,
    ringThickness: Dp = 5.dp,
    baseColor: Color = MaterialTheme.colorScheme.primary,
    successColor: Color = Color(0xFF2ecc71),
    errorColor: Color = Color(0xFFe74c3c),
) {
    val stroke = with(LocalDensity.current) { ringThickness.toPx() }

    var angleDeg by remember { mutableFloatStateOf(0f) }
    var pulseScale by remember { mutableFloatStateOf(1f) }
    var scanPhase by remember { mutableFloatStateOf(0f) }  // radians

    val orbitDegPerSec = 180f       // dot orbit speed
    val pulseFreqHz = 0.8f          // pulse cycles per second
    val scanFreqHz = 0.75f          // vertical scan cycles per second
    val pulseAmp = 0.04f            // +/- 4%

    LaunchedEffect(state) {
        if (state != BiometricState.Scanning) return@LaunchedEffect
        var lastNanos = 0L
        withFrameNanos { lastNanos = it }
        var pulsePhase = 0f
        while (true) {
            withFrameNanos { now ->
                val dt = (now - lastNanos) / 1_000_000_000f
                lastNanos = now

                angleDeg = (angleDeg + orbitDegPerSec * dt) % 360f

                pulsePhase += (2f * PI.toFloat() * pulseFreqHz * dt)
                pulseScale = 1f + pulseAmp * sin(pulsePhase)

                scanPhase += (2f * PI.toFloat() * scanFreqHz * dt)
            }
            if (state != BiometricState.Scanning) break
        }
    }

    val transition = updateTransition(targetState = state, label = "state")

    val ringColor by transition.animateColor(
        transitionSpec = { spring(stiffness = Spring.StiffnessLow, dampingRatio = 0.9f) },
        label = "ringColor"
    ) { s ->
        when (s) {
            BiometricState.Success -> successColor
            BiometricState.Error -> errorColor
            else -> baseColor.copy(alpha = 0.9f)
        }
    }

    val overlayAlpha by transition.animateFloat(
        transitionSpec = { spring(stiffness = Spring.StiffnessLow, dampingRatio = 0.9f) },
        label = "overlayAlpha"
    ) { s ->
        when (s) {
            BiometricState.Idle -> 0.08f
            BiometricState.Scanning -> 0.12f
            BiometricState.Success, BiometricState.Error -> 0.16f
        }
    }

    val indicatorProgress by transition.animateFloat(
        transitionSpec = {
            when (targetState) {
                BiometricState.Success -> androidx.compose.animation.core.tween(
                    durationMillis = 630,
                    easing = androidx.compose.animation.core.FastOutSlowInEasing
                )

                BiometricState.Error -> androidx.compose.animation.core.tween(
                    durationMillis = 1030,
                    easing = androidx.compose.animation.core.FastOutSlowInEasing
                )

                else -> androidx.compose.animation.core.tween(durationMillis = 200)
            }
        },
        label = "indicatorProgress"
    ) { s ->
        if (s == BiometricState.Success || s == BiometricState.Error) 1f else 0f
    }

    val tRaw = indicatorProgress.coerceIn(0f, 1f)
    val t = tRaw * tRaw * (3f - 2f * tRaw)

    val scale = if (state == BiometricState.Scanning) pulseScale else 1f

    val scanLineAlpha by transition.animateFloat(
        transitionSpec = { spring(stiffness = Spring.StiffnessLow, dampingRatio = 0.85f) },
        label = "scanLineAlpha"
    ) { s -> if (s == BiometricState.Scanning) 1f else 0f }

    Box(
        modifier = modifier
            .size(size)
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .matchParentSize()
                .drawBehind {
                    val r = this.size.minDimension / 2
                    drawCircle(
                        color = ringColor.copy(alpha = overlayAlpha),
                        radius = r,
                        center = center,
                    )
                }
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val minDim = this.size.minDimension
            val radius = minDim / 2f
            val arcRadius = radius - stroke / 2f
            val arcRect = Rect(
                left = center.x - arcRadius,
                top = center.y - arcRadius,
                right = center.x + arcRadius,
                bottom = center.y + arcRadius
            )

            // Base ring
            drawArc(
                color = ringColor.copy(alpha = 0.25f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(arcRect.left, arcRect.top),
                size = Size(arcRect.width, arcRect.height),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            // Orbiting dots (Scanning only)
            if (state == BiometricState.Scanning) {
                val angleRad = angleDeg * (PI.toFloat() / 180f)
                val ringR = arcRect.width / 2f

                // Main (bigger) dot
                val mainCenter = Offset(
                    x = center.x + ringR * cos(angleRad),
                    y = center.y + ringR * sin(angleRad)
                )
                val mainGlow = stroke * 3.0f
                val mainDot = stroke * 1.6f
                drawCircle(ringColor.copy(alpha = 0.20f), mainGlow, mainCenter)
                drawCircle(ringColor, mainDot, mainCenter)

                // Opposite (also big but slightly smaller) dot
                val oppAngle = angleRad + PI.toFloat()
                val oppCenter = Offset(
                    x = center.x + ringR * cos(oppAngle),
                    y = center.y + ringR * sin(oppAngle)
                )
                val oppGlow = stroke * 2.2f
                val oppDot = stroke * 1.1f
                drawCircle(ringColor.copy(alpha = 0.18f), oppGlow, oppCenter)
                drawCircle(ringColor, oppDot, oppCenter)
            }

            if (scanLineAlpha > 0f) {
                val clipPath = Path().apply { addOval(arcRect) }

                val iconSizePx = (size * 0.50f).toPx()
                val iconHalf = iconSizePx / 2f
                val iconPadding = stroke * 0.23f

                val top = center.y - iconHalf + iconPadding
                val bottom = center.y + iconHalf - iconPadding

                val smoothP = (1f - cos(scanPhase)) * 0.5f
                val y = top + (bottom - top) * smoothP

                val lineLength = iconSizePx * 0.75f
                val startX = center.x - lineLength / 2f
                val endX = center.x + lineLength / 2f

                withTransform({ clipPath(clipPath) }) {
                    // soft glow
                    drawLine(
                        color = ringColor.copy(alpha = 0.18f * scanLineAlpha),
                        start = Offset(startX, y),
                        end = Offset(endX, y),
                        strokeWidth = stroke * 2.0f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = ringColor.copy(alpha = 0.92f * scanLineAlpha),
                        start = Offset(startX, y),
                        end = Offset(endX, y),
                        strokeWidth = stroke * 0.9f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        val iconAlpha by transition.animateFloat(
            transitionSpec = { spring(stiffness = Spring.StiffnessLow, dampingRatio = 0.85f) },
            label = "iconAlpha"
        ) { s -> if (s == BiometricState.Success || s == BiometricState.Error) 0.2f else 1f }

        Icon(
            imageVector = Icons.Outlined.Fingerprint,
            contentDescription = null,
            tint = ringColor.copy(alpha = iconAlpha),
            modifier = Modifier.size(size * 0.40f)
        )

        if (state == BiometricState.Success || state == BiometricState.Error) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1f)
            ) {
                val minDim = this.size.minDimension
                val radius = minDim / 2f
                val arcRadius = radius - stroke / 2f
                val arcRect = Rect(
                    left = center.x - arcRadius,
                    top = center.y - arcRadius,
                    right = center.x + arcRadius,
                    bottom = center.y + arcRadius
                )

                if (state == BiometricState.Success) {
                    val path = Path().apply {
                        val w = arcRect.width
                        val h = arcRect.height
                        val left = arcRect.left
                        val top = arcRect.top
                        moveTo(left + 0.28f * w, top + 0.55f * h)
                        lineTo(left + 0.45f * w, top + 0.72f * h)
                        lineTo(left + 0.74f * w, top + 0.36f * h)
                    }
                    val measure = PathMeasure().apply { setPath(path, false) }
                    val segment = Path()
                    val ok = measure.getSegment(
                        0f,
                        measure.length * t, // <- use eased t
                        segment,
                        true
                    )
                    if (ok) {
                        drawPath(
                            path = segment,
                            color = successColor,
                            style = Stroke(
                                width = stroke,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                } else {
                    val p1 = Path().apply {
                        val w = arcRect.width
                        val h = arcRect.height
                        val left = arcRect.left
                        val top = arcRect.top
                        moveTo(left + 0.32f * w, top + 0.32f * h)
                        lineTo(left + 0.68f * w, top + 0.68f * h)
                    }
                    val p2 = Path().apply {
                        val w = arcRect.width
                        val h = arcRect.height
                        val left = arcRect.left
                        val top = arcRect.top
                        moveTo(left + 0.68f * w, top + 0.32f * h)
                        lineTo(left + 0.32f * w, top + 0.68f * h)
                    }
                    val m1 = PathMeasure().apply { setPath(p1, false) }
                    val m2 = PathMeasure().apply { setPath(p2, false) }

                    val tRaw = indicatorProgress.coerceIn(0f, 1f)
                    val t = tRaw * tRaw * (3f - 2f * tRaw) // smoothstep

                    if (t > 0f) {
                        val seg1 = Path()
                        val part1 = (t.coerceAtMost(0.5f) * 2f) // 0..1 over first half of t
                        val ok1 = m1.getSegment(0f, m1.length * part1, seg1, true)
                        if (ok1 && part1 > 0f) {
                            drawPath(
                                seg1,
                                errorColor,
                                style = Stroke(width = stroke, cap = StrokeCap.Round)
                            )
                        }
                    }
                    if (t > 0.5f) {
                        val seg2 = Path()
                        val part2 =
                            ((t - 0.5f).coerceIn(0f, 0.5f) * 2f) // 0..1 over second half of t
                        val ok2 = m2.getSegment(0f, m2.length * part2, seg2, true)
                        if (ok2 && part2 > 0f) {
                            drawPath(
                                seg2,
                                errorColor,
                                style = Stroke(width = stroke, cap = StrokeCap.Round)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BiometricDemoPanel() {
    var state by remember { mutableStateOf(BiometricState.Idle) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        BiometricRecognitionAnimation(state = state)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DemoChip("Idle") { state = BiometricState.Idle }
            DemoChip("Scan") { state = BiometricState.Scanning }
            DemoChip("Success") { state = BiometricState.Success }
            DemoChip("Error") { state = BiometricState.Error }
        }
    }
}

@Composable
private fun DemoChip(text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        tonalElevation = 1.dp,
        modifier = Modifier
            .height(40.dp)
            .padding(horizontal = 2.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Box(Modifier.padding(horizontal = 14.dp), contentAlignment = Alignment.Center) {
            androidx.compose.material3.Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}