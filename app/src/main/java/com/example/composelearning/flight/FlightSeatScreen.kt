package com.example.composelearning.flight

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FlightSeatScreen() {
    val state = rememberFlightSeatState()
    val colors = FlightSeatDefaults.colors()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(colors.sky)
    ) {
        // Title bar matching the FlightSeat app
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "FlightSeat",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            FlightSeatView(
                state = state,
                colors = colors,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Bottom action bar — CLEAR | counter | OK
        Surface(
            color = Color(0xFFE9EBED),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { state.reset() }) {
                    Text("CLEAR", color = Color(0xFF6C7480), fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = "${state.selectingCount} selecting · ${state.selectedCount} booked",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF6C7480)
                )
                Spacer(Modifier.weight(1f))
                TextButton(
                    onClick = { state.confirmSelection() },
                    enabled = state.selectingCount > 0
                ) {
                    Text("OK", color = Color(0xFF6C7480), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
@Suppress("unused")
private fun LegendChip(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(modifier = Modifier.size(14.dp), contentAlignment = Alignment.Center) {
            Surface(color = color, shape = RoundedCornerShape(3.dp), modifier = Modifier.fillMaxSize()) {}
        }
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}
