package com.example.composelearning.shaders

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RuntimeShader
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.toIntSize
import com.example.composelearning.R
import com.example.composelearning.sliders.Slider
import org.intellij.lang.annotations.Language


@Composable
fun BlurImageComposable() {
    val context = LocalContext.current
    var blurAmount by remember { mutableStateOf(8f) }
    val imageBitmap = ImageBitmap.imageResource(id = R.drawable.ic_launcher_background) // Replace with your image resource

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Blur Amount: ${blurAmount.toInt()}")
        Slider(
            value = blurAmount,
            onValueChange = { blurAmount = it },
            valueRange = 0f..20f,
            modifier = Modifier.padding(16.dp)
        )

        val shader = RuntimeShader(blurShader)
        shader.setFloatUniform("Size", blurAmount)
        shader.setFloatUniform("iResolution", context.resources.displayMetrics.widthPixels.toFloat(), context.resources.displayMetrics.heightPixels.toFloat())

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawIntoCanvas {
                it.drawImageRect(
                    imageBitmap,
                    dstSize = size.toIntSize(),
                    paint = Paint().apply {
                        this.shader = shader
                    }
                )
            }
        }
    }
}


@Composable
fun MyApp() {
    Surface(modifier = Modifier.fillMaxSize()) {
        BlurImageComposable()
    }
}

@Preview
@Composable
fun PreviewMyApp() {
    MyApp()
}

@Language("AGSL")
private val blurShader = """
    uniform float Size;
    uniform float2 resolution;
    uniform shader iChannel0;

    float4 main(float2 fragCoord) {
        const float Pi = 6.28318530718;
        const int Directions = 16;
        const int Quality = 3;
        float2 Radius = Size / resolution;
        float2 uv = fragCoord / resolution;
        float4 color = iChannel0.eval(uv);

        for (int d = 0; d < Directions; d++) {
            float angle = float(d) * Pi / float(Directions);
            for (int i = 1; i <= Quality; i++) {
                float quality = float(i) / float(Quality);
                color += iChannel0.eval(uv + float2(cos(angle), sin(angle)) * Radius * quality);
            }
        }
        color /= float(Quality * Directions - 15);
        return color;
    }
""".trimIndent()