/*
 * Copyright 2024 Raghunandan Kavi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Created by Raghunandan Kavi on 24th May 2024.
 */

package com.example.composelearning.animcompose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// ====================================================================================
// Particle3D DATA CLASS
// ====================================================================================
/**
 * Represents a single particle in 3D space.
 *
 * PHYSICS EXPLAINED:
 * - Position (posX, posY, posZ) is updated every frame using its velocity.
 * - Velocity (velX, velY, velZ) is influenced by gravity and air resistance (drag).
 * - The simulation uses Euler Integration:
 *      velocity = velocity + acceleration * time
 *      position = position + velocity * time
 */
class Particle3D(
    val originX: Float,
    val originY: Float,
    var velX: Float,
    var velY: Float,
    var velZ: Float,
    val baseSize: Float,
    val color: Color,
    val maxLifeMillis: Float,
    val gravityScale: Float = 1f,
    val drag: Float = 0.98f // Simulates air resistance (momentum loss per frame)
) {
    var ageMillis: Float = 0f
    var isAlive: Boolean = true

    // Current relative 3D position (offset from origin)
    var posX: Float = 0f
    var posY: Float = 0f
    var posZ: Float = 0f

    // Projected 2D screen coordinates and size (calculated during projection)
    var screenX: Float = originX
    var screenY: Float = originY
    var currentSize: Float = baseSize
    var currentAlpha: Float = 1f
    var currentScale: Float = 1f

    // Random phase offset (0 to 2PI) for the sparkle animation's sine wave
    // This ensures particles don't all twinkle in perfect sync.
    val sparkleOffset = Random.nextFloat() * PI.toFloat()
}

// ====================================================================================
// EXPLOSION SYSTEM STATE
// ====================================================================================
class ExplosionSystem(
    val particles: MutableList<Particle3D> = mutableStateListOf(),
    val gravity: Float = 980f,      // Earth-like gravity magnitude (pixels/sec^2)
    val focalLength: Float = 800f   // Distance from camera to screen (controls perspective intensity)
) {
    companion object {
        /**
         * Creates a burst of particles using spherical distribution.
         */
        fun createExplosion(
            originX: Float,
            originY: Float,
            count: Int = 150,
            gravity: Float = 980f,
            focalLength: Float = 800f
        ): ExplosionSystem {
            val system = ExplosionSystem(gravity = gravity, focalLength = focalLength)

            repeat(count) {
                // MATH: Spherically uniform random distribution
                // 1. Azimuth (horizontal angle around the Y-axis): Range [0, 2π]
                val azimuth = Random.nextDouble(0.0, 2.0 * PI).toFloat()
                // 2. Elevation (vertical angle from the X-Z plane): Range [-π/2, π/2]
                val elevation = Random.nextDouble(-PI / 2.0, PI / 2.0).toFloat()

                // Speed magnitude: Random value between 200 and 800 units/second
                val speed = Random.nextFloat() * 600f + 200f

                // MATH: Spherical to Cartesian conversion
                // x = speed * cos(elevation) * cos(azimuth)
                // y = speed * sin(elevation)
                // z = speed * cos(elevation) * sin(azimuth)
                val vx = speed * cos(elevation) * cos(azimuth)
                // Note: -200f is an "upward" initial impulse (screen -Y is up)
                val vy = speed * sin(elevation) - 200f 
                val vz = speed * cos(elevation) * sin(azimuth)

                // Variance in size and lifetime
                val baseSize = Random.nextFloat() * 4f + 2f
                val maxLife = Random.nextFloat() * 1200f + 1000f
                val gravityScale = Random.nextFloat() * 0.5f + 0.75f

                val color = when (Random.nextInt(6)) {
                    0 -> Color(0xFFFF5722) // Deep Orange
                    1 -> Color(0xFFFF9800) // Orange
                    2 -> Color(0xFFFFEB3B) // Yellow
                    3 -> Color(0xFFF44336) // Red
                    4 -> Color(0xFFFFFFFF) // White (Hot core)
                    else -> Color(0xFFFF7043)
                }

                system.particles.add(
                    Particle3D(
                        originX = originX,
                        originY = originY,
                        velX = vx,
                        velY = vy,
                        velZ = vz,
                        baseSize = baseSize,
                        color = color,
                        maxLifeMillis = maxLife,
                        gravityScale = gravityScale
                    )
                )
            }
            return system
        }
    }

    /**
     * Advances the simulation.
     * @param deltaMillis elapsed time since the previous frame in milliseconds.
     */
    fun update(deltaMillis: Float) {
        // MATH: Convert milliseconds to seconds for physics units (units/sec)
        val dtSeconds = deltaMillis / 1000f

        particles.forEach { p ->
            if (!p.isAlive) return@forEach

            p.ageMillis += deltaMillis
            if (p.ageMillis >= p.maxLifeMillis) {
                p.isAlive = false
                return@forEach
            }

            // --- 1. PHYSICS UPDATE (Euler Integration) ---
            
            // a. Update Velocity with Gravity: v = v0 + a * dt
            // gravityScale allows particles to fall at slightly different rates.
            p.velY += (gravity * p.gravityScale) * dtSeconds

            // b. Apply Air Resistance (Drag): v = v * drag_coefficient
            // Simple model where particle loses a percentage of speed every frame.
            p.velX *= p.drag
            p.velY *= p.drag
            p.velZ *= p.drag

            // c. Update Position: s = s + v * dt
            p.posX += p.velX * dtSeconds
            p.posY += p.velY * dtSeconds
            p.posZ += p.velZ * dtSeconds

            // --- 2. 3D TO 2D PERSPECTIVE PROJECTION ---
            
            // MATH: Projection Scale Formula
            // scale = focalLength / (focalLength + depth)
            // As depth (posZ) increases, the scale factor decreases, pushing coordinates
            // closer to the origin and shrinking the object size.
            val scale = focalLength / (focalLength + p.posZ)
            p.currentScale = scale
            
            // Map 3D position to 2D screen coordinates:
            // screen_coord = origin + (relative_pos * scale)
            p.screenX = p.originX + p.posX * scale
            p.screenY = p.originY + p.posY * scale
            
            // Adjust visual size based on depth
            p.currentSize = p.baseSize * scale

            // --- 3. VISUAL EFFECTS MATH ---

            // Normalize age into a ratio [0.0, 1.0]
            val lifeRatio = p.ageMillis / p.maxLifeMillis
            
            // MATH: Sparkle Oscillation
            // We use a sine wave to flicker the alpha: alpha = sin(time * freq + phase) * amp + base
            // freq = 0.05, phase = sparkleOffset, amp = 0.2, base = 0.8 (flicker between 0.6 and 1.0)
            val flicker = (sin(p.ageMillis * 0.05f + p.sparkleOffset) * 0.2f + 0.8f)
            
            // MATH: Alpha Fade Calculation
            // Constant alpha (1.0) for the first 60% of life, then linear decay to 0.0.
            // Decay formula: 1 - ((current - start) / range)
            p.currentAlpha = (if (lifeRatio < 0.6f) 1f else 1f - ((lifeRatio - 0.6f) / 0.4f)) * flicker
            p.currentAlpha = p.currentAlpha.coerceIn(0f, 1f)
        }
    }

    fun isComplete(): Boolean = particles.all { !it.isAlive }
}

