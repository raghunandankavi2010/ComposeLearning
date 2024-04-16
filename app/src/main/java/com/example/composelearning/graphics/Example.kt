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
import com.example.composelearning.customshapes.dpToPx
import com.example.composelearning.speedometer.addRoundedPolarBox
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
        repeat(1) {
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


fun Path.drawRoundedRightEndArc(
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

    // Draw the part of the arc before the rounded corner
    arcTo(
        rect = Rect(center = center, radius = innerRadius),
        startAngleDegrees = startAngleDegrees,
        sweepAngleDegrees = (sweepAngleDegrees - outerAngleShift).toFloat(),
        forceMoveTo = true,
    )

    // Draw the part of the arc where the right end is rounded
    if (sweepAngleDegrees >= 360) {
        // If the sweep angle is greater than or equal to 360, we round the corner at the end
        arcTo(
            rect = Rect(
                center = center,
                radius = outerRadius,
            ),
            startAngleDegrees = endAngleDegrees.toFloat() - outerAngleShift.toFloat(),
            sweepAngleDegrees = outerAngleShift.toFloat(),
            forceMoveTo = false,
        )
    } else {
        // Otherwise, we draw the remaining part of the arc with a flat end
        arcTo(
            rect = Rect(
                center = center,
                radius = outerRadius,
            ),
            startAngleDegrees = endAngleDegrees.toFloat(),
            sweepAngleDegrees = -(sweepAngleDegrees - outerAngleShift).toFloat(),
            forceMoveTo = false,
        )
    }

    close()
}






fun Path.trying(
    center: Offset,
    startAngleDegrees: Float,
    sweepAngleDegrees: Float,
    innerRadius: Float,
    outerRadius: Float,
    cornerRadius: Float,
    radius: Float
) {
    val endAngleDegrees = startAngleDegrees + sweepAngleDegrees.toDouble()
    val innerRadiusShift = innerRadius + cornerRadius.toDouble()
    val innerAngleShift = asin(cornerRadius / innerRadiusShift) * 180 / PI
    val outerRadiusShift = outerRadius - cornerRadius.toDouble()
    val outerAngleShift = asin(cornerRadius / outerRadiusShift) * 180 / PI

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
            radius = radius,
        ),
        startAngleDegrees = (startAngleDegrees).toFloat(),
        sweepAngleDegrees = outerAngleShift.toFloat(),
        forceMoveTo = false,
    )
    arcTo(
        rect = Rect(center = center, radius = outerRadius),
        startAngleDegrees = endAngleDegrees.toFloat(),
        sweepAngleDegrees = -(sweepAngleDegrees - outerAngleShift).toFloat(),
        forceMoveTo = false,
    )



//    arcTo(
//        rect = Rect(
//            center = Offset(
//                x = (center.x + innerRadiusShift * cos((startAngleDegrees + innerAngleShift) * PI / 180)).toFloat(),
//                y = (center.y + innerRadiusShift * sin((startAngleDegrees + innerAngleShift) * PI / 180)).toFloat(),
//            ),
//            radius = 0.1f,
//        ),
//        startAngleDegrees = startAngleDegrees - 90,
//        sweepAngleDegrees = (innerAngleShift - 90).toFloat(),
//        forceMoveTo = true,
//    )
    close()
}

fun Path.addRoundedPolarBoxEnd(
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
            radius = 0.1f,
        ),
        startAngleDegrees = (startAngleDegrees).toFloat(),
        sweepAngleDegrees = outerAngleShift.toFloat(),
        forceMoveTo = false,
    )

    arcTo(
        rect = Rect(
            center = Offset(
                x = (center.x + innerRadiusShift * cos((startAngleDegrees + innerAngleShift) * PI / 180)).toFloat(),
                y = (center.y + innerRadiusShift * sin((startAngleDegrees + innerAngleShift) * PI / 180)).toFloat(),
            ),
            radius = 0.1f,
        ),
        startAngleDegrees = startAngleDegrees - 90,
        sweepAngleDegrees = (innerAngleShift - 90).toFloat(),
        forceMoveTo = true,
    )
    close()
}



//fun Path.addBox(
//    center: Offset,
//    startAngleDegrees: Float,
//    sweepAngleDegrees: Float,
//    innerRadius: Float,
//    outerRadius: Float,
//) {
//    val endAngleDegrees = startAngleDegrees + sweepAngleDegrees.toDouble()
//    val innerAngleShift = asin(innerRadius) * 180 / PI
//    val outerAngleShift = asin(outerRadius) * 180 / PI
//    arcTo(
//        rect = Rect(
//            center = Offset(
//                x = (center.x + innerRadius * cos((startAngleDegrees + innerAngleShift) * PI / 180)).toFloat(),
//                y = (center.y + innerRadius * sin((startAngleDegrees + innerAngleShift) * PI / 180)).toFloat(),
//            ),
//            radius = 0f,
//        ),
//        startAngleDegrees = startAngleDegrees - 90,
//        sweepAngleDegrees = (innerAngleShift - 90).toFloat(),
//        forceMoveTo = true,
//    )
//    arcTo(
//        rect = Rect(center = center, radius = innerRadius),
//        startAngleDegrees = (startAngleDegrees + innerAngleShift).toFloat(),
//        sweepAngleDegrees = (sweepAngleDegrees - innerAngleShift).toFloat(),
//        forceMoveTo = false,
//    )
//    arcTo(
//        rect = Rect(center = center, radius = outerRadius),
//        startAngleDegrees = (endAngleDegrees ).toFloat(),
//        sweepAngleDegrees = -(sweepAngleDegrees - 2 * outerAngleShift).toFloat(),
//        forceMoveTo = false,
//    )
//    arcTo(
//        rect = Rect(
//            center = Offset(
//                x = (center.x + outerRadius * cos((startAngleDegrees + outerAngleShift) * PI / 180)).toFloat(),
//                y = (center.y + outerRadius * sin((startAngleDegrees + outerAngleShift) * PI / 180)).toFloat(),
//            ),
//            radius = 0f,
//        ),
//        startAngleDegrees = (startAngleDegrees + outerAngleShift).toFloat(),
//        sweepAngleDegrees = -(outerAngleShift + 90).toFloat(),
//        forceMoveTo = false,
//    )
//    close()
//}


