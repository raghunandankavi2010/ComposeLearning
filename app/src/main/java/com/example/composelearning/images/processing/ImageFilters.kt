package com.example.composelearning.images.processing

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Shader
import org.intellij.lang.annotations.Language
import kotlin.math.sin
import androidx.core.graphics.createBitmap

// =================================================================================================
// Filter catalog. Each preset emulates the kind of look TikTok / Instagram bake into LUTs.
// The pipeline is the same for every filter:
//   1) 3x4 color matrix (cross-channel mixing — sepia, greyscale, invert, tint shifts).
//   2) Per-channel 1D LUT (tone curve for shadows / mids / highlights — vintage S-curve, fade).
//   3) Brightness / contrast / saturation knobs (live user adjustments).
//   4) Optional radial vignette.
//   5) Blend back with the source by `intensity` so each preset has a strength slider.
//
// GPU path: one AGSL shader bound as a RenderEffect on a graphicsLayer — runs per pixel on the GPU,
// stays at display refresh rate even on 4K bitmaps. The LUT is shipped to the shader as a 256x1
// BitmapShader so the shader does a 1-sample texture lookup per channel.
//
// CPU path: identical math, but in Kotlin, parallelized across cores via coroutines. Slower than
// GPU, but useful for offline export (saving to disk) and for instructive A/B comparison.
// =================================================================================================

enum class ImageFilter(val label: String) {
    Original("Original"),
    Greyscale("Greyscale"),
    Sepia("Sepia"),
    Invert("Invert"),
    Vivid("Vivid"),
    Cool("Cool"),
    Warm("Warm"),
    Vintage("Vintage"),
    Cinematic("Cinematic"),
    Polaroid("Polaroid"),
    Noir("Noir"),
    Fade("Fade"),
    Cyberpunk("Cyberpunk"),
}

// One immutable description of a filter's math. Lives in memory, passed to both pipelines.
// `mat` is row-major 3x4 — three output channels (R,G,B), four inputs (R,G,B,A).
// `offset` is the constant added after the matrix multiply (per output channel).
// `lutR/G/B` are 256-entry tone curves; null means "leave the channel alone".
data class FilterSpec(
    val mat: FloatArray,
    val offset: FloatArray,
    val lutR: IntArray?,
    val lutG: IntArray?,
    val lutB: IntArray?,
) {
    val hasLut: Boolean get() = lutR != null && lutG != null && lutB != null
}

private val IDENTITY_MAT = floatArrayOf(
    1f, 0f, 0f, 0f,
    0f, 1f, 0f, 0f,
    0f, 0f, 1f, 0f,
)
private val ZERO_OFFSET = floatArrayOf(0f, 0f, 0f)

