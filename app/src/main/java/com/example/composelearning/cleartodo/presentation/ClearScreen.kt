package com.example.composelearning.cleartodo.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.composelearning.cleartodo.domain.model.TaskItem
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val TaskHeight = 64.dp
private val ColorTop = Color(0xFFC52B27)
private val ColorBottom = Color(0xFFE1B044)
private val CreateGreen = Color(0xFF2E9E5B)
private const val COMMIT_THRESHOLD = 0.6f

@Composable
fun ClearScreen(
    viewModel: ClearViewModel = viewModel(factory = ClearViewModel.Factory()),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier.fillMaxSize().background(Color.Black).systemBarsPadding(),
    ) {
        Text(
            "Clear",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Light,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        )
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }
        } else {
            ClearList(
                tasks = state.tasks,
                onCreateAt = { viewModel.onIntent(ClearIntent.CreateTaskAt(it)) },
            )
        }
    }
}

@Composable
private fun ClearList(tasks: List<TaskItem>, onCreateAt: (Int) -> Unit) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val taskPx = with(density) { TaskHeight.toPx() }

    // How far the gap is opened (0..1) and which boundary it opens at.
    val open = remember { Animatable(0f) }
    var focal by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .pointerInput(tasks.size) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    var raw = 1f
                    var started = false
                    do {
                        val event = awaitPointerEvent()
                        val pressed = event.changes.count { it.pressed }
                        if (pressed >= 2) {
                            if (!started) {
                                val c = event.calculateCentroid(useCurrent = true)
                                focal = (c.y / taskPx).roundToInt().coerceIn(0, tasks.size)
                                started = true
                            }
                            raw *= event.calculateZoom()
                            val o = (raw - 1f).coerceIn(0f, 1f)
                            scope.launch { open.snapTo(o) }
                            event.changes.forEach { it.consume() }
                        }
                    } while (event.changes.any { it.pressed })

                    val commit = open.value >= COMMIT_THRESHOLD
                    scope.launch {
                        if (commit) onCreateAt(focal)
                        open.animateTo(0f, tween(220))
                    }
                }
            },
    ) {
        val openValue = open.value
        val halfPx = openValue * taskPx / 2f

        // Tasks slide apart around the focal boundary.
        tasks.forEachIndexed { index, task ->
            val shift = if (index < focal) -halfPx else halfPx
            val y = index * taskPx + shift
            TaskRow(
                task = task,
                color = lerp(ColorTop, ColorBottom, if (tasks.size <= 1) 0f else index / (tasks.size - 1f)),
                modifier = Modifier.offset { IntOffset(0, y.roundToInt()) },
            )
        }

        // The folding "create" row that unfolds in the opening gap.
        if (openValue > 0.001f) {
            CreateRow(
                boundaryYpx = focal * taskPx,
                gapPx = openValue * taskPx,
                unfold = openValue,
                density = density.density,
            )
        }
    }
}

@Composable
private fun TaskRow(task: TaskItem, color: Color, modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(TaskHeight)
            .background(color)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(task.text, color = Color.White, fontSize = 17.sp)
    }
}

/**
 * Two green faces folding open about the shared boundary. At `unfold = 1` they're flat
 * (a full task-height row); at `unfold → 0` each face rotates to ±90° (edge-on, invisible).
 * The label fades in with `unfold`.
 */
@Composable
private fun CreateRow(boundaryYpx: Float, gapPx: Float, unfold: Float, density: Float) {
    val d = LocalDensity.current
    val gapDp = with(d) { gapPx.toDp() }
    val halfDp = gapDp / 2
    val topY = boundaryYpx - gapPx / 2f

    Box(
        modifier = Modifier
            .offset { IntOffset(0, topY.roundToInt()) }
            .fillMaxWidth()
            .height(gapDp),
    ) {
        // Top face: pivots about its bottom edge (the crease).
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(halfDp)
                .graphicsLayer {
                    transformOrigin = TransformOrigin(0.5f, 1f)
                    rotationX = 90f * (1f - unfold)
                    cameraDistance = 16f * density
                }
                .clipToBounds()
                .background(CreateGreen),
        )
        // Bottom face: pivots about its top edge.
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(halfDp)
                .graphicsLayer {
                    transformOrigin = TransformOrigin(0.5f, 0f)
                    rotationX = -90f * (1f - unfold)
                    cameraDistance = 16f * density
                }
                .clipToBounds()
                .background(CreateGreen),
        )
        Text(
            "＋  Create a new Task",
            color = Color.White.copy(alpha = unfold),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}
