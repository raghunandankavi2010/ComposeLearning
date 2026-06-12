# Wave Loading Circle — Math & Recomposition Notes

This document covers the [CircleLoader.kt](CircleLoader.kt) wave-fill loader: the geometry/math behind the drawing, why the original implementation recomposed on every animation frame, and how deferring state reads to the draw phase fixed it.

---

## 1. The math behind the drawing

The loader is a circle that "fills up" with liquid. The liquid surface is a travelling sine wave whose height rises with progress and whose amplitude fades out near empty and full.

### 1.1 The two animated inputs

| Input | Range | Period | Role |
|---|---|---|---|
| `fillProgress` | `0f → 1f` | 6000 ms | Vertical fill level of the liquid |
| `wavePhase` | `0f → 2π` | 1200 ms | Horizontal travel of the wave crests |

Both are driven by a single `rememberInfiniteTransition` with `LinearEasing`, so they advance at a constant rate. Because `wavePhase` sweeps exactly one full period (`2π`) per cycle, the wave loops seamlessly — the end state of one cycle is bit-identical to the start of the next.

### 1.2 Baseline (fill level)

The canvas is `w × h` pixels with the origin at the **top-left** (y grows downward). The resting surface of the liquid sits at:

```
baselineY = h − (fillProgress · h) = h · (1 − fillProgress)
```

- `fillProgress = 0` → `baselineY = h` (surface at the bottom, circle empty)
- `fillProgress = 1` → `baselineY = 0` (surface at the top, circle full)

### 1.3 Amplitude dampening

A raw sine wave looks wrong at the extremes: at 0% the wave would poke up out of nothing, and at 100% troughs would leave unfilled gaps at the top. The amplitude is therefore modulated by a half-sine envelope over the progress:

```
A(progress) = A_max · sin(progress · π)        where A_max = 0.05 · h
```

Properties of this envelope:

- `A(0) = sin(0) = 0` — flat surface when empty
- `A(0.5) = sin(π/2) = 1` — full 5%-of-height amplitude at half full
- `A(1) = sin(π) = 0` — flat surface when full, so the circle fills cleanly

So the wave "breathes in" as filling starts and "flattens out" as it completes, with the maximum sloshing exactly at the midpoint.

### 1.4 The wave surface

The surface is a sine curve sampled across the width. With `waveCount = 1` crest across the canvas, the angular frequency is:

```
ω = 2π · waveCount / w
```

so that as `x` goes from `0` to `w`, the argument `ωx` sweeps exactly `waveCount` full periods. Each surface point is:

```
y(x) = baselineY + A(progress) · sin(ω·x + wavePhase)
```

`wavePhase` is a phase offset: increasing it shifts the whole sine curve horizontally, which reads as the wave **travelling sideways**. Since `wavePhase` animates `0 → 2π` linearly, the crests move at constant speed and wrap perfectly.

The curve is plotted with straight `lineTo` segments every `stepPx = 2f` pixels — at 2 px resolution the polyline is visually indistinguishable from a true sine curve.

### 1.5 Closing the path and clipping

The sampled curve alone is just a line. To make it a solid body of liquid, the path is closed underneath it:

```
moveTo(0, h)            // bottom-left corner
lineTo(x, y(x)) …       // the sine surface, left → right
lineTo(w, h)            // down to bottom-right corner
close()                 // back to bottom-left
```

This produces a filled region bounded by the wavy top edge and the bottom of the canvas. Finally, the whole thing is clipped to a circle:

```
circlePath = oval inscribed in (0, 0, w, h)
clipPath(circlePath) { drawPath(wavePath, color) }
```

Only the intersection of the liquid block and the circle is painted, which gives the "liquid inside a round container" look. The `border(2.dp, CircleShape)` on the modifier draws the container outline.

---

## 2. The recomposition problem (before)

### 2.1 Original code

```kotlin
@Composable
fun WaveLoadingCircle(
    fillProgress: Float,   // ← raw value read during composition
    wavePhase: Float,      // ← raw value read during composition
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val baselineY = h - (fillProgress * h)
        // ... uses fillProgress and wavePhase to build the path ...
    }
}

@Composable
fun WaveLoadingCircleScreen(modifier: Modifier = Modifier) {
    val wavePhase by infiniteTransition.animateFloat(/* 0 → 2π, 1200 ms */)
    val fillProgress by infiniteTransition.animateFloat(/* 0 → 1, 6000 ms */)

    Box(...) {
        Column(...) {
            WaveLoadingCircle(
                fillProgress = fillProgress,   // ← state read HERE, in Screen's scope
                wavePhase = wavePhase,         // ← state read HERE, in Screen's scope
                ...
            )
            Text(text = "${(fillProgress * 100).toInt()}%", ...)  // ← and HERE
        }
    }
}
```

### 2.2 Why this recomposes every frame

Compose renders a frame in three phases:

```
Composition  →  Layout  →  Draw
(what to show)  (where)     (pixels)
```

Snapshot state (which is what `animateFloat` returns) tracks **where it is read**. Whichever phase reads the state is the phase that gets invalidated when the state changes.

In the original code, `fillProgress` and `wavePhase` were read **in the composition phase**, twice over:

