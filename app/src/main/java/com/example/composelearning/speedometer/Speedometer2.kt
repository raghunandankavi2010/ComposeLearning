package com.example.composelearning.speedometer


import android.graphics.Paint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
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
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.example.composelearning.LogCompositions
import com.example.composelearning.R
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin


@Composable
fun Speedometer2(
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


    val vector = ImageVector.vectorResource(id = R.drawable.arc)
    val painter = rememberVectorPainter(image = vector)

    val pointerVector = ImageVector.vectorResource(id = R.drawable.img)
    val pointer = rememberVectorPainter(image = pointerVector)

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
            .width(200.dp)
            .height(200.dp)
            .aspectRatio(1f),
        onDraw = {
            drawIntoCanvas {
                val w = drawContext.size.width
                val h = drawContext.size.height
                val centerOffset = Offset(w / 2f, h / 2f)

                translate(left = centerOffset.x - 75.dp.toPx(), top = centerOffset.y - 75.dp.toPx()) {
                    with(painter) {
                        draw(
                            size = Size(150.dp.toPx(), 150.dp.toPx())
                        )
                    }
                }

                rotate(progressAnimation.value * (arcDegrees) / 100f -  118f, pivot = Offset(centerOffset.x,centerOffset.y)) {
                    translate(
                        left = centerOffset.x - 70.dp.toPx(),
                        top = centerOffset.y - 70.dp.toPx()
                    ) {
                        with(pointer) {
                            draw(
                                size = Size(140.dp.toPx(), 140.dp.toPx())
                            )
                        }
                    }
                }
            }
        }
    )
}


