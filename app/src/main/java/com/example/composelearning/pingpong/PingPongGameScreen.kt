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

import android.content.Context
import android.graphics.Paint
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sin

@Composable
fun PingPongGameScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val engine = remember { PingPongGameEngine() }

    var lastFrameTimeNanos by remember { mutableLongStateOf(0L) }
    var soundEnabled by remember { mutableStateOf(true) }
    var hapticsEnabled by remember { mutableStateOf(true) }

    // Audio & Haptic Feedback Setup
    val toneGenerator = remember {
        try {
            ToneGenerator(AudioManager.STREAM_MUSIC, 80)
        } catch (_: Exception) {
            null
        }
    }

    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            toneGenerator?.release()
        }
    }

    // Audio / Haptic Triggers
    LaunchedEffect(soundEnabled, hapticsEnabled) {
        engine.onHitSound = {
            if (soundEnabled) {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 25)
            }
            if (hapticsEnabled) {
                vibrator?.vibrate(VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        }
        engine.onWallSound = {
            if (soundEnabled) {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 20)
            }
        }
        engine.onScoreSound = { playerScored ->
            if (soundEnabled) {
                val tone = if (playerScored) ToneGenerator.TONE_PROP_BEEP2 else ToneGenerator.TONE_PROP_PROMPT
                toneGenerator?.startTone(tone, 120)
            }
            if (hapticsEnabled) {
                vibrator?.vibrate(VibrationEffect.createOneShot(60, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        }
        engine.onPowerUpSound = {
            if (soundEnabled) {
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 80)
            }
        }
    }

    // High precision game loop using withFrameNanos
    LaunchedEffect(engine.gameState) {
        if (engine.gameState == PingPongGameState.PLAYING) {
            lastFrameTimeNanos = 0L
            while (true) {
                withFrameNanos { frameTimeNanos ->
                    if (lastFrameTimeNanos != 0L) {
                        val dt = ((frameTimeNanos - lastFrameTimeNanos) / 1_000_000_000f).coerceAtMost(0.033f)
                        engine.update(dt)
                    }
                    lastFrameTimeNanos = frameTimeNanos
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E17))
            .systemBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header HUD Bar
            PingPongHeaderHud(
                engine = engine,
                soundEnabled = soundEnabled,
                onToggleSound = { soundEnabled = !soundEnabled },
                onBack = onBack,
                onPause = {
                    if (engine.gameState == PingPongGameState.PLAYING) {
                        engine.gameState = PingPongGameState.PAUSED
                    }
                }
            )

            // Game Play Canvas with Touch Drag Gesture
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(2.dp, Color(0xFF00E5FF).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            if (engine.gameState == PingPongGameState.PLAYING) {
                                engine.onPlayerDrag(change.position.x)
                            }
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    engine.updateDimensions(size.width, size.height)

                    // Draw Cyber Table Markings
                    drawTableBackground()

                    // Draw Active Power-Ups on Field
                    drawPowerUps(engine)

                    // Draw Ball Trail & Particles
                    drawParticlesAndTrails(engine)

                    // Draw Balls
                    drawBalls(engine)

                    // Draw Paddles
                    drawPaddles(engine)
                }

                // Overlay for READY state (Tap to Serve)
                if (engine.gameState == PingPongGameState.READY) {
                    StartGameOverlay(
                        engine = engine,
                        onStartGame = { engine.startPlaying() }
                    )
                }

                // Overlay for PAUSED state
                if (engine.gameState == PingPongGameState.PAUSED) {
                    PauseMenuOverlay(
                        onResume = { engine.startPlaying() },
                        onRestart = { engine.resetGame() },
                        soundEnabled = soundEnabled,
                        onToggleSound = { soundEnabled = !soundEnabled },
                        hapticsEnabled = hapticsEnabled,
                        onToggleHaptics = { hapticsEnabled = !hapticsEnabled }
                    )
                }
            }

            // Bottom HUD Bar with Level Progress & Active Power-Ups
            PingPongBottomHud(engine = engine)
        }
    }
}

@Composable
private fun PingPongHeaderHud(
    engine: PingPongGameEngine,
    soundEnabled: Boolean,
    onToggleSound: () -> Unit,
    onBack: () -> Unit,
    onPause: () -> Unit
) {
    Surface(
        color = Color(0xFF121929),
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            // Score Display (AI vs Player)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${engine.aiScore}",
                        color = Color(0xFFFF1744),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = " : ",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${engine.playerScore}",
                        color = Color(0xFF00E5FF),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "AI   VS   YOU",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
            }

            Row {
                IconButton(onClick = onPause) {
                    Icon(
                        Icons.Default.Pause,
                        contentDescription = "Pause",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun PingPongBottomHud(engine: PingPongGameEngine) {
    Surface(
        color = Color(0xFF121929),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Difficulty Level Badge
                Box(
                    modifier = Modifier
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF00E5FF).copy(alpha = 0.2f), Color(0xFF76FF03).copy(alpha = 0.2f))
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "LEVEL ${engine.currentLevel}",
                        color = Color(0xFF00E5FF),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Speedometer & Rally
                val primaryBall = engine.balls.firstOrNull()
                val speedMph = if (primaryBall != null) engine.getBallSpeedMph(primaryBall) else 0

                Text(
                    text = "SPEED: $speedMph MPH  •  RALLY: ${engine.currentRally}",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Level Progress Bar
            LinearProgressIndicator(
                progress = { engine.levelProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape),
                color = Color(0xFF00E5FF),
                trackColor = Color.White.copy(alpha = 0.1f)
            )
        }
    }
}

@Composable
private fun StartGameOverlay(
    engine: PingPongGameEngine,
    onStartGame: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A233A)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🏓 CYBER PONG",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF00E5FF)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Compete against an adaptive AI engine.\nDifficulty scales up over time!",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "SELECT DIFFICULTY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.5f),
                    letterSpacing = 1.2.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Difficulty Selector Buttons
                DifficultyMode.entries.forEach { mode ->
                    val isSelected = engine.mode == mode
                    Button(
                        onClick = { engine.mode = mode },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) Color(0xFF00E5FF) else Color(0xFF25324E),
                            contentColor = if (isSelected) Color.Black else Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = mode.displayName,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onStartGame,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF76FF03)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "SERVE BALL & PLAY",
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun PauseMenuOverlay(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    soundEnabled: Boolean,
    onToggleSound: () -> Unit,
    hapticsEnabled: Boolean,
    onToggleHaptics: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A233A)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .padding(28.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "GAME PAUSED",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onResume,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("RESUME", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onRestart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("RESTART MATCH", color = Color.White)
                }
            }
        }
    }
}

