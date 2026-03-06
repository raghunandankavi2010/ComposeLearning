package com.example.composelearning.animcompose

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.withSign

/**
 * A premium sensor-reactive credit card with 3D parallax effects.
 * Responds to device tilt with realistic physics and holographic depth.
 */
@Composable
fun SensorReactiveCard(
    modifier: Modifier = Modifier,
    cardNumber: String = "4532  8912  3456  7890",
    cardHolder: String = "KYRIAKOS G.",
    expiryDate: String = "12/28",
    cvv: String = "847",
    cardType: CardType = CardType.VISA
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }

    // Raw sensor values
    var rawPitch by remember { mutableFloatStateOf(0f) } // X-axis tilt (forward/back)
    var rawRoll by remember { mutableFloatStateOf(0f) }  // Y-axis tilt (left/right)

    // Smoothed physics-based values using Animatable for spring animation
    val pitch = remember { Animatable(0f) }
    val roll = remember { Animatable(0f) }

    // For tap-to-flip functionality (separate from sensor tilt)
    var isFlipped by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }

    // Combine sensor tilt with flip state
    val targetRotationX = if (isFlipped) 180f else 0f
    val currentRotationY by remember { derivedStateOf { roll.value * 15f } } // Max 15° tilt
    val currentRotationX by remember { derivedStateOf {
        (pitch.value * 10f) + if (isFlipped) 180f else 0f
    } }

    // Parallax offsets for internal elements
    val parallaxX by remember { derivedStateOf { roll.value * 20f } }
    val parallaxY by remember { derivedStateOf { pitch.value * 20f } }

    // Gloss position based on tilt
    val glossX by remember { derivedStateOf { 0.5f + (roll.value * 0.3f) } }
    val glossY by remember { derivedStateOf { 0.5f + (pitch.value * 0.3f) } }

    // Sensor listener with low-pass filtering
    DisposableEffect(sensorManager) {
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        val accelerometerReading = FloatArray(3)
        val magnetometerReading = FloatArray(3)
        val rotationMatrix = FloatArray(9)
        val orientationAngles = FloatArray(3)

        var lastUpdate = 0L
        val updateInterval = 16L // ~60fps

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> {
                        System.arraycopy(event.values, 0, accelerometerReading, 0, 3)
                    }
                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        System.arraycopy(event.values, 0, magnetometerReading, 0, 3)
                    }
                }

                val now = System.currentTimeMillis()
                if (now - lastUpdate > updateInterval) {
                    lastUpdate = now

                    // Calculate orientation
                    SensorManager.getRotationMatrix(
                        rotationMatrix, null,
                        accelerometerReading,
                        magnetometerReading
                    )
                    SensorManager.getOrientation(rotationMatrix, orientationAngles)

                    // Convert to degrees and apply deadzone
                    val newRoll = -orientationAngles[2].toDegrees().coerceIn(-45f, 45f) / 45f
                    val newPitch = -orientationAngles[1].toDegrees().coerceIn(-45f, 45f) / 45f

                    // Apply low-pass filter for smoothness
                    rawRoll = rawRoll * 0.8f + newRoll * 0.2f
                    rawPitch = rawPitch * 0.8f + newPitch * 0.2f
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(listener, magnetometer, SensorManager.SENSOR_DELAY_GAME)

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    // Spring animation to smooth sensor input
    LaunchedEffect(rawRoll, rawPitch) {
        scope.launch {
            roll.animateTo(
                targetValue = rawRoll,
                animationSpec = spring(
                    dampingRatio = 0.6f,
                    stiffness = 150f
                )
            )
        }
        launch {
            pitch.animateTo(
                targetValue = rawPitch,
                animationSpec = spring(
                    dampingRatio = 0.6f,
                    stiffness = 150f
                )
            )
        }
    }

    // Press animation
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f),
        label = "pressScale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .graphicsLayer {
                this.scaleX = scale
                this.scaleY = scale
                this.cameraDistance = 16f * density
                this.rotationY = currentRotationY
                this.rotationX = currentRotationX
                this.transformOrigin = TransformOrigin(0.5f, 0.5f)

                // Dynamic shadow based on tilt
                this.shadowElevation = (20f + (pitch.value.absoluteValue * 10f)).coerceIn(0f, 30f)
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        isPressed = event.changes.any { it.pressed }
                    }
                }
            }.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isFlipped = !isFlipped
            },
        contentAlignment = Alignment.Center
    ) {
        // Front Face
        AnimatedVisibility(
            visible = !isFlipped || (currentRotationX % 360) < 90f || (currentRotationX % 360) > 270f,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            CardFace3D(
                glossX = glossX,
                glossY = glossY,
                parallaxX = parallaxX,
                parallaxY = parallaxY,
                tiltIntensity = roll.value.absoluteValue + pitch.value.absoluteValue
            ) {
                CardFrontContent(
                    cardNumber = cardNumber,
                    cardHolder = cardHolder,
                    expiryDate = expiryDate,
                    cardType = cardType,
                    parallaxX = parallaxX,
                    parallaxY = parallaxY
                )
            }
        }

        // Back Face
        AnimatedVisibility(
            visible = isFlipped && (currentRotationX % 360) in 90f..270f,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            CardFace3D(
                glossX = glossX,
                glossY = glossY,
                parallaxX = parallaxX,
                parallaxY = parallaxY,
                tiltIntensity = roll.value.absoluteValue + pitch.value.absoluteValue,
                isBack = true
            ) {
                CardBackContent(
                    cvv = cvv,
                    cardHolder = cardHolder,
                    parallaxX = parallaxX,
                    parallaxY = parallaxY
                )
            }
        }
    }
}

