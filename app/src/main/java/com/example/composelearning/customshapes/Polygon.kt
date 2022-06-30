package com.example.composelearning.customshapes

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.cos
import kotlin.math.sin

class Polygon(val sides: Int, val rotation: Float = 0f) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        return Outline.Generic(
            Path().apply {
                // same logic as below
/*                val unitAngle = 2.0f * Math.PI / sides // calculate cell angle
                var angle = 0f
                val radius = if (size.width > size.height) size.width / 2f else size.height / 2f
                val originx = size.width / 2f
                val originy = size.height / 2f

                moveTo(
                    originx + (radius * cos(0.0 ).toFloat()),
                    originx + (radius * sin(0.0 ).toFloat())
                )

                for (i in 0 until sides) {
                    val xLength = radius * cos(angle)
                    val yLength = radius * sin(angle)
                    lineTo(originx + xLength, originy - yLength); // draw path
                    angle += unitAngle.toFloat()
                }*/
                // calculate radius
                val radius = if (size.width > size.height) size.width / 2f else size.height / 2f
                // calculate 180/number of sides in radians
                val angle = 2.0 * Math.PI / sides
                // center of the shape
                val cx = size.width / 2f
                val cy = size.height / 2f
                // in case you need to rotate
                val r = rotation * (Math.PI / 180)
                // move to the right its like calculating a point on the circumference of the circle
                moveTo(
                    cx + (radius * cos(0.0 + r).toFloat()),
                    cy + (radius * sin(0.0 + r).toFloat())
                )
                // calculate the other points using the same logic
                for (i in 1 until sides) {
                    lineTo(
                        cx + (radius * cos(angle * i + r).toFloat()),
                        cy + (radius * sin(angle * i + r).toFloat())
                    )
                }
                // close the path
                close()
            })
    }
}