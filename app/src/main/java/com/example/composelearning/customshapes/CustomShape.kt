package com.example.composelearning.customshapes


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp


class CustomShape : Shape {
    private val cornerRadius = 50f // Adjust corner radius as needed

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = androidx.compose.ui.graphics.Path().apply {
            lineTo(0f, size.height)
            lineTo(size.width, size.height)
            lineTo(size.width, (size.height / 2.5).toFloat())
            close()

        }
        return Outline.Generic(path)

    }
}


@Composable
fun ShapeTry() {
    Box(
        modifier = Modifier
            .padding(top = 100.dp)
            .widthIn(min = 250.dp, max = 250.dp)
    ) {
       val list =  listOf(Color(0xFF5936B4), Color(0xFF362A84))
        Canvas(
            modifier = Modifier
                .width(250.dp)
                .height(150.dp)
        ) {
            val rect = Rect(Offset.Zero, size)

            val path = androidx.compose.ui.graphics.Path().apply {
                lineTo(0f, size.height)
                lineTo(size.width, size.height)
                lineTo(size.width, (size.height / 2.5).toFloat())
                close()

            }

           val paint =  Paint().apply {
               shader = LinearGradientShader(
                   rect.topLeft,
                   rect.bottomRight,
                   list
               )
               pathEffect = PathEffect.cornerPathEffect(60f)
           }
            drawIntoCanvas { canvas ->
                canvas.drawOutline(
                    outline = Outline.Generic(path),
                    paint = paint
                )
            }

        }
    }
}
