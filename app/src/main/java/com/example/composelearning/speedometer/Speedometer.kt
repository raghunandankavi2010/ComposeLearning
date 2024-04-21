package com.example.composelearning.speedometer

import android.graphics.Paint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.example.composelearning.LogCompositions
import com.example.composelearning.R
import com.example.composelearning.customshapes.dpToPx

import com.example.composelearning.graphics.drawRoundedRightEndArc
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin


@Composable
fun Speedometer(
    progress: Int,
) {
    LogCompositions("Speedometer", "Running")
    val arcDegrees = 275
    val startArcAngle = 135f
    val startAngleRadians = startArcAngle * (PI / 180f)
    val progressInRadians = (progress * (arcDegrees) / 100f) * (PI / 180f)
    val endProgressInRadians = startAngleRadians + progressInRadians
    // remember
    val progressAnimation = remember {
        Animatable(0f)
    }
    val pointerAnimation = remember {
        Animatable(startAngleRadians.toFloat())
    }

    LaunchedEffect(progress) {
        launch {
            pointerAnimation.animateTo(
                targetValue = endProgressInRadians.toFloat(),
                animationSpec = tween(durationMillis = 3000, easing = CustomEaseOutBounce)
            )
        }
        launch {
            progressAnimation.animateTo(
                targetValue = progress.toFloat(),
                animationSpec = tween(durationMillis = 3000, easing = CustomEaseOutBounce)
            )
        }
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        drawIntoCanvas {
            val w = drawContext.size.width
            val h = drawContext.size.height
            val centerOffset = Offset(w / 2f, h / 2f)
            val quarterOffset = Offset(w / 4f, h / 4f)
            // Drawing Center Arc background
            val (mainColor, secondaryColor) = when {
                progressAnimation.value < 50 -> // Red
                    Color(0xFFD32F2F) to Color(0xFFFFCDD2)

                progressAnimation.value < 80 -> // Orange
                    Color(0xFFF57C00) to Color(0xFFFFE0B2)

                else -> // Green
                    Color(0xFF388E3C) to Color(0xFFC8E6C9)
            }
            val centerArcSize = Size(w / 2f, h / 2f)
            val centerArcStroke = Stroke(20f, 0f, StrokeCap.Butt)
            drawArc(
                secondaryColor,
                startArcAngle,
                arcDegrees.toFloat(),
                false,
                topLeft = quarterOffset,
                size = centerArcSize,
                style = centerArcStroke
            )
            // Drawing Center Arc progress
            drawArc(
                mainColor,
                startArcAngle,
                progressAnimation.value * (arcDegrees) / 100f,
                false,
                topLeft = quarterOffset,
                size = centerArcSize,
                style = centerArcStroke
            )
            // Drawing the pointer circle
            drawCircle(mainColor, 20f, centerOffset)
            // draw pointer
            val r = w / 4f
            val x = (r - 30f) * cos(pointerAnimation.value) + w / 2
            val y = (r - 30f) * sin(pointerAnimation.value) + h / 2

            drawLine(mainColor, Offset(w / 2, h / 2), Offset(x, y), 20f, StrokeCap.Round)
            var markerAngleRadians: Double
            var textValue = 0
            var angleStart = 135f
            val angleEnd = 410f
            // draw markers
            while (angleStart <= angleEnd) {
                markerAngleRadians = angleStart * (PI / 180f)
                val x1 = (r - 20f) * cos(markerAngleRadians) + w / 2
                val y1 = (r - 20f) * sin(markerAngleRadians) + h / 2
                val x2 = (r + 20f) * cos(markerAngleRadians) + w / 2
                val y2 = (r + 20f) * sin(markerAngleRadians) + h / 2
                val tx = (r + 40f) * cos(markerAngleRadians) + w / 2
                val ty = (r + 40f) * sin(markerAngleRadians) + h / 2

                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        "$textValue",
                        tx.toFloat(),
                        ty.toFloat(),
                        Paint().apply {
                            textSize = 20f
                            color = android.graphics.Color.BLACK
                            textAlign = Paint.Align.CENTER
                        }
                    )
                }
                textValue += 10
                drawLine(
                    Color.Black,
                    Offset(x1.toFloat(), y1.toFloat()),
                    Offset(x2.toFloat(), y2.toFloat()),
                    5f,
                    StrokeCap.Square
                )

                angleStart += 27.5f
            }
        }
    }
}

