package com.example.composelearning.pager

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

// Swiggy-Instamart-style thumbnail strip: a horizontal LazyRow that follows a subtle arc, with
// the centred item highlighted by a coloured border ring. All bubbles are the same size — the
// only difference for the active item is the border.

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
)

@Composable
fun ArcCarouselScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Text(
            text = "Arc Carousel",
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(24.dp),
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        ArcCarousel(
            items = sampleArcItems,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
        )
    }
}

@Composable
fun ArcCarousel(
    items: List<ArcItem>,
    modifier: Modifier = Modifier,
    itemSize: Dp = 64.dp,
    itemSpacing: Dp = 18.dp,
    arcDepth: Dp = 20.dp, // subtle curve — Swiggy's strip is almost flat
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
        // bubble + tight gap + 1 line label + arc drop room
        val containerHeight = itemSize + 4.dp + 18.dp + arcDepth + 8.dp

        Box(modifier = Modifier.height(containerHeight)) {
            LazyRow(
                state = state,
                flingBehavior = flingBehavior,
                horizontalArrangement = Arrangement.spacedBy(itemSpacing),
                contentPadding = PaddingValues(horizontal = sidePadding),
                modifier = Modifier.fillMaxWidth()
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

    // Transform applied to the column → bubble and label translate together, so the label
    // always sits a few dp below its bubble regardless of where the bubble lands on the arc.
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(bubbleSize)
            .graphicsLayer {
                val info = state.layoutInfo
                val itemInfo = info.visibleItemsInfo.find { it.index == index }
                if (itemInfo != null) {
                    val halfViewport =
                        (info.viewportEndOffset - info.viewportStartOffset) / 2f
                    val viewportCenter = info.viewportStartOffset + halfViewport
                    val itemCenter = itemInfo.offset + itemInfo.size / 2f
                    val normalised =
                        ((itemCenter - viewportCenter) / halfViewport).coerceIn(-1f, 1f)
                    // y = x² × arcDepth — dome: centre stays at top, edges drop down.
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
