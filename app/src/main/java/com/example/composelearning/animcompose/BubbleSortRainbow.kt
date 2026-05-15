package com.example.composelearning.animcompose

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

private const val COLUMN_COUNT = 60
private const val SWAP_DELAY_MS = 28L
private const val PEEK_DELAY_MS = 3L
private const val PLACEMENT_ANIM_MS = 320

/**
 * Compose port of intmath.com/math-art-code/bubble-sort-rainbow.php
 *
 * Visual model: a row of vertical columns whose colours are hues evenly spaced around the wheel
 * (0°..360°). Start shuffled, run bubble sort, render — every time two adjacent columns are
 * swapped in the backing list, `LazyRow` sees the stable key landed in a different slot and
 * animates the placement for us. That's a 1:1 analog of the CSS-`order` reflow the source uses.
 *
 * Tap anywhere to reshuffle and start again.
 */
@Composable
fun BubbleSortRainbow(modifier: Modifier = Modifier) {
    // Bumping `generation` triggers a fresh shuffle and a new sort pass.
    var generation by remember { mutableIntStateOf(0) }

    val columns = remember(generation) {
        mutableStateListOf<ColorColumn>().apply {
            addAll(
                List(COLUMN_COUNT) { i -> ColorColumn(id = i, hue = i * 360f / COLUMN_COUNT) }
                    .shuffled(),
            )
        }
    }
    var comparing by remember(generation) { mutableStateOf<Pair<Int, Int>?>(null) }
    var status by remember(generation) { mutableStateOf("Sorting…") }

    LaunchedEffect(generation) {
        val n = columns.size
        for (i in 0 until n - 1) {
            for (j in 0 until n - i - 1) {
                comparing = columns[j].id to columns[j + 1].id
                if (columns[j].hue > columns[j + 1].hue) {
                    val tmp = columns[j]
                    columns[j] = columns[j + 1]
                    columns[j + 1] = tmp
                    delay(SWAP_DELAY_MS)
                } else {
                    delay(PEEK_DELAY_MS)
                }
            }
        }
        comparing = null
        status = "Sorted ✓  Tap anywhere to reshuffle."
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures { generation += 1 }
            },
    ) {
        val columnWidthDp = maxWidth / COLUMN_COUNT

        LazyRow(
            // Black background on the outer Box bleeds edge-to-edge behind the system bars,
            // but the rainbow itself stays within the safe area so no column gets clipped.
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
            userScrollEnabled = false,
        ) {
            items(items = columns, key = { it.id }) { item ->
                val isComparing = comparing
                    ?.let { (a, b) -> item.id == a || item.id == b }
                    ?: false
                Box(
                    modifier = Modifier
                        .animateItem(
                            placementSpec = tween(durationMillis = PLACEMENT_ANIM_MS),
                        )
                        .fillMaxHeight()
                        .width(columnWidthDp)
                        .background(Color.hsl(item.hue, 1f, 0.5f))
                        .then(
                            if (isComparing) Modifier.border(width = 2.dp, color = Color.White)
                            else Modifier,
                        ),
                )
            }
        }

        Text(
            text = status,
            color = Color.White,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                .background(Color.Black.copy(alpha = 0.55f))
                .padding(horizontal = 14.dp, vertical = 8.dp),
        )
    }
}

private data class ColorColumn(val id: Int, val hue: Float)
