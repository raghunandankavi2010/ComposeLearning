package com.example.composelearning.graphics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.composelearning.R

@Composable
fun ThermometerCanvas(modifier: Modifier = Modifier, progress: Int = 90) {
    val totalProgress = 161 - 32 - 20
    val actualProgress = progress  * (totalProgress) / 100 - (6*2)
    val vector = ImageVector.vectorResource(id = R.drawable.thermometer)
    val painter = rememberVectorPainter(image = vector)
    Canvas(
        modifier = modifier.fillMaxSize().padding(start = 50.dp, top = 50.dp)
    ) {
        with(painter) {
            draw(
                size = Size(50.dp.toPx(), 161.dp.toPx())
            )
        }

        // Drawing the circle at the bottom for filling the thermometer
        drawCircle(
            color = Color.Red,
            center = Offset(25.dp.toPx(), 161.dp.toPx() - 26.dp.toPx()),
            radius = 20.dp.toPx(),
        )

        // Drawing the line above the circle to indicate the meter
        drawLine(
            color = Color.Red,
            start = Offset(25.dp.toPx(), totalProgress.dp.toPx() + 6.dp.toPx()),
            end = Offset(25.dp.toPx(),totalProgress.dp.toPx() - actualProgress.dp.toPx()  ),
            strokeWidth = 15.dp.toPx(),
            StrokeCap.Round

        )
    }
}

@Preview
@Composable
fun PreviewThermometerCanvas() {
    ThermometerCanvas()
}
