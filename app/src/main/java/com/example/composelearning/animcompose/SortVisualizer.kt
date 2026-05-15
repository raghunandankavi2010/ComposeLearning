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
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal const val COLUMN_COUNT = 32
internal const val SWAP_DELAY_MS = 10L
internal const val PEEK_DELAY_MS = 2L
internal const val PLACEMENT_ANIM_MS = 220

internal enum class SortMode { IDLE, OFF_THREAD, MAIN_THREAD }

@Stable
internal class Col(val hue: Float, val color: Color, initialSlot: Float = 0f) {
    val xSlot = Animatable(initialSlot)
}

/**
 * Mutable scratch space passed to a sort algorithm. The algorithm reorders slots by calling
 * [swap]; each swap launches frame-paced placement animations into the visualizer's Main scope.
 */
internal class SortContext(
    val columns: List<Col>,
    val slotToColumn: IntArray,
    private val animScope: CoroutineScope,
) {
    var swaps: Int = 0
    var comparisons: Int = 0

    suspend fun swap(slotA: Int, slotB: Int) {
        if (slotA == slotB) return
        val colAIdx = slotToColumn[slotA]
        val colBIdx = slotToColumn[slotB]
        slotToColumn[slotA] = colBIdx
        slotToColumn[slotB] = colAIdx
        swaps++
        val colA = columns[colAIdx]
        val colB = columns[colBIdx]
        animScope.launch {
            colA.xSlot.animateTo(
                targetValue = slotB.toFloat(),
                animationSpec = tween(PLACEMENT_ANIM_MS, easing = FastOutSlowInEasing),
            )
        }
        animScope.launch {
            colB.xSlot.animateTo(
                targetValue = slotA.toFloat(),
                animationSpec = tween(PLACEMENT_ANIM_MS, easing = FastOutSlowInEasing),
            )
        }
        delay(SWAP_DELAY_MS)
    }

    suspend fun peek() {
        delay(PEEK_DELAY_MS)
    }
}

internal typealias SortAction = suspend SortContext.() -> Unit

/**
 * Shared scaffold for sort-animation screens. Owns the column data, the shuffle-and-animate
 * loop, off-thread vs main-thread benchmarking, and the overlay UI. The algorithm itself is
 * supplied as a [SortAction] — see [BubbleSortRainbow] and [QuickSortRainbow].
 *
 * @param applyOwnInsets when true, the visualizer pads itself against system bars (standalone
 * usage). Set to false when hosted inside a container that already applies status-bar padding
 * (e.g. [SortAnimationScreen]).
 */
@Composable
internal fun SortVisualizer(
    sortName: String,
    sortAction: SortAction,
    modifier: Modifier = Modifier,
    applyOwnInsets: Boolean = true,
) {
    var sortMode by remember { mutableStateOf(SortMode.IDLE) }
    var generation by remember { mutableIntStateOf(0) }
    var lastDurationOffThreadMs by remember { mutableLongStateOf(0L) }
    var lastDurationMainMs by remember { mutableLongStateOf(0L) }
    var lastSwaps by remember { mutableIntStateOf(0) }
    var lastComparisons by remember { mutableIntStateOf(0) }
    var status by remember { mutableStateOf("Tap a button to benchmark") }

    // Pre-shuffle so the first frame already shows columns spread across the screen instead of
    // 32 columns stacked at slot 0 (black-screen-with-a-red-sliver bug).
    val columns = remember {
        val initial = (0 until COLUMN_COUNT).shuffled()
        val columnToSlot = IntArray(COLUMN_COUNT)
        initial.forEachIndexed { slot, colIdx -> columnToSlot[colIdx] = slot }
        List(COLUMN_COUNT) { i ->
            val hue = i * 360f / COLUMN_COUNT
            Col(hue = hue, color = Color.hsl(hue, 1f, 0.5f), initialSlot = columnToSlot[i].toFloat())
        }
    }

    LaunchedEffect(sortMode, generation) {
        if (sortMode == SortMode.IDLE) return@LaunchedEffect

        // coroutineScope blocks until all swap-animation children complete, so the LaunchedEffect
        // doesn't return (and cancel them) before the columns settle. Without this, the final
        // swaps' animateTo coroutines get cancelled mid-flight when we set sortMode = IDLE, and
        // a few columns end up frozen at intermediate positions — visible as black gaps and
        // mis-placed stripes after the sort completes.
        coroutineScope {
            val animScope: CoroutineScope = this

            val slotToColumn = (0 until COLUMN_COUNT).shuffled().toIntArray()
            val columnToSlot = IntArray(COLUMN_COUNT)
            for (slot in 0 until COLUMN_COUNT) columnToSlot[slotToColumn[slot]] = slot
            for (colIdx in 0 until COLUMN_COUNT) {
                columns[colIdx].xSlot.snapTo(columnToSlot[colIdx].toFloat())
            }
            delay(150)

            status = if (sortMode == SortMode.OFF_THREAD) "Running off-thread…" else "Running on Main…"

            val context = SortContext(columns, slotToColumn, animScope)

            val startTime = System.nanoTime()
            if (sortMode == SortMode.OFF_THREAD) {
                withContext(Dispatchers.Default) { context.sortAction() }
            } else {
                context.sortAction()
            }
            val durationMs = (System.nanoTime() - startTime) / 1_000_000

            if (sortMode == SortMode.OFF_THREAD) lastDurationOffThreadMs = durationMs
            else lastDurationMainMs = durationMs
            lastSwaps = context.swaps
            lastComparisons = context.comparisons
            status = "Completed in ${durationMs}ms"
        }
        sortMode = SortMode.IDLE
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        Canvas(
            modifier = if (applyOwnInsets) {
                Modifier.fillMaxSize().systemBarsPadding()
            } else {
                Modifier.fillMaxSize()
            },
        ) {
            val slotWidth = size.width / COLUMN_COUNT
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
            sortName = sortName,
            status = status,
            sortMode = sortMode,
            lastDurationOffThreadMs = lastDurationOffThreadMs,
            lastDurationMainMs = lastDurationMainMs,
            swaps = lastSwaps,
            comparisons = lastComparisons,
            applyTopInset = applyOwnInsets,
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
    sortName: String,
    status: String,
    sortMode: SortMode,
    lastDurationOffThreadMs: Long,
    lastDurationMainMs: Long,
    swaps: Int,
    comparisons: Int,
    applyTopInset: Boolean,
    onRunOffThread: () -> Unit,
    onRunMain: () -> Unit,
) {
    val enabled = sortMode == SortMode.IDLE
    val topInsetModifier = if (applyTopInset) Modifier.statusBarsPadding() else Modifier
    Column(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .then(topInsetModifier)
            .padding(16.dp)
            .background(Color.Black.copy(alpha = 0.8f))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = sortName,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = status,
            color = Color.White.copy(alpha = 0.8f),
            style = MaterialTheme.typography.bodySmall,
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