package com.example.composelearning.animcompose

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

// Particle data class remains the same
data class Particle(
    val initialColor: Color,
    val initialAngleRad: Float,
    val maxDistance: Float,
    val durationMillis: Long,
    val startTime: Long
)

@Composable
fun ContinuousParticleStream(
    modifier: Modifier = Modifier.fillMaxSize(),
    circleRadiusDp: Dp = 150.dp,
    minDuration: Long = 800L,
    maxDuration: Long = 5000L,
    minSpawnDelay: Long = 2L,
    maxSpawnDelay: Long = 10L,
    // ⭐ NEW SCALE PARAMETERS ⭐
    minRadiusDp: Dp = 1.dp,      // Starting size at the center
    maxRadiusDp: Dp = 6.dp       // Max size reached mid-travel
) {
    // 1. Setup State and Units
    val circleRadiusPx = with(LocalDensity.current) { circleRadiusDp.toPx() }
    val minRadiusPx = with(LocalDensity.current) { minRadiusDp.toPx() }
    val maxRadiusPx = with(LocalDensity.current) { maxRadiusDp.toPx() }

    val particles = remember { mutableStateListOf<Particle>() }
    var currentTime by remember { mutableStateOf(0L) }
    var relativeStartTime by remember { mutableStateOf<Long?>(null) }


    // --- A. Animation Timing Loop ---
    LaunchedEffect(Unit) {
        while (true) {
            withFrameMillis { frameTimeMillis ->
                if (relativeStartTime == null) {
                    relativeStartTime = frameTimeMillis
                }
                currentTime = frameTimeMillis - (relativeStartTime ?: frameTimeMillis)
            }
        }
    }

    // --- B. Particle Generation Loop ---
    LaunchedEffect(relativeStartTime) {
        if (relativeStartTime == null) return@LaunchedEffect

        while (true) {
            // ... (particle creation logic remains the same)
            val angle = Random.nextFloat() * 2 * PI.toFloat()
            val duration = Random.nextLong(minDuration, maxDuration + 1)
            val color = Color(Random.nextFloat(), Random.nextFloat(), Random.nextFloat())

            val newParticle = Particle(
                initialColor = color,
                initialAngleRad = angle,
                maxDistance = circleRadiusPx,
                durationMillis = duration,
                startTime = currentTime
            )

            particles.add(newParticle)

            val spawnDelay = Random.nextLong(minSpawnDelay, maxSpawnDelay + 1)
            delay(spawnDelay)
        }
    }

    // --- C. Cleanup Logic ---
    LaunchedEffect(currentTime) {
        val finishedParticles = particles.filter { particle ->
            val timeAlive = currentTime - particle.startTime
            timeAlive >= particle.durationMillis
        }
        particles.removeAll(finishedParticles)
    }

    // --- D. Drawing on Canvas (UPDATED) ---
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)

        particles.forEach { particle ->
            val timeElapsed = currentTime - particle.startTime

            if (timeElapsed < 0) return@forEach

            // Progress (0.0 to 1.0)
            val progress = (timeElapsed.toFloat() / particle.durationMillis).coerceIn(0f, 1f)

            // Apply Easing for smoother movement
            val easedProgress = FastOutSlowInEasing.transform(progress)

            // --- 1. Movement ---
            val distance = easedProgress * particle.maxDistance
            val x = center.x + distance * cos(particle.initialAngleRad)
            val y = center.y + distance * sin(particle.initialAngleRad)

            // --- 2. SCALE (Size Change) ---
            // Scale up from minRadiusPx to maxRadiusPx in the first half (0.0 to 0.5)
            // Then scale down from maxRadiusPx to minRadiusPx/0 at the end (0.5 to 1.0)
            val radius = if (progress <= 0.5f) {
                // Scale up: Interpolate from min to max in the first 50%
                val scaleFactor = progress / 0.5f
                minRadiusPx + (maxRadiusPx - minRadiusPx) * scaleFactor
            } else {
                // Scale down: Interpolate from max back to min/0 in the second 50%
                val scaleFactor = (progress - 0.5f) / 0.5f // 0.0 to 1.0 over second half
                maxRadiusPx - (maxRadiusPx) * scaleFactor // Fade size completely
            }.coerceAtLeast(0f) // Ensure radius doesn't go negative

            // --- 3. ALPHA (Opacity Change) ---
            // Fade in quickly in the first 10% (0.0 to 0.1)
            // Stay fully opaque (1.0) for the mid-section (0.1 to 0.8)
            // Fade out slowly in the last 20% (0.8 to 1.0)
            val alpha = when {
                progress <= 0.1f -> progress / 0.1f // Fade in
                progress >= 0.8f -> 1f - ((progress - 0.8f) / 0.5f) // Fade out
                else -> 1f // Full opacity
            }.coerceIn(0f, 1f)

            // --- Draw ---
            drawCircle(
                color = particle.initialColor.copy(alpha = alpha),
                radius = radius,
                center = Offset(x, y)
            )
        }
    }
}
@Composable
fun ParticleExplosionScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        ContinuousParticleStream(
            modifier = Modifier.fillMaxSize(),
            circleRadiusDp = 150.dp,
            minSpawnDelay = 2L,
            maxSpawnDelay = 10L,
            minDuration = 1000L,
            maxDuration = 5000L,
            // ⭐ Using the new scale parameters for a dramatic effect
            minRadiusDp = 0.5.dp, // Starts very small
            maxRadiusDp = 8.dp    // Grows significantly
        )
    }
}