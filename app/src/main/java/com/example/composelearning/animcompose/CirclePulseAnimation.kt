package com.example.composelearning.animcompose

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * A highly polished pulsating animation similar to Google Maps.
 * It uses multiple ripple layers with staggered start times to create a fluid outward motion.
 */
@Composable
fun MapsStylePulsatingCircle() {
    val infiniteTransition = rememberInfiniteTransition(label = "MapsPulse")

    // The primary pulse value that goes from 0 to 1
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseProgress"
    )

    val blueColor = Color(0xFF4285F4) // Signature Google Maps Blue

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA)) // Light Google Maps-like background
            .systemBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(300.dp)) {
            val centerRadius = 8.dp.toPx()
            val maxRippleRadius = 100.dp.toPx()

            // We draw 3 ripple layers using the same progress but with different offsets
            // to create a continuous outward ripple effect.
            val ripples = listOf(
                progress,             // Ripple 1
                (progress + 0.33f) % 1f, // Ripple 2 (staggered)
                (progress + 0.66f) % 1f  // Ripple 3 (staggered)
            )

            ripples.forEach { rippleProgress ->
                val alpha = (1f - rippleProgress) * 0.35f
                val radius = centerRadius + (maxRippleRadius - centerRadius) * rippleProgress
                
                // Outer soft halo
                drawCircle(
                    color = blueColor.copy(alpha = alpha),
                    radius = radius,
                    style = Fill
                )
                
                // Subtle outline for the ripple
                drawCircle(
                    color = blueColor.copy(alpha = alpha * 0.5f),
                    radius = radius,
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            // High-contrast center dot
            // 1. White border/glow
            drawCircle(
                color = Color.White,
                radius = centerRadius + 2.dp.toPx(),
                style = Fill
            )
            // 2. The blue core
            drawCircle(
                color = blueColor,
                radius = centerRadius,
                style = Fill
            )
        }
    }
}

/**
 * A simpler pulsating effect that uses RepeatMode.Reverse for a "breathing" feel.
 */
@Composable
fun PulsatingCircle2() {
    val infiniteTransition = rememberInfiniteTransition(label = "PulsatingCircles")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1115))
            .systemBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(200.dp)) {
            val baseRadius = size.minDimension / 2
            
            // Outer glow
            drawCircle(
                color = Color(0xFF6C63FF).copy(alpha = 0.2f * (1f - scale + 0.6f)),
                radius = baseRadius * scale,
                style = Fill
            )

            // Inner core
            drawCircle(
                color = Color(0xFF6C63FF).copy(alpha = 0.8f),
                radius = baseRadius * 0.4f,
                style = Fill
            )
        }
    }
}
