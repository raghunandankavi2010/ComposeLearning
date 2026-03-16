package com.example.composelearning.animcompose

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.*

@Composable
fun BottleWaveAnimation() {
    var fillLevel by remember { mutableFloatStateOf(0.5f) }
    var waveAmplitude by remember { mutableFloatStateOf(10f) }
    var waveFrequency by remember { mutableFloatStateOf(3f) }
    var isAnimating by remember { mutableStateOf(false) }

    // Wave movement animation
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing)
        ),
        label = "wave_offset"
    )

    // Auto fill animation
    LaunchedEffect(isAnimating) {
        while (isAnimating) {
            fillLevel = (fillLevel + 0.01f).coerceIn(0f, 1f)
            if (fillLevel >= 1f || fillLevel <= 0f) {
                isAnimating = false
            }
            delay(50)
        }
    }

    val density = LocalDensity.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Canvas for bottle and water
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            BottleWithWater(
                fillLevel = fillLevel,
                waveOffset = waveOffset,
                waveAmplitude = with(density) { waveAmplitude.dp.toPx() },
                waveFrequency = waveFrequency
            )
        }

        // Controls
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Bottle Wave Fill Animation",
                    style = MaterialTheme.typography.titleLarge
                )

                // Fill level slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Fill Level: ${(fillLevel * 100).toInt()}%", modifier = Modifier.width(100.dp))
                    Slider(
                        value = fillLevel,
                        onValueChange = { fillLevel = it },
                        valueRange = 0f..1f,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Wave amplitude slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Wave Height: ${waveAmplitude.toInt()}", modifier = Modifier.width(100.dp))
                    Slider(
                        value = waveAmplitude,
                        onValueChange = { waveAmplitude = it },
                        valueRange = 0f..30f,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Wave frequency slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Wave Count: ${waveFrequency.toInt()}", modifier = Modifier.width(100.dp))
                    Slider(
                        value = waveFrequency,
                        onValueChange = { waveFrequency = it },
                        valueRange = 1f..8f,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Control buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { isAnimating = !isAnimating },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isAnimating) "Stop" else "Auto Fill")
                    }

                    Button(
                        onClick = { fillLevel = 1f },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("Full")
                    }

                    Button(
                        onClick = { fillLevel = 0f },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                    ) {
                        Text("Empty")
                    }
                }
            }
        }
    }
}

@Composable
fun BottleWithWater(
    fillLevel: Float,
    waveOffset: Float,
    waveAmplitude: Float,
    waveFrequency: Float
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        val bottleWidth = size.width * 0.4f
        val bottleHeight = size.height * 0.7f
        val neckWidth = bottleWidth * 0.4f
        val neckHeight = bottleHeight * 0.15f

        val startX = center.x - bottleWidth / 2
        val endX = center.x + bottleWidth / 2
        val topY = center.y - bottleHeight / 2
        val bottomY = center.y + bottleHeight / 2

        // Draw water first
        if (fillLevel > 0) {
            drawWater(
                startX = startX,
                endX = endX,
                bottomY = bottomY,
                topY = topY,
                neckHeight = neckHeight,
                fillLevel = fillLevel,
                waveOffset = waveOffset,
                waveAmplitude = waveAmplitude,
                waveFrequency = waveFrequency
            )
        }

        // Draw bottle outline
        drawBottleOutline(
            startX = startX,
            endX = endX,
            topY = topY,
            bottomY = bottomY,
            neckWidth = neckWidth,
            neckHeight = neckHeight
        )

        // Draw bottle neck highlight
        drawBottleNeck(
            center = center,
            neckWidth = neckWidth,
            neckHeight = neckHeight,
            topY = topY
        )

        // Draw measurement lines (optional)
        drawMeasurementLines(
            startX = startX,
            endX = endX,
            topY = topY,
            bottomY = bottomY,
            neckHeight = neckHeight
        )

        // Draw fill percentage text
        drawContext.canvas.nativeCanvas.drawText(
            "${(fillLevel * 100).toInt()}%",
            center.x,
            bottomY + 40.dp.toPx(),
            android.graphics.Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 24.sp.toPx()
                textAlign = android.graphics.Paint.Align.CENTER
                isFakeBoldText = true
            }
        )
    }
}