val EaseOutBounce: Easing = Easing { fraction ->
    val n1 = 7.5625f
    val d1 = 2.75f
    var newFraction = fraction

    return@Easing if (newFraction < 1f / d1) {
        n1 * newFraction * newFraction
    } else if (newFraction < 2f / d1) {
        newFraction -= 1.5f / d1
        n1 * newFraction * newFraction + 0.75f
    } else if (newFraction < 2.5f / d1) {
        newFraction -= 2.25f / d1
        n1 * newFraction * newFraction + 0.9375f
    } else {
        newFraction -= 2.625f / d1
        n1 * newFraction * newFraction + 0.984375f
    }
}

val CustomEaseOutBounce: Easing = Easing { fraction ->
    return@Easing if (fraction < 0.61f) {
        2.77f * fraction * fraction
    } else {
        1.250f * fraction * fraction - 2 * fraction + 1.750f
    }

}


@Composable
fun SpeedometerTry(
    modifier: Modifier = Modifier,
    progress: Int,
) {

    BoxWithConstraints(modifier = Modifier
        .padding(top = 25.dp)
        .width(300.dp)
        .height(130.dp)) {

        LogCompositions("Speedometer", "Running")
        val textMeasurer = rememberTextMeasurer()

        val arcDegrees = 180f
        val startArcAngle = 180f

        // remember
        val progressAnimation = remember {
            Animatable(0f)
        }

        LaunchedEffect(progress) {

            launch {
                progressAnimation.animateTo(
                    targetValue = progress.toFloat(),
                    animationSpec = tween(durationMillis = 3000, easing = CustomEaseOutBounce)
                )
            }
        }
        val innerRadius = 60.dp.dpToPx()
        val outerRadius = 114.dp.dpToPx()
        val cornerRadius = 12.dp.dpToPx() //(outerRadius - innerRadius) /


        val drawEntireArc = remember {
            Path().apply {
                drawRoundedRightEndArc(
                    center = Offset(constraints.maxWidth / 2f, constraints.maxHeight.toFloat()),
                    startAngleDegrees = startArcAngle,
                    sweepAngleDegrees = 180f,
                    innerRadius = innerRadius,
                    outerRadius = outerRadius,
                    cornerRadius = cornerRadius,

                )
            }
        }

//        val redPath = remember {
//            Path().apply {
//                addRoundedPolarBox(
//                    center = Offset(constraints.maxWidth / 2f, constraints.maxHeight.toFloat()),
//                    startAngleDegrees = startArcAngle,
//                    sweepAngleDegrees = 25 * (arcDegrees) / 100f,
//                    innerRadius = innerRadius,
//                    outerRadius = outerRadius,
//                    cornerRadius = cornerRadius,
//                )
//            }
//        }
//
//        val yellow = remember {
//            Path().apply {
//                addRoundedPolarBox(
//                    center = Offset(constraints.maxWidth / 2f, constraints.maxHeight.toFloat()),
//                    startAngleDegrees = startArcAngle + 45,
//                    sweepAngleDegrees = 50 * (arcDegrees) / 100f,
//                    innerRadius = innerRadius,
//                    outerRadius = outerRadius,
//                    cornerRadius = 0.1f
//                )
//            }
//        }
//
//        val green = remember {
//            Path().apply {
//                // Modified for flat top and rounded bottom
//                addRoundedPolarBoxEnd(
//                    center = Offset(constraints.maxWidth / 2f, constraints.maxHeight.toFloat()),
//                    startAngleDegrees = startArcAngle + 135,
//                    sweepAngleDegrees = 25 * (arcDegrees) / 100f,
//                    innerRadius = innerRadius,
//                    outerRadius = outerRadius,
//                    cornerRadius = cornerRadius
//                )
//            }
//        }

//        val green = remember {
//            Path().apply {
//                addRoundedPolarBox123(
//                    center = Offset(constraints.maxWidth / 2f, constraints.maxHeight.toFloat()),
//                    startAngleDegrees = 360f,
//                    sweepAngleDegrees = -(25 * (arcDegrees) / 100f),
//                    innerRadius = innerRadius,
//                    outerRadius = outerRadius,
//                    cornerRadius = cornerRadius
//                )
//            }
//
//        }

        val vector = ImageVector.vectorResource(id = R.drawable.pointer_black)
        val painter = rememberVectorPainter(image = vector)

        Canvas(
            modifier = Modifier
                .aspectRatio(1f),
            onDraw = {
                drawIntoCanvas {
                    val w = drawContext.size.width
                    val h = drawContext.size.height
                    val centerOffset =
                        Offset(constraints.maxWidth / 2f, constraints.maxHeight.toFloat())

                    drawPath(drawEntireArc, Color.Green)
//                    drawPath(redPath, Color(0xFFE30513))
//                    drawPath(yellow, Color(0xFFF7AB20))
//                    rotate(
//                        -180f,
//                        pivot = Offset(0f, 0f)
//                    ) {
//                        drawPath(green,Color(0xFF25AB21))
//                    }

                    rotate(
                        progressAnimation.value * (arcDegrees) / 100f,
                        pivot = Offset(centerOffset.x, centerOffset.y)
                    ) {
                        translate(
                            left = centerOffset.x - 125.dp.toPx(),
                            top = centerOffset.y - 11.dp.toPx()
                        ) {
                            with(painter) {
                                draw(
                                    size = Size(129.dp.toPx(), 22.dp.toPx())
                                )
                            }
                        }
                    }

                }
            }
        )
    }
}
@Composable
fun Speedometer3(
    modifier: Modifier = Modifier,
    progress: Int,
) {

    BoxWithConstraints(modifier = Modifier
        .fillMaxWidth()
        .height(130.dp)) {

        LogCompositions("Speedometer", "Running")
        val textMeasurer = rememberTextMeasurer()

        val arcDegrees = 180f
        val startArcAngle = 180f

        // remember
        val progressAnimation = remember {
            Animatable(0f)
        }

        LaunchedEffect(progress) {
            //launch {
                progressAnimation.animateTo(
                    targetValue = progress.toFloat(),
                    animationSpec = tween(durationMillis = 3000, easing = CustomEaseOutBounce)
                )
            //}
        }
        val innerRadius = 60.dp.dpToPx()
        val outerRadius = 114.dp.dpToPx()
        val cornerRadius = 12.dp.dpToPx()


        val drawEntireArc = remember {
            Path().apply {
                addRoundedPolarBoxAllSides(
                    center = Offset(constraints.maxWidth / 2f, constraints.maxHeight.toFloat()),
                    startAngleDegrees = startArcAngle,
                    sweepAngleDegrees = 180f,
                    innerRadius = innerRadius,
                    outerRadius = outerRadius,
                    cornerRadius = cornerRadius
                )
            }
        }

        val redPath = remember {
            Path().apply {
                addRoundedPolarBox(
                    center = Offset(constraints.maxWidth / 2f, constraints.maxHeight.toFloat()),
                    startAngleDegrees = startArcAngle,
                    sweepAngleDegrees = 25 * (arcDegrees) / 100f,
                    innerRadius = innerRadius,
                    outerRadius = outerRadius,
                    cornerRadius = cornerRadius,
                )
            }
        }

        val yellow = remember {
            Path().apply {
                addRoundedPolarBoxAllSides(
                    center = Offset(constraints.maxWidth / 2f, constraints.maxHeight.toFloat()),
                    startAngleDegrees = startArcAngle + 45,
                    sweepAngleDegrees = 50 * (arcDegrees) / 100f,
                    innerRadius = innerRadius,
                    outerRadius = outerRadius,
                    cornerRadius = 0.1f
                )
            }
        }

        val green = remember {
            Path().apply {
                addRoundedEndBox(
                    center = Offset(constraints.maxWidth / 2f, constraints.maxHeight.toFloat()),
                    startAngleDegrees = startArcAngle + 135,
                    sweepAngleDegrees = 25 * (arcDegrees) / 100f,
                    innerRadius = innerRadius,
                    outerRadius = outerRadius,
                    cornerRadius = cornerRadius
                )
            }
        }


        val vector = ImageVector.vectorResource(id = R.drawable.pointer_black)
        val painter = rememberVectorPainter(image = vector)

        Canvas(
            modifier = Modifier
                .aspectRatio(1f),
            onDraw = {
                drawIntoCanvas {
                    val w = drawContext.size.width
                    val h = drawContext.size.height
                    val centerOffset =
                        Offset(constraints.maxWidth / 2f, constraints.maxHeight.toFloat())

                    //drawPath(drawEntireArc, Color.Gray)
                    drawPath(redPath, Color(0xFFE30513))
                    drawPath(yellow, Color(0xFFF7AB20))
                    drawPath(green,Color(0xFF25AB21))

                    // Just for reference to see the center of the canvas
                    //drawCircle(Color.Green, center = centerOffset, radius = 20f)


                    rotate(
                        progressAnimation.value * (arcDegrees) / 100f,
                        pivot = Offset(centerOffset.x, centerOffset.y)
                    ) {
                        translate(
                            left = centerOffset.x - 111.dp.toPx(),
                            top = centerOffset.y - 11.dp.toPx()
                        ) {
                            with(painter) {
                                draw(
                                    size = Size(111.dp.toPx(), 22.dp.toPx())
                                )
                            }
                        }
                    }

                }
            }
        )
    }
}

