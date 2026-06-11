package com.example.composelearning.foldcard.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.composelearning.foldcard.domain.model.FoldCardItem
import kotlinx.coroutines.launch

@Composable
fun FoldCardScreen(
    viewModel: FoldCardViewModel = viewModel(factory = FoldCardViewModel.Factory())
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        val card = state.card
        if (state.isLoading || card == null) {
            CircularProgressIndicator(color = Color.White)
        } else {
            FoldCard(card)
            Text(
                "Pinch vertically to fold",
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 48.dp)
            )
        }
    }
}

/**
 * A card that bends in half along its horizontal middle as you pinch.
 *
 * `scale` runs `1 → 0.5` while pinching; we map it to a fold factor `f = (1−scale)/0.5`
 * in `[0,1]`. The two halves pivot about the shared crease (top half's bottom edge, bottom
 * half's top edge) and rotate on X by `±90°·f` with a perspective camera, while a black
 * overlay fades in to shade the fold. Releasing springs `scale` back to 1 (flat).
 */
@Composable
private fun FoldCard(card: FoldCardItem) {
    val scope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    do {
                        val event = awaitPointerEvent()
                        val zoom = event.calculateZoom()
                        if (zoom != 1f) {
                            val next = (scale.value * zoom).coerceIn(0.5f, 1f)
                            scope.launch { scale.snapTo(next) }
                            event.changes.forEach { it.consume() }
                        }
                    } while (event.changes.any { it.pressed })
                    scope.launch {
                        scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        val cardW = maxWidth * 0.72f
        val cardH = cardW * 1.4f
        val halfH = cardH / 2f
        val f = ((1f - scale.value) / 0.5f).coerceIn(0f, 1f)
        val camera = 16f

        Box(Modifier.width(cardW).height(cardH)) {
            // Top half — pivots about its bottom edge (the crease), tilts back.
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .width(cardW)
                    .height(halfH)
                    .graphicsLayer {
                        transformOrigin = TransformOrigin(0.5f, 1f)
                        rotationX = 90f * f
                        cameraDistance = camera * density
                    }
                    .clipToBounds()
            ) {
                CardFace(card, Modifier.width(cardW).height(cardH))
                Shade(f)
            }

            // Bottom half — pivots about its top edge, tilts forward.
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .width(cardW)
                    .height(halfH)
                    .graphicsLayer {
                        transformOrigin = TransformOrigin(0.5f, 0f)
                        rotationX = -90f * f
                        cameraDistance = camera * density
                    }
                    .clipToBounds()
            ) {
                CardFace(card, Modifier.width(cardW).height(cardH).offset(y = -halfH))
                Shade(f)
            }
        }
    }
}

@Composable
private fun CardFace(card: FoldCardItem, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(listOf(Color(card.gradientStart), Color(card.gradientEnd))))
    ) {
        Text(
            "${card.rank}${card.suit}",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.TopStart).padding(12.dp)
        )
        Text(
            "${card.rank}${card.suit}",
            color = Color.White.copy(alpha = 0.95f),
            fontSize = 96.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Center)
        )
        Text(
            "${card.rank}${card.suit}",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp)
        )
    }
}

@Composable
private fun Shade(f: Float) {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f * f)))
}
