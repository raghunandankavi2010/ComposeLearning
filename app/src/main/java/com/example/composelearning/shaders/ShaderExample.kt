package com.example.composelearning.shaders

import android.graphics.RenderEffect as AndroidRenderEffect
import android.graphics.RuntimeShader
import android.graphics.Shader as AndroidShader
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.composelearning.sliders.Slider
import org.intellij.lang.annotations.Language
import kotlin.math.roundToInt


// =================================================================================================
// HUB SCREEN — entry point. Wire ShadersHubScreen() into your AppNavigation as a destination.
// =================================================================================================

// Stable seeded Picsum URL — same image every time. Swap for your own CDN photo in production.
private const val DEMO_PHOTO_URL = "https://picsum.photos/seed/agsl-shader-demo/1080/1920"

private enum class ShaderDemo(val title: String, val subtitle: String) {
    BlurImage("Image blur (AGSL)", "Fixed-radius AGSL blur applied via RenderEffect"),
    BlurImageControls("Blur with controls (AGSL)", "Two sliders driving radius + alpha uniforms"),
    NativeBlur("Native blur (production)", "Android's hardware Gaussian — 5-10x faster than AGSL"),
    FrostedGlass("Frosted glass card", "Glassmorphism over a photo backdrop (native blur)"),
    MeshGradient("Animated mesh gradient", "Time-driven blobs as a ShaderBrush"),
    Shimmer("Shimmer skeleton", "Diagonal sweep for loading placeholders"),
    LiquidButton("Liquid button", "Touch-driven ripple distortion"),
    FilmGrain("Film grain overlay", "Animated noise on top of any content"),
}

@Composable
fun ShadersHubScreen() {
    var current by remember { mutableStateOf<ShaderDemo?>(null) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        if (current == null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp),
            ) {
                item {
                    Text(
                        "AGSL Shader Demos",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }
                items(ShaderDemo.entries) { demo ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { current = demo },
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(demo.title, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(demo.subtitle, color = Color.Gray, fontSize = 13.sp)
                        }
                    }
                }
            }
        } else {
            Column(Modifier.fillMaxSize().statusBarsPadding()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { current = null }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Text(current!!.title, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                }
                Box(Modifier.fillMaxSize()) {
                    when (current!!) {
                        ShaderDemo.BlurImage -> BlurImageDemo()
                        ShaderDemo.BlurImageControls -> BlurImageWithControlsDemo()
                        ShaderDemo.NativeBlur -> NativeBlurDemo()
                        ShaderDemo.FrostedGlass -> FrostedGlassDemo()
                        ShaderDemo.MeshGradient -> MeshGradientDemo()
                        ShaderDemo.Shimmer -> ShimmerSkeletonDemo()
                        ShaderDemo.LiquidButton -> LiquidButtonDemo()
                        ShaderDemo.FilmGrain -> FilmGrainDemo()
                    }
                }
            }
        }
    }
}


// =================================================================================================
// HELPERS
// =================================================================================================

// Apply a RuntimeShader as a content-transforming render effect on a layer.
// The named `sampler` uniform inside the AGSL receives the layer's rasterized content.
// `setUniforms` runs every recomposition before the effect is built, so you can pass time/state
// uniforms there.
private fun Modifier.runtimeShaderRenderEffect(
    shader: RuntimeShader,
    sampler: String = "content",
    setUniforms: RuntimeShader.() -> Unit = {},
): Modifier = this
    .graphicsLayer {
        clip = true
        shader.setUniforms()
        // setFloatUniform("resolution", w, h) — every shader below expects this uniform.
        // We set it here so callers don't have to remember.
        shader.setFloatUniform("resolution", size.width, size.height)
        renderEffect = AndroidRenderEffect
            .createRuntimeShaderEffect(shader, sampler)
            .asComposeRenderEffect()
    }

