package com.example.composelearning.animcompose

import android.app.Activity
import android.graphics.Rect
import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private const val DRAG_THRESHOLD = 0.5f

/**
 * A simplified side panel layout with drag gestures to change its width.
 *
 * Features:
 * - Smooth drag gesture handling to adjust panel width.
 * - Panel expands from the right (or left based on arrangement).
 * - No complex animations (rainbow, arc, etc.).
 *
 * @param modifier Modifier to be applied to the layout. IMPORTANT: If you set a fixed width (e.g., .width(300.dp)),
 *                  that width will be used as the maximum expansion width for the panel.
 * @param state State holder for managing the side panel state
 * @param arrangement Placement of the side panel (Start or End)
 * @param containerColor Background color of the side panel container
 * @param dragHandleContent Composable content for the drag handle
 * @param content Composable content inside the side panel (currently not used for drawing as per request)
 */
/**
 * A simplified side panel layout with drag gestures to change its width.
 *
 * Features:
 * - Smooth drag gesture handling to adjust panel width.
 * - Panel expands from the right (or left based on arrangement).
 * - No complex animations (rainbow, arc, etc.).
 *
 * @param modifier Modifier to be applied to the layout
 * @param state State holder for managing the side panel state
 * @param maxPanelWidth The maximum width the side panel can expand to. This is crucial for its behavior.
 * @param arrangement Placement of the side panel (Start or End)
 * @param containerColor Background color of the side panel container
 * @param dragHandleContent Composable content for the drag handle
 * @param content Composable content inside the side panel
 */
@Composable
fun SidePanelLayout(
    modifier: Modifier = Modifier,
    state: SidePanelStateHolder,
    maxPanelWidth: Dp, // <--- NEW PARAMETER
    arrangement: SidePanelArrangement = SidePanelArrangement.End,
    containerColor: Color = Color.Green,
    dragHandleContent: @Composable (isExpanded: Boolean, progress: Float) -> Unit,
    content: @Composable (Float) -> Unit
) {
    val windowInfo = LocalWindowInfo.current
    val screenWidthPx = windowInfo.containerSize.width.toFloat()
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    // Convert maxPanelWidth Dp to Px once
    val maxPanelWidthPx = remember(maxPanelWidth, density) {
        with(density) { maxPanelWidth.toPx() }
    }

    // Set the max sheet width in the state holder as soon as it's available
    LaunchedEffect(maxPanelWidthPx) {
        if (maxPanelWidthPx > 0f) {
            state.setMaxSheetWidth(maxPanelWidthPx)
        }
    }

    Layout(
        content = {
            // Measurable 0: Main sheet content area.
            // Apply the actual maxPanelWidth here, not `modifier.width(300.dp)` from the call site directly,
            // as we want this Box to *be* the max width when fully expanded.
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(maxPanelWidth) // <--- APPLY maxPanelWidth HERE
                    .background(color = containerColor)
            ) {
                content(state.progress)
            }

            // Measurable 1: Drag Handle
            DragHandle(
                state = state,
                arrangement = arrangement,
                dragHandleContent = dragHandleContent,
                scope = scope
            )
        }
    ) { measurables, constraints ->
        require(measurables.size == 2) { "SidePanelLayout expects exactly 2 measurables (sheet and drag handle)" }

        // Measure the main sheet content. Its maxWidth is now driven by animatedWidth.
        val sheetPlaceable = measurables[0].measure(
            constraints.copy(
                minWidth = 0,
                // The actual width this sheet takes is controlled by the animatedWidth
                maxWidth = state.animatedWidth.toInt().coerceAtLeast(0)
            )
        )

        val dragHandlePlaceable = measurables[1].measure(
            constraints.copy(minWidth = 0)
        )

        // No more `setMaxSheetWidth` here, it's handled by LaunchedEffect above.
        // We ensure state.maxSheetWidthPx is set correctly before this layout pass really matters.

        layout(constraints.maxWidth, constraints.maxHeight) {
            val positions = calculatePositions(
                arrangement = arrangement,
                currentWidth = state.animatedWidth.toInt(),
                screenWidth = screenWidthPx.toInt(),
                sheetWidth = sheetPlaceable.width, // This is the width due to current animation
                dragHandleWidth = dragHandlePlaceable.width,
                maxHeight = constraints.maxHeight,
                sheetHeight = sheetPlaceable.height,
                dragHandleHeight = dragHandlePlaceable.height
            )

            sheetPlaceable.place(positions.sheetX, positions.sheetY)
            dragHandlePlaceable.place(positions.dragHandleX, positions.dragHandleY)
        }
    }
}

@Composable
private fun DragHandle(
    state: SidePanelStateHolder,
    arrangement: SidePanelArrangement,
    dragHandleContent: @Composable (Boolean, Float) -> Unit,
    scope: CoroutineScope
) {
    var dragHandleRect by remember { mutableStateOf(Rect(0, 0, 0, 0)) }
    ExcludeDragHandleRectFromGesture(dragHandleRect)

    Box(
        modifier = Modifier
            .onGloballyPositioned { layoutCoordinates ->
                dragHandleRect = calculateDragHandleRect(layoutCoordinates)
            }
            .pointerInput(state) {
                detectDragGestures(
                    onDragEnd = {
                        scope.launch { state.handleDragEnd() }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        scope.launch {
                            val dragDelta = calculateDragDelta(arrangement, dragAmount.x)
                            state.handleDrag(dragDelta)
                        }
                    }
                )
            }
    ) {
        dragHandleContent(
            state.currentState == SidePanelState.Expanded,
            state.progress
        )
    }
}


