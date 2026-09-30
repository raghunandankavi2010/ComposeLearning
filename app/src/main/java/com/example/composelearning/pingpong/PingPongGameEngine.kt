/*
 * Copyright 2026 Raghunandan Kavi
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
 */

package com.example.composelearning.pingpong

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

enum class PingPongGameState {
    READY,
    PLAYING,
    PAUSED,
    GAME_OVER
}

enum class DifficultyMode(val displayName: String, val speedFactor: Float, val aiReaction: Float) {
    PROGRESSIVE("Dynamic Ramp-Up", 1.0f, 0.85f),
    EASY("Easy Casual", 0.8f, 0.65f),
    MEDIUM("Medium Pro", 1.0f, 0.85f),
    HARD("Hard Cyber", 1.25f, 0.96f),
    IMPOSSIBLE("Impossible AI", 1.5f, 0.99f)
}

enum class PowerUpType(val displayName: String, val color: Color, val symbol: String) {
    PADDLE_EXPAND("Wide Paddle", Color(0xFF00E676), "↔"),
    SLOW_MO("Time Warp", Color(0xFF00E5FF), "⏱"),
    MULTI_BALL("Double Ball", Color(0xFFFFD600), "⚽"),
    FIREBALL("Hyper Fire", Color(0xFFFF3D00), "🔥")
}

data class PowerUpItem(
    val id: Long,
    var position: Offset,
    val type: PowerUpType,
    val radius: Float = 22f,
    var activeTime: Float = 0f
)

data class ActivePowerUp(
    val type: PowerUpType,
    var remainingSeconds: Float
)

data class TrailParticle(
    val position: Offset,
    val radius: Float,
    val alpha: Float,
    val color: Color
)

data class SparkParticle(
    var position: Offset,
    var velocity: Offset,
    val color: Color,
    var radius: Float,
    var alpha: Float,
    val maxLife: Float,
    var currentLife: Float = 0f
)

data class Ball(
    var position: Offset,
    var velocity: Offset,
    var radius: Float = 18f,
    var isFireball: Boolean = false,
    var lastHitByPlayer: Boolean = false
)

class PingPongGameEngine {
    // Canvas Dimensions
    var width by mutableFloatStateOf(1000f)
    var height by mutableFloatStateOf(1800f)

    // Game Lifecycle
    var gameState by mutableStateOf(PingPongGameState.READY)
    var mode by mutableStateOf(DifficultyMode.PROGRESSIVE)

    // Scores & Stats
    var playerScore by mutableIntStateOf(0)
    var aiScore by mutableIntStateOf(0)
    var currentRally by mutableIntStateOf(0)
    var maxRally by mutableIntStateOf(0)
    var playTimeSeconds by mutableFloatStateOf(0f)
    var currentLevel by mutableIntStateOf(1)
    var levelProgress by mutableFloatStateOf(0f) // 0.0 to 1.0 towards next level

    // Paddles
    var paddleWidth by mutableFloatStateOf(180f)
    val basePaddleWidth: Float = 180f
    val paddleHeight: Float = 28f

    var playerPaddleX by mutableFloatStateOf(500f)
    var playerPaddleVelocity by mutableFloatStateOf(0f)
    var aiPaddleX by mutableFloatStateOf(500f)
    var aiTargetX by mutableFloatStateOf(500f)

    // Balls (Supports multi-ball power up)
    val balls = mutableStateListOf<Ball>()

    // Effects & Particles
    val trailParticles = mutableStateListOf<TrailParticle>()
    val sparkParticles = mutableStateListOf<SparkParticle>()
    val powerUpsOnField = mutableStateListOf<PowerUpItem>()
    val activePowerUps = mutableStateListOf<ActivePowerUp>()

    // Audio / Haptic Trigger Callbacks
    var onHitSound: (() -> Unit)? = null
    var onWallSound: (() -> Unit)? = null
    var onScoreSound: ((Boolean) -> Unit)? = null // true = player scored
    var onPowerUpSound: (() -> Unit)? = null

    // Screen Shake Effect
    var screenShakeIntensity by mutableFloatStateOf(0f)