// A monotonic seconds counter that updates once per frame.
// IMPORTANT: callers MUST NOT destructure with `by` at composable scope. Doing so registers the
// caller composable as a reader of this state, causing it to recompose at 60Hz. Instead capture
// the State<Float> as a `val` and read `.value` inside draw lambdas (drawBehind / drawWithCache /
// graphicsLayer block) so invalidation is scoped to the draw, not composition.
@Composable
private fun rememberShaderTime(): State<Float> = produceState(0f) {
    val start = withFrameNanos { it }
    while (true) {
        withFrameNanos { now ->
            value = (now - start) / 1_000_000_000f
        }
    }
}


// =================================================================================================
// DEMO 1 — Image blur (fixed radius). The original example had two bugs: the `iChannel0` sampler
// was never bound, and the uniform name in Kotlin (`iResolution`) did not match the AGSL
// (`resolution`). Both are fixed here by using `RenderEffect.createRuntimeShaderEffect`, which
// auto-binds the layer content to the named sampler.
// =================================================================================================

@Composable
fun BlurImageDemo() {
    val shader = remember { RuntimeShader(BLUR_SHADER) }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AsyncImage(
            model = DEMO_PHOTO_URL,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .runtimeShaderRenderEffect(shader) {
                    setFloatUniform("radius", 12f)
                    setFloatUniform("alpha", 1f)
                },
        )
    }
}


// =================================================================================================
// DEMO 2 — Same blur shader, exposed via two sliders. This is the production-shaped pattern:
// uniforms come from state, the Image (or any composable) carries a graphicsLayer render effect,
// and recomposition is cheap because Compose only re-applies the effect when state changes.
// =================================================================================================

