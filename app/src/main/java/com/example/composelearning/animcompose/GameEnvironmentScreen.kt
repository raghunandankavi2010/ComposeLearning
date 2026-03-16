package com.example.composelearning.animcompose

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*
import kotlin.random.Random

enum class GameState { TUTORIAL, PLAYING, GAME_OVER }
enum class BubbleType { NORMAL, BONUS, GOLDEN, BOMB }

data class Bubble(
    val id: Int,
    val position: Offset,
    val velocity: Offset,
    val radius: Float,
    val type: BubbleType
)

data class BubbleParticle(
    val position: Offset,
    val velocity: Offset,
    val color: Color,
    val radius: Float,
    val alpha: Float
)

data class PopEffect(
    val particles: List<BubbleParticle>
)

@Composable
fun GameEnvironmentScreen() {
    var gameState by remember { mutableStateOf(GameState.TUTORIAL) }
    var score by remember { mutableIntStateOf(0) }
    var lives by remember { mutableIntStateOf(3) }
    var highScore by remember { mutableIntStateOf(0) }
    var bubbles by remember { mutableStateOf(emptyList<Bubble>()) }
    var popEffects by remember { mutableStateOf(emptyList<PopEffect>()) }
    var lastFrameTime by remember { mutableLongStateOf(0L) }
    var animationTime by remember { mutableFloatStateOf(0f) }
    var spawnTimer by remember { mutableFloatStateOf(0f) }

    // Game loop
    LaunchedEffect(gameState) {
        if (gameState == GameState.PLAYING) {
            while (true) {
                withFrameNanos { frameTimeNanos ->
                    val deltaTime = if (lastFrameTime != 0L) {
                        ((frameTimeNanos - lastFrameTime) / 1_000_000f / 1000f).coerceAtMost(0.05f)
                    } else {
                        16f / 1000f
                    }
                    lastFrameTime = frameTimeNanos
                    animationTime += deltaTime

                    // Spawn bubbles
                    spawnTimer -= deltaTime
                    if (spawnTimer <= 0f) {
                        val newBubble = createBubble(animationTime)
                        bubbles = bubbles + newBubble
                        spawnTimer = Random.nextFloat() * 0.5f + 0.8f
                    }

                    // Update bubbles
                    val updatedBubbles = mutableListOf<Bubble>()
                    bubbles.forEach { bubble ->
                        val updated = updateBubble(bubble, bubbles, deltaTime)
                        if (updated.position.y < -100f) {
                            if (updated.type != BubbleType.BOMB) {
                                lives--
                                if (lives <= 0) {
                                    gameState = GameState.GAME_OVER
                                    if (score > highScore) highScore = score
                                }
                            }
                        } else {
                            updatedBubbles.add(updated)
                        }
                    }
                    bubbles = updatedBubbles

                    // Update pop effects
                    popEffects = popEffects.mapNotNull { effect ->
                        val updatedParticles = effect.particles.mapNotNull { particle ->
                            val updatedParticle = BubbleParticle(
                                position = particle.position + particle.velocity * deltaTime,
                                velocity = particle.velocity * 0.97f,
                                color = particle.color,
                                radius = particle.radius,
                                alpha = (particle.alpha - deltaTime * 2f).coerceAtLeast(0f)
                            )
                            if (updatedParticle.alpha > 0f) updatedParticle else null
                        }
                        if (updatedParticles.isNotEmpty()) PopEffect(updatedParticles) else null
                    }
                }
            }
        }
    }

    when (gameState) {
        GameState.TUTORIAL -> {
            TutorialScreen(
                onStartGame = {
                    gameState = GameState.PLAYING
                    score = 0
                    lives = 3
                    bubbles = emptyList()
                    popEffects = emptyList()
                    lastFrameTime = 0L
                    animationTime = 0f
                    spawnTimer = 0f
                }
            )
        }
        GameState.PLAYING -> {
            BubbleGameScreen(
                bubbles = bubbles,
                popEffects = popEffects,
                score = score,
                lives = lives,
                animationTime = animationTime,
                onTap = { offset ->
                    val tappedBubble = bubbles.reversed().find { bubble ->
                        val distance = (offset - bubble.position).getDistance()
                        distance <= bubble.radius
                    }
                    if (tappedBubble != null) {
                        bubbles = bubbles - tappedBubble
                        popEffects = popEffects + createPopEffect(tappedBubble)
                        when (tappedBubble.type) {
                            BubbleType.NORMAL -> score += 10
                            BubbleType.BONUS -> score += 25
                            BubbleType.GOLDEN -> score += 50
                            BubbleType.BOMB -> {
                                lives--
                                score = (score - 20).coerceAtLeast(0)
                                if (lives <= 0) {
                                    gameState = GameState.GAME_OVER
                                    if (score > highScore) highScore = score
                                }
                            }
                        }
                    }
                }
            )
        }
        GameState.GAME_OVER -> {
            GameOverScreen(
                score = score,
                highScore = highScore,
                onRestart = { gameState = GameState.TUTORIAL }
            )
        }
    }
}