    private var powerUpIdCounter = 0L
    private var powerUpSpawnTimer = 0f

    fun resetGame(selectedMode: DifficultyMode = mode) {
        mode = selectedMode
        playerScore = 0
        aiScore = 0
        currentRally = 0
        maxRally = 0
        playTimeSeconds = 0f
        currentLevel = 1
        levelProgress = 0f
        paddleWidth = basePaddleWidth
        activePowerUps.clear()
        powerUpsOnField.clear()
        sparkParticles.clear()
        trailParticles.clear()

        resetBallPosition(playerServing = true)
        playerPaddleX = width / 2f
        aiPaddleX = width / 2f
        gameState = PingPongGameState.READY
    }

    fun startPlaying() {
        if (gameState == PingPongGameState.READY || gameState == PingPongGameState.PAUSED) {
            gameState = PingPongGameState.PLAYING
        }
    }

    fun updateDimensions(w: Float, h: Float) {
        if (w <= 0f || h <= 0f) return
        val ratioX = w / width
        width = w
        height = h

        if (gameState == PingPongGameState.READY) {
            playerPaddleX = width / 2f
            aiPaddleX = width / 2f
            resetBallPosition(playerServing = true)
        } else {
            playerPaddleX = (playerPaddleX * ratioX).coerceIn(paddleWidth / 2f, width - paddleWidth / 2f)
            aiPaddleX = (aiPaddleX * ratioX).coerceIn(paddleWidth / 2f, width - paddleWidth / 2f)
        }
    }

    fun onPlayerDrag(targetX: Float) {
        val oldX = playerPaddleX
        val clampedX = targetX.coerceIn(paddleWidth / 2f, width - paddleWidth / 2f)
        playerPaddleVelocity = clampedX - oldX
        playerPaddleX = clampedX
    }

    fun update(rawDeltaTime: Float) {
        if (gameState != PingPongGameState.PLAYING) return

        // Apply Slow-Mo power-up if active
        val isSlowMo = activePowerUps.any { it.type == PowerUpType.SLOW_MO }
        val dt = if (isSlowMo) rawDeltaTime * 0.5f else rawDeltaTime

        // Update match timer & level difficulty scaling
        playTimeSeconds += dt
        updateLevelDifficulty()

        // Update Active Power-Ups timers
        val iterator = activePowerUps.iterator()
        while (iterator.hasNext()) {
            val pu = iterator.next()
            pu.remainingSeconds -= rawDeltaTime
            if (pu.remainingSeconds <= 0f) {
                iterator.remove()
                if (pu.type == PowerUpType.PADDLE_EXPAND) {
                    paddleWidth = basePaddleWidth
                }
            }
        }

        // Handle Paddle Expand Power-Up
        if (activePowerUps.any { it.type == PowerUpType.PADDLE_EXPAND }) {
            paddleWidth = basePaddleWidth * 1.5f
        } else {
            paddleWidth = basePaddleWidth
        }

        // Update Screen Shake
        if (screenShakeIntensity > 0f) {
            screenShakeIntensity = (screenShakeIntensity - rawDeltaTime * 20f).coerceAtLeast(0f)
        }

        // Spawn Power-Ups randomly
        powerUpSpawnTimer += dt
        if (powerUpSpawnTimer >= 12f) {
            powerUpSpawnTimer = 0f
            if (Random.nextFloat() < 0.65f && powerUpsOnField.size < 2) {
                spawnPowerUp()
            }
        }

        // Update Power-Ups on field
        val puIter = powerUpsOnField.iterator()
        while (puIter.hasNext()) {
            val pu = puIter.next()
            pu.activeTime += dt
            if (pu.activeTime > 15f) {
                puIter.remove()
            }
        }

        // Update AI paddle target & movement
        updateAiPaddle(dt)

        // Sub-step physics update for smooth fast ball movement
        val subSteps = 3
        val subDt = dt / subSteps
        repeat(subSteps) {
            updatePhysicsSubstep(subDt)
        }

        // Particle updates
        updateParticles(dt)

        // Check for round win / loss condition (if no balls left)
        if (balls.isEmpty()) {
            resetBallPosition(playerServing = false)
        }
    }

