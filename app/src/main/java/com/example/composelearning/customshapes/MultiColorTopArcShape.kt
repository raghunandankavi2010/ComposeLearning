package com.example.composelearning.customshapes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.example.composelearning.sotry.topBorder
import okhttp3.Route

@Composable
fun MultiColoredTopArcsShape(
    modifier: Modifier = Modifier,
    cornerRadius: Float = 50f // Example corner radius
) {
    Canvas(
        modifier = modifier
            .size(100.dp)
            .background(Color.Green)
            .clip(RoundedCornerShape(cornerRadius))
    ) {
        val pathStroke = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

        val topLeftX = cornerRadius
        val topRightX = size.width - cornerRadius
        val path1 = Path().apply {
            moveTo(x = topLeftX, y = 0f)
            lineTo(x = topRightX, y = 0f)
        }
        drawPath(path = path1, color = Color.Red, style = pathStroke)

        // Segment 2: Top Right Arc (Green)
        val path2 = Path().apply {
            moveTo(x = topRightX, y = 0f)
            arcTo(
                rect = Rect(
                    left = size.width - 2 * cornerRadius,
                    top = 0f,
                    right = size.width,
                    bottom = 2 * cornerRadius
                ),
                startAngleDegrees = 270.0f,
                sweepAngleDegrees = 90.0f,
                forceMoveTo = false
            )
        }
        drawPath(path = path2, color = Color.Red, style = pathStroke)

//        // Segment 3: Right Straight Line (Blue)
//        val path3 = Path().apply {
//            // The previous arc ends at (size.width, cornerRadius) in arc's local coordinates,
//            // which is (size.width, cornerRadius) relative to the top-left of the overall shape if the arc started at 0,0.
//            // Let's explicitly move to the end of the arc for clarity.
//            // The arc ends at (size.width, cornerRadius)
//            moveTo(x = size.width, y = cornerRadius)
//            lineTo(x = size.width, y = size.height)
//        }
//        drawPath(path = path3, color = Color.Transparent, style = pathStroke)
//
//
//        // Segment 4: Bottom Straight Line (Magenta)
//        val path4 = Path().apply {
//            moveTo(x = size.width, y = size.height)
//            lineTo(x = 0f, y = size.height)
//        }
//        drawPath(path = path4, color = Color.Transparent, style = pathStroke)
//
//
//        // Segment 5: Left Straight Line (Cyan)
//        val path5 = Path().apply {
//            moveTo(x = 0f, y = size.height)
//            lineTo(x = 0f, y = cornerRadius)
//        }
//        drawPath(path = path5, color = Color.Transparent, style = pathStroke)


        // Segment 6: Top Left Arc (Yellow)
        val path6 = Path().apply {
            moveTo(x = 0f, y = cornerRadius) // Start where the previous segment ended
            arcTo(
                rect = Rect(
                    left = 0f,
                    top = 0f,
                    right = 2 * cornerRadius,
                    bottom = 2 * cornerRadius
                ),
                startAngleDegrees = 180.0f,
                sweepAngleDegrees = 90.0f,
                forceMoveTo = false // Continue from the last point
            )
        }
        drawPath(path = path6, color = Color.Red, style = pathStroke)


    }
}


@Preview(showBackground = true, widthDp = 300, heightDp = 200)
@Composable
fun MultiColoredTopArcsShapePreview() {
    MultiColoredTopArcsShape()
}