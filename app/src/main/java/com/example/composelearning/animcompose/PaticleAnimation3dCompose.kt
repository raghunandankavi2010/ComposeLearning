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
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import com.example.composelearning.util.LocalAnimationsEnabled
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

// ====================================================================================
// Particle3D DATA CLASS (Optimized)
// ====================================================================================
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
    val drag: Float = 0.98f 
) {
    var ageMillis: Float = 0f
    var isAlive: Boolean = true

    var posX: Float = 0f
    var posY: Float = 0f
    var posZ: Float = 0f

    var screenX: Float = originX
    var screenY: Float = originY
    var currentSize: Float = baseSize
    var currentAlpha: Float = 1f

    val sparkleOffset = Random.nextFloat() * PI.toFloat()
}

// ====================================================================================
// EXPLOSION SYSTEM
// ====================================================================================
class ExplosionSystem(
    val particles: MutableList<Particle3D> = mutableListOf(), 
    val gravity: Float = 980f,
    val focalLength: Float = 800f,
    val globalDrag: Float = 0.98f
) {
    var totalAliveParticles = 0
        private set

    companion object {
        fun createExplosion(originX: Float, originY: Float): ExplosionSystem {
            val system = ExplosionSystem()
            val count = 180
            repeat(count) {
                val azimuth = Random.nextDouble(0.0, 2.0 * PI).toFloat()
                val elevation = Random.nextDouble(-PI / 2.0, PI / 2.0).toFloat()
                val speed = Random.nextFloat() * 600f + 200f

                val vx = speed * cos(elevation) * cos(azimuth)
                val vy = speed * sin(elevation) - 200f 
                val vz = speed * cos(elevation) * sin(azimuth)

                system.particles.add(
                    Particle3D(
                        originX = originX, originY = originY,
                        velX = vx, velY = vy, velZ = vz,
                        baseSize = Random.nextFloat() * 4f + 2f,
                        color = getRandomColor(),
                        maxLifeMillis = Random.nextFloat() * 1200f + 1000f,
                        gravityScale = Random.nextFloat() * 0.5f + 0.75f,
                        drag = system.globalDrag
                    )
                )
            }
            system.totalAliveParticles = count
            return system
        }

        private fun getRandomColor(): Color = when (Random.nextInt(6)) {
            0 -> Color(0xFFFF5722) 
            1 -> Color(0xFFFF9800) 
            2 -> Color(0xFFFFEB3B) 
            3 -> Color(0xFFF44336) 
            4 -> Color(0xFFFFFFFF) 
            else -> Color(0xFFFF7043)
        }
    }

    fun update(deltaMillis: Float) {
        val dtSeconds = deltaMillis / 1000f
        
        // MATH FIX: Time-corrected drag
        val timeFactor = deltaMillis / 16.666f
        val adjustedDrag = globalDrag.toDouble().pow(timeFactor.toDouble()).toFloat()

        var aliveCount = 0
        for (i in particles.indices) {
            val p = particles[i]
            if (!p.isAlive) continue

            p.ageMillis += deltaMillis
            if (p.ageMillis >= p.maxLifeMillis) {
                p.isAlive = false
                continue
            }

            aliveCount++

            p.velY += (gravity * p.gravityScale) * dtSeconds
            p.velX *= adjustedDrag
            p.velY *= adjustedDrag
            p.velZ *= adjustedDrag

            p.posX += p.velX * dtSeconds
            p.posY += p.velY * dtSeconds
            p.posZ += p.velZ * dtSeconds

            val scale = focalLength / (focalLength + p.posZ)
            p.screenX = p.originX + p.posX * scale
            p.screenY = p.originY + p.posY * scale
            p.currentSize = p.baseSize * scale

            val lifeRatio = p.ageMillis / p.maxLifeMillis
            val flicker = (sin(p.ageMillis * 0.05f + p.sparkleOffset) * 0.2f + 0.8f)
            p.currentAlpha = (if (lifeRatio < 0.6f) 1f else 1f - ((lifeRatio - 0.6f) / 0.4f)) * flicker
        }
        totalAliveParticles = aliveCount
    }

    fun isComplete(): Boolean = totalAliveParticles == 0
}

@Composable
fun ParticleExpExplosion3D(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val explosions = remember { mutableStateListOf<ExplosionSystem>() }
    val fpsCounter = remember { mutableIntStateOf(0) }
    val activeParticlesCount = remember { mutableIntStateOf(0) }

    val animationsEnabled = LocalAnimationsEnabled.current

    LaunchedEffect(animationsEnabled) {
        if (!animationsEnabled) return@LaunchedEffect
        var lastFrameTime = 0L
        var frames = 0
        var elapsed = 0L

        while (isActive) {
            val frameTime = withFrameNanos { it }
            if (lastFrameTime != 0L) {
                val deltaNanos = frameTime - lastFrameTime
                val deltaMillis = deltaNanos / 1_000_000f

                frames++
                elapsed += deltaNanos
                if (elapsed >= 500_000_000L) {
                    fpsCounter.intValue = (frames * 2)
                    frames = 0
                    elapsed = 0
                }

                var currentTotal = 0
                for (i in explosions.indices) {
                    val system = explosions[i]
                    system.update(deltaMillis)
                    currentTotal += system.totalAliveParticles
                }
                
                if (explosions.any { it.isComplete() }) {
                    explosions.removeAll { it.isComplete() }
                }
                
                activeParticlesCount.intValue = currentTotal
            }
            lastFrameTime = frameTime
        }
    }

    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        Canvas(modifier = Modifier.fillMaxSize().pointerInput(Unit) {
            detectTapGestures { offset ->
                explosions.add(ExplosionSystem.createExplosion(offset.x, offset.y))
            }
        }) {
            for (i in explosions.indices) {
                val pList = explosions[i].particles
                for (j in pList.indices) {
                    val p = pList[j]
                    if (p.isAlive) drawParticle(p)
                }
            }
        }

        ExplosionHUD(
            fpsCounter.intValue, 
            activeParticlesCount.intValue, 
            explosions.size,
            modifier = Modifier.padding(contentPadding)
        )
        
        if (activeParticlesCount.intValue == 0 && explosions.isEmpty()) {
            Text(
                text = "Tap to Spark\n✨ 3D Physics (Optimized) ✨",
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
    if (alpha <= 0.01f) return

    val center = Offset(p.screenX, p.screenY)
    
    drawCircle(
        color = p.color,
        radius = p.currentSize * 3f,
        center = center,
        alpha = alpha * 0.2f,
        blendMode = BlendMode.Screen
    )
    
    drawCircle(
        color = p.color,
        radius = p.currentSize,
        center = center,
        alpha = alpha,
        blendMode = BlendMode.Screen
    )

    if (alpha > 0.8f) {
        drawCircle(
            color = Color.White,
            radius = p.currentSize * 0.4f,
            center = center,
            alpha = alpha * 0.5f
        )
    }
}

@Composable
private fun ExplosionHUD(
    fps: Int, 
    particles: Int, 
    systems: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp)) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.4f)),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text("FPS: $fps", color = if (fps >= 60) Color.Cyan else Color.Green, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("Particles: $particles", color = Color.White, fontSize = 10.sp)
                Text("Systems: $systems", color = Color.White, fontSize = 10.sp)
            }
        }
    }
}
