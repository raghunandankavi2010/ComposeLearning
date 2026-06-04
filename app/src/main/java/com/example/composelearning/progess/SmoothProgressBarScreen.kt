package com.example.composelearning.progess

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

private val DemoColors = listOf(
    Color(0xFF33B5E5),
    Color(0xFFAA66CC),
    Color(0xFF99CC00),
    Color(0xFFFFBB33),
    Color(0xFFFF4444),
)

@Composable
fun SmoothProgressBarScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = "SmoothProgressBar (Compose port)",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = "Port of castorflex/SmoothProgressBar — indeterminate horizontal sections that slide with cycling colors and accelerate-easing widths.",
            style = MaterialTheme.typography.bodyMedium,
        )

        Sample(title = "Default (Holo blue, 4 sections)") {
            SmoothProgressBar()
        }

        Sample(title = "Multi-color, 5 sections") {
            SmoothProgressBar(
                colors = DemoColors,
                sectionsCount = 5,
            )
        }

        Sample(title = "Fast (speed 3x), thicker stroke") {
            SmoothProgressBar(
                colors = DemoColors,
                speed = 3f,
                strokeWidth = 10.dp,
                separatorLength = 6.dp,
            )
        }

        Sample(title = "Slow (speed 0.4x)") {
            SmoothProgressBar(
                colors = DemoColors,
                speed = 0.4f,
                strokeWidth = 6.dp,
            )
        }

        Sample(title = "Mirror mode") {
            SmoothProgressBar(
                colors = DemoColors,
                mirrorMode = true,
                strokeWidth = 8.dp,
            )
        }

        Sample(title = "Reversed") {
            SmoothProgressBar(
                colors = DemoColors,
                reversed = true,
                strokeWidth = 6.dp,
            )
        }

        Sample(title = "Linear easing (no acceleration)") {
            SmoothProgressBar(
                colors = DemoColors,
                easing = { it },
                strokeWidth = 6.dp,
            )
        }

        Sample(title = "Many narrow sections (8)") {
            SmoothProgressBar(
                colors = DemoColors,
                sectionsCount = 8,
                separatorLength = 2.dp,
                strokeWidth = 6.dp,
            )
        }

        Text(
            text = "SmoothCircularProgressBar (the sister library)",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 12.dp),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            CircularDemo("Default") {
                SmoothCircularProgressBar()
            }
            CircularDemo("Multi-color") {
                SmoothCircularProgressBar(
                    colors = DemoColors,
                    strokeWidth = 5.dp,
                    size = 56.dp,
                )
            }
            CircularDemo("Thick + butt") {
                SmoothCircularProgressBar(
                    colors = DemoColors,
                    strokeWidth = 8.dp,
                    rounded = false,
                    size = 56.dp,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            CircularDemo("Fast spin") {
                SmoothCircularProgressBar(
                    colors = DemoColors,
                    rotationSpeed = 2.5f,
                    sweepSpeed = 2f,
                    strokeWidth = 5.dp,
                    size = 56.dp,
                )
            }
            CircularDemo("Slow / wide arc") {
                SmoothCircularProgressBar(
                    colors = DemoColors,
                    sweepSpeed = 0.6f,
                    maxSweepAngle = 340f,
                    strokeWidth = 5.dp,
                    size = 56.dp,
                )
            }
            CircularDemo("Narrow arc") {
                SmoothCircularProgressBar(
                    colors = DemoColors,
                    minSweepAngle = 5f,
                    maxSweepAngle = 180f,
                    strokeWidth = 5.dp,
                    size = 56.dp,
                )
            }
        }

        Text(
            text = "PremiumCircularProgressIndicator",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 12.dp),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            CircularDemo("Default") {
                PremiumCircularProgressIndicator()
            }
            CircularDemo("Thick") {
                PremiumCircularProgressIndicator(
                    strokeWidth = 12.dp,
                    modifier = Modifier.size(64.dp)
                )
            }
            CircularDemo("Custom Brush") {
                PremiumCircularProgressIndicator(
                    brush = Brush.sweepGradient(
                        listOf(Color.Red, Color.Yellow, Color.Red)
                    ),
                    modifier = Modifier.size(56.dp)
                )
            }
        }

        Text(
            text = "TriColorCircularProgressIndicator (3 solid segments)",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            text = "Same premium head/tail motion, but the progress arc is split into 3 equal sections (e.g. 75° → 25° each), each with its own solid color instead of a gradient.",
            style = MaterialTheme.typography.bodyMedium,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            CircularDemo("Default") {
                TriColorCircularProgressIndicator()
            }
            CircularDemo("Thick") {
                TriColorCircularProgressIndicator(
                    strokeWidth = 12.dp,
                    modifier = Modifier.size(64.dp)
                )
            }
            CircularDemo("Custom colors") {
                TriColorCircularProgressIndicator(
                    colors = listOf(Color.Red, Color(0xFFFFBB33), Color(0xFF99CC00)),
                    modifier = Modifier.size(56.dp)
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun CircularDemo(label: String, content: @Composable () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(modifier = Modifier.size(64.dp), contentAlignment = Alignment.Center) {
            content()
        }
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun Sample(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        content()
    }
}

@Preview
@Composable
private fun SmoothProgressBarScreenPreview() {
    SmoothProgressBarScreen()
}