@Composable
fun ParticleExpExplosion3D(
    modifier: Modifier = Modifier
) {
    val explosions = remember { mutableStateListOf<ExplosionSystem>() }
    val fpsCounter = remember { mutableFloatStateOf(0f) }
    val particleCount = remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        var lastFrameTime = 0L
        var fpsFrameCount = 0
        var fpsElapsedTime = 0L

        while (isActive) {
            val frameTime = withFrameNanos { it }
            if (lastFrameTime != 0L) {
                // MATH: Delta time in ms = (currentNanos - lastNanos) / 1,000,000
                val deltaMillis = (frameTime - lastFrameTime) / 1_000_000f

                // FPS Logic: count frames over a 500ms window
                fpsFrameCount++
                fpsElapsedTime += (frameTime - lastFrameTime)
                if (fpsElapsedTime >= 500_000_000L) {
                    fpsCounter.floatValue = fpsFrameCount * 2f // frames * (1s / 0.5s)
                    fpsFrameCount = 0
                    fpsElapsedTime = 0L
                }

                explosions.forEach { it.update(deltaMillis) }
                explosions.removeAll { it.isComplete() }
                particleCount.intValue = explosions.sumOf { it.particles.count { p -> p.isAlive } }
            }
            lastFrameTime = frameTime
        }
    }

    Box(modifier = modifier.fillMaxSize().background(Color(0xFF050505))) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        // Add a new system at the tap location (x, y)
                        explosions.add(ExplosionSystem.createExplosion(offset.x, offset.y))
                    }
                }
        ) {
            explosions.forEach { system ->
                system.particles.forEach { p ->
                    if (p.isAlive) {
                        drawParticle(p)
                    }
                }
            }
        }

        ExplosionHUD(fpsCounter.floatValue, particleCount.intValue, explosions.size)

        if (explosions.isEmpty()) {
            Text(
                text = "Tap to Spark\n\u2728 3D Physics \u2728",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 18.sp,
                modifier = Modifier.align(Alignment.Center),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

private fun DrawScope.drawParticle(p: Particle3D) {
    val alpha = p.currentAlpha
    val size = p.currentSize
    val center = Offset(p.screenX, p.screenY)

    // VISUAL MATH: Multi-layer Glow
    // We use BlendMode.Screen for additive blending (simulates light emission).
    
    // 1. Soft Outer Glow (3x radius, 20% alpha)
    drawCircle(
        color = p.color.copy(alpha = alpha * 0.2f),
        radius = size * 3f,
        center = center,
        blendMode = BlendMode.Screen
    )
    
    // 2. Core (1x radius, full alpha)
    drawCircle(
        color = p.color.copy(alpha = alpha),
        radius = size,
        center = center,
        blendMode = BlendMode.Screen
    )

    // 3. Hot highlight (0.4x radius, white, visible only when bright)
    if (alpha > 0.8f) {
        drawCircle(
            color = Color.White.copy(alpha = alpha * 0.5f),
            radius = size * 0.4f,
            center = center
        )
    }
}

@Composable
private fun ExplosionHUD(fps: Float, particles: Int, systems: Int) {
    Column(modifier = Modifier.padding(16.dp)) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.4f)),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text("FPS: ${fps.toInt()}", color = Color.Green, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("Particles: $particles", color = Color.White, fontSize = 10.sp)
                Text("Systems: $systems", color = Color.White, fontSize = 10.sp)
            }
        }
    }
}
