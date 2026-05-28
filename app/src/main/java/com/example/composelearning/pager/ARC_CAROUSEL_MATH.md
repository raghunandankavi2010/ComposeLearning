# The Math Behind UI Effects — Notes from the Arc Carousel

The arc carousel is the running example. The principles generalise to *every* scroll-driven visual effect: parallax, cover-flow, fan stacks, wheel pickers, depth blur, snap menus, anything where a widget reacts to where it sits relative to the viewport.

> **Mental model.** A UI effect is a pure function: `(scroll position) → (visual property)`. Your job is (1) define an input axis that doesn't depend on screen size, (2) pick a curve that maps that axis to whatever property you're animating (Y offset, scale, alpha, rotation…). Everything else is plumbing.

---

## 1. Define a normalised axis

Pixel coordinates are useless on their own — they depend on density, screen width, fling state. The standard trick is to compress everything to `[-1, +1]`, where `0` means "centred in the viewport" and `±1` means "at the viewport edge".

Compose hands you these every frame via `LazyListState.layoutInfo`:

| Field | Meaning |
|---|---|
| `viewportStartOffset` | Left edge of the visible window, in px (usually 0). |
| `viewportEndOffset` | Right edge of the visible window, in px. |
| `visibleItemsInfo[i].offset` | Left edge of item *i*, in px. |
| `visibleItemsInfo[i].size` | Width of item *i*, in px. |

Compose them:

```
halfViewport   = (viewportEndOffset - viewportStartOffset) / 2
viewportCenter = viewportStartOffset + halfViewport
itemCenter     = itemInfo.offset + itemInfo.size / 2

normalised     = (itemCenter - viewportCenter) / halfViewport   // ∈ [-1, +1]
```

`.coerceIn(-1f, 1f)` clamps the rare overshoot during fling. This `normalised` is the **one variable** every downstream curve consumes.

```
              viewport
   ┌─────────────────────────────────────┐
   │     ╔══╗            ╔══╗            │
   │     ║A ║            ║B ║            │
   │     ╚══╝            ╚══╝            │
   └─────────────────────────────────────┘
          ↑                ↑          ↑
   itemCenter(A)    viewportCenter   viewportEnd
   normalised(A) = -0.5        normalised(B) = +0.1
```

---

## 2. Pick a curve

`normalised` is your X axis. Now pick a function `y = f(x)` that maps it to whatever you're animating. Scale `y` by `arcDepth` (or `maxScale - 1`, or `maxRotation`, …).

### 2a. Parabola — `y = x²`
```
y = x² × arcDepth
```
- `x=0` → `y=0`. `x=±1` → `y=arcDepth`. Smooth, symmetric, cheap.
- **Visual shape**: dome (centre stays high, edges drop).
- Default choice. Good for arc carousels, parallax depth, scale-down-at-edges effects.

### 2b. Inverted parabola — `y = 1 - x²`
- Bowl (centre dips, edges rise).
- Or, the "proximity" function used for scale/alpha (1 at centre, 0 at edges).

### 2c. Circle — `y = 1 - √(1 - x²)`
```
y = (1 - √(1 - x²)) × arcDepth
```
- Exact geometric arc (parabola is the cheap approximation).
- Difference vs parabola is only visible at large `arcDepth` — at small depths they're indistinguishable. Stick with parabola unless you specifically want that round-corner profile.

### 2d. Cosine — `y = 1 - cos(x · π/2)`
- Same endpoints (0 at centre, `arcDepth` at edges).
- **Flatter near centre, steeper near edges.** Use when you want a wide "dead zone" of unchanged items around the snapped one.

### 2e. Sine — `y = sin(|x| · π/2)`
- Mirror of cosine: **steeper near centre, flatter near edges.**

### Curve gallery (x: −1 → +1, y: 0 → 1)

```
parabola x²              cosine (1-cos)            sine (sin)
1┤    /‾\               1┤  /‾‾\                  1┤    /‾‾\__
 │   /   \                │ /    \                  │   /
 │  /     \               │/      \                 │  /
0┼─/───────\─           0┼/────────\─             0┼─/────────
 │/         \            │          \              │/
-1   0    +1            -1   0    +1              -1   0    +1
```

