package com.example.composelearning.graphics

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composelearning.R
import kotlin.math.abs


@Composable
fun TemperatureChart(
    modifier: Modifier = Modifier,
    temp: Int,
    minTemp: Int = 0,
    maxTemp: Int = 60
) {

    //val tickInterval = (maxTemp - minTemp) / (5 - 1)
    val tickInterval = (maxTemp.toFloat() - minTemp.toFloat()) / (5.0f - 1.0f) // Use float for accurate calculation

    val numTicks = (maxTemp - minTemp) / tickInterval.toInt() + 1

    val vector = ImageVector.vectorResource(id = R.drawable.indicator)
    val painter = rememberVectorPainter(image = vector)

    val textMeasurer = rememberTextMeasurer()


    Canvas(
        modifier = modifier
    ) {

        val tickSpacing = (this.size.width) / (numTicks - 1)

        drawRoundRect(
            Color(0xFF169B4A),
            size = Size(this.size.width, 50.dp.toPx()),
            cornerRadius = CornerRadius(8.dp.toPx())
        )

        val tempRatio =
            (temp.toFloat() - minTemp.toFloat()) / (maxTemp.toFloat() - minTemp.toFloat())
        val left = tempRatio * this.size.width

        translate(
            left = left - 5.dp.toPx(),
            top = -3.dp.toPx()
        ) {
            with(painter) {
                draw(
                    size = Size(10.dp.toPx(), 56.dp.toPx())
                )
            }
        }

        // Loop through each tick position based on spacing
        for (i in 0 until numTicks) {
            val xPosition = when (i) {
                0 -> {
                    i * tickSpacing + 20
                }

                numTicks - 1 -> {
                    i * tickSpacing - 20
                }

                else -> {
                    i * tickSpacing
                }
            }
            val text = (minTemp + i * tickInterval).toInt().toString()

            // Measure text width for accurate centering
            val textMeasureResult = textMeasurer.measure(
                text = text,
                style = TextStyle(
                    fontSize = 11.sp,
                    lineHeight = 13.sp,
                    fontFamily = FontFamily(Font(R.font.jio_type_medium)),
                    fontWeight = FontWeight(500),
                    color = Color(0xA6000000),

                    textAlign = TextAlign.Center,
                )
            )

            drawLine(
                color = Color(0xA6000000),
                start = Offset(x = xPosition, y = 54.dp.toPx()),
                end = Offset(x = xPosition, y = 64.dp.toPx()),
                strokeWidth = 2.dp.toPx()
            )

            val textSize = textMeasureResult.size


            drawText(
                textLayoutResult = textMeasureResult,
                color = Color(0xA6000000),
                topLeft = Offset(
                    xPosition - (textSize.width )/  2, 66.dp.toPx())
                )

        }
    }
}


//val textSize = textMeasureResult.size

//val textCenter = textSize.center

//            drawText(
//                textLayoutResult = textMeasureResult,
//                color = Color.Black,
//                topLeft = Offset(xPosition - (textCenter.x/2), 66.dp.toPx() - (textCenter.x/2)
//                )
//            )
//            val textMeasureResult = textMeasurer.measure(
//                text = "$intial",
//                style = TextStyle(
//                    fontSize = 11.sp,
//                    lineHeight = 13.sp,
//                    fontFamily = FontFamily(Font(R.font.jio_type_medium)),
//                    fontWeight = FontWeight(500),
//                    color = Color(0xA6000000),
//
//                    textAlign = TextAlign.Center,
//                )
//            )

//@Composable
//fun TemperatureChart(
//    modifier: Modifier = Modifier,
//    temp: Int,
//    minTemp: Int = 0,
//    maxTemp: Int = 60
//) {
//
//    val tickInterval = 15
//    val numTicks = (maxTemp - minTemp) / tickInterval + 1
//
//
//    Canvas(
//        modifier = modifier
//    ) {
//
//        val tickSpacing = this.size.width / 5
//
//        drawRoundRect(Color(0xFF169B4A), size = Size(this.size.width,50.dp.toPx()), cornerRadius = CornerRadius(8.dp.toPx()))
//
//        // Loop through each tick position based on spacing
//        for (i in 0..numTicks) {
//            val xPosition = i * tickSpacing // Calculate x-coordinate in pixels
//
//            drawLine(
//                color = Color(0xA6000000),
//                start = Offset(x = xPosition, y = 54.dp.toPx()),
//                end = Offset(x = xPosition, y = 64.dp.toPx()),
//                strokeWidth = 2.dp.toPx()
//            )
//        }
//    }
//}


//fun TemperatureChart(modifier: Modifier = Modifier, temp: Int, minTemp: Int, maxTemp: Int) {
//
//
//
//    val tickInterval = 15
//    val numTicks = (maxTemp - minTemp) / tickInterval + 1 //
//
//    Canvas(
//        modifier = modifier
//            .height(70.dp)
//            .fillMaxWidth()
//    ) {
//        val canvasWidth = size.width // Get the actual canvas width
//
//        val tickSpacing = canvasWidth / (numTicks - 1) // Calculate spacing between ticks
//
//        drawRoundRect(Color(0xFF169B4A), size = Size(this.size.width,50.dp.toPx() ), cornerRadius = CornerRadius(8.dp.toPx()))
//
//        // Loop through each tick position based on spacing
//        for (i in 0..numTicks) {
//            val xPosition =i * tickSpacing // Calculate x-coordinate based on spacing
//
//            drawLine(
//                color = Color(0xA6000000),
//                start = Offset(x = xPosition.dp.toPx(), y = 54.dp.toPx()),
//                end = Offset(x = xPosition.dp.toPx(), y = 64.dp.toPx()),
//                strokeWidth = 2.dp.toPx()
//            )
//        }
//    }
//}