// ── Draw Scope Helpers ───────────────────────────────────────────────────

private fun DrawScope.drawTableBackground() {
    // Draw Table Line Boundaries
    drawRect(
        color = Color(0xFF0D1527),
        size = size
    )

    // Center Dashed Net Line
    val centerY = size.height / 2f
    drawLine(
        color = Color.White.copy(alpha = 0.25f),
        start = Offset(0f, centerY),
        end = Offset(size.width, centerY),
        strokeWidth = 3f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 15f), 0f)
    )

    // Goal zone boundaries
    drawRect(
        color = Color(0xFFFF1744).copy(alpha = 0.05f),
        topLeft = Offset(0f, 0f),
        size = Size(size.width, 80f)
    )

    drawRect(
        color = Color(0xFF00E5FF).copy(alpha = 0.05f),
        topLeft = Offset(0f, size.height - 80f),
        size = Size(size.width, 80f)
    )
}

private fun DrawScope.drawPowerUps(engine: PingPongGameEngine) {
    engine.powerUpsOnField.forEach { pu ->
        val pulse = (sin(pu.activeTime * 6f) * 4f)
        val radius = pu.radius + pulse

        // Glowing outer circle
        drawCircle(
            color = pu.type.color.copy(alpha = 0.3f),
            radius = radius * 1.4f,
            center = pu.position
        )

        // Solid inner circle
        drawCircle(
            color = pu.type.color,
            radius = radius,
            center = pu.position
        )

        // Draw symbol
        drawContext.canvas.nativeCanvas.apply {
            val paint = Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 32f
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            drawText(pu.type.symbol, pu.position.x, pu.position.y + 10f, paint)
        }
    }
}

private fun DrawScope.drawParticlesAndTrails(engine: PingPongGameEngine) {
    // Draw Ball Motion Trail
    engine.trailParticles.forEach { p ->
        drawCircle(
            color = p.color.copy(alpha = p.alpha),
            radius = p.radius,
            center = p.position
        )
    }

    // Draw Spark Particles
    engine.sparkParticles.forEach { s ->
        drawCircle(
            color = s.color.copy(alpha = s.alpha),
            radius = s.radius,
            center = s.position
        )
    }
}

private fun DrawScope.drawBalls(engine: PingPongGameEngine) {
    engine.balls.forEach { ball ->
        val color = engine.getBallThemeColor(ball)

        // Ball Glow Aura
        drawCircle(
            color = color.copy(alpha = 0.4f),
            radius = ball.radius * 1.8f,
            center = ball.position
        )

        // Solid Ball
        drawCircle(
            color = color,
            radius = ball.radius,
            center = ball.position
        )

        // Inner Highlight
        drawCircle(
            color = Color.White.copy(alpha = 0.8f),
            radius = ball.radius * 0.35f,
            center = Offset(ball.position.x - ball.radius * 0.25f, ball.position.y - ball.radius * 0.25f)
        )
    }
}

private fun DrawScope.drawPaddles(engine: PingPongGameEngine) {
    val paddleHeight = engine.paddleHeight
    val paddleRadius = CornerRadius(14f, 14f)

    // Player Paddle (Bottom)
    val playerY = size.height - 90f
    val playerLeft = engine.playerPaddleX - engine.paddleWidth / 2f
    val playerTop = playerY - paddleHeight / 2f

    // Glow
    drawRoundRect(
        color = Color(0xFF00E5FF).copy(alpha = 0.35f),
        topLeft = Offset(playerLeft - 4f, playerTop - 4f),
        size = Size(engine.paddleWidth + 8f, paddleHeight + 8f),
        cornerRadius = CornerRadius(18f, 18f)
    )

    // Paddle Body
    drawRoundRect(
        color = Color(0xFF00E5FF),
        topLeft = Offset(playerLeft, playerTop),
        size = Size(engine.paddleWidth, paddleHeight),
        cornerRadius = paddleRadius
    )

    // AI Paddle (Top)
    val aiY = 60f
    val aiLeft = engine.aiPaddleX - engine.paddleWidth / 2f
    val aiTop = aiY - paddleHeight / 2f

    // Glow
    drawRoundRect(
        color = Color(0xFFFF1744).copy(alpha = 0.35f),
        topLeft = Offset(aiLeft - 4f, aiTop - 4f),
        size = Size(engine.paddleWidth + 8f, paddleHeight + 8f),
        cornerRadius = CornerRadius(18f, 18f)
    )

    // Paddle Body
    drawRoundRect(
        color = Color(0xFFFF1744),
        topLeft = Offset(aiLeft, aiTop),
        size = Size(engine.paddleWidth, paddleHeight),
        cornerRadius = paddleRadius
    )
}
