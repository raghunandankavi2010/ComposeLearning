package com.example.composelearning.tutorial.ui.overlay

import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.geometry.Rect

@Stable
class SpotlightController {

    // Snapshot-backed map — only written to when active so we don't churn snapshots during normal scroll.
    private val observableBounds: SnapshotStateMap<String, Rect> = mutableStateMapOf()

    // Plain HashMap, used while inactive. Cheap writes (no Snapshot mutation, no recomposition fan-out).
    private val cachedBounds: HashMap<String, Rect> = HashMap()

    var isActive: Boolean by mutableStateOf(false)
        private set

    fun publish(key: String, rect: Rect) {
        if (cachedBounds[key] == rect) return
        cachedBounds[key] = rect
        if (isActive) {
            observableBounds[key] = rect
        }
    }

    fun clear(key: String) {
        cachedBounds.remove(key)
        observableBounds.remove(key)
    }

    fun boundsOf(key: String): Rect? = observableBounds[key]

    fun activate() {
        if (isActive) return
        isActive = true
        // Flush whatever bounds we already know — visible targets that won't relayout
        // would otherwise leave the snapshot map empty.
        cachedBounds.forEach { (k, v) ->
            if (observableBounds[k] != v) observableBounds[k] = v
        }
    }

    fun deactivate() {
        if (!isActive) return
        isActive = false
        observableBounds.clear()
    }
}

val LocalSpotlightController = compositionLocalOf<SpotlightController?> { null }