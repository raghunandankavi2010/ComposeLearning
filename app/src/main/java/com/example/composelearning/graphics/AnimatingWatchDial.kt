package com.example.composelearning.graphics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private val FaceCenter = Color(0xFF1C2330)
private val FaceEdge = Color(0xFF0B0E14)
private val Bezel = Color(0xFF2B3344)
private val TickMajor = Color(0xFFEDEFF2)
private val TickMinor = Color(0xFF6B7280)
private val Numerals = Color(0xFFEDEFF2)
private val HandColor = Color(0xFFEDEFF2)
private val SecondHand = Color(0xFFE5484D)
private val HubColor = Color(0xFFE5484D)

/**
 * A smooth analog watch face: hour, minute and a continuously *sweeping* second
 * hand (no ticking), 1–12 numerals, 60 minute ticks with longer hour ticks.
 *
 * ### Performance
 * The clock ticks ~60 times a second, but it must **never recompose** for that.
 * Two techniques keep it cheap:
 *
 *  1. **Draw-phase state read.** `timeState` is read inside `onDrawBehind` (the
 *     draw phase), not in the composable body. Writing a new time therefore
 *     invalidates only *drawing*, so recomposition stays at 1 for the lifetime
 *     of the screen instead of running every frame.
 *  2. **Cached static dial.** The bezel, ticks and 12 numerals never change, so
 *     they are rendered once into an [ImageBitmap] inside [drawWithCache] (which
 *     only re-runs when the size changes). Each frame simply blits that bitmap
 *     and draws the three hands.
 */
@Composable
fun AnimatingWatchDial(modifier: Modifier = Modifier) {
    // Updated every frame. Read ONLY in the draw phase below -> no recomposition.
    val timeState = remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            withFrameMillis { timeState.longValue = System.currentTimeMillis() }
        }
    }

    val calendar = remember { Calendar.getInstance() }
    val textMeasurer = rememberTextMeasurer()

    Spacer(
        modifier = modifier.drawWithCache {
            val radius = min(size.width, size.height) / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            // --- Build the static dial ONCE per size (re-runs only when size changes) ---
            val staticDial = ImageBitmap(
                width = size.width.toInt().coerceAtLeast(1),
                height = size.height.toInt().coerceAtLeast(1)
            )
            CanvasDrawScope().draw(
                density = this,
                layoutDirection = LayoutDirection.Ltr,
                canvas = Canvas(staticDial),
                size = size
            ) {
                drawFace(center, radius)
                drawTicks(center, radius)
                drawNumerals(center, radius, textMeasurer)
            }

            // --- Per-frame: blit the cached dial, then draw the moving hands ---
            onDrawBehind {
                calendar.timeInMillis = timeState.longValue // draw-phase read
                val fractionalSecond =
                    calendar.get(Calendar.SECOND) + calendar.get(Calendar.MILLISECOND) / 1000f
                val fractionalMinute = calendar.get(Calendar.MINUTE) + fractionalSecond / 60f
                val fractionalHour = (calendar.get(Calendar.HOUR) % 12) + fractionalMinute / 60f

                drawImage(staticDial)

                // Hour hand
                drawHand(center, fractionalHour * 30f, radius * 0.50f, radius * 0.14f, radius * 0.040f, HandColor)
                // Minute hand
                drawHand(center, fractionalMinute * 6f, radius * 0.74f, radius * 0.18f, radius * 0.026f, HandColor)
                // Second hand
                drawHand(center, fractionalSecond * 6f, radius * 0.82f, radius * 0.22f, radius * 0.012f, SecondHand)

                // Center hub
                drawCircle(color = HubColor, radius = radius * 0.045f, center = center)
                drawCircle(color = FaceEdge, radius = radius * 0.018f, center = center)
            }
        }
    )
}

private fun DrawScope.drawFace(center: Offset, radius: Float) {
    // Bezel ring
    drawCircle(color = Bezel, radius = radius, center = center)
    // Face with a soft radial gradient for depth
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(FaceCenter, FaceEdge),
            center = center,
            radius = radius
        ),
        radius = radius * 0.94f,
        center = center
    )
}

private fun DrawScope.drawTicks(center: Offset, radius: Float) {
    val outer = radius * 0.90f
    for (i in 0 until 60) {
        val isHour = i % 5 == 0
        val angleRad = Math.toRadians(i * 6.0)
        val sin = sin(angleRad).toFloat()
        val cos = cos(angleRad).toFloat()

        val tickLength = if (isHour) radius * 0.11f else radius * 0.05f
        val inner = outer - tickLength
        val start = Offset(center.x + sin * inner, center.y - cos * inner)
        val end = Offset(center.x + sin * outer, center.y - cos * outer)

        drawLine(
            color = if (isHour) TickMajor else TickMinor,
            start = start,
            end = end,
            strokeWidth = if (isHour) radius * 0.018f else radius * 0.008f,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawNumerals(
    center: Offset,
    radius: Float,
    textMeasurer: TextMeasurer
) {
    val numeralRadius = radius * 0.72f
    val style = TextStyle(
        color = Numerals,
        fontSize = (radius * 0.13f).toSp(),
        fontWeight = FontWeight.SemiBold
    )
    for (hour in 1..12) {
        val angleRad = Math.toRadians(hour * 30.0)
        val x = center.x + sin(angleRad).toFloat() * numeralRadius
        val y = center.y - cos(angleRad).toFloat() * numeralRadius

        val measured = textMeasurer.measure(text = hour.toString(), style = style)
        drawText(
            textLayoutResult = measured,
            topLeft = Offset(
                x - measured.size.width / 2f,
                y - measured.size.height / 2f
            )
        )
    }
}

private fun DrawScope.drawHand(
    center: Offset,
    angleDegrees: Float,
    length: Float,
    tail: Float,
    strokeWidth: Float,
    color: Color
) {
    rotate(degrees = angleDegrees, pivot = center) {
        drawLine(
            color = color,
            start = Offset(center.x, center.y + tail),
            end = Offset(center.x, center.y - length),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AnimatingWatchDialPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF05070B))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatingWatchDial(
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(1f)
        )
    }
}
