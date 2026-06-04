package com.example.composelearning.riveo.presentation

import android.graphics.RenderEffect as AndroidRenderEffect
import android.graphics.RuntimeShader
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import org.intellij.lang.annotations.Language

/**
 * AGSL port of William Candillon's Riveo page-curl shader
 * (`can-it-be-done-in-react-native/season5/src/Riveo/pageCurl.ts`).
 *
 * The full derivation of every line lives in `riveo/PageCurlMath.md`. In short:
 *
 *  - `image`        the rasterized card content (bound automatically by the RenderEffect).
 *  - `pointer`      current drag X (px). `origin` drag-start X (px).
 *  - `container`    the inner card rect as (left, top, right, bottom) in px.
 *  - `cornerRadius` rounded-corner radius of that rect, in px.
 *  - `resolution`   the layer size in px.
 *
 * The page is modeled as wrapping over a cylinder of radius [R]. A vertical "fold line"
 * sits at `x = container.right - (origin - pointer)`; the signed distance `d = xy.x - x`
 * of each pixel from that line selects one of three regions: flat page, the curling band
 * over the cylinder, or the area past the curl (transparent + drop shadow).
 *
 * SkSL → AGSL notes:
 *  - entry point is `half4 main(float2)`, sampler read with `image.eval(pixelCoord)`.
 *  - the original `project(p, mat3)` un-projects through a uniform scale-about-center. For a
 *    uniform scale `s` about pivot `c` that is exactly `c + (p - c) / s`, so we drop the
 *    `mat3`/`inverse()` machinery entirely (smaller, faster, identical result).
 */
@Language("AGSL")
private val PAGE_CURL_AGSL = """
    uniform shader image;
    uniform float pointer;
    uniform float origin;
    uniform float4 container;     // left, top, right, bottom (px)
    uniform float cornerRadius;
    uniform float2 resolution;

    const float PI = 3.141592653589793;
    const float R = 150.0;        // radius of the curl cylinder (px)

    // Inverse of "scale by s about pivot c", applied to point p.
    float2 project(float2 p, float2 s, float2 c) {
        return c + (p - c) / s;
    }

    // Is p inside the rounded rect rct = (left, top, right, bottom)?
    bool inRect(float2 p, float4 rct) {
        bool inside = p.x > rct.x && p.x < rct.z && p.y > rct.y && p.y < rct.w;
        if (!inside) {
            return false;
        }
        // Reject the four rounded corners.
        if (p.x < rct.x + cornerRadius && p.y < rct.y + cornerRadius) {
            return length(p - float2(rct.x + cornerRadius, rct.y + cornerRadius)) < cornerRadius;
        }
        if (p.x > rct.z - cornerRadius && p.y < rct.y + cornerRadius) {
            return length(p - float2(rct.z - cornerRadius, rct.y + cornerRadius)) < cornerRadius;
        }
        if (p.x < rct.x + cornerRadius && p.y > rct.w - cornerRadius) {
            return length(p - float2(rct.x + cornerRadius, rct.w - cornerRadius)) < cornerRadius;
        }
        if (p.x > rct.z - cornerRadius && p.y > rct.w - cornerRadius) {
            return length(p - float2(rct.z - cornerRadius, rct.w - cornerRadius)) < cornerRadius;
        }
        return true;
    }

    half4 main(float2 xy) {
        half4 color = image.eval(xy);
        float2 center = resolution * 0.5;

        float dx = origin - pointer;       // how far the finger dragged left
        float x = container.z - dx;        // fold line X (starts at the right edge)
        float d = xy.x - x;                // signed distance from the fold

        if (d > R) {
            // Past the curl: transparent, with a fading drop shadow over the card area.
            color = half4(0.0);
            if (inRect(xy, container)) {
                color.a = half(mix(0.5, 0.0, (d - R) / R));
            }
        } else if (d > 0.0) {
            // The curling band wrapped over the cylinder.
            float theta = asin(clamp(d / R, 0.0, 1.0));
            float d1 = theta * R;          // arc length on the front face
            float d2 = (PI - theta) * R;   // arc length on the back (underside)

            // Front face: slight scale to fake perspective foreshortening.
            float2 s1 = float2(1.0 + (1.0 - sin(PI / 2.0 + theta)) * 0.1);
            float2 uv1 = project(xy, s1, center);
            float2 p1 = float2(x + d1, uv1.y);

            // Back face (underside) shows through, scaled a touch more.
            float2 s2 = float2(1.1 + sin(PI / 2.0 + theta) * 0.1);
            float2 uv2 = project(xy, s2, center);
            float2 p2 = float2(x + d2, uv2.y);

            if (inRect(p2, container)) {
                color = image.eval(p2);
            } else if (inRect(p1, container)) {
                color = image.eval(p1);
                // Shade the front of the curl darker toward the fold.
                color.rgb *= half(pow(clamp((R - d) / R, 0.0, 1.0), 0.2));
            } else if (inRect(xy, container)) {
                color = half4(0.0);
                color.a = 0.5;
            }
        } else {
            // Flat, un-curled part of the page → original image.
            float2 s = float2(1.2);
            float2 uv = project(xy, s, center);
            float2 p = float2(x + abs(d) + PI * R, uv.y);
            if (inRect(p, container)) {
                color = image.eval(p);
            } else {
                color = image.eval(xy);
            }
        }
        return color;
    }
""".trimIndent()

/** Each card needs its own [RuntimeShader] instance (it carries per-card uniforms). */
fun createPageCurlShader(): RuntimeShader = RuntimeShader(PAGE_CURL_AGSL)

/**
 * Applies the page-curl [shader] as a content-transforming render effect on the card layer.
 *
 * [pointer] and [origin] are read as lambdas *inside* the `graphicsLayer` block so that the
 * per-frame drag updates invalidate only this draw layer — the composable itself never
 * recomposes (the same performance rule used by the AGSL demos in `ShaderExample.kt`).
 *
 * @param padding inset of the card content from the layer edges (defines `container`).
 * @param cornerRadius rounded-corner radius of the card.
 */
fun Modifier.pageCurl(
    shader: RuntimeShader,
    pointer: () -> Float,
    origin: () -> Float,
    padding: Dp,
    cornerRadius: Dp,
): Modifier = this.graphicsLayer {
    clip = false
    val padPx = padding.toPx()
    val radiusPx = cornerRadius.toPx()

    shader.setFloatUniform("resolution", size.width, size.height)
    shader.setFloatUniform("pointer", pointer())
    shader.setFloatUniform("origin", origin())
    shader.setFloatUniform(
        "container",
        padPx,                  // left
        padPx,                  // top
        size.width - padPx,     // right
        size.height - padPx,    // bottom
    )
    shader.setFloatUniform("cornerRadius", radiusPx)

    renderEffect = AndroidRenderEffect
        .createRuntimeShaderEffect(shader, "image")
        .asComposeRenderEffect()
}
