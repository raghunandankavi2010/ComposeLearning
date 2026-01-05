package com.example.composelearning.animcompose

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import kotlin.math.pow

// Particle data class remains the same
data class Particle1(
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

    val particles = remember { mutableStateListOf<Particle1>() }
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

            val newParticle = Particle1(
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
            minDuration = 700L,
            maxDuration = 1000L,
            // ⭐ Using the new scale parameters for a dramatic effect
            minRadiusDp = 0.5.dp, // Starts very small
            maxRadiusDp = 8.dp    // Grows significantly
        )
    }
}



/**
 * Data class representing a single physical particle.
 */
data class RealisticParticle(
    val color: Color,
    val vx: Float,          // Velocity X (pixels per tick)
    val vy: Float,          // Velocity Y (pixels per tick)
    val gravity: Float,     // Constant acceleration downward
    val drag: Float,        // Friction coefficient (0.0 to 1.0)
    val maxLife: Long,      // Total lifespan in ms
    val startTime: Long,    // Birth timestamp
    val initialSize: Float  // Starting radius
)

@Composable
fun RealisticExplosionScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A)), // Dark background for contrast
        contentAlignment = Alignment.Center
    ) {
        ContinuousExplosionSystem()
    }
}

@Composable
fun ContinuousExplosionSystem() {
    val particles = remember { mutableStateListOf<RealisticParticle>() }
    var currentTime by remember { mutableLongStateOf(0L) }
    var appStartTime by remember { mutableStateOf<Long?>(null) }

    // 1. ANIMATION TICKER: Drives the clock for physics calculations
    LaunchedEffect(Unit) {
        while (true) {
            withFrameMillis { frameTime ->
                if (appStartTime == null) appStartTime = frameTime
                currentTime = frameTime - (appStartTime!!)
            }
        }
    }

    // 2. SPAWN LOOP: Creates new bursts or individual particles
    LaunchedEffect(Unit) {
        while (true) {
            // Spawn a burst of particles at once
            val burstSize = Random.nextInt(5, 15)
            val burstColor = Color(Random.nextFloat(), Random.nextFloat(), Random.nextFloat(), 1f)

            repeat(burstSize) {
                val angle = Random.nextFloat() * 2 * PI.toFloat()
                val speed = Random.nextFloat() * 12f + 4f // Randomized launch speed

                particles.add(
                    RealisticParticle(
                        color = burstColor,
                        vx = cos(angle) * speed,
                        vy = sin(angle) * speed,
                        gravity = 0.2f,        // Adjust for "heaviness"
                        drag = 0.96f,           // 0.98 = thin air, 0.90 = thick water
                        maxLife = Random.nextLong(800, 1500),
                        startTime = currentTime,
                        initialSize = Random.nextFloat() * 10f + 5f
                    )
                )
            }
            delay(Random.nextLong(20, 100)) // Frequency of sparks
        }
    }

    // 3. CLEANUP: Remove dead particles
    LaunchedEffect(currentTime) {
        particles.removeAll { currentTime - it.startTime > it.maxLife }
    }

    // 4. DRAWING: Calculates position based on physics equations
    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)

        particles.forEach { particle ->
            val lifeTime = currentTime - particle.startTime
            if (lifeTime < 0) return@forEach

            // Convert time to "ticks" (roughly 60fps) for easier physics math
            val t = lifeTime / 16f

            /**
             * PHYSICS MATH:
             * x = v0 * t * drag^t
             * y = (v0 * t + 0.5 * g * t^2) * drag^t
             */
            val friction = particle.drag.pow(t)
            val dx = (particle.vx * t) * friction
            val dy = (particle.vy * t + 0.5f * particle.gravity * t.pow(2)) * friction

            val progress = lifeTime.toFloat() / particle.maxLife
            val alpha = (1f - progress).coerceIn(0f, 1f)
            val currentRadius = particle.initialSize * (1f - progress)

            drawCircle(
                color = particle.color.copy(alpha = alpha),
                radius = currentRadius,
                center = Offset(center.x + dx, center.y + dy)
            )
        }
    }
}