//fun Path.addRoundedPolarEndBox(
//    center: Offset,
//    startAngleDegrees: Float,
//    sweepAngleDegrees: Float,
//    innerRadius: Float,
//    outerRadius: Float,
//    cornerRadius: Float,
//) {
//    val endAngleDegrees = startAngleDegrees + sweepAngleDegrees.toDouble()
//    val innerRadiusShift = innerRadius + cornerRadius.toDouble()
//    val innerAngleShift = asin(cornerRadius / innerRadiusShift) * 180 / PI
//    val outerRadiusShift = outerRadius - cornerRadius.toDouble()
//    val outerAngleShift = asin(cornerRadius / outerRadiusShift) * 180 / PI
//    arcTo(
//        rect = Rect(
//            center = Offset(
//                x = (center.x + innerRadiusShift * cos((startAngleDegrees + innerAngleShift) * PI / 180)).toFloat(),
//                y = (center.y + innerRadiusShift * sin((startAngleDegrees + innerAngleShift) * PI / 180)).toFloat(),
//            ),
//            radius = cornerRadius,
//        ),
//        startAngleDegrees = startAngleDegrees - 90,
//        sweepAngleDegrees = (innerAngleShift - 90).toFloat(),
//        forceMoveTo = true,
//    )
//    arcTo(
//        rect = Rect(center = center, radius = innerRadius),
//        startAngleDegrees = (startAngleDegrees + innerAngleShift).toFloat(),
//        sweepAngleDegrees = (sweepAngleDegrees - 2 * innerAngleShift).toFloat(),
//        forceMoveTo = false,
//    )
//    arcTo(
//        rect = Rect(
//            center = Offset(
//                x = (center.x + innerRadiusShift * cos((endAngleDegrees - innerAngleShift) * PI / 180)).toFloat(),
//                y = (center.y + innerRadiusShift * sin((endAngleDegrees - innerAngleShift) * PI / 180)).toFloat(),
//            ),
//            radius = cornerRadius,
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
//            radius = cornerRadius,
//        ),
//        startAngleDegrees = (endAngleDegrees + 90).toFloat(),
//        sweepAngleDegrees = -(outerAngleShift + 90).toFloat(),
//        forceMoveTo = false,
//    )
//    arcTo(
//        rect = Rect(center = center, radius = outerRadius),
//        startAngleDegrees = (endAngleDegrees - outerAngleShift).toFloat(),
//        sweepAngleDegrees = -(sweepAngleDegrees - 2 * outerAngleShift).toFloat(),
//        forceMoveTo = false,
//    )
//    arcTo(
//        rect = Rect(
//            center = Offset(
//                x = (center.x + outerRadiusShift * cos((startAngleDegrees + outerAngleShift) * PI / 180)).toFloat(),
//                y = (center.y + outerRadiusShift * sin((startAngleDegrees + outerAngleShift) * PI / 180)).toFloat(),
//            ),
//            radius = cornerRadius,
//        ),
//        startAngleDegrees = (startAngleDegrees + outerAngleShift).toFloat(),
//        sweepAngleDegrees = -(outerAngleShift + 90).toFloat(),
//        forceMoveTo = false,
//    )
//    close()
//}


fun Path.addRoundedPolarBox2(

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

    // Draw the flat start
    lineTo(
        x = (center.x + innerRadius * cos((startAngleDegrees) * PI / 180)).toFloat(),
        y = (center.y + innerRadius * sin((startAngleDegrees) * PI / 180)).toFloat()
    )

    // Draw the arc from the flat start to the rounded end
    arcTo(
        rect = Rect(center = center, radius = innerRadius),
        startAngleDegrees = (startAngleDegrees),
        sweepAngleDegrees = (sweepAngleDegrees - outerAngleShift).toFloat(),
        forceMoveTo = false,
    )

    // Draw the rounded corner at the end of the sweep angle side
    arcTo(
        rect = Rect(center = center, radius = outerRadius),
        startAngleDegrees = (endAngleDegrees - outerAngleShift).toFloat(),
        sweepAngleDegrees = outerAngleShift.toFloat(),
        forceMoveTo = false,
    )

    // Draw the corner to close the shape
    arcTo(
        rect = Rect(
            center = Offset(
                x = (center.x + outerRadiusShift * cos((endAngleDegrees - outerAngleShift) * PI / 180)).toFloat(),
                y = (center.y + outerRadiusShift * sin((endAngleDegrees - outerAngleShift) * PI / 180)).toFloat(),
            ),
            radius = cornerRadius,
        ),
        startAngleDegrees = (endAngleDegrees - outerAngleShift).toFloat(),
        sweepAngleDegrees = outerAngleShift.toFloat(),
        forceMoveTo = false,
    )

    close()
}


