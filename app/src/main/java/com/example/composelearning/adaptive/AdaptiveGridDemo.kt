package com.example.composelearning.adaptive

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Two ways to make a **grid** adaptive — and the trade-off between them.
 *
 *  1. **[GridCells.Adaptive]** — you specify a *minimum cell size* and the grid
 *     fits as many equal columns as possible. This is the "just works" option:
 *     it reflows continuously as the window resizes, no breakpoints needed.
 *
 *  2. **[GridCells.Fixed] driven by [WindowSizeClass]** — you pick the exact
 *     column count per size class (2 / 4 / 6 here). Use this when you want precise
 *     control of the count at each breakpoint rather than a purely size-driven one.
 *
 * Toggle the chips to compare. Note that for the size-class option we reuse the
 * shared [rememberAdaptiveWidthClass] helper — the same signal the detail demo uses.
 */
@Composable
fun AdaptiveGridDemo(modifier: Modifier = Modifier) {
    var useSizeClass by remember { mutableStateOf(false) }
    val widthClass = rememberAdaptiveWidthClass()

    val columns = if (useSizeClass) {
        // Hand-picked counts per breakpoint.
        GridCells.Fixed(
            when (widthClass) {
                AdaptiveWidthClass.COMPACT -> 2
                AdaptiveWidthClass.MEDIUM -> 4
                AdaptiveWidthClass.EXPANDED -> 6
            }
        )
    } else {
        // Let the grid choose the count from a minimum cell width.
        GridCells.Adaptive(minSize = 140.dp)
    }

    Surface(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Strategy",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row {
                    FilterChip(
                        selected = !useSizeClass,
                        onClick = { useSizeClass = false },
                        label = { Text("Adaptive(minSize)") }
                    )
                    Spacer(Modifier.padding(4.dp))
                    FilterChip(
                        selected = useSizeClass,
                        onClick = { useSizeClass = true },
                        label = { Text("Fixed by WindowSizeClass") }
                    )
                }
                Text(
                    text = if (useSizeClass) {
                        "Fixed columns for the current width class: ${widthClass.label}."
                    } else {
                        "As many 140dp-min columns as fit. Resize the window to watch it reflow."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            LazyVerticalGrid(
                columns = columns,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(samplePhotos, key = { it.id }) { photo ->
                    PhotoCell(photo)
                }
            }
        }
    }
}

@Composable
private fun PhotoCell(photo: Photo) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(photo.color),
        contentAlignment = Alignment.Center
    ) {
        Text(
            photo.title,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.titleMedium
        )
    }
}