fun Path.addRoundedPolarBox(
    center: Offset,
    startAngleDegrees: Float,
    sweepAngleDegrees: Float,
    innerRadius: Float,
    outerRadius: Float,
    cornerRadius: Float,
) {
    val endAngleDegrees = startAngleDegrees + sweepAngleDegrees.toDouble()
    val innerRadiusShift = innerRadius + cornerRadius.toDouble()
    val innerAngleShift = asin(cornerRadius / innerRadiusShift) * 180 / PI
    val outerRadiusShift = outerRadius - cornerRadius.toDouble()
    val outerAngleShift = asin(cornerRadius / outerRadiusShift) * 180 / PI
    arcTo(
        rect = Rect(
            center = Offset(
                x = (center.x + innerRadiusShift * cos((startAngleDegrees + innerAngleShift) * PI / 180)).toFloat(),
                y = (center.y + innerRadiusShift * sin((startAngleDegrees + innerAngleShift) * PI / 180)).toFloat(),
            ),
            radius = cornerRadius,
        ),
        startAngleDegrees = startAngleDegrees - 90,
        sweepAngleDegrees = (innerAngleShift - 90).toFloat(),
        forceMoveTo = true,
    )
    arcTo(
        rect = Rect(center = center, radius = innerRadius),
        startAngleDegrees = (startAngleDegrees + innerAngleShift).toFloat(),
        sweepAngleDegrees = (sweepAngleDegrees - innerAngleShift).toFloat(),
        forceMoveTo = false,
    )

    arcTo(
        rect = Rect(center = center, radius = outerRadius),
        startAngleDegrees = (endAngleDegrees ).toFloat(),
        sweepAngleDegrees = -(sweepAngleDegrees - 2 * outerAngleShift).toFloat(),
        forceMoveTo = false,
    )
    arcTo(
        rect = Rect(
            center = Offset(
                x = (center.x + outerRadiusShift * cos((startAngleDegrees + outerAngleShift) * PI / 180)).toFloat(),
                y = (center.y + outerRadiusShift * sin((startAngleDegrees + outerAngleShift) * PI / 180)).toFloat(),
            ),
            radius = cornerRadius,
        ),
        startAngleDegrees = (startAngleDegrees + outerAngleShift).toFloat(),
        sweepAngleDegrees = -(outerAngleShift + 90).toFloat(),
        forceMoveTo = false,
    )
    close()
}



