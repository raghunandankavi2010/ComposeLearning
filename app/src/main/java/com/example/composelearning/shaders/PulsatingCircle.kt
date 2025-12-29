package com.example.composelearning.shaders


import android.graphics.RuntimeShader
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.toArgb
import org.intellij.lang.annotations.Language

val RED = Color(0xFFD72206)
val BLUE = Color(0xFF06A6D7)
val GREEN = Color(0xFF8706D7)

@Language("AGSL")
private val blurShader = """
    
layout(color) uniform half4 iColor;
uniform float iResolution_x;
uniform float iResolution_y;
uniform float iTime;

half4 main(float2 fragCoord) {
    // Normalize coordinates to range from -1 to 1, maintaining aspect ratio
    float aspect = iResolution_x / iResolution_y;
    float2 uv = fragCoord / iResolution_y * 2.0 - 1.0;
    uv.x *= aspect;

    // Distance from the center
    float dist = length(uv);

    // Create a pulsating/glowing effect based on time
    // The sin function creates the pulse, smoothed with abs for a back and forth motion
    float pulse = abs(sin(iTime * 2.0));

    // A smooth step function for the main circle, with a soft edge
    float circle = smoothstep(0.5 + pulse * 0.1, 0.45 + pulse * 0.1, dist);

    // A wider, softer glow ring
    float glow = smoothstep(0.7 + pulse * 0.2, 0.55 + pulse * 0.2, dist) - circle;

    // Combine the circle and glow with the uniform color
    // The background remains transparent where circle and glow are 0
    half4 color = iColor * (circle + glow);

    return color;
}
""".trimIndent()

@Composable
fun PulsatingCircleShader() {
    val runtimeShader = remember { RuntimeShader(blurShader) }

    val infiniteTransition =
        rememberInfiniteTransition(label = "infinite transition for pulse and color")

    // Animation for the pulsation effect (iTime)
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f, // Large value for a long-running sine wave
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // Animation for the color change every second
    val animatedColor by infiniteTransition.animateColor(
        initialValue = RED,
        targetValue = BLUE,
        animationSpec = infiniteRepeatable(
            // Keyframes allow us to specify colors at different points in time
            animation = keyframes {
                durationMillis = 3000 // Total duration for one loop (3 colors, 1 second each)
                RED at 0 using LinearEasing // Start with RED
                GREEN at 1000 using LinearEasing // Switch to GREEN at 1 second
                BLUE at 2000 using LinearEasing // Switch to BLUE at 2 seconds
                // It will automatically loop back to RED at 3000ms
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "color"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawWithCache {
                val shaderBrush = ShaderBrush(runtimeShader)

                // This block re-executes whenever `time` or `animatedColor` changes.
                onDrawBehind {
                    // 1. Update pulsation uniform (iTime)
                    runtimeShader.setFloatUniform("iTime", time)

                    // 2. Update color uniform (iColor)
                    runtimeShader.setColorUniform("iColor", animatedColor.toArgb())

                    // Set resolution uniforms (only changes if size changes)
                    runtimeShader.setFloatUniform("iResolution_x", size.width)
                    runtimeShader.setFloatUniform("iResolution_y", size.height)

                    // 3. Draw the effect
                    drawCircle(
                        brush = shaderBrush,
                        radius = size.minDimension / 3f,
                        center = center
                    )
                }
            }
    )
}


//@Composable
//fun PulsatingCircleShader() {
//    // Create and remember the RuntimeShader and ShaderBrush
//    val runtimeShader = remember { RuntimeShader(blurShader) }
//    //val shaderBrush = remember { ShaderBrush(runtimeShader) }
//
//    // Animate time for the pulsating effect
//    val infiniteTransition = rememberInfiniteTransition(label = "infinite transition")
//    val time by infiniteTransition.animateFloat(
//        initialValue = 0f,
//        targetValue = 100f, // Target value doesn't matter much for an infinite sine wave
//        animationSpec = infiniteRepeatable(
//            animation = tween(durationMillis = 2000, easing = LinearEasing),
//            repeatMode = RepeatMode.Restart
//        ),
//        label = "time animation"
//    )
//
//    val shaderBrush = remember(time) {
//        ShaderBrush(runtimeShader)
//    }
//
//    Canvas(modifier = Modifier.fillMaxSize()) {
//
//        runtimeShader.setColorUniform(
//            "iColor",
//            RED.toArgb() // Correctly converts the RED color object to the required ARGB integer
//        )
////        runtimeShader.setColorUniform(
////            "iColor",
////            android.graphics.Color.valueOf(
////                RED.red,
////                BLUE.blue,
////                GREEN.blue,
////                RED.alpha
////            )
////        )
//        // Pass uniforms to the shader
//        runtimeShader.setFloatUniform("iResolution_x", size.width)
//        runtimeShader.setFloatUniform("iResolution_y", size.height)
//        runtimeShader.setFloatUniform("iTime", time)
//        //runtimeShader.setFloatUniform("iColor", circleColor.toArgb().toFloat()) // Set the desired color
//
//        // Draw the circle using the shader brush
//        drawCircle(
//            brush = shaderBrush,
//            radius = size.minDimension / 2f,
//            center = center
//        )
//    }
//}
