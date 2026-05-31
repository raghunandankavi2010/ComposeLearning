package com.example.composelearning.shaders

import android.graphics.RuntimeShader
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.ShaderBrush

@Composable
fun SpiralShaderScreen() {
    val infiniteTransition = rememberInfiniteTransition(label = "time")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "time"
    )

    val shaderCode = """ // AGSL requires uniforms to be declared at the top
uniform vec2 u_resolution;
uniform float u_time;

// Helper function to convert HSL to RGB
float3 hsl2rgb(float3 hsl) {
    float h = hsl.x;
    float s = hsl.y;
    float l = hsl.z;
    float3 rgb = float3(l);
    float c = (1.0 - abs(2.0 * l - 1.0)) * s;
    float m = l - c / 2.0;

    float segment = h * 6.0;
    float x = c * (1.0 - abs(mod(segment, 2.0) - 1.0));

    if (segment < 1.0) { rgb += float3(c, x, 0.0); } 
    else if (segment < 2.0) { rgb += float3(x, c, 0.0); } 
    else if (segment < 3.0) { rgb += float3(0.0, c, x); } 
    else if (segment < 4.0) { rgb += float3(0.0, x, c); } 
    else if (segment < 5.0) { rgb += float3(x, 0.0, c); } 
    else { rgb += float3(c, 0.0, x); }

    return rgb + m;
}

// AGSL main function takes the current pixel coordinate directly
half4 main(float2 fragCoord) {
    // 1. Properly center coordinates and correct aspect ratio
    float2 st = (fragCoord - 0.5 * u_resolution) / u_resolution.y;

    // 2. Get Polar Coordinates 
    float angle = atan(st.y, st.x);     // Range: -PI to PI
    float radius = length(st);          // Distance from center

    // 3. The Spiral Logic
    // "angle / (2.0 * 3.14159)" normalizes the angle to a 0.0 - 1.0 range.
    float normalized_angle = angle / (2.0 * 3.14159);
    
    // Multiple coils (tightness) and pulls the spiral outward continuously.
    float spiral = radius * 8.0 - normalized_angle - (u_time * 1.5);

    // Sharp, clean line for the spiral arm
    float line = smoothstep(0.1, 0.0, abs(fract(spiral) - 0.5));

    // 4. Color Logic
    // Map the angle to a 0.0 - 1.0 hue wheel, shifting over time
    float hue = fract(normalized_angle + u_time * 0.1);
    float3 spiral_color = hsl2rgb(float3(hue, 0.8, 0.5));

    // Multiply by line to only show color on the spiral arm
    float3 final_color = spiral_color * line;

    // Return the final color with full opacity
    return half4(half3(final_color), 1.0);
} """

    // 1. Initialize the Android RuntimeShader
    val runtimeShader = remember { RuntimeShader(shaderCode) }

    // 2. Wrap it in a Compose ShaderBrush so drawRect can read it
    val shaderBrush = remember(runtimeShader) { ShaderBrush(runtimeShader) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawWithCache {
                // 3. Update uniforms using the original runtimeShader
                runtimeShader.setFloatUniform("u_resolution", size.width, size.height)
                runtimeShader.setFloatUniform("u_time", time)

                onDrawWithContent {
                    // 4. Pass the brush wrapper to drawRect instead of the raw shader
                    drawRect(brush = shaderBrush)
                }
            }
    )
}