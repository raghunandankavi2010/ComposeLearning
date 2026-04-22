package com.example.composelearning.animcompose

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

data class TinderCardModel(
    val id: Int,
    val imageUrl: String,
    val title: String,
    val breed: String
)

@Composable
fun TinderSwipeScreen() {
    val initialCards = remember {
        listOf(
            TinderCardModel(1, "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=800&q=80", "Buddy", "Golden Retriever"),
            TinderCardModel(2, "https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=800&q=80", "Max", "Beagle"),
            TinderCardModel(3, "https://images.unsplash.com/photo-1537151608828-ea2b11777ee8?w=800&q=80", "Bella", "Pomeranian"),
            TinderCardModel(4, "https://images.unsplash.com/photo-1583511655857-d19b40a7a54e?w=800&q=80", "Luna", "French Bulldog"),
            TinderCardModel(5, "https://images.unsplash.com/photo-1598133894008-61f7fdb8cc3a?w=800&q=80", "Charlie", "German Shepherd")
        )
    }

    var cards by remember { mutableStateOf(initialCards) }

    Scaffold(
        containerColor = Color(0xFFF5F5F7)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            if (cards.isEmpty()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No more dogs nearby!", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { cards = initialCards },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Reset Stack")
                    }
                }
            } else {
                // Show up to 4 cards. Reversed to draw top card last.
                val visibleCards = cards.take(4).reversed()
                visibleCards.forEachIndexed { index, card ->
                    val positionFromTop = visibleCards.size - 1 - index
                    key(card.id) {
                        SwipeableCard(
                            card = card,
                            onSwiped = {
                                cards = cards.drop(1)
                            },
                            isSwipeable = positionFromTop == 0,
                            positionFromTop = positionFromTop
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SwipeableCard(
    card: TinderCardModel,
    onSwiped: () -> Unit,
    isSwipeable: Boolean,
    positionFromTop: Int
) {
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    // Use Spring for smooth return and interaction
    val springSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    // Background card visual properties
    val targetScale by animateFloatAsState(
        targetValue = 1f - (positionFromTop * 0.04f),
        animationSpec = springSpec,
        label = "scale"
    )
    val targetTranslationY by animateFloatAsState(
        targetValue = (positionFromTop * 25f),
        animationSpec = springSpec,
        label = "translationY"
    )
    val targetRotation by animateFloatAsState(
        targetValue = when (positionFromTop) {
            1 -> -2f
            2 -> 2f
            3 -> -1f
            else -> 0f
        },
        animationSpec = springSpec,
        label = "rotation"
    )

    Card(
        modifier = Modifier
            .zIndex(10f - positionFromTop)
            .size(320.dp, 520.dp)
            .graphicsLayer {
                // Translation - ONLY apply horizontal offset to the top card
                translationX = if (isSwipeable) offsetX.value else 0f
                translationY = if (isSwipeable) offsetY.value else targetTranslationY.dp.toPx()
                
                // Rotation
                rotationZ = if (isSwipeable) {
                    // Tilt the card as it moves horizontally
                    offsetX.value * 0.04f
                } else {
                    targetRotation
                }

                // Scale
                scaleX = targetScale
                scaleY = targetScale
                
                // Opacity fades slightly for very back cards
                alpha = if (positionFromTop >= 3) 0.6f else 1f
            }
            .pointerInput(isSwipeable) {
                if (!isSwipeable) return@pointerInput
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        scope.launch {
                            offsetX.snapTo(offsetX.value + dragAmount.x)
                            offsetY.snapTo(offsetY.value + dragAmount.y)
                        }
                    },
                    onDragEnd = {
                        val horizontalThreshold = 400f
                        if (abs(offsetX.value) > horizontalThreshold) {
                            scope.launch {
                                val targetX = if (offsetX.value > 0) 1200f else -1200f
                                offsetX.animateTo(targetX, tween(400, easing = FastOutSlowInEasing))
                                onSwiped()
                            }
                        } else {
                            // Snap back to center
                            scope.launch {
                                launch { offsetX.animateTo(0f, springSpec) }
                                launch { offsetY.animateTo(0f, springSpec) }
                            }
                        }
                    }
                )
            }
            .shadow(
                elevation = if (isSwipeable) 12.dp else 4.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = Color.Black.copy(alpha = 0.2f)
            ),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = card.imageUrl,
                contentDescription = card.title,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(28.dp)),
                contentScale = ContentScale.Crop
            )
            
            // Bottom Gradient for text readability
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.5f)
                    .align(Alignment.BottomCenter)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = card.title,
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "2",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = card.breed,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
