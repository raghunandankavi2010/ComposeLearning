package com.example.composelearning.tutorial.ui.overlay

import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.geometry.Rect

@Stable
class SpotlightController {

    private val bounds: SnapshotStateMap<String, Rect> = mutableStateMapOf()

    fun publish(key: String, rect: Rect) {
        val existing = bounds[key]
        if (existing == null || existing != rect) {
            bounds[key] = rect
        }
    }

    fun clear(key: String) {
        bounds.remove(key)
    }

    fun boundsOf(key: String): Rect? = bounds[key]
}

val LocalSpotlightController = compositionLocalOf<SpotlightController?> { null }