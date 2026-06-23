package com.example.composelearning.shaders


import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.composelearning.R
import kotlinx.coroutines.launch

val RIPPLE_SHADER = """
    uniform shader u_texture;    // The underlying UI layer
    uniform vec2 u_resolution;   // Canvas size
    uniform vec2 u_touch;        // Touch coordinate (normalized 0 to 1)
    uniform float u_time;        // Time since touch occurred

    half4 main(in vec2 fragCoord) {
        // 1. Normalize current pixel coordinate
        vec2 uv = fragCoord.xy / u_resolution.xy;

        // Fix aspect ratio distortion for distance calculations
        vec2 aspect = vec2(u_resolution.x / u_resolution.y, 1.0);
        vec2 uvCorrected = uv * aspect;
        vec2 touchCorrected = u_touch * aspect;

        // 2. Calculate distance from current pixel to touch point
        float dist = distance(uvCorrected, touchCorrected);

        // Define ripple properties
        float waveSpeed = 2.5;
        float waveFrequency = 40.0;
        float waveAmplitude = 0.03;

        // 3. Create the expanding wave front logic
        // The wave only exists behind the expanding radius boundary
        float currentRadius = u_time * waveSpeed;

        vec2 rippleOffset = vec2(0.0);

        if (dist < currentRadius) {
            // 4. Calculate sine wave intensity with an exponential decay factor
            // The wave dampens (dies out) the further it gets from the center
            float decay = exp(-2.0 * (currentRadius - dist));

            // Generate the wave pattern
            float wave = sin((dist - currentRadius) * waveFrequency);

            // Calculate direction pointing away from touch center
            vec2 direction = normalize(uvCorrected - touchCorrected);

            // Compute the coordinate displacement vector
            rippleOffset = direction * wave * waveAmplitude * decay;
        }

        // 5. Sample the original texture using warped coordinates
        return u_texture.eval((uv + rippleOffset) * u_resolution.xy);
    }
""".trimIndent()


@Composable
fun ShaderRippleScreen(onBack: () -> Unit = {}) {
    val scope = rememberCoroutineScope()

    // Initialize RuntimeShader
    val shader = remember { RuntimeShader(RIPPLE_SHADER) }

    // Animate time from 0.0s to 1.5s when tapped
    val animTime = remember { Animatable(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .onSizeChanged { size ->
                // Provide canvas size configurations to the GPU
                shader.setFloatUniform("u_resolution", size.width.toFloat(), size.height.toFloat())
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    scope.launch {
                        // Reset uniforms on a fresh tap event
                        shader.setFloatUniform(
                            "u_touch",
                            offset.x / size.width.toFloat(),
                            offset.y / size.height.toFloat()
                        )
                        animTime.snapTo(0f)

                        // Animate outward smoothly over 1 second
                        animTime.animateTo(
                            targetValue = 1.0f,
                            animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
                        )
                    }
                }
            }
            .graphicsLayer {
                // Pipe runtime animation frames directly to your uniform input
                shader.setFloatUniform("u_time", animTime.value)
                shader.setFloatUniform("u_resolution", size.width, size.height)

                // IMPORTANT: createRuntimeShaderEffect binds the content to the "u_texture" uniform
                renderEffect = RenderEffect.createRuntimeShaderEffect(shader, "u_texture").asComposeRenderEffect()
            }
    ) {
        // Any UI elements placed inside this Box will inherit the ripple distortion effect!
        Image(
            painter = painterResource(id = R.drawable.droid), // Substitute with your image asset
            contentDescription = "Target Backdrop View",
            modifier = Modifier.fillMaxSize()
        )

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
