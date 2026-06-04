package com.example.composelearning.wallet.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.composelearning.wallet.domain.model.WalletCard
import kotlin.math.min

@Composable
fun WalletScreen(
    viewModel: WalletViewModel = viewModel(factory = WalletViewModel.Factory()),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            WalletStack(state.cards)
        }
    }
}

/**
 * Scroll-driven collapsing card stack (the-10-min/Wallet).
 *
 * Each card sits in a fixed slot of height `CH` (card + vertical margins). As the user
 * scrolls (`y`), per-card `graphicsLayer` transforms reproduce the original Reanimated math:
 *
 *  - `position = index*CH - y` — the card's distance from the top of the viewport.
 *  - a "sticky" translation pins the card to the top once `y` passes `index*CH`.
 *  - scale & alpha interpolate `0.5 → 1 → 1 → 0.5` across
 *    `[disappearing(-CH), top(0), bottom(H-CH), appearing(H)]`, so piled-up cards shrink
 *    and fade. `H` is the viewport height.
 *
 * `y` is read inside each `graphicsLayer` block, so scrolling invalidates only the draw
 * layers — no recomposition.
 */
@Composable
private fun WalletStack(cards: List<WalletCard>) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val viewportHeightDp = maxHeight
        val viewportPx = with(density) { maxHeight.toPx() }

        val cardWidthDp = maxWidth * 0.85f
        val cardHeightDp = cardWidthDp * (228f / 362f)
        val marginDp = 16.dp
        val slotDp = cardHeightDp + marginDp * 2
        val chPx = with(density) { slotDp.toPx() }

        val scroll = rememberScrollState()

        Column(Modifier.fillMaxSize().verticalScroll(scroll)) {
            cards.forEachIndexed { index, card ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(slotDp)
                        .graphicsLayer {
                            val y = scroll.value.toFloat()
                            val position = index * chPx - y

                            val isTop = 0f
                            val isBottom = viewportPx - chPx
                            val isAppearing = viewportPx
                            val isDisappearing = -chPx

                            // Sticky-to-top: hold the card until y reaches its slot, then pin.
                            val stick = y - min(y, index * chPx)
                            // Tiny nudge as a card appears from the bottom.
                            val bottom = interp(
                                position, isBottom, isAppearing, 0f, -chPx / 4f,
                            )
                            translationY = stick + bottom

                            val s = interp4(
                                position, isDisappearing, isTop, isBottom, isAppearing,
                                0.5f, 1f, 1f, 0.5f,
                            )
                            scaleX = s
                            scaleY = s
                            alpha = interp4(
                                position, isDisappearing, isTop, isBottom, isAppearing,
                                0.5f, 1f, 1f, 0.5f,
                            ).coerceIn(0f, 1f)
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    WalletCardItem(
                        card = card,
                        modifier = Modifier.width(cardWidthDp).height(cardHeightDp),
                    )
                }
            }
            // Trailing space so the last cards can scroll all the way up and stick.
            Spacer(Modifier.height(viewportHeightDp))
        }
    }
}

@Composable
private fun WalletCardItem(card: WalletCard, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(listOf(Color(card.gradientStart), Color(card.gradientEnd))),
            )
            .padding(20.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(card.network, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            // Chip
            Box(
                Modifier
                    .size(width = 44.dp, height = 32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White.copy(alpha = 0.85f)),
            )
            Text(
                "•••• •••• •••• ${card.last4}",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
            )
            Text(card.holder.uppercase(), color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
        }
    }
}

/** Two-stop clamped linear interpolation (matches RN `interpolate` with clamp). */
private fun interp(x: Float, inA: Float, inB: Float, outA: Float, outB: Float): Float {
    if (inA == inB) return outA
    val t = ((x - inA) / (inB - inA)).coerceIn(0f, 1f)
    return outA + (outB - outA) * t
}

/** Four-stop clamped piecewise interpolation. Assumes i0 < i1 < i2 < i3. */
private fun interp4(
    x: Float,
    i0: Float, i1: Float, i2: Float, i3: Float,
    o0: Float, o1: Float, o2: Float, o3: Float,
): Float = when {
    x <= i0 -> o0
    x < i1 -> interp(x, i0, i1, o0, o1)
    x < i2 -> interp(x, i1, i2, o1, o2)
    x < i3 -> interp(x, i2, i3, o2, o3)
    else -> o3
}
