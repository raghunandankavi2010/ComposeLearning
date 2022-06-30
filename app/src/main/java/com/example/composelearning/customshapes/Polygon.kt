package com.example.composelearning.customshapes

import android.util.Log
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import java.lang.Math.cos
import java.lang.Math.sin

class Polygon(val sides: Int, val rotation: Float = 0f) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return Outline.Generic(
            Path().apply {
                val radius = if (size.width > size.height) size.width / 2f else size.height / 2f
                Log.i("Polygon","Radius $radius")
                val angle = 2.0 * Math.PI / sides
                Log.i("Polygon","$angle")
                val cx = size.width / 2f
                val cy = size.height / 2f
                Log.i("Polygon","$cx $cy")
                val r = rotation * (Math.PI / 180)
                Log.i("Polygon","$r")
                moveTo(
                    cx + (radius * kotlin.math.cos(0.0 + r).toFloat()),
                    cy + (radius * sin(0.0 + r).toFloat())
                )
                Log.i("Polygon x","${cx + (radius * cos(0.0 + r).toFloat())}")
                Log.i("Polygon y","${cy + (radius * sin(0.0 + r).toFloat())}")
                for (i in 1 until sides) {
                    lineTo(
                        cx + (radius * cos(angle * i + r).toFloat()),
                        cy + (radius * sin(angle * i + r).toFloat())
                    )
                }
                close()
            })
    }
}