fun Path.addRoundedPolarBoxAllSides(
    center: Offset,
    startAngleDegrees: Float,
    sweepAngleDegrees: Float,
    innerRadius: Float,
    outerRadius: Float,
    cornerRadius: Float,
) {
    val endAngleDegrees = startAngleDegrees + sweepAngleDegrees.toDouble()
    val innerRadiusShift = innerRadius + cornerRadius.toDouble()
    val innerAngleShift = asin(cornerRadius / innerRadiusShift) * 180 / PI
    val outerRadiusShift = outerRadius - cornerRadius.toDouble()
    val outerAngleShift = asin(cornerRadius / outerRadiusShift) * 180 / PI
    arcTo(
        rect = Rect(
            center = Offset(
                x = (center.x + innerRadiusShift * cos((startAngleDegrees + innerAngleShift) * PI / 180)).toFloat(),
                y = (center.y + innerRadiusShift * sin((startAngleDegrees + innerAngleShift) * PI / 180)).toFloat(),
            ),
            radius = cornerRadius,
        ),
        startAngleDegrees = startAngleDegrees - 90,
        sweepAngleDegrees = (innerAngleShift - 90).toFloat(),
        forceMoveTo = true,
    )
    arcTo(
        rect = Rect(center = center, radius = innerRadius),
        startAngleDegrees = (startAngleDegrees + innerAngleShift).toFloat(),
        sweepAngleDegrees = (sweepAngleDegrees - innerAngleShift).toFloat(),
        forceMoveTo = false,
    )
    arcTo(
        rect = Rect(
            center = Offset(
                x = (center.x + innerRadiusShift * cos((endAngleDegrees - innerAngleShift) * PI / 180)).toFloat(),
                y = (center.y + innerRadiusShift * sin((endAngleDegrees - innerAngleShift) * PI / 180)).toFloat(),
            ),
            radius = cornerRadius,
        ),
        startAngleDegrees = (endAngleDegrees - innerAngleShift + 180).toFloat(),
        sweepAngleDegrees = (innerAngleShift - 90).toFloat(),
        forceMoveTo = false,
    )
    arcTo(
        rect = Rect(
            center = Offset(
                x = (center.x + outerRadiusShift * cos((endAngleDegrees - outerAngleShift) * PI / 180)).toFloat(),
                y = (center.y + outerRadiusShift * sin((endAngleDegrees - outerAngleShift) * PI / 180)).toFloat(),
            ),
            radius = cornerRadius,
        ),
        startAngleDegrees = (endAngleDegrees + 90).toFloat(),
        sweepAngleDegrees = -(outerAngleShift + 90).toFloat(),
        forceMoveTo = false,
    )
    arcTo(
        rect = Rect(center = center, radius = outerRadius),
        startAngleDegrees = (endAngleDegrees ).toFloat(),
        sweepAngleDegrees = -(sweepAngleDegrees - 2 * outerAngleShift).toFloat(),
        forceMoveTo = false,
    )
    arcTo(
        rect = Rect(
            center = Offset(
                x = (center.x + outerRadiusShift * cos((startAngleDegrees + outerAngleShift) * PI / 180)).toFloat(),
                y = (center.y + outerRadiusShift * sin((startAngleDegrees + outerAngleShift) * PI / 180)).toFloat(),
            ),
            radius = cornerRadius,
        ),
        startAngleDegrees = (startAngleDegrees + outerAngleShift).toFloat(),
        sweepAngleDegrees = -(outerAngleShift + 90).toFloat(),
        forceMoveTo = false,
    )
    close()
}


