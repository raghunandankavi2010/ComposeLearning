# Bottle Wave Animation — The Maths

This document explains every piece of mathematics used in
[`BottleWaveAnimation.kt`](BottleWaveAnimation.kt): the travelling sine wave,
wave superposition, the phase loop, the bottle geometry (quadratic Bézier
curves), fill-level mapping, and the polyline sampling of the wave surface.

---

## 1. The core idea

The "water" is a closed `Path`: a rectangle whose **top edge is a travelling
sine wave**. That path is filled with a vertical gradient and clipped to the
bottle shape, so the water can only ever appear inside the bottle.

```
        clip(bottlePath) {
            fill(waterPath)        // rectangle with a wavy top
        }
```

Everything below is about computing that wavy top edge and the bottle outline.

---

## 2. The travelling sine wave

### 2.1 General form

A travelling wave along the x-axis is:

```
y(x, t) = A · sin(k·x + φ(t))
```

| Symbol | Meaning                            | In the code                |
|--------|------------------------------------|----------------------------|
| `A`    | amplitude (wave height, px)        | `waveAmplitude`            |
| `k`    | angular wavenumber (radians / px)  | `waveFrequency / 100f`     |
| `φ(t)` | phase offset, advances with time   | `waveOffset`               |
| `x`    | horizontal position (px)           | sample point on the canvas |

In the code:

```kotlin
val wave1 = sin((x * waveFrequency / 100f) + waveOffset) * waveAmplitude
```

### 2.2 Why divide by 100?

`sin` works in **radians**, but `x` is in **pixels** (hundreds to thousands).
Without scaling, `sin(x)` would oscillate every `2π ≈ 6.28` px — far too fast
to see as a wave. Dividing by 100 turns the slider value into a usable
wavenumber:

```
k = waveFrequency / 100        (radians per pixel)
wavelength λ = 2π / k = 200π / waveFrequency  ≈ 628 / waveFrequency px
```

So with `waveFrequency = 3`, one full wave is ≈ 209 px wide — roughly the
bottle width, i.e. the slider value approximates "number of wave crests
visible across the screen region".

### 2.3 Making the wave *move* — the phase loop

The wave travels because `φ(t)` grows over time. The infinite transition
animates it from `0` to `2π` linearly over 2000 ms and repeats:

```kotlin
initialValue = 0f
targetValue  = 2f * PI.toFloat()
```

**Why exactly `2π`?** Sine is periodic with period `2π`:

```
sin(θ + 2π) = sin(θ)
```

So the frame at `φ = 2π` is pixel-identical to the frame at `φ = 0`, and the
`infiniteRepeatable` restart is **seamless** — no visible jump. Any other
target value would cause a snap at the loop boundary.

The wave's horizontal speed is the phase speed:

```
v = (dφ/dt) / k = (2π / 2s) / k = π/k px per second
```

Increasing `waveFrequency` (bigger `k`) therefore makes crests *narrower and
slower-moving*; the foam line keeps the same temporal rhythm.

---

## 3. Superposition — why two waves?

