package com.example.composelearning.graphics


import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.composelearning.R
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun PieChartPreview(
    onClick: ((data: ChartData, index: Int) -> Unit)? = null,
) {
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
                .padding(top =100.dp)
                .size(140.dp)
                .align(Alignment.CenterHorizontally),
            data = data,
            outerRingPercent = 35,
            onClick = { data, index ->

            }
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
    onClick: ((data: ChartData, index: Int) -> Unit)? = null
) {

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center,
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
                                        onClick?.invoke(
                                            ChartData(
                                                color = chartData.color,
                                                data = chartData.data
                                            ), index
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
            drawText = drawText
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
    drawText: Boolean
) {

    val pointerVector = ImageVector.vectorResource(id = R.drawable.tip)
    val pointerTip = rememberVectorPainter(image = pointerVector)

    Canvas(modifier = modifier) {

        val width = size.width
        var startAngle = chartStartAngle

        var offsetX: Float = 0f
        var offsetY: Float = 0f
        var indexSelected = -1
        var angRad = -1f
        var arcW = -1f


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
                if (drawText && currentSweepAngle == chartEndAngle && chartData.isSelected) {
                    indexSelected = index
                    offsetX = center.x + (innerRadius + arcWidth / 2) * cos(angleInRadians)
                    offsetY = center.y + (innerRadius + arcWidth / 2) * sin(angleInRadians)
                    angRad = angleInRadians
                    arcW = arcWidth
                }

                startAngle += sweepAngle
            }
        }

        if (indexSelected != -1) {
            val chartData = chartDataList[indexSelected]
            val textMeasureResult = textMeasureResults[indexSelected]
            val textSize = textMeasureResult.size
            val textCenter = textSize.center

            if (drawText && currentSweepAngle == chartEndAngle && chartData.isSelected) {


                // Draw the tip
                translate(
                    left = offsetX - 12.dp.toPx(),
                    top = offsetY - 4.dp.toPx()
                ) {
                    with(pointerTip) {
                        draw(
                            size = Size(24.dp.toPx(), 8.dp.toPx())
                        )
                    }
                }

                // Draw rectangle with elevation
                drawRoundRect(
                    color = Color.Black,
                    topLeft = Offset(offsetX - 49.dp.toPx() , offsetY - 54.dp.toPx() - 4.dp.toPx()),
                    size = Size(99.dp.toPx(), 54.dp.toPx()),
                    style = Fill,
                    cornerRadius = CornerRadius(8.dp.toPx())

                )

                // Draw text centered
                drawText(
                    textLayoutResult = textMeasureResult,
                    color = Color.White,
                    topLeft = Offset(
                        (-textCenter.x + center.x
                                + (innerRadius + arcW / 2) * cos(angRad)),
                        (-textCenter.y + center.y
                                + (innerRadius + arcW / 2) * sin(angRad) - 100 - 8)
                    )
                )
            }
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

private fun DrawScope.drawTriangleTip(center: Offset, size: Float, color: Color) {
    val halfSize = size / 2
    val trianglePath = Path().apply {
        moveTo(center.x - halfSize, center.y - halfSize) // Move to the top-left corner
        lineTo(center.x + halfSize, center.y - halfSize) // Draw a line to the top-right corner
        lineTo(center.x, center.y + halfSize) // Draw a line to the bottom-center
        close()
    }
    drawPath(trianglePath, color)
}


// Draw the triangle tip
//                drawTriangleTip(
//                    center = Offset(
//                        offsetX,
//                        offsetY
//                    ),
//                    size = 16.dp.toPx(),
//                    color = Color.Black
//                )cal
