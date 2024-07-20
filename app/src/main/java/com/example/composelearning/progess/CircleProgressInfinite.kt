package com.example.composelearning.progess

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Green
import androidx.compose.ui.graphics.Color.Companion.LightGray
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import com.example.composelearning.R

@Composable
fun CircleProgressInfinite() {

    CircularProgressIndicator(
        modifier = Modifier.size(100.dp),
        color = Color.Green,
        strokeWidth = 10.dp)

    val transition = rememberInfiniteTransition()
    val currentRotation by transition.animateValue(
        0F,
        targetValue = 360F,
        Float.VectorConverter,
        infiniteRepeatable(
            animation = tween(
                durationMillis = 5000,
                easing = LinearEasing
            )
        )
    )

    val strokeWidth = dimensionResource(R.dimen.stroke)
    Box(modifier = Modifier
        .padding(16.dp)
        .size(100.dp)
        .drawBehind {

            //background fixed circle
            drawCircle(
                color = LightGray,
                radius = size.width / 2 - strokeWidth.toPx() / 2,
                style = Stroke(strokeWidth.toPx())
            )

            val diameterOffset = strokeWidth.toPx() / 2
            val arcDimen = size.width - 2 * diameterOffset

            //arc with indeterminate animation
            rotate(currentRotation) {
                drawArc(
                    color = Green,
                    startAngle = 45F,
                    sweepAngle = 90F,
                    useCenter = false,
                    topLeft = Offset(diameterOffset, diameterOffset),
                    size = Size(arcDimen, arcDimen),
                    style = Stroke(strokeWidth.toPx())
                )
            }
        }

    )
}