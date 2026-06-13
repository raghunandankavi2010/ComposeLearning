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

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.SliderDefaults
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

    // Optimization: Use a State object and read its value only inside the Canvas.
    // This prevents the entire Slider from recomposing on every animation frame.
    val phaseProvider = if (animationsEnabled) {
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
        remember { mutableFloatStateOf(0f) }
    }

    val interactionSource = remember { MutableInteractionSource() }

    // Optimization: Reuse the Path object to avoid allocations during draw calls.
    val activePath = remember { Path() }

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
                val phase = phaseProvider.value
                val width = size.width
                val centerY = size.height / 2
                val limitX = width * value

                val ampPx = amplitude.dp.toPx()
                val wavePx = wavelength.dp.toPx()

                // 1. Draw the Inactive Track (Straight)
                // Drawing from limitX onwards avoids overlapping with the active wavy track.
                drawLine(
                    color = inactiveColor,
                    start = Offset(limitX, centerY),
                    end = Offset(width, centerY),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // 2. Draw the Active Track (Wavy)
                if (wavePx > 0f && limitX > 0f) {
                    activePath.reset()
                    var x = 0f
                    activePath.moveTo(0f, centerY + ampPx * sin(phase))

                    // Use a density-aware step for a smooth curve across different screen densities.
                    val step = 1.dp.toPx()
                    val frequency = (2f * PI.toFloat()) / wavePx

                    while (x < limitX) {
                        x = (x + step).coerceAtMost(limitX)
                        val y = centerY + ampPx * sin((frequency * x) + phase)
                        activePath.lineTo(x, y)
                    }

                    drawPath(
                        path = activePath,
                        color = activeColor,
                        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
        },
        thumb = {
            SliderDefaults.Thumb(
                interactionSource = interactionSource,
                colors = SliderDefaults.colors(thumbColor = activeColor)
            )
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun SquigglySliderSamplePreview() {
    MaterialTheme {
        SquigglySliderSample(onBack = {})
    }
}
