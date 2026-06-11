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

package com.example.composelearning.sliders

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composelearning.util.LocalAnimationsEnabled
import kotlin.math.PI
import kotlin.math.sin

/**
 * A custom Slider implementation that mimics the Material Expressive Squiggly Slider.
 * The inactive track is straight, while the active progress part is wavy and animated.
 * It uses the standard Material 3 Slider with a custom track and thumb.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SquigglySliderSample(onBack: () -> Unit) {
    var sliderValue by remember { mutableFloatStateOf(0.4f) }
    var amplitude by remember { mutableFloatStateOf(4f) }
    var wavelength by remember { mutableFloatStateOf(20f) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Squiggly Slider") },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Material Expressive Squiggly",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Value: ${(sliderValue * 100).toInt()}%",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(64.dp))

            // THE SQUIGGLY SLIDER
            SquigglySlider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                amplitude = amplitude,
                wavelength = wavelength,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(80.dp))

            // Controls to customize the squiggle
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Design Parameters", fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Squiggle Amplitude: ${amplitude.toInt()}dp", fontSize = 12.sp)
                    Slider(value = amplitude, onValueChange = { amplitude = it }, valueRange = 0f..15f)

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Squiggle Wavelength: ${wavelength.toInt()}dp", fontSize = 12.sp)
                    Slider(value = wavelength, onValueChange = { wavelength = it }, valueRange = 10f..60f)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SquigglySlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    amplitude: Float,
    wavelength: Float,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.outlineVariant
) {
    val animationsEnabled = LocalAnimationsEnabled.current
    val infiniteTransition = rememberInfiniteTransition(label = "SquigglyTransition")
    val phase by if (animationsEnabled) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 2f * PI.toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "SquigglyPhase"
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    val interactionSource = remember { MutableInteractionSource() }

    Slider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        interactionSource = interactionSource,
        track = {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                val width = size.width
                val centerY = size.height / 2

                val ampPx = amplitude.dp.toPx()
                val wavePx = wavelength.dp.toPx()
                val piFloat = PI.toFloat()

                // 1. Draw the Inactive Track (Full Width, Straight)
                drawLine(
                    color = inactiveColor,
                    start = Offset(0f, centerY),
                    end = Offset(width, centerY),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // 2. Draw the Active Track (Up to slider value, Wavy)
                val activePath = Path()
                val limitX = width * value
                var x = 0f
                activePath.moveTo(0f, centerY + ampPx * sin(phase))
                while (x <= limitX) {
                    val y = centerY + ampPx * sin((2f * piFloat * x / wavePx) + phase)
                    activePath.lineTo(x, y)
                    x += 2f
                }

                // Ensure it ends exactly at thumb position
                if (limitX > 0f) {
                   activePath.lineTo(limitX, centerY + ampPx * sin((2f * piFloat * limitX / wavePx) + phase))
                }

                drawPath(
                    path = activePath,
                    color = activeColor,
                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        },
        thumb = {
            // Material 3 default thumb
            androidx.compose.material3.SliderDefaults.Thumb(
                interactionSource = interactionSource,
                colors = androidx.compose.material3.SliderDefaults.colors(thumbColor = activeColor)
            )
        }
    )
}

@Preview(showBackground = true)
@Composable
fun SquigglySliderSamplePreview() {
    MaterialTheme {
        SquigglySliderSample(onBack = {})
    }
}