    private fun updateLevelDifficulty() {
        if (mode == DifficultyMode.PROGRESSIVE) {
            val timeLevel = (playTimeSeconds / 12f).toInt() + 1
            val rallyBonus = currentRally / 5
            val targetLevel = (timeLevel + rallyBonus).coerceIn(1, 15)
            currentLevel = targetLevel

            val currentLevelStart = (currentLevel - 1) * 12f
            levelProgress = ((playTimeSeconds - currentLevelStart) / 12f).coerceIn(0f, 1f)
        } else {
            currentLevel = when (mode) {
                DifficultyMode.EASY -> 1
                DifficultyMode.MEDIUM -> 3
                DifficultyMode.HARD -> 7
                DifficultyMode.IMPOSSIBLE -> 12
                else -> 1
            }
            levelProgress = 1.0f
        }
    }

    private fun updateAiPaddle(dt: Float) {
        val primaryBall = balls.minByOrNull { it.position.y } ?: return

        val aiSpeedBase = 500f + (currentLevel * 120f) * mode.speedFactor
        val aiReactionFactor = (mode.aiReaction + (currentLevel * 0.015f)).coerceAtMost(0.98f)

        if (primaryBall.velocity.y < 0) {
            val distanceToAiY = primaryBall.position.y - 60f
            val timeToImpact = if (primaryBall.velocity.y != 0f) distanceToAiY / primaryBall.velocity.y else 0f
            var predictedX = primaryBall.position.x + (primaryBall.velocity.x * timeToImpact)

            while (predictedX < primaryBall.radius || predictedX > width - primaryBall.radius) {
                if (predictedX < primaryBall.radius) {
                    predictedX = primaryBall.radius + (primaryBall.radius - predictedX)
                } else if (predictedX > width - primaryBall.radius) {
                    predictedX = (width - primaryBall.radius) - (predictedX - (width - primaryBall.radius))
                }
            }

            val maxError = max(0f, 60f - currentLevel * 4f)
            val error = if (currentLevel < 10) (sin(playTimeSeconds * 3f) * maxError) else 0f

            aiTargetX = predictedX + error
        } else {
            aiTargetX = width / 2f
        }

        val dx = aiTargetX - aiPaddleX
        val moveDist = dx * aiReactionFactor * (dt * 15f)
        val clampedMove = moveDist.coerceIn(-aiSpeedBase * dt, aiSpeedBase * dt)
        aiPaddleX = (aiPaddleX + clampedMove).coerceIn(paddleWidth / 2f, width - paddleWidth / 2f)
    }

