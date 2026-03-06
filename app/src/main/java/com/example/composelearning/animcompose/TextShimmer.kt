package com.example.composelearning.animcompose

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.withSaveLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.absoluteValue

/**
 * Collection of premium shimmer effects for text in Jetpack Compose.
 * Includes: Linear sweep, wave, gradient mask, and spotlight effects.
 */

// ============================================
// 1. LINEAR SHIMMER (Classic loading effect)
// ============================================

@Composable
fun LinearShimmerText(
    text: String,
    modifier: Modifier = Modifier,
    baseColor: Color = Color(0xFF94A3B8),
    shimmerColor: Color = Color.White,
    durationMillis: Int = 2000,
    style: TextStyle = TextStyle.Default
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")

    val translateAnim by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "translate"
    )

    val brush = Brush.linearGradient(
        colors = listOf(
            baseColor.copy(alpha = 0.3f),
            shimmerColor.copy(alpha = 0.9f),
            baseColor.copy(alpha = 0.3f)
        ),
        start = Offset(translateAnim * 300f, 0f),
        end = Offset(translateAnim * 300f + 200f, 0f)
    )

    Text(
        text = text,
        modifier = modifier,
        style = style.copy(brush = brush)
    )
}

// ============================================
// 2. WAVE SHIMMER (Sinusoidal movement)
// ============================================

@Composable
fun WaveShimmerText(
    text: String,
    modifier: Modifier = Modifier,
    baseColor: Color = Color(0xFF64748B),
    shimmerColor: Color = Color(0xFFE2E8F0),
    waveAmplitude: Float = 20f,
    durationMillis: Int = 3000,
    style: TextStyle = TextStyle.Default
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")

    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    Text(
        text = text,
        modifier = modifier.drawWithContent {
            drawContent()

            // Create wave mask
            val waveWidth = size.width * 0.4f
            val waveX = (progress * (size.width + waveWidth)) - waveWidth

            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        shimmerColor.copy(alpha = 0.8f),
                        Color.Transparent
                    ),
                    start = Offset(waveX - waveAmplitude, 0f),
                    end = Offset(waveX + waveWidth, size.height)
                ),
                blendMode = androidx.compose.ui.graphics.BlendMode.SrcAtop
            )
        },
        style = style.copy(color = baseColor)
    )
}

// ============================================
// 3. GRADIENT MASK SHIMMER (Text clip effect)
// ============================================

@Composable
fun GradientMaskShimmer(
    text: String,
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(
        Color(0xFF3B82F6),
        Color(0xFF8B5CF6),
        Color(0xFFEC4899),
        Color(0xFF3B82F6)
    ),
    durationMillis: Int = 4000,
    style: TextStyle = TextStyle.Default
) {
    val infiniteTransition = rememberInfiniteTransition(label = "gradient")

    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset"
    )

    // Create animated gradient brush
    val brush = remember(offset) {
        object : ShaderBrush() {
            override fun createShader(size: Size): androidx.compose.ui.graphics.Shader {
                return LinearGradientShader(
                    colors = colors,
                    from = Offset(offset * size.width * 2 - size.width, 0f),
                    to = Offset(offset * size.width * 2, 0f),
                    tileMode = androidx.compose.ui.graphics.TileMode.Mirror
                )
            }
        }
    }

    Text(
        text = text,
        modifier = modifier,
        style = style.copy(brush = brush)
    )
}

// ============================================
// 4. SPOTLIGHT SHIMMER (Radial glow sweep)
// ============================================

@Composable
fun SpotlightShimmerText(
    text: String,
    modifier: Modifier = Modifier,
    baseColor: Color = Color(0xFF1E293B),
    spotlightColor: Color = Color.White,
    durationMillis: Int = 2500,
    style: TextStyle = TextStyle.Default
) {
    val infiniteTransition = rememberInfiniteTransition(label = "spotlight")

    val progress by infiniteTransition.animateFloat(
        initialValue = -0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spotlight"
    )

    Text(
        text = text,
        modifier = modifier.drawWithContent {
            // Draw base text
            drawContent()

            // Draw spotlight overlay
            val centerX = size.width * progress
            val centerY = size.height / 2

            drawIntoCanvas { canvas ->
                canvas.withSaveLayer(bounds = Rect(Offset.Zero, size), paint = Paint()) {
                    // Create radial gradient spotlight
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                spotlightColor.copy(alpha = 0.9f),
                                spotlightColor.copy(alpha = 0.4f),
                                Color.Transparent
                            ),
                            center = Offset(centerX, centerY),
                            radius = size.width * 0.3f
                        ),
                        blendMode = androidx.compose.ui.graphics.BlendMode.SrcAtop
                    )
                }
            }
        },
        style = style.copy(color = baseColor)
    )
}

// ============================================
// 5. CHARACTER STAGGER SHIMMER (Wave through letters)
// ============================================

@Composable
fun CharacterStaggerShimmer(
    text: String,
    modifier: Modifier = Modifier,
    baseColor: Color = Color(0xFF475569),
    shimmerColor: Color = Color(0xFFF8FAFC),
    staggerDelay: Int = 50,
    waveDuration: Int = 1500,
    style: TextStyle = TextStyle.Default
) {
    val infiniteTransition = rememberInfiniteTransition(label = "stagger")

    val waveProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(waveDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave"
    )

    Row(modifier = modifier) {
        text.forEachIndexed { index, char ->
            val charDelay = index * staggerDelay / waveDuration.toFloat()
            val adjustedProgress = (waveProgress + charDelay) % 1f

            // Create bell curve for shimmer intensity
            val shimmerIntensity = 1f - (adjustedProgress - 0.5f).absoluteValue * 2f

            val color = lerp(
                baseColor,
                shimmerColor,
                shimmerIntensity.coerceIn(0f, 1f)
            )

            Text(
                text = char.toString(),
                style = style.copy(color = color),
                modifier = Modifier.graphicsLayer {
                    alpha = 0.3f + (shimmerIntensity * 0.7f)
                    scaleX = 1f + (shimmerIntensity * 0.1f)
                    scaleY = 1f + (shimmerIntensity * 0.1f)
                }
            )
        }
    }
}

