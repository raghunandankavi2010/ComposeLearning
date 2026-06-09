package com.example.composelearning.pager

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.hypot

/**
 * A stacked-card carousel where the front card sits at the center and the rest fan toward the
 * top-right of the screen with decreasing scale, alpha and a positive Z rotation. Drag the front
 * card past a threshold in any direction to dismiss it; back cards smoothly shift forward
 * one position as you drag (and slot into place when the dismiss completes).
 */
@Composable
fun TopRightFanCarousel(
    cards: List<CarouselCard>,
    modifier: Modifier = Modifier,
    cardWidth: Dp = 280.dp,
    cardHeight: Dp = 380.dp,
    visibleStackDepth: Int = 4,
    stackStepXDp: Dp = 44.dp,
    stackStepYDp: Dp = (-32).dp,
    stackStepScale: Float = 0.085f,
    stackStepAlpha: Float = 0.24f,
    stackStepRotation: Float = 7f,
) {
    if (cards.isEmpty()) return
    val scope = rememberCoroutineScope()
    var currentIndex by remember { mutableIntStateOf(0) }
    var isAdvancing by remember { mutableStateOf(false) }
    val dragX = remember { Animatable(0f) }
    val dragY = remember { Animatable(0f) }

    val density = LocalDensity.current
    val stepX = with(density) { stackStepXDp.toPx() }
    val stepY = with(density) { stackStepYDp.toPx() }
    val dismissDistancePx = with(density) { 160.dp.toPx() }

    val dragMagnitude by remember {
        derivedStateOf {
            (hypot(dragX.value, dragY.value) / dismissDistancePx).coerceIn(0f, 1f)
        }
    }

    Box(modifier, contentAlignment = Alignment.Center) {
        val visible = visibleStackDepth.coerceAtMost(cards.size - 1)
        // Draw back-to-front so the front card receives gestures (it's drawn last = on top)
        for (depth in visible downTo 0) {
            val cardIndex = (currentIndex + depth) % cards.size
            val card = cards[cardIndex]
            val isFront = depth == 0

            CarouselCardItem(
                card = card,
                modifier = Modifier
                    .size(cardWidth, cardHeight)
                    .graphicsLayer {
                        if (isFront) {
                            translationX = dragX.value
                            translationY = dragY.value
                            scaleX = 1f
                            scaleY = 1f
                            alpha = 1f - dragMagnitude * 0.45f
                            // Slight tilt while dragging — feels physical
                            rotationZ = dragX.value / 28f
                        } else {
                            // effectiveDepth shrinks from `depth` toward `depth-1` as the user drags,
                            // so back cards smoothly slot into the position of the one in front
                            val effectiveDepth = depth - dragMagnitude
                            translationX = effectiveDepth * stepX
                            translationY = effectiveDepth * stepY
                            val s = (1f - effectiveDepth * stackStepScale).coerceAtLeast(0.55f)
                            scaleX = s
                            scaleY = s
                            alpha = (1f - effectiveDepth * stackStepAlpha).coerceIn(0f, 1f)
                            rotationZ = effectiveDepth * stackStepRotation
                        }
                        transformOrigin = TransformOrigin(0.5f, 0.5f)
                    }
                    .let { m ->
                        if (isFront && !isAdvancing) {
                            m.systemGestureExclusion().pointerInput(currentIndex) {
                                detectDragGestures(
                                    onDrag = { change, drag ->
                                        change.consume()
                                        scope.launch {
                                            dragX.snapTo(dragX.value + drag.x)
                                            dragY.snapTo(dragY.value + drag.y)
                                        }
                                    },
                                    onDragEnd = {
                                        val dist = hypot(dragX.value, dragY.value)
                                        if (dist > dismissDistancePx * 0.7f) {
                                            scope.launch {
                                                isAdvancing = true
                                                val dirX = dragX.value / dist
                                                val dirY = dragY.value / dist
                                                val flyTarget = dismissDistancePx * 4.5f
                                                val jobs = listOf(
                                                    launch { dragX.animateTo(dirX * flyTarget, tween(260)) },
                                                    launch { dragY.animateTo(dirY * flyTarget, tween(260)) },
                                                )
                                                jobs.forEach { it.join() }
                                                currentIndex = (currentIndex + 1) % cards.size
                                                dragX.snapTo(0f)
                                                dragY.snapTo(0f)
                                                isAdvancing = false
                                            }
                                        } else {
                                            scope.launch {
                                                listOf(
                                                    launch { dragX.animateTo(0f, spring(stiffness = Spring.StiffnessLow)) },
                                                    launch { dragY.animateTo(0f, spring(stiffness = Spring.StiffnessLow)) },
                                                ).forEach { it.join() }
                                            }
                                        }
                                    },
                                    onDragCancel = {
                                        scope.launch {
                                            dragX.animateTo(0f, spring())
                                            dragY.animateTo(0f, spring())
                                        }
                                    },
                                )
                            }
                        } else m
                    },
            )
        }
    }
}

@Immutable
data class CarouselCard(
    val title: String,
    val subtitle: String,
    val tag: String,
    val brush: Brush,
)

@Composable
private fun CarouselCardItem(
    card: CarouselCard,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .shadow(elevation = 14.dp, shape = RoundedCornerShape(28.dp))
            .clip(RoundedCornerShape(28.dp))
            .background(card.brush),
    ) {
        // Subtle top-right tag chip
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .background(
                    color = Color.White.copy(alpha = 0.22f),
                    shape = RoundedCornerShape(20.dp),
                )
                .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            androidx.compose.material3.Text(
                text = card.tag,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 28.dp),
        ) {
            androidx.compose.material3.Text(
                text = card.title,
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 36.sp,
            )
            Spacer(Modifier.size(6.dp))
            androidx.compose.material3.Text(
                text = card.subtitle,
                color = Color.White.copy(alpha = 0.88f),
                fontSize = 14.sp,
                lineHeight = 18.sp,
            )
            Spacer(Modifier.height(20.dp))
            // Decorative thin progress underline
            Box(
                modifier = Modifier
                    .height(3.dp)
                    .fillMaxWidth(0.35f)
                    .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(2.dp)),
            )
        }
    }
}