package com.example.composelearning.googlecalendar.ui.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composelearning.googlecalendar.util.DateUtils
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun DayCell(
    date: LocalDate,
    currentMonth: YearMonth,
    isSelected: Boolean,
    eventCount: Int,
    onClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val isToday = DateUtils.isToday(date)
    val isInMonth = DateUtils.isSameMonth(date, currentMonth)

    val textColor = when {
        isSelected && isToday -> Color.White
        isSelected -> Color.White
        isToday -> MaterialTheme.colorScheme.primary
        isInMonth -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    }

    val backgroundColor = when {
        isSelected && isToday -> MaterialTheme.colorScheme.primary
        isSelected -> MaterialTheme.colorScheme.onSurface
        isToday -> Color.Transparent
        else -> Color.Transparent
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(CircleShape)
            .clickable { onClick(date) }
            .padding(2.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(backgroundColor)
        ) {
            Text(
                text = DateUtils.formatDayNumber(date),
                color = textColor,
                fontSize = 13.sp,
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Event dots (max 3)
        if (eventCount > 0) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                repeat(eventCount.coerceAtMost(3)) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) Color.White.copy(alpha = 0.7f)
                                else MaterialTheme.colorScheme.primary
                            )
                    )
                }
            }
        } else {
            // Placeholder to keep consistent height
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