    private fun updatePhysicsSubstep(dt: Float) {
        val playerPaddleY = height - 90f
        val aiPaddleY = 60f

        val ballIterator = balls.iterator()
        while (ballIterator.hasNext()) {
            val ball = ballIterator.next()

            ball.position = Offset(
                ball.position.x + ball.velocity.x * dt,
                ball.position.y + ball.velocity.y * dt
            )

            if (Random.nextFloat() < 0.8f) {
                trailParticles.add(
                    TrailParticle(
                        position = ball.position,
                        radius = ball.radius * 0.7f,
                        alpha = 0.6f,
                        color = getBallThemeColor(ball)
                    )
                )
            }

            if (ball.position.x - ball.radius <= 0f) {
                ball.position = Offset(ball.radius, ball.position.y)
                ball.velocity = Offset(abs(ball.velocity.x), ball.velocity.y)
                spawnSparkBurst(ball.position, Color(0xFF00E5FF), count = 12)
                onWallSound?.invoke()
            } else if (ball.position.x + ball.radius >= width) {
                ball.position = Offset(width - ball.radius, ball.position.y)
                ball.velocity = Offset(-abs(ball.velocity.x), ball.velocity.y)
                spawnSparkBurst(ball.position, Color(0xFF00E5FF), count = 12)
                onWallSound?.invoke()
            }

            if (ball.velocity.y > 0 &&
                ball.position.y + ball.radius >= playerPaddleY - paddleHeight / 2f &&
                ball.position.y - ball.radius <= playerPaddleY + paddleHeight / 2f
            ) {
                val hitOffset = ball.position.x - playerPaddleX
                if (abs(hitOffset) <= paddleWidth / 2f + ball.radius) {
                    ball.position = Offset(ball.position.x, playerPaddleY - paddleHeight / 2f - ball.radius)

                    val normalizedOffset = (hitOffset / (paddleWidth / 2f)).coerceIn(-1.0f, 1.0f)
                    val bounceAngle = normalizedOffset * (Math.PI / 3.0).toFloat()

                    val baseSpeed = sqrt(ball.velocity.x * ball.velocity.x + ball.velocity.y * ball.velocity.y)
                    val newSpeed = (baseSpeed * 1.04f).coerceAtMost(1800f + currentLevel * 80f)

                    val spinX = playerPaddleVelocity * 0.25f
                    val vx = newSpeed * sin(bounceAngle) + spinX
                    val vy = -newSpeed * cos(bounceAngle)

                    ball.velocity = Offset(vx, vy)
                    ball.lastHitByPlayer = true

                    currentRally++
                    if (currentRally > maxRally) maxRally = currentRally

                    screenShakeIntensity = 6f
                    spawnSparkBurst(ball.position, Color(0xFF00E5FF), count = 22)
                    onHitSound?.invoke()

                    checkPowerUpCollision(ball)
                }
            }

            if (ball.velocity.y < 0 &&
                ball.position.y - ball.radius <= aiPaddleY + paddleHeight / 2f &&
                ball.position.y + ball.radius >= aiPaddleY - paddleHeight / 2f
            ) {
                val hitOffset = ball.position.x - aiPaddleX
                if (abs(hitOffset) <= paddleWidth / 2f + ball.radius) {
                    ball.position = Offset(ball.position.x, aiPaddleY + paddleHeight / 2f + ball.radius)

                    val normalizedOffset = (hitOffset / (paddleWidth / 2f)).coerceIn(-1.0f, 1.0f)
                    val bounceAngle = normalizedOffset * (Math.PI / 3.0).toFloat()

                    val baseSpeed = sqrt(ball.velocity.x * ball.velocity.x + ball.velocity.y * ball.velocity.y)
                    val newSpeed = (baseSpeed * 1.03f).coerceAtMost(1800f + currentLevel * 80f)

                    val vx = newSpeed * sin(bounceAngle)
                    val vy = newSpeed * cos(bounceAngle)

                    ball.velocity = Offset(vx, vy)
                    ball.lastHitByPlayer = false

                    spawnSparkBurst(ball.position, Color(0xFFFF1744), count = 22)
                    onHitSound?.invoke()
                }
            }

            if (ball.position.y < -50f) {
                playerScore++
                onScoreSound?.invoke(true)
                spawnSparkBurst(Offset(ball.position.x, 30f), Color(0xFF00E676), count = 40)
                screenShakeIntensity = 12f
                ballIterator.remove()
            } else if (ball.position.y > height + 50f) {
                aiScore++
                onScoreSound?.invoke(false)
                spawnSparkBurst(Offset(ball.position.x, height - 30f), Color(0xFFFF1744), count = 40)
                screenShakeIntensity = 12f
                ballIterator.remove()
            }
        }
    }

    private fun checkPowerUpCollision(ball: Ball) {
        val puIterator = powerUpsOnField.iterator()
        while (puIterator.hasNext()) {
            val pu = puIterator.next()
            val dx = ball.position.x - pu.position.x
            val dy = ball.position.y - pu.position.y
            val distSq = dx * dx + dy * dy
            val minRadius = ball.radius + pu.radius

            if (distSq <= minRadius * minRadius) {
                applyPowerUp(pu.type)
                spawnSparkBurst(pu.position, pu.type.color, count = 30)
                onPowerUpSound?.invoke()
                puIterator.remove()
            }
        }
    }

