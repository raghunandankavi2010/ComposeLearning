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

package com.example.composelearning.pager

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composelearning.customlayout.ArcList
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

data class ArcItem(val emoji: String, val label: String)

private val sampleArcItems = listOf(
    ArcItem("🥦", "Vegetables"),
    ArcItem("🍎", "Fruits"),
    ArcItem("🥛", "Dairy"),
    ArcItem("🍞", "Bakery"),
    ArcItem("🍫", "Snacks"),
    ArcItem("🥤", "Drinks"),
    ArcItem("🧴", "Personal Care"),
    ArcItem("🧹", "Home"),
    ArcItem("🐶", "Pet Care"),
    ArcItem("🧸", "Baby"),
    ArcItem("🍔", "Burgers"),
    ArcItem("🍕", "Pizza"),
    ArcItem("🍩", "Donuts"),
    ArcItem("🍦", "Ice Cream")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArcCarouselScreen(onBack: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Instamart", "Circular", "Diagonal Arc")

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Arc Layouts Showcase") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontSize = 12.sp) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (selectedTab) {
                0 -> InstamartTab()
                1 -> CircularMenuTab()
                2 -> DiagonalArcTab()
            }
        }
    }
}

@Composable
private fun InstamartTab() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Swiggy Instamart Style",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.titleMedium
        )
        ArcCarousel(
            items = sampleArcItems,
            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
        )
    }
}

@Composable
private fun CircularMenuTab() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Circular Orbital Menu", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(32.dp))
        
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier.size(80.dp).background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("CENTER", fontWeight = FontWeight.Bold)
            }
            
            ArcList(
                items = sampleArcItems.take(8),
                radius = 140f,
                rotateItems = true,
                animate = true
            ) { item ->
                Box(
                    modifier = Modifier.size(50.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(item.emoji, fontSize = 20.sp)
                }
            }
        }
    }
}

@Composable
private fun DiagonalArcTab() {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "Center-Left to Top-Right Arc",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.titleMedium
        )
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 32.dp) // Leave room for labels
        ) {
            val listState = rememberLazyListState()

            LazyRow(
                state = listState,
                modifier = Modifier.fillMaxSize().systemGestureExclusion(),
                // Use contentPadding to ensure items don't hit the screen edges immediately
                contentPadding = PaddingValues(horizontal = 40.dp, vertical = 60.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                itemsIndexed(sampleArcItems) { index, item ->
                    DiagonalArcItem(
                        item = item,
                        index = index,
                        state = listState
                    )
                }
            }
        }
    }
}

@Composable
private fun DiagonalArcItem(
    item: ArcItem,
    index: Int,
    state: LazyListState
) {
    val bubbleSize = 90.dp
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(bubbleSize)
            .graphicsLayer {
                val layoutInfo = state.layoutInfo
                val itemInfo = layoutInfo.visibleItemsInfo.find { it.index == index }
                
                if (itemInfo != null) {
                    val viewportWidth = layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset
                    
                    // x range: 0.0 (left) to 1.0 (right)
                    val xPos = (itemInfo.offset + itemInfo.size / 2f) / viewportWidth.toFloat()
                    val x = xPos.coerceIn(0f, 1f)
                    
                    // Arc Math: Center-Left (0, 0.5) to Top-Right (1, 0)
                    // Linear path would be: y = 0.5 * (1 - x)
                    // Curved path (Arc): y = 0.5 * sqrt(1 - x^2) 
                    // Or simpler quadratic curve: y = 0.5 * (1 - x^2)
                    
                    val arcY = 0.5f * (1f - (x * x))
                    
                    // We translate from the "natural" middle vertical alignment
                    // Offset goes from 0 (middle) to -0.5 (top)
                    translationY = -(x * size.height * 0.45f)
                    
                    // Add subtle scaling based on position
                    val scale = 0.8f + (0.2f * (1f - abs(x - 0.5f) * 2))
                    scaleX = scale
                    scaleY = scale
                }
            }
    ) {
        Box(
            modifier = Modifier
                .size(bubbleSize)
                .background(
                    Brush.radialGradient(
                        colors = listOf(MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.tertiary),
                    ),
                    CircleShape
                )
                .border(3.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(item.emoji, fontSize = 36.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            item.label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// --- Original Components ---

@Composable
fun ArcCarousel(
    items: List<ArcItem>,
    modifier: Modifier = Modifier,
    itemSize: Dp = 64.dp,
    itemSpacing: Dp = 18.dp,
    arcDepth: Dp = 20.dp,
) {
    val state = rememberLazyListState()
    val flingBehavior = rememberSnapFlingBehavior(
        lazyListState = state,
        snapPosition = SnapPosition.Center,
    )
    val arcDepthPx = with(LocalDensity.current) { arcDepth.toPx() }

    val activeIndex by remember {
        derivedStateOf {
            val info = state.layoutInfo
            if (info.visibleItemsInfo.isEmpty()) return@derivedStateOf -1
            val center =
                info.viewportStartOffset + (info.viewportEndOffset - info.viewportStartOffset) / 2
            info.visibleItemsInfo.minBy { abs((it.offset + it.size / 2) - center) }.index
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val sidePadding = (maxWidth - itemSize) / 2
        val containerHeight = itemSize + 4.dp + 18.dp + arcDepth + 8.dp

        Box(modifier = Modifier.height(containerHeight)) {
            LazyRow(
                state = state,
                flingBehavior = flingBehavior,
                horizontalArrangement = Arrangement.spacedBy(itemSpacing),
                contentPadding = PaddingValues(horizontal = sidePadding),
                modifier = Modifier.fillMaxWidth().systemGestureExclusion()
            ) {
                itemsIndexed(items) { index, item ->
                    ArcItemView(
                        item = item,
                        index = index,
                        isActive = index == activeIndex,
                        bubbleSize = itemSize,
                        arcDepthPx = arcDepthPx,
                        state = state,
                    )
                }
            }
        }
    }
}

@Composable
private fun ArcItemView(
    item: ArcItem,
    index: Int,
    isActive: Boolean,
    bubbleSize: Dp,
    arcDepthPx: Float,
    state: LazyListState,
) {
    val activeBorder = MaterialTheme.colorScheme.primary
    val bubbleFill = MaterialTheme.colorScheme.surfaceVariant
    val labelColor = MaterialTheme.colorScheme.onSurface

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(bubbleSize)
            .graphicsLayer {
                val info = state.layoutInfo
                val itemInfo = info.visibleItemsInfo.find { it.index == index }
                if (itemInfo != null) {
                    val halfViewport = (info.viewportEndOffset - info.viewportStartOffset) / 2f
                    val viewportCenter = info.viewportStartOffset + halfViewport
                    val itemCenter = itemInfo.offset + itemInfo.size / 2f
                    val normalised = ((itemCenter - viewportCenter) / halfViewport).coerceIn(-1f, 1f)
                    translationY = (normalised * normalised) * arcDepthPx
                }
            }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(bubbleSize)
                .clip(CircleShape)
                .background(bubbleFill)
                .then(
                    if (isActive) Modifier.border(
                        width = 2.dp,
                        color = activeBorder,
                        shape = CircleShape
                    ) else Modifier
                )
        ) {
            Text(text = item.emoji, fontSize = 26.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = item.label,
            fontSize = 11.sp,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isActive) activeBorder else labelColor.copy(alpha = 0.85f),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ArcCarouselScreenPreview() {
    MaterialTheme {
        ArcCarouselScreen(onBack = {})
    }
}
