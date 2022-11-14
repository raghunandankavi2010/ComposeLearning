package com.example.composelearning.lists

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CenterCircle(
    modifier: Modifier = Modifier,
    fillColor: Color,
    strokeWidth: Dp
) {
    Canvas(
        modifier = modifier
            .size(75.dp)
    ) {

        drawArc(
            color = fillColor,
            0f,
            360f,
            true,
            style = Stroke(strokeWidth.toPx(), cap = StrokeCap.Round),
            size = Size(size.width, size.height)
        )
    }

}