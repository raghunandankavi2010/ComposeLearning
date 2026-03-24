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
import androidx.compose.ui.graphics.drawscope.clipPath
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
        val bottleWidth = size.width * 0.35f
        val bottleHeight = size.height * 0.7f
        val neckWidth = bottleWidth * 0.45f
        val neckHeight = bottleHeight * 0.2f
        val shoulderHeight = bottleHeight * 0.1f
        val bodyHeight = bottleHeight - neckHeight - shoulderHeight

        val startX = center.x - bottleWidth / 2
        val endX = center.x + bottleWidth / 2
        val bottomY = center.y + bottleHeight / 2
        val topY = bottomY - bottleHeight

        // Bottle Path for outline and clipping
        val bottlePath = createBottlePath(
            center = center,
            bottleWidth = bottleWidth,
            bottleHeight = bottleHeight,
            neckWidth = neckWidth,
            neckHeight = neckHeight,
            shoulderHeight = shoulderHeight
        )

        // Draw water first with clipping to bottle shape
        if (fillLevel > 0) {
            clipPath(bottlePath) {
                drawWater(
                    startX = startX,
                    endX = endX,
                    bottomY = bottomY,
                    topY = topY,
                    fillLevel = fillLevel,
                    waveOffset = waveOffset,
                    waveAmplitude = waveAmplitude,
                    waveFrequency = waveFrequency
                )
            }
        }

        // Draw measurement lines
        drawMeasurementLines(
            startX = startX,
            endX = endX,
            bottomY = bottomY,
            bodyHeight = bodyHeight
        )

        // Draw bottle outline
        drawPath(
            path = bottlePath,
            color = Color.DarkGray,
            style = Stroke(width = 4.dp.toPx(), join = StrokeJoin.Round)
        )

        // Draw the Cap
        drawBottleCap(
            center = center,
            neckWidth = neckWidth,
            topY = topY
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

private fun createBottlePath(
    center: Offset,
    bottleWidth: Float,
    bottleHeight: Float,
    neckWidth: Float,
    neckHeight: Float,
    shoulderHeight: Float
): Path {
    val startX = center.x - bottleWidth / 2
    val endX = center.x + bottleWidth / 2
    val bottomY = center.y + bottleHeight / 2
    val topY = bottomY - bottleHeight
    val bodyHeight = bottleHeight - neckHeight - shoulderHeight
    
    return Path().apply {
        // Bottom left corner (rounded)
        moveTo(startX + 20f, bottomY)
        quadraticTo(startX, bottomY, startX, bottomY - 20f)
        
        // Left side body
        lineTo(startX, bottomY - bodyHeight)
        
        // Left shoulder (curved transition to neck)
        quadraticTo(
            startX, 
            topY + neckHeight, 
            center.x - neckWidth / 2, 
            topY + neckHeight
        )
        
        // Left neck
        lineTo(center.x - neckWidth / 2, topY)
        
        // Top opening
        lineTo(center.x + neckWidth / 2, topY)
        
        // Right neck
        lineTo(center.x + neckWidth / 2, topY + neckHeight)
        
        // Right shoulder
        quadraticTo(
            endX, 
            topY + neckHeight, 
            endX, 
            bottomY - bodyHeight
        )
        
        // Right side body
        lineTo(endX, bottomY - 20f)
        
        // Bottom right corner (rounded)
        quadraticTo(endX, bottomY, endX - 20f, bottomY)
        
        close()
    }
}

private fun DrawScope.drawBottleCap(
    center: Offset,
    neckWidth: Float,
    topY: Float
) {
    val capWidth = neckWidth + 10.dp.toPx()
    val capHeight = 22.dp.toPx()
    
    // Cap Body
    drawRoundRect(
        color = Color(0xFF2C3E50), // Nice dark navy/gray for cap
        topLeft = Offset(center.x - capWidth / 2, topY - capHeight + 2.dp.toPx()),
        size = Size(capWidth, capHeight),
        cornerRadius = CornerRadius(4.dp.toPx())
    )
    
    // Cap Details (vertical ridges)
    for (i in 0..6) {
        val x = (center.x - capWidth / 2) + (capWidth / 6) * i
        drawLine(
            color = Color.White.copy(alpha = 0.15f),
            start = Offset(x, topY - capHeight + 6.dp.toPx()),
            end = Offset(x, topY - 6.dp.toPx()),
            strokeWidth = 1.dp.toPx()
        )
    }
}

private fun DrawScope.drawMeasurementLines(
    startX: Float,
    endX: Float,
    bottomY: Float,
    bodyHeight: Float
) {
    val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
    val markers = listOf(
        0.25f to "250ml",
        0.50f to "500ml",
        0.75f to "750ml",
        1.00f to "1000ml"
    )

    markers.forEach { (ratio, label) ->
        val y = bottomY - (bodyHeight * ratio)
        
        // Draw dashed line across the bottle
        drawLine(
            color = Color.Black.copy(alpha = 0.2f),
            start = Offset(startX, y),
            end = Offset(endX, y),
            strokeWidth = 1.dp.toPx(),
            pathEffect = dashPathEffect
        )

        // Draw small tick marks outside
        drawLine(
            color = Color.DarkGray,
            start = Offset(startX - 5.dp.toPx(), y),
            end = Offset(startX, y),
            strokeWidth = 2.dp.toPx()
        )

        // Draw label text
        drawContext.canvas.nativeCanvas.drawText(
            label,
            startX - 10.dp.toPx(),
            y + 4.dp.toPx(),
            android.graphics.Paint().apply {
                color = android.graphics.Color.DKGRAY
                textSize = 12.sp.toPx()
                textAlign = android.graphics.Paint.Align.RIGHT
            }
        )
    }
}

private fun DrawScope.drawWater(
    startX: Float,
    endX: Float,
    bottomY: Float,
    topY: Float,
    fillLevel: Float,
    waveOffset: Float,
    waveAmplitude: Float,
    waveFrequency: Float
) {
    val totalHeight = bottomY - topY
    val waterHeight = totalHeight * fillLevel
    val waterSurfaceY = bottomY - waterHeight

    val waterPath = Path()
    // We draw water slightly wider than the bottle to ensure clipping covers edges
    val overfill = 100f
    
    waterPath.moveTo(startX - overfill, bottomY + overfill)
    waterPath.lineTo(endX + overfill, bottomY + overfill)
    waterPath.lineTo(endX + overfill, waterSurfaceY)

    val steps = 40
    for (i in steps downTo 0) {
        val x = (startX - overfill) + (endX - startX + 2 * overfill) * (i.toFloat() / steps)
        val wave1 = sin((x * waveFrequency / 100f) + waveOffset) * waveAmplitude
        val wave2 = sin((x * waveFrequency * 1.5f / 100f) + waveOffset * 1.5f) * (waveAmplitude * 0.4f)
        val waveY = waterSurfaceY + wave1 + wave2
        waterPath.lineTo(x, waveY)
    }
    waterPath.close()

    drawPath(
        path = waterPath,
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF4FC3F7), // Light Blue
                Color(0xFF0288D1)  // Deeper Blue
            ),
            startY = waterSurfaceY,
            endY = bottomY
        )
    )
    
    // Subtle foam/light line on top of waves
    val foamPath = Path()
    for (i in 0..steps) {
        val x = (startX - overfill) + (endX - startX + 2 * overfill) * (i.toFloat() / steps)
        val wave1 = sin((x * waveFrequency / 100f) + waveOffset) * waveAmplitude
        val wave2 = sin((x * waveFrequency * 1.5f / 100f) + waveOffset * 1.5f) * (waveAmplitude * 0.4f)
        val waveY = waterSurfaceY + wave1 + wave2
        
        if (i == 0) foamPath.moveTo(x, waveY)
        else foamPath.lineTo(x, waveY)
    }

    drawPath(
        path = foamPath,
        color = Color.White.copy(alpha = 0.3f),
        style = Stroke(width = 2.dp.toPx())
    )
}
