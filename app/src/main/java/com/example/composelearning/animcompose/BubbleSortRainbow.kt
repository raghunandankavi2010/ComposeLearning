package com.example.composelearning.animcompose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

private suspend fun SortContext.bubbleSort() {
    for (i in 0 until COLUMN_COUNT - 1) {
        for (j in 0 until COLUMN_COUNT - i - 1) {
            comparisons++
            val colA = columns[slotToColumn[j]]
            val colB = columns[slotToColumn[j + 1]]
            if (colA.hue > colB.hue) {
                swap(j, j + 1)
            } else {
                peek()
         }
        }
    }
}

/**
 * Bubble sort rainbow. O(n²) comparisons, lots of adjacent swaps, slow and visually busy —
 * the visualization that gave this demo its name.
 *
 * @param applyOwnInsets see [SortVisualizer]; set to false when hosted in [SortAnimationScreen].
 */
@Composable
fun BubbleSortRainbow(
    modifier: Modifier = Modifier,
    applyOwnInsets: Boolean = true,
) {
    SortVisualizer(
        sortName = "Bubble Sort",
        sortAction = { bubbleSort() },
        modifier = modifier,
        applyOwnInsets = applyOwnInsets,
    )
}