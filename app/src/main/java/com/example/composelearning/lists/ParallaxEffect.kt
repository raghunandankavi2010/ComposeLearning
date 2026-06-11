package com.example.composelearning.lists

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

/*
 * Copyright 2026 Raghunandan Kavi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@Composable
fun FullParallaxBackgroundListScreen() {
    val lazyListState = rememberLazyListState()

    // Calculate the background translation based on total scroll content
    val parallaxOffset by remember {
        derivedStateOf {
            // We use the first visible item index and its offset to calculate total pixels scrolled
            val layoutInfo = lazyListState.layoutInfo
            val firstVisibleItem = layoutInfo.visibleItemsInfo.firstOrNull()

            if (firstVisibleItem != null) {
                // Estimate overall scroll progress: (Index * Estimated Item Height) - Current Item Offset
                // Let's assume an average item height of roughly 150px for the translation factor
                val estimatedScroll = (firstVisibleItem.index * 150) - firstVisibleItem.offset

                // Multiply by a smaller factor (e.g., -0.2f) so it moves slowly upward as you scroll down
                -estimatedScroll * 0.2f
            } else {
                0f
            }
        }
    }

    // Outer Box stacks the background image and the list on top of each other
    Box(modifier = Modifier.fillMaxSize()) {

        // LAYER 1: Fixed/Slow-Moving Background
        AsyncImage(
            model = "https://picsum.photos/id/26/1000/1200", // Using a taller image to handle movement
            contentDescription = "Parallax Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // Apply the continuous parallax shift to the Y axis
                    translationY = parallaxOffset
                }
        )

        // LAYER 2: Transparent LazyColumn that scrolls over the background
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize()
        ) {
            items(50) { index ->
                Text(
                    text = "List Item $index",
                    color = Color.White, // High contrast text against the image background
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                )
            }
        }
    }
}