fun filterSpec(filter: ImageFilter): FilterSpec = when (filter) {
    ImageFilter.Original -> FilterSpec(IDENTITY_MAT, ZERO_OFFSET, null, null, null)

    // Rec.601 luma weights. Same matrix is applied to all three output channels so each pixel
    // collapses to its perceptual brightness.
    ImageFilter.Greyscale -> FilterSpec(
        mat = floatArrayOf(
            0.299f, 0.587f, 0.114f, 0f,
            0.299f, 0.587f, 0.114f, 0f,
            0.299f, 0.587f, 0.114f, 0f,
        ),
        offset = ZERO_OFFSET,
        lutR = null, lutG = null, lutB = null,
    )

    // Classic 1969 sepia transform (Microsoft's published matrix).
    ImageFilter.Sepia -> FilterSpec(
        mat = floatArrayOf(
            0.393f, 0.769f, 0.189f, 0f,
            0.349f, 0.686f, 0.168f, 0f,
            0.272f, 0.534f, 0.131f, 0f,
        ),
        offset = ZERO_OFFSET,
        lutR = null, lutG = null, lutB = null,
    )

    // Negative film inversion: 1 - channel.
    ImageFilter.Invert -> FilterSpec(
        mat = floatArrayOf(
            -1f, 0f, 0f, 0f,
            0f, -1f, 0f, 0f,
            0f, 0f, -1f, 0f,
        ),
        offset = floatArrayOf(1f, 1f, 1f),
        lutR = null, lutG = null, lutB = null,
    )

    // Punchy saturated look — handled mostly by user-facing sat/contrast bumps in the VM defaults.
    ImageFilter.Vivid -> FilterSpec(IDENTITY_MAT, ZERO_OFFSET, null, null, null)

    // Cool: amplify blue, pull red.
    ImageFilter.Cool -> FilterSpec(
        mat = floatArrayOf(
            0.9f, 0f, 0f, 0f,
            0f, 1.0f, 0f, 0f,
            0f, 0f, 1.15f, 0f,
        ),
        offset = floatArrayOf(0f, 0.02f, 0.04f),
        lutR = null, lutG = null, lutB = null,
    )

    // Warm: amplify red, pull blue.
    ImageFilter.Warm -> FilterSpec(
        mat = floatArrayOf(
            1.15f, 0f, 0f, 0f,
            0f, 1.0f, 0f, 0f,
            0f, 0f, 0.85f, 0f,
        ),
        offset = floatArrayOf(0.05f, 0.03f, 0f),
        lutR = null, lutG = null, lutB = null,
    )

    // Vintage: S-curve LUT for film-stock contrast + slight warm tint.
    ImageFilter.Vintage -> FilterSpec(
        mat = floatArrayOf(
            1.0f, 0f, 0f, 0f,
            0f, 0.95f, 0f, 0f,
            0f, 0f, 0.85f, 0f,
        ),
        offset = floatArrayOf(0.03f, 0.02f, 0f),
        lutR = sCurveLut(strength = 0.35f),
        lutG = sCurveLut(strength = 0.28f),
        lutB = sCurveLut(strength = 0.25f),
    )

    // Cinematic: teal shadows, orange highlights. Encoded as a per-channel curve that splits
    // around mid-grey then a small color matrix push.
    ImageFilter.Cinematic -> FilterSpec(
        mat = floatArrayOf(
            1.05f, 0f, 0f, 0f,
            0f, 0.98f, 0.04f, 0f,
            0.02f, 0.04f, 0.95f, 0f,
        ),
        offset = floatArrayOf(0.02f, 0f, 0f),
        lutR = tealOrangeLut(channel = 0),
        lutG = tealOrangeLut(channel = 1),
        lutB = tealOrangeLut(channel = 2),
    )

    // Polaroid: raised blacks, lowered whites (faded), slight cyan cast.
    ImageFilter.Polaroid -> FilterSpec(
        mat = floatArrayOf(
            1.0f, 0f, 0f, 0f,
            0f, 1.0f, 0f, 0f,
            0f, 0f, 1.0f, 0f,
        ),
        offset = floatArrayOf(0f, 0.02f, 0.04f),
        lutR = fadeLut(minOut = 30, maxOut = 220),
        lutG = fadeLut(minOut = 35, maxOut = 225),
        lutB = fadeLut(minOut = 50, maxOut = 230),
    )

    // Noir: full desaturation + steep S-curve. Greyscale matrix + aggressive contrast LUT.
    ImageFilter.Noir -> FilterSpec(
        mat = floatArrayOf(
            0.299f, 0.587f, 0.114f, 0f,
            0.299f, 0.587f, 0.114f, 0f,
            0.299f, 0.587f, 0.114f, 0f,
        ),
        offset = ZERO_OFFSET,
        lutR = sCurveLut(strength = 0.7f),
        lutG = sCurveLut(strength = 0.7f),
        lutB = sCurveLut(strength = 0.7f),
    )

    // Fade: low-contrast pastel look. Pure LUT remap (linear compression).
    ImageFilter.Fade -> FilterSpec(
        mat = IDENTITY_MAT,
        offset = ZERO_OFFSET,
        lutR = fadeLut(minOut = 40, maxOut = 210),
        lutG = fadeLut(minOut = 40, maxOut = 215),
        lutB = fadeLut(minOut = 55, maxOut = 225),
    )

    // Cyberpunk: magenta highlights, cyan shadows (neon city look).
    ImageFilter.Cyberpunk -> FilterSpec(
        mat = floatArrayOf(
            1.1f, 0.0f, 0.1f, 0f,
            0.0f, 0.85f, 0.05f, 0f,
            0.05f, 0f, 1.2f, 0f,
        ),
        offset = floatArrayOf(0.05f, -0.02f, 0.05f),
        lutR = neonLut(channel = 0),
        lutG = neonLut(channel = 1),
        lutB = neonLut(channel = 2),
    )
}

// =================================================================================================
// LUT generators. Each returns a 256-entry array indexed 0..255. The math is straightforward —
// per-channel response curves. Real film stocks ship multi-MB 3D cube LUTs but for a 1D channel
// curve these analytic forms get 90% of the look at ~1 kB of memory each.
// =================================================================================================

