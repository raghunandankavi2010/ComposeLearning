package com.example.composelearning.panel

import androidx.compose.runtime.Stable


@Stable
interface DragState {
    val currentHeight: Float
    val maxHeight: Float

    suspend fun snapTo(value: Float)
    suspend fun decayTo(velocity: Float, value: Float)
    suspend fun stop()
}