enum class SidePanelState { Expanded, Collapsed }
enum class SidePanelArrangement { Start, End }

@Stable
class SidePanelStateHolder internal constructor(
    initialState: SidePanelState,
    private val animationSpec: FiniteAnimationSpec<Float>
) {
    private val _state = mutableStateOf(initialState)
    val currentState: SidePanelState by _state

    private val currentSheetWidth = Animatable(0f)
    private var maxSheetWidthPx by mutableFloatStateOf(0f)
    private var isInitialized by mutableStateOf(false)

    val animatedWidth: Float get() = currentSheetWidth.value
    val progress: Float
        get() = if (maxSheetWidthPx > 0) {
            (currentSheetWidth.value / maxSheetWidthPx).coerceIn(0f, 1f)
        } else {
            0f
        }

    internal suspend fun setMaxSheetWidth(width: Float) {
        if (maxSheetWidthPx == width || width <= 0f) return // Avoid unnecessary updates or zero/negative widths

        maxSheetWidthPx = width
        if (!isInitialized) { // Only initialize once
            isInitialized = true
            val targetWidth = if (currentState == SidePanelState.Expanded) width else 0f
            currentSheetWidth.updateBounds(0f, width)
            currentSheetWidth.snapTo(targetWidth)
        } else {
            // If max width changes *after* initialization (e.g., orientation change),
            // update bounds but don't snap unless explicitly needed.
            currentSheetWidth.updateBounds(0f, width)
        }
    }

    suspend fun animateTo(state: SidePanelState) {
        if (maxSheetWidthPx <= 0f) return // Cannot animate if max width isn't known

        val targetWidth = if (state == SidePanelState.Expanded) maxSheetWidthPx else 0f
        currentSheetWidth.animateTo(targetWidth, animationSpec)
        _state.value = state
    }

    suspend fun expand() = animateTo(SidePanelState.Expanded)
    suspend fun collapse() = animateTo(SidePanelState.Collapsed)

    suspend fun toggle() {
        val newState = if (currentState == SidePanelState.Expanded) {
            SidePanelState.Collapsed
        } else {
            SidePanelState.Expanded
        }
        animateTo(newState)
    }

    internal suspend fun handleDrag(dragDelta: Float) {
        if (maxSheetWidthPx <= 0f) return
        val newWidth = (currentSheetWidth.value + dragDelta).coerceIn(0f, maxSheetWidthPx)
        currentSheetWidth.snapTo(newWidth)
    }

    internal suspend fun handleDragEnd() {
        if (maxSheetWidthPx <= 0f) return
        val expandThreshold = maxSheetWidthPx * DRAG_THRESHOLD
        val newState = if (currentSheetWidth.value < expandThreshold) {
            SidePanelState.Collapsed
        } else {
            SidePanelState.Expanded
        }
        animateTo(newState)
    }
}

@Composable
fun rememberSidePanelState(
    initialState: SidePanelState = SidePanelState.Collapsed,
    animationSpec: FiniteAnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessLow
    )
): SidePanelStateHolder {
    return remember(initialState, animationSpec) {
        SidePanelStateHolder(initialState, animationSpec)
    }
}

private fun calculateDragDelta(arrangement: SidePanelArrangement, dragX: Float): Float {
    return if (arrangement == SidePanelArrangement.End) -dragX else dragX
}

private fun calculateDragHandleRect(layoutCoordinates: androidx.compose.ui.layout.LayoutCoordinates): Rect {
    val position = layoutCoordinates.localToWindow(Offset.Zero)
    val size = layoutCoordinates.size
    return Rect(
        position.x.toInt(),
        position.y.toInt(),
        (position.x + size.width).toInt(),
        (position.y + size.height).toInt()
    )
}

private data class LayoutPositions(
    val sheetX: Int,
    val sheetY: Int,
    val dragHandleX: Int,
    val dragHandleY: Int
)

private fun calculatePositions(
    arrangement: SidePanelArrangement,
    currentWidth: Int,
    screenWidth: Int,
    sheetWidth: Int, // The current animated width of the sheet
    dragHandleWidth: Int,
    maxHeight: Int,
    sheetHeight: Int,
    dragHandleHeight: Int
): LayoutPositions {
    val isEnd = arrangement == SidePanelArrangement.End

    val sheetX = if (isEnd) {
        screenWidth - currentWidth
    } else {
        0
    }

    val dragHandleX = if (isEnd) {
        screenWidth - currentWidth - (dragHandleWidth / 2)
    } else {
        currentWidth - (dragHandleWidth / 2)
    }

    val sheetY = (maxHeight - sheetHeight) / 2
    val dragHandleY = (maxHeight - dragHandleHeight) / 2

    return LayoutPositions(sheetX, sheetY, dragHandleX, dragHandleY)
}

/**
 * Exclude the drag handle area from system gesture detection to prevent conflicts.
 */
@Composable
private fun ExcludeDragHandleRectFromGesture(dragHandleRect: Rect) {
    val context = LocalContext.current

    LaunchedEffect(dragHandleRect) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val activity = context as? Activity ?: return@LaunchedEffect
            val decorView = activity.window?.decorView ?: return@LaunchedEffect
            decorView.systemGestureExclusionRects = listOf(dragHandleRect)
        }
    }
}