package com.example.composelearning.breathing.domain.model

/** Configuration for the breathing scene (colors + copy). */
data class BreathingSession(
    val title: String,
    val baseColor: Long,
    val wave1: Long,
    val wave2: Long,
    val wave3: Long,
    val startLabel: String,
    val endLabel: String
)