fun Path.addRoundedEndBox(
    center: Offset,
    startAngleDegrees: Float,
    sweepAngleDegrees: Float,
    innerRadius: Float,
    outerRadius: Float,
    cornerRadius: Float,
) {
    val endAngleDegrees = startAngleDegrees + sweepAngleDegrees.toDouble()
    val innerRadiusShift = innerRadius + cornerRadius.toDouble()
    // the length of the arc at inner radius arc
    val innerAngleShift = asin(cornerRadius / innerRadiusShift) * 180 / PI
    val outerRadiusShift = outerRadius - cornerRadius.toDouble()
    // the length of the arc at outer radius arc
    val outerAngleShift = asin(cornerRadius / outerRadiusShift) * 180 / PI
    // start inner arc no rounded  it left bottom arc
    arcTo(
        rect = Rect(
            center = Offset(
                x = (center.x + innerRadiusShift * cos((startAngleDegrees ) * PI / 180)).toFloat(),
                y = (center.y + innerRadiusShift * sin((startAngleDegrees ) * PI / 180)).toFloat(),
            ),
            radius = 0.1f,
        ),
        startAngleDegrees = startAngleDegrees ,
        sweepAngleDegrees = (90).toFloat(),
        forceMoveTo = true,
    )
    // arc from start to sweep minus the arc part ie start to sweep angle arc
    arcTo(
        rect = Rect(center = center, radius = innerRadius),
        startAngleDegrees = (startAngleDegrees ).toFloat(),
        sweepAngleDegrees = (sweepAngleDegrees - 2 * innerAngleShift).toFloat(),
        forceMoveTo = false,
    )
    // arc of inner circle which is at the end of arc ie left bottom arc
    arcTo(
        rect = Rect(
            center = Offset(
                x = (center.x + innerRadiusShift * cos((endAngleDegrees - innerAngleShift) * PI / 180)).toFloat(),
                y = (center.y + innerRadiusShift * sin((endAngleDegrees - innerAngleShift) * PI / 180)).toFloat(),
            ),
            radius = cornerRadius,
        ),
        startAngleDegrees = (endAngleDegrees - innerAngleShift + 180).toFloat(),
        sweepAngleDegrees = (innerAngleShift - 90).toFloat(),
        forceMoveTo = false,
    )
    // out arc with rounded arc ie right bottom arc
    arcTo(
        rect = Rect(
            center = Offset(
                x = (center.x + outerRadiusShift * cos((endAngleDegrees - outerAngleShift) * PI / 180)).toFloat(),
                y = (center.y + outerRadiusShift * sin((endAngleDegrees - outerAngleShift) * PI / 180)).toFloat(),
            ),
            radius = cornerRadius,
        ),
        startAngleDegrees = (endAngleDegrees + 90).toFloat(),
        sweepAngleDegrees = -(outerAngleShift + 90).toFloat(),
        forceMoveTo = false,
    )
    // left top arc
    arcTo(
        rect = Rect(center = center, radius = outerRadius),
        startAngleDegrees = (endAngleDegrees - innerAngleShift ).toFloat(),
        sweepAngleDegrees = -(sweepAngleDegrees-  innerAngleShift).toFloat(),
        forceMoveTo = false,
    )

    close()
}

