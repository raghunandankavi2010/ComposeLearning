package com.example.composelearning.animcompose

/**
 * https://gist.github.com/skydoves/ade49203240be5a0fd781c0e9899a886
 * This code is picked from the above link and belongs to the owner of the gist
 */

import android.graphics.BlurMaskFilter
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*
import kotlin.random.Random

@Composable
fun NewYearsEveFireworksScreen() {
    var isSpinning by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isSpinning = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // Fireworks background
        FireworksBackground()

        // Christmas tree overlay
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo/title area (replace with your own image)
            Text(
                text = "🎄",
                fontSize = 64.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Animated tree
            ChristmasTree(
                isSpinning = isSpinning
            )
        }

        // Title at top
        Text(
            text = "Happy New Year 2026!",
            style = TextStyle(
                color = Color.White,
                fontSize = 20.sp
            ),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp)
        )
    }
}

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var life: Float,
    var maxLife: Float,
    var color: Color,
    var size: Float,
    var type: ParticleType
)

enum class ParticleType {
    TRAIL, EXPLOSION
}

data class Firework(
    var x: Float,
    var y: Float,
    var vy: Float,
    var targetY: Float,
    var color: Color,
    var exploded: Boolean = false,
    var particles: MutableList<Particle> = mutableListOf(),
    var trailParticles: MutableList<Particle> = mutableListOf()
)

class FireworksSystem {
    private val fireworks = mutableListOf<Firework>()
    private val random = Random

    private val colors = listOf(
        Color(0xFFFF7E79), // Coral
        Color(0xFFFFD479), // Peach
        Color(0xFFD4FB79), // Lime
        Color(0xFF49FA79), // Spring Green
        Color(0xFF49FCD6), // Pale Aqua
        Color(0xFF4AD6FF), // Sky Blue
        Color(0xFF7A81FF), // Lavender
        Color(0xFFD883FF), // Purple
        Color.White
    )

    fun update(width: Float, height: Float, deltaTime: Float) {
        // Spawn new fireworks
        if (random.nextFloat() < 0.03f) {
            spawnFirework(width, height)
        }

        // Update existing fireworks
        val iterator = fireworks.iterator()
        while (iterator.hasNext()) {
            val firework = iterator.next()

            if (!firework.exploded) {
                // Rising phase
                firework.y += firework.vy * deltaTime
                firework.vy += 50f * deltaTime // Gravity reduction while rising

                // Add trail particles
                if (random.nextFloat() < 0.8f) {
                    firework.trailParticles.add(
                        Particle(
                            x = firework.x + random.nextFloat() * 4f - 2f,
                            y = firework.y,
                            vx = random.nextFloat() * 20f - 10f,
                            vy = random.nextFloat() * 50f + 20f,
                            life = 0.5f,
                            maxLife = 0.5f,
                            color = firework.color.copy(alpha = 0.8f),
                            size = random.nextFloat() * 3f + 1f,
                            type = ParticleType.TRAIL
                        )
                    )
                }

                // Check if should explode
                if (firework.y <= firework.targetY || firework.vy >= 0) {
                    explode(firework)
                }
            }

            // Update trail particles
            updateParticles(firework.trailParticles, deltaTime, gravity = 80f)

            // Update explosion particles
            updateParticles(firework.particles, deltaTime, gravity = 60f)

            // Remove dead fireworks
            if (firework.exploded && firework.particles.isEmpty() && firework.trailParticles.isEmpty()) {
                iterator.remove()
            }
        }
    }

    private fun spawnFirework(width: Float, height: Float) {
        val color = colors[random.nextInt(colors.size)]
        fireworks.add(
            Firework(
                x = random.nextFloat() * width * 0.8f + width * 0.1f,
                y = height,
                vy = -(random.nextFloat() * 200f + 400f),
                targetY = random.nextFloat() * height * 0.4f + height * 0.1f,
                color = color
            )
        )
    }

    private fun explode(firework: Firework) {
        firework.exploded = true
        val particleCount = random.nextInt(80) + 120

        for (i in 0 until particleCount) {
            val angle = random.nextFloat() * 2f * PI.toFloat()
            val speed = random.nextFloat() * 200f + 50f
            val life = random.nextFloat() * 1.5f + 1f

            firework.particles.add(
                Particle(
                    x = firework.x,
                    y = firework.y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    life = life,
                    maxLife = life,
                    color = firework.color,
                    size = random.nextFloat() * 4f + 2f,
                    type = ParticleType.EXPLOSION
                )
            )
        }
    }

