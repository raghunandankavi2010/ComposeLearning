# Parallax Onboarding — a sky diorama

An onboarding flow built as a **layered sky scene** that walks through one day:
**dawn → noon → sunset → night**. Behind a transparent pager sit several depth
layers — stars, the sun/moon, distant hills, clouds, near hills and birds. Each
layer does **two** things at once:

1. **Parallax** — it scrolls horizontally at its own speed as you swipe, so far
   things barely drift and near things rush past. That speed difference is what
   your eye reads as depth.
2. **Its own animation** — the sun's rays rotate, clouds drift, birds flap and
   fly, stars twinkle, the moon rises. These run continuously, independent of the
   swipe.

The sky color, button and page indicator all interpolate between the adjacent
pages, and a "night factor" cross-fades the sun into the moon.

> Source: [`ParallaxOnboardingScreen.kt`](ParallaxOnboardingScreen.kt)

![scene anatomy](docs/scene.svg)

---

## 1. The master clock: `position`

`HorizontalPager` gives two values:

- `pagerState.currentPage` — the integer page it's settled on / closest to.
- `pagerState.currentPageOffsetFraction` — how far you've dragged, in fractions of a page, roughly `[-0.5, +0.5]`.

Add them for one **continuous** position along the whole flow:

```kotlin
val position = pagerState.currentPage + pagerState.currentPageOffsetFraction
```

![position on a number line](docs/position.svg)

`0.0` = page 0 centered; `1.6` = 60% of the way from page 1 to page 2.
Everything — every layer's scroll, the sky color, the time of day, the indicator
— is a function of this one number.

---

## 2. Scene parallax: `shift = −position × width × speed`

The scene layers are **persistent** (they span the whole flow, not one page), so
each layer is translated by the *absolute* position:

```kotlin
Modifier.graphicsLayer {
    translationX = -position() * size.width * speed
}
```

`speed` is the fraction of a **screen width** a layer scrolls per page. Small
speed → little movement per page → looks far away. Larger speed → moves a lot →
looks close.

| Layer | `speed` | Feel |
|-------|--------:|------|
| Stars | `0.05` | farthest — almost pinned to the sky |
| Sun / Moon | `0.08` | very distant |
| Back hills | `0.15` | distant ridge |
| Clouds | `0.30` | mid-air |
| Front hills | `0.45` | near ridge |
| Birds | `0.60` | closest scene layer — zip past |

![per-layer scroll for one page of swipe](docs/layers.svg)

Plot the shift against `position` and each layer is a straight line whose
**slope is its speed** — same line, different steepness:

![shift vs position](docs/translation-graph.svg)

> **Why `position()` is a lambda.** It's read *inside* the `Canvas` draw block (and
> inside `graphicsLayer`), i.e. at **draw time**, so it is re-sampled every frame
> as you drag — no recomposition needed. Reading it during composition instead
> would freeze the layers between recompositions. (The continuous animations
> below already invalidate the Canvas each frame, so the draw re-runs anyway.)

### Wrapping so the scene never runs out

A layer scrolling left would eventually empty the right side. Each object's x is
wrapped over a span a few screens wide, so as one slides off the left another
appears on the right:

```kotlin
fun wrap(x: Float, span: Float): Float { var v = x % span; if (v < 0) v += span; return v }
```

Hills are instead drawn as one long silhouette that extends well past both edges
(`-0.6w … 2.4w`) and slides as a whole.

---

## 3. The continuous animations

