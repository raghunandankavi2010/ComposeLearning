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
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.sin

/**
 * A custom Slider implementation that uses a Sine Wave as its track.
 * This demonstrates how to break away from standard "straight line" UI components.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SquigglySliderSample(onBack: () -> Unit) {
    var sliderValue by remember { mutableFloatStateOf(0.3f) }
    var amplitude by remember { mutableFloatStateOf(10f) }
    var wavelength by remember { mutableFloatStateOf(40f) }

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
                "Custom Path Slider",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Value: ${(sliderValue * 100).toInt()}%",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(48.dp))

            // THE SQUIGGLY SLIDER
            SquigglySlider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                amplitude = amplitude,
                wavelength = wavelength,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )

            Spacer(modifier = Modifier.height(64.dp))

            // Controls to customize the squiggle
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Design Parameters", fontWeight = FontWeight.Bold)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Amplitude (Wiggle Height): ${amplitude.toInt()}dp", fontSize = 12.sp)
                    Slider(value = amplitude, onValueChange = { amplitude = it }, valueRange = 0f..30f)

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Wavelength (Wiggle Frequency): ${wavelength.toInt()}dp", fontSize = 12.sp)
                    Slider(value = wavelength, onValueChange = { wavelength = it }, valueRange = 10f..100f)
                }
            }
        }
    }
}

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
    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    onValueChange((offset.x / size.width).coerceIn(0f, 1f))
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    onValueChange((change.position.x / size.width).coerceIn(0f, 1f))
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerY = height / 2
            
            val ampPx = amplitude.dp.toPx()
            val wavePx = wavelength.dp.toPx()

            // 1. Draw the Inactive Track (Full Width)
            val inactivePath = Path()
            var x = 0f
            inactivePath.moveTo(0f, centerY + ampPx * sin(0f))
            while (x <= width) {
                x += 2f
                val y = centerY + ampPx * sin(2 * PI.toFloat() * x / wavePx)
                inactivePath.lineTo(x, y)
            }
            drawPath(inactivePath, inactiveColor, style = Stroke(width = 4.dp.toPx()))

            // 2. Draw the Active Track (Up to slider value)
            val activePath = Path()
            val limitX = width * value
            x = 0f
            activePath.moveTo(0f, centerY + ampPx * sin(0f))
            while (x <= limitX) {
                x += 2f
                val y = centerY + ampPx * sin(2 * PI.toFloat() * x / wavePx)
                activePath.lineTo(x, y)
            }
            drawPath(activePath, activeColor, style = Stroke(width = 6.dp.toPx()))

            // 3. Draw the Thumb (The dot)
            val thumbX = width * value
            val thumbY = centerY + ampPx * sin(2 * PI.toFloat() * thumbX / wavePx)
            
            // Outer white glow
            drawCircle(
                color = Color.White,
                radius = 12.dp.toPx(),
                center = Offset(thumbX, thumbY)
            )
            // Inner color
            drawCircle(
                color = activeColor,
                radius = 10.dp.toPx(),
                center = Offset(thumbX, thumbY)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SquigglySliderSamplePreview() {
    MaterialTheme {
        SquigglySliderSample(onBack = {})
    }
}
