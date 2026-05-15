package com.example.composelearning.animcompose

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val COLUMN_COUNT = 32
private const val SWAP_DELAY_MS = 10L
private const val PEEK_DELAY_MS = 2L
private const val PLACEMENT_ANIM_MS = 220

private enum class SortMode { IDLE, OFF_THREAD, MAIN_THREAD }

/**
 * Each column is created once at composition and **never reordered** in the list. Sorting only
 * mutates a local `IntArray slotToColumn` (plain heap memory, no Compose state) and animates each
 * column's `xSlot` Animatable to its new slot index. The Canvas reads `xSlot.value * slotWidth`
 * to position each rectangle every frame.
 */
@Stable
private class Col(val hue: Float, val color: Color) {
    val xSlot = Animatable(0f)
}

/**
 * Performance-tuned bubble-sort rainbow.
 *
 *  - No `LazyRow`, no `animateItem`, no `SnapshotStateList`. One Canvas, 32 `drawRect` calls/frame.
 *  - "comparing" highlight removed from the hot loop (was invalidating all 32 items per comparison).
 *  - Counters batched: incremented in local Ints, written to Compose state once at the end.
 *  - Sort loop runs on Dispatchers.Default for delay() precision (Main coalesces sub-frame delays
 *    to ~16 ms Choreographer ticks).
 *  - Each swap launches two `animateTo` calls into the LaunchedEffect scope (Main + Compose's
 *    MonotonicFrameClock), so animations stay frame-paced even though the sort is off-Main.
 *  - withContext(Default) only waits for the sort loop itself; animations live in the outer scope
 *    and keep running after the loop ends, so the timing measures pure sort cost.
 */
