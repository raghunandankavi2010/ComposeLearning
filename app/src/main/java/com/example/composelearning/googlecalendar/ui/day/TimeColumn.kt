package com.example.composelearning.googlecalendar.ui.day

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

const val HOUR_HEIGHT_DP = 60

@Composable
fun TimeLabel(
    hour: Int,
    hourHeight: Dp = HOUR_HEIGHT_DP.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(hourHeight)
            .width(52.dp)
            .padding(end = 8.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        val label = when (hour) {
            0 -> "12 AM"
            in 1..11 -> "$hour AM"
            12 -> "12 PM"
            else -> "${hour - 12} PM"
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
        )
    }
}