A single [`rememberInfiniteTransition`](https://developer.android.com/develop/ui/compose/animation/value-based)
drives every looping motion. Each value is read **inside the `Canvas` draw lambda**,
so reading them subscribes the *draw* phase — the scene repaints every frame
without recomposing.

| State | Period | Drives |
|-------|-------:|--------|
| `drift` | 60 s | clouds sliding left |
| `fly` | 14 s | birds travelling across |
| `flap` | 0.45 s | birds' wing beat |
| `rayPhase` | 40 s | sun's rays rotating (0→360°) |
| `twinkle` | 2.4 s, reversing | star brightness |

Two helpers turn a 0..1 phase into motion:

```kotlin
fun sinWave(t: Float) = (sin(t * 2 * PI) + 1) / 2          // 0..1 oscillation

// bird wing tips rise & fall:
val lift = s * (0.4f + 0.9f * sinWave(flap + phase))
quadraticBezierTo(center.x - s*0.4f, center.y - lift, center.x, center.y)  // one wing
```

Each element's final x combines **animation + parallax**, e.g. a cloud:

```kotlin
val x = wrap(c.xf*w - drift*1.4f*w - position()*w*CLOUD_SPEED, 2.4f*w) - 0.2f*w
//            base      ↑ drift loop        ↑ swipe parallax
```

---

## 4. Time of day: the `night` factor

One scalar derived from `position` turns the daytime scene into night across the
final page:

```kotlin
val night = (position - 2f).coerceIn(0f, 1f)   // 0 on pages 0–2, ramps to 1 on page 3
```

It cross-fades and gates everything time-dependent:

```kotlin
if (night < 0.99f) drawSun(..., alpha = 1f - night)   // sun fades out
if (night > 0.01f) drawMoon(..., alpha = night)        // moon fades in
val cloudAlpha = 1f - 0.85f * night                    // clouds thin out
val birdAlpha  = 1f - night                            // birds gone at night
// stars only drawn when night > 0, brightness scaled by night
```

The hill colors also darken with `night` via `lerp(dayColor, nightColor, night)`.

---

## 5. Sky & accent color interpolation

The sky is a vertical gradient whose top and bottom colors are interpolated
between the two surrounding pages; the same `lerp`-between-pages drives the button
and indicator accent:

```kotlin
fun colorBetweenPages(position, select): Color {
    val lower = position.toInt(); val upper = (lower + 1).coerceAtMost(lastIndex)
    return lerp(select(pages[lower]), select(pages[upper]), position - lower)
}
val skyTop    = colorBetweenPages(position) { it.skyTop }
val skyBottom = colorBetweenPages(position) { it.skyBottom }
```

`lerp(a, b, t)` blends each channel: `a + (b − a)·t`. So the whole sky shifts
smoothly from dawn indigo/peach → daytime blue → sunset orange → night navy as
you drag.

---

## 6. Foreground (the page content)

The icon, title and subtitle ride the pager itself (so they're effectively the
*closest* layer) plus a small **per-page** parallax for life. This part uses
`pageOffset` (distance of *this page* from center), not the absolute `position`:

```kotlin
val pageOffset = { (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction }

Modifier.graphicsLayer {
    translationX = -pageOffset() * size.width * depth          // depth 0.18 / 0.08 / 0.04
    alpha = (1f - pageOffset().absoluteValue).coerceIn(0f, 1f)  // fade as the page leaves
}
```

`pageOffset` is `0` when the page is centered and `±1` one page away — the icon
(largest depth) drifts a touch more than the text, and each page fades out as it
slides off.

---

## 7. The page indicator

Each dot's **selectedness** is a tent peaking at 1 when `position` lands on it:

```kotlin
val selectedness = (1f - (position - index).absoluteValue).coerceIn(0f, 1f)
val width = lerp(8.dp, 28.dp, selectedness)                            // stretches
val tint  = lerp(Color.White.copy(alpha = .3f), accent, selectedness)  // lights up
```

![selectedness tent function](docs/indicator.svg)

So the active "pill" flows between dots instead of snapping.

---

## TL;DR

```
position   = currentPage + currentPageOffsetFraction      // master clock
sceneShift = -position * screenWidth * speed              // parallax (slope = speed)
night      = clamp(position - 2, 0, 1)                    // day → night cross-fade
x_element  = wrap(base - drift*w - position*w*speed, …)   // animation + parallax
skyColor   = lerp(pageA, pageB, fractional(position))     // continuous recolor
selected   = clamp(1 - |position - i|, 0, 1)              // indicator tent
```

One continuous `position` scrolls every layer at its own speed, advances the
clock from dawn to night, and recolors the sky — while an infinite transition
keeps the sun, clouds, birds and stars alive on top.
