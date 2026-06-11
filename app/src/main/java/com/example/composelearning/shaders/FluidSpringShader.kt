package com.example.composelearning.shaders

import android.graphics.RuntimeShader
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize

/**
 * MASTERCLASS: Fluid Spring Shader
 * Demonstrates:
 * 1. Euler Integration (Spring Physics)
 * 2. AGSL Thin-Film Interference Simulation
 * 3. Zero-Allocation Render Loop
 */
@Composable
fun FluidSpringShaderScreen(onBack: () -> Unit = {}) {
    // --- 1. PHYSICS STATE (Euler Springs) ---
    // We manage velocity manually for momentum instead of simple tweens.
    var targetX by remember { mutableFloatStateOf(0.5f) }
    var targetY by remember { mutableFloatStateOf(0.5f) }

    var currentX by remember { mutableFloatStateOf(0.5f) }
    var currentY by remember { mutableFloatStateOf(0.5f) }

    var velX by remember { mutableFloatStateOf(0f) }
    var velY by remember { mutableFloatStateOf(0f) }

    val stiffness = 180f
    val damping = 15f

    // The Physics Loop: Runs every frame, zero recomposition of the UI tree
    LaunchedEffect(Unit) {
        var lastTime = 0L
        while (true) {
            withFrameNanos { nanos ->
                if (lastTime == 0L) {
                    lastTime = nanos
                    return@withFrameNanos
                }
                val dt = (nanos - lastTime) / 1_000_000_000f // Delta time in seconds
                lastTime = nanos

                // Euler Spring Math: f = kx - bv
                val forceX = (targetX - currentX) * stiffness - velX * damping
                val forceY = (targetY - currentY) * stiffness - velY * damping

                velX += forceX * dt
                velY += forceY * dt

                currentX += velX * dt
                currentY += velY * dt
            }
        }
    }

    // --- 2. THE SHADER (Thin-Film / Snell Approximation) ---
    val shaderCode = """
        uniform vec2 u_resolution;
        uniform float u_time;
        uniform vec2 u_pointer; // Driven by physics spring

        half4 main(float2 fragCoord) {
            vec2 uv = fragCoord / u_resolution.xy;
            vec2 p = (fragCoord - 0.5 * u_resolution.xy) / u_resolution.y;
            vec2 pointer = (u_pointer - 0.5) * (u_resolution.x / u_resolution.y);

            // Calculate distance to physics-driven pointer
            float d = length(p - pointer);

            // Simulate Thin-Film Interference
            // thickness = distance-based phase shift
            float thickness = d * 15.0 - u_time * 0.8;

            // RGB shift based on wave interference phases (Interference colors)
            vec3 color;
            color.r = 0.5 + 0.5 * cos(thickness + 0.0);
            color.g = 0.5 + 0.5 * cos(thickness + 2.094); // 120 deg shift
            color.b = 0.5 + 0.5 * cos(thickness + 4.188); // 240 deg shift

            // Add a "fluid" gloss highlight near the pointer
            float gloss = smoothstep(0.08, 0.0, abs(d - 0.02));
            color += gloss * 0.4;

            // Darken edges for focus
            color *= smoothstep(1.5, 0.2, length(p));

            return half4(color, 1.0);
        }
    """.trimIndent()

    val runtimeShader = remember { RuntimeShader(shaderCode) }
    val shaderBrush = remember(runtimeShader) { ShaderBrush(runtimeShader) }
    var size by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { size = it }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    targetX = change.position.x / size.width
                    targetY = change.position.y / size.height
                }
            }
            .drawWithCache {
                // --- 3. ZERO-ALLOCATION UNIFORM UPDATES ---
                // This block runs every frame but doesn't trigger recomposition.
                onDrawWithContent {
                    if (size.width > 0 && size.height > 0) {
                        runtimeShader.setFloatUniform("u_resolution", size.width.toFloat(), size.height.toFloat())
                        runtimeShader.setFloatUniform("u_time", System.currentTimeMillis() / 1000f)
                        runtimeShader.setFloatUniform("u_pointer", currentX, currentY)

                        drawRect(brush = shaderBrush)
                    }
                }
            }
    )
}
