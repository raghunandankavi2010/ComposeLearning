package com.example.composelearning

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Demonstrates positioning a child as a percentage of its parent's available height.
 *
 * The math: given an `available = parentHeight - childHeight` track, the child's top-left Y is
 * simply `available * percentage`. At 0% it sits at the top; at 100% the bottom edge kisses the
 * bottom of the container. `BoxWithConstraints` is the right primitive because we need the parent's
 * measured height (in dp) to do that math — everything else is plain Compose state + offset.
 */
@Composable
fun PercentageBaseLayout(modifier: Modifier = Modifier) {
    var percentage by remember { mutableFloatStateOf(0.5f) }
    val childSize: Dp = 96.dp

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Header()

        PercentageTrack(
            percentage = percentage,
            childSize = childSize,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )

        PercentageReadout(percentage)

        Slider(
            value = percentage,
            onValueChange = { percentage = it },
        )
    }
}

@Composable
private fun Header() {
    Column {
        Text(
            text = "Percentage layout",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Drag the slider — the avatar's Y is (parent − child) × percentage.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PercentageTrack(
    percentage: Float,
    childSize: Dp,
    modifier: Modifier = Modifier,
) {
    val outline = MaterialTheme.colorScheme.outlineVariant
    val accent = MaterialTheme.colorScheme.primary

    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(16.dp),
    ) {
        val parentHeight = maxHeight
        val travel = parentHeight - childSize
        val y = travel * percentage

        TickRuler(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = childSize + 24.dp),
            tickColor = outline,
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(y = y)
                .size(childSize)
                .clip(RoundedCornerShape(24.dp))
                .background(accent),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "${(percentage * 100).toInt()}%",
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun TickRuler(modifier: Modifier, tickColor: Color) {
    Canvas(modifier = modifier) {
        val ticks = 11
        val strokePx = 2.dp.toPx()
        for (i in 0 until ticks) {
            val y = size.height * i / (ticks - 1)
            val isMajor = i % 5 == 0
            val w = if (isMajor) size.width else size.width * 0.55f
            val xStart = size.width - w
            drawLine(
                color = tickColor,
                start = Offset(xStart, y),
                end = Offset(size.width, y),
                strokeWidth = strokePx,
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
private fun PercentageReadout(percentage: Float) {
    val density = LocalDensity.current
    Text(
        text = "Position: ${(percentage * 100).toInt()}% · density: ${density.density}x",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}