// Symmetric S-curve around 128 — like a soft contrast boost. `strength` 0..1 controls how steep
// the central slope is. At 0 the curve is the identity; at 1 the response saturates to a hard step.
private fun sCurveLut(strength: Float): IntArray = IntArray(256) { i ->
    val x = i / 255f
    // Smoothstep gives a soft S; mix with identity by `strength` so we never go fully clipped.
    val s = x * x * (3f - 2f * x)
    val out = x * (1f - strength) + s * strength
    (out.coerceIn(0f, 1f) * 255f).toInt()
}

// Linear remap: [0..255] → [minOut..maxOut]. Used by fade / polaroid for raised blacks +
// rolled-off whites — the "milky" film look you cannot get with a matrix alone.
private fun fadeLut(minOut: Int, maxOut: Int): IntArray {
    val span = maxOut - minOut
    return IntArray(256) { i -> (minOut + (span * i + 127) / 255).coerceIn(0, 255) }
}

// Teal-shadow / orange-highlight split. Each channel curve is shaped so that dark input pixels
// shift cyan-ish and bright pixels shift orange-ish. The standard "Hollywood blockbuster" grade.
private fun tealOrangeLut(channel: Int): IntArray = IntArray(256) { i ->
    val x = i / 255f
    val highlight = x * x * (3f - 2f * x)            // brighter on highlights
    val shadow = 1f - (1f - x) * (1f - x) * (3f - 2f * (1f - x)) // dimmer on shadows
    val out = when (channel) {
        0 -> x * 0.6f + highlight * 0.4f + 0.04f            // R: push highlights warm
        1 -> x * 0.75f + highlight * 0.20f + shadow * 0.05f // G: subtle
        else -> x * 0.55f + shadow * 0.45f                  // B: pull shadows cool
    }
    (out.coerceIn(0f, 1f) * 255f).toInt()
}

// Neon palette for cyberpunk. Magenta highlights, cyan shadows, with a small sine bend
// (channel-dependent phase) to inject color variance through the midtones.
private fun neonLut(channel: Int): IntArray = IntArray(256) { i ->
    val x = i / 255f
    val phase = when (channel) { 0 -> 0f; 1 -> 2.094f; else -> 4.188f } // 0°, 120°, 240°
    val bend = sin(x * Math.PI.toFloat() + phase) * 0.08f
    val tinted = when (channel) {
        0 -> x * 1.05f + 0.05f + bend
        1 -> x * 0.85f + bend
        else -> x * 1.05f + 0.08f + bend
    }
    (tinted.coerceIn(0f, 1f) * 255f).toInt()
}

// =================================================================================================
// LUT → GPU. Pack three channel curves into one 256x1 ARGB bitmap. The shader reads it as
// `lut.eval(float2(channelValue * 255 + 0.5, 0.5))` and uses the .r/.g/.b component matching the
// output channel — one sampler instead of three.
// =================================================================================================

fun buildLutBitmap(lutR: IntArray, lutG: IntArray, lutB: IntArray): Bitmap {
    val bmp = createBitmap(256, 1)
    val pixels = IntArray(256) { i ->
        (0xff shl 24) or
            ((lutR[i] and 0xff) shl 16) or
            ((lutG[i] and 0xff) shl 8) or
            (lutB[i] and 0xff)
    }
    bmp.setPixels(pixels, 0, 256, 0, 0, 256, 1)
    return bmp
}

// A no-op LUT (identity ramp). The GPU shader is built around a LUT sampler input that must be
// bound every frame — even when the current filter has no tone curve. We bind this one in that
// case and gate the lookup with a `useLut` uniform set to 0 so the eval is bypassed.
fun identityLutBitmap(): Bitmap {
    val ramp = IntArray(256) { it }
    return buildLutBitmap(ramp, ramp, ramp)
}

fun Bitmap.toClampShader(): BitmapShader =
    BitmapShader(this, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)

// =================================================================================================
// The single unified AGSL filter shader. Branching on uniforms is cheap on modern GPUs because
// every fragment in a draw sees the same uniform value — there's no warp divergence.
// =================================================================================================

