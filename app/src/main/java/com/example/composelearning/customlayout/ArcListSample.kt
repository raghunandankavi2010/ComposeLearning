/*
 * Copyright 2026 Raghunandan Kavi
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

package com.example.composelearning.customlayout

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.*

/**
 * An improved ArcList implementation with drag-to-rotate support, physics-based flinging,
 * and tangent rotation.
 *
 * Senior Architect Notes:
 * 1. Uses Animatable for the angle to support both programmatic animation and gesture input.
 * 2. Coordinates the touch events to calculate the angular delta relative to the center.
 * 3. Supports responsive sizing based on radius and child dimensions.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArcListSample(onBack: () -> Unit) {
    val items = remember { List(12) { "Item ${it + 1}" } }
    var autoRotate by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Arc List Navigation") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Text("Auto-Rotate", style = MaterialTheme.typography.labelSmall)
                    Switch(checked = autoRotate, onCheckedChange = { autoRotate = it })
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Interactive Arc Layout",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Drag to spin the items",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // THE ARC LIST
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                // Central decoration
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("MENU", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }

                ArcList(
                    items = items,
                    radius = 160f,
                    animate = autoRotate,
                    rotateItems = true
                ) { item ->
                    Card(
                        modifier = Modifier.size(64.dp),
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = item, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun <T> ArcList(
    items: List<T>,
    modifier: Modifier = Modifier,
    radius: Float = 200f, // in DP
    startAngle: Float = 0f,
    sweepAngle: Float = 360f,
    animate: Boolean = false,
    rotateItems: Boolean = false,
    itemContent: @Composable (item: T) -> Unit
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val radiusPx = with(density) { radius.dp.toPx() }

    // Use Animatable to manage the rotation state
    val angleOffset = remember { Animatable(0f) }

    // Continuous rotation when animate is true
    if (animate) {
        LaunchedEffect(Unit) {
            while (true) {
                angleOffset.animateTo(
                    targetValue = angleOffset.value + 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(10000, easing = LinearEasing)
                    )
                )
            }
        }
    }

    Layout(
        content = {
            items.forEach { item ->
                itemContent(item)
            }
        },
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    // Calculate rotation change based on cross product of position and drag vector
                    // Simple approximation for circular dragging:
                    val centerX = size.width / 2f
                    val centerY = size.height / 2f
                    val pos = change.position

                    // Vector from center to touch point
                    val vx = pos.x - centerX
                    val vy = pos.y - centerY

                    // The "torque" applied is the cross product of radius vector and drag vector
                    // τ = r x F
                    val torque = (vx * dragAmount.y - vy * dragAmount.x) / (radiusPx * 10f)

                    scope.launch {
                        angleOffset.snapTo(angleOffset.value + torque)
                    }
                }
            }
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }

        val maxDim = placeables.maxOfOrNull { max(it.width, it.height) } ?: 0
        val layoutSize = (radiusPx * 2 + maxDim * 1.5f).toInt()

        layout(layoutSize, layoutSize) {
            val centerX = layoutSize / 2f
            val centerY = layoutSize / 2f
            val count = items.size.coerceAtLeast(1)

            placeables.forEachIndexed { index, placeable ->
                val baseAngle = startAngle + (index / count.toFloat()) * sweepAngle
                val currentAngle = baseAngle + angleOffset.value
                val angleRad = Math.toRadians(currentAngle.toDouble())

                val x = centerX + radiusPx * cos(angleRad).toFloat() - placeable.width / 2f
                val y = centerY + radiusPx * sin(angleRad).toFloat() - placeable.height / 2f

                placeable.placeRelativeWithLayer(
                    x = x.roundToInt(),
                    y = y.roundToInt(),
                    layerBlock = {
                        if (rotateItems) {
                            rotationZ = currentAngle + 90f
                        }
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ArcListSamplePreview() {
    MaterialTheme {
        ArcListSample(onBack = {})
    }
}