A single sine looks mechanical. Real water surfaces are a sum of waves of
different frequencies (Fourier's idea in miniature). The code superimposes a
second harmonic-ish wave:

```kotlin
val wave2 = sin((x * waveFrequency * 1.5f / 100f) + waveOffset * 1.5f) * (waveAmplitude * 0.4f)
val waveY = waterSurfaceY + wave1 + wave2
```

Mathematically:

```
y(x, t) = A·sin(k·x + φ)  +  0.4A·sin(1.5k·x + 1.5φ)
```

Key properties of this choice:

- **1.5× frequency, 0.4× amplitude** — the second wave adds small ripples on
  top of the main swell instead of doubling its height.
- **1.5× the phase too** — both terms can be written as functions of
  `(k·x + φ)` alone (the second term is `0.4A·sin(1.5·(k·x + φ))`), so the
  combined shape still **repeats exactly when φ advances by 2π**… *almost*:
  `sin(1.5θ)` has period `4π/1.5·… ` — strictly, `sin(1.5θ)` repeats every
  `4π/3` in θ, and `2π` is not an integer multiple of that. In practice the
  combined surface at `φ = 2π` equals `sin(k·x) + 0.4·sin(1.5k·x + 3π)` =
  `sin(k·x) − 0.4·sin(1.5k·x)` — a mirrored ripple, which reads as continuous
  motion because the ripple component is small. (Using a 2× multiplier instead
  of 1.5× would make the loop mathematically perfect: `sin(2(θ+2π)) = sin(2θ)`.)
- **Worst-case surface excursion** is bounded by `|A| + |0.4A| = 1.4A`, which
  matters for how much headroom the water needs near a full bottle.

---

## 4. Mapping fill level to a water line

The fill level `f ∈ [0, 1]` maps linearly to a y-coordinate. Remember that on
a Canvas **y grows downward**, so "more water" means a *smaller* y:

```kotlin
val totalHeight   = bottomY - topY            // bottle height H
val waterHeight   = totalHeight * f           // h = H·f
val waterSurfaceY = bottomY - waterHeight     // y_surface = y_bottom − H·f
```

As a single affine map:

```
y_surface(f) = y_bottom − f · (y_bottom − y_top)     = lerp(y_bottom, y_top, f)
```

- `f = 0` → surface sits at the bottle bottom (no water visible)
- `f = 1` → surface at the bottle top (waves clipped by the neck)

The auto-fill animation simply steps `f` by `+0.01` every 50 ms, i.e. a fill
rate of `0.2 / second` → 5 seconds from empty to full.

> Note: this is *height*-linear, not *volume*-linear. Because the bottle's
> cross-section narrows at the shoulder/neck, equal `Δf` steps near the top
> represent less actual volume. The dashed "ml" markers are also placed
> height-linearly along the straight body only (`y = bottomY − bodyHeight·ratio`),
> which is correct for the cylindrical body section.

---

## 5. Sampling the wave — from continuous maths to a Path

`Path` has no `sineTo()`, so the continuous curve is approximated by a
**polyline of 41 points** (`steps = 40` segments):

```kotlin
for (i in steps downTo 0) {
    val x = (startX - overfill) + (endX - startX + 2 * overfill) * (i.toFloat() / steps)
    ...
    waterPath.lineTo(x, waveY)
}
```

This is uniform sampling of `x` over the (widened) interval:

```
xᵢ = x_left + (x_right − x_left) · i/N ,   i = 0 … N,  N = 40
```

Why 40 is enough: the Nyquist-style rule of thumb for a visually smooth
polyline is ~10+ segments per wavelength. At the slider max
(`waveFrequency = 8`, λ ≈ 78 px) a ~350 px wide sampling region contains
about 4.5 wavelengths → ~9 segments per wavelength, right at the edge but
still smooth at typical screen densities. Doubling `steps` would cost almost
nothing if higher frequencies were ever needed.

The loop runs `downTo 0` because the path is being traced **clockwise**:
bottom-left → bottom-right → up the right edge → *right-to-left along the
wave* → `close()` back down the left edge. A consistent winding direction
gives a simple, non-self-intersecting polygon that fills correctly.

### 5.1 The `overfill` margin

```kotlin
val overfill = 100f
```

The water rectangle is built 100 px wider and deeper than the bottle on each
side. Two reasons:

1. The wave displaces the surface by up to `±1.4A` (see §3); the geometry must
   extend past the bottle walls so the clip never exposes a gap at the edges.
2. The bottle's bottom corners are rounded (quadratic curves, §6) — the water
   must extend below `bottomY` so the clip, not the water path, defines the
   corner shape.

The clip makes the extra geometry free: only the intersection with the bottle
path is rasterised.

---

## 6. Bottle geometry — quadratic Bézier curves

The bottle outline is straight lines plus four **quadratic Bézier** segments
(two bottom corners, two shoulders). A quadratic Bézier from `P₀` to `P₂`
with control point `P₁` is:

```
B(t) = (1−t)²·P₀ + 2t(1−t)·P₁ + t²·P₂ ,   t ∈ [0, 1]
```

Useful property used here: the curve **starts tangent to the line P₀→P₁ and
ends tangent to P₁→P₂**. Placing the control point at the *corner* of the two
straight edges being joined produces a smooth fillet:

### 6.1 Bottom corners

```kotlin
moveTo(startX + 20f, bottomY)
quadraticTo(startX, bottomY, startX, bottomY - 20f)
```

`P₀ = (startX+20, bottomY)`, `P₁ = (startX, bottomY)` (the sharp corner),
`P₂ = (startX, bottomY−20)`. The result is a rounded corner with ~20 px
radius, tangent to both the bottom edge and the left wall.

### 6.2 Shoulders

```kotlin
quadraticTo(
    startX,               // P₁: where the wall would meet the neck line
    topY + neckHeight,
    center.x - neckWidth / 2,   // P₂: base of the neck
    topY + neckHeight
)
```

`P₀` is the top of the straight body wall, `P₁ = (startX, topY+neckHeight)`
is the imaginary square corner, `P₂` is the neck base. Tangency at `P₀` keeps
the wall vertical into the curve; tangency at `P₂` lands horizontally at the
neck — a smooth shoulder with no kink.

### 6.3 Proportions

All dimensions are ratios of the canvas, so the bottle scales with any screen:

```
bottleWidth    = 0.35 · canvasWidth
bottleHeight   = 0.70 · canvasHeight
neckWidth      = 0.45 · bottleWidth
neckHeight     = 0.20 · bottleHeight
shoulderHeight = 0.10 · bottleHeight
bodyHeight     = bottleHeight − neckHeight − shoulderHeight   // = 0.70 · bottleHeight
```

And the bounding coordinates, centred on the canvas:

```
startX  = cx − bottleWidth/2        endX = cx + bottleWidth/2
bottomY = cy + bottleHeight/2       topY = bottomY − bottleHeight
```

---

## 7. Clipping — constructive solid geometry, cheaply

```kotlin
clipPath(bottlePath) { drawWater(...) }
```

Drawing clipped to a path is the rasteriser computing, per pixel, the set
**intersection**:

```
visibleWater = waterPath ∩ bottlePath
```

This is why neither the wave nor the rectangle ever needs to know about the
bottle's curved walls — the intersection handles the shoulder taper, neck and
rounded corners automatically, at any fill level.

---

## 8. The gradient

```kotlin
Brush.verticalGradient(
    colors = listOf(lightBlue, deepBlue),
    startY = waterSurfaceY,
    endY   = bottomY
)
```

A vertical gradient is a linear interpolation in colour space along y:

```
t(y)      = clamp((y − waterSurfaceY) / (bottomY − waterSurfaceY), 0, 1)
color(y)  = lerp(lightBlue, deepBlue, t(y))
```

Anchoring `startY` at the (mean) water surface rather than the canvas top
means the light-to-deep transition always spans exactly the visible water
column — at 10% fill the gradient compresses, at 100% it stretches, instead
of the water just sampling a slice of a fixed gradient.

The foam line is the same sampled wave polyline (§5) stroked instead of
filled, traced left-to-right (`0..steps`) since direction doesn't matter for
a stroke.

---

## 9. Quick reference

| Effect                | Formula                                                       |
|-----------------------|---------------------------------------------------------------|
| Main wave             | `y = A·sin(kx + φ)` with `k = freq/100`                       |
| Ripple (2nd wave)     | `+ 0.4A·sin(1.5kx + 1.5φ)`                                    |
| Seamless loop         | animate `φ: 0 → 2π`, because `sin(θ+2π) = sin(θ)`             |
| Wavelength            | `λ = 2π/k ≈ 628/freq` px                                      |
| Water line            | `y_surface = y_bottom − f·H` (y is inverted on Canvas)        |
| Wave sampling         | 41 uniform x-samples, polyline `lineTo`                       |
| Rounded corner        | quadratic Bézier, control point at the square corner          |
| Shoulder curve        | quadratic Bézier, tangent to wall and neck base               |
| Water-in-bottle       | per-pixel intersection via `clipPath`                         |
| Gradient              | `color(y) = lerp(c₁, c₂, (y − y_surface)/(y_bottom − y_surface))` |
