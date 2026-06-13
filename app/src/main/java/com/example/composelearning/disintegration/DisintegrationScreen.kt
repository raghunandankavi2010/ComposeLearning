package com.example.composelearning.disintegration

import androidx.compose.foundation.Image
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
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composelearning.R

/** Which composable to dissolve in the demo — shows it works on anything. */
private enum class DemoTarget(val label: String) {
    IMAGE("Image"),
    TEXT("Text"),
    CARD("Full layout")
}

/**
 * Demo for [Disintegration]: pick an image / text / full layout, tap
 * **Disintegrate** to dissolve it into drifting pixels, then **Reset** to
 * rebuild it. The same [DisintegrationState] + [Disintegration] pair drives all
 * three, because the effect snapshots rendered pixels and never inspects what it
 * wraps.
 */
@Composable
fun DisintegrationScreen(modifier: Modifier = Modifier) {
    var target by remember { mutableStateOf(DemoTarget.IMAGE) }
    // A fresh state per target so switching gives a clean (un-triggered) capture.
    val state = remember(target) { DisintegrationState(durationMillis = 1600) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Disintegration Effect",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "GraphicsLayer captures the pixels, then they break into dust. " +
                "Works on any composable.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            DemoTarget.entries.forEach { entry ->
                FilterChip(
                    selected = target == entry,
                    onClick = { target = entry },
                    label = { Text(entry.label) }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp),
            contentAlignment = Alignment.Center
        ) {
            Disintegration(state = state) {
                when (target) {
                    DemoTarget.IMAGE -> DemoImage()
                    DemoTarget.TEXT -> DemoText()
                    DemoTarget.CARD -> DemoCard()
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { state.trigger() },
                enabled = !state.triggered
            ) {
                Text("Disintegrate")
            }
            OutlinedButton(
                onClick = { state.reset() },
                enabled = state.triggered
            ) {
                Text("Reset")
            }
        }
    }
}

@Composable
private fun DemoImage() {
    Image(
        painter = painterResource(R.drawable.sample_photo),
        contentDescription = "Sample photo to disintegrate",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(280.dp)
            .clip(RoundedCornerShape(20.dp))
    )
}

@Composable
private fun DemoText() {
    Text(
        text = "DUST",
        fontSize = 96.sp,
        fontWeight = FontWeight.Black,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun DemoCard() {
    Column(
        modifier = Modifier
            .size(280.dp, 180.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF6A11CB), Color(0xFF2575FC))
                )
            )
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text("•••• 4242", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Medium)
        Column {
            Text("CARDHOLDER", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
            Text("ADA LOVELACE", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}
