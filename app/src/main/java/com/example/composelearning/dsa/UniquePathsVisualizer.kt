package com.example.composelearning.dsa

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/* ---------------------------------- Colors ---------------------------------- */
private val OpenColor = Color(0xFFECEFF1)
private val ObstacleColor = Color(0xFF37474F)
private val PathColor = Color(0xFF42A5F5)   // cell currently on the recursion stack
private val HeadColor = Color(0xFFFF7043)   // the cell DFS is currently visiting
private val FoundColor = Color(0xFF66BB6A)  // flashes when a full path is completed
private val StartColor = Color(0xFFB3E5FC)
private val EndColor = Color(0xFFFFCCBC)

/* ------------------------------- DFS step model ------------------------------ */
private sealed interface DfsStep {
    data class Enter(val r: Int, val c: Int) : DfsStep   // push cell onto path (mark visited)
    data class Leave(val r: Int, val c: Int) : DfsStep   // backtrack (unmark)
    data object PathFound : DfsStep                       // reached target: count++
}

/**
 * Re-runs the exact algorithm from `programs.neetcode.uniquePaths`, but instead of
 * just counting it records every Enter / Leave / PathFound event so the UI can
 * replay the depth-first search step by step.
 */
private fun buildDfsSteps(obstacles: List<List<Boolean>>): List<DfsStep> {
    val rows = obstacles.size
    val cols = if (rows > 0) obstacles[0].size else 0
    val steps = mutableListOf<DfsStep>()
    if (rows == 0 || cols == 0) return steps

    // 0 = open, 1 = obstacle, 2 = visited (on the current path)
    val g = Array(rows) { r -> IntArray(cols) { c -> if (obstacles[r][c]) 1 else 0 } }
    if (g[0][0] == 1 || g[rows - 1][cols - 1] == 1) return steps

    fun dfs(r: Int, c: Int) {
        if (r !in 0 until rows || c !in 0 until cols || g[r][c] != 0) return
        if (r == rows - 1 && c == cols - 1) {
            steps += DfsStep.Enter(r, c)
            steps += DfsStep.PathFound
            steps += DfsStep.Leave(r, c)
            return
        }
        g[r][c] = 2
        steps += DfsStep.Enter(r, c)
        dfs(r + 1, c) // down
        dfs(r - 1, c) // up
        dfs(r, c + 1) // right
        dfs(r, c - 1) // left
        g[r][c] = 0
        steps += DfsStep.Leave(r, c)
    }
    dfs(0, 0)
    return steps
}

/* --------------------------------- Presets ---------------------------------- */
private fun exampleGrid(): List<List<Boolean>> = listOf(
    listOf(false, false, false, false),
    listOf(true, true, false, false),
    listOf(false, false, false, true),
    listOf(false, true, false, false),
)

private fun openGrid(): List<List<Boolean>> = List(3) { List(3) { false } }