// ============================================
// 6. NEON PULSE SHIMMER (Glowing effect)
// ============================================

@Composable
fun NeonPulseShimmer(
    text: String,
    modifier: Modifier = Modifier,
    neonColor: Color = Color(0xFF00D9FF),
    durationMillis: Int = 2000,
    style: TextStyle = TextStyle.Default
) {
    val infiniteTransition = rememberInfiniteTransition(label = "neon")

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis / 2, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val sweep by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep"
    )

    // Multi-layer glow effect
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Outer glow
        Text(
            text = text,
            style = style.copy(
                color = neonColor.copy(alpha = pulse * 0.3f),
                fontSize = style.fontSize * 1.1
            ),
            modifier = Modifier.graphicsLayer {
                scaleX = 1.05f
                scaleY = 1.05f
                alpha = pulse * 0.5f
            }
        )

        // Middle glow
        Text(
            text = text,
            style = style.copy(
                color = neonColor.copy(alpha = pulse * 0.6f),
                fontSize = style.fontSize * 1.02
            )
        )

        // Core with shimmer
        Text(
            text = text,
            style = style.copy(
                brush = Brush.linearGradient(
                    colors = listOf(
                        neonColor.copy(alpha = 0.8f),
                        Color.White.copy(alpha = pulse),
                        neonColor.copy(alpha = 0.8f)
                    ),
                    start = Offset(sweep * 200f, 0f),
                    end = Offset(sweep * 200f + 100f, 0f)
                )
            )
        )
    }
}

// ============================================
// 7. SKELETON SHIMMER (Loading placeholder)
// ============================================

@Composable
fun SkeletonShimmer(
    lines: Int = 3,
    modifier: Modifier = Modifier,
    baseColor: Color = Color(0xFFE2E8F0),
    shimmerColor: Color = Color(0xFFF8FAFC),
    durationMillis: Int = 1500
) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")

    val translate by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "skeleton"
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(lines) { index ->
            val widthFraction = when (index) {
                lines - 1 -> 0.6f
                else -> 1f
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth(widthFraction)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(baseColor)
                    .drawWithContent {
                        val brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                shimmerColor.copy(alpha = 0.6f),
                                Color.Transparent
                            ),
                            start = Offset(translate * size.width, 0f),
                            end = Offset(translate * size.width + size.width * 0.3f, 0f)
                        )

                        drawContent()
                        drawRect(brush = brush)
                    }
            )
        }
    }
}

// ============================================
// 8. RAINBOW SHIMMER (Full spectrum)
// ============================================

@Composable
fun RainbowShimmerText(
    text: String,
    modifier: Modifier = Modifier,
    durationMillis: Int = 3000,
    style: TextStyle = TextStyle.Default
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rainbow")

    val hue by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "hue"
    )

    val brush = Brush.linearGradient(
        colors = listOf(
            Color.hsl(hue, 0.8f, 0.5f),
            Color.hsl((hue + 60) % 360, 0.8f, 0.6f),
            Color.hsl((hue + 120) % 360, 0.8f, 0.5f),
            Color.hsl((hue + 180) % 360, 0.8f, 0.6f)
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, 0f),
        tileMode = androidx.compose.ui.graphics.TileMode.Mirror
    )

    Text(
        text = text,
        modifier = modifier,
        style = style.copy(brush = brush)
    )
}

// ============================================
// PREVIEW SHOWCASE
// ============================================

@Preview(showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
fun ShimmerTextShowcase() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Linear Shimmer
        LinearShimmerText(
            text = "Linear Shimmer",
            style = TextStyle(
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        )

        // 2. Gradient Mask
        GradientMaskShimmer(
            text = "Gradient Mask",
            style = TextStyle(
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold
            )
        )

        // 3. Spotlight
        SpotlightShimmerText(
            text = "Spotlight Effect",
            style = TextStyle(
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        )

        // 4. Character Stagger
        CharacterStaggerShimmer(
            text = "WAVE",
            style = TextStyle(
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 8.sp
            )
        )

        // 5. Neon Pulse
        NeonPulseShimmer(
            text = "NEON",
            style = TextStyle(
                fontSize = 36.sp,
                fontWeight = FontWeight.Black
            )
        )

        // 6. Rainbow
        RainbowShimmerText(
            text = "Rainbow Flow",
            style = TextStyle(
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 7. Skeleton Loading
        SkeletonShimmer(
            lines = 3,
            modifier = Modifier.fillMaxWidth(0.8f)
        )
    }
}

// Helper extension
private fun lerp(start: Color, stop: Color, fraction: Float): Color {
    return Color(
        red = androidx.compose.ui.util.lerp(start.red, stop.red, fraction),
        green = androidx.compose.ui.util.lerp(start.green, stop.green, fraction),
        blue = androidx.compose.ui.util.lerp(start.blue, stop.blue, fraction),
        alpha = androidx.compose.ui.util.lerp(start.alpha, stop.alpha, fraction)
    )
}