    private fun applyPowerUp(type: PowerUpType) {
        when (type) {
            PowerUpType.MULTI_BALL -> {
                if (balls.isNotEmpty()) {
                    val primary = balls.first()
                    val extraBall = Ball(
                        position = primary.position,
                        velocity = Offset(-primary.velocity.x, primary.velocity.y),
                        radius = primary.radius,
                        isFireball = false,
                        lastHitByPlayer = true
                    )
                    balls.add(extraBall)
                }
            }
            PowerUpType.FIREBALL -> {
                balls.forEach { it.isFireball = true }
                activePowerUps.removeAll { it.type == PowerUpType.FIREBALL }
                activePowerUps.add(ActivePowerUp(PowerUpType.FIREBALL, 8f))
            }
            else -> {
                activePowerUps.removeAll { it.type == type }
                activePowerUps.add(ActivePowerUp(type, 8f))
            }
        }
    }

    private fun spawnPowerUp() {
        val margin = 120f
        val px = Random.nextFloat() * (width - margin * 2) + margin
        val py = Random.nextFloat() * (height * 0.4f) + (height * 0.3f)
        val type = PowerUpType.entries.random()

        powerUpsOnField.add(
            PowerUpItem(
                id = ++powerUpIdCounter,
                position = Offset(px, py),
                type = type
            )
        )
    }

    private fun resetBallPosition(playerServing: Boolean) {
        currentRally = 0
        balls.clear()

        val startY = if (playerServing) height - 200f else 200f
        val startVx = (Random.nextFloat() * 200f - 100f)
        val initialSpeed = 650f + (currentLevel * 45f) * mode.speedFactor
        val startVy = if (playerServing) -initialSpeed else initialSpeed

        balls.add(
            Ball(
                position = Offset(width / 2f, startY),
                velocity = Offset(startVx, startVy),
                radius = 18f,
                isFireball = false,
                lastHitByPlayer = playerServing
            )
        )
    }

    private fun updateParticles(dt: Float) {
        val trailIter = trailParticles.iterator()
        while (trailIter.hasNext()) {
            val p = trailIter.next()
            val newAlpha = p.alpha - dt * 3.5f
            if (newAlpha <= 0f) {
                trailIter.remove()
            } else {
                val index = trailParticles.indexOf(p)
                if (index >= 0) {
                    trailParticles[index] = p.copy(alpha = newAlpha, radius = p.radius * 0.94f)
                }
            }
        }

        val sparkIter = sparkParticles.iterator()
        while (sparkIter.hasNext()) {
            val s = sparkIter.next()
            s.currentLife += dt
            if (s.currentLife >= s.maxLife) {
                sparkIter.remove()
            } else {
                val progress = s.currentLife / s.maxLife
                s.position = Offset(
                    s.position.x + s.velocity.x * dt,
                    s.position.y + s.velocity.y * dt
                )
                s.velocity = Offset(s.velocity.x * 0.95f, s.velocity.y * 0.95f)
                s.alpha = 1.0f - progress
                s.radius = s.radius * 0.96f
            }
        }
    }

    private fun spawnSparkBurst(center: Offset, color: Color, count: Int = 18) {
        for (i in 0 until count) {
            val angle = Random.nextFloat() * Math.PI.toFloat() * 2f
            val speed = Random.nextFloat() * 450f + 100f
            val vx = cos(angle) * speed
            val vy = sin(angle) * speed
            val life = Random.nextFloat() * 0.4f + 0.2f

            sparkParticles.add(
                SparkParticle(
                    position = center,
                    velocity = Offset(vx, vy),
                    color = color,
                    radius = Random.nextFloat() * 4f + 3f,
                    alpha = 1f,
                    maxLife = life
                )
            )
        }
    }

    fun getBallThemeColor(ball: Ball): Color {
        if (ball.isFireball) return Color(0xFFFF3D00)
        return when {
            currentLevel <= 2 -> Color(0xFF00E5FF)
            currentLevel <= 4 -> Color(0xFF76FF03)
            currentLevel <= 7 -> Color(0xFFFFD600)
            currentLevel <= 10 -> Color(0xFFFF6D00)
            else -> Color(0xFFD500F9)
        }
    }

    fun getBallSpeedMph(ball: Ball): Int {
        val speedPx = sqrt(ball.velocity.x * ball.velocity.x + ball.velocity.y * ball.velocity.y)
        return (speedPx * 0.08f).toInt()
    }
}
