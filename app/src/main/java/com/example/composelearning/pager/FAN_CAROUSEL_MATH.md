# The Math Behind the Top-Right Fan Carousel ("Destinations")

The Destinations screen shows a stacked deck of cards. The front card sits centred; behind it, 4 more cards fan toward the top-right corner, each one smaller, more rotated, and more transparent than the one in front of it. You can drag the front card in any direction; release past a threshold and it flies off, the next card slotting into place.

Two different math problems here, neatly separated:

| Problem | Tool |
|---|---|
| Where does each *non-front* card sit? | Linear functions of an integer "depth" 0…N. Pure layout. |
| What happens when the user drags the front card? | 2D vector math: drag → magnitude → direction → unit vector → fly-out. |

The implementation is in [`TopRightFanCarousel.kt`](TopRightFanCarousel.kt). Pair this doc with [`ARC_CAROUSEL_MATH.md`](ARC_CAROUSEL_MATH.md) — same toolbox (`graphicsLayer`, `derivedStateOf`), different problem shape.

---

## 1. The stack model — `depth` is the only input

Every card draws with the same `graphicsLayer` block, parameterised by a single integer `depth ∈ {0, 1, 2, 3, 4}`. `depth = 0` is the front card; higher values go further back.

For each non-front card:

```
translationX = depth × stepX          // px right per layer
translationY = depth × stepY          // px down per layer (negative = up)
scale        = 1 - depth × stepScale  // smaller as you go back
alpha        = 1 - depth × stepAlpha  // fainter as you go back
rotationZ    = depth × stepRotation   // more tilt as you go back
```

In the implementation those step constants are:

```
stepX        =  44.dp →  px       // shift right per layer
stepY        = -32.dp →  px       // shift up per layer (negative Y = up in Android)
stepScale    =  0.085             // 8.5% smaller per layer
stepAlpha    =  0.24              // 24% fainter per layer
stepRotation =  7° (degrees)      // tilted clockwise 7° per layer
```

Each property is a **linear function of `depth`**, with slope = the step constant. That's it. No trig, no easing. The fan effect is purely the *compound* of five independent linear scales.

### Why linear works here

You're discretising the effect — there are only 5 layers, the user never sees a card "between" depth 2 and depth 3. So a smooth easing function would buy you nothing visual but cost you predictability. Linear means you can read the spacing off your fingers: card 3 sits 3×44 = 132 px right of card 0.

```
depth = 0  ┌─────────┐         ← front (drawn LAST so it gets gestures)
           │ Tokyo   │
           └─────────┘
                        ┌─────────┐
depth = 1               │ Bali    │
                        └─────────┘
                                     ┌─────────┐
depth = 2                            │ Paris   │
                                     └─────────┘
                                                  ↗ further toward top-right
depth = 3, 4 ...
```

### Coordinate sign reminder

Android UI Y points **down**. `stepY = -32.dp.toPx()` means each layer moves **up** by 32 dp. Visualising this saves bugs: if your stack fans the wrong way, flip the sign on one axis.

---

## 2. Draw order matters for hit-testing

The loop draws **back-to-front**:

```kotlin
for (depth in visible downTo 0) { … }
```

This is because Compose stacks `Box` children in draw order — last-drawn is on top. The front card (`depth = 0`) is drawn *last* so:
- It visually appears on top.
- It receives pointer events first (Compose dispatches gestures front-to-back).

Drawing front-to-back here would put the back card on top visually *and* it would intercept drag gestures meant for the front card. Always draw layered card decks last-on-top.

---

## 3. The "slot forward" effect — `effectiveDepth = depth - dragMagnitude`

The clever bit. When the user drags the front card halfway to its dismiss threshold, the back cards have already crept halfway forward to take its place. When the dismiss completes, there's no visible "jump" — the cards have already arrived at their new positions, and the only thing that happens is the front card flying off-screen.

The mechanism is one extra line in the back-card branch:

```kotlin
val effectiveDepth = depth - dragMagnitude   // ∈ [depth - 1, depth]
```

