package com.example.composelearning.globe

import android.graphics.BitmapShader
import android.graphics.RuntimeShader
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

/**
 * The per-pixel globe.
 *
 * Every pixel runs GLOBE.md §5 (inverse orthographic projection) to find the (λ, φ) under
 * it, §7 to turn that into a country id, and §8–§10 to shade it. Four extra taps offset in
 * **screen space** — not texture space, see §6b — give constant-width borders and graticule
 * lines regardless of how foreshortened the surface is at that pixel.
 */
const val GlobeAgsl = """
uniform float2 uCentre;      // disc centre in local pixels
uniform float  uRadius;      // K: sphere radius in pixels
uniform float3 uRot0;        // rows of R = Rx(phiC) * Ry(-lambdaC)   (GLOBE.md 3)
uniform float3 uRot1;
uniform float3 uRot2;
uniform float3 uSun;         // unit vector toward the sun, world frame
uniform float2 uTexSize;     // index texture size in texels
uniform float  uSelected;    // highlighted country id, or -1
uniform float  uBorderPx;    // tap offset for edge detection, in pixels
uniform float  uGratStep;    // graticule spacing in degrees; 0 disables
uniform float  uNight;       // 0 = flat lighting, 1 = full day/night model
uniform float  uBorders;     // 0/1
layout(color) uniform half4 uBackground;  // opaque backdrop; the shader composites onto it
layout(color) uniform half4 uBorderInk;
layout(color) uniform half4 uSelectInk;
uniform shader uIndex;       // bound with setInputBuffer: raw ids, no colour conversion
uniform shader uPalette;     // 256x1 id -> colour

const float PI = 3.14159265;
const float TWO_PI = 6.28318531;

// GLOBE.md 5: pixel -> view frame. Returns (u, v, w, rho^2); rho^2 > 1 means "misses".
float4 viewPoint(float2 frag) {
    float2 uv = (frag - uCentre) / uRadius;
    uv.y = -uv.y;
    float r2 = dot(uv, uv);
    return float4(uv.x, uv.y, sqrt(max(1.0 - r2, 0.0)), r2);
}

// n = R^T * (u,v,w): the inverse is the transpose because R is orthonormal (GLOBE.md 3).
float3 toWorld(float3 pv) {
    return float3(
        uRot0.x * pv.x + uRot1.x * pv.y + uRot2.x * pv.z,
        uRot0.y * pv.x + uRot1.y * pv.y + uRot2.y * pv.z,
        uRot0.z * pv.x + uRot1.z * pv.y + uRot2.z * pv.z);
}

float2 lonLat(float3 n) {
    return float2(atan(n.x, n.z), asin(clamp(n.y, -1.0, 1.0)));
}

// GLOBE.md 7: equirectangular fetch. Nearest filtering + REPEAT in x makes the antimeridian
// seam exact; the round() absorbs any 8-bit round-trip error in the id.
float idAt(float2 ll) {
    float s = (ll.x / TWO_PI + 0.5) * uTexSize.x;
    float t = (0.5 - ll.y / PI) * uTexSize.y;
    return floor(float(uIndex.eval(float2(s, t)).r) * 255.0 + 0.5);
}

// One neighbour tap: does it belong to another country, or another graticule cell?
float2 tapDiff(float2 frag, float centreId, float2 centreCell, float centreLatDeg) {
    float4 p = viewPoint(frag);
    if (p.w > 1.0) return float2(0.0, 0.0);
    float2 ll = lonLat(toWorld(float3(p.x, p.y, p.z)));
    float border = abs(idAt(ll) - centreId) > 0.5 ? 1.0 : 0.0;
    float grat = 0.0;
    if (uGratStep > 0.0) {
        float2 cell = floor(float2(degrees(ll.x), degrees(ll.y)) / uGratStep);
        // Meridians are suppressed near the poles, where 24 of them converge into a starburst.
        bool meridian = cell.x != centreCell.x && abs(centreLatDeg) < 78.0;
        bool parallel = cell.y != centreCell.y;
        grat = (meridian || parallel) ? 1.0 : 0.0;
    }
    return float2(border, grat);
}

half4 main(float2 frag) {
    float4 p = viewPoint(frag);
    float rho = sqrt(p.w);

    // GLOBE.md 10: the halo lives outside the disc, and the silhouette fades into it so the
    // sphere edge and the atmosphere meet continuously.
    float halo = exp(-max(rho - 1.0, 0.0) / 0.045) * 0.6;
    float3 space = mix(uBackground.rgb, float3(0.20, 0.42, 0.85), clamp(halo, 0.0, 1.0));
    if (p.w > 1.0) {
        return half4(half3(space), 1.0);
    }

    float3 n = toWorld(float3(p.x, p.y, p.z));
    float2 ll = lonLat(n);
    float id = idAt(ll);

    float3 col = uPalette.eval(float2(id + 0.5, 0.5)).rgb;
    if (uSelected >= 0.0 && abs(id - uSelected) < 0.5) {
        col = mix(col, uSelectInk.rgb, 0.55);
    }

    // GLOBE.md 8: borders and graticule from four screen-space taps; the fraction of taps
    // that disagree is a free 4-sample anti-alias.
    float latDeg = degrees(ll.y);
    float2 cell = floor(float2(degrees(ll.x), latDeg) / max(uGratStep, 0.0001));
    float b = uBorderPx;
    float2 acc = tapDiff(frag + float2(b, 0.0), id, cell, latDeg)
               + tapDiff(frag - float2(b, 0.0), id, cell, latDeg)
               + tapDiff(frag + float2(0.0, b), id, cell, latDeg)
               + tapDiff(frag - float2(0.0, b), id, cell, latDeg);
    acc *= 0.25;
    col = mix(col, uBorderInk.rgb, acc.x * uBorders);
    col = mix(col, float3(0.85, 0.92, 1.00), acc.y * 0.22);

    // GLOBE.md 9: on a unit sphere the normal *is* the position, so Lambert is one dot product.
    float day = smoothstep(-0.105, 0.105, dot(n, uSun));
    float lit = mix(1.0, 0.52 + 0.48 * day, uNight);
    col *= lit * (0.62 + 0.38 * pow(max(p.z, 0.0), 0.4));

    if (id < 0.5) {
        // Blinn-Phong sheen on water. The eye direction is exactly (0,0,1) for an
        // orthographic camera, so the half vector is constant over the whole frame.
        float3 lv = float3(dot(uRot0, uSun), dot(uRot1, uSun), dot(uRot2, uSun));
        float3 h = normalize(lv + float3(0.0, 0.0, 1.0));
        float spec = pow(max(dot(float3(p.x, p.y, p.z), h), 0.0), 48.0) * mix(1.0, day, uNight);
        col += float3(0.45, 0.52, 0.60) * spec;
    }

    col += float3(0.22, 0.40, 0.80) * pow(1.0 - p.z, 3.0);          // inner rim (10)
    float aa = clamp((1.0 - rho) * uRadius + 0.5, 0.0, 1.0);        // silhouette AA (4)
    float3 outc = mix(space, col, aa);
    return half4(half3(outc), 1.0);
}
"""