@Composable
fun BubbleGameScreen(
    bubbles: List<Bubble>,
    popEffects: List<PopEffect>,
    score: Int,
    lives: Int,
    animationTime: Float,
    onTap: (Offset) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1a237e),
                        Color(0xFF0d47a1),
                        Color(0xFF01579b)
                    )
                )
            )
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        onTap(offset)
                    }
                }
        ) {
            drawBackgroundParticles(animationTime)
            popEffects.forEach { drawPopEffect(it) }
            bubbles.forEach { drawBubble(it, animationTime) }
            drawGameUI(score, lives)
        }
    }
}

@Composable
fun TutorialScreen(onStartGame: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1a237e), Color(0xFF0d47a1), Color(0xFF01579b))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text("🫧 BUBBLE POP 🫧", style = MaterialTheme.typography.headlineLarge, color = Color.Cyan)
            Spacer(modifier = Modifier.height(40.dp))
            Button(onClick = onStartGame) {
                Text("START GAME", modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp))
            }
        }
    }
}

@Composable
fun GameOverScreen(score: Int, highScore: Int, onRestart: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("GAME OVER", style = MaterialTheme.typography.headlineLarge, color = Color.Red)
            Text("Score: $score", color = Color.White)
            Text("Best: $highScore", color = Color.Yellow)
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onRestart) { Text("Try Again") }
        }
    }
}

fun DrawScope.drawBackgroundParticles(animationTime: Float) {
    for (i in 0..15) {
        val offset = i * 123f
        val x = (sin(animationTime * 0.5f + offset) * size.width * 0.3f + size.width / 2f)
        val y = ((animationTime * 20f + offset * 50f) % (size.height + 200f) - 100f)
        val alpha = 0.1f + sin(animationTime * 2f + offset) * 0.05f
        drawCircle(
            color = Color.White.copy(alpha = alpha),
            radius = 3f + sin(animationTime + offset) * 2f,
            center = Offset(x, y)
        )
    }
}

fun DrawScope.drawBubble(bubble: Bubble, animationTime: Float) {
    val pos = bubble.position
    val wobble = sin(animationTime * 3f + bubble.id) * 2f
    val mainColor = when (bubble.type) {
        BubbleType.NORMAL -> Color(0xFF00D4FF)
        BubbleType.BONUS -> Color(0xFF00FF88)
        BubbleType.GOLDEN -> Color(0xFFFFD700)
        BubbleType.BOMB -> Color(0xFFFF3333)
    }

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha = 0.9f), mainColor),
            center = pos + Offset(-bubble.radius * 0.3f + wobble, -bubble.radius * 0.3f)
        ),
        radius = bubble.radius,
        center = pos + Offset(wobble, 0f)
    )
}

fun DrawScope.drawPopEffect(effect: PopEffect) {
    effect.particles.forEach { particle ->
        drawCircle(
            color = particle.color.copy(alpha = particle.alpha),
            radius = particle.radius,
            center = particle.position
        )
    }
}

fun DrawScope.drawGameUI(score: Int, lives: Int) {
    // Basic UI draw calls here
}

fun createBubble(animationTime: Float): Bubble {
    val type = BubbleType.entries.toTypedArray().random()
    val radius = Random.nextFloat() * 20f + 40f
    return Bubble(
        id = animationTime.toInt() + Random.nextInt(1000),
        position = Offset(Random.nextFloat() * 300f + 50f, 900f),
        velocity = Offset(Random.nextFloat() * 30f - 15f, -(Random.nextFloat() * 100f + 100f)),
        radius = radius,
        type = type
    )
}

fun updateBubble(bubble: Bubble, allBubbles: List<Bubble>, deltaTime: Float): Bubble {
    var newVelocity = bubble.velocity
    var newPosition = bubble.position + newVelocity * deltaTime
    return bubble.copy(position = newPosition, velocity = newVelocity)
}

fun createPopEffect(bubble: Bubble): PopEffect {
    val particles = List(15) {
        val angle = Random.nextFloat() * 2f * PI.toFloat()
        val speed = Random.nextFloat() * 200f + 50f
        BubbleParticle(
            position = bubble.position,
            velocity = Offset(cos(angle) * speed, sin(angle) * speed),
            color = Color.Cyan,
            radius = 5f,
            alpha = 1f
        )
    }
    return PopEffect(particles)
}
