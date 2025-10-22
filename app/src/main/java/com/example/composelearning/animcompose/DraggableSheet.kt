package com.example.composelearning.animcompose

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableDefaults
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

enum class SheetState {
    Collapsed,
    Expanded
}

@Composable
fun DraggableSheetRight(modifier: Modifier = Modifier) {

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(Color.Transparent)
    ) {

        val screenWidth = maxWidth
        val sheetContentWidth = maxWidth * 0.7f
        val handleWidth = 40.dp
        val density = LocalDensity.current
        val totalDraggableWidth = sheetContentWidth + handleWidth

        val anchors = remember(screenWidth, sheetContentWidth, handleWidth, totalDraggableWidth) {
            DraggableAnchors {

                SheetState.Collapsed at with(density) { (screenWidth - handleWidth).toPx() }

                SheetState.Expanded at with(density) { (screenWidth * 0.3f).toPx() }
            }
        }

        val dragState: AnchoredDraggableState<SheetState> =
            rememberSaveable(saver = AnchoredDraggableState.Saver()) {
                AnchoredDraggableState(
                    initialValue = SheetState.Collapsed,
                    anchors = anchors
                )
            }
        OverlappingBoxes(
            modifier = Modifier
                .offset { IntOffset(x = dragState.requireOffset().roundToInt(), y = 0) }
                .width(totalDraggableWidth)
                .fillMaxHeight()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(sheetContentWidth)
                    .background(
                        Color.White,
                        shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)
                    ),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier,
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Sheet Content",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(handleWidth)
                    .background(Color.Gray.copy(alpha = 0.5f), shape = RoundedCornerShape(8.dp))
                    .anchoredDraggable(
                        state = dragState,
                        orientation = Orientation.Horizontal,
                        reverseDirection = false,
                        flingBehavior = AnchoredDraggableDefaults.flingBehavior(
                            dragState,
                            positionalThreshold = { distance -> distance * 0.5f },
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (dragState.currentValue == SheetState.Collapsed) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Expand",
                        tint = Color.White
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Collapse",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun OverlappingBoxes(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Layout(
        modifier = modifier,
        content = content,
    ) { measurables, constraints ->
        val largeBox = measurables[0]
        val smallBox = measurables[1]
        val looseConstraints = constraints.copy(
            minWidth = 0,
            minHeight = 0,
        )
        val largePlaceable = largeBox.measure(looseConstraints)
        val smallPlaceable = smallBox.measure(looseConstraints)
        layout(
            width = constraints.maxWidth,
            height = constraints.maxHeight
        ) {
            largePlaceable.placeRelative(
                x = 0,
                y = 0,
            )
            smallPlaceable.placeRelative(
                x = -smallPlaceable.width / 2, // overlap by exactly half the size of second box x axis wise
                y = largePlaceable.height / 2 - smallPlaceable.height / 2 //center with y axis
            )
        }
    }
}
