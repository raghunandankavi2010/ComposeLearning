package com.example.composelearning.panel

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ScreenWithSidePanel() {
    val (panelPosition, setPanelPosition) = remember { mutableStateOf(Offset(300f, 0f)) }
    val state = rememberDraggableState(
        onDelta = { delta -> Log.d("SidePanel", "Dragged $delta") }
    )
    Box(modifier = Modifier.fillMaxSize()) {
        // Content of the screen
        Column(
            modifier = Modifier
                .offset(panelPosition.x.dp, panelPosition.y.dp)
                .width(300.dp)
                .fillMaxHeight()
                .background(Color.Blue)
                .draggable(
                    state = state,
                    orientation = Orientation.Horizontal,
                    onDragStarted = { dragDistance ->
                        val newX = panelPosition.x - dragDistance.x
                        setPanelPosition(Offset(x = newX, y = 0f))
                    }
                )
        ) {
            // Side panel content
        }
    }
}
