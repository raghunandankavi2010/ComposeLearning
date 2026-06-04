package com.example.composelearning.pathmorph.domain.model

/** A phone silhouette to morph between. Coordinates are in the shared 100×300 viewBox. */
data class PhoneShape(
    val label: String,            // e.g. the year "1990"
    val pathData: String,         // SVG path "d" for the outline
    val screen: ScreenRect,       // the screen cut-out, in viewBox coords
)

data class ScreenRect(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
)
