package com.example.composelearning.graphics

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.composelearning.R

@Composable
fun ThermometerCanvas(modifier: Modifier = Modifier, progress: Int = 50) {
    val totalProgress = 161 - 32 - 20
    val actualProgress = progress  * (totalProgress) / 100 - (6*2)
    val vector = ImageVector.vectorResource(id = R.drawable.thermometer)
    val painter = rememberVectorPainter(image = vector)

    val vectorNum = ImageVector.vectorResource(id = R.drawable.number)
    val painterNum = rememberVectorPainter(image = vectorNum)

    val vectorLine = ImageVector.vectorResource(id = R.drawable.line)
    val painterLine = rememberVectorPainter(image = vectorLine)

    val animVal = remember { Animatable(0f) }
    LaunchedEffect(animVal) {
        animVal.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 10000, easing = LinearEasing)
        )
    }

    Canvas(
        modifier = modifier.fillMaxSize().padding(start = 50.dp, top = 50.dp)
    ) {

//        translate(left = 150.dp.toPx() - 25.dp.toPx()- 55.dp.toPx() - 40.dp.toPx(),
//            top = 150.dp.toPx() - 25.dp.toPx() - totalProgress.dp.toPx() ) {
//            with(painterNum) {
//                draw(
//                    size = Size(21.dp.toPx(), 121.dp.toPx())
//                )
//            }
//        }
//
//        translate(left = 150.dp.toPx() - 25.dp.toPx()- 55.dp.toPx() - 10.dp.toPx(),
//            top = 150.dp.toPx() - 20.dp.toPx() - totalProgress.dp.toPx() ) {
//            with(painterLine) {
//                draw(
//                    size = Size(12.dp.toPx(), 108.dp.toPx())
//                )
//            }
//        }


        translate(left = 150.dp.toPx() - 25.dp.toPx()- 55.dp.toPx(), top = 150.dp.toPx() - 25.dp.toPx() - totalProgress.dp.toPx()) {
            with(painter) {
                draw(
                    size = Size(50.dp.toPx(), 161.dp.toPx())
                )
            }

            // Drawing the circle at the bottom for filling the thermometer
            drawCircle(
                color = Color(0xFF25AB21),
                center = Offset(25.dp.toPx(), 161.dp.toPx() - 26.dp.toPx()),
                radius = 20.dp.toPx(),
            )

            // Drawing the line above the circle to indicate the meter
            drawLine(
                color = Color(0xFF25AB21),
                start = Offset(25.dp.toPx(), 161.dp.toPx() - 26.dp.toPx()),
                end = Offset(
                    25.dp.toPx(),
                    totalProgress.dp.toPx() - (actualProgress.dp.toPx() * animVal.value)
                ),
                strokeWidth = 15.dp.toPx(),
                StrokeCap.Round

            )
        }
    }
}

@Preview
@Composable
fun PreviewThermometerCanvas() {
    ThermometerCanvas()
}