    private fun updateParticles(particles: MutableList<Particle>, deltaTime: Float, gravity: Float) {
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            p.x += p.vx * deltaTime
            p.y += p.vy * deltaTime
            p.vy += gravity * deltaTime
            p.vx *= 0.99f
            p.life -= deltaTime

            if (p.life <= 0) {
                iterator.remove()
            }
        }
    }

    fun draw(drawScope: DrawScope) {
        for (firework in fireworks) {
            // Draw trail particles
            for (p in firework.trailParticles) {
                val alpha = (p.life / p.maxLife).coerceIn(0f, 1f)
                drawScope.drawCircle(
                    color = p.color.copy(alpha = alpha * 0.7f),
                    radius = p.size,
                    center = Offset(p.x, p.y),
                    blendMode = BlendMode.Plus
                )
            }

            // Draw rising firework
            if (!firework.exploded) {
                drawScope.drawCircle(
                    color = firework.color,
                    radius = 4f,
                    center = Offset(firework.x, firework.y),
                    blendMode = BlendMode.Plus
                )
            }

            // Draw explosion particles
            for (p in firework.particles) {
                val alpha = (p.life / p.maxLife).coerceIn(0f, 1f)
                val size = p.size * (0.3f + alpha * 0.7f)

                // Glow effect
                drawScope.drawCircle(
                    color = p.color.copy(alpha = alpha * 0.3f),
                    radius = size * 2f,
                    center = Offset(p.x, p.y),
                    blendMode = BlendMode.Plus
                )

                // Core
                drawScope.drawCircle(
                    color = p.color.copy(alpha = alpha),
                    radius = size,
                    center = Offset(p.x, p.y),
                    blendMode = BlendMode.Plus
                )
            }
        }
    }
}

@Composable
fun FireworksBackground(modifier: Modifier = Modifier) {
    val fireworksSystem = remember { FireworksSystem() }
    var lastFrameTime by remember { mutableLongStateOf(System.nanoTime()) }

    val infiniteTransition = rememberInfiniteTransition(label = "fireworks")
    val frameCount by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000000000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "frame"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val currentTime = System.nanoTime()
        val deltaTime = ((currentTime - lastFrameTime) / 1_000_000_000f).coerceIn(0f, 0.1f)
        lastFrameTime = currentTime

        // Force recomposition
        @Suppress("UNUSED_EXPRESSION")
        frameCount

        fireworksSystem.update(size.width, size.height, deltaTime)
        fireworksSystem.draw(this)
    }
}

data class CircleLayer(
    val diameter: Dp,
    val strokeWidth: Dp,
    val color: Color,
    val ornamentType: OrnamentType,
    val delay: Int,
    val yOffset: Dp
)

enum class OrnamentType {
    SPARKLE,      // ✨
    STAR,         // 🌟
    DIZZY,        // 💫
    CIRCLE_RED,   // Red circle
    STAR_ICON,    // ⭐
    RED_ENVELOPE, // 🧧
    BOUQUET       // 💐
}

// ============================================================================
// ANIMATED ORNAMENT COMPOSABLE
// ============================================================================

@Composable
fun AnimatedOrnament(
    type: OrnamentType,
    index: Int,
    radius: Dp,
    rotationDegrees: Float,
    modifier: Modifier = Modifier
) {
    val angle = index * 90f + rotationDegrees
    val radians = Math.toRadians(angle.toDouble())

    val density = LocalDensity.current
    val radiusPx = with(density) { radius.toPx() }

    val offsetX = (cos(radians) * radiusPx).toFloat()
    val offsetY = (sin(radians) * radiusPx).toFloat()

    Box(
        modifier = modifier
            .offset(
                x = with(density) { offsetX.toDp() },
                y = with(density) { offsetY.toDp() }
            ),
        contentAlignment = Alignment.Center
    ) {
        when (type) {
            OrnamentType.SPARKLE -> Text("✨", fontSize = 10.sp)
            OrnamentType.STAR -> Text("🌟", fontSize = 10.sp)
            OrnamentType.DIZZY -> Text("💫", fontSize = 10.sp)
            OrnamentType.CIRCLE_RED -> {
                Canvas(modifier = Modifier.size(8.dp)) {
                    drawCircle(color = Color.Red)
                }
            }
            OrnamentType.STAR_ICON -> Text("⭐", fontSize = 12.sp)
            OrnamentType.RED_ENVELOPE -> Text("🧧", fontSize = 10.sp)
            OrnamentType.BOUQUET -> Text(
                "💐",
                fontSize = 10.sp,
                modifier = Modifier.rotate(-45f)
            )
        }
    }
}

