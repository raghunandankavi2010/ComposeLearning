/*
 * Copyright 2024 Raghunandan Kavi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.composelearning.graphics

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.composelearning.util.LocalAnimationsEnabled
import kotlin.math.PI
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SineWaveSample(onBack: () -> Unit) {
    var amplitude by remember { mutableFloatStateOf(50f) }
    var frequency by remember { mutableFloatStateOf(1f) }
    var dotProgress by remember { mutableFloatStateOf(0.5f) }

    val animationsEnabled = LocalAnimationsEnabled.current
    val infiniteTransition = rememberInfiniteTransition(label = "SineWaveTransition")
    val phase by if (animationsEnabled) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 2f * PI.toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "PhaseAnimation"
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

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
private fun SineWaveSamplePreview() {
    MaterialTheme {
        SineWaveSample(onBack = {})
    }
}