Where `dragMagnitude ∈ [0, 1]` is the current drag progress (computed in §4). Now substitute `effectiveDepth` into the same linear formulas:

```
translationX = effectiveDepth × stepX
translationY = effectiveDepth × stepY
scale        = 1 - effectiveDepth × stepScale
…
```

When `dragMagnitude = 0`: `effectiveDepth = depth` → cards in their original stacked positions.
When `dragMagnitude = 1`: `effectiveDepth = depth - 1` → every card has visually moved exactly one slot forward.

Linear interpolation over a single scalar (`dragMagnitude`) shifts **every layer** in lockstep. That's the trick.

```
dragMag = 0.0          dragMag = 0.5          dragMag = 1.0
[0][1][2][3]           [0~][0.5][1.5][2.5]    [—][0][1][2]
 ↑                      ↑                      ↑
 front                  front sliding off      old front gone,
                                               old [1] is now front
```

After the dismiss animation completes, `currentIndex` increments by 1 and `dragMagnitude` resets to 0. Result: the *exact same stack shape*, just showing a different slice of the cards array. Visually seamless.

---

## 4. Drag magnitude — Pythagorean theorem

```kotlin
val dragMagnitude by remember {
    derivedStateOf {
        (hypot(dragX.value, dragY.value) / dismissDistancePx).coerceIn(0f, 1f)
    }
}
```

`hypot(a, b)` is just `sqrt(a² + b²)` — the Pythagorean theorem, computed in a way that avoids overflow for huge inputs. It gives you the **radial distance** the user has dragged from rest, regardless of direction.

```
        dragY
          ↑
          │   • (dragX, dragY)
          │  /
          │ /  ← hypot(dragX, dragY) = distance from origin
          │/
 ─────────┼─────→ dragX
          │
          │
```

Dividing by `dismissDistancePx` normalises this to `[0, 1]`. `coerceIn(0f, 1f)` clips overshoot (the user can drag past the threshold, but the visual effect stops progressing).

Why radial (not just X or Y)? Because the dismiss is direction-agnostic — drag up, down, left, right, or diagonal, and the threshold treats them all the same. The Pythagorean distance is the natural scalar for "how committed is this drag, in any direction?"

---

## 5. Tilt while dragging — small-angle approximation

```kotlin
rotationZ = dragX.value / 28f
```

A horizontal drag of 28 px produces 1° of clockwise tilt. Linear. Why 28? Because that "feels right" — empirically tuned. The principle is:

> For small angles, linear coupling between displacement and rotation is indistinguishable from a true physical model.

If you wanted true physics — a card pivoting around its bottom edge as you drag the top — you'd compute `θ = atan2(dragX, fulcrumDistance)`. But the result is nearly identical to `dragX / 28f` for the displacement range a finger actually produces, at a fraction of the CPU cost. **Save trig for when the displacements get big.**

`28f` is just `1 / sensitivity`. Bigger denominator → less tilt per pixel of drag. If you want a more aggressive tilt, try `dragX / 14f`. For a wobbly, drunken feel, try `dragX / 60f`.

---

## 6. Dismiss threshold — the 70 % rule

```kotlin
if (dist > dismissDistancePx * 0.7f) { dismiss() } else { rebound() }
```