@Composable
fun BlurImageWithControlsDemo() {
    var radius by remember { mutableFloatStateOf(8f) }
    var alpha by remember { mutableFloatStateOf(1f) }
    val shader = remember { RuntimeShader(BLUR_SHADER) }

    Column(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = DEMO_PHOTO_URL,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .runtimeShaderRenderEffect(shader) {
                        setFloatUniform("radius", radius)
                        setFloatUniform("alpha", alpha)
                    },
            )
        }

        Column(Modifier.padding(16.dp)) {
            Text("Radius: ${radius.roundToInt()}px")
            Slider(
                value = radius,
                onValueChange = { radius = it },
                valueRange = 0f..40f,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            Text("Alpha: ${"%.2f".format(alpha)}")
            Slider(
                value = alpha,
                onValueChange = { alpha = it },
                valueRange = 0f..1f,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}


// =================================================================================================
// DEMO 2b — Native blur. Same UI as DEMO 2 (radius + alpha sliders) but routed through Android's
// built-in RenderEffect.createBlurEffect, which is a hardware-tuned Gaussian implemented in
// native C++ — typically 5-10x faster than the AGSL fake-Gaussian above at large radii.
//
// Use AGSL when you need custom math (the other demos in this file). Use native blur whenever
// you just need a Gaussian — production frosted glass, hero backdrops, modal scrims, etc.
// `alpha` here is just `Modifier.alpha`; the native blur does not expose an alpha uniform.
// =================================================================================================

// Production helper: blur any composable using Android's native Gaussian.
// Cost is proportional to radius but vastly cheaper than the AGSL equivalent because the kernel
// is separable and hardware-vectorized. radiusX/Y are in pixels; Shader.TileMode.CLAMP avoids
// edge fade at the bounds. Pass radius == 0 to disable (createBlurEffect throws on 0).
fun Modifier.nativeBlur(radius: Float): Modifier =
    if (radius <= 0f) this
    else this.graphicsLayer {
        clip = true
        renderEffect = AndroidRenderEffect
            .createBlurEffect(radius, radius, AndroidShader.TileMode.CLAMP)
            .asComposeRenderEffect()
    }

@Composable
fun NativeBlurDemo() {
    var radius by remember { mutableFloatStateOf(8f) }
    var alpha by remember { mutableFloatStateOf(1f) }

    Column(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = DEMO_PHOTO_URL,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alpha = alpha,
                modifier = Modifier
                    .fillMaxSize()
                    .nativeBlur(radius),
            )
        }

        Column(Modifier.padding(16.dp)) {
            Text("Radius: ${radius.roundToInt()}px  (native Gaussian)")
            Slider(
                value = radius,
                onValueChange = { radius = it },
                valueRange = 0f..40f,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            Text("Alpha: ${"%.2f".format(alpha)}")
            Slider(
                value = alpha,
                onValueChange = { alpha = it },
                valueRange = 0f..1f,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}


// =================================================================================================
// DEMO 3 — Frosted glass card over a photo. The card composable renders the SAME background image
// underneath itself, sized + offset to align with the parent backdrop, then runs the blur render
// effect on that inner image. Result: the card appears to blur whatever is behind it.
//
// Production path: uses Modifier.nativeBlur (Android's hardware Gaussian) rather than the AGSL
// blur — cheaper at this size and visually indistinguishable.
// =================================================================================================

@Composable
fun FrostedGlassDemo() {
    androidx.compose.foundation.layout.BoxWithConstraints(Modifier.fillMaxSize()) {
        val containerW = maxWidth
        val containerH = maxHeight
        val cardW = 300.dp
        val cardH = 180.dp

        AsyncImage(
            model = DEMO_PHOTO_URL,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        // The frosted card. Stacks: blurred backdrop slice + white tint + content.
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(cardW, cardH)
                .clip(RoundedCornerShape(28.dp)),
        ) {
            val density = LocalDensity.current
            // Offsets so the inner image aligns with what's behind the card. Card is centered, so
            // shift the full-size backdrop by -(container - card)/2.
            val offsetX = with(density) { (-(containerW - cardW) / 2).roundToPx() }
            val offsetY = with(density) { (-(containerH - cardH) / 2).roundToPx() }

            AsyncImage(
                model = DEMO_PHOTO_URL,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(containerW, containerH)
                    .offset { IntOffset(offsetX, offsetY) }
                    .nativeBlur(radius = 32f),
            )
            // Translucent white tint — the "frost" that makes the blur look milky.
            Box(Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.18f)))

            Column(
                Modifier.fillMaxSize().padding(20.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text("Frosted Glass", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Backdrop blurred by an AGSL render effect.",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 14.sp,
                )
            }
        }
    }
}


// =================================================================================================
// DEMO 4 — Animated mesh gradient. No content sampler; the shader generates color from scratch
// using time-driven sin/cos blobs. Drawn via ShaderBrush so it composes like any other Brush.
// =================================================================================================

@Composable
fun MeshGradientDemo() {
    val shader = remember { RuntimeShader(MESH_GRADIENT_SHADER) }
    val timeState = rememberShaderTime()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawWithCache {
                shader.setFloatUniform("resolution", size.width, size.height)
                val brush = ShaderBrush(shader)
                onDrawBehind {
                    // Reading timeState.value HERE (inside onDrawBehind) scopes the per-frame
                    // invalidation to draw only — the composable itself does not recompose.
                    shader.setFloatUniform("time", timeState.value)
                    drawRect(brush)
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Welcome back",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Sign in to continue",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 16.sp,
            )
        }
    }
}


// =================================================================================================
// DEMO 5 — Shimmer skeleton. Each placeholder draws a ShaderBrush with a moving diagonal
// highlight. Cheaper than animating a LinearGradient because the sweep is computed per-pixel on
// the GPU instead of by recomposing brush positions on the CPU.
// =================================================================================================

@Composable
fun ShimmerSkeletonDemo() {
    val timeState = rememberShaderTime()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        repeat(5) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ShimmerBox(Modifier.size(64.dp).clip(CircleShape), timeState)
                Spacer(Modifier.width(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ShimmerBox(
                        Modifier
                            .fillMaxWidth(0.7f)
                            .height(14.dp)
                            .clip(RoundedCornerShape(7.dp)),
                        timeState,
                    )
                    ShimmerBox(
                        Modifier
                            .fillMaxWidth(0.45f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        timeState,
                    )
                }
            }
        }
    }
}

@Composable
private fun ShimmerBox(modifier: Modifier, timeState: State<Float>) {
    val shader = remember { RuntimeShader(SHIMMER_SHADER) }
    val base = MaterialTheme.colorScheme.surfaceVariant
    val highlight = MaterialTheme.colorScheme.surface
    Box(
        modifier = modifier.drawBehind {
            shader.setFloatUniform("resolution", size.width, size.height)
            shader.setFloatUniform("time", timeState.value)
            // Pass colors as 4 floats. Avoids the layout(color) / setColorUniform pairing entirely,
            // which is finicky across API levels and throws cryptic "non-color specific APIs" errors
            // when the qualifier on the uniform doesn't match the Kotlin call.
            shader.setFloatUniform("baseColor", base.red, base.green, base.blue, base.alpha)
            shader.setFloatUniform("highlightColor", highlight.red, highlight.green, highlight.blue, highlight.alpha)
            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint().apply { this.shader = shader }
                canvas.nativeCanvas.drawRect(0f, 0f, size.width, size.height, paint)
            }
        },
    )
}