Pick by feel: *how much should the effect "kick in" near the snapped item?* Sine kicks early, cosine kicks late, parabola is in the middle.

---

## 3. Proximity and easing

When you want centre-amplified properties (scale up at centre, fade in at centre, glow at centre), use:

```
t = 1 - |x|              // ∈ [0, 1], 1 at centre, 0 at edges
property = lerp(idle, peak, t)
```

That's a linear interpolation. To make the peak feel sharper (property stays near `idle` until you're really close to centre, then ramps fast):

```
t = (1 - |x|)²    or    pow(1 - |x|, 3)
```

`pow(t, n)` is your easing knob. Higher `n` = later kick-in, more dramatic snap. Compose has `EaseOutQuart`, `EaseInOutCubic`, etc. in `androidx.compose.animation.core` if you want named curves — but writing `t * t` is fine and avoids an import.

```
t   = 1 - |x|       smooth lerp (linear ramp)
t²  = (1 - |x|)²    "kicks in" near centre
t³  = (1 - |x|)³    aggressive snap
```

---

## 4. graphicsLayer — translation, scale, rotation, pivot

`Modifier.graphicsLayer { ... }` is your transform sink. The lambda re-runs **every frame** when state inside it changes — no recomposition, just a GPU update. That's why scroll-driven animations are essentially free.

```kotlin
.graphicsLayer {
    translationX = ...        // px right (negative = left)
    translationY = ...        // px down  (negative = up)
    scaleX = ...; scaleY = ...
    rotationZ = ...           // degrees clockwise
    alpha = ...               // 0..1
    transformOrigin = TransformOrigin(px = 0.5f, py = 0.5f)
}
```

### Pivot (transformOrigin) — easy to miss
Scale and rotation happen *around* a point. The default is centre (`0.5f, 0.5f`). If you set `transformOrigin = TransformOrigin(0.5f, 1f)`, the pivot is the bottom-middle of the element — so scaling makes it grow *upward* instead of outward.

This matters when you want growth to look "anchored" — e.g. a bubble growing upward from a label baseline.

### Coordinate sign reminder
Android UI Y axis points **down**. `translationY = +10f` moves the element 10 px **down**. To lift, use negative.

---

## 5. Compose data plumbing — the three layers

Three different mechanisms, three different costs:

| Mechanism | Re-runs when… | Cost | Use for |
|---|---|---|---|
| Composition (`@Composable` body) | State the function *reads* changes | High — rebuild UI tree | Discrete state changes (which screen, which tab) |
| `derivedStateOf { … }` | Result value changes (not inputs) | Medium — recomposes readers | "Which item is active" — flips rarely even though scroll is constant |
| `graphicsLayer { … }` lambda | State inside it changes; no recomposition | Low — GPU update | Continuous per-frame values (translation, scale) |

The rule: **discrete things go through `derivedStateOf`, continuous things go through `graphicsLayer`.** Mixing them up causes either bad performance (recomposing 60 times per second) or stale frames (graphicsLayer reading stale composition values).

In the carousel:
- `activeIndex` is discrete → `derivedStateOf`. Recomposes once when the snapped item flips.
- `translationY` is continuous → `graphicsLayer`. Re-evaluates every frame during fling.

---

## 6. Side padding — letting the ends reach centre

A `LazyRow` puts item 0 at `viewportStartOffset = 0` by default. The user can drag right, but item 0's *centre* will only ever reach `itemSize/2`, never the viewport centre. Fix:

```
sidePadding = (viewportWidth - itemSize) / 2
contentPadding = PaddingValues(horizontal = sidePadding)
```

Now item 0 starts at `viewportCenter - itemSize/2` — its centre lines up with viewport centre. Same on the right end.

`BoxWithConstraints` is how you read `viewportWidth` (as a `Dp`) before the row lays out.

---

## 7. Snap fling — how items always end up centred

`rememberSnapFlingBehavior(state, SnapPosition.Center)` replaces the default fling. The behaviour:

