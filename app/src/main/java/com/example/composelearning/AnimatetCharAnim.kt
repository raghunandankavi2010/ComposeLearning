package com.example.composelearning

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A composable that animates text by fading in and sliding up each character one by one.
 *
 * @param modifier The modifier to be applied to the container Box.
 * @param text The text string to animate.
 * @param staggerDelay The delay in milliseconds between each character's animation.
 */
@Composable
fun StaggeredTextAnimation(
    modifier: Modifier = Modifier,
    text: String,
    staggerDelay: Long = 50
) {
    // State to trigger the animation. We'll set this to true when the composable is launched.
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    // A Row to hold each character Composable side-by-side.
    Row(modifier = modifier) {
        text.forEachIndexed { index, char ->
            // Each character is wrapped in its own AnimatedVisibility.
            AnimatedVisibility(
                visible = visible,
                // Define the "enter" animation for when the visibility becomes true.
                enter =
                    // Fade in from 0f to 1f
                    fadeIn(
                        animationSpec = tween(
                            durationMillis = 3000, // Animation duration
                            delayMillis = index * staggerDelay.toInt() // Staggered delay
                        )
                    ) +
                            // Slide in from the bottom. A positive initialOffsetY slides UP.
                            slideInVertically(
                                initialOffsetY = { it / 2 }, // Slide in from half its height down
                                animationSpec = tween(
                                    durationMillis = 3000,
                                    delayMillis = index * staggerDelay.toInt()
                                )
                            )
            ) {
                // The actual character displayed as a Text composable.
                Text(
                    text = char.toString(),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TextAnimationPreview() {
    // A main container to center everything on the screen
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Your requirement: A Box of 100.dp height
        Box(
            modifier = Modifier
                .height(100.dp)
                .border(1.dp, Color.Gray), // Border to visualize the box
            contentAlignment = Alignment.Center // Center the content (the animated text)
        ) {
            // Use our custom animated text composable
            StaggeredTextAnimation(text = "Hello World")
        }
    }
}