package com.example.composelearning.animcompose

import android.Manifest
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

// ─────────────────────────────────────────────────────────────────────────────
// Piggy Bank Theme Options
// ─────────────────────────────────────────────────────────────────────────────
enum class PiggyTheme(
    val label: String,
    val primary: Color,
    val darkShade: Color,
    val highlight: Color,
    val belly: Color,
    val snout: Color
) {
    CLASSIC_PINK(
        label = "Pinky 🐷",
        primary = Color(0xFFFFB6C1),
        darkShade = Color(0xFFE07A8B),
        highlight = Color(0xFFFFF0F5),
        belly = Color(0xFFFFC0CB),
        snout = Color(0xFFFF8DA1)
    ),
    GOLDEN_PIG(
        label = "Gold 🌟",
        primary = Color(0xFFFFD700),
        darkShade = Color(0xFFC59B27),
        highlight = Color(0xFFFFFAD1),
        belly = Color(0xFFFFE66D),
        snout = Color(0xFFE6AC00)
    ),
    MINT_FRESH(
        label = "Mint 🌿",
        primary = Color(0xFFA8E6CF),
        darkShade = Color(0xFF56B894),
        highlight = Color(0xFFE8FAF1),
        belly = Color(0xFF88D8B0),
        snout = Color(0xFF3EA37A)
    ),
    CYBER_PURPLE(
        label = "Cyber 🔮",
        primary = Color(0xFFD8B4F8),
        darkShade = Color(0xFF9055A2),
        highlight = Color(0xFFF3E8FF),
        belly = Color(0xFFC896E6),
        snout = Color(0xFFA566C7)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Coin Type Options
// ─────────────────────────────────────────────────────────────────────────────
enum class CoinType(
    val label: String,
    val value: Float,
    val symbol: String,
    val primaryColor: Color,
    val darkEdge: Color,
    val shineColor: Color
) {
    GOLD_DOLLAR(
        label = "Gold ($1)",
        value = 1.00f,
        symbol = "$",
        primaryColor = Color(0xFFFFD700),
        darkEdge = Color(0xFFB8860B),
        shineColor = Color(0xFFFFF8DC)
    ),
    SILVER_QUARTER(
        label = "Silver (25¢)",
        value = 0.25f,
        symbol = "25¢",
        primaryColor = Color(0xFFE0E0E0),
        darkEdge = Color(0xFF757575),
        shineColor = Color(0xFFFFFFFF)
    ),
    BRONZE_CENT(
        label = "Bronze (1¢)",
        value = 0.01f,
        symbol = "1¢",
        primaryColor = Color(0xFFCD7F32),
        darkEdge = Color(0xFF8B4513),
        shineColor = Color(0xFFFFE4C4)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Coin Particle Sparkle Model
// ─────────────────────────────────────────────────────────────────────────────
private data class SparkleParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var alpha: Float = 1f,
    var color: Color,
    var size: Float,
    val maxLife: Float = 1f,
    var life: Float = 0f
)

// ─────────────────────────────────────────────────────────────────────────────
// Floating Value Indicator (+ $1.00)
// ─────────────────────────────────────────────────────────────────────────────
private data class FloatingText(
    var x: Float,
    var y: Float,
    val text: String,
    var alpha: Float = 1f,
    var scale: Float = 0.5f,
    var life: Float = 0f
)

// ─────────────────────────────────────────────────────────────────────────────
// Active In-Flight Coin Model
// ─────────────────────────────────────────────────────────────────────────────
private class ActiveCoin(
    val id: Long,
    val startPos: Offset,
    val slotPos: Offset,
    val coinType: CoinType,
    val arcHeight: Float,
    val durationSeconds: Float,
    val spinSpeedX: Float,
    val spinSpeedY: Float,
    val sizePx: Float
) {
    var progress by mutableFloatStateOf(0f) // 0f to 1f
    var spinX by mutableFloatStateOf(Random.nextFloat() * 2f * PI.toFloat())
    var spinY by mutableFloatStateOf(Random.nextFloat() * 2f * PI.toFloat())
    var isFinished by mutableStateOf(false)

    fun update(deltaSeconds: Float) {
        if (isFinished) return
        progress = (progress + deltaSeconds / durationSeconds).coerceIn(0f, 1f)
        spinX += spinSpeedX * deltaSeconds
        spinY += spinSpeedY * deltaSeconds
        if (progress >= 1f) {
            isFinished = true
        }
    }

    fun currentPosition(): Offset {
        val currX = startPos.x + (slotPos.x - startPos.x) * progress
        val linearY = startPos.y + (slotPos.y - startPos.y) * progress
        // Parabolic arc displacement
        val arcY = sin(progress * PI.toFloat()) * arcHeight
        return Offset(currX, linearY - arcY)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Main Piggy Bank & Coin Toss Screen
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PiggyBankCoinScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val textMeasurer = rememberTextMeasurer()

    // State & Customization Settings
    var piggyTheme by remember { mutableStateOf(PiggyTheme.CLASSIC_PINK) }
    var coinType by remember { mutableStateOf(CoinType.GOLD_DOLLAR) }
    var autoDropEnabled by remember { mutableStateOf(false) }
    var dropSpeedFactor by remember { mutableFloatStateOf(1.0f) } // 0.5f (fast) to 2.0f (slow)
    var spinVelocityFactor by remember { mutableFloatStateOf(1.0f) }

    // Stats
    var totalSavings by remember { mutableFloatStateOf(0.00f) }
    var coinCount by remember { mutableIntStateOf(0) }

    // Physics Animation States
    val piggySquashY = remember { Animatable(1.0f) } // Vertical squash factor
    val piggySquashX = remember { Animatable(1.0f) } // Horizontal stretch factor
    val snoutWiggleAngle = remember { Animatable(0f) }

    // In-flight coins and visual effects
    val activeCoins = remember { mutableStateListOf<ActiveCoin>() }
    val particles = remember { mutableStateListOf<SparkleParticle>() }
    val floatingTexts = remember { mutableStateListOf<FloatingText>() }

    // Vibrator helper
    @RequiresPermission(Manifest.permission.VIBRATE)
    fun triggerHaptic() {
        try {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator.vibrate(
                VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } catch (_: Exception) {
            // Ignore if vibration permissions or hardware unavailable
        }
    }

    // Function to launch a coin into piggy slot
    fun spawnCoin(customStart: Offset? = null, canvasWidth: Float = 800f, canvasHeight: Float = 1000f) {
        val slotX = canvasWidth / 2f
        val slotY = canvasHeight * 0.42f // Slot vertical position

        val startX = customStart?.x ?: (canvasWidth * 0.2f + Random.nextFloat() * (canvasWidth * 0.6f))
        val startY = customStart?.y ?: (canvasHeight * 0.12f)

        val arc = (180f + Random.nextFloat() * 120f)
        val duration = (0.75f / dropSpeedFactor).coerceIn(0.3f, 2.5f)
        val spinX = (4f + Random.nextFloat() * 8f) * spinVelocityFactor
        val spinY = (6f + Random.nextFloat() * 10f) * spinVelocityFactor

        val coin = ActiveCoin(
            id = System.currentTimeMillis() + Random.nextLong(10000),
            startPos = Offset(startX, startY),
            slotPos = Offset(slotX, slotY),
            coinType = coinType,
            arcHeight = arc,
            durationSeconds = duration,
            spinSpeedX = spinX,
            spinSpeedY = spinY,
            sizePx = 72f
        )
        activeCoins.add(coin)
    }

    // Function triggered when coin hits slot
    fun onCoinEnteredSlot(coin: ActiveCoin) {
        totalSavings += coin.coinType.value
        coinCount += 1
        triggerHaptic()

        // Piggy Squash & Stretch Spring Effect
        scope.launch {
            piggySquashY.snapTo(0.84f)
            piggySquashX.snapTo(1.14f)
            snoutWiggleAngle.snapTo(12f)

            launch { piggySquashY.animateTo(1.0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) }
            launch { piggySquashX.animateTo(1.0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) }
            launch { snoutWiggleAngle.animateTo(0f, spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessMedium)) }
        }

        // Spawn Gold Sparkles from slot
        val slotPos = coin.slotPos
        repeat(16) {
            val angle = Random.nextFloat() * 2f * PI.toFloat()
            val speed = 80f + Random.nextFloat() * 220f
            particles.add(
                SparkleParticle(
                    x = slotPos.x,
                    y = slotPos.y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed - 60f, // initial upward burst
                    color = if (Random.nextBoolean()) coin.coinType.primaryColor else Color.White,
                    size = 6f + Random.nextFloat() * 10f,
                    maxLife = 0.6f + Random.nextFloat() * 0.4f
                )
            )
        }

        // Spawn Floating Text (+ $1.00)
        floatingTexts.add(
            FloatingText(
                x = slotPos.x,
                y = slotPos.y - 20f,
                text = "+${coin.coinType.symbol}"
            )
        )
    }

    // Animation Loop: Updates physics every frame
    LaunchedEffect(Unit) {
        var lastNano = System.nanoTime()
        while (true) {
            withFrameNanos { nowNano ->
                val deltaSeconds = ((nowNano - lastNano) / 1_000_000_000f).coerceIn(0f, 0.1f)
                lastNano = nowNano

                // Update active coins
                val iterator = activeCoins.iterator()
                while (iterator.hasNext()) {
                    val coin = iterator.next()
                    val wasFinished = coin.isFinished
                    coin.update(deltaSeconds)

                    if (!wasFinished && coin.isFinished) {
                        onCoinEnteredSlot(coin)
                        iterator.remove()
                    }
                }

                // Update Sparkle Particles
                val pIter = particles.iterator()
                while (pIter.hasNext()) {
                    val p = pIter.next()
                    p.life += deltaSeconds
                    if (p.life >= p.maxLife) {
                        pIter.remove()
                    } else {
                        p.x += p.vx * deltaSeconds
                        p.y += p.vy * deltaSeconds
                        p.vy += 380f * deltaSeconds // gravity on particles
                        p.alpha = (1f - (p.life / p.maxLife)).coerceIn(0f, 1f)
                    }
                }

                // Update Floating Text
                val tIter = floatingTexts.iterator()
                while (tIter.hasNext()) {
                    val txt = tIter.next()
                    txt.life += deltaSeconds
                    if (txt.life >= 0.8f) {
                        tIter.remove()
                    } else {
                        txt.y -= 70f * deltaSeconds // float upward
                        txt.alpha = (1f - (txt.life / 0.8f)).coerceIn(0f, 1f)
                        txt.scale = 0.5f + (txt.life / 0.2f).coerceAtMost(1f) * 0.5f
                    }
                }
            }
        }
    }

    // Auto-drop timer loop
    LaunchedEffect(autoDropEnabled, dropSpeedFactor) {
        if (!autoDropEnabled) return@LaunchedEffect
        while (autoDropEnabled) {
            spawnCoin()
            delay((600L / dropSpeedFactor).toLong().coerceAtLeast(150L).milliseconds)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Piggy Bank Coin Toss", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        totalSavings = 0f
                        coinCount = 0
                        activeCoins.clear()
                        particles.clear()
                        floatingTexts.clear()
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset Savings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                    )
                )
        ) {
            // ── Top Stats Header ───────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = piggyTheme.primary.copy(alpha = 0.18f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TOTAL SAVINGS",
                            style = TextStyle(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Text(
                            text = "$${String.format("%.2f", totalSavings)}",
                            style = TextStyle(
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🪙 ", fontSize = 16.sp)
                            Text(
                                text = "$coinCount coins",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // ── Main Interactive Piggy Canvas Area ─────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                spawnCoin(customStart = offset, canvasWidth = size.width.toFloat(), canvasHeight = size.height.toFloat())
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height

                    // Background Floor & Ambient Shadow
                    drawFloorAndShadows(width, height)

                    // Piggy Bank Center Coordinates
                    val piggyCenterX = width / 2f
                    val piggyCenterY = height * 0.52f
                    val slotX = piggyCenterX
                    val slotY = height * 0.42f

                    // 1. Draw Piggy Bank
                    drawPiggyBank(
                        centerX = piggyCenterX,
                        centerY = piggyCenterY,
                        theme = piggyTheme,
                        squashX = piggySquashX.value,
                        squashY = piggySquashY.value,
                        snoutWiggle = snoutWiggleAngle.value
                    )

                    // 2. Draw In-flight Coins (Back phase or before slot)
                    activeCoins.forEach { coin ->
                        drawCoin3D(
                            coin = coin,
                            slotX = slotX,
                            slotY = slotY,
                            textMeasurer = textMeasurer
                        )
                    }

                    // 3. Draw Sparkle Particles
                    particles.forEach { p ->
                        drawCircle(
                            color = p.color.copy(alpha = p.alpha),
                            radius = p.size,
                            center = Offset(p.x, p.y)
                        )
                    }

                    // 4. Draw Floating Text (+ $1.00)
                    floatingTexts.forEach { txt ->
                        drawText(
                            textMeasurer = textMeasurer,
                            text = txt.text,
                            topLeft = Offset(txt.x - 30f, txt.y),
                            style = TextStyle(
                                color = Color(0xFFFFD700).copy(alpha = txt.alpha),
                                fontSize = (22 * txt.scale).sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                    }
                }

                // Tap Hint Overlay
                if (coinCount == 0 && activeCoins.isEmpty()) {
                    Text(
                        text = "👉 Tap anywhere or press 'Toss Coin' to drop!",
                        style = TextStyle(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 12.dp)
                    )
                }
            }

            // ── Interactive Control Panel ──────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Action Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { spawnCoin() },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = piggyTheme.primary,
                                contentColor = Color.Black
                            )
                        ) {
                            Text("Toss Coin 🪙", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }

                        OutlinedButton(
                            onClick = { autoDropEnabled = !autoDropEnabled },
                            modifier = Modifier.height(52.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Auto Drop",
                                    tint = if (autoDropEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (autoDropEnabled) "Auto Off" else "Auto On",
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Piggy Theme Selector Chips
                    Column {
                        Text(
                            text = "PIGGY THEME",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PiggyTheme.entries.forEach { theme ->
                                FilterChip(
                                    selected = piggyTheme == theme,
                                    onClick = { piggyTheme = theme },
                                    label = { Text(theme.label, fontSize = 12.sp) }
                                )
                            }
                        }
                    }

                    // Coin Type Selector Chips
                    Column {
                        Text(
                            text = "COIN TYPE",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CoinType.entries.forEach { type ->
                                FilterChip(
                                    selected = coinType == type,
                                    onClick = { coinType = type },
                                    label = { Text(type.label, fontSize = 12.sp) }
                                )
                            }
                        }
                    }

                    // Sliders for Gravity / Speed and Spin Velocity
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Drop Speed: ${String.format("%.1fx", dropSpeedFactor)}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Slider(
                                value = dropSpeedFactor,
                                onValueChange = { dropSpeedFactor = it },
                                valueRange = 0.5f..2.5f,
                                colors = SliderDefaults.colors(thumbColor = piggyTheme.primary)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Coin Spin: ${String.format("%.1fx", spinVelocityFactor)}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Slider(
                                value = spinVelocityFactor,
                                onValueChange = { spinVelocityFactor = it },
                                valueRange = 0.3f..3.0f,
                                colors = SliderDefaults.colors(thumbColor = piggyTheme.primary)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Canvas Drawing Helper: Ground & Ambient Shadows
// ─────────────────────────────────────────────────────────────────────────────
private fun DrawScope.drawFloorAndShadows(width: Float, height: Float) {
    val floorY = height * 0.65f

    // Soft gradient floor platform
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.Transparent,
                Color.Black.copy(alpha = 0.05f)
            ),
            startY = floorY - 50f,
            endY = height
        ),
        topLeft = Offset(0f, floorY - 50f),
        size = Size(width, height - (floorY - 50f))
    )

    // Piggy Bank Drop Shadow on Floor
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.Black.copy(alpha = 0.35f),
                Color.Black.copy(alpha = 0.12f),
                Color.Transparent
            ),
            center = Offset(width / 2f, floorY + 15f),
            radius = width * 0.32f
        ),
        topLeft = Offset(width / 2f - width * 0.32f, floorY - 15f),
        size = Size(width * 0.64f, 60f)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Canvas Drawing Helper: Cute Shaded Piggy Bank with Squash & Stretch
// ─────────────────────────────────────────────────────────────────────────────
private fun DrawScope.drawPiggyBank(
    centerX: Float,
    centerY: Float,
    theme: PiggyTheme,
    squashX: Float,
    squashY: Float,
    snoutWiggle: Float
) {
    val bodyWidth = 280f * squashX
    val bodyHeight = 200f * squashY

    val bodyLeft = centerX - bodyWidth / 2f
    val bodyTop = centerY - bodyHeight / 2f

    // Slot position on top of the piggy
    val slotX = centerX
    val slotY = bodyTop + 14f * squashY
    val slotWidth = 70f * squashX
    val slotHeight = 16f * squashY

    withTransform({
        // Scale around center for squash/stretch
        scale(scaleX = 1f, scaleY = 1f, pivot = Offset(centerX, centerY + bodyHeight / 2f))
    }) {

        // 1. Four Stubby Legs
        val legWidth = 36f * squashX
        val legHeight = 48f * squashY
        val legY = centerY + bodyHeight / 2f - 18f

        val legOffsets = listOf(-85f, -35f, 35f, 85f)
        legOffsets.forEach { offsetX ->
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(theme.primary, theme.darkShade),
                    startY = legY,
                    endY = legY + legHeight
                ),
                topLeft = Offset(centerX + offsetX * squashX - legWidth / 2f, legY),
                size = Size(legWidth, legHeight),
                cornerRadius = CornerRadius(16f, 16f)
            )
        }

        // 2. Curly Piggy Tail (Left/Rear)
        val tailPath = Path().apply {
            val startTail = Offset(bodyLeft + 15f, centerY - 10f)
            moveTo(startTail.x, startTail.y)
            cubicTo(
                startTail.x - 30f, startTail.y - 25f,
                startTail.x - 45f, startTail.y + 15f,
                startTail.x - 20f, startTail.y + 5f
            )
            cubicTo(
                startTail.x - 10f, startTail.y - 5f,
                startTail.x - 35f, startTail.y - 45f,
                startTail.x - 50f, startTail.y - 20f
            )
        }
        drawPath(
            path = tailPath,
            color = theme.darkShade,
            style = Stroke(width = 8f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // 3. Main Body Oval with 3D Radial Highlight
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(
                    theme.highlight,
                    theme.primary,
                    theme.darkShade
                ),
                center = Offset(centerX - bodyWidth * 0.15f, centerY - bodyHeight * 0.25f),
                radius = bodyWidth * 0.7f
            ),
            topLeft = Offset(bodyLeft, bodyTop),
            size = Size(bodyWidth, bodyHeight)
        )

        // 4. Outer Soft Rim Contour
        drawOval(
            color = theme.darkShade.copy(alpha = 0.4f),
            topLeft = Offset(bodyLeft, bodyTop),
            size = Size(bodyWidth, bodyHeight),
            style = Stroke(width = 3f)
        )

        // 5. Ears (Cute Curved Triangles)
        val earY = bodyTop + 15f
        val earLeftPath = Path().apply {
            moveTo(centerX + 30f * squashX, earY)
            quadraticTo(centerX + 45f * squashX, earY - 45f * squashY, centerX + 75f * squashX, earY + 5f)
            close()
        }
        drawPath(
            path = earLeftPath,
            brush = Brush.linearGradient(listOf(theme.primary, theme.snout))
        )
        // Inner ear blush
        val innerEarPath = Path().apply {
            moveTo(centerX + 38f * squashX, earY - 5f)
            quadraticTo(centerX + 48f * squashX, earY - 35f * squashY, centerX + 68f * squashX, earY)
            close()
        }
        drawPath(path = innerEarPath, color = theme.highlight.copy(alpha = 0.6f))

        // 6. Snout (Right/Front Side)
        val snoutWidth = 65f * squashX
        val snoutHeight = 48f * squashY
        val snoutX = centerX + bodyWidth / 2f - 25f + snoutWiggle
        val snoutY = centerY - 5f

        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(theme.highlight, theme.snout, theme.darkShade),
                center = Offset(snoutX + snoutWidth * 0.3f, snoutY + snoutHeight * 0.3f),
                radius = snoutWidth
            ),
            topLeft = Offset(snoutX, snoutY),
            size = Size(snoutWidth, snoutHeight)
        )

        // Nostrils
        drawOval(
            color = theme.darkShade,
            topLeft = Offset(snoutX + 16f * squashX, snoutY + 16f * squashY),
            size = Size(10f * squashX, 16f * squashY)
        )
        drawOval(
            color = theme.darkShade,
            topLeft = Offset(snoutX + 36f * squashX, snoutY + 16f * squashY),
            size = Size(10f * squashX, 16f * squashY)
        )

        // 7. Eye (Happy Curved Arc)
        val eyeX = centerX + bodyWidth * 0.22f
        val eyeY = centerY - 32f * squashY
        val eyePath = Path().apply {
            moveTo(eyeX - 12f, eyeY + 4f)
            quadraticTo(eyeX, eyeY - 10f, eyeX + 12f, eyeY + 4f)
        }
        drawPath(
            path = eyePath,
            color = Color(0xFF2C2C2C),
            style = Stroke(width = 5f, cap = StrokeCap.Round)
        )

        // Cute Cheek Blush
        drawOval(
            color = Color(0xFFFF5252).copy(alpha = 0.35f),
            topLeft = Offset(eyeX - 16f, eyeY + 14f),
            size = Size(32f, 18f)
        )

        // 8. Coin Slot (Darkened Slot with Metallic Rim)
        // Outer Slot Rim
        drawOval(
            brush = Brush.verticalGradient(
                colors = listOf(theme.darkShade, Color.Black)
            ),
            topLeft = Offset(slotX - slotWidth / 2f, slotY - slotHeight / 2f),
            size = Size(slotWidth, slotHeight)
        )
        // Dark Inner Hole
        drawOval(
            color = Color(0xFF1A1A1A),
            topLeft = Offset(slotX - (slotWidth - 8f) / 2f, slotY - (slotHeight - 6f) / 2f),
            size = Size(slotWidth - 8f, slotHeight - 6f)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Canvas Drawing Helper: Real 3D Coin Flipping & Parabolic Drop with Slot Mask
// ─────────────────────────────────────────────────────────────────────────────
private fun DrawScope.drawCoin3D(
    coin: ActiveCoin,
    slotX: Float,
    slotY: Float,
    textMeasurer: TextMeasurer
) {
    val pos = coin.currentPosition()
    val type = coin.coinType
    val radius = coin.sizePx / 2f

    // 3D Rotation Math
    val cosX = cos(coin.spinX)
    val cosY = cos(coin.spinY)
    val sinY = sin(coin.spinY)

    // Horizontal & Vertical Projection Scaling
    val scaleX = abs(cosY).coerceAtLeast(0.06f) // Prevent total 0 thickness collapse
    val scaleY = abs(cosX).coerceAtLeast(0.15f)

    // Extruded 3D Edge Depth
    val edgeDepth = abs(sinY) * (radius * 0.35f)
    val isFrontFace = cosY >= 0f

    // Entry Phase (Slot Clipping when approaching slot)
    val isNearSlot = coin.progress >= 0.82f

    val drawBlock: DrawScope.() -> Unit = {
        withTransform({
            translate(left = pos.x, top = pos.y)
            scale(scaleX = scaleX, scaleY = scaleY, pivot = Offset.Zero)
        }) {
            // 1. Draw 3D Extruded Cylinder Edge if tilted
            if (edgeDepth > 1f) {
                val edgeOffset = if (isFrontFace) edgeDepth else -edgeDepth
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(type.darkEdge, Color.Black),
                        center = Offset(edgeOffset * 0.5f, 0f),
                        radius = radius * 1.2f
                    ),
                    radius = radius,
                    center = Offset(edgeOffset, 0f)
                )
            }

            // 2. Main Coin Face Outer Ring
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        type.shineColor,
                        type.primaryColor,
                        type.darkEdge
                    ),
                    center = Offset(-radius * 0.3f, -radius * 0.3f),
                    radius = radius * 1.3f
                ),
                radius = radius,
                center = Offset.Zero
            )

            // 3. Inner Medallion Border
            drawCircle(
                color = type.darkEdge.copy(alpha = 0.6f),
                radius = radius * 0.82f,
                center = Offset.Zero,
                style = Stroke(width = 3f)
            )

            // 4. Ribbed Rim Outer Dots
            val dotsCount = 12
            for (i in 0 until dotsCount) {
                val angle = (i * 2f * PI / dotsCount).toFloat()
                val dotX = cos(angle) * (radius * 0.9f)
                val dotY = sin(angle) * (radius * 0.9f)
                drawCircle(
                    color = type.darkEdge.copy(alpha = 0.5f),
                    radius = 2.2f,
                    center = Offset(dotX, dotY)
                )
            }

            // 5. Center Symbol ($ or 25¢ or 1¢)
            drawText(
                textMeasurer = textMeasurer,
                text = type.symbol,
                topLeft = Offset(-radius * 0.45f, -radius * 0.5f),
                style = TextStyle(
                    color = type.darkEdge,
                    fontSize = (radius * 0.95f).sp,
                    fontWeight = FontWeight.ExtraBold
                )
            )

            // 6. Specular Reflection Shine Streak
            val shinePath = Path().apply {
                moveTo(-radius * 0.6f, -radius * 0.8f)
                lineTo(-radius * 0.2f, -radius * 0.8f)
                lineTo(radius * 0.6f, radius * 0.8f)
                lineTo(radius * 0.2f, radius * 0.8f)
                close()
            }
            drawPath(
                path = shinePath,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.0f),
                        Color.White.copy(alpha = 0.45f),
                        Color.White.copy(alpha = 0.0f)
                    )
                )
            )
        }
    }

    // Clip coin behind piggy slot edge if entering slot
    if (isNearSlot) {
        // Only draw upper portion of coin that hasn't disappeared into slot
        val clipLimitY = slotY + 6f
        clipRect(
            left = pos.x - radius * 2f,
            top = pos.y - radius * 2f,
            right = pos.x + radius * 2f,
            bottom = clipLimitY
        ) {
            drawBlock()
        }
    } else {
        drawBlock()
    }
}
