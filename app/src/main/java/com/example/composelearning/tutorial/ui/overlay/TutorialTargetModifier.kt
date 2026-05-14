package com.example.composelearning.tutorial.ui.overlay

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.tutorialTarget(
    key: String,
    padding: Dp = 8.dp,
    controller: SpotlightController? = LocalSpotlightController.current,
): Modifier = composed {
    if (controller == null) return@composed this
    val density = LocalDensity.current
    val paddingPx = with(density) { padding.toPx() }

    DisposableEffect(controller, key) {
        onDispose { controller.clear(key) }
    }

    this.onGloballyPositioned { coords ->
        if (!coords.isAttached) return@onGloballyPositioned
        val bounds = coords.boundsInWindow()
        val padded = Rect(
            offset = bounds.topLeft - Offset(paddingPx, paddingPx),
            size = Size(bounds.width + 2 * paddingPx, bounds.height + 2 * paddingPx),
        )
        controller.publish(key, padded)
    }
}