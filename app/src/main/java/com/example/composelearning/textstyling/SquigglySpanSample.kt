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

package com.example.composelearning.textstyling

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.sin

/**
 * An advanced version of squiggly spans.
 * This sample shows how to:
 * 1. Annotate parts of text using AnnotatedString.
 * 2. Find visual bounds of annotated parts using TextLayoutResult.
 * 3. Draw animated squiggles specifically under those parts, supporting multi-line wrapping.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SquigglySpanSample(onBack: () -> Unit) {
    var amplitude by remember { mutableFloatStateOf(3f) }
    var wavelength by remember { mutableFloatStateOf(10f) }

    val infiniteTransition = rememberInfiniteTransition(label = "SquiggleTransition")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Phase"
    )

    // The annotated text
    val annotatedText = buildAnnotatedString {
        append("This is normal text. ")
        withAnnotation(tag = "squiggly", annotation = "squiggle_1") {
            append("This part has a squiggly underline that might even wrap to the next line if the text is long enough.")
        }
        append(" And back to normal. ")
        withAnnotation(tag = "squiggly", annotation = "squiggle_2") {
            append("Short squiggle.")
        }
    }

    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Squiggly Spans with Annotations") },
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
                text = "Annotated Squiggly Underline",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Text with dynamic squiggle decoration
            Text(
                text = annotatedText,
                fontSize = 20.sp,
                lineHeight = 32.sp,
                onTextLayout = { textLayoutResult = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        val layout = textLayoutResult ?: return@drawBehind

                        // Find all annotations with tag "squiggly"
                        annotatedText.getStringAnnotations("squiggly", 0, annotatedText.length)
                            .forEach { annotation ->
                                // For each annotated range, we draw the squiggle
                                drawSquiggleForRange(
                                    layout = layout,
                                    start = annotation.start,
                                    end = annotation.end,
                                    amplitude = amplitude,
                                    wavelength = wavelength,
                                    phase = phase,
                                    color = Color.Red
                                )
                            }
                    }
            )

            Spacer(modifier = Modifier.height(48.dp))

            MathExplanation()

            Spacer(modifier = Modifier.height(24.dp))

            Text("Adjust the Math Parameters:", fontWeight = FontWeight.Bold)

            Text("Amplitude (Height): ${amplitude.toInt()}")
            Slider(value = amplitude, onValueChange = { amplitude = it }, valueRange = 1f..10f)

            Text("Wavelength (Width of one curve): ${wavelength.toInt()}")
            Slider(value = wavelength, onValueChange = { wavelength = it }, valueRange = 5f..50f)
        }
    }
}

/**
 * Draws a squiggly line under a specific range of text, handling multi-line wrapping.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSquiggleForRange(
    layout: TextLayoutResult,
    start: Int,
    end: Int,
    amplitude: Float,
    wavelength: Float,
    phase: Float,
    color: Color
) {
    val strokeWidth = 2.dp.toPx()
    val squigglePath = Path()

    // Get the lines that this range covers
    val startLine = layout.getLineForOffset(start)
    val endLine = layout.getLineForOffset(end)

    for (lineIndex in startLine..endLine) {
        // Determine the horizontal bounds for this segment on this specific line
        val lineStart = if (lineIndex == startLine) start else layout.getLineStart(lineIndex)
        val lineEnd = if (lineIndex == endLine) end else layout.getLineEnd(lineIndex)

        val startX = layout.getHorizontalPosition(lineStart, usePrimaryDirection = true)
        val endX = layout.getHorizontalPosition(lineEnd, usePrimaryDirection = true)

        // The Y position is at the baseline of the current line
        // We add a small offset so it's clearly an "underline"
        val yBase = layout.getLineBottom(lineIndex) - 2.dp.toPx()

        // Draw the segment
        var currentX = startX
        squigglePath.moveTo(currentX, yBase + amplitude * sin(2 * PI.toFloat() * currentX / wavelength + phase))

        while (currentX <= endX) {
            currentX += 1f // Small step for smoothness
            val y = yBase + amplitude * sin(2 * PI.toFloat() * currentX / wavelength + phase)
            squigglePath.lineTo(currentX, y)
        }
    }

    drawPath(
        path = squigglePath,
        color = color,
        style = Stroke(width = strokeWidth)
    )
}

@Composable
fun MathExplanation() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("How Annotated Squiggles Work", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Instead of drawing across the whole component, we use the `TextLayoutResult` to find the exact coordinates of specific character ranges.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("The logic flow:", fontWeight = FontWeight.SemiBold)
            Text(
                "1. Loop through `getStringAnnotations` to find specific parts.\n" +
                "2. For each part, find which lines it spans using `getLineForOffset`.\n" +
                "3. For each line, get the `startX` and `endX` using `getHorizontalPosition`.\n" +
                "4. Draw the sine wave between those points at `getLineBottom`.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SquigglySpanSamplePreview() {
    MaterialTheme {
         SquigglySpanSample(onBack = {})
    }
}
