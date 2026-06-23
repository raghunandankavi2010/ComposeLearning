package com.example.composelearning.shaders

import android.graphics.RuntimeShader
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.unit.dp

/**
 * A simple shader that changes color over time using a sine wave.
 * Formula: vec4(abs(sin(u_time)), 0.0, 0.0, 1.0)
 */
@Composable
fun SimpleTimeShaderScreen(onBack: () -> Unit = {}) {
    // Monotonic time in seconds
    val time by produceState(0f) {
        val startNanos = withFrameNanos { it }
        while (true) {
            withFrameNanos { now ->
                value = (now - startNanos) / 1_000_000_000f
            }
        }
    }

    // AGSL version of the provided GLSL shader
    val shaderCode = """
        uniform float u_time;

        half4 main(float2 fragCoord) {
            // abs(sin(u_time)) for the red channel, as requested
            return half4(abs(sin(u_time)), 0.0, 0.0, 1.0);
        }
    """.trimIndent()

    val runtimeShader = remember { RuntimeShader(shaderCode) }
    val shaderBrush = remember(runtimeShader) { ShaderBrush(runtimeShader) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawWithCache {
                onDrawWithContent {
                    // Update the time uniform
                    runtimeShader.setFloatUniform("u_time", time)
                    drawRect(brush = shaderBrush)
                }
            }
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .systemBarsPadding()
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
    }
}
