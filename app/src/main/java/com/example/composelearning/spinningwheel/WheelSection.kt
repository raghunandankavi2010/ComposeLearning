package com.example.composelearning.spinningwheel

import androidx.compose.ui.graphics.Color

/**
 * One slice of the spinning wheel.
 *
 * @param label text drawn radially inside the slice.
 * @param color fill color of the slice.
 */
data class WheelSection(
    val label: String,
    val color: Color,
)

/**
 * A balanced, vibrant default wheel used by the demo screen and previews.
 * Colors alternate around the wheel so neighbouring slices stay readable.
 */
val defaultWheelSections: List<WheelSection> = listOf(
    WheelSection("100", Color(0xFFEF5350)),
    WheelSection("Try\nAgain", Color(0xFFFFB300)),
    WheelSection("200", Color(0xFF26A69A)),
    WheelSection("Bonus", Color(0xFF5C6BC0)),
    WheelSection("50", Color(0xFFEC407A)),
    WheelSection("500", Color(0xFF66BB6A)),
    WheelSection("Jackpot", Color(0xFFAB47BC)),
    WheelSection("10", Color(0xFF29B6F6)),
)