@Language("AGSL")
val FILTER_SHADER: String = """
    uniform shader content;       // the image being filtered (bound automatically by RenderEffect)
    uniform shader lut;           // 256x1 RGBA tone curve; bypassed when useLut < 0.5
    uniform float2 resolution;    // layer size in pixels — used by the vignette
    uniform float useLut;         // 0 or 1: gate the LUT lookup
    uniform float intensity;      // 0..1: blend the filtered pixel back with the original
    uniform float brightness;     // -0.5..0.5 added after the tone curve
    uniform float contrast;       // 0..2 multiplier around 0.5
    uniform float saturation;     // 0..2: 0 = grey, 1 = identity, 2 = double sat
    uniform float vignette;       // 0..1: radial darkening at the corners

    uniform float4 matRow0;       // R-out row of the 3x4 color matrix
    uniform float4 matRow1;       // G-out
    uniform float4 matRow2;       // B-out
    uniform float3 matOffset;     // constant added per output channel

    half4 main(float2 fragCoord) {
        half4 src = content.eval(fragCoord);

        // 1) Color matrix: each output channel is a dot product of the input RGBA with one row.
        //    The matrix uniforms are float for setFloatUniform compatibility across API levels.
        half3 m = half3(
            dot(float4(src), matRow0) + matOffset.r,
            dot(float4(src), matRow1) + matOffset.g,
            dot(float4(src), matRow2) + matOffset.b
        );
        m = clamp(m, half3(0.0), half3(1.0));

        // 2) Per-channel LUT. The LUT bitmap is 256 pixels wide — sample x = value * 255 + 0.5
        // for nearest-texel precision (CLAMP tile mode keeps the ends in range).
        if (useLut > 0.5) {
            half lr = lut.eval(float2(float(m.r) * 255.0 + 0.5, 0.5)).r;
            half lg = lut.eval(float2(float(m.g) * 255.0 + 0.5, 0.5)).g;
            half lb = lut.eval(float2(float(m.b) * 255.0 + 0.5, 0.5)).b;
            m = half3(lr, lg, lb);
        }

        // 3) Brightness + contrast (around mid-grey).
        m = (m - half(0.5)) * half(contrast) + half(0.5) + half(brightness);

        // 4) Saturation. Mix between the luma-grey and the current color.
        half luma = dot(m, half3(0.299, 0.587, 0.114));
        m = mix(half3(luma), m, half(saturation));

        m = clamp(m, half3(0.0), half3(1.0));

        // 5) Vignette: radial 0..1 mask centered on the image, darkening the corners.
        if (vignette > 0.001) {
            float2 uv = fragCoord / resolution;
            float d = distance(uv, float2(0.5));
            half v = half(1.0 - smoothstep(0.35, 0.85, d) * vignette);
            m *= v;
        }

        // 6) Intensity blend back with the source so each preset has a 0..1 strength.
        half3 outRgb = mix(src.rgb, m, half(intensity));
        return half4(outRgb, src.a);
    }
""".trimIndent()

// =================================================================================================
// 3-D LUT cube swatches for the filter strip thumbnails. Each enum entry is rendered into a small
// gradient swatch so the user can see the look at a glance without having to apply it.
// =================================================================================================

fun thumbnailGradient(filter: ImageFilter): IntArray {
    val size = 64
    val pixels = IntArray(size * size)
    val spec = filterSpec(filter)
    for (y in 0 until size) {
        for (x in 0 until size) {
            // Sample a smooth hue/value gradient as a stand-in for "a photo".
            val u = x / (size - 1f)
            val v = y / (size - 1f)
            val r = u
            val g = (0.5f + 0.5f * sin(u * 6.28f + v * 3.14f))
            val b = 1f - v
            val (fr, fg, fb) = applySpecForSwatch(spec, r, g, b)
            val rr = (fr.coerceIn(0f, 1f) * 255).toInt()
            val gg = (fg.coerceIn(0f, 1f) * 255).toInt()
            val bb = (fb.coerceIn(0f, 1f) * 255).toInt()
            pixels[y * size + x] = (0xff shl 24) or (rr shl 16) or (gg shl 8) or bb
        }
    }
    return pixels
}

const val THUMBNAIL_SIZE: Int = 64

private fun applySpecForSwatch(spec: FilterSpec, r: Float, g: Float, b: Float): Triple<Float, Float, Float> {
    val nr = spec.mat[0] * r + spec.mat[1] * g + spec.mat[2] * b + spec.offset[0]
    val ng = spec.mat[4] * r + spec.mat[5] * g + spec.mat[6] * b + spec.offset[1]
    val nb = spec.mat[8] * r + spec.mat[9] * g + spec.mat[10] * b + spec.offset[2]
    var fr = nr.coerceIn(0f, 1f)
    var fg = ng.coerceIn(0f, 1f)
    var fb = nb.coerceIn(0f, 1f)
    if (spec.hasLut) {
        fr = spec.lutR!![(fr * 255).toInt().coerceIn(0, 255)] / 255f
        fg = spec.lutG!![(fg * 255).toInt().coerceIn(0, 255)] / 255f
        fb = spec.lutB!![(fb * 255).toInt().coerceIn(0, 255)] / 255f
    }
    return Triple(fr, fg, fb)
}