/** Everything the shader needs for one frame. */
class GlobeUniforms(
    val centre: Offset,
    val radiusPx: Float,
    val rotation: GlobeRotation,
    val sun: Vec3,
    val texWidth: Int,
    val texHeight: Int,
    val selectedId: Int,
    val borderPx: Float,
    val graticuleStepDeg: Float,
    val nightStrength: Float,
    val bordersEnabled: Boolean,
    val background: Color,
    val borderInk: Color,
    val selectInk: Color
)

fun RuntimeShader.applyGlobeUniforms(
    u: GlobeUniforms,
    indexShader: BitmapShader,
    paletteShader: BitmapShader
) {
    setFloatUniform("uCentre", u.centre.x, u.centre.y)
    setFloatUniform("uRadius", u.radiusPx)
    setFloatUniform("uRot0", u.rotation.row0.x, u.rotation.row0.y, u.rotation.row0.z)
    setFloatUniform("uRot1", u.rotation.row1.x, u.rotation.row1.y, u.rotation.row1.z)
    setFloatUniform("uRot2", u.rotation.row2.x, u.rotation.row2.y, u.rotation.row2.z)
    setFloatUniform("uSun", u.sun.x, u.sun.y, u.sun.z)
    setFloatUniform("uTexSize", u.texWidth.toFloat(), u.texHeight.toFloat())
    setFloatUniform("uSelected", u.selectedId.toFloat())
    setFloatUniform("uBorderPx", u.borderPx)
    setFloatUniform("uGratStep", u.graticuleStepDeg)
    setFloatUniform("uNight", u.nightStrength)
    setFloatUniform("uBorders", if (u.bordersEnabled) 1f else 0f)
    setColorUniform("uBackground", u.background.toArgb())
    setColorUniform("uBorderInk", u.borderInk.toArgb())
    setColorUniform("uSelectInk", u.selectInk.toArgb())
    // Raw ids must not be colour-managed; real colours must be. Hence the two different APIs.
    setInputBuffer("uIndex", indexShader)
    setInputShader("uPalette", paletteShader)
}