The user is committed to dismissing if they dragged past 70 % of the threshold. Not 100 % — that would feel finicky (you'd often think "I dismissed!" only for the card to spring back). Not 50 % — that would feel jumpy (cards would dismiss when you barely meant to peek).

70 % is a common UX number for "definitely intended" — same idea as a swipe-to-delete that triggers at 60–70 % of the row width rather than the full width. **Threshold should be slightly less than the visible dismiss boundary** so the action feels *eager*, not pedantic.

---

## 7. Fly-out direction — unit vector

When dismiss fires, we want the card to continue in the **direction the user was dragging**. To do this, compute the unit vector of the drag offset:

```kotlin
val dist = hypot(dragX.value, dragY.value)
val dirX = dragX.value / dist        // ∈ [-1, +1]
val dirY = dragY.value / dist        // ∈ [-1, +1]
// dirX² + dirY² = 1  (unit vector)
```

Then throw the card a long way along that vector:

```kotlin
val flyTarget = dismissDistancePx * 4.5f
dragX.animateTo(dirX * flyTarget, tween(260))
dragY.animateTo(dirY * flyTarget, tween(260))
```

`dirX × flyTarget` and `dirY × flyTarget` are the final coordinates — `4.5×` the dismiss distance, so the card definitely leaves the screen. Both axes animate in parallel with the same 260 ms duration, so the card flies in a straight line at constant velocity — even when going diagonal.

### Why unit vectors are everywhere in graphics

The pattern `vector / magnitude` to get a direction-only thing, then `direction × scalar` to throw something along that direction at a controlled distance — that's the foundation of every "fling in the drag direction" effect, every "knockback" in a game, every Bezier handle adjustment, every spotlight direction.

```
drag vector:        unit (direction-only):     fly-out target:
v = (dragX, dragY)  d = v / |v|                t = d × flyDistance
|v| = some px       |d| = 1                    |t| = flyDistance
```

You'll use this constantly.

---

## 8. Spring vs tween — pick by physics

```kotlin
// Card snaps back to rest after a non-dismissal drag:
dragX.animateTo(0f, spring(stiffness = Spring.StiffnessLow))

// Card flies off-screen after a dismissal:
dragX.animateTo(dirX * flyTarget, tween(260))
```

Rule of thumb:

| Use spring when… | Use tween when… |
|---|---|
| The end value is "rest" / a settled position | You're moving *to* a specific value at a specific time |
| The motion should feel physical (settling, overshoot, bounce) | The motion is a deterministic exit (off-screen, completed) |
| Duration is flexible | Duration must be controlled |

A spring computes the next position from physical params (stiffness, damping ratio). It naturally overshoots a tiny bit then settles. Tween is a parametric curve — `value(t) = lerp(from, to, easing(t / duration))` — bounded by duration.

For the rebound, spring is right because "rest" *is* a physical concept and the user expects spring-like settling. For fly-out, tween is right because you want predictable timing — the next card needs to slot in after exactly 260 ms.

### Bonus: spring math, in case you're curious

A spring's equation is `F = -k·x - c·v` (Hooke's law + damping). The Compose `spring` animator integrates this with the parameters:

- **stiffness `k`** — how strongly it pulls toward target. Higher = faster, snappier.
- **damping ratio** — how much resistance opposes velocity. <1 overshoots, =1 critically damps (no overshoot, fastest non-oscillating), >1 underdamps (slow approach).

`Spring.StiffnessLow` is a forgiving setting that lets a small overshoot through. `Spring.StiffnessMediumLow`, `StiffnessMedium`, `StiffnessHigh` are progressively snappier.

---

## 9. Cycling through cards — modulo arithmetic

```kotlin
val cardIndex = (currentIndex + depth) % cards.size
…
currentIndex = (currentIndex + 1) % cards.size
```

Modulo wraps the index back to 0 when you run off the end. With 6 cards and `currentIndex = 5`, asking for `depth = 2` gives `(5 + 2) % 6 = 1` — the second card in the array. The deck loops forever.

If you wanted "stop at the end" instead of "loop", you'd `coerceAtMost(cards.lastIndex)` and stop drawing layers that go out of range. Modulo is cheaper and feels infinite, which is what most card decks want.

---

## 10. The full per-frame pipeline

For each card at `depth = 0..4`, every frame:

```
1.  cardIndex = (currentIndex + depth) % cards.size
2.  IF depth == 0 (front card):
        translationX = dragX
        translationY = dragY
        scale        = 1
        alpha        = 1 - dragMagnitude × 0.45     (slight fade as it leaves)
        rotationZ    = dragX / 28
    ELSE:
        effectiveDepth = depth - dragMagnitude
        translationX = effectiveDepth × stepX
        translationY = effectiveDepth × stepY
        scale        = max(1 - effectiveDepth × stepScale, 0.55)
        alpha        = clamp(1 - effectiveDepth × stepAlpha, 0..1)
        rotationZ    = effectiveDepth × stepRotation
3.  transformOrigin = (0.5, 0.5)
```

