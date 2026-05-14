package com.example.composelearning.clocks

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin

private const val MINUTES_PER_DAY = 24 * 60
private const val MINUTES_PER_HALF = 12 * 60   // dial face spans 12 hours
private const val SNAP_MINUTES = 5

@Composable
fun TimeRangeKnobScreen() {
    // Default range: 22:30 → 06:30 (night-time slot, so we boot in night theme).
    var startMinutes by remember { mutableIntStateOf(22 * 60 + 30) }
    var endMinutes by remember { mutableIntStateOf(6 * 60 + 30) }

    val palette = paletteFor(midpointMinutes(startMinutes, endMinutes))

    val background by animateColorAsState(
        palette.background,
        animationSpec = tween(600),
        label = "background",
    )
    val surface by animateColorAsState(palette.surface, tween(600), label = "surface")
    val onSurface by animateColorAsState(palette.onSurface, tween(600), label = "onSurface")
    val onSurfaceMuted by animateColorAsState(palette.onSurfaceMuted, tween(600), label = "onSurfaceMuted")
    val accentStart by animateColorAsState(palette.accentStart, tween(600), label = "accentStart")
    val accentEnd by animateColorAsState(palette.accentEnd, tween(600), label = "accentEnd")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .systemBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(8.dp))
        Text(
            text = palette.title,
            style = MaterialTheme.typography.titleMedium,
            color = onSurfaceMuted,
            fontWeight = FontWeight.Medium,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Schedule",
            style = MaterialTheme.typography.headlineLarge,
            color = onSurface,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(28.dp))

        TimeRangeDial(
            startMinutes = startMinutes,
            endMinutes = endMinutes,
            onChange = { s, e -> startMinutes = s; endMinutes = e },
            trackColor = onSurface,
            onTrackColor = onSurfaceMuted,
            surfaceColor = surface,
            accentStart = accentStart,
            accentEnd = accentEnd,
            backgroundColor = background,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
        )

        Spacer(Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TimeBadge(
                label = "Bedtime",
                icon = Icons.Default.DarkMode,
                time = formatTime(startMinutes),
                tint = accentStart,
                surface = surface,
                onSurface = onSurface,
                onSurfaceMuted = onSurfaceMuted,
                onClick = { startMinutes = toggleAmPm(startMinutes) },
            )
            TimeBadge(
                label = "Wake up",
                icon = Icons.Default.WbSunny,
                time = formatTime(endMinutes),
                tint = accentEnd,
                surface = surface,
                onSurface = onSurface,
                onSurfaceMuted = onSurfaceMuted,
                onClick = { endMinutes = toggleAmPm(endMinutes) },
            )
        }

        Spacer(Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .dropShadow(
                    shape = RoundedCornerShape(28.dp),
                    shadow = Shadow(
                        radius = 28.dp,
                        color = accentStart,
                        offset = DpOffset(0.dp, 12.dp),
                        alpha = 0.35f,
                    ),
                )
                .background(surface, RoundedCornerShape(28.dp))
                .padding(horizontal = 20.dp, vertical = 18.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Duration", color = onSurfaceMuted, style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = formatDuration(durationMinutes(startMinutes, endMinutes)),
                        color = onSurface,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Text(
                    text = palette.modeChip,
                    color = accentStart,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier
                        .background(
                            color = accentStart.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(50),
                        )
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun TimeBadge(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    time: String,
    tint: Color,
    surface: Color,
    onSurface: Color,
    onSurfaceMuted: Color,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(surface, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint)
        }
        Spacer(Modifier.size(12.dp))
        Column {
            Text(label, color = onSurfaceMuted, style = MaterialTheme.typography.labelMedium)
            Text(
                text = time,
                color = onSurface,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun TimeRangeDial(
    startMinutes: Int,
    endMinutes: Int,
    onChange: (Int, Int) -> Unit,
    trackColor: Color,
    onTrackColor: Color,
    surfaceColor: Color,
    accentStart: Color,
    accentEnd: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    val thumbSize: Dp = 48.dp
    val ringStrokeDp = 26.dp

    val thumbRadiusPx = with(density) { thumbSize.toPx() / 2f }
    val ringStrokePx = with(density) { ringStrokeDp.toPx() }
    val tickGapPx = with(density) { 10.dp.toPx() }
    val majorTickPx = with(density) { 14.dp.toPx() }
    val minorTickPx = with(density) { 6.dp.toPx() }
    val labelGapPx = with(density) { 22.dp.toPx() }
    val edgePadPx = with(density) { 4.dp.toPx() }

    var activeThumb by remember { mutableStateOf<Int?>(null) }
    // Tracks the previous touch angle so we can apply angular *deltas* — this lets the
    // 24h time wrap correctly when the user drags past the 12 marker (AM/PM auto-flips).
    var lastDragAngle by remember { mutableStateOf<Float?>(null) }

    // pointerInput(Unit) only captures variables once. Use rememberUpdatedState so the
    // gesture lambda always reads the latest values for the *other* thumb when this one moves.
    val currentStart by rememberUpdatedState(startMinutes)
    val currentEnd by rememberUpdatedState(endMinutes)
    val currentOnChange by rememberUpdatedState(onChange)

    val arcBrush = remember(accentStart, accentEnd) {
        Brush.sweepGradient(listOf(accentStart, accentEnd, accentStart))
    }
    val labelStyle = remember(onTrackColor) {
        TextStyle(
            color = onTrackColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
        )
    }
    val centerStyle = remember(trackColor) {
        TextStyle(color = trackColor, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
    }

    BoxWithConstraints(modifier = modifier) {
        val widthPx = with(LocalDensity.current) { maxWidth.toPx() }
        val centerPx = Offset(widthPx / 2f, widthPx / 2f)
        val ringRadiusPx = widthPx / 2f - thumbRadiusPx - edgePadPx
        val startPos = thumbPosition(centerPx, ringRadiusPx, startMinutes)
        val endPos = thumbPosition(centerPx, ringRadiusPx, endMinutes)

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { pos ->
                            val sPos = thumbPosition(centerPx, ringRadiusPx, currentStart)
                            val ePos = thumbPosition(centerPx, ringRadiusPx, currentEnd)
                            val ds = hypot(pos.x - sPos.x, pos.y - sPos.y)
                            val de = hypot(pos.x - ePos.x, pos.y - ePos.y)
                            activeThumb = if (ds <= de) 0 else 1
                            lastDragAngle = angleAt(pos, centerPx)
                        },
                        onDragEnd = { activeThumb = null; lastDragAngle = null },
                        onDragCancel = { activeThumb = null; lastDragAngle = null },
                        onDrag = { change, _ ->
                            change.consume()
                            val newAngle = angleAt(change.position, centerPx)
                            val prev = lastDragAngle ?: newAngle
                            var deltaAngle = newAngle - prev
                            // Shortest-path wrap into [-PI, PI] so the 11→0 boundary doesn't jump.
                            val twoPi = 2f * PI.toFloat()
                            if (deltaAngle > PI.toFloat()) deltaAngle -= twoPi
                            if (deltaAngle < -PI.toFloat()) deltaAngle += twoPi
                            val deltaMinutesRaw =
                                (deltaAngle / twoPi * MINUTES_PER_HALF).roundToInt()
                            val deltaMinutes = (deltaMinutesRaw / SNAP_MINUTES) * SNAP_MINUTES
                            if (deltaMinutes != 0) {
                                when (activeThumb) {
                                    0 -> currentOnChange(
                                        wrapDay(currentStart + deltaMinutes),
                                        currentEnd,
                                    )
                                    1 -> currentOnChange(
                                        currentStart,
                                        wrapDay(currentEnd + deltaMinutes),
                                    )
                                }
                                // Only commit the new reference angle when we actually consumed
                                // a full snap step — otherwise small jitter would never accumulate.
                                lastDragAngle = newAngle
                            }
                        },
                    )
                },
        ) {
            val ringTopLeft = Offset(centerPx.x - ringRadiusPx, centerPx.y - ringRadiusPx)
            val ringSize = Size(ringRadiusPx * 2, ringRadiusPx * 2)

            // 1. Track (background ring)
            drawCircle(
                color = onTrackColor.copy(alpha = 0.10f),
                radius = ringRadiusPx,
                center = centerPx,
                style = Stroke(width = ringStrokePx),
            )

            // 2. Active arc (sweep gradient between the thumbs)
            val startAngleDeg = ((startMinutes % MINUTES_PER_HALF) /
                    MINUTES_PER_HALF.toFloat()) * 360f - 90f
            val sweepDeg = sweepDegrees(startMinutes, endMinutes)
            drawArc(
                brush = arcBrush,
                startAngle = startAngleDeg,
                sweepAngle = sweepDeg,
                useCenter = false,
                topLeft = ringTopLeft,
                size = ringSize,
                style = Stroke(width = ringStrokePx, cap = StrokeCap.Round),
            )

            // 3. Tick marks — 48 total (every 15 minutes on a 12h face).
            //    Three weight levels: cardinal (12/3/6/9), hourly, and quarter-hourly.
            val tickInnerRadius = ringRadiusPx - ringStrokePx / 2f - tickGapPx
            val totalTicks = 48
            val cardinalLen = majorTickPx
            val hourLen = majorTickPx * 0.65f
            val quarterLen = minorTickPx
            val cardinalStroke = 2.5.dp.toPx()
            val hourStroke = 1.5.dp.toPx()
            val quarterStroke = 1.dp.toPx()
            for (i in 0 until totalTicks) {
                val angle = (i / totalTicks.toFloat()) * 2f * PI.toFloat() - (PI.toFloat() / 2f)
                val onCardinal = i % (totalTicks / 4) == 0   // every 3h on a 12h face
                val onHour = i % 4 == 0                       // every hour
                val len: Float
                val alpha: Float
                val stroke: Float
                when {
                    onCardinal -> { len = cardinalLen; alpha = 0.90f; stroke = cardinalStroke }
                    onHour -> { len = hourLen; alpha = 0.55f; stroke = hourStroke }
                    else -> { len = quarterLen; alpha = 0.22f; stroke = quarterStroke }
                }
                val r1 = tickInnerRadius
                val r0 = tickInnerRadius - len
                val cosA = cos(angle)
                val sinA = sin(angle)
                drawLine(
                    color = onTrackColor.copy(alpha = alpha),
                    start = Offset(centerPx.x + cosA * r0, centerPx.y + sinA * r0),
                    end = Offset(centerPx.x + cosA * r1, centerPx.y + sinA * r1),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round,
                )
            }

            // 4. Cardinal labels (12, 3, 6, 9 — analog clock face)
            val labelRadius = tickInnerRadius - majorTickPx - labelGapPx
            listOf(0 to "12", 3 to "3", 6 to "6", 9 to "9").forEach { (h, label) ->
                val angle = (h / 12f) * 2f * PI.toFloat() - (PI.toFloat() / 2f)
                val pos = Offset(
                    centerPx.x + cos(angle) * labelRadius,
                    centerPx.y + sin(angle) * labelRadius,
                )
                val measured = textMeasurer.measure(AnnotatedString(label), style = labelStyle)
                drawText(
                    textLayoutResult = measured,
                    topLeft = Offset(
                        pos.x - measured.size.width / 2f,
                        pos.y - measured.size.height / 2f,
                    ),
                )
            }

            // 5. Center: total duration (big) + supporting caption.
            val duration = formatDuration(durationMinutes(startMinutes, endMinutes))
            val durationMeasured = textMeasurer.measure(
                AnnotatedString(duration),
                style = TextStyle(
                    color = trackColor,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
            val captionMeasured = textMeasurer.measure(
                AnnotatedString("Duration"),
                style = TextStyle(
                    color = onTrackColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                ),
            )
            drawText(
                textLayoutResult = captionMeasured,
                topLeft = Offset(
                    centerPx.x - captionMeasured.size.width / 2f,
                    centerPx.y - durationMeasured.size.height / 2f - captionMeasured.size.height - 4.dp.toPx(),
                ),
            )
            drawText(
                textLayoutResult = durationMeasured,
                topLeft = Offset(
                    centerPx.x - durationMeasured.size.width / 2f,
                    centerPx.y - durationMeasured.size.height / 2f,
                ),
            )
        }

        // Overlay thumbs as composables so we get Icon + dropShadow for free.
        Thumb(
            offsetPx = IntOffset((startPos.x - thumbRadiusPx).toInt(), (startPos.y - thumbRadiusPx).toInt()),
            iconColor = accentStart,
            surface = surfaceColor,
            iconVector = Icons.Default.DarkMode,
            thumbSize = thumbSize,
        )
        Thumb(
            offsetPx = IntOffset((endPos.x - thumbRadiusPx).toInt(), (endPos.y - thumbRadiusPx).toInt()),
            iconColor = accentEnd,
            surface = surfaceColor,
            iconVector = Icons.Default.WbSunny,
            thumbSize = thumbSize,
        )
    }
}

@Composable
private fun Thumb(
    offsetPx: IntOffset,
    iconColor: Color,
    surface: Color,
    iconVector: androidx.compose.ui.graphics.vector.ImageVector,
    thumbSize: Dp,
) {
    Box(
        modifier = Modifier
            .offset { offsetPx }
            .size(thumbSize)
            .dropShadow(
                shape = CircleShape,
                shadow = Shadow(
                    radius = 16.dp,
                    color = iconColor,
                    offset = DpOffset(0.dp, 4.dp),
                    alpha = 0.55f,
                ),
            )
            .background(surface, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = iconVector,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(thumbSize * 0.55f),
        )
    }
}

// ---------------- math helpers ----------------

private fun thumbPosition(center: Offset, radius: Float, minutes: Int): Offset {
    val angle = ((minutes % MINUTES_PER_HALF) / MINUTES_PER_HALF.toFloat()) *
            2f * PI.toFloat() - PI.toFloat() / 2f
    return Offset(center.x + cos(angle) * radius, center.y + sin(angle) * radius)
}

/** Angle from a touch point, measured from the top going clockwise, in radians. */
private fun angleAt(point: Offset, center: Offset): Float {
    val dx = point.x - center.x
    val dy = point.y - center.y
    var a = atan2(dy, dx) + PI.toFloat() / 2f
    if (a < 0f) a += 2f * PI.toFloat()
    return a
}

/** Keep a minutes value in [0, 1440). */
private fun wrapDay(minutes: Int): Int = ((minutes % MINUTES_PER_DAY) + MINUTES_PER_DAY) % MINUTES_PER_DAY

/** Flip a time by +/- 12h, mod 24h. */
private fun toggleAmPm(minutes: Int): Int =
    (minutes + MINUTES_PER_HALF) % MINUTES_PER_DAY

private fun sweepDegrees(start: Int, end: Int): Float {
    val dur = durationMinutes(start, end)
    return if (dur >= MINUTES_PER_HALF) 360f
    else (dur / MINUTES_PER_HALF.toFloat()) * 360f
}

private fun durationMinutes(start: Int, end: Int): Int =
    if (end >= start) end - start else MINUTES_PER_DAY - start + end

private fun midpointMinutes(start: Int, end: Int): Int =
    (start + durationMinutes(start, end) / 2) % MINUTES_PER_DAY

private fun formatTime(minutes: Int): String {
    val h24 = minutes / 60
    val m = minutes % 60
    val isPm = h24 >= 12
    val h12 = ((h24 + 11) % 12) + 1
    return "%d:%02d %s".format(h12, m, if (isPm) "PM" else "AM")
}

private fun formatDuration(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return "${h}h ${"%02d".format(m)}m"
}

// ---------------- palette ----------------

private data class DialPalette(
    val title: String,
    val modeChip: String,
    val background: Color,
    val surface: Color,
    val onSurface: Color,
    val onSurfaceMuted: Color,
    val accentStart: Color,
    val accentEnd: Color,
)

private fun paletteFor(midpointMin: Int): DialPalette {
    val isNight = midpointMin >= 18 * 60 || midpointMin < 6 * 60
    return if (isNight) {
        DialPalette(
            title = "Good evening",
            modeChip = "Night",
            background = Color(0xFF0B1226),
            surface = Color(0xFF18223C),
            onSurface = Color(0xFFE8ECFF),
            onSurfaceMuted = Color(0xFF8E97C9),
            accentStart = Color(0xFF818CF8),
            accentEnd = Color(0xFFC084FC),
        )
    } else {
        DialPalette(
            title = "Good morning",
            modeChip = "Day",
            background = Color(0xFFFFF6E5),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF1F2937),
            onSurfaceMuted = Color(0xFF6B7280),
            accentStart = Color(0xFFFF8A65),
            accentEnd = Color(0xFFFBBF24),
        )
    }
}
