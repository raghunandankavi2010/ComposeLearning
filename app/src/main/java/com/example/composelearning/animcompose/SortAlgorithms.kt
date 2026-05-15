package com.example.composelearning.animcompose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Six more sorting algorithms wired into [SortVisualizer]. Each algorithm reorders the visualizer's
 * slot array via the same [SortContext.swap]/[SortContext.peek] API used by bubble & quick sort,
 * so every visualization runs on the same 32 hue columns with the same benchmark overlay.
 */

// region Insertion Sort
private suspend fun SortContext.insertionSort() {
    insertionSortRange(0, COLUMN_COUNT)
}

private suspend fun SortContext.insertionSortRange(lo: Int, hi: Int) {
    for (i in lo + 1 until hi) {
        var j = i
        while (j > lo) {
            comparisons++
            if (columns[slotToColumn[j]].hue < columns[slotToColumn[j - 1]].hue) {
                swap(j, j - 1)
                j--
            } else {
                peek()
                break
            }
        }
    }
}

@Composable
fun InsertionSortRainbow(modifier: Modifier = Modifier, applyOwnInsets: Boolean = true) {
    SortVisualizer(
        sortName = "Insertion Sort",
        sortAction = { insertionSort() },
        modifier = modifier,
        applyOwnInsets = applyOwnInsets,
    )
}
// endregion

// region Selection Sort
private suspend fun SortContext.selectionSort() {
    for (i in 0 until COLUMN_COUNT - 1) {
        var minSlot = i
        for (j in i + 1 until COLUMN_COUNT) {
            comparisons++
            if (columns[slotToColumn[j]].hue < columns[slotToColumn[minSlot]].hue) {
                minSlot = j
            }
            peek()
        }
        if (minSlot != i) swap(i, minSlot)
    }
}

@Composable
fun SelectionSortRainbow(modifier: Modifier = Modifier, applyOwnInsets: Boolean = true) {
    SortVisualizer(
        sortName = "Selection Sort",
        sortAction = { selectionSort() },
        modifier = modifier,
        applyOwnInsets = applyOwnInsets,
    )
}
// endregion

// region Shell Sort
private suspend fun SortContext.shellSort() {
    // Knuth-style gap sequence trimmed to fit n=32.
    val gaps = intArrayOf(13, 4, 1)
    for (gap in gaps) {
        if (gap >= COLUMN_COUNT) continue
        for (i in gap until COLUMN_COUNT) {
            var j = i
            while (j >= gap) {
                comparisons++
                if (columns[slotToColumn[j]].hue < columns[slotToColumn[j - gap]].hue) {
                    swap(j, j - gap)
                    j -= gap
                } else {
                    peek()
                    break
                }
            }
        }
    }
}

@Composable
fun ShellSortRainbow(modifier: Modifier = Modifier, applyOwnInsets: Boolean = true) {
    SortVisualizer(
        sortName = "Shell Sort",
        sortAction = { shellSort() },
        modifier = modifier,
        applyOwnInsets = applyOwnInsets,
    )
}
// endregion

// region Merge Sort (bottom-up, swap-only in-place merge)
private suspend fun SortContext.mergeSort() {
    var width = 1
    while (width < COLUMN_COUNT) {
        var lo = 0
        while (lo < COLUMN_COUNT) {
            val mid = (lo + width).coerceAtMost(COLUMN_COUNT)
            val hi = (lo + 2 * width).coerceAtMost(COLUMN_COUNT)
            if (mid < hi) merge(lo, mid, hi)
            lo += 2 * width
        }
        width *= 2
    }
}

/**
 * Merge two sorted runs `[lo, mid)` and `[mid, hi)` using only adjacent swaps — when the
 * right-hand element wins, we shift it left to its destination. O(run²) merges, but the API stays
 * pure-swap and the visualization shows the merge as a series of insertions.
 */
private suspend fun SortContext.merge(lo: Int, mid: Int, hi: Int) {
    var left = lo
    var right = mid
    while (left < right && right < hi) {
        comparisons++
        if (columns[slotToColumn[left]].hue <= columns[slotToColumn[right]].hue) {
            left++
            peek()
        } else {
            var k = right
            while (k > left) {
                swap(k, k - 1)
                k--
            }
            left++
            right++
        }
    }
}

@Composable
fun MergeSortRainbow(modifier: Modifier = Modifier, applyOwnInsets: Boolean = true) {
    SortVisualizer(
        sortName = "Merge Sort",
        sortAction = { mergeSort() },
        modifier = modifier,
        applyOwnInsets = applyOwnInsets,
    )
}
// endregion

// region Heap Sort
private suspend fun SortContext.heapSort() {
    val n = COLUMN_COUNT
    for (i in n / 2 - 1 downTo 0) siftDown(i, n)
    for (end in n - 1 downTo 1) {
        swap(0, end)
        siftDown(0, end)
    }
}

private suspend fun SortContext.siftDown(start: Int, heapSize: Int) {
    var i = start
    while (true) {
        val left = 2 * i + 1
        val right = 2 * i + 2
        var largest = i
        if (left < heapSize) {
            comparisons++
            if (columns[slotToColumn[left]].hue > columns[slotToColumn[largest]].hue) largest = left
        }
        if (right < heapSize) {
            comparisons++
            if (columns[slotToColumn[right]].hue > columns[slotToColumn[largest]].hue) largest = right
        }
        if (largest == i) return
        swap(i, largest)
        i = largest
    }
}

@Composable
fun HeapSortRainbow(modifier: Modifier = Modifier, applyOwnInsets: Boolean = true) {
    SortVisualizer(
        sortName = "Heap Sort",
        sortAction = { heapSort() },
        modifier = modifier,
        applyOwnInsets = applyOwnInsets,
    )
}
// endregion

// region Timsort (simplified)
/**
 * Simplified Timsort: insertion-sort runs of length [TIMSORT_RUN], then merge adjacent runs
 * pairwise. With n=32 we get four runs of eight that merge in two passes — enough to see Timsort's
 * structure (small-run insertion → tournament-style merging) without writing the full galloping
 * variant.
 */
private const val TIMSORT_RUN = 8

private suspend fun SortContext.timSort() {
    var start = 0
    while (start < COLUMN_COUNT) {
        val end = (start + TIMSORT_RUN).coerceAtMost(COLUMN_COUNT)
        insertionSortRange(start, end)
        start = end
    }
    var size = TIMSORT_RUN
    while (size < COLUMN_COUNT) {
        var lo = 0
        while (lo < COLUMN_COUNT) {
            val mid = (lo + size).coerceAtMost(COLUMN_COUNT)
            val hi = (lo + 2 * size).coerceAtMost(COLUMN_COUNT)
            if (mid < hi) merge(lo, mid, hi)
            lo += 2 * size
        }
        size *= 2
    }
}

@Composable
fun TimSortRainbow(modifier: Modifier = Modifier, applyOwnInsets: Boolean = true) {
    SortVisualizer(
        sortName = "Timsort",
        sortAction = { timSort() },
        modifier = modifier,
        applyOwnInsets = applyOwnInsets,
    )
}
// endregion