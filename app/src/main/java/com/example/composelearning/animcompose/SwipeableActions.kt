package com.example.composelearning

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableDefaults
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

enum class SheetState {
    Collapsed,
    Expanded
}

@Composable
fun DraggableSheet() {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val sheetWidth = screenWidth / 2 // Half screen width
    val handleWidth = 40.dp // The width of the draggable handle

    val density = LocalDensity.current

    val anchors = remember(sheetWidth, handleWidth) {
        DraggableAnchors {
            // These anchors now represent the X position of the *left edge of the entire sheet container*.
            // The sheet container has a total width of `sheetWidth`.

            // Expanded State: The left edge of the sheet container is at `screenWidth - sheetWidth`.
            // The offset is then relative to its *initial* position if it were aligned `CenterEnd` (i.e., its right edge is at `0`).
            // So, to move it left by `sheetWidth`, the offset is `-sheetWidth.toPx()`.
            SheetState.Collapsed at with(density) { (screenWidth- handleWidth).toPx() }

            // Collapsed State: Only the handle (width `handleWidth`) is visible at the screen's right edge.
            // The sheet container's right edge is at the screen's right edge (0 offset from CenterEnd).
            // This means the sheet container's left edge (which is controlled by the offset) is at `0f`.
            SheetState.Expanded at with(density) { (screenWidth - sheetWidth).toPx() }
        }
    }

    val dragState: AnchoredDraggableState<SheetState> =
        rememberSaveable(saver = AnchoredDraggableState.Saver()) {
            AnchoredDraggableState(
                initialValue = SheetState.Collapsed,
                anchors = anchors
            )
        }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(Color.Transparent)
    ) {
        // This Box now acts as the main draggable component, containing both handle and content.
        // Its offset is controlled by the dragState.
        Box(
            modifier = Modifier
                .offset { IntOffset(x = -dragState.requireOffset().roundToInt(), y = 0) }
                .width(sheetWidth) // Total width of sheet including handle
                .fillMaxHeight()
                .align(Alignment.CenterEnd) // Aligns the right edge of this Box to the screen's right edge
                .anchoredDraggable(
                    state = dragState,
                    orientation = Orientation.Horizontal,
                    reverseDirection = true, // Dragging left *decreases* offset (makes it more negative -> expands)
                    // Dragging right *increases* offset (makes it less negative -> collapses)
                    flingBehavior = AnchoredDraggableDefaults.flingBehavior(
                        dragState,
                        positionalThreshold = { distance -> distance * 0.5f },
                    )
                ),
        ) {
            // The Sheet Content (now positioned relative to its parent Box)
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(sheetWidth) // Content takes the remaining width
                    .align(Alignment.CenterEnd) // Content aligns to the right within the parent Box
                    .background(Color.White, shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)),
                shadowElevation = 8.dp
            ) {
                Text(
                    text = "Sheet Content (Half Screen Width)",
                    modifier = Modifier.padding(16.dp)
                )
            }

            // The Draggable Handle (positioned at the start/left of the parent Box)
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.CenterEnd) // Aligns handle to the left within the parent Box
                    .background(Color.Gray.copy(alpha = 0.5f), shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)),
                contentAlignment = Alignment.Center // Centers the icon inside the handle
            ) {
                if (dragState.currentValue == SheetState.Collapsed) {
                    Icon(
                        imageVector = Icons.Filled.ArrowForward,
                        contentDescription = "Expand",
                        tint = Color.White
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Collapse",
                        tint = Color.White
                    )
                }
            }
        }
    }
}




@Composable
fun DraggableSheetRight() {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val sheetContentWidth = screenWidth / 2 // Content width when expanded
    val handleWidth = 40.dp // The width of the draggable handle
    val totalDraggableWidth = sheetContentWidth + handleWidth // Total width of the draggable component

    val density = LocalDensity.current

    val anchors = remember(screenWidth, sheetContentWidth, handleWidth, totalDraggableWidth) {
        DraggableAnchors {

            SheetState.Collapsed at with(density) { (handleWidth/2 ).toPx() }

            SheetState.Expanded at with(density) { (screenWidth/2).toPx() }
        }
    }

    val dragState: AnchoredDraggableState<SheetState> =
        rememberSaveable(saver = AnchoredDraggableState.Saver()) {
            AnchoredDraggableState(
                initialValue = SheetState.Expanded,
                anchors = anchors
            )
        }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(Color.Transparent)
    ) {
        // This Box is the main draggable component, containing both handle and content.
        // Its offset is controlled by the dragState.
        Box(
            modifier = Modifier
                .offset { IntOffset(x = dragState.requireOffset().roundToInt(), y = 0) }
                .width(totalDraggableWidth) // Total width of sheet + handle
                .fillMaxHeight()
                .align(Alignment.CenterEnd) // Aligns the right edge of this Box to the screen's right edge
                .anchoredDraggable(
                    state = dragState,
                    orientation = Orientation.Horizontal,
                    reverseDirection = false, // Dragging left *decreases* offset (moves left -> expands)
                    // Dragging right *increases* offset (moves right -> collapses)
                    flingBehavior = AnchoredDraggableDefaults.flingBehavior(
                        dragState,
                        positionalThreshold = { distance -> distance * 0.5f },
                    )
                ),
        ) {
            // The Draggable Handle (positioned at the start/left of the parent Box)
            Box(
                modifier = Modifier
                    .size(handleWidth)
                    .align(Alignment.CenterStart) // Aligns handle to the left within the parent Box
                    .background(Color.Gray.copy(alpha = 0.5f), shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)), // Rounded on the right for handle
                contentAlignment = Alignment.Center // Centers the icon inside the handle
            ) {
                if (dragState.currentValue == SheetState.Expanded) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack, // Drag left to expand
                        contentDescription = "Expand",
                        tint = Color.White
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.ArrowForward, // Drag right to collapse
                        contentDescription = "Collapse",
                        tint = Color.White
                    )
                }
            }

            // The Sheet Content (now positioned relative to its parent Box)
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(sheetContentWidth) // Content takes the defined width
                    .align(Alignment.CenterEnd) // Content aligns to the right within the parent Box (to the right of the handle)
                    .background(Color.White, shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)), // Rounded on the left for content
                shadowElevation = 8.dp
            ) {
                Text(
                    text = "Sheet Content (Half Screen Width)",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}