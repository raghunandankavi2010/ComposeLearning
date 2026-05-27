package com.example.composelearning.googlecalendar.ui.day

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composelearning.googlecalendar.domain.model.LayoutEvent
import com.example.composelearning.googlecalendar.util.DateUtils

/**
 * Renders a single event block positioned in a day timeline.
 * [topOffset] is the Y position from the top of the day grid.
 * [height] is the height of the event based on duration.
 * [widthFraction] and [xOffsetFraction] are derived from overlap layout.
 */
@Composable
fun EventBlock(
    layoutEvent: LayoutEvent,
    hourHeight: Dp = HOUR_HEIGHT_DP.dp,
    modifier: Modifier = Modifier
) {
    val event = layoutEvent.event
    val startMinutes = event.displayStartTime.hour * 60 + event.displayStartTime.minute
    val endMinutes = event.displayEndTime.hour * 60 + event.displayEndTime.minute
    val durationMinutes = (endMinutes - startMinutes).coerceAtLeast(15)

    val density = LocalDensity.current
    val hourHeightPx = with(density) { hourHeight.toPx() }
    val minuteHeight = hourHeightPx / 60f

    val topOffsetDp = with(density) { (startMinutes * minuteHeight).toDp() }
    val eventHeightDp = with(density) { (durationMinutes * minuteHeight).toDp() }

    val widthFraction = 1f / layoutEvent.totalColumns
    val xOffsetFraction = layoutEvent.column.toFloat() / layoutEvent.totalColumns

    val shape = RoundedCornerShape(4.dp)
    val surfaceColor = event.color.copy(alpha = 0.15f)
    val borderColor = event.color

    Box(
        modifier = modifier
            .fillMaxWidth(widthFraction)
            .offset(y = topOffsetDp)
            .padding(start = (xOffsetFraction * 100).dp) // approximate
            .height(eventHeightDp)
            .clip(shape)
            .background(surfaceColor)
            .border(width = 0.5.dp, color = borderColor.copy(alpha = 0.4f), shape = shape)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Column {
            Text(
                text = event.title,
                color = event.color,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp
            )
            if (durationMinutes >= 30) {
                Text(
                    text = DateUtils.formatEventTimeRange(event.startTime, event.endTime),
                    color = event.color.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 12.sp
                )
            }
        }
    }
}
