package com.example.composelearning.speedometer

import android.graphics.Paint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import com.example.composelearning.LogCompositions
import com.example.composelearning.R
import kotlinx.coroutines.launch
import kotlin.math.PI
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
                animationSpec = tween(durationMillis = 3000, easing = CustomEaseOutBounce))
        }
        launch {
            progressAnimation.animateTo(
                targetValue = progress.toFloat(),
                animationSpec = tween(durationMillis = 3000, easing = CustomEaseOutBounce))
        }
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        onDraw = {
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
                    drawLine(Color.Black,
                        Offset(x1.toFloat(), y1.toFloat()),
                        Offset(x2.toFloat(), y2.toFloat()),
                        5f,
                        StrokeCap.Square)

                    angleStart += 27.5f
                }
            }
        }
    )
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
fun Speedometer3(
    progress: Int,
) {
    LogCompositions("Speedometer", "Running")
    val arcDegrees = 180f
    val startArcAngle = 180f
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

    val context = LocalContext.current

    val fontFamily = remember {
        FontFamily(
            typeface = ResourcesCompat.getFont(context, R.font.jio_type_medium)!!
        )
    }

    LaunchedEffect(progress) {
        launch {
            pointerAnimation.animateTo(
                targetValue = endProgressInRadians.toFloat(),
                animationSpec = tween(durationMillis = 3000, easing = CustomEaseOutBounce))
        }
        launch {
            progressAnimation.animateTo(
                targetValue = progress.toFloat(),
                animationSpec = tween(durationMillis = 3000, easing = CustomEaseOutBounce))
        }
    }

    val vector = ImageVector.vectorResource(id = R.drawable.pointer_black)
    val painter = rememberVectorPainter(image = vector)

    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        onDraw = {
            drawIntoCanvas {
                val w = drawContext.size.width
                val h = drawContext.size.height
                val centerOffset = Offset(w / 2f, h / 2f)
                val quarterOffset = Offset(w / 4f, h / 4f)
                val centerArcSize = Size(w / 2f, h / 2f)
                val centerArcStroke = Stroke(80f, 0f, StrokeCap.Butt)
                drawArc(
                    Color.Gray,
                    startArcAngle,
                    arcDegrees,
                    false,
                    topLeft = quarterOffset,
                    size = centerArcSize,
                    style =  Stroke(80f, 0f, StrokeCap.Round)
                )


                drawArc(
                    Color.Red,
                    startArcAngle,
                    25 * (arcDegrees) / 100f,
                    false,
                    topLeft = quarterOffset,
                    size = centerArcSize,
                    style = Stroke(80f, 0f, StrokeCap.Round)
                )


                drawArc(
                    Color.Green,
                    startArcAngle + 45 ,
                    25 * (arcDegrees) / 100f,
                    false,
                    topLeft = quarterOffset,
                    size = centerArcSize,
                    style = centerArcStroke
                )

                drawArc(
                    Color.Blue,
                    startArcAngle + 90 ,
                    25 * (arcDegrees) / 100f,
                    false,
                    topLeft = quarterOffset,
                    size = centerArcSize,
                    style = centerArcStroke
                )
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
               rotate(progressAnimation.value * (arcDegrees) / 100f , pivot = Offset(centerOffset.x,centerOffset.y)) {
                    translate(
                        left = centerOffset.x  - 125.dp.toPx(),
                        top = centerOffset.y -  11.dp.toPx()
                    ) {
                        with(painter) {
                            draw(
                                size = Size(129.dp.toPx(),22.dp.toPx())
                            )
                        }
                    }
                }

               // drawCircle(Color.Black, 20f, centerOffset)
                // draw pointer
                val r = w / 4f
                val x = (r - 30f) * cos(pointerAnimation.value) + w / 2
                val y = (r - 30f) * sin(pointerAnimation.value) + h / 2

               // drawLine(Color.Black, Offset(w / 2, h / 2), Offset(x, y), 20f, StrokeCap.Round)
                var markerAngleRadians: Double
                var textValue = 0
                var angleStart = 180f
                val angleEnd = 360f
                // draw markers
                while (angleStart <= angleEnd) {
                    markerAngleRadians = angleStart * (PI / 180f)
                    val x1 = (r + 60f) * cos(markerAngleRadians) + w / 2
                    val y1 = (r  + 60f) * sin(markerAngleRadians) + h / 2
                    val x2 = (r + 80f) * cos(markerAngleRadians) + w / 2
                    val y2 = (r + 80f) * sin(markerAngleRadians) + h / 2
                    val tx = (r + 100f) * cos(markerAngleRadians) + w / 2
                    val ty = (r + 100f) * sin(markerAngleRadians) + h / 2


                   val rotationAngle = if(textValue == 0 || textValue == 180 || textValue == 90) {
                         0f
                    } else {
                        -45f
                   }

//                    rotate(rotationAngle , pivot = Offset(tx.toFloat(),ty.toFloat())) {
//                        //translate(left = tx.toFloat() / 2, top = ty.toFloat() / 2) {
//                            drawText(
//                                textMeasurer,
//                                text = "$textValue",
//                                topLeft = Offset(tx.toFloat()  , ty.toFloat()),
//                                style = TextStyle(fontSize = 16.sp, fontFamily = fontFamily)
//                            )
//                        }
//                   // }

                    //rotate(rotationAngle , pivot = Offset(tx.toFloat(),ty.toFloat())) {
                        drawContext.canvas.nativeCanvas.apply {
                            drawText(
                                "$textValue",
                                tx.toFloat(),
                                ty.toFloat(),
                                Paint().apply {
                                    textSize = 20.sp.toPx()
                                    color = android.graphics.Color.BLACK
                                    textAlign = Paint.Align.CENTER
                                }
                            )
                        }
                  // }
                    textValue += 45
                    drawLine(Color.Black,
                        Offset(x1.toFloat(), y1.toFloat()),
                        Offset(x2.toFloat(), y2.toFloat()),
                        5f,
                        StrokeCap.Square)

                    angleStart += 45
                }
            }
        }
    )
}

@Composable
fun measureTextWidth(text: String, style: TextStyle): Dp {
    val textMeasurer = rememberTextMeasurer()
    val widthInPixels = textMeasurer.measure(text, style).size.width
    return with(LocalDensity.current) { widthInPixels.toDp() }
}



