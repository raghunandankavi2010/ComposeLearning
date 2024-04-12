package com.example.composelearning.graphics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
@Composable
@Preview
fun Example(
    modifier: Modifier = Modifier,
    count: Int = 16,
    gapDegrees: Float = 30f / count,
    innerRadius: Dp = 120.dp,
    outerRadius: Dp = innerRadius * 1.25f,
    cornerRadius: Dp = (outerRadius - innerRadius) / 3,
    brush: Brush = SolidColor(Color.Red),
) {
    Canvas(modifier = modifier.size(outerRadius * 2)) {
        val path = Path()
        repeat(count) {
            path.addRoundedPolarBox(
                center = center,
                startAngleDegrees = it * 360f / count + gapDegrees / 2,
                sweepAngleDegrees = 360f / count - gapDegrees,
                innerRadius = innerRadius.toPx(),
                outerRadius = outerRadius.toPx(),
                cornerRadius = cornerRadius.toPx(),
            )
        }
        drawPath(path = path, brush = brush)
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
//    arcTo(
//        rect = Rect(
//            center = Offset(
//                x = (center.x + innerRadiusShift * cos((endAngleDegrees - innerAngleShift) * PI / 180)).toFloat(),
//                y = (center.y + innerRadiusShift * sin((endAngleDegrees - innerAngleShift) * PI / 180)).toFloat(),
//            ),
//            radius = 0f,
//        ),
//        startAngleDegrees = (endAngleDegrees - innerAngleShift + 180).toFloat(),
//        sweepAngleDegrees = (innerAngleShift - 90).toFloat(),
//        forceMoveTo = false,
//    )
//    arcTo(
//        rect = Rect(
//            center = Offset(
//                x = (center.x + outerRadiusShift * cos((endAngleDegrees - outerAngleShift) * PI / 180)).toFloat(),
//                y = (center.y + outerRadiusShift * sin((endAngleDegrees - outerAngleShift) * PI / 180)).toFloat(),
//            ),
//            radius = 0f,
//        ),
//        startAngleDegrees = (endAngleDegrees + 90).toFloat(),
//        sweepAngleDegrees = -(outerAngleShift + 90).toFloat(),
//        forceMoveTo = false,
//    )
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