@Composable
fun DashedCircle(
    diameter: Dp,
    strokeWidth: Dp,
    color: Color,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    Canvas(modifier = modifier.size(diameter)) {
        val dashLength = 7.dp.toPx()
        val gapLength = 7.dp.toPx()

        drawCircle(
            color = color,
            radius = (size.minDimension - strokeWidth.toPx()) / 2,
            style = Stroke(
                width = strokeWidth.toPx(),
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(dashLength, gapLength),
                    0f
                )
            )
        )
    }
}

// ============================================================================
// ANIMATED CIRCLE LAYER COMPOSABLE
// ============================================================================

@Composable
fun AnimatedCircleLayer(
    layer: CircleLayer,
    isSpinning: Boolean,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (isSpinning) 180f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1500,
                easing = LinearEasing,
                delayMillis = layer.delay
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation_${layer.diameter}"
    )

    Box(
        modifier = modifier
            .offset(y = layer.yOffset)
            .graphicsLayer {
                // 3D rotation effect (simulated by scaling Y)
                rotationX = 60f
            },
        contentAlignment = Alignment.Center
    ) {
        // Dashed circle
        DashedCircle(
            diameter = layer.diameter,
            strokeWidth = layer.strokeWidth,
            color = layer.color
        )

        // Ornaments at 4 positions
        for (i in 0 until 4) {
            AnimatedOrnament(
                type = layer.ornamentType,
                index = i,
                radius = layer.diameter / 2,
                rotationDegrees = rotation
            )
        }
    }
}

@Composable
fun ChristmasTree(
    isSpinning: Boolean,
    modifier: Modifier = Modifier
) {
    // Color definitions matching SwiftUI
    val coral = Color(0xFFFF7E79)
    val peach = Color(0xFFFFD479)
    val lightLimeGreen = Color(0xFFD4FB79)
    val springGreen = Color(0xFF49FA79)
    val paleAqua = Color(0xFF49FCD6)
    val skyBlue = Color(0xFF4AD6FF)
    val softLavender = Color(0xFF7A81FF)
    val electricPurple = Color(0xFFD883FF)
    val olive = Color(0xFF935A00)
    val forestGreen = Color(0xFF008F00)

    val layers = listOf(
        CircleLayer(20.dp, 1.dp, coral, OrnamentType.SPARKLE, 0, (-160).dp),
        CircleLayer(50.dp, 2.dp, peach, OrnamentType.STAR, 100, (-120).dp),
        CircleLayer(80.dp, 3.dp, lightLimeGreen, OrnamentType.DIZZY, 200, (-80).dp),
        CircleLayer(110.dp, 5.dp, springGreen, OrnamentType.CIRCLE_RED, 300, (-40).dp),
        CircleLayer(140.dp, 5.dp, paleAqua, OrnamentType.CIRCLE_RED, 400, 0.dp),
        CircleLayer(170.dp, 5.dp, skyBlue, OrnamentType.CIRCLE_RED, 500, 40.dp),
        CircleLayer(200.dp, 5.dp, softLavender, OrnamentType.STAR_ICON, 600, 80.dp),
        CircleLayer(230.dp, 5.dp, electricPurple, OrnamentType.CIRCLE_RED, 700, 120.dp),
        CircleLayer(260.dp, 5.dp, olive, OrnamentType.RED_ENVELOPE, 800, 160.dp),
        CircleLayer(290.dp, 5.dp, forestGreen, OrnamentType.BOUQUET, 900, 200.dp)
    )

    // Hue rotation animation
    val infiniteTransition = rememberInfiniteTransition(label = "tree")
    val hueRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 150f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hueRotation"
    )

    // Scale animation
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.4f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = modifier
            .size(300.dp, 500.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        contentAlignment = Alignment.Center
    ) {
        layers.forEach { layer ->
            AnimatedCircleLayer(
                layer = layer,
                isSpinning = isSpinning
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun PreviewNewYearsEveFireworks() {
    NewYearsEveFireworksScreen()
}