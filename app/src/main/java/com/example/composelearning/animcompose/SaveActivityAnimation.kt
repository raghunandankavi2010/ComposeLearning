package com.example.composelearning.animcompose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.ThumbUpOffAlt
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import androidx.compose.ui.graphics.lerp as lerpColor

private val StravaOrange = Color(0xFFFC4C02)
private val StravaOrangeMid = Color(0xFFFF6B35)
private val StravaDark = Color(0xFF1A1A2E)

private data class BurstIconData(
    val type: Int,
    val cos: Float,
    val sin: Float,
    val speed: Float,
    val rotation: Float,
    val delayFactor: Float
)

@Composable
fun SaveActivityScreen() {
    var isPlaying by remember { mutableStateOf(false) }
    var playCount by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(StravaDark)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (!isPlaying) {
                    isPlaying = true
                    playCount++
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (!isPlaying) {
            Text(
                text = "Tap to save activity",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        if (isPlaying) {
            SaveActivityTransition(
                key = playCount,
                onComplete = { isPlaying = false }
            )
        }
    }
}

@Composable
private fun SaveActivityTransition(
    key: Int,
    modifier: Modifier = Modifier,
    onComplete: () -> Unit = {}
) {
    val liquidRiseAnim = remember(key) { Animatable(0f) }
    val blobStrokeAnim = remember(key) { Animatable(0f) }
    val fillExpandAnim = remember(key) { Animatable(0f) }
    val strokeColorAnim = remember(key) { Animatable(0f) }
    val iconBurstAnim = remember(key) { Animatable(0f) }
    val leadPathAnim = remember(key) { Animatable(0f) }
    val clearAnim = remember(key) { Animatable(0f) }

    val starPainter = rememberVectorPainter(image = Icons.Default.StarBorder)
    val thumbPainter = rememberVectorPainter(image = Icons.Default.ThumbUpOffAlt)
    val flashPainter = rememberVectorPainter(image = Icons.Default.FlashOn)
    val heartPainter = rememberVectorPainter(image = Icons.Filled.Favorite)
    val bikePainter = rememberVectorPainter(image = Icons.AutoMirrored.Filled.DirectionsBike)

    val burstPainters = remember(starPainter, thumbPainter, flashPainter) {
        listOf(starPainter, thumbPainter, flashPainter)
    }

    val density = LocalDensity.current

    val burstIcons = remember(key, density) {
        buildList {
            var currentAngle = 35f
            var painterIndex = 0

            while (currentAngle < 315f) {
                val rad = Math.toRadians(currentAngle.toDouble()).toFloat()
                add(
                    BurstIconData(
                        type = painterIndex % 3,
                        cos = cos(rad),
                        sin = sin(rad),
                        speed = with(density) {
                            (Random.nextFloat() * 600.dp.toPx() + 20.dp.toPx())
                                .coerceAtLeast(60.dp.toPx())
                        },
                        rotation = Random.nextFloat() * 40f + 20f,
                        delayFactor = Random.nextFloat() * 0.2f
                    )
                )
                currentAngle += Random.nextFloat() * 15f + 10f
                painterIndex++
            }

            val guaranteedAngle = Random.nextFloat() * 30f + 325f
            val guaranteedRad = Math.toRadians(guaranteedAngle.toDouble()).toFloat()
            add(
                BurstIconData(
                    type = 2,
                    cos = cos(guaranteedRad),
                    sin = sin(guaranteedRad),
                    speed = with(density) { 100.dp.toPx() },
                    rotation = 40f,
                    delayFactor = 0.1f
                )
            )
        }
    }

    LaunchedEffect(key) {
        delay(200)

        launch {
            liquidRiseAnim.animateTo(1f, tween(200, easing = LinearOutSlowInEasing))
        }

        delay(100)

        launch {
            blobStrokeAnim.animateTo(1f, tween(650, easing = EaseInOutCubic))
        }

        launch {
            delay(450)
            iconBurstAnim.animateTo(1f, tween(3000, easing = LinearOutSlowInEasing))
        }

        launch {
            delay(500)
            launch {
                strokeColorAnim.animateTo(1f, tween(500, easing = EaseInOutCubic))
            }
            fillExpandAnim.animateTo(1f, tween(350, easing = LinearEasing))
        }

        launch {
            delay(900)
            leadPathAnim.animateTo(1f, tween(1200, easing = EaseInOutCubic))
        }

        delay(3000)

        clearAnim.animateTo(1f, tween(1000, easing = EaseInOutCubic))
        onComplete()
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .drawWithCache {
                val blobPath = Path().apply {
                    moveTo(size.width * 0.2f, size.height)
                    cubicTo(
                        size.width * 2.1f, size.height * 0.75f,
                        size.width * 0.0f, size.height * 0.35f,
                        size.width * 0.20f, 0.0f
                    )
                }

                val blobMeasure = PathMeasure().apply { setPath(blobPath, false) }
                val blobPathLength = blobMeasure.length

                val leadPath = Path().apply {
                    moveTo(size.width * 0.9f, -size.height * 0.05f)
                    cubicTo(
                        size.width * 1.1f, size.height * 0.25f,
                        -size.width * 1f, size.height * 0.4f,
                        -size.width * 0.25f, size.height * 0.55f
                    )
                    lineTo(-size.width, 0f)
                    cubicTo(
                        -size.width * 1.1f, size.height * 0.75f,
                        size.width * 0.5f, size.height,
                        size.width * 1.1f, size.height * 0.75f
                    )
                }

                val leadMeasure = PathMeasure().apply { setPath(leadPath, false) }
                val leadPathLength = leadMeasure.length

                val maxDimension = maxOf(size.width, size.height)
                val burstOriginRadius = size.width * 0.20f
                val burstIconSizePx = 42.dp.toPx()
                val burstIconSize = Size(burstIconSizePx, burstIconSizePx)
                val burstIconHalf = burstIconSizePx / 2f

                val liquidSurfacePath = Path()
                val blobSegmentPath = Path()
                val leadSegmentPath = Path()

                val startStrokeWidth = 80.dp.toPx()
                val midStrokeWidth = 120.dp.toPx()
                val leadStrokeWidth = 80.dp.toPx()

                onDrawWithContent {
                    val liquidRise = liquidRiseAnim.value
                    val blobStroke = blobStrokeAnim.value
                    val fillExpand = fillExpandAnim.value
                    val iconBurst = iconBurstAnim.value
                    val strokeColor = strokeColorAnim.value
                    val leadPath2 = leadPathAnim.value
                    val clearing = clearAnim.value

                    val contentAlpha = (1f - clearing * 5f).coerceIn(0f, 1f)

                    if (contentAlpha > 0f) {
                        val strokeWidth = lerp(
                            lerp(startStrokeWidth, midStrokeWidth, blobStroke),
                            maxDimension * 3f,
                            fillExpand
                        )

                        val surfaceY = size.height * (1f - 0.085f * liquidRise)
                        val blobHeadPos = blobMeasure.getPosition(blobStroke * blobPathLength)
                        val upwardPull = if (liquidRise == 1f) {
                            (size.height - blobHeadPos.y).coerceAtLeast(0f) * 0.2f
                        } else 0f
                        val liquidPeakY = surfaceY - upwardPull

                        liquidSurfacePath.apply {
                            reset()
                            moveTo(0f, size.height)
                            lineTo(0f, liquidPeakY + (upwardPull * 0.25f))
                            cubicTo(
                                size.width * 0.25f, liquidPeakY + upwardPull,
                                size.width * 0.75f, liquidPeakY + upwardPull,
                                size.width, liquidPeakY - upwardPull
                            )
                            lineTo(size.width, size.height)
                            close()
                        }

                        drawPath(
                            path = liquidSurfacePath,
                            color = StravaOrange,
                            alpha = contentAlpha
                        )

                        if (blobStroke > 0f) {
                            blobSegmentPath.reset()
                            blobMeasure.getSegment(
                                0f,
                                blobStroke * blobPathLength,
                                blobSegmentPath,
                                true
                            )

                            drawPath(
                                path = blobSegmentPath,
                                color = lerpColor(
                                    StravaOrange,
                                    StravaDark,
                                    strokeColor
                                ),
                                alpha = contentAlpha,
                                style = Stroke(
                                    width = strokeWidth,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }

                        if (leadPath2 > 0f) {
                            leadSegmentPath.reset()
                            leadMeasure.getSegment(
                                0f,
                                leadPath2 * leadPathLength,
                                leadSegmentPath,
                                true
                            )

                            drawPath(
                                path = leadSegmentPath,
                                brush = Brush.linearGradient(
                                    listOf(StravaOrangeMid, StravaOrange)
                                ),
                                alpha = contentAlpha,
                                style = Stroke(
                                    width = leadStrokeWidth,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }

                        if (iconBurst > 0f) {
                            burstIcons.forEach { icon ->
                                val iconProgress =
                                    ((iconBurst - icon.delayFactor) / (1f - icon.delayFactor))
                                        .coerceIn(0f, 1f)

                                if (iconProgress > 0f) {
                                    val scale = (iconProgress * 2f) *
                                            if (iconProgress > 0.95f) {
                                                (1f - iconProgress) * 20f
                                            } else 1f

                                    val travelDist =
                                        burstOriginRadius + icon.speed * iconProgress
                                    val iconCenterX =
                                        (size.width / 2f) + icon.cos * travelDist
                                    val iconCenterY =
                                        (size.height / 2f) + icon.sin * travelDist

                                    withTransform({
                                        translate(iconCenterX, iconCenterY)
                                        rotate(
                                            icon.rotation * iconProgress,
                                            pivot = Offset.Zero
                                        )
                                        scale(scale, scale, pivot = Offset.Zero)
                                        translate(-burstIconHalf, -burstIconHalf)
                                    }) {
                                        with(burstPainters[icon.type]) {
                                            draw(
                                                size = burstIconSize,
                                                colorFilter = ColorFilter.tint(Color.White),
                                                alpha = contentAlpha
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    drawContent()
                }
            }
    ) {
        AnimatedVisibility(
            visible = fillExpandAnim.value > 0f,
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(visible = leadPathAnim.value == 1.0f) {
                    Box(
                        modifier = Modifier
                            .size(164.dp)
                            .padding(bottom = 32.dp)
                    ) {
                        Image(
                            modifier = Modifier.fillMaxSize(),
                            painter = bikePainter,
                            colorFilter = ColorFilter.tint(StravaOrangeMid),
                            contentDescription = null
                        )
                    }
                }

                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            alpha = (1f - clearAnim.value * 100f).coerceIn(0f, 1f)
                        },
                    textAlign = TextAlign.Center,
                    text = "Activity Saved!",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
