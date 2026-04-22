package com.example.composelearning.animcompose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

@Composable
fun StackedImageCards(
    imageResIds: List<Int>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        imageResIds.forEachIndexed { index, imageRes ->
            // Determine the "depth" of the card.
            // Index 0 is the front card (depth 0). Index 1 is right behind it.
            val isFrontCard = index == 0

            // Calculate a slight alternating rotation for the cards in the back
            val rotation = when (index) {
                0 -> 0f    // Front card is straight
                1 -> -6f   // Second card tilts left
                2 -> 8f    // Third card tilts right
                else -> (index % 2 * 10f) - 5f // Randomize further cards
            }

            Card(
                modifier = Modifier
                    // 1. Z-Index ensures index 0 draws on top of index 1, 2, etc.
                    .zIndex(imageResIds.size - index.toFloat())
                    // 2. graphicsLayer handles the transforms performantly
                    .graphicsLayer {
                        rotationZ = rotation

                        // Optional: Shrink the cards slightly the further back they are
                        val scale = 1f - (index * 0.05f)
                        scaleX = scale
                        scaleY = scale

                        // Optional: Push the back cards slightly up or down
                        translationY = -(index * 15f)
                    }
                    .size(width = 300.dp, height = 400.dp)
                    .shadow(
                        elevation = if (isFrontCard) 12.dp else 4.dp,
                        shape = RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // Handled by shadow modifier
            ) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = "Stacked Image $index",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}