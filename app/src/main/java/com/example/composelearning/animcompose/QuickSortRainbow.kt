package com.example.composelearning.animcompose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Lomuto partition scheme: pivot is the rightmost slot's hue; everything ≤ pivot moves left,
 * then the pivot drops into its final position. The visualization shows blocks of similar hues
 * collapsing toward the pivot, then the algorithm recurses into the two halves.
 */
private suspend fun SortContext.quickSort(lo: Int, hi: Int) {
    if (lo >= hi) return
    val pivotHue = columns[slotToColumn[hi]].hue
    var i = lo - 1
    for (j in lo until hi) {
        comparisons++
        if (columns[slotToColumn[j]].hue <= pivotHue) {
            i++
            if (i != j) swap(i, j) else peek()
        } else {
            peek()
        }
    }
    val pivotSlot = i + 1
    if (pivotSlot != hi) swap(pivotSlot, hi) else peek()
    quickSort(lo, pivotSlot - 1)
    quickSort(pivotSlot + 1, hi)
}

/**
 * Quick sort rainbow. O(n log n) average — far fewer swaps than [BubbleSortRainbow] on the
 * same 32-column data, with a partitioning visualization that flows toward the pivot.
 *
 * @param applyOwnInsets see [SortVisualizer]; set to false when hosted in [SortAnimationScreen].
 */
@Composable
fun QuickSortRainbow(
    modifier: Modifier = Modifier,
    applyOwnInsets: Boolean = true,
) {
    SortVisualizer(
        sortName = "Quick Sort",
        sortAction = { quickSort(0, COLUMN_COUNT - 1) },
        modifier = modifier,
        applyOwnInsets = applyOwnInsets,
    )
}