//drawCircle(Color.Black, 5f, centerOffset)
// draw pointer
//                val r = w / 4f
//                val x = (r - 30f) * cos(pointerAnimation.value) + w / 2
//                val y = (r - 30f) * sin(pointerAnimation.value) + h / 2
//
//                // drawLine(Color.Black, Offset(w / 2, h / 2), Offset(x, y), 20f, StrokeCap.Round)
//                var markerAngleRadians: Double
//                var textValue = 0
//                var angleStart = 180f
//                val angleEnd = 360f
//                // draw markers
//                while (angleStart <= angleEnd) {
//                    markerAngleRadians = angleStart * (PI / 180f)
//                    val x1 = (r + 60f) * cos(markerAngleRadians) + w / 2
//                    val y1 = (r + 60f) * sin(markerAngleRadians) + h / 2
//                    val x2 = (r + 80f) * cos(markerAngleRadians) + w / 2
//                    val y2 = (r + 80f) * sin(markerAngleRadians) + h / 2
//                    val tx = (r + 120f) * cos(markerAngleRadians) + w / 2
//                    val ty = (r + 120f) * sin(markerAngleRadians) + h / 2
//
//                    val textLayoutResult = textMeasurer.measure(
//                        AnnotatedString("$textValue")
//                    )
//
//                    val tSize = textLayoutResult.size
//                    //rotate(-markerAngleRadians.toFloat(), pivot = Offset(tx.toFloat(), ty.toFloat())) {
//                        drawText(
//                            textMeasurer = textMeasurer,
//                            text = "$textValue",
//                            topLeft = Offset(
//                                tx.toFloat() - (tSize.width) / 2f,
//                                ty.toFloat() - (tSize.height) / 2f
//                            ),
//                        )
//                   // }
//
//                    //rotate(rotationAngle , pivot = Offset(tx.toFloat(),ty.toFloat())) {
////                    drawContext.canvas.nativeCanvas.apply {
////                        drawText(
////                            "$textValue",
////                            tx.toFloat() - (tSize.width) / 2f,
////                            ty.toFloat() ,
////                            Paint().apply {
////                                textSize = 20.sp.toPx()
////                                color = android.graphics.Color.BLACK
////                                textAlign = Paint.Align.CENTER
////                            }
////                        )
////                    }
//                    // }
//                    textValue += 45
//                    drawLine(
//                        Color.Black,
//                        Offset(x1.toFloat(), y1.toFloat()),
//                        Offset(x2.toFloat(), y2.toFloat()),
//                        5f,
//                        StrokeCap.Square
//                    )
//
//                    angleStart += 45
//                }


