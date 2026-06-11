package com.example.composelearning.lists

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage

@Composable
fun ParallaxListScreen() {
    val lazyListState = rememberLazyListState()

    // Controlled calculation to keep the image perfectly inside the viewport
    val parallaxOffset by remember {
        derivedStateOf {
            val layoutInfo = lazyListState.layoutInfo
            val firstVisibleItem = layoutInfo.visibleItemsInfo.firstOrNull { it.index == 0 }

            if (firstVisibleItem != null) {
                // firstVisibleItem.offset goes from 0 down to -300dp (in pixels).
                // We negate it so the image translates DOWN relative to the box as
                // the header scrolls up. This keeps the image's BOTTOM edge pinned to
                // the box bottom (no gap above the list), while the uncovered sliver
                // stays at the TOP of the header — which is already scrolled off-screen.
                // The image still moves at half speed, preserving the parallax depth.
                -firstVisibleItem.offset * 0.5f
            } else {
                0f
            }
        }
    }

    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .fillMaxSize()
            .testTag("parallax_list")
    ) {
        // Parallax Header Item
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clipToBounds() // Keeps the image from bleeding into other items
                    .testTag("parallax_header")
            ) {
                // LAYER 1: Background Image
                AsyncImage(
                    model = "https://picsum.photos/id/26/1000/600",
                    contentDescription = "Parallax Background",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        // We scale the image slightly up (e.g., 1.3x) so it has extra "slack"
                        // to move up and down without exposing its edges!
                        .fillMaxSize()
                        .testTag("parallax_image")
                        .graphicsLayer {
                            translationY = parallaxOffset
                        }
                )

                // LAYER 2: Text Header
                Text(
                    text = "Parallax Header",
                    color = Color.White,
                    fontSize = 32.sp,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                )
            }
        }

        // Standard List Content
        items(50) { index ->
            Text(
                text = "List Item $index",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }
}
