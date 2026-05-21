package com.example.composelearning.flight

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun FlightSeatScreen() {
    val state = rememberFlightSeatState()
    val colors = FlightSeatDefaults.colors()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "FlightSeat (Compose port)",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = "Tap a seat to select it. Confirm to lock the selection. Port of ldoublem/FlightSeat — redrawn with Compose primitives instead of bitmap drawables.",
            style = MaterialTheme.typography.bodySmall,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LegendChip("Available", colors.seatAvailable)
            LegendChip("Selecting", colors.seatSelecting)
            LegendChip("Booked", colors.seatSelected)
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            shape = RoundedCornerShape(12.dp),
        ) {
            FlightSeatView(
                state = state,
                colors = colors,
                modifier = Modifier.padding(8.dp),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${state.selectingCount} selecting · ${state.selectedCount} booked",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            OutlinedButton(onClick = { state.reset() }) { Text("Reset") }
            Button(
                onClick = { state.confirmSelection() },
                enabled = state.selectingCount > 0,
            ) { Text("Confirm") }
        }
    }
}

@Composable
private fun LegendChip(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(modifier = Modifier.size(14.dp), contentAlignment = Alignment.Center) {
            Surface(color = color, shape = RoundedCornerShape(3.dp), modifier = Modifier.fillMaxSize()) {}
        }
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}