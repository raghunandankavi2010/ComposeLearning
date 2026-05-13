package com.example.composelearning.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class ThermometerSpec(
    val minValue: Float = 0f,
    val maxValue: Float = 100f,
    val coolColor: Color = Color(0xFF2196F3),
    val warmColor: Color = Color(0xFFFFC107),
    val hotColor: Color = Color(0xFFE53935),
    val warmThreshold: Float = 0.4f,
    val hotThreshold: Float = 0.75f,
    val tubeWidth: Dp = 24.dp,
    val bulbDiameter: Dp = 56.dp,
    val tickIntervals: Int = 5,
    val animate: Boolean = true,
    val showScale: Boolean = true,
)

/**
 * Material-themed thermometer that scales to whatever bounds the parent provides.
 *
 * Improvements over the legacy `ThermometerCanvas`:
 *   • No magic 161dp / 40dp hardcodes — sizes come from the measured canvas.
 *   • Value-driven color (cool → warm → hot) using the [ThermometerSpec] thresholds.
 *   • Vector-drawable independence — drawn entirely with primitives.
 *   • Accessibility: exposes the current reading via [Modifier.semantics].
 *   • Single Animatable shared across recompositions; animates whenever [value] changes.
 */
@Composable
fun ThermometerV2(
    value: Float,
    modifier: Modifier = Modifier,
    spec: ThermometerSpec = ThermometerSpec(),
    theme: ChartTheme = ChartDefaults.theme(),
    label: String? = null,
) {
    val anim = remember { Animatable(initialValue = value) }
    LaunchedEffect(value, spec.minValue, spec.maxValue, spec.animate) {
        if (spec.animate) {
            anim.animateTo(value, animationSpec = tween(900, easing = FastOutSlowInEasing))
        } else {
            anim.snapTo(value)
        }
    }
    val measurer = rememberTextMeasurer()

    val fraction = ((anim.value - spec.minValue) / (spec.maxValue - spec.minValue))
        .coerceIn(0f, 1f)
    val color = when {
        fraction >= spec.hotThreshold -> spec.hotColor
        fraction >= spec.warmThreshold -> spec.warmColor
        else -> spec.coolColor
    }

    Box(
        modifier = modifier
            .semantics { contentDescription = "${label ?: "Thermometer"}: ${anim.value.toInt()}" },
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            val tube = spec.tubeWidth.toPx()
            val bulb = spec.bulbDiameter.toPx()
            val centerX = if (spec.showScale) size.width - bulb / 2f - 16.dp.toPx() else size.width / 2f
            val bulbCenterY = size.height - bulb / 2f - 4.dp.toPx()
            val tubeTopY = 8.dp.toPx()
            val tubeBottomY = bulbCenterY - bulb * 0.3f
            val tubeHeight = tubeBottomY - tubeTopY

            // Outer tube outline
            drawRoundRect(
                color = theme.gridColor,
                topLeft = Offset(centerX - tube / 2f - 4.dp.toPx(), tubeTopY - 6.dp.toPx()),
                size = Size(tube + 8.dp.toPx(), tubeHeight + 12.dp.toPx()),
                cornerRadius = CornerRadius(tube / 2f + 4.dp.toPx(), tube / 2f + 4.dp.toPx()),
                style = Stroke(width = 1.5.dp.toPx()),
            )
            // Tube background (light tube)
            drawRoundRect(
                color = theme.surface,
                topLeft = Offset(centerX - tube / 2f, tubeTopY),
                size = Size(tube, tubeHeight),
                cornerRadius = CornerRadius(tube / 2f, tube / 2f),
            )

            // Bulb outline + background
            drawCircle(theme.gridColor, radius = bulb / 2f + 2.dp.toPx(), center = Offset(centerX, bulbCenterY), style = Stroke(1.5.dp.toPx()))
            drawCircle(color.copy(alpha = 0.25f), radius = bulb / 2f, center = Offset(centerX, bulbCenterY))

            // Mercury column
            val filledHeight = tubeHeight * fraction
            val mercuryTop = tubeBottomY - filledHeight
            val brush = Brush.verticalGradient(
                colors = listOf(color, color.copy(alpha = 0.85f)),
                startY = mercuryTop,
                endY = tubeBottomY,
            )
            drawRoundRect(
                brush = brush,
                topLeft = Offset(centerX - tube / 2f, mercuryTop),
                size = Size(tube, filledHeight),
                cornerRadius = CornerRadius(tube / 2f, tube / 2f),
            )

            // Bulb mercury
            drawCircle(color = color, radius = bulb / 2f - 4.dp.toPx(), center = Offset(centerX, bulbCenterY))
            // Specular highlight on bulb
            drawCircle(
                color = Color.White.copy(alpha = 0.35f),
                radius = bulb / 5f,
                center = Offset(centerX - bulb / 6f, bulbCenterY - bulb / 6f),
            )

            // Tick marks + labels
            if (spec.showScale) {
                val ticks = spec.tickIntervals.coerceAtLeast(2)
                for (i in 0..ticks) {
                    val t = i / ticks.toFloat()
                    val y = tubeBottomY - tubeHeight * t
                    val v = spec.minValue + (spec.maxValue - spec.minValue) * t
                    drawLine(
                        color = theme.axisColor,
                        start = Offset(centerX - tube / 2f - 14.dp.toPx(), y),
                        end = Offset(centerX - tube / 2f - 6.dp.toPx(), y),
                        strokeWidth = 1.5.dp.toPx(),
                    )
                    val text = v.toInt().toString()
                    val layout = measurer.measure(text, style = theme.axisLabelStyle)
                    drawText(
                        textLayoutResult = layout,
                        topLeft = Offset(
                            centerX - tube / 2f - 18.dp.toPx() - layout.size.width,
                            y - layout.size.height / 2f,
                        ),
                    )
                }
            }
        }

        if (label != null) {
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(text = label, style = theme.titleStyle)
                Text(
                    text = "${anim.value.toInt()}",
                    style = theme.titleStyle.copy(fontSize = theme.titleStyle.fontSize * 1.6f),
                )
            }
        }
    }
}