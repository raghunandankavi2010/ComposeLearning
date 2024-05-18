package com.example.composelearning.graphics

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composelearning.R
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

@Composable
fun Modifier.dragIndicatorModifier2(
    state: MutableState<Float>,
    minTemp: Float,
    maxTemp: Float,
    indicatorWidth: Dp = 10.dp,
    onDragEnd: (Float) -> Unit = {}
): Modifier {
    return this.pointerInput(Unit) {
        detectDragGestures(
            onDragStart = {
                // No action needed on drag start
            },
            onDrag = { change, dragAmount ->
                val canvasWidth = size.width - 8.dp.toPx().toInt()
                val dragRatio = dragAmount.x / canvasWidth.coerceAtLeast(1.toDp().toPx().toInt())

                val positionChange = (maxTemp - minTemp) * dragRatio
                val newPosition = state.value + positionChange
                val clampedPosition =
                    max(minTemp, min(maxTemp, newPosition))
                state.value = clampedPosition
            },
            onDragEnd = {
                onDragEnd(state.value)
            }
        )
        detectTapGestures(
            onTap = {
                val canvasSize = size
                if (it.x in 0f..(indicatorWidth.toPx() - 5.dp.toPx())) {
                    val newPosition = (it.x / canvasSize.width) * (maxTemp - minTemp) + minTemp
                    val clampedPosition = max(minTemp, min(maxTemp, newPosition))
                    state.value = clampedPosition
                }
            }
        )
    }
}

fun getHalfOfRange(min: Float, max: Float): Float {
    // Calculate the range (difference between max and min)
    val range = max - min

    // Divide the range by 2 to find the half point
    val half = range / 2.0f

    // Add the half point to the minimum value to get the middle point
    return min + half
}

@Composable
fun TemperatureChart3(
    modifier: Modifier = Modifier,
    temp: Int,
    minTemp: Int, // Adjusted minTemp considering offset
    maxTemp: Int, // Adjusted maxTemp considering offset
    textMeasurer: TextMeasurer = rememberTextMeasurer()
) {
    val context = LocalContext.current
    val tickInterval = (maxTemp.toFloat() - minTemp.toFloat()) / (5.0f - 1.0f)

    val numTicks = (maxTemp - minTemp) / tickInterval.toInt() + 1

    val vector = ImageVector.vectorResource(id = R.drawable.indicator)
    val painter = rememberVectorPainter(image = vector)

    val state = remember { mutableFloatStateOf((temp).toFloat()) } // Track indicator position with offset

    Canvas(
        modifier = modifier
            .dragIndicatorModifier2( // Apply the custom modifier
                state = state,
                minTemp = minTemp.toFloat(),
                maxTemp = maxTemp.toFloat(),
                onDragEnd = { newTemp ->
                    val roundedTemp = round(newTemp).toInt() // Adjust for offset before showing toast
                    Toast.makeText(context.applicationContext, "$roundedTemp", Toast.LENGTH_SHORT).show()
                }
            )
    ) {

        val tickSpacing = (this.size.width - 16.dp.toPx()) / (numTicks - 1)

        val cornerRadius = CornerRadius(8.dp.toPx())

        drawRoundRect(
            Color(0xFF169B4A),
            size = Size(this.size.width, 50.dp.toPx()),
            cornerRadius = cornerRadius
        )

        val adjustedLeft = (state.floatValue - minTemp.toFloat()) / (maxTemp.toFloat() - minTemp.toFloat()) * (this.size.width  - cornerRadius.x * 2 )

        translate(
            left =   adjustedLeft - 5.dp.toPx() + 8.dp.toPx() ,
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
            val xPosition = i * tickSpacing
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

            translate(left = 8.dp.toPx() ) {
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
                        xPosition - (textSize.width) / 2, 66.dp.toPx()
                    )
                )
            }
        }
    }
}



