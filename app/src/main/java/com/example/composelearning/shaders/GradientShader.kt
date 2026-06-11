package com.example.composelearning.shaders // Change this to your project's actual package structure

import android.graphics.RuntimeShader
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.composelearning.ui.theme.ComposeLearningTheme

/**
 * Technical Specs from Figma:
 * Dimensions: 360.dp x 326.dp
 * Linear Gradient: 180deg (Top to Bottom)
 * Colors: #FFFFFF (100% Alpha) to rgba(197, 64, 80, 0.30)
 * Post Blur Filter: 52px (Equivalent to 104.dp radius mapping)
 */

private const val AGSL_ATMOSPHERIC_SRC = """
    uniform vec2 uSize;
    vec4 main(in vec2 fragCoord) {
        vec2 uv = fragCoord / uSize;

        // Define Figma design colors
        vec4 colorTop = vec4(1.0, 1.0, 1.0, 1.0); // Opaque White

        // Android graphics pipeline requires pre-multiplied alpha values
        // to prevent colors from wiping out into gray or background transparency.
        // Changing to a more reddish tone (rgba(197, 64, 80, 0.3))
        // RGB components are multiplied by Alpha (0.3)
        vec4 colorBottom = vec4(0.77255 * 0.3, 0.25098 * 0.3, 0.31372 * 0.3, 0.3);

        // Curve modification: controls how deep the red bleeds upward
        float gradientFactor = smoothstep(-0.05, 0.95, uv.y);
        vec4 mixedColor = mix(colorTop, colorBottom, gradientFactor);

        // Precision feather masking simulating the 52px edge-blur bleed bounds
        float leftEdge   = smoothstep(0.0, 0.08, uv.x);
        float rightEdge  = smoothstep(1.0, 0.92, uv.x);
        float topEdge    = smoothstep(0.0, 0.04, uv.y);
        float bottomEdge = smoothstep(1.0, 0.92, uv.y);

        float edgeMask = leftEdge * rightEdge * topEdge * bottomEdge;

        return mixedColor * edgeMask;
    }
"""

@Composable
fun GradientShader(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(320.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        // Shader Engine layer
        val shader = remember { RuntimeShader(AGSL_ATMOSPHERIC_SRC) }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithCache {
                    shader.setFloatUniform("uSize", size.width, size.height)
                    onDrawBehind {
                        drawRect(ShaderBrush(shader))
                    }
                }
        )
        content()
    }
}

@Preview(showBackground = true, heightDp = 320, backgroundColor = 0xFF000000)
@Composable
private fun GradientShaderPreview() {
    ComposeLearningTheme {
        GradientShader()
    }
}
