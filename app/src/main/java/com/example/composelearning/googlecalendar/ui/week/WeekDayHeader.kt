package com.example.composelearning.googlecalendar.ui.week

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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

@Composable
fun WeekDayHeader(
    weekDays: List<LocalDate>,
    selectedDate: LocalDate,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Time column spacer (40dp time column + 1dp divider)
        Spacer(modifier = Modifier.width(41.dp))

        weekDays.forEach { date ->
            val isToday = DateUtils.isToday(date)
            val isSelected = date == selectedDate

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = DateUtils.formatShortDay(date).uppercase().take(3),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isToday) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isToday -> MaterialTheme.colorScheme.primary
                                isSelected -> MaterialTheme.colorScheme.primaryContainer
                                else -> Color.Transparent
                            }
                        )
                ) {
                    Text(
                        text = DateUtils.formatDayNumber(date),
                        fontSize = 13.sp,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                        color = when {
                            isToday -> Color.White
                            isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