1. User lifts finger with some velocity `v` (px/s).
2. The fling animator decelerates `v` using a friction curve (Compose default: roughly exponential decay).
3. As `v` approaches 0, the animator computes where each item *would* stop and adjusts the deceleration so the nearest item lands exactly at `SnapPosition.Center` (= viewport centre).

The "nearest item" check uses the same midpoint math as your `activeIndex`: distance between item centre and viewport centre. The snap position **must match** your carousel's "centre" definition — otherwise your arc apex won't align with the snapped item.

Friction-based deceleration is itself math: `v(t) = v₀ · e^(-k·t)`, and position is the integral, `x(t) = (v₀/k) · (1 - e^(-k·t))`. Compose exposes the constant `k` as `flingDecay = exponentialDecay(...)`. You rarely need to touch it — but if your scrolls "feel slippery", that's the knob.

---

## 8. Touch → pixels → state — the input pipeline

You asked specifically about "converting velocity into pixels moving touch drag". The pipeline is:

```
finger moves Δpx → MotionEvent.ACTION_MOVE
        ↓
Compose's pointer input modifier reports a PointerInputChange
        ↓
The scroll/drag modifier calls `state.scrollBy(Δpx)` or `state.dispatchRawDelta(Δpx)`
        ↓
LazyListState recalculates which items are visible and at what offset
        ↓
graphicsLayer lambdas re-read state.layoutInfo on next frame
```

Two units to keep straight:

- **px** — what `PointerInputChange.position` and `layoutInfo` give you.
- **dp** — what `Modifier.size(64.dp)` accepts. Convert with `LocalDensity.current.density` (`px = dp × density`) or `with(LocalDensity.current) { 64.dp.toPx() }`.

### Velocity tracking
For a fling, Compose tracks pointer history and fits a velocity (`px/s`) to the recent motion. The `VelocityTracker` class does this; the algorithm is essentially a weighted least-squares fit over the last few touch samples. You get the result as a `Velocity` value (px/s in both axes), which is what the fling animator uses as its `v₀`.

You can read it yourself with `androidx.compose.ui.input.pointer.util.VelocityTracker` if you're building a custom gesture.

### Drag → translate (simplest possible)
The most direct case is `Modifier.draggable` or `Modifier.pointerInput` with `detectDragGestures`:

```kotlin
var offset by remember { mutableStateOf(0f) }
Box(
    Modifier
        .offset { IntOffset(offset.toInt(), 0) }
        .pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
                offset += dragAmount.x   // dragAmount is px since last sample
            }
        }
)
```

`dragAmount.x` is already in pixels. No conversion needed.

---

## 9. Trigonometry shows up when you go 2D or radial

Linear stuff (rows, columns, parallax) needs only polynomials. The moment you draw on a `Canvas` with circular layouts (radial menus, dials, knobs, wheel pickers), trig appears:

```
For a point at angle θ on a circle of radius r centred at (cx, cy):
    x = cx + r · cos(θ)
    y = cy + r · sin(θ)

For N items evenly spaced around a circle:
    θᵢ = (2π · i / N) - π/2     // - π/2 puts item 0 at the top instead of right
```

Angles in Compose are **radians for `cos`/`sin`**, **degrees for `rotationZ`**. The conversion is `radians = degrees × π / 180`.

For a knob/dial where the user drags a thumb around an arc:
```
1. Get finger position (px, py)
2. dx = px - cx,  dy = py - cy
3. θ = atan2(dy, dx)            // radians in [-π, +π]
4. Clamp θ to the dial's valid range
5. value = lerp(min, max, (θ - θ_start) / (θ_end - θ_start))
```

