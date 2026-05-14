package com.example.composelearning.tutorial.ui.overlay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

@Composable
fun CalloutCard(
    title: String,
    description: String,
    stepNumber: Int,
    totalSteps: Int,
    targetRect: Rect?,
    overlaySizePx: androidx.compose.ui.unit.IntSize,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    isLast: Boolean,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val horizontalPaddingPx = with(density) { 16.dp.toPx() }
    val verticalGapPx = with(density) { 16.dp.toPx() }

    val placement = remember(targetRect, overlaySizePx) {
        computePlacement(
            targetRect = targetRect,
            overlayWidthPx = overlaySizePx.width.toFloat(),
            overlayHeightPx = overlaySizePx.height.toFloat(),
            estimatedCardHeightPx = with(density) { 200.dp.toPx() },
            verticalGapPx = verticalGapPx,
            horizontalPaddingPx = horizontalPaddingPx,
        )
    }

    val maxWidthDp = with(density) { placement.maxWidth.toDp() }

    AnimatedVisibility(
        visible = targetRect != null,
        modifier = modifier,
        enter = fadeIn(tween(240)) + slideInVertically(
            animationSpec = tween(durationMillis = 320),
            initialOffsetY = { if (placement.above) -it / 4 else it / 4 },
        ),
        exit = fadeOut(tween(180)) + slideOutVertically(
            animationSpec = tween(durationMillis = 200),
            targetOffsetY = { if (placement.above) -it / 4 else it / 4 },
        ),
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(placement.x.toInt(), placement.y.toInt()) }
                .width(maxWidthDp),
        ) {
            CalloutContent(
                title = title,
                description = description,
                stepNumber = stepNumber,
                totalSteps = totalSteps,
                isLast = isLast,
                onNext = onNext,
                onSkip = onSkip,
            )
        }
    }
}

@Composable
private fun CalloutContent(
    title: String,
    description: String,
    stepNumber: Int,
    totalSteps: Int,
    isLast: Boolean,
    onNext: () -> Unit,
    onSkip: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shadowElevation = 12.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Step $stepNumber of $totalSteps",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                StepDots(current = stepNumber - 1, total = totalSteps)
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onSkip) {
                    Text("Skip")
                }
                Button(
                    onClick = onNext,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Text(if (isLast) "Done" else "Next")
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        imageVector = if (isLast) Icons.Default.Check
                        else Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun StepDots(current: Int, total: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(total) { index ->
            val active = index == current
            val color = if (active) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outlineVariant
            Box(
                Modifier
                    .size(if (active) 8.dp else 6.dp)
                    .clip(CircleShape)
                    .background(color),
            )
        }
    }
}

private data class CalloutPlacement(
    val x: Float,
    val y: Float,
    val maxWidth: Float,
    val above: Boolean,
)

private fun computePlacement(
    targetRect: Rect?,
    overlayWidthPx: Float,
    overlayHeightPx: Float,
    estimatedCardHeightPx: Float,
    verticalGapPx: Float,
    horizontalPaddingPx: Float,
): CalloutPlacement {
    val maxWidth = (overlayWidthPx - 2 * horizontalPaddingPx).coerceAtLeast(0f)
    if (targetRect == null) {
        return CalloutPlacement(
            x = horizontalPaddingPx,
            y = overlayHeightPx - estimatedCardHeightPx - verticalGapPx,
            maxWidth = maxWidth,
            above = false,
        )
    }

    val spaceBelow = overlayHeightPx - targetRect.bottom
    val spaceAbove = targetRect.top
    val placeAbove = spaceBelow < estimatedCardHeightPx + verticalGapPx &&
            spaceAbove > spaceBelow

    val y = if (placeAbove) {
        (targetRect.top - estimatedCardHeightPx - verticalGapPx)
            .coerceAtLeast(verticalGapPx)
    } else {
        (targetRect.bottom + verticalGapPx)
            .coerceAtMost(overlayHeightPx - estimatedCardHeightPx - verticalGapPx)
    }

    return CalloutPlacement(
        x = horizontalPaddingPx,
        y = y,
        maxWidth = maxWidth,
        above = placeAbove,
    )
}