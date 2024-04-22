package com.example.composelearning.graphics


import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composelearning.R
import com.example.composelearning.customshapes.dpToPx
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PieChartPreview(
    onClick: (data: ChartData, index: Int) -> Unit
) {

    var dismissToolTip by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable {
                dismissToolTip = true
            }
    ) {
        val data = remember {
            listOf(
                ChartData(Color(0xFF0F7737), 3f),
                ChartData(Color(0xFFF7AB20), 27f),
                ChartData(Color(0xFF2253DA), 1f),
                ChartData(Color(0xFFBBCEF5), 6f),
                ChartData(Color(0xFFB72ABB), 7f),
                ChartData(Color(0xFFF5A8B8), 26f),
                ChartData(Color(0xFF8C652C), 30f),
//                ChartData(Color.LightGray, 25f),
//                ChartData(Color.Green, 18f),
//              ChartData(Color.Red, 15f),
//              ChartData(Color.Cyan, 35f),

            )
        }

        PieChart(
            modifier = Modifier
                .height(300.dp)
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally),
            data = data,
            outerRingPercent = 35,
            onClick = { chartData, index ->
               // onClick(chartData, index)
            },
            dimissToolTip = dismissToolTip
        ) {
            dismissToolTip = it
        }
    }
}

@Composable
fun PieChart(
    modifier: Modifier,
    data: List<ChartData>,
    startAngle: Float = 0f,
    outerRingPercent: Int = 35,
    drawText: Boolean = true,
    onClick: ((data: ChartData, index: Int) -> Unit)? = null,
    dimissToolTip: Boolean,
    resetDismiss: (Boolean) -> Unit
) {

    BoxWithConstraints(
        modifier = modifier.clickable(enabled = false) {

        },
        contentAlignment = Alignment.Center,
    ) {

        val width = constraints.maxWidth.toFloat()

        val innerRadius = 74.dp.dpToPx()
        val outerRadius = 128.dp.dpToPx()
        val outerStrokeWidthPx = outerRadius - innerRadius

        // Outer radius of chart. This is edge of stroke width as
        //val innerRadius = 74.dp.dpToPx()
        //val outerRadius = 118.dp.dpToPx()
        //val radius = (width / 2f) * .9f

        //val outerStrokeWidthPx = 44.dp.dpToPx()//outerRadius - innerRadius
          //  maxOf((outerRadius * outerRingPercent / 100f), 44f).coerceIn(0f, outerRadius)
        //val outerStrokeWidthPx = 44.dp.dpToPx()
         //   (outerRadius * outerRingPercent / 100f).coerceIn(0f, radius)

        // Inner radius of chart. Semi transparent inner ring
       // val innerRadius = (radius - outerStrokeWidthPx).coerceIn(0f, radius)

        // Start angle of chart. Top center is -90, right center 0,
        // bottom center 90, left center 180
        val animatableInitialSweepAngle = remember {
            Animatable(startAngle)
        }

        val chartEndAngle = 360f + startAngle

        val sum = data.sumOf { it.data.toDouble() }.toFloat()

// Calculate the total sum of data points less than 8.
        val smallDataSum = data.filter { it.data <= 6 }.sumOf { it.data.toDouble() }.toFloat()

// Calculate the remaining sum for large data points (8 or more).
        val largeDataSum = sum - smallDataSum
        val coEfficient = 360f / largeDataSum

        val smallDataCount = data.count { it.data <= 6 }.toFloat()

// Distribute the remaining angle equally among small data points.
        val smallDataAngle = (data.count { it.data <= 6 }) * 20f
        val smallDataCoefficient = smallDataAngle / smallDataCount


        val coEfficientLarge = (360f - smallDataAngle) / largeDataSum

        var currentAngle = 0f
        val currentSweepAngle = animatableInitialSweepAngle.value

        val chartDataList = remember(data) {
            data.map {
                val chartData = it.data
                val range = if (chartData <= 6) {
                    currentAngle..currentAngle + 20
                } else {
                    currentAngle..currentAngle + chartData * coEfficientLarge
                }
                currentAngle += if (chartData <= 6) {
                    chartData * (20 / chartData)
                } else {
                    chartData * coEfficientLarge
                }

                AnimatedChartData(
                    color = it.color,
                    data = it.data,
                    selected = false,
                    range = range
                )
            }
        }


//        var currentAngle = 0f
//        val currentSweepAngle = animatableInitialSweepAngle.value
//
//        val chartDataList = remember(data) {
//            data.map {
//
//                val chartData = it.data
//                val range = currentAngle..currentAngle + chartData * coEfficient
//                currentAngle += chartData * coEfficient
//
//                AnimatedChartData(
//                    color = it.color,
//                    data = it.data,
//                    selected = false,
//                    range = range
//                )
//            }
//        }


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
        val textMeasureResults: List<TextLayoutResult> =
            remember(chartDataList) {
                chartDataList.map {
                    textMeasurer.measure(
                        text = "${it.data.toInt()}%",
                        style = TextStyle(
                                fontSize = 11.sp,
                                lineHeight = 16.5.sp,
                                fontFamily = FontFamily(Font(R.font.jio_type_medium)),
                                fontWeight = FontWeight(500),
                                color = Color(0xFF141414),

                                )
                        )

                }
            }

        val chartModifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
//            .pointerInput(Unit) {
//                detectTapGestures(
//                    onTap = { position: Offset ->
//                        val xPos = size.center.x - position.x
//                        val yPos = size.center.y - position.y
//                        val length = sqrt(xPos * xPos + yPos * yPos)
//                        val isTouched = length in innerRadius..radius
//
//                        if (isTouched) {
//                            var touchAngle =
//                                (-startAngle + 180f + atan2(
//                                    yPos,
//                                    xPos
//                                ) * 180 / Math.PI) % 360f
//
//                            if (touchAngle < 0) {
//                                touchAngle += 360f
//                            }
//
//                            chartDataList.forEachIndexed { index, chartData ->
//                                val range = chartData.range
//                                val isTouchInArcSegment = touchAngle in range
//                                if (chartData.isSelected) {
//                                    chartData.isSelected = false
//                                    resetDismiss(false)
//                                } else {
//                                    chartData.isSelected = isTouchInArcSegment
//
//                                    if (isTouchInArcSegment) {
//                                        onClick?.invoke(
//                                            ChartData(
//                                                color = chartData.color,
//                                                data = chartData.data
//                                            ), index
//                                        )
//
//                                    }
//                                }
//                            }
//                        }
//                    }
//                )
//            }

        PieChartImpl(
            modifier = chartModifier,
            chartDataList = chartDataList,
            textMeasureResults = textMeasureResults,
            currentSweepAngle = currentSweepAngle,
            chartStartAngle = startAngle,
            chartEndAngle = chartEndAngle,
            outerRadius = outerRadius,
            outerStrokeWidth = outerStrokeWidthPx,
            innerRadius = innerRadius,
            drawText = drawText,
            dimissToolTip = dimissToolTip
        )

Column(modifier = Modifier.align(Alignment.Center),
    horizontalAlignment = Alignment.CenterHorizontally){

    Text(
        text = "Total expense",
// en/Desktop/Body/XXS
        style = TextStyle(
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontFamily = FontFamily(Font(R.font.jio_type_medium)),
            fontWeight = FontWeight(500),
            color = Color(0xA6000000),
            textAlign = TextAlign.Center,
            )
    )

    Text(
        modifier = Modifier,
        text = "₹2500000",
        style = TextStyle(
            fontSize = 24.sp,
            lineHeight = 28.sp,
            fontFamily = FontFamily(Font(R.font.jio_type_black)),
            fontWeight = FontWeight(900),
            color = Color(0xFF0D0D0E),
            textAlign = TextAlign.Center,
        )
    )
}

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
    rectWidth: Int = 99,
    rectHeight: Int = 54,
    rectCornerRadius: Int = 8,
    triangleWidth: Int = 24,
    triangleHeight: Int = 8,
    dimissToolTip: Boolean
) {

    val pointerVector = ImageVector.vectorResource(id = R.drawable.tip)
    val pointerTip = rememberVectorPainter(image = pointerVector)

    Canvas(modifier = modifier) {

        val width = size.width
        var startAngle = chartStartAngle

        var offsetX = 0f
        var offsetY = 0f
        var indexSelected = -1
        var angRad = -1f
        var arcW = -1f

        val halfRectWidth = rectWidth / 2
        val halfRectHeight = rectHeight / 2
        val halfTriangleWidth = triangleWidth / 2
        val halfTriangleHeight = triangleHeight / 2


        for (index in 0..chartDataList.lastIndex) {

            val chartData = chartDataList[index]
            val range = chartData.range
            val sweepAngle = range.endInclusive - range.start
            val angleInRadians = (startAngle + sweepAngle / 2).degreeToRadian

            val textMeasureResult = textMeasureResults[index]
            val textSize = textMeasureResult.size

            val currentStrokeWidth = outerStrokeWidth

            val arcWidth = outerStrokeWidth


            /*if (chartData.isSelected && !dimissToolTip)  {
                // Increase arc width for the first arc
                outerStrokeWidth + 50f // You can adjust the value as needed
            } else {
                // Keep the same arc width for other arcs
                outerStrokeWidth
            }*/

            if (startAngle <= currentSweepAngle) {

                val color = chartData.color
                val diff = (width / 2 - outerRadius) / outerRadius
                val fraction = (chartData.animatable.value - 1f) / diff

                val animatedColor = androidx.compose.ui.graphics.lerp(
                    color,
                    color.copy(alpha = .8f),
                    fraction
                )

                //val alphaValue  = if (chartData.isSelected) 1f  else 0.5f
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
                if (drawText && currentSweepAngle == chartEndAngle) {
                    val textCenter = textSize.center
                    drawText(
                        textLayoutResult = textMeasureResult,
                        color = Color(0xFF141414),
                        topLeft = Offset(
                            -textCenter.x + center.x
                                    + (innerRadius + currentStrokeWidth / 2) * cos(angleInRadians),
                            -textCenter.y + center.y
                                    + (innerRadius + currentStrokeWidth / 2) * sin(angleInRadians)
                        )
                    )
                }

                startAngle += sweepAngle
            }
        }

        // this is to make sure tool tip is drawn after all the arc segments
        // to prevent overlap
//        if (indexSelected != -1) {
//            val chartData = chartDataList[indexSelected]
//            val textMeasureResult = textMeasureResults[indexSelected]
//            val typeSize = textMeasureResult.first.size
//            val expenseSize = textMeasureResult.second.size
//            val typeCenter = typeSize.center
//            val expenseCenter = typeSize.center
//
//            if (!dimissToolTip && drawText && currentSweepAngle == chartEndAngle && chartData.isSelected) {
//
//                // Draw the tip
//                translate(
//                    left = offsetX - halfTriangleWidth.dp.toPx(),
//                    top = offsetY - halfTriangleHeight.dp.toPx()
//                ) {
//                    with(pointerTip) {
//                        draw(
//                            size = Size(triangleWidth.dp.toPx(), triangleHeight.dp.toPx())
//                        )
//                    }
//                }
//
//                // Draw rectangle
//                drawRoundRect(
//                    color = Color.Black,
//                    topLeft = Offset(
//                        offsetX - halfRectWidth.dp.toPx(),
//                        offsetY - rectHeight.dp.toPx() - halfTriangleHeight.dp.toPx()
//                    ),
//                    size = Size(rectWidth.dp.toPx(), rectHeight.dp.toPx()),
//                    style = Fill,
//                    cornerRadius = CornerRadius(rectCornerRadius.dp.toPx())
//                )
//
//                // Draw text centered
//                drawText(
//                    textLayoutResult = textMeasureResult.first,
//                    color = Color.White,
//                    topLeft = Offset(
//                        (-typeCenter.x + center.x
//                                + (innerRadius + arcW / 2) * cos(angRad)),
//                        (-typeCenter.y + center.y
//                                + (innerRadius + arcW / 2) * sin(angRad) - halfRectHeight.dp.toPx() - halfTriangleHeight.dp.toPx() - typeSize.height / 2)
//                    )
//                )
//
//                drawText(
//                    textLayoutResult = textMeasureResult.second,
//                    color = Color.White,
//                    topLeft = Offset(
//                        (-expenseCenter.x + center.x
//                                + (innerRadius + arcW / 2) * cos(angRad)),
//                        (-expenseSize.height / 2 + center.y
//                                + (innerRadius + arcW / 2) * sin(angRad) - halfRectHeight.dp.toPx() - halfTriangleHeight.dp.toPx() + expenseSize.height / 2 + 5.dp.toPx())
//                    )
//                )
//            }
//        }
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


