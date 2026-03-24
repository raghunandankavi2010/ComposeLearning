package com.example.composelearning.animcompose

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun ThermometerAnimation() {
    var temperature by remember { mutableFloatStateOf(37f) } // Default body temp
    val animatedTemperature by animateFloatAsState(
        targetValue = temperature,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "temp_anim"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Text(
            text = "Thermometer Animation",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Thermometer(
                temperature = animatedTemperature,
                minTemp = 35f,
                maxTemp = 42f
            )
        }

        // Temperature Control Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Set Temperature",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "%.1f°C", temperature),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (temperature > 38f) Color(0xFFEF4444) else Color(0xFF3B82F6)
                    )
                }

                Slider(
                    value = temperature,
                    onValueChange = { temperature = it },
                    valueRange = 35f..42f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF3B82F6),
                        activeTrackColor = Color(0xFF3B82F6).copy(alpha = 0.5f)
                    )
                )

                // Quick Presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PresetButton("Normal", 36.6f, Modifier.weight(1f)) { temperature = it }
                    PresetButton("Fever", 39.5f, Modifier.weight(1f)) { temperature = it }
                    PresetButton("High", 41.0f, Modifier.weight(1f)) { temperature = it }
                }
            }
        }
    }
}

@Composable
private fun PresetButton(label: String, temp: Float, modifier: Modifier, onClick: (Float) -> Unit) {
    OutlinedButton(
        onClick = { onClick(temp) },
        modifier = modifier,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(label, fontSize = 12.sp)
    }
}

@Composable
fun Thermometer(
    temperature: Float,
    minTemp: Float,
    maxTemp: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxHeight().width(120.dp)) {
        val bulbRadius = 35.dp.toPx()
        val tubeWidth = 24.dp.toPx()
        val totalHeight = size.height
        val bottomOffset = 60.dp.toPx()
        
        val centerX = size.width / 2
        val bulbCenterY = totalHeight - bottomOffset - bulbRadius
        
        val tubeTop = 40.dp.toPx()
        val tubeBottom = bulbCenterY
        val tubeHeight = tubeBottom - tubeTop

        // 1. Draw Glass Outline (Tube + Bulb)
        val glassPath = Path().apply {
            // Bulb
            addOval(Rect(centerX - bulbRadius, bulbCenterY - bulbRadius, centerX + bulbRadius, bulbCenterY + bulbRadius))
            
            // Tube
            moveTo(centerX - tubeWidth / 2, tubeBottom)
            lineTo(centerX - tubeWidth / 2, tubeTop + 10.dp.toPx())
            quadraticTo(centerX - tubeWidth / 2, tubeTop, centerX, tubeTop)
            quadraticTo(centerX + tubeWidth / 2, tubeTop, centerX + tubeWidth / 2, tubeTop + 10.dp.toPx())
            lineTo(centerX + tubeWidth / 2, tubeBottom)
        }

        // Draw Outer Glass Shadow/Effect
        drawPath(
            path = glassPath,
            color = Color(0xFFE2E8F0),
            style = Stroke(width = 8.dp.toPx(), join = StrokeJoin.Round)
        )
        
        // Inner Glass Background
        drawPath(
            path = glassPath,
            color = Color.White,
            style = Fill
        )

        // 2. Draw Measurement Markers
        drawMarkers(
            centerX = centerX,
            tubeWidth = tubeWidth,
            tubeTop = tubeTop,
            tubeBottom = tubeBottom,
            minTemp = minTemp,
            maxTemp = maxTemp
        )

        // 3. Draw Liquid (Mercury/Red Fluid)
        val fillRatio = ((temperature - minTemp) / (maxTemp - minTemp)).coerceIn(0f, 1f)
        val liquidTopY = tubeBottom - (tubeHeight * fillRatio)
        
        // Liquid Color based on temperature
        val liquidColor = if (temperature > 38f) Color(0xFFEF4444) else Color(0xFF3B82F6)

        // Liquid in Bulb
        drawCircle(
            color = liquidColor,
            radius = bulbRadius - 8.dp.toPx(),
            center = Offset(centerX, bulbCenterY)
        )

        // Liquid in Tube
        drawRoundRect(
            color = liquidColor,
            topLeft = Offset(centerX - (tubeWidth / 2) + 6.dp.toPx(), liquidTopY),
            size = Size(tubeWidth - 12.dp.toPx(), tubeBottom - liquidTopY + 2.dp.toPx()),
            cornerRadius = CornerRadius(4.dp.toPx())
        )
        
        // Highlight on liquid for 3D effect
        drawRoundRect(
            color = Color.White.copy(alpha = 0.3f),
            topLeft = Offset(centerX - (tubeWidth / 2) + 8.dp.toPx(), liquidTopY + 4.dp.toPx()),
            size = Size(4.dp.toPx(), tubeBottom - liquidTopY - 8.dp.toPx()),
            cornerRadius = CornerRadius(2.dp.toPx())
        )
    }
}

private fun DrawScope.drawMarkers(
    centerX: Float,
    tubeWidth: Float,
    tubeTop: Float,
    tubeBottom: Float,
    minTemp: Float,
    maxTemp: Float
) {
    val tubeHeight = tubeBottom - tubeTop
    val totalTicks = (maxTemp - minTemp).toInt()
    
    for (i in 0..totalTicks) {
        val y = tubeBottom - (tubeHeight * (i.toFloat() / totalTicks))
        val tempValue = minTemp + i
        
        // Main Ticks
        drawLine(
            color = Color(0xFF94A3B8),
            start = Offset(centerX + tubeWidth / 2, y),
            end = Offset(centerX + tubeWidth / 2 + 12.dp.toPx(), y),
            strokeWidth = 2.dp.toPx()
        )
        
        // Sub-ticks
        if (i < totalTicks) {
            for (j in 1..4) {
                val subY = y - (tubeHeight / totalTicks) * (j.toFloat() / 5f)
                drawLine(
                    color = Color(0xFFCBD5E1),
                    start = Offset(centerX + tubeWidth / 2, subY),
                    end = Offset(centerX + tubeWidth / 2 + 6.dp.toPx(), subY),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }

        // Labels
        drawContext.canvas.nativeCanvas.drawText(
            "${tempValue.toInt()}",
            centerX + tubeWidth / 2 + 18.dp.toPx(),
            y + 5.dp.toPx(),
            android.graphics.Paint().apply {
                color = android.graphics.Color.GRAY
                textSize = 12.sp.toPx()
                textAlign = android.graphics.Paint.Align.LEFT
                isAntiAlias = true
            }
        )
    }
}