`atan2(y, x)` is your friend — it gives the angle from the positive X axis to the point `(x, y)`, handling all four quadrants correctly. (Don't use `atan(y/x)` — it loses sign info.)

---

## 10. Generalising — same idea, different axis

The arc carousel uses horizontal scroll as its axis. Replace the axis, get a different effect:

| Layout | "Position along axis" | Use for |
|---|---|---|
| Horizontal carousel | `itemCenter.x - viewportCenter.x` | arc, fan, depth blur |
| Vertical list (LazyColumn) | `itemCenter.y - viewportCenter.y` | wheel pickers, parallax bg |
| `HorizontalPager` | `pagerState.getOffsetFractionForPage(page)` — already normalised to ±1 | cover-flow, page tilt |
| 2D grid | `√(dx² + dy²)` from grid centre | radial reveal, magnification |
| Touch/drag | `pointerOffset - elementCenter` | drag-to-snap, swipe-to-dismiss |

For `HorizontalPager` specifically, you don't even need `layoutInfo` — `getOffsetFractionForPage(page)` returns the normalised position directly. Same curves apply.

---

## 11. The full per-frame pipeline for the arc carousel

For each visible item, every frame during scroll:

```
1. itemInfo = layoutInfo.visibleItemsInfo.find { it.index == i }
2. halfViewport   = (viewportEnd - viewportStart) / 2
3. viewportCenter = viewportStart + halfViewport
4. itemCenter     = itemInfo.offset + itemInfo.size / 2
5. x = ((itemCenter - viewportCenter) / halfViewport).coerceIn(-1f, 1f)
6. translationY   = f(x) × arcDepthPx       — arc curve
7. (optional) scale = g(x) × maxScale       — growth curve
8. (optional) alpha = h(x)                  — fade curve
9. transformOrigin = (0.5, 0.5)             — or (0.5, 1) for bottom-anchored scale
```

That's the whole engine. Step 5 is the *only* universal part. Steps 6–8 are independent and composable — pick any curve for each, in any combination.

---

## 12. Tuning checklist — what to change when it looks wrong

| Symptom | Fix |
|---|---|
| Arc too steep | Lower `arcDepth`. Try `itemSize × 0.3` first. |
| Arc too flat | Raise `arcDepth`, or swap parabola → cosine for steeper edges. |
| Items overlap when scaled | Increase spacing, or factor `centerScale` into spacing budget. |
| Snap stops between items | `SnapPosition.Center` must use the same "centre" definition as your normalised axis. |
| Edge items get clipped vertically | `containerHeight = itemSize × centerScale + labelHeight + arcDepth + breathingRoom`. |
| Centre item label is far from bubble | Apply the `graphicsLayer` transform to the whole column (bubble + label move together). |
| Centre item label overlaps bubble | Apply the transform only to the bubble; reserve fixed space for the label below. |
| Growth feels abrupt | Square or cube the `t` in step 7. |
| Animation is laggy | You're probably reading scroll state during *composition*, not in `graphicsLayer`. Move it. |

---

## 13. Where to look in the source

- [`ArcCarouselScreen.kt`](ArcCarouselScreen.kt) — full implementation. The `graphicsLayer` block in `ArcItemView` is the entire animation engine; everything else is layout.

## 14. Things worth reading next

- `androidx.compose.animation.core.Easing` — named easing functions (`EaseOutQuart`, `FastOutSlowInEasing`, …). All of them are just `(Float) → Float`.
- `androidx.compose.ui.input.pointer.util.VelocityTracker` — the actual velocity-from-touch implementation.
- `androidx.compose.foundation.gestures.snapping.SnapFlingBehavior` — the snap math, including how it computes the target item from the current velocity.
- `androidx.compose.foundation.pager.PagerState.getOffsetFractionForPage` — the same normalised axis, but pre-computed for pagers.
- `androidx.compose.ui.graphics.TransformOrigin` — for understanding the pivot point.

## TL;DR

1. **Compress your input** to `[-1, +1]` so curves don't care about screen size.
2. **Pick a curve** (parabola is almost always right) and scale its output by your effect's amplitude.
3. **Do continuous animation in `graphicsLayer`**, discrete state changes in `derivedStateOf`, composition for screens.
4. **Pivot matters** — `TransformOrigin` decides where scale/rotation happen *from*.
5. **2D? Radial?** — `atan2` for angle, `cos`/`sin` for placement, `√(dx² + dy²)` for distance.
6. **Touch → state → graphicsLayer** is the input pipeline. Velocity is `px/s` and the fling animator does exponential decay on it.

Everything else is layout, plumbing, and taste.
