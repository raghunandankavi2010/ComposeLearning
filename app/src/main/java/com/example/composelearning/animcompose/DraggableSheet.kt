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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

enum class SheetState {
    Collapsed,
    Expanded
}

class CurvedSideSheetShape(private val curveAmount: Float) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            moveTo(size.width, 0f)
            lineTo(size.width, size.height)
            lineTo(curveAmount, size.height)
            // Quadratic curve with control point at -curveAmount to ensure peak reaches 0
            // This creates a prominent bulge that moves from peak (0) to base (curveAmount)
            quadraticTo(-curveAmount, size.height / 2f, curveAmount, 0f)
            close()
        }
        return Outline.Generic(path)
    }
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
        // Increase visible width when collapsed to ensure the curve is prominent and visible
        val visibleCollapsedWidth = 64.dp 
        val density = LocalDensity.current
        val totalDraggableWidth = sheetContentWidth + handleWidth

        val anchors = remember(screenWidth, sheetContentWidth, visibleCollapsedWidth) {
            DraggableAnchors {
                // Anchors defined by how much of the sheet is visible
                SheetState.Collapsed at with(density) { (screenWidth - visibleCollapsedWidth).toPx() }
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

        val maxCurvePx = with(density) { visibleCollapsedWidth.toPx() }
        val curveAmountPx by remember(anchors) {
            derivedStateOf {
                val offset = dragState.offset
                val collapsed = anchors.positionOf(SheetState.Collapsed)
                val expanded = anchors.positionOf(SheetState.Expanded)
                
                if (offset.isNaN()) maxCurvePx
                else {
                    // progress: 1.0 at collapsed (max curve), 0.0 at expanded (straight)
                    val progress = ((offset - expanded) / (collapsed - expanded)).coerceIn(0f, 1f)
                    maxCurvePx * progress
                }
            }
        }

        val curvedShape = remember(curveAmountPx) {
            CurvedSideSheetShape(curveAmountPx)
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
                    .width(sheetContentWidth),
                shape = curvedShape,
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(start = with(density) { curveAmountPx.toDp() }),
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
