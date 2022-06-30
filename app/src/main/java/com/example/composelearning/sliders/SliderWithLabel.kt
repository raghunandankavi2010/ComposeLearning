
package com.example.composelearning.sliders

import android.content.res.Resources.getSystem
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.composelearning.ui.theme.ActiveTrackColor
import com.example.composelearning.ui.theme.InactiveTrackColor
import kotlin.math.sqrt


@Composable
fun SliderLabelDemo() {
    val sliderPosition = remember { mutableStateOf(0f) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        SliderWithLabel(
            value = sliderPosition.value,
            finiteEnd = true,
            valueRange = 0f..100f
        )
    }
}

@Composable
fun SliderWithLabel(
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    finiteEnd: Boolean,
    labelMinWidth: Dp = 30.dp,
) {
    var sliderPosition by remember { mutableStateOf(value) }

    Column {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            val offset = getSliderOffset(
                value = sliderPosition,
                valueRange = valueRange,
                boxWidth = maxWidth,
                labelWidth = labelMinWidth
            )
            val endValueText = if (!finiteEnd && sliderPosition >= valueRange.endInclusive)
                "${sliderPosition.toInt()} +" else sliderPosition.toInt().toString()

            SliderLabel(
                label = valueRange.start.toInt().toString(),
                minWidth = labelMinWidth,
                modifier = Modifier
            )

            if (sliderPosition > valueRange.start) {
                SliderLabel(
                    label = endValueText,
                    minWidth = labelMinWidth,
                    modifier = Modifier
                        .padding(start = offset)
                )
            }
        }

        Slider(
            onValueChangeFinished = {
                // do something on value change finished
                println(sliderPosition.toInt())
            },
            valueRange = 0f..100f,
            value = sliderPosition,
            onValueChange = { value ->
                sliderPosition = value
            },
            colors = SliderDefaults.colors(
                activeTrackColor = ActiveTrackColor,
                inactiveTrackColor = InactiveTrackColor
            )
        )
    }
}

@Composable
fun SliderLabel(
    modifier: Modifier = Modifier,
    label: String,
    minWidth: Dp = 24.dp
) {
    Text(
        text = label,
        textAlign = TextAlign.Center,
        color = Color.White,

        modifier = modifier
            .background(color = Color.Black).
                defaultMinSize(minWidth,minWidth)


    )
}

@Preview(showBackground = false)
@Composable
fun SliderPreview() {
    SliderLabel(label = "100", minWidth = 0.dp, modifier = Modifier.wrapContentSize())
}

private fun getSliderOffset(
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    boxWidth: Dp,
    labelWidth: Dp,
): Dp {
    val coerced = value.coerceIn(valueRange.start, valueRange.endInclusive)
    val positionFraction = calcFraction(valueRange.start, valueRange.endInclusive, coerced)

    return (boxWidth - labelWidth) * positionFraction
}

private fun calcFraction(a: Float, b: Float, pos: Float) =
    (if (b - a == 0f) 0f else (pos - a) / (b - a)).coerceIn(0f, 1f)

class BallonShape(private val cornerRadius: Float) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        return Outline.Generic(
            // Draw your custom path here
            path = drawBallonShape(size = size, mBubbleRadius = cornerRadius)
        )
    }

    private fun drawBallonShape(size: Size, mBubbleRadius: Float): Path {
        return Path().apply {
            val x0 = size.width / 2f
            val y0 = size.height - mBubbleRadius / 3f
            moveTo(x0, y0)
            val x1 = (size.width / 2f - sqrt(3.0) / 2f * mBubbleRadius).toFloat()
            val y1 = 3 / 2f * mBubbleRadius
            quadraticBezierTo(
                x1 - 2.px, y1 - 2.px,
                x1, y1
            )
            val rect = Rect(
                size.width / 2f - mBubbleRadius,
                0f,
                size.width / 2f + mBubbleRadius,
                2 * mBubbleRadius
            )
            arcTo(rect, 150f, 240f, forceMoveTo = false)
            val x2 = (size.width / 2f + sqrt(3f) / 2f * mBubbleRadius).toFloat()
            quadraticBezierTo(
                x2 + 2.px, y1 - 2.px,
                x0, y0
            )
            close()
        }
    }
}

val Int.px: Int get() = (this * getSystem().displayMetrics.density).toInt()


