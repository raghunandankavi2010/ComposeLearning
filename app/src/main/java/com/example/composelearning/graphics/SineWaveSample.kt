package com.example.composelearning.graphics

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SineWaveSample(onBack: () -> Unit) {
    var amplitude by remember { mutableFloatStateOf(50f) }
    var frequency by remember { mutableFloatStateOf(1f) }
    var dotProgress by remember { mutableFloatStateOf(0.5f) }
    
    val infiniteTransition = rememberInfiniteTransition(label = "SineWaveTransition")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PhaseAnimation"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sine Wave Animation") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Dynamic Sine Wave",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Formula: y = A * sin(2π * f * x + φ)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Wave Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = MaterialTheme.shapes.medium
                    ),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val centerY = height / 2

                    val path = Path()
                    val points = 100
                    for (i in 0..points) {
                        // values from left to right. we have 100 points. Each point is width /points
                        // initially i is 0. start at left. if i is 50 its the middle and 100 is the end
                        val x = i * (width / points)
                        // Normalize x to 0..1 for frequency calculation
                        val normalizedX = i.toFloat() / points
                        val y = centerY + amplitude * sin(2 * PI.toFloat() * frequency * normalizedX + phase)
                        
                        if (i == 0) {
                            path.moveTo(x, y)
                        } else {
                            path.lineTo(x, y)
                        }
                    }

                    // Draw the wave
                    drawPath(
                        path = path,
                        color = Color(0xFF2196F3),
                        style = Stroke(width = 4.dp.toPx())
                    )

                    // Draw the dot
                    val dotX = dotProgress * width
                    val dotY = centerY + amplitude * sin(2 * PI.toFloat() * frequency * dotProgress + phase)
                    
                    // Draw vertical guide
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.5f),
                        start = Offset(dotX, 0f),
                        end = Offset(dotX, height),
                        strokeWidth = 1.dp.toPx()
                    )

                    drawCircle(
                        color = Color.Red,
                        radius = 8.dp.toPx(),
                        center = Offset(dotX, dotY)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Controls
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Controls",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Amplitude Slider
                    Text(text = "Peak Amplitude: ${amplitude.toInt()} px", style = MaterialTheme.typography.bodySmall)
                    Slider(
                        value = amplitude,
                        onValueChange = { amplitude = it },
                        valueRange = 0f..120f
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Frequency Slider
                    Text(text = "Frequency: ${String.format("%.1f", frequency)} Hz", style = MaterialTheme.typography.bodySmall)
                    Slider(
                        value = frequency,
                        onValueChange = { frequency = it },
                        valueRange = 0.5f..5f
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Dot Progress Slider
                    Text(text = "Dot Position (x): ${String.format("%.2f", dotProgress)}", style = MaterialTheme.typography.bodySmall)
                    Slider(
                        value = dotProgress,
                        onValueChange = { dotProgress = it },
                        valueRange = 0f..1f
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            InfoSection()
        }
    }
}

@Composable
private fun InfoSection() {
    Column {
        Text(
            text = "How it works",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "We use rememberInfiniteTransition to animate the 'phase' (φ) of the sine wave. " +
                   "The Canvas then redraws the Path on every frame. " +
                   "The dot's position is calculated using the same sine formula, ensuring it stays perfectly on the curve.",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SineWaveSamplePreview() {
    MaterialTheme {
        SineWaveSample(onBack = {})
    }
}