private fun DrawScope.drawWater(
    startX: Float,
    endX: Float,
    bottomY: Float,
    topY: Float,
    neckHeight: Float,
    fillLevel: Float,
    waveOffset: Float,
    waveAmplitude: Float,
    waveFrequency: Float
) {
    val waterHeight = (bottomY - topY - neckHeight) * fillLevel
    val waterSurfaceY = bottomY - waterHeight

    val waterPath = Path()

    // Start at bottom left
    waterPath.moveTo(startX, bottomY)

    // Draw right side bottom to surface
    waterPath.lineTo(endX, bottomY)
    waterPath.lineTo(endX, waterSurfaceY)

    // Draw wave at the top (right to left)
    val steps = 30
    for (i in steps downTo 0) {
        val x = startX + (endX - startX) * (i.toFloat() / steps)

        // Advanced sine wave with multiple harmonics for more natural look
        val wave1 = sin((x * waveFrequency / 100f) + waveOffset) * waveAmplitude
        val wave2 = sin((x * waveFrequency * 2 / 100f) + waveOffset * 2) * (waveAmplitude * 0.3f)
        val waveY = waterSurfaceY + wave1 + wave2

        waterPath.lineTo(x, waveY)
    }

    // Close path
    waterPath.close()

    // Draw water with gradient and subtle transparency
    drawPath(
        path = waterPath,
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF81D4FA).copy(alpha = 0.9f), // Light blue at top
                Color(0xFF0277BD).copy(alpha = 0.95f)  // Dark blue at bottom
            ),
            startY = waterSurfaceY,
            endY = bottomY
        )
    )

    // Add water surface highlight (small white line at the top of waves)
    val surfacePath = Path()
    for (i in 0..steps) {
        val x = startX + (endX - startX) * (i.toFloat() / steps)
        val wave1 = sin((x * waveFrequency / 100f) + waveOffset) * waveAmplitude
        val wave2 = sin((x * waveFrequency * 2 / 100f) + waveOffset * 2) * (waveAmplitude * 0.3f)
        val waveY = waterSurfaceY + wave1 + wave2

        if (i == 0) surfacePath.moveTo(x, waveY)
        else surfacePath.lineTo(x, waveY)
    }

    drawPath(
        path = surfacePath,
        color = Color.White.copy(alpha = 0.5f),
        style = Stroke(width = 2.dp.toPx())
    )
}

private fun DrawScope.drawBottleOutline(
    startX: Float,
    endX: Float,
    topY: Float,
    bottomY: Float,
    neckWidth: Float,
    neckHeight: Float
) {
    val path = Path()

    // Start at bottom left
    path.moveTo(startX, bottomY)

    // Left side of bottle
    path.lineTo(startX, topY + neckHeight)

    // Left side of neck
    path.lineTo(center.x - neckWidth / 2, topY + neckHeight)
    path.lineTo(center.x - neckWidth / 2, topY)

    // Top of bottle (opening)
    path.lineTo(center.x + neckWidth / 2, topY)

    // Right side of neck
    path.lineTo(center.x + neckWidth / 2, topY + neckHeight)

    // Right side of bottle
    path.lineTo(endX, topY + neckHeight)
    path.lineTo(endX, bottomY)

    // Close the path (bottom)
    path.close()

    // Draw bottle outline
    drawPath(
        path = path,
        color = Color.Black,
        style = Stroke(width = 4.dp.toPx())
    )
}

private fun DrawScope.drawBottleNeck(
    center: Offset,
    neckWidth: Float,
    neckHeight: Float,
    topY: Float
) {
    // Draw neck highlight
    drawLine(
        color = Color.LightGray,
        start = Offset(center.x - neckWidth / 2 + 2.dp.toPx(), topY + neckHeight),
        end = Offset(center.x - neckWidth / 2 + 2.dp.toPx(), topY),
        strokeWidth = 2.dp.toPx()
    )

    // Draw bottle rim
    drawRoundRect(
        color = Color.Gray,
        topLeft = Offset(center.x - neckWidth / 2 - 2.dp.toPx(), topY - 2.dp.toPx()),
        size = Size(neckWidth + 4.dp.toPx(), 4.dp.toPx()),
        cornerRadius = CornerRadius(2.dp.toPx())
    )
}

private fun DrawScope.drawMeasurementLines(
    startX: Float,
    endX: Float,
    topY: Float,
    bottomY: Float,
    neckHeight: Float
) {
    val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 5f))

    // Draw measurement lines at 25%, 50%, 75%
    for (i in 1..3) {
        val y = bottomY - (bottomY - topY - neckHeight) * (i * 0.25f)
        drawLine(
            color = Color.Gray.copy(alpha = 0.3f),
            start = Offset(startX - 5.dp.toPx(), y),
            end = Offset(endX + 5.dp.toPx(), y),
            strokeWidth = 1.dp.toPx(),
            pathEffect = dashPathEffect
        )

        // Draw percentage labels
        drawContext.canvas.nativeCanvas.drawText(
            "${i * 25}%",
            endX + 10.dp.toPx(),
            y + 4.dp.toPx(),
            android.graphics.Paint().apply {
                color = android.graphics.Color.GRAY
                textSize = 10.sp.toPx()
            }
        )
    }
}