/* -------------------------------- Visualizer -------------------------------- */
@Composable
fun UniquePathsVisualizer(modifier: Modifier = Modifier) {
    var grid by remember { mutableStateOf(exampleGrid()) }
    val rows = grid.size
    val cols = grid[0].size

    // Recomputed whenever the grid changes; all playback state is keyed to it so
    // editing the grid resets the animation cleanly.
    val steps = remember(grid) { buildDfsSteps(grid) }
    val totalPaths = remember(steps) { steps.count { it is DfsStep.PathFound } }

    val pathStack = remember(steps) { mutableStateListOf<Int>() }
    var stepIndex by remember(steps) { mutableIntStateOf(0) }
    var pathsFound by remember(steps) { mutableIntStateOf(0) }
    var justFound by remember(steps) { mutableStateOf(false) }
    var isPlaying by remember(steps) { mutableStateOf(false) }
    var speedMs by remember { mutableFloatStateOf(280f) }

    fun idx(r: Int, c: Int) = r * cols + c

    fun applyStep(step: DfsStep) {
        when (step) {
            is DfsStep.Enter -> {
                justFound = false
                pathStack.add(idx(step.r, step.c))
            }
            is DfsStep.Leave -> {
                justFound = false
                if (pathStack.isNotEmpty()) pathStack.removeAt(pathStack.lastIndex)
            }
            DfsStep.PathFound -> {
                justFound = true
                pathsFound++
            }
        }
    }

    fun reset() {
        isPlaying = false
        stepIndex = 0
        pathStack.clear()
        pathsFound = 0
        justFound = false
    }

    fun stepForward() {
        if (stepIndex < steps.size) {
            applyStep(steps[stepIndex])
            stepIndex++
        }
    }

    // Auto-play loop. Reads speedMs each iteration so the slider takes effect live.
    LaunchedEffect(isPlaying, steps) {
        if (!isPlaying) return@LaunchedEffect
        while (isPlaying && stepIndex < steps.size) {
            applyStep(steps[stepIndex])
            stepIndex++
            delay(speedMs.toLong())
        }
        if (stepIndex >= steps.size) isPlaying = false
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "Unique Paths — DFS + Backtracking",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            "Paths found: $pathsFound / $totalPaths      •      Step ${stepIndex.coerceAtMost(steps.size)}/${steps.size}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // ---- Grid ----
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val cellSize = maxWidth / cols
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                for (r in 0 until rows) {
                    Row {
                        for (c in 0 until cols) {
                            val cellIdx = idx(r, c)
                            val isObstacle = grid[r][c]
                            val onPath = pathStack.contains(cellIdx)
                            val isHead = pathStack.lastOrNull() == cellIdx
                            val isStart = r == 0 && c == 0
                            val isEnd = r == rows - 1 && c == cols - 1

                            val target = when {
                                isObstacle -> ObstacleColor
                                justFound && onPath -> FoundColor
                                isHead -> HeadColor
                                onPath -> PathColor
                                isStart -> StartColor
                                isEnd -> EndColor
                                else -> OpenColor
                            }
                            val color by animateColorAsState(target, tween(180), label = "cell")
                            val scale by animateFloatAsState(
                                if (isHead) 1f else 0.94f, tween(180), label = "scale",
                            )

                            Box(
                                modifier = Modifier
                                    .size(cellSize)
                                    .padding(2.dp)
                                    .scale(scale)
                                    .background(color, RoundedCornerShape(8.dp))
                                    .border(
                                        width = if (isHead) 2.dp else 0.dp,
                                        color = if (isHead) Color(0xFFBF360C) else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp),
                                    )
                                    .clickable(enabled = !isPlaying && !isStart && !isEnd) {
                                        grid = grid.mapIndexed { rr, row ->
                                            row.mapIndexed { cc, v -> if (rr == r && cc == c) !v else v }
                                        }
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                val label = when {
                                    isStart -> "S"
                                    isEnd -> "E"
                                    isObstacle -> "✕"
                                    else -> ""
                                }
                                if (label.isNotEmpty()) {
                                    Text(
                                        label,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = (cellSize.value * 0.32f).sp,
                                        color = if (isObstacle) Color.White else Color(0xFF455A64),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ---- Legend ----
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        ) {
            LegendChip(HeadColor, "Visiting")
            LegendChip(PathColor, "On path")
            LegendChip(FoundColor, "Path!")
            LegendChip(ObstacleColor, "Wall")
        }

        Text(
            "Tap any cell (while paused) to toggle a wall.",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        // ---- Controls ----
        Card(elevation = CardDefaults.cardElevation(2.dp), modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            if (stepIndex >= steps.size) reset()
                            isPlaying = !isPlaying
                        },
                        modifier = Modifier.weight(1f),
                    ) { Text(if (isPlaying) "Pause" else "Play") }

                    OutlinedButton(
                        onClick = { isPlaying = false; stepForward() },
                        enabled = !isPlaying && stepIndex < steps.size,
                        modifier = Modifier.weight(1f),
                    ) { Text("Step") }

                    OutlinedButton(
                        onClick = { reset() },
                        modifier = Modifier.weight(1f),
                    ) { Text("Reset") }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Speed", modifier = Modifier.width(56.dp), style = MaterialTheme.typography.labelLarge)
                    // Slider is inverted so dragging right = faster (smaller delay).
                    Slider(
                        value = 620f - speedMs,
                        onValueChange = { speedMs = 620f - it },
                        valueRange = 20f..600f,
                        modifier = Modifier.weight(1f),
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { grid = exampleGrid(); reset() },
                        modifier = Modifier.weight(1f),
                    ) { Text("Example → 2") }
                    OutlinedButton(
                        onClick = { grid = openGrid(); reset() },
                        modifier = Modifier.weight(1f),
                    ) { Text("Open 3×3 → 12") }
                }
            }
        }
    }
}

@Composable
private fun LegendChip(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .background(color, RoundedCornerShape(3.dp)),
        )
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, heightDp = 800)
@Composable
private fun UniquePathsVisualizerPreview() {
    UniquePathsVisualizer()
}
