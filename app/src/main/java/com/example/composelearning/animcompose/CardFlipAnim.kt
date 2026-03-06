package com.example.composelearning.animcompose

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue

/**
 * A premium 3D card flip animation with realistic gloss, shadows, and spring physics.
 * Features:
 * - True 3D perspective rotation with backface visibility
 * - Dynamic gloss effect that shifts during rotation
 * - Holographic security elements
 * - Magnetic strip and signature panel on back
 * - Spring-based animation with anticipatory overshoot
 */
@Composable
fun CreditCardFlip(
    modifier: Modifier = Modifier,
    cardNumber: String = "4532  8912  3456  7890",
    cardHolder: String = "KYRIAKOS G.",
    expiryDate: String = "12/28",
    cvv: String = "847",
    cardType: CardType = CardType.VISA
) {
    var isFlipped by remember { mutableStateOf(false) }
    var isGlossActive by remember { mutableStateOf(false) }

    // Spring-based rotation with anticipatory feel
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = spring(
            dampingRatio = 0.6f, // Slight overshoot for premium feel
            stiffness = 200f,     // Not too stiff, not too loose
            visibilityThreshold = 0.5f
        ),
        label = "cardRotation"
    )

    // Scale animation for "lift" effect when flipping
    val scale by animateFloatAsState(
        targetValue = if (isGlossActive) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = 300f
        ),
        label = "cardScale"
    )

    // Gloss position animation
    val glossOffset = remember { Animatable(-1f) }

    LaunchedEffect(rotation) {
        // Animate gloss sweep during rotation
        val progress = rotation / 180f
        glossOffset.animateTo(
            targetValue = if (progress < 0.5f) progress * 2 else (1 - progress) * 2,
            animationSpec = tween(300)
        )
    }

    // Calculate which side is visible
    val normalizedRotation = rotation % 360
    val isFrontVisible = normalizedRotation in 0f..90f || normalizedRotation in 270f..360f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isGlossActive = true
                isFlipped = !isFlipped
            }
            .graphicsLayer {
                this.scaleX = scale
                this.scaleY = scale
                cameraDistance = 12f * density // Critical for 3D depth
            },
        contentAlignment = Alignment.Center
    ) {
        // Front Face
        CardFace(
            isVisible = isFrontVisible,
            rotation = rotation,
            isFront = true,
            glossOffset = glossOffset.value
        ) {
            CardFrontContent(
                cardNumber = cardNumber,
                cardHolder = cardHolder,
                expiryDate = expiryDate,
                cardType = cardType
            )
        }

        // Back Face
        CardFace(
            isVisible = !isFrontVisible,
            rotation = rotation,
            isFront = false,
            glossOffset = glossOffset.value
        ) {
            CardBackContent(
                cvv = cvv,
                cardHolder = cardHolder
            )
        }
    }
}

@Composable
private fun CardFace(
    isVisible: Boolean,
    rotation: Float,
    isFront: Boolean,
    glossOffset: Float,
    content: @Composable () -> Unit
) {
    // Calculate 3D transform
    val adjustedRotation = if (isFront) rotation else rotation - 180f

    // Fade out when perpendicular to viewer (at 90 degrees)
    val alpha = 1f - (adjustedRotation.absoluteValue / 90f).coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                this.rotationY = adjustedRotation
                this.transformOrigin = TransformOrigin(0.5f, 0.5f)
                this.alpha = alpha
                // Add subtle shadow based on rotation
                this.shadowElevation = if (isVisible) 20f else 5f
            }
            .clip(RoundedCornerShape(24.dp))
            .drawWithContent {
                drawContent()

                // Dynamic gloss effect
                if (isVisible && alpha > 0.3f) {
                    val glossX = size.width * glossOffset
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0f),
                                Color.White.copy(alpha = 0.15f),
                                Color.White.copy(alpha = 0f)
                            ),
                            start = Offset(glossX - 100f, 0f),
                            end = Offset(glossX + 100f, size.height)
                        )
                    )
                }
            }
    ) {
        content()
    }
}

@Composable
private fun CardFrontContent(
    cardNumber: String,
    cardHolder: String,
    expiryDate: String,
    cardType: CardType
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF1e3c72),
                        Color(0xFF2a5298),
                        Color(0xFF1e3c72)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
    ) {
        // Subtle pattern overlay
        CardPattern()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top row: Chip and Contactless
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // EMV Chip
                ChipElement()

                // Contactless icon
                Icon(
                    imageVector = Icons.Rounded.Wifi,
                    contentDescription = "Contactless",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(28.dp)
                )
            }

            // Card Number with spacing
            Text(
                text = cardNumber,
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    letterSpacing = 2.sp
                ),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Bottom row: Cardholder and Expiry
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "CARD HOLDER",
                        style = TextStyle(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.7f),
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = cardHolder,
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            letterSpacing = 1.5.sp
                        )
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "EXPIRES",
                        style = TextStyle(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.7f),
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = expiryDate,
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }
        }

        // Card type logo (bottom right)
        CardTypeLogo(
            type = cardType,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .offset(y = (-8).dp)
        )
    }
}