1. **In `WaveLoadingCircleScreen`'s body** — the `by` delegate evaluates `wavePhase`/`fillProgress` at the point they're passed as arguments. That read happens while the Screen composable is executing, so the Screen's recompose scope subscribes to both states.
2. **As plain `Float` parameters of `WaveLoadingCircle`** — once unwrapped to a raw `Float`, the value is fixed at composition time. The only way the `Canvas` can ever see a new value is for `WaveLoadingCircle` to be **recomposed with new arguments**.

The animation ticks `wavePhase` on every frame of the choreographer — 60 to 120 times per second. Each tick therefore caused, **per frame**:

- recomposition of `WaveLoadingCircleScreen` (it read the state),
- recomposition of `WaveLoadingCircle` (its `Float` parameter changed),
- re-execution of `Box`/`Column` content lambdas in that scope,
- recomposition of `Text` (its `String` argument is derived from `fillProgress`),
- and only **then** the draw invalidation that was actually needed.

All of that work existed solely to ferry two floats into a draw lambda. Nothing about the UI *structure* changes between frames — the tree, sizes, and positions are identical; only pixels differ. The ideal frame cost is **draw-only**.

A secondary issue: the `Text` showed an integer percent (`0…100`), but it was fed by a state that changes every frame. It recomposed ~60–120×/s even though its visible output changes only ~17×/s (100 distinct values over a 6 s cycle).

---

## 3. The fix: defer state reads to the draw phase (after)

### 3.1 Lambda parameters instead of raw values

```kotlin
@Composable
fun WaveLoadingCircle(
    fillProgress: () -> Float,   // ← lambda: no state read at composition time
    wavePhase: () -> Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        // State is read here, in the DRAW phase
        val progress = fillProgress()
        val phase = wavePhase()
        // ... build and draw the path ...
    }
}

// Call site:
WaveLoadingCircle(
    fillProgress = { fillProgress },   // lambda capture — not a read
    wavePhase = { wavePhase },
    ...
)
```

The key insight: `{ fillProgress }` does **not** read the state when the Screen composes — it merely creates a lambda that *will* read it later. The actual snapshot read now happens inside the `Canvas` draw lambda, i.e. during the **draw phase**.

Consequences per animation frame:

- The Screen's composition scope no longer subscribes to `wavePhase`/`fillProgress` → **zero recompositions**.
- `WaveLoadingCircle`'s parameters (two stable lambdas) never change → it is **skipped** entirely.
- The snapshot system sees the state was read during draw, so a state change invalidates **only the draw pass** of that one `Canvas` node. Layout isn't touched either, since size never changes.

Per-frame work drops from *recompose Screen + Circle + Text → layout → draw* down to just *redraw one node* — which is the minimum possible for an animation that only changes pixels.

### 3.2 Scoping the percent text with `derivedStateOf`

The `Text` is the one piece that legitimately needs recomposition (its `String` changes), but it should recompose (a) in its own small scope, not the Screen's, and (b) only when the *displayed* value changes:

```kotlin
@Composable
private fun ProgressPercentText(fillProgress: () -> Float) {
    val percent by remember { derivedStateOf { (fillProgress() * 100).toInt() } }
    Text(text = "$percent%", ...)
}
```

Two mechanisms at work:

- **Extraction into a child composable** moves the state read out of the Screen's recompose scope. When the value changes, only `ProgressPercentText` re-executes.
- **`derivedStateOf`** inserts a change filter: the underlying float changes every frame, but the derived `Int` only changes when the truncated percent crosses a boundary. Compose compares the derived value and only invalidates readers when it actually differs.

Recomposition rate of the text drops from the frame rate (~60–120/s) to the rate of visible change (100 recompositions per 6 s cycle ≈ **17/s**), and each one is scoped to a single small composable.

### 3.3 Summary of per-frame cost

| | Before | After |
|---|---|---|
| `WaveLoadingCircleScreen` recomposes | every frame | never |
| `WaveLoadingCircle` recomposes | every frame | never (skipped) |
| `Text` recomposes | every frame | only on whole-percent change, in its own scope |
| Layout pass | every frame | never |
| Draw pass | every frame | every frame (unavoidable — and all that's needed) |

### 3.4 The general rule

> **Read animated/snapshot state in the lowest phase that needs it.**

- Needed only for pixels → read inside `Canvas` / `drawBehind` / `drawWithCache` (draw phase).
- Needed only for position/size → read inside `Modifier.offset { }` / `layout { }` / `graphicsLayer { }` (layout/draw phase).
- Needed for tree structure or text content → composition is required; contain the blast radius with a small dedicated composable and `derivedStateOf` to filter out changes that don't alter the output.

The lambda-parameter pattern (`() -> Float` instead of `Float`) is the standard way to let a caller hand state *down* without reading it *up* in the caller's composition scope.

---

## 4. Verifying

Drop the project's `util/LogCompositions` helper into `WaveLoadingCircleScreen` and `WaveLoadingCircle`: while the animation runs, neither should log after the initial composition. Alternatively, use Android Studio's **Layout Inspector → Recomposition counts**: the Screen and Canvas rows should stay flat while `ProgressPercentText` ticks up roughly 17 times per second.