@Composable
private fun CardFace3D(
    glossX: Float,
    glossY: Float,
    parallaxX: Float,
    parallaxY: Float,
    tiltIntensity: Float,
    isBack: Boolean = false,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .drawWithContent {
                drawContent()

                // Dynamic holographic gloss
                val centerX = size.width * glossX
                val centerY = size.height * glossY

                // Primary gloss
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.2f * (1f - tiltIntensity * 0.5f)),
                            Color.White.copy(alpha = 0.05f),
                            Color.Transparent
                        ),
                        center = Offset(centerX, centerY),
                        radius = size.width * 0.4f
                    ),
                    center = Offset(centerX, centerY),
                    radius = size.width * 0.4f
                )

                // Secondary edge reflection
                if (tiltIntensity > 0.3f) {
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.1f),
                                Color.Transparent
                            ),
                            start = Offset(
                                if (parallaxX > 0) 0f else size.width,
                                0f
                            ),
                            end = Offset(
                                if (parallaxX > 0) size.width * 0.3f else size.width * 0.7f,
                                size.height
                            )
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
    cardType: CardType,
    parallaxX: Float,
    parallaxY: Float
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
        // Background pattern with inverse parallax
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = -parallaxX * 0.5f
                    translationY = -parallaxY * 0.5f
                }
                .alpha(0.1f)
                .drawWithContent {
                    drawCircle(
                        color = Color.White,
                        radius = 200f,
                        center = Offset(size.width * 0.8f, size.height * 0.2f)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 150f,
                        center = Offset(size.width * 0.2f, size.height * 0.8f)
                    )
                }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top row with parallax
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        translationX = parallaxX * 1.2f
                        translationY = parallaxY * 1.2f
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ChipElement()
                Icon(
                    imageVector = Icons.Rounded.Wifi,
                    contentDescription = "Contactless",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(28.dp)
                )
            }

            // Card number with stronger parallax
            Text(
                text = cardNumber,
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    letterSpacing = 2.sp
                ),
                modifier = Modifier
                    .graphicsLayer {
                        translationX = parallaxX * 0.8f
                        translationY = parallaxY * 0.8f
                    }
                    .padding(vertical = 8.dp)
            )

            // Bottom row with varied parallax
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        translationX = parallaxX * 0.6f
                        translationY = parallaxY * 0.6f
                    },
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

        // Card type logo with independent parallax
        CardTypeLogo(
            type = cardType,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .offset(y = (-8).dp)
                .graphicsLayer {
                    translationX = parallaxX * 1.5f
                    translationY = parallaxY * 1.5f
                }
        )
    }
}

@Composable
private fun CardBackContent(
    cvv: String,
    cardHolder: String,
    parallaxX: Float,
    parallaxY: Float
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
            // Magnetic strip with parallax
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp)
                    .height(48.dp)
                    .graphicsLayer {
                        translationX = parallaxX * 0.3f
                    }
                    .background(Color(0xFF0a0a0a))
            )

            // Signature panel and CVV with depth
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 24.dp)
                    .graphicsLayer {
                        translationX = parallaxX * 0.8f
                        translationY = parallaxY * 0.8f
                    },
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
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

            // Hologram with strong parallax
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .graphicsLayer {
                        translationX = parallaxX * 1.2f
                        translationY = parallaxY * 1.2f
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                HologramElement()
                Text(
                    text = "This card is property of the issuing bank.",
                    style = TextStyle(
                        fontSize = 8.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.width(200.dp)
                )
            }
        }
    }
}

// Helper functions and components remain similar but with 3D enhancements...

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
private fun HologramElement() {
    Box(
        modifier = Modifier
            .size(48.dp, 32.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFc0c0c0),
                        Color(0xFFe8e8e8),
                        Color(0xFFa0a0a0)
                    )
                ),
                shape = RoundedCornerShape(4.dp)
            ),
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

enum class CardType { VISA, MASTERCARD, AMEX }

private fun Float.toDegrees(): Float = Math.toDegrees(this.toDouble()).toFloat()

@Preview
@Composable
fun SensorReactiveCardPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        SensorReactiveCard()
    }
}