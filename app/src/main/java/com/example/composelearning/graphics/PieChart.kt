package com.example.composelearning.graphics


import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun PieChartPreview(onClick: ((data: ChartData, index: Int, Offset) -> Unit)? = null, offsetChange: (Offset) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        val data = remember {
            listOf(
                ChartData(Color.Green, 10f),
                ChartData(Color.Red, 20f),
                ChartData(Color.Cyan, 15f),
                ChartData(Color.Blue, 5f),
                ChartData(Color.Yellow, 35f),
                ChartData(Color.Magenta, 15f)
            )
        }



        PieChart(
            modifier = Modifier
                .fillMaxWidth()
                .height(600.dp)
                .padding(top = 50.dp),
            data = data,
            outerRingPercent = 35,
            onClick = { data, index, Offset ->
            },
            offsetChange = offsetChange
        )
    }
}

@Composable
fun PieChart(
    modifier: Modifier,
    data: List<ChartData>,
    startAngle: Float = 0f,
    outerRingPercent: Int = 35,
    drawText: Boolean = true,
    onClick: ((data: ChartData, index: Int, Offset) -> Unit)? = null,
    offsetChange: (Offset) -> Unit
) {

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {

        val density = LocalDensity.current

        val width = constraints.maxWidth.toFloat()


        // Outer radius of chart. This is edge of stroke width as
        val radius = (width / 2f) * .9f
        val outerStrokeWidthPx =
            (radius * outerRingPercent / 100f).coerceIn(0f, radius)

        // Inner radius of chart. Semi transparent inner ring
        val innerRadius = (radius - outerStrokeWidthPx).coerceIn(0f, radius)

        // Start angle of chart. Top center is -90, right center 0,
        // bottom center 90, left center 180
        val chartStartAngle = startAngle
        val animatableInitialSweepAngle = remember {
            Animatable(chartStartAngle)
        }

        val chartEndAngle = 360f + chartStartAngle

        val sum = data.sumOf {
            it.data.toDouble()
        }.toFloat()

        val coEfficient = 360f / sum
        var currentAngle = 0f
        val currentSweepAngle = animatableInitialSweepAngle.value

        val chartDataList = remember(data) {
            data.map {

                val chartData = it.data
                val range = currentAngle..currentAngle + chartData * coEfficient
                currentAngle += chartData * coEfficient

                AnimatedChartData(
                    color = it.color,
                    data = it.data,
                    selected = false,
                    range = range
                )
            }
        }


        LaunchedEffect(key1 = animatableInitialSweepAngle) {
            animatableInitialSweepAngle.animateTo(
                targetValue = chartEndAngle,
                animationSpec = tween(
                    delayMillis = 1000,
                    durationMillis = 1500
                )
            )
        }

        val textMeasurer = rememberTextMeasurer()
        val textMeasureResults: List<TextLayoutResult> = remember(chartDataList) {
            chartDataList.map {
                textMeasurer.measure(
                    text = "%${it.data.toInt()}",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        val chartModifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { position: Offset ->
                        val xPos = size.center.x - position.x
                        val yPos = size.center.y - position.y
                        val length = sqrt(xPos * xPos + yPos * yPos)
                        val isTouched = length in innerRadius..radius

                        if (isTouched) {
                            var touchAngle =
                                (-chartStartAngle + 180f + atan2(
                                    yPos,
                                    xPos
                                ) * 180 / Math.PI) % 360f

                            if (touchAngle < 0) {
                                touchAngle += 360f
                            }

                            chartDataList.forEachIndexed { index, chartData ->
                                val range = chartData.range
                                val isTouchInArcSegment = touchAngle in range
                                if (chartData.isSelected) {
                                    chartData.isSelected = false
                                } else {
                                    chartData.isSelected = isTouchInArcSegment

                                    if (isTouchInArcSegment) {
                                        val arcCenterAngle = touchAngle
                                        val arcCenterOffset = calculateArcCenterOffset(
                                            size.center.x,
                                            size.center.y,
                                            arcCenterAngle.toFloat(),
                                            innerRadius
                                        )
                                        onClick?.invoke(
                                            ChartData(
                                                color = chartData.color,
                                                data = chartData.data
                                            ), index, arcCenterOffset
                                        )

                                    }
                                }
                            }
                        }
                    }
                )
            }

        PieChartImpl(
            modifier = chartModifier,
            chartDataList = chartDataList,
            textMeasureResults = textMeasureResults,
            currentSweepAngle = currentSweepAngle,
            chartStartAngle = chartStartAngle,
            chartEndAngle = chartEndAngle,
            outerRadius = radius,
            outerStrokeWidth = outerStrokeWidthPx,
            innerRadius = innerRadius,
            drawText = drawText,
            offsetChange = offsetChange
        )

    }
}

@Composable
private fun PieChartImpl(
    modifier: Modifier = Modifier,
    chartDataList: List<AnimatedChartData>,
    textMeasureResults: List<TextLayoutResult>,
    currentSweepAngle: Float,
    chartStartAngle: Float,
    chartEndAngle: Float,
    outerRadius: Float,
    outerStrokeWidth: Float,
    innerRadius: Float,
    drawText: Boolean,
    offsetChange: (Offset) -> Unit
) {
    Canvas(modifier = modifier) {

        val width = size.width
        var startAngle = chartStartAngle


        for (index in 0..chartDataList.lastIndex) {

            val chartData = chartDataList[index]
            val range = chartData.range
            val sweepAngle = range.endInclusive - range.start
            val angleInRadians = (startAngle + sweepAngle / 2).degreeToRadian
            val textMeasureResult = textMeasureResults[index]
            val textSize = textMeasureResult.size

            val arcWidth = if (chartData.isSelected) {
                // Increase arc width for the first arc
                outerStrokeWidth + 50f // You can adjust the value as needed
            } else {
                // Keep the same arc width for other arcs
                outerStrokeWidth
            }

            withTransform(
                {
                    val scale = chartData.animatable.value
                    scale(
                        scaleX = scale,
                        scaleY = scale
                    )
                }
            ) {

                if (startAngle <= currentSweepAngle) {

                    val color = chartData.color
                    val diff = (width / 2 - outerRadius) / outerRadius
                    val fraction = (chartData.animatable.value - 1f) / diff

                    val animatedColor = androidx.compose.ui.graphics.lerp(
                        color,
                        color.copy(alpha = .8f),
                        fraction
                    )

                    drawArc(
                        color = animatedColor,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle.coerceAtMost(
                            currentSweepAngle - startAngle
                        ),
                        useCenter = false,
                        topLeft = Offset(
                            (width - 2 * innerRadius - arcWidth) / 2,
                            (width - 2 * innerRadius - arcWidth) / 2
                        ),
                        size = Size(
                            innerRadius * 2 + arcWidth,
                            innerRadius * 2 + arcWidth
                        ),
                        style = Stroke(arcWidth)
                    )
                }

                val textCenter = textSize.center

                if (drawText && currentSweepAngle == chartEndAngle && chartData.isSelected) {
                    val offset = Offset(center.x
                            + (innerRadius + arcWidth / 2) * cos(angleInRadians),
                    center.y
                    + (innerRadius + arcWidth / 2) * sin(angleInRadians)
                    )
                    //offsetChange(offset)

                    val rectSize = Size(200f, 200f)
                    val cornerRadius = 16f
                    val textColor = Color.Black

                    // Draw rounded rectangle
                    drawRoundRect(
                        color = Color.Black,
                        topLeft = Offset(offset.x - 100, offset.y - 100),
                        size = rectSize,
                        cornerRadius = CornerRadius(5f)
                    )
                    drawText(
                        textLayoutResult = textMeasureResult,
                        color = Color.White,
                        topLeft = Offset(
                            (-textCenter.x + center.x
                                    + (innerRadius + arcWidth / 2) * cos(angleInRadians)),
                            (-textCenter.y + center.y
                                    + (innerRadius + arcWidth / 2) * sin(angleInRadians))
                        )
                    )
                }
            }

            startAngle += sweepAngle
        }
    }
}


@Immutable
data class ChartData(val color: Color, val data: Float)

@Immutable
internal class AnimatedChartData(
    val color: Color,
    val data: Float,
    selected: Boolean = false,
    val range: ClosedFloatingPointRange<Float>,
    val animatable: Animatable<Float, AnimationVector1D> = Animatable(1f)
) {
    var isSelected by mutableStateOf(selected)
}

val Float.degreeToRadian
    get() = (this * Math.PI / 180f).toFloat()

val Float.asAngle: Float
    get() = this * 360f / 100f


private fun calculateArcCenterOffset(x: Int, y: Int, angle: Float, innerRadius: Float): Offset {
    val angleInRadians = angle.degreeToRadian
    val centerX = x + (innerRadius + innerRadius) / 2 * cos(angleInRadians)
    val centerY = y + (innerRadius + innerRadius) / 2 * sin(angleInRadians)
    return Offset(centerX, centerY)
}

//@Composable
//fun PopupContent(centerOffset: Offset) {
//    // Your popup content goes here
//    Box(
//        modifier = Modifier
//            .padding(16.dp)
//            .background(Color.White)
//            .clip(RoundedCornerShape(8.dp))
//            .padding(16.dp)
//    ) {
//        Text("Popup Content")
//    }
//}
//
//// Call this function where you handle the click event in PieChart
//@Composable
//fun ShowPopup(centerOffset: Offset) {
//    Dialog(onDismissRequest = { /* Handle dismiss if needed */ }) {
//        PopupContent(centerOffset)
//    }
//}


@Composable
fun ShowDialog(context: Context, centerOffset: Offset, shouldShow: Boolean, show:(Boolean) -> Unit) {
    val dialog = remember { mutableStateOf(true) }

    if (shouldShow) {
        AlertDialog(
            onDismissRequest = {
                show(false)
            },
            title = {
                Text("Popup Content")
            },
            text = {
                Text("Your custom popup content goes here.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                       // dialog.value = false
                    }
                ) {
                    Text("OK")
                }
            },
            modifier = Modifier
                .size(100.dp)
               .offset(centerOffset.x.dp,centerOffset.y.dp)


        )
    }
}