And once per drag:

```
4.  dragMagnitude = hypot(dragX, dragY) / dismissThreshold, clamped to [0, 1]

On drag end:
5.  IF hypot(dragX, dragY) > 0.7 × dismissThreshold:
        dirX, dirY = dragX/dist, dragY/dist          (unit vector)
        flyTarget  = 4.5 × dismissThreshold
        animate dragX → dirX × flyTarget, tween(260)
        animate dragY → dirY × flyTarget, tween(260)
        wait for both
        currentIndex++ (mod size)
        reset dragX, dragY = 0
    ELSE:
        animate dragX, dragY → 0 with spring
```

That's the whole thing. The interactive 2D part is steps 4–5; the visual layout is steps 1–3.

---

## 11. Tuning checklist

| You want… | Change |
|---|---|
| Fan more aggressively to the right | Increase `stackStepXDp`. |
| Fan straight up (no horizontal spread) | Set `stackStepXDp = 0.dp`. |
| Each layer to shrink more | Increase `stackStepScale`. |
| Back cards more faded | Increase `stackStepAlpha`. |
| Different fan direction (top-left, bottom-right…) | Flip signs on `stepX` / `stepY`. |
| Easier dismiss | Lower the `0.7f` constant in §6, or lower `dismissDistancePx`. |
| Snappier rebound | `Spring.StiffnessMediumLow` or `StiffnessMedium`. |
| Faster fly-out | Shorter `tween(150)` or larger `flyTarget`. |
| Heavier tilt while dragging | Smaller divisor in `dragX / 28f`. |
| Card visibly "decides" earlier whether it'll dismiss | Drive a colour or scale change from `dragMagnitude` — same input, more visual feedback. |

---

## 12. How this compares to the Arc Carousel

| | Arc Carousel | Fan Carousel |
|---|---|---|
| Input axis | Continuous scroll, normalised to `[-1, +1]` | Discrete `depth` (0..N) + 2D drag vector |
| Per-item formula | One curve (`x² × arcDepth`) on one axis | Five linear functions of `depth` on five properties |
| Active item picked by | `derivedStateOf { closest to centre }` | Hardcoded as `depth == 0` |
| Interaction | Passive (just scroll) | Active (drag-to-dismiss with threshold) |
| Animation primitive | `graphicsLayer` reading scroll state | `graphicsLayer` reading `Animatable<Float>` state |
| Math intensity | Polynomial curves | Pythagoras + unit vectors + linear lerp |

Both use the same Compose primitives. The difference is *what's driving the input* — passive scroll position vs interactive 2D vector.

---

## TL;DR

1. **Stack layout is linear functions of integer depth** — translation, scale, alpha, rotation. Five independent linear scales compounded.
2. **`effectiveDepth = depth - dragMagnitude`** smoothly slots back cards forward as the front card drags away. One subtraction does it.
3. **`hypot(dragX, dragY)` is the Pythagorean theorem** — radial distance, direction-agnostic. Divide by threshold to get a `[0, 1]` progress scalar.
4. **Unit vectors** (`drag / magnitude`) carry direction without magnitude. Multiply by any scalar to throw things along that direction.
5. **70 % threshold** for commit-to-dismiss feels eager without being twitchy.
6. **Tilt = dragX / k** is small-angle linear approximation. Cheaper than `atan2` and indistinguishable in practice.
7. **Spring for settling, tween for exits.** Spring is physical, tween is deterministic.
8. **Modulo** for infinite cycling. `(index + depth) % size`.

Same primitives as the arc carousel; different problem shape. Together they cover the two main "scroll-driven" UI animation patterns — continuous and interactive.