@Composable
fun Modifier.dragIndicatorModifier(
    state: MutableState<Float>,
    minTemp: Float,
    maxTemp: Float,
    indicatorWidth: Dp = 10.dp,
    onDragEnd: (Float) -> Unit = {}
): Modifier {
    return this.pointerInput(Unit) {
        detectDragGestures(
            onDragStart = {
                // No action needed on drag start
            },
            onDrag = { change, dragAmount ->
                val canvasWidth = size.width
                val dragRatio = dragAmount.x / canvasWidth.coerceAtLeast(1.toDp().toPx().toInt())

                val positionChange = (maxTemp - minTemp) * dragRatio
                val newPosition = state.value + positionChange
                val clampedPosition =
                    max(minTemp, min(maxTemp, newPosition))
                state.value = clampedPosition
            },
            onDragEnd = {
                onDragEnd(state.value)
            }
        )
        detectTapGestures(
            onTap = {
                val canvasSize = size
                if (it.x in 0f..(indicatorWidth.toPx() + 5.dp.toPx())) {
                    val newPosition = (it.x / canvasSize.width) * (maxTemp - minTemp) + minTemp
                    val clampedPosition = max(minTemp, min(maxTemp, newPosition))
                    state.value = clampedPosition
                }
            }
        )
    }
}



@Composable
fun TemperatureChart2(
    modifier: Modifier = Modifier,
    temp: Int,
    radius: Int = 8,
    minTemp: Int = 0,
    maxTemp: Int = 60
) {
    val context = LocalContext.current
    val tickInterval = (maxTemp.toFloat() - minTemp.toFloat()) / (5.0f - 1.0f)

    val numTicks = (maxTemp - minTemp) / tickInterval.toInt() + 1

    val vector = ImageVector.vectorResource(id = R.drawable.indicator)
    val painter = rememberVectorPainter(image = vector)

    val textMeasurer = rememberTextMeasurer()
    val state = remember { mutableFloatStateOf(temp.toFloat()) } // Track indicator position

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 5.dp) // Add some padding for aesthetics
            .dragIndicatorModifier( // Apply the custom modifier
                state = state,
                minTemp = minTemp.toFloat(),
                maxTemp = maxTemp.toFloat(),
                onDragEnd = { newTemp ->
                    val roundedTemp = round(newTemp).toInt()
                   Toast.makeText(context.applicationContext,"$roundedTemp",Toast.LENGTH_SHORT).show()
                }
            )
    ) {

        val tickSpacing = (this.size.width - 16.dp.toPx()) / (numTicks - 1)

        val cornerRadius = CornerRadius((radius.dp.toPx()))


        drawRoundRect(
            Color(0xFF169B4A),
            size = Size(this.size.width, 50.dp.toPx()),
            cornerRadius = cornerRadius
        )

        val adjustedLeft = (state.floatValue - minTemp.toFloat()) / (maxTemp.toFloat() - minTemp.toFloat()) * (this.size.width  - cornerRadius.x * 2 )

        translate(
            left = adjustedLeft - 5.dp.toPx() + radius.dp.toPx(),
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
            val xPosition =  i * tickSpacing
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

@Composable
fun TemperatureChart(
    modifier: Modifier = Modifier,
    temp: Int,
    minTemp: Int,
    maxTemp: Int
) {
    val tickInterval = (maxTemp.toFloat() - minTemp.toFloat()) / (5.0f - 1.0f) // Use float for accurate calculation

    val numTicks = (maxTemp - minTemp) / tickInterval.toInt() + 1

    val vector = ImageVector.vectorResource(id = R.drawable.indicator)
    val painter = rememberVectorPainter(image = vector)

    val textMeasurer = rememberTextMeasurer()

    val state = remember { mutableFloatStateOf(temp.toFloat()) } // Track indicator position


    Canvas(
        modifier = modifier
    ) {

        val tickSpacing = (this.size.width - 16.dp.toPx()) / (numTicks - 1)

        val cornerRadius = CornerRadius(8.dp.toPx())

        drawRoundRect(
            Color(0xFF169B4A),
            size = Size(this.size.width, 50.dp.toPx()),
            cornerRadius = cornerRadius
        )

        val adjustedLeft = (state.floatValue - minTemp.toFloat()) / (maxTemp.toFloat() - minTemp.toFloat()) * (this.size.width  - cornerRadius.x * 2 )

        translate(
            left = adjustedLeft - 5.dp.toPx() + 8.dp.toPx(),
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
            val xPosition = i * tickSpacing
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

            translate(left = 8.dp.toPx() ) {
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
                        xPosition - (textSize.width) / 2, 66.dp.toPx()
                    )
                )
            }
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