//                drawArc(
//                    Color.Blue,
//                    startArcAngle + 90,
//                    25 * (arcDegrees) / 100f,
//                    false,
//                    topLeft = quarterOffset,
//                    size = centerArcSize,
//                    style = centerArcStroke
//                )


//                drawArc(
//                    Color.Red,
//                    startArcAngle,
//                    25 * (arcDegrees) / 100f,
//                    false,
//                    topLeft = quarterOffset,
//                    size = centerArcSize,
//                    style = Stroke(80f, 0f, StrokeCap.Round)
//                )


//                drawArc(
//                    Color.Green,
//                    startArcAngle + 45,
//                    25 * (arcDegrees) / 100f,
//                    false,
//                    topLeft = quarterOffset,
//                    size = centerArcSize,
//                    style = centerArcStroke
//                )

//                drawArc(
//                    Color.Blue,
//                    startArcAngle + 90,
//                    25 * (arcDegrees) / 100f,
//                    false,
//                    topLeft = quarterOffset,
//                    size = centerArcSize,
//                    style = centerArcStroke
//                )
// Drawing Center Arc progress
//                drawArc(
//                    mainColor,
//                    startArcAngle,
//                    progressAnimation.value * (arcDegrees) / 100f,
//                    false,
//                    topLeft = quarterOffset,
//                    size = centerArcSize,
//                    style = centerArcStroke
//                )
// Drawing the pointer circle

//val markerAngleRadians = 25 * (PI / 180f)
//val tx = ((innerRadius + outerRadius + 25.dp.toPx()) * cos( markerAngleRadians))
//val ty = ((innerRadius + outerRadius +  5.dp.toPx()) * sin( markerAngleRadians))
//
//val textLayoutResult = textMeasurer.measure(
//    AnnotatedString("25")
//)
//
//val tSize = textLayoutResult.size
//
//
//drawText(
//textMeasurer = textMeasurer,
//text = "25",
//topLeft = Offset(
//tx.toFloat() - (tSize.width) / 2f,
//ty.toFloat() - (tSize.height) / 2f
//),
//)
