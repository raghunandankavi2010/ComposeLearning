package com.example.composelearning.customlayout

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomPagerSample(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Custom Pager (Low Level)") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            Text(
                "Manual Pager Logic",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(16.dp)
            )
            
            // The actual Pager implementation
            CustomPager(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                pageCount = 5
            ) { pageIndex ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            listOf(Color(0xFFE91E63), Color(0xFF2196F3), Color(0xFF4CAF50), Color(0xFFFF9800), Color(0xFF9C27B0))[pageIndex]
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Page ${pageIndex + 1}",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            PagerLogicExplanation()
        }
    }
}

@Composable
fun CustomPager(
    modifier: Modifier = Modifier,
    pageCount: Int,
    content: @Composable (Int) -> Unit
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    
    // Total offset in pixels
    val offset = remember { Animatable(0f) }
    var pageWidth by remember { mutableFloatStateOf(0f) }
    
    // We use dp for the velocity threshold to ensure consistency across screens
    val velocityThreshold = with(density) { 1000.dp.toPx() } 

    Box(
        modifier = modifier
            .pointerInput(pageCount) {
                val velocityTracker = VelocityTracker()
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        velocityTracker.addPosition(change.uptimeMillis, change.position)
                        scope.launch {
                            // Boundary checks for the drag
                            val minOffset = -( (pageCount - 1) * pageWidth)
                            val maxOffset = 0f
                            val newOffset = (offset.value + dragAmount.x).coerceIn(minOffset - 100f, maxOffset + 100f)
                            offset.snapTo(newOffset)
                        }
                    },
                    onDragEnd = {
                        val velocity = velocityTracker.calculateVelocity().x
                        
                        // LOGIC: Convert velocity/position to a Target Page
                        val currentPage = -(offset.value / pageWidth)
                        val targetPage = when {
                            // If swiping fast enough, change page regardless of distance
                            velocity.absoluteValue > velocityThreshold -> {
                                if (velocity > 0) currentPage.toInt() else currentPage.toInt() + 1
                            }
                            // Otherwise, check if we've crossed the 50% threshold
                            else -> currentPage.roundToInt()
                        }.coerceIn(0, pageCount - 1)

                        scope.launch {
                            offset.animateTo(
                                targetValue = -(targetPage * pageWidth),
                                initialVelocity = velocity,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                        }
                    }
                )
            }
    ) {
        Layout(
            content = { repeat(pageCount) { content(it) } }
        ) { measurables, constraints ->
            pageWidth = constraints.maxWidth.toFloat()
            val placeables = measurables.map { it.measure(constraints) }

            layout(constraints.maxWidth, constraints.maxHeight) {
                placeables.forEachIndexed { index, placeable ->
                    placeable.placeRelative(
                        x = (index * constraints.maxWidth) + offset.value.roundToInt(),
                        y = 0
                    )
                }
            }
        }
    }
}

@Composable
fun PagerLogicExplanation() {
    Card(
        modifier = Modifier.padding(16.dp).fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Converting Velocity & Thresholds", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "• Velocity to Pixels: Velocity in Compose is in pixels per second. 1000dp/s is a good 'fling' threshold.",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                "• Positional Threshold: offset.value / pageWidth. We use roundToInt() to find the nearest page when velocity is low.",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                "• Boundary Physics: We coerce the drag but allow a slight 'over-scroll' (100px) which the spring then snaps back.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CustomPagerSamplePreview() {
    MaterialTheme {
        CustomPagerSample(onBack = {})
    }
}