@Composable
private fun CardBackContent(
    cvv: String,
    cardHolder: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF16213e),
                        Color(0xFF1a1a2e),
                        Color(0xFF16213e)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Magnetic Strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp)
                    .height(48.dp)
                    .background(Color(0xFF0a0a0a))
            ) {
                // Strip texture
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF1a1a1a),
                                    Color(0xFF0a0a0a),
                                    Color(0xFF1a1a1a)
                                )
                            )
                        )
                )
            }

            // Signature panel and CVV
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Signature strip
                Box(
                    modifier = Modifier
                        .weight(2f)
                        .height(40.dp)
                        .background(
                            color = Color(0xFFf0f0f0),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = cardHolder,
                        style = TextStyle(
                            fontFamily = FontFamily.Cursive,
                            fontSize = 18.sp,
                            color = Color(0xFF333333)
                        )
                    )
                }

                // CVV Box
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "CVV",
                        style = TextStyle(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(32.dp)
                            .background(
                                color = Color.White,
                                shape = RoundedCornerShape(4.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cvv,
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1a1a2e)
                            )
                        )
                    }
                }
            }

            // Hologram and security text
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Holographic element
                HologramElement()

                // Security text
                Text(
                    text = "This card is property of the issuing bank. If found, please return to the nearest branch.",
                    style = TextStyle(
                        fontSize = 8.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        lineHeight = 12.sp
                    ),
                    modifier = Modifier.width(200.dp)
                )
            }
        }
    }
}

@Composable
private fun ChipElement() {
    Box(
        modifier = Modifier
            .width(48.dp)
            .height(36.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFd4af37),
                        Color(0xFFf4d03f),
                        Color(0xFFd4af37)
                    )
                ),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(4.dp)
    ) {
        // Chip circuitry pattern
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(Color(0xFF8b6914))
                )
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(16.dp)
                .background(
                    color = Color(0xFFd4af37),
                    shape = RoundedCornerShape(4.dp)
                )
                .border(2.dp, Color(0xFF8b6914), RoundedCornerShape(4.dp))
        )
    }
}

@Composable
private fun CardTypeLogo(type: CardType, modifier: Modifier = Modifier) {
    when (type) {
        CardType.VISA -> {
            Text(
                text = "VISA",
                style = TextStyle(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 2.sp
                ),
                modifier = modifier
            )
        }
        CardType.MASTERCARD -> {
            Row(modifier = modifier) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFFeb001b), shape = RoundedCornerShape(16.dp))
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .offset(x = (-12).dp)
                        .background(Color(0xFFf79e1b), shape = RoundedCornerShape(16.dp))
                        .alpha(0.9f)
                )
            }
        }
        CardType.AMEX -> {
            Text(
                text = "AMEX",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E86C1)
                ),
                modifier = modifier
            )
        }
    }
}

@Composable
private fun CardPattern() {
    // Subtle geometric pattern
    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(0.03f)
            .drawWithContent {
                drawContent()
                // Draw subtle circles
                for (i in 0..5) {
                    drawCircle(
                        color = Color.White,
                        radius = 100f + i * 50f,
                        center = Offset(size.width * 0.8f, size.height * 0.2f)
                    )
                }
            }
    )
}

@Composable
private fun HologramElement() {
    Box(
        modifier = Modifier
            .size(48.dp, 32.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFc0c0c0),
                        Color(0xFFe8e8e8),
                        Color(0xFFa0a0a0),
                        Color(0xFFd0d0d0)
                    )
                ),
                shape = RoundedCornerShape(4.dp)
            )
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.CreditCard,
            contentDescription = null,
            tint = Color(0xFF666666),
            modifier = Modifier.size(20.dp)
        )
    }
}

// Extension for border modifier
private fun Modifier.border(width: Int, color: Color, shape: RoundedCornerShape): Modifier {
    return this.then(
        Modifier.drawWithContent {
            drawContent()
            drawRoundRect(
                color = color,
                size = size,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                    shape.topStart.toPx(size, this),
                    shape.topStart.toPx(size, this)
                ),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = width.toFloat())
            )
        }
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF0F0F0)
@Composable
fun CreditCardFlipPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        CreditCardFlip()
    }
}