@Composable
fun BubbleSortRainbow(modifier: Modifier = Modifier) {
    var sortMode by remember { mutableStateOf(SortMode.IDLE) }
    var generation by remember { mutableIntStateOf(0) }
    var lastDurationOffThreadMs by remember { mutableLongStateOf(0L) }
    var lastDurationMainMs by remember { mutableLongStateOf(0L) }
    var lastSwaps by remember { mutableIntStateOf(0) }
    var lastComparisons by remember { mutableIntStateOf(0) }
    var status by remember { mutableStateOf("Tap a button to benchmark") }

    // Stable column data — list order never changes; only each Col's xSlot animates.
    // Color.hsl is evaluated once per column at first composition.
    val columns = remember {
        List(COLUMN_COUNT) { i ->
            val hue = i * 360f / COLUMN_COUNT
            Col(hue = hue, color = Color.hsl(hue, 1f, 0.5f))
        }
    }

    LaunchedEffect(sortMode, generation) {
        if (sortMode == SortMode.IDLE) return@LaunchedEffect

        // Outer scope = LaunchedEffect's scope (Main, carries Compose's MonotonicFrameClock).
        // We launch animations into this scope so they're frame-paced; the sort loop itself
        // runs in withContext(Default) below.
        val animScope: CoroutineScope = this

        // Fresh shuffle: pick a slot per column and snapTo, no animation yet.
        val slotToColumn = (0 until COLUMN_COUNT).shuffled().toIntArray()
        val columnToSlot = IntArray(COLUMN_COUNT)
        for (slot in 0 until COLUMN_COUNT) columnToSlot[slotToColumn[slot]] = slot
        for (colIdx in 0 until COLUMN_COUNT) {
            columns[colIdx].xSlot.snapTo(columnToSlot[colIdx].toFloat())
        }
        delay(150)

        status = if (sortMode == SortMode.OFF_THREAD) "Running off-thread…" else "Running on Main…"

        var swaps = 0
        var comparisons = 0

        val sortAction: suspend () -> Unit = {
            for (i in 0 until COLUMN_COUNT - 1) {
                for (j in 0 until COLUMN_COUNT - i - 1) {
                    comparisons++
                    val colAIdx = slotToColumn[j]
                    val colBIdx = slotToColumn[j + 1]
                    val colA = columns[colAIdx]
                    val colB = columns[colBIdx]
                    if (colA.hue > colB.hue) {
                        swaps++
                        slotToColumn[j] = colBIdx
                        slotToColumn[j + 1] = colAIdx
                        val newSlotA = (j + 1).toFloat()
                        val newSlotB = j.toFloat()
                        // Animations are launched into animScope (Main) so the sort itself
                        // never blocks on a frame tick. They overlap to give the cascading
                        // rainbow-flow effect from the original CSS demo.
                        animScope.launch {
                            colA.xSlot.animateTo(
                                targetValue = newSlotA,
                                animationSpec = tween(PLACEMENT_ANIM_MS, easing = FastOutSlowInEasing),
                            )
                        }
                        animScope.launch {
                            colB.xSlot.animateTo(
                                targetValue = newSlotB,
                                animationSpec = tween(PLACEMENT_ANIM_MS, easing = FastOutSlowInEasing),
                            )
                        }
                        delay(SWAP_DELAY_MS)
                    } else {
                        delay(PEEK_DELAY_MS)
                    }
                }
            }
        }

        val startTime = System.nanoTime()
        if (sortMode == SortMode.OFF_THREAD) {
            withContext(Dispatchers.Default) { sortAction() }
        } else {
            sortAction()
        }
        val durationMs = (System.nanoTime() - startTime) / 1_000_000

        if (sortMode == SortMode.OFF_THREAD) lastDurationOffThreadMs = durationMs
        else lastDurationMainMs = durationMs
        lastSwaps = swaps
        lastComparisons = comparisons
        status = "Completed in ${durationMs}ms"
        sortMode = SortMode.IDLE
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        // Single Canvas, one draw pass per frame for all 32 columns.
        // Reading every xSlot.value here means the drawBehind invalidates per frame any
        // Animatable changes — but it's still one draw, not 32 recompositions.
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
        ) {
            val slotWidth = size.width / COLUMN_COUNT
            // Add a hairline overlap to avoid sub-pixel gaps between adjacent columns.
            val rectWidth = slotWidth + 0.5f
            for (col in columns) {
                drawRect(
                    color = col.color,
                    topLeft = Offset(col.xSlot.value * slotWidth, 0f),
                    size = Size(rectWidth, size.height),
                )
            }
        }

        BenchmarkOverlay(
            status = status,
            sortMode = sortMode,
            lastDurationOffThreadMs = lastDurationOffThreadMs,
            lastDurationMainMs = lastDurationMainMs,
            swaps = lastSwaps,
            comparisons = lastComparisons,
            onRunOffThread = {
                generation++
                sortMode = SortMode.OFF_THREAD
            },
            onRunMain = {
                generation++
                sortMode = SortMode.MAIN_THREAD
            },
        )
    }
}

@Composable
private fun BoxScope.BenchmarkOverlay(
    status: String,
    sortMode: SortMode,
    lastDurationOffThreadMs: Long,
    lastDurationMainMs: Long,
    swaps: Int,
    comparisons: Int,
    onRunOffThread: () -> Unit,
    onRunMain: () -> Unit,
) {
    val enabled = sortMode == SortMode.IDLE
    Column(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .statusBarsPadding()
            .padding(16.dp)
            .background(Color.Black.copy(alpha = 0.8f))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = status,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Button(
                onClick = onRunOffThread,
                enabled = enabled,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
            ) { Text("Off-Thread") }
            Button(
                onClick = onRunMain,
                enabled = enabled,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
            ) { Text("Main Thread") }
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            ResultRow("Dispatchers.Default:", lastDurationOffThreadMs)
            ResultRow("Main Thread:", lastDurationMainMs)
            Text(
                text = "Swaps: $swaps | Comparisons: $comparisons",
                color = Color.White.copy(alpha = 0.6f),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun ResultRow(label: String, duration: Long) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = Color.LightGray, style = MaterialTheme.typography.labelMedium)
        Text(
            text = if (duration > 0) "${duration}ms" else "--",
            color = if (duration > 0) Color.Green else Color.Gray,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}