// =================================================================================================
// DEMO 6 — Liquid button. The render effect samples the button's own content (a flat colored box +
// text) and displaces each pixel based on distance to the last touch point. The ripple amplitude
// is driven by an Animatable that snaps to 1 on press and decays to 0 over one second.
// =================================================================================================

@Composable
fun LiquidButtonDemo() {
    val shader = remember { RuntimeShader(LIQUID_BUTTON_SHADER) }
    val timeState = rememberShaderTime()
    // Holding these as State (not destructured) so reads only happen inside the graphicsLayer
    // block in runtimeShaderRenderEffect — that scopes per-frame invalidation to the layer.
    val touchState = remember { mutableStateOf(Offset(0.5f, 0.5f)) }
    val ripple = remember { Animatable(0f) }
    var pressCounter by remember { mutableIntStateOf(0) }

    LaunchedEffect(pressCounter) {
        if (pressCounter == 0) return@LaunchedEffect
        ripple.snapTo(1f)
        ripple.animateTo(0f, animationSpec = tween(900, easing = LinearEasing))
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(width = 240.dp, height = 72.dp)
                .clip(RoundedCornerShape(36.dp))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = { offset ->
                            touchState.value = Offset(offset.x / size.width, offset.y / size.height)
                            pressCounter++
                            tryAwaitRelease()
                        },
                    )
                }
                .runtimeShaderRenderEffect(shader) {
                    setFloatUniform("time", timeState.value)
                    val t = touchState.value
                    setFloatUniform("touch", t.x, t.y)
                    setFloatUniform("pressure", ripple.value)
                },
            contentAlignment = Alignment.Center,
        ) {
            // The "content" the shader will displace. Solid color + label.
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF6A11CB), Color(0xFF2575FC)),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text("Press me", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
            }
        }
    }
}


// =================================================================================================
// DEMO 7 — Film grain overlay. A modifier you can drop on any composable. The shader samples the
// content sampler and adds per-pixel hash noise mixed by `intensity`. Time perturbs the hash seed
// so grain animates each frame.
// =================================================================================================

// Self-contained: the modifier owns its own time clock so callers don't need to track frames.
// timeState.value is read inside the graphicsLayer block (via runtimeShaderRenderEffect's lambda),
// so per-frame invalidation is scoped to the layer — the caller composable does NOT recompose.
fun Modifier.filmGrain(intensity: Float = 0.18f): Modifier = composed("filmGrain") {
    val shader = remember { RuntimeShader(FILM_GRAIN_SHADER) }
    val timeState = rememberShaderTime()
    runtimeShaderRenderEffect(shader) {
        setFloatUniform("time", timeState.value)
        setFloatUniform("intensity", intensity)
    }
}

