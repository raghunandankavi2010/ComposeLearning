package com.example.composelearning.animcompose

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun BouncingBallAnimation() {

    BoxWithConstraints(
        modifier = Modifier
            .padding(WindowInsets.systemBars.asPaddingValues())
            .fillMaxSize()
            .background(Color.Cyan.copy(alpha = 0.1f))
    ) {
        val density = LocalDensity.current

        val circleSize = 50.dp
        val circleSizePx = with(density) { circleSize.toPx() }

        val maxHeightPx = with(density) { maxHeight.toPx() }
        val maxWidthPx = with(density) { maxWidth.toPx() }

        val navBarInsets = WindowInsets.navigationBars.asPaddingValues()
        val bottomInsetPx = with(density) { navBarInsets.calculateBottomPadding().toPx() }

        val xOffset = maxWidthPx/2 - circleSizePx / 2
        val xOffsetDp = remember { with(density) { xOffset.toDp() } }

        val maxOffsetPx = remember(maxHeightPx, bottomInsetPx) {
            (maxHeightPx - circleSizePx).coerceAtLeast(0f)
        }

        val infiniteTransition = rememberInfiniteTransition(label = "DynamicHeightBounce")

        val yOffsetPx by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = maxOffsetPx,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 10000,
                    easing = EaseInOutSine
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "VerticalOffsetAnimation"
        )

        val yOffsetDp = with(density) { yOffsetPx.toDp() }
        Spacer(
            modifier = Modifier
                .offset(x = xOffsetDp, y = yOffsetDp)
                .size(circleSize)
                .background(Color.Blue, CircleShape)
        )
    }
}