@Composable
fun FilmGrainDemo() {
    var intensity by remember { mutableFloatStateOf(0.25f) }

    Column(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxWidth().weight(1f)) {
            AsyncImage(
                model = DEMO_PHOTO_URL,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .filmGrain(intensity),
            )
        }
        Column(Modifier.padding(16.dp)) {
            Text("Grain intensity: ${"%.2f".format(intensity)}")
            Slider(
                value = intensity,
                onValueChange = { intensity = it },
                valueRange = 0f..0.6f,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}


// =================================================================================================
// AGSL SHADERS
//
// AGSL primer (vs Shadertoy GLSL):
//   • Entry point is `half4 main(float2 fragCoord)` — NOT `void mainImage(out vec4, in vec2)`.
//   • `fragCoord` is in pixel coords (0..resolution), not normalized.
//   • There are no Shadertoy globals (`iTime`, `iResolution`, `iMouse`). You declare your own
//     `uniform` lines and set them from Kotlin via setFloatUniform / setColorUniform.
//   • Use `half` where precision allows — GPUs run it faster.
//   • Sampler input is declared `uniform shader content;` and read with `content.eval(coord)`,
//     where `coord` is in PIXEL coords (not UVs) when the sampler comes from a RenderEffect layer.
// =================================================================================================

// Directional/spiral Gaussian-ish blur. Sampler `content` is the layer's rasterized output.
// `radius` is in pixels. `alpha` multiplies the final output's alpha for fade transitions.
@Language("AGSL")
private val BLUR_SHADER = """
    uniform float2 resolution;   // layer size in pixels (set automatically by runtimeShaderRenderEffect)
    uniform float radius;        // blur radius in pixels
    uniform float alpha;         // 0..1 final alpha multiplier
    uniform shader content;      // the layer's content; eval() takes pixel coords

    half4 main(float2 fragCoord) {
        const float TWO_PI = 6.28318530718;
        const int DIRECTIONS = 16;   // samples around the circle
        const int QUALITY = 3;       // samples along each radius

        // Center sample first.
        half4 color = content.eval(fragCoord);

        // Walk the disk: for each direction, take QUALITY samples at increasing distance.
        for (int d = 0; d < DIRECTIONS; d++) {
            float angle = float(d) * TWO_PI / float(DIRECTIONS);
            float2 dir = float2(cos(angle), sin(angle));
            for (int i = 1; i <= QUALITY; i++) {
                float t = float(i) / float(QUALITY);   // 0..1 along the radius
                color += content.eval(fragCoord + dir * radius * t);
            }
        }
        // Average: 1 center + DIRECTIONS*QUALITY ring samples.
        color /= float(DIRECTIONS * QUALITY + 1);
        color.a *= half(alpha);
        return color;
    }
""".trimIndent()


// Three colored blobs moving with sin/cos, smoothstep'd for soft edges, plus a tonal palette.
// Useful pattern: cosine-palette gradients (Inigo Quilez) for cheap procedural colors.
@Language("AGSL")
private val MESH_GRADIENT_SHADER = """
    uniform float2 resolution;
    uniform float time;

    // Cosine palette: a + b * cos(2π * (c*t + d)). Each of a/b/c/d is an RGB vector.
    half3 palette(float t) {
        half3 a = half3(0.5, 0.5, 0.5);
        half3 b = half3(0.5, 0.5, 0.5);
        half3 c = half3(1.0, 1.0, 1.0);
        half3 d = half3(0.20, 0.42, 0.58);   // tweak this for color theme
        return a + b * cos(half(6.28318) * (c * half(t) + d));
    }

    half4 main(float2 fragCoord) {
        // Centered, square-aspect UV in range roughly -1..1.
        float2 uv = (fragCoord * 2.0 - resolution) / min(resolution.x, resolution.y);

        half3 col = half3(0.0);
        // Three blobs orbiting on Lissajous-ish paths.
        for (int i = 0; i < 3; i++) {
            float fi = float(i);
            float2 p = float2(
                sin(time * 0.35 + fi * 1.7),
                cos(time * 0.45 + fi * 2.3)
            ) * 0.6;
            float d = length(uv - p);
            // Soft falloff: bright at the blob center, zero past 0.9.
            half intensity = half(smoothstep(0.9, 0.0, d));
            col += palette(fi * 0.33 + time * 0.05) * intensity;
        }
        // Tonemap-ish soft clamp so overlapping blobs don't blow out.
        col = col / (col + half3(1.0));
        return half4(col, 1.0);
    }
""".trimIndent()


// Diagonal highlight sweep over a base color. `time` advances the sweep position.
@Language("AGSL")
private val SHIMMER_SHADER = """
    uniform float2 resolution;
    uniform float time;
    // `layout(color)` is required when setting these via setColorUniform from Kotlin.
    // Without it the runtime treats the uniform as a plain vec4 and setColorUniform throws.
    uniform half4 baseColor;     // RGBA in 0..1, set via setFloatUniform(name, r, g, b, a)
    uniform half4 highlightColor;

    half4 main(float2 fragCoord) {
        float2 uv = fragCoord / resolution;
        // Diagonal coordinate: uv.x + uv.y ranges 0..2. Wrap time to a 0..2 sweep position.
        float pos = uv.x + uv.y;
        float sweep = fract(time * 0.6) * 2.0;
        // Soft highlight 0.6 wide centered on `sweep`.
        float dist = abs(pos - sweep);
        half intensity = half(smoothstep(0.6, 0.0, dist));
        return mix(baseColor, highlightColor, intensity);
    }
""".trimIndent()


// Touch-driven displacement: each pixel is shifted along the direction away from the touch
// point, by a sine wave whose amplitude decays with distance and with `pressure`.
@Language("AGSL")
private val LIQUID_BUTTON_SHADER = """
    uniform float2 resolution;
    uniform float time;
    uniform float2 touch;     // normalized 0..1 touch position
    uniform float pressure;   // 0..1 ripple amplitude (decays after release)
    uniform shader content;

    half4 main(float2 fragCoord) {
        float2 uv = fragCoord / resolution;
        float2 toTouch = uv - touch;
        float dist = length(toTouch);

        // Sine wave radiating outward, attenuated by distance.
        float wave = sin(dist * 28.0 - time * 9.0) * exp(-dist * 4.0);
        float amp = wave * 0.04 * pressure;

        // Push each pixel along (uv - touch), converted back to pixel coords for the eval call.
        float2 dir = normalize(toTouch + float2(1e-5, 1e-5));
        float2 displaced = fragCoord + dir * amp * resolution.x;

        return content.eval(displaced);
    }
""".trimIndent()


// Per-pixel pseudo-random noise added to the content. `time` perturbs the hash seed so each
// frame's grain is different (the characteristic "moving grain" of analog film).
@Language("AGSL")
private val FILM_GRAIN_SHADER = """
    uniform float2 resolution;
    uniform float time;
    uniform float intensity;
    uniform shader content;

    // Classic hash from (x, y) → 0..1. Time slips into the seed for animation.
    float hash(float2 p) {
        return fract(sin(dot(p, float2(127.1, 311.7)) + time * 100.0) * 43758.5453);
    }

    half4 main(float2 fragCoord) {
        half4 color = content.eval(fragCoord);
        float n = hash(fragCoord) - 0.5;   // -0.5..0.5 so grain is signed
        half3 grain = half3(n * intensity);
        return half4(color.rgb + grain, color.a);
    }
""".trimIndent()


// =================================================================================================
// PREVIEWS — each demo previewable in isolation in Android Studio.
// =================================================================================================

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun PreviewHub() {
    ShadersHubScreen()
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun PreviewBlurImage() { BlurImageDemo() }

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun PreviewBlurImageControls() { BlurImageWithControlsDemo() }

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun PreviewNativeBlur() { NativeBlurDemo() }

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun PreviewFrostedGlass() { FrostedGlassDemo() }

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun PreviewMeshGradient() { MeshGradientDemo() }

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun PreviewShimmer() { ShimmerSkeletonDemo() }

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun PreviewLiquidButton() { LiquidButtonDemo() }

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun PreviewFilmGrain() { FilmGrainDemo() }
