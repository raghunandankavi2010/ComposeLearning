# Pet Particles — how to build a living particle creature on a Canvas

This document is the *why* behind the four files in this package. It is written
so you can throw the cat and dog away and still build the next one: a logo that
shatters, a face that breathes, a chart that assembles itself out of dust.

| File | Job |
| --- | --- |
| `PetArtwork.kt` | Draws the cat/dog **once**, off-screen, with ordinary Canvas primitives. |
| `PetSampler.kt` | Turns those pixels into a **point cloud** (positions + colours), sorted for batching. |
| `PetParticleEngine.kt` | The **simulation**: springs, forces, integration. No Compose types anywhere. |
| `PetParticlesScreen.kt` | The **Compose layer**: one Canvas, one frame loop, gestures, batched draw calls. |

---

## 0. The mental model

Almost every "wow" particle effect you have seen — Thanos snaps, dissolving
logos, dot-matrix portraits, interactive bear silhouettes — is the same four
stage pipeline:

```
   ART                SAMPLING              SIMULATION            RENDER
 something    →   point cloud with     →   per-particle      →   a handful of
 you can          a home position          forces + an           batched draw
 rasterise        and a colour             integrator            calls
```

The single most useful idea here: **the artwork is data, not output.** You draw
the cat into a bitmap you never show anybody. Its pixels become the *target
positions* and *colours* of a few thousand dots. Everything downstream is
generic — swap the artwork and the engine does not change one line.

That decoupling is why this is worth learning once. The hard parts (sampling,
physics, batching) are reusable; only stage 1 is ever bespoke.

---

## 1. Stage 1 — The source art

### Author in a unit square

`PetArtwork` draws everything in coordinates from `0..1`, then applies
`canvas.scale(size, size)` before drawing. That means:

* the same code renders a 90 px mask on a phone and a 190 px mask on a tablet;
* every stroke width is expressed as a *fraction of the pet* (`0.085f` for the
  tail), so nothing gets thin or fat at different densities;
* mirroring is `1 - x`, so half the artwork is written once.

```kotlin
canvas.scale(size.toFloat(), size.toFloat())
c.ellipse(0.50f, 0.285f, 0.225f, 0.198f, p.fill(FUR))   // the cat's head
```

### Draw *back to front*, and make the palette flat

The bitmap is composited with the painter's algorithm: tail, body, stripes,
bib, ears, head, muzzle, eyes. Ordinary layering.

The one non-obvious rule: **use a small set of exact, flat colours.** No
gradients. The sampler snaps each pixel to the nearest palette entry, and flat
fills match exactly — only the anti-aliased seam between two layers ever needs
the nearest-neighbour fallback. A gradient would smear particles across every
bucket and destroy the batching in stage 4.

Keep palette entries far apart in RGB. The first draft of the cat had a pink
nose `#F08A96` and pink inner ears `#EE9C99`; squared distance ≈ 340, close
enough that anti-aliased pixels flipped between them at random. They were merged
into one `PINK` tone. Rule of thumb: keep squared RGB distance above ~2000
between any two tones.

### A tone is more than a colour

```kotlin
data class FurTone(
    val argb: Int,
    val sizeScale: Float = 1f,   // whiskers 0.62, eyes 1.0
    val glow: Boolean = false,   // extra oversized low-alpha pass
    val isEye: Boolean = false   // dimmed while blinking
)
```

Because each tone gets its own `Paint`, per-tone dot size and bloom are **free**
— they are properties of a draw call you were making anyway. This is the trick
that makes whiskers read as single-file hairlines and eyes read as glowing.

### Tagging body parts: the second mask

The tail has to wag. But the tail is *the same colour as the body*, so the
palette cannot identify it. Solution: rasterise the artwork a second time with
`tailOnly = true` — tail in flat white, everything else transparent — and use it
as a 1-bit tag:

```kotlin
tail[n] = if ((tailPixels[row + x] ushr 24) >= ALPHA_CUTOFF) 1 else 0
```

One subtlety that caused a real bug: in the full portrait the body is drawn
*over* the tail root, so the naive tail mask also tags a slice of the flank.
Rotating those with the tail sheared the body. The fix is to erase the body from
the tag mask:

```kotlin
private fun Canvas.punchOut(shape: Path) {
    val eraser = Paint(ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }
    drawPath(shape, eraser)
}
```

Generalise: **one extra mask per articulated body part.** Head, ears, legs, jaw
— each is a cheap boolean channel, and the engine turns each into a weight.

---

## 2. Stage 2 — Sampling: pixels → point cloud

### Choose the resolution, not the count

The naive approach is "render at screen resolution and pick N random opaque
pixels". Don't. Render the mask at *exactly the density you want particles at*:

```kotlin
fun maskSizeFor(fitPx: Float, targetSpacing: Float = 6.2f): Int =
    (fitPx / targetSpacing).toInt().coerceIn(90, 190)
```

If the pet occupies 800 px and you want a dot every ~6.2 px, render a 129×129
mask. Now **one opaque mask pixel = one particle**, which gives you:

* perfectly even coverage with no sampling algorithm at all;
* constant *visual* density across phones, tablets and foldables — a 6 px gap
  looks the same everywhere, whereas a fixed particle count would look sparse
  on a tablet and mushy on a phone;
* a tiny `getPixels` (129² = 16 641 ints ≈ 66 KB) instead of a 2 MP read;
* an automatic particle budget, clamped by the `coerceIn(90, 190)` — the worst
  case is 190² × ~35 % opaque ≈ 12 600 candidates, then capped at 9 000.

### Why a jittered grid beats random sampling

Uniform random points clump. It is the same reason random star fields look
wrong: the probability of two points landing close is not small enough. Formally
the nearest-neighbour distances follow an exponential distribution, so you get
both clusters and holes.

A grid has the opposite problem — it is *too* regular and reads as a lattice,
complete with moiré against the pixel grid.

The cheap fix, and the one used here, is a **jittered grid** (stratified
sampling): one sample per cell, displaced by up to half a cell.

```kotlin
ux[dst] = (xs[src] + rng.nextFloat() - 0.5f) * inv
```

You get grid-like uniformity with random-like appearance. This is a poor man's
blue noise, and it is what most real particle systems use. Proper Poisson-disc
or void-and-cluster blue noise is better still, but costs orders of magnitude
more to generate and you will not see the difference at 6 px spacing.

### Colour quantisation

For each opaque pixel, find the nearest palette entry by squared RGB distance:

```kotlin
val d = dr * dr + dg * dg + db * db
```

Notes:

* **No `sqrt`.** Comparing distances never needs the square root — `√` is
  monotonic, so `a² < b² ⟺ a < b` for non-negative values. This trick appears
  three more times in the engine.
* Squared *Euclidean RGB* distance is not perceptually correct (that would be
  CIE Lab ΔE), but for snapping flat fills back to their own palette it is
  exactly right and far cheaper.
* The palette is pre-split into `pr[]`, `pg[]`, `pb[]` int arrays so the inner
  loop touches primitives instead of unpacking `argb` every iteration.

### Counting sort: the trick that buys batched rendering

This is the most important twelve lines in the sampler.

Particles are stored **sorted by tone**, so tone *b* owns the contiguous slice
`[bucketStart[b], bucketStart[b+1])`. That slice is precisely the argument list
of one draw call:

```kotlin
native.drawPoints(renderBuffer, from shl 1, n shl 1, paint)
```

Sorting by a small integer key does not need comparisons. Counting sort is
O(n + k) in two passes:

```kotlin
val counts = IntArray(k)
for (i in 0 until target) counts[tone[pick(i, target)]]++      // 1. histogram
val bucketStart = IntArray(k + 1)
for (b in 0 until k) bucketStart[b + 1] = bucketStart[b] + counts[b]   // 2. prefix sum
val cursor = bucketStart.copyOf()
for (i in 0 until target) { ... val dst = cursor[tone[src]]++ ... }    // 3. scatter
```

The prefix sum is the bucket layout; the cursor copy is the write head per
bucket. Because tone membership never changes at runtime, **this sort happens
once, off the main thread, and the offsets are valid forever.**

### Making two shapes morphable

Cat and dog are sampled at the same mask size, then both trimmed to
`min(nCat, nDog)` particles. Particle *i* in the cat maps to particle *i* in the
dog — no matching algorithm, no Hungarian assignment. Trimming uses stride
decimation *in raster order*:

```kotlin
private fun pick(i: Int, target: Int): Int =
    if (target >= n) i else (i.toLong() * n / target).toInt()
```

Dropping a random subset instead would leave visible bald patches; striding
spreads the thinning evenly over the body. (Note the `toLong()` — `i * n` for
20 000 candidates and 9 000 targets is 180 million, comfortable in `Int`, but at
higher densities it is one careless multiply away from overflow.)

**Trade-off you should know about:** because the two clouds are sorted
independently, particle *i* can change tone when you switch pets. Colours pop at
the instant of the switch. The demo hides this by firing an outward burst at the
same moment, so the eye is tracking motion rather than colour. The alternative —
cross-fading colours per particle — would mean per-particle colour, which means
giving up the one-draw-call-per-tone batching. That is a bad trade for a 200 ms
transition, and knowing *why* you rejected it is the point.

---

## 3. Stage 3 — The simulation

### Structure of arrays, not array of structures

```kotlin
// This:
private val px = FloatArray(count); private val py = FloatArray(count)
private val vx = FloatArray(count); private val vy = FloatArray(count)

// Not this:
class Particle(var x: Float, var y: Float, var vx: Float, var vy: Float)
val particles = List(count) { Particle(...) }
```

For 8 000 particles the object version means 8 000 heap objects, 8 000 pointer
dereferences per pass, headers and padding roughly tripling the memory, and a
GC that has to trace all of it. The SoA version is eight flat float arrays the
prefetcher walks in a straight line. On the JVM this is routinely a 3–10×
difference on the update loop, and — more importantly on Android — it is the
difference between allocating per frame and allocating never.

Rule: **anything you touch once per particle per frame belongs in a
`FloatArray`.**

### Every particle is a damped spring

The whole behaviour rests on one equation. Each particle is anchored to its home
pixel by a spring, with viscous damping:

```
a = k·(home − p) − c·v
```

* `k` (`stiffness = 340`) is the spring constant, units 1/s² (we treat mass as
  1, so force *is* acceleration).
* `c` (`damping`) is the drag. It is *derived*, not tuned:

```kotlin
val damping = 2f * dampingRatio * sqrt(stiffness)
```

This is the standard form `c = 2ζ√k`, where **ζ (zeta) is the damping ratio** —
the only number you actually want to reason about:

| ζ | behaviour |
| --- | --- |
| 0 | undamped — oscillates forever |
| 0 < ζ < 1 | underdamped — overshoots, rings, settles |
| 1 | **critically damped** — fastest possible return with no overshoot |
| ζ > 1 | overdamped — sluggish, no overshoot |

Two formulas worth memorising:

* natural frequency `ω = √k` (rad/s); here `√340 = 18.4`
* 2 % settling time `≈ 4 / (ζ·ω)`; here `4 / (0.72 × 18.4) ≈ 0.30 s`
* overshoot fraction `= exp(−πζ/√(1−ζ²))`; here ≈ 4 %

So `stiffness = 340, dampingRatio = 0.72` is not a magic pair — it is "come home
in about a third of a second with a 4 % bounce". Decide the *feel* in those
terms, then solve for the constants. This is exactly what Compose's own
`spring(dampingRatio, stiffness)` animation spec does, and
`Spring.DampingRatioMediumBouncy` is 0.5, `DampingRatioNoBouncy` is 1.0.

### Integration: use semi-implicit Euler

```kotlin
v += a * dt      // velocity first…
p += v * dt      // …then position with the NEW velocity
```

That ordering is the entire difference between semi-implicit (symplectic) Euler
and explicit (forward) Euler, and it matters enormously:

* **Explicit Euler** (`p += v*dt; v += a*dt`) injects energy into oscillators.
  A spring simulated this way grows in amplitude every cycle and eventually
  explodes. It is unconditionally unstable for undamped springs.
* **Semi-implicit Euler** conserves energy on average. For a spring it is stable
  as long as `dt < 2/ω`.

Same cost, same line count, categorically better. Use it every time.

Here `2/ω = 2/18.4 = 0.108 s`, which is why the frame loop clamps:

```kotlin
val dt = ((now - last) / 1_000_000_000f).coerceIn(0f, 0.033f)
```

**Clamping `dt` is not optional.** Without it a paused app, a slow first frame
or a debugger breakpoint hands you `dt = 2.0` and the entire field detonates. The
clamp turns a dropped frame into a moment of slow motion — always the right
choice for decorative animation. (Anything above ~0.033 s and you have already
dropped a frame; nobody can tell the difference between catching up and not.)

If you need bigger steps than the stability limit, the fixes are: sub-step the
integrator (loop `n` times with `dt/n`), or switch to a Verlet / analytic spring
solution. For UI-rate work, clamping is enough.

### Forces, and the shape of a falloff

Everything else is added to `a` before integration. The interesting part is the
*kernel* — how influence decays with distance.

```kotlin
val f  = 1f - d / touchRadius     // linear ramp, 1 at centre → 0 at rim
val ff = f * f                    // quadratic
ax += nx * pushStrength * ff
```

Why squared and not linear? At the rim, the linear kernel has value 0 but slope
−1/R. As your finger sweeps across the field, particles crossing the boundary
get a sudden non-zero jerk — you see a hard circular edge. The quadratic kernel
has value 0 *and* slope 0 at the rim (`d/dx (1−x)² = −2(1−x)` → 0 at x=1), so
influence fades in smoothly. **Match the derivative at the boundary, not just
the value.** Same principle as `smoothstep` versus a linear ramp.

Kernels worth knowing:

| kernel | formula | feel |
| --- | --- | --- |
| linear | `1 − d/R` | hard rim, cheap |
| quadratic (used here) | `(1 − d/R)²` | soft rim, still cheap |
| smoothstep | `t²(3−2t)` | soft at *both* ends |
| Gaussian | `exp(−d²/2σ²)` | softest, no hard cutoff, needs `exp` |
| inverse square | `1/(d² + ε)` | physical (gravity, charge), needs the ε or it divides by zero |

Note the `ε` in that last row: never write `1/d²` with a raw distance. This code
guards with `if (d2 < touchRadiusSq && d2 > 1e-4f)`, which both skips faraway
particles *before* the `sqrt` and avoids the singularity at `d = 0`.

### Three forces make touch feel physical

A single repulsion force feels like a hole punched in the fur. What sells the
interaction is the combination:

```kotlin
// 1. push — radial, away from the finger
ax += nx * pushStrength * ff
ay += ny * pushStrength * ff
// 2. swirl — perpendicular, scaled by how fast you're moving
ax += -ny * swirlNow * ff
ay +=  nx * swirlNow * ff
// 3. advection — fur gets dragged along with the stroke
ax += touchVx * advection * ff
ay += touchVy * advection * ff
```

`(-ny, nx)` is the radial vector rotated 90°. In 2-D that is all a "curl" is:
the perpendicular of a gradient. Scaling it by pointer speed means a slow touch
just parts the fur while a fast swipe curls it into a vortex.

Advection is the one people forget, and the one that makes it feel like *fur*
instead of a force field: matter you push moves in the direction you pushed it,
not merely away from you.

Finger velocity has to be filtered before it can drive a force. Raw deltas
between pointer events are extremely spiky:

```kotlin
vx += ((nx - x) / dt - vx) * 0.35f     // one-pole low-pass
```

That is exponential smoothing — a first-order IIR filter. `0.35` is the cutoff;
lower is smoother and laggier. Also decay it every frame, because when a finger
is held still *no events arrive* and a naive tracker would report the last
velocity forever:

```kotlin
val k = Math.pow(0.86, (dt * 60f).toDouble()).toFloat()
vx *= k
```

Note the `pow(base, dt*60)` form. `v *= 0.86` per frame is frame-rate dependent —
it decays twice as fast at 120 Hz as at 60 Hz. Raising the base to the power of
`dt·60` normalises it to "0.86 per 60 Hz frame" at any refresh rate. Any time
you write a per-frame multiplier, write it this way.

### The purr ripple: an expanding shell of force

```kotlin
rippleR += rippleSpeed * dt                       // radius grows with time
val band = 1f - abs(d - rippleR) * invBand        // triangular shell
if (band > 0f) { ax += dx / d * band * force }
```

A wave is a function of `(d − r)`, not of `d`. Particles near the *current*
radius get pushed; everything else is untouched. A Gaussian shell
`exp(−((d−r)/σ)²)` is prettier but costs an `exp` per particle per frame; the
triangular band is visually indistinguishable in motion. Amplitude fades with
`1 − r/rMax` so the wave dies out at the edge instead of stopping abruptly.

### Making it feel alive: weights, not branches

Idle life is what separates "particle demo" from "creature". Four layers, all
driven off the *home* position before the spring is evaluated:

**Breathing** — a uniform scale about the seated base:

```kotlin
val breath = 1f + 0.014f * sin(time * 1.65f)
tx = basePivotX + (tx - basePivotX) * breath
```

Scaling about a pivot is always `pivot + (p − pivot) · s`. Pivot at the *base*,
not the centre, or the pet appears to levitate. 1.4 % is deliberately tiny —
past ~3 % it reads as a pulsing blob rather than breathing.

**Head bob** — a vertical offset, weighted so the neck does not shear:

```kotlin
bob[i] = smoothstep(0.50f, 0.30f, uy)   // 1 at the top of the head, 0 below the neck
ty += bobNow * bob[i]
```

`smoothstep(e0, e1, x) = t²(3−2t)` with `t` clamped is the standard
zero-derivative-at-both-ends ramp. Note the edges are given *reversed*
(`0.50 → 0.30`) to invert the ramp — a neat idiom worth stealing.

**Tail wag** — rotation about a pivot, weighted by distance from it:

```kotlin
val a = wagNow * wag[i]
val s = sin(a); val c = cos(a)
tx = pivotX + rx * c - ry * s
ty = pivotY + rx * s + ry * c
```

That is the 2×2 rotation matrix `[[cos, −sin], [sin, cos]]` applied to the
offset from the pivot. Because the angle is scaled by a per-particle weight
that grows with distance from the pivot, the tail *bends* rather than swinging
rigidly — a free approximation of a jointed chain.

**Shimmer** — a Lissajous wobble with a per-particle phase:

```kotlin
val ph = phase[i] + time * 1.9f
tx += sin(ph) * shimmer
ty += cos(ph * 0.87f) * shimmer
```

The random `phase[i]` is the whole trick: identical motion with different phases
reads as a living coat, whereas a shared phase reads as the whole image
vibrating. The `0.87` factor makes x and y incommensurate, so the little orbits
never repeat.

Notice that all four are expressed as **per-particle weights** (`wag[i]`,
`bob[i]`, `phase[i]`) rather than `if (isTail)` branches. Weights interpolate;
booleans produce seams.

### Trig lookup tables

At 8 000 particles the loop evaluates `sin`/`cos` up to four times per particle,
i.e. ~2 million calls per second. `Math.sin` is accurate to the last ulp and you
need none of that for a wobble.

```kotlin
private object Trig {
    private const val N = 2048               // power of two
    private const val MASK = N - 1
    private val SIN = FloatArray(N) { sin(it * 2.0 * PI / N).toFloat() }
    private const val TO_INDEX = N / (2f * PI.toFloat())
    fun sin(a: Float) = SIN[(a * TO_INDEX).toInt() and MASK]
    fun cos(a: Float) = SIN[((a * TO_INDEX).toInt() + N / 4) and MASK]
}
```

Details that matter:

* `N` must be a power of two so wrapping is `and MASK` instead of `%`.
* Two's-complement `and` makes **negative angles wrap correctly for free**.
* `cos` is `sin` shifted by a quarter table — one table, not two.
* 2048 entries gives ~0.003 rad resolution; at these amplitudes the error is
  sub-pixel. Use interpolation between neighbours only if you can see banding.

The same idea generalises: any expensive pure function of one variable that you
call per-particle (`exp`, `pow`, easing curves, noise) can become a table.

---

## 4. Stage 4 — Rendering, and how Compose actually gets out of the way

### The three phases

Compose UI runs three phases per frame:

```
composition  →  layout  →  draw
(what to show)  (where)     (how)
```

The catastrophic beginner mistake in animation is doing frame-rate work in the
first phase. If your 60 Hz ticker writes state that is read during
**composition**, you re-run composition, layout and draw sixty times a second —
for the whole subtree.

The rule is: **read frame-rate state as late as possible.** If a `State` is only
ever read inside a draw lambda, Compose records that read against the draw phase
and invalidates *only* draw.

```kotlin
var frame by remember { mutableIntStateOf(0) }

LaunchedEffect(engine) {
    while (isActive) {
        withFrameNanos { ... }
        engine.update(dt, ...)
        frame++                       // written from a coroutine
    }
}

Canvas(modifier) {
    frame                             // ← read HERE, inside DrawScope
    ...
}
```

The lone `frame` expression inside the draw lambda looks like dead code and is
in fact the load-bearing line of the whole screen: it is the subscription that
schedules the next draw. Delete it and the animation freezes even though the
engine keeps stepping.

`mutableIntStateOf` rather than `mutableStateOf(0)` avoids autoboxing an `Int`
sixty times a second — small, but it is free.

### Do not put per-frame values in composable parameters

Corollary of the same rule: never pass an animating value down as a parameter
(`Particles(offset = animatedOffset)`), because reading it to build the
parameter *is* a composition read. Pass a lambda (`offset = { animatedOffset }`)
or a `State` and read it in the draw scope. The `StatsBadge` in this screen uses
that idiom for a much slower value:

```kotlin
@Composable
private fun StatsBadge(stats: () -> FieldStats, ...) {
    val value = stats()   // the read happens in THIS scope, not the parent's
```

so the twice-a-second stats update recomposes a single `Surface` rather than the
whole screen.

### Keep pointer input out of snapshot state entirely

Pointer events arrive far more often than frames — up to 240 Hz on some
devices. Writing each one into `mutableStateOf` schedules snapshot work that
nothing needs:

```kotlin
private class PointerTracker {   // a plain class, no State anywhere
    var x = 0f; var y = 0f
    var vx = 0f; var vy = 0f
    var active = false
}
val pointer = remember { PointerTracker() }
```

The frame loop reads these fields once per frame. Both sides run on the main
thread, so there is no visibility problem. State is for things the *UI* must
react to; this is data flowing into a simulation.

### `withFrameNanos`, not a timer

```kotlin
var last = withFrameNanos { it }
while (isActive) {
    val now = withFrameNanos { it }
    val dt = ((now - last) / 1e9f).coerceIn(0f, 0.033f)
```

`withFrameNanos` suspends until the next Choreographer frame callback and hands
you the frame's timestamp. That means:

* you are aligned with vsync — no tearing, no double-stepping;
* you automatically stop when the composable leaves the composition, because the
  `LaunchedEffect` coroutine is cancelled;
* you get the *frame* time, not "now", so `dt` does not accumulate the jitter of
  your own scheduling.

A `Handler.postDelayed(16)` loop gives you none of those.

### One draw call per colour

The naive renderer is `particles.forEach { drawCircle(it.color, ...) }`. At
8 000 particles that is 8 000 draw ops per frame, each with its own paint
lookup and command-buffer entry. It will not hold 60 Hz.

Instead, positions are streamed into one interleaved array during the update:

```kotlin
val o = i shl 1
renderBuffer[o] = x
renderBuffer[o + 1] = y
```

and because the particles are tone-sorted, each tone is one contiguous run:

```kotlin
for (b in set.fill.indices) {
    val from = starts[b]
    val n = starts[b + 1] - from
    native.drawPoints(renderBuffer, from shl 1, n shl 1, set.fill[b])
}
```

**7–9 draw calls per frame, total**, regardless of particle count.
`Canvas.drawPoints(pts, offset, count, paint)` takes an *offset* and a *count of
floats* (hence `shl 1` — two floats per point), which is exactly why the sort
was worth doing. `Paint.Cap.ROUND` on a `STROKE` paint makes each point a round
dot of `strokeWidth` diameter.

You reach `nativeCanvas` through `drawIntoCanvas { it.nativeCanvas }`, because
Compose's `DrawScope` has no batched points API. That is fine — `DrawScope` is a
convenience layer over the same `android.graphics.Canvas`.

### Cheap bloom

Tones flagged `glow` get a second pass first: same buffer, same offsets,
`strokeWidth × 3.4`, `alpha = 46`. Two low-alpha round caps stacked read as a
soft halo. It costs one extra draw call for the two or three tones that want it,
versus a `RenderEffect.createBlurEffect` over the whole layer, which would cost
a full-screen offscreen pass.

Blink is the same idea: `paint.alpha = eyeAlpha` on the eye tones before their
draw call. One field write animates 300 particles.

---

## 5. The performance playbook

Ordered by how much they actually buy you:

1. **Batch the draw calls.** 8 000 `drawCircle`s → 8 `drawPoints`. This is the
   difference between 4 fps and 60 fps; nothing else comes close.
2. **Structure of arrays.** Flat `FloatArray`s, zero allocation in the loop.
3. **Invalidate the draw phase only.** Frame state read inside `DrawScope`.
4. **Clamp `dt`** and use semi-implicit Euler. Stability is performance: an
   exploded field draws a screenful of overlapping points.
5. **Reject before you compute.** Compare squared distances; `sqrt` only for
   particles that passed the radius test.
6. **Hoist per-frame constants** out of the particle loop (`breath`, `wagNow`,
   pivot positions, `1/rippleBand`). Anything not indexed by `i` computes once.
7. **Lookup tables** for `sin`/`cos`/`exp` in the hot loop.
8. **Size the work to the screen**, not to a constant. Particle spacing in
   pixels, count derived from it, then clamped.
9. **Reuse `Paint` objects.** Allocating a `Paint` per frame allocates a native
   peer too.
10. **Do setup off the main thread.** Rasterising and sampling both pets is a
    few milliseconds — `withContext(Dispatchers.Default)`, then publish.
11. **`mutableIntStateOf`/`mutableFloatStateOf`** for primitive animation state.
12. **`recycle()` scratch bitmaps** as soon as `getPixels` returns.

### A rough budget

At 60 Hz you have 16.6 ms per frame, and you want to fit in ~8 ms so the system
has room for everything else. **Do not take the numbers below from me — read
them off the badge on your own device**, which averages the update loop over 30
frames. Measure before and after every change you make.

The shape of the curve, though, is reliable: the update loop is O(n) with a very
small constant (a few dozen float ops per particle), and the draw cost is
dominated by *fill rate* — the number of pixels the dots cover — not by the
number of particles, because the draw call count is fixed. So doubling the
particle count roughly doubles update time, while doubling the dot *size*
quadruples draw time. If your frame is slow, find out which of the two it is
before optimising: the badge tells you the update half, and whatever is left
over in `Choreographer#doFrame` is the draw half.

Rough regimes for this architecture on a CPU:

| particles | verdict |
| --- | --- |
| ≤ 10 000 | comfortable; this demo sits here |
| 10 000 – 50 000 | fine on flagships, worth profiling on budget devices |
| 50 000+ | leave the CPU: instanced GPU rendering or a compute shader |

### When to stop using this architecture

* **Particle-to-particle interaction** (flocking, collisions, SPH fluids) is
  O(n²) as written. Add a **uniform spatial hash**: bucket particles into cells
  of the interaction radius, and only test the 9 neighbouring cells. That turns
  it into roughly O(n). Nothing in this demo needs it, because the finger is a
  single point queried against every particle — already O(n).
* **Large particle counts**, or per-particle textures/trails: move to the GPU. On
  Android that means AGSL (`RuntimeShader`) with the state packed into a
  texture, or dropping to OpenGL/Vulkan. See this project's `shaders/` and
  `riveo/` packages for the AGSL approach.
* **Physics you want to be *correct*** (stacking, friction, joints): use a real
  solver. Hand-rolled forces are for feel, not fidelity.

### How to measure, not guess

* The on-screen badge here (`ms sim`) is the cheapest possible instrument:
  average the update time over 30 frames.
* **Perfetto / `systrace`** for the real picture — look for `Choreographer#doFrame`
  exceeding your budget, and check whether the time is in `Composition`,
  `Layout` or `Draw`. If you see *any* `Composition` slices during a steady
  animation, you have a deferred-read bug.
* **Layout Inspector → recomposition counts.** A correctly built particle screen
  shows a recomposition count that stays flat while the animation runs. If it
  climbs, your frame ticker is being read in composition.
* **Android Studio's power/memory profiler** for allocation spikes — a sawtooth
  during animation means something in the loop allocates.
* Test on the slowest device you support, in release mode with R8. Debug builds
  with the Compose compiler's live-literal instrumentation can be 5× slower and
  will send you chasing ghosts.

---

## 6. The same ideas in other frameworks

The pipeline is universal; only the API names change.

| | Android / Compose | Flutter | Web | SwiftUI |
| --- | --- | --- | --- | --- |
| offscreen art | `Bitmap` + `android.graphics.Canvas` | `PictureRecorder` → `Image` | offscreen `<canvas>` | `ImageRenderer` / `CGContext` |
| read pixels | `Bitmap.getPixels` | `Image.toByteData` | `getImageData` | `CGContext` buffer |
| frame clock | `withFrameNanos` | `Ticker` / `AnimationController` | `requestAnimationFrame` | `TimelineView(.animation)` |
| batched draw | `Canvas.drawPoints` | `Canvas.drawRawPoints` | one `Path` + `fill()`, or WebGL instancing | `Canvas` + `Path`, or Metal |
| escape hatch | AGSL `RuntimeShader` | `FragmentProgram` | WebGL / WebGPU | Metal shaders |

Two portability notes. Flutter's `drawRawPoints(PointMode.points, Float32List)`
is the exact analogue of `drawPoints`, including the interleaved buffer — the
LinkedIn-style bear demos are usually built on it. On the web, `fillRect` per
particle is slow, but accumulating all particles of one colour into a single
`Path2D` and filling once is the same batching trick.

---

## 7. Recipe: designing your own

1. **Draw the thing** in a unit square with flat colours from a small,
   well-separated palette. Layer back to front.
2. **Add a tag mask** for every part you want to articulate. Punch out whatever
   occludes it.
3. **Pick a spacing in pixels**, derive the mask resolution from the available
   space, clamp it. One opaque pixel = one particle.
4. **Quantise to the palette, jitter, counting-sort by tone.** Record the bucket
   offsets. Do all of this once, off the main thread.
5. **Give every particle a home** and a damped spring to it. Choose ζ and a
   settling time first, then solve for `k` and `c`.
6. **Layer forces on top of the spring**, each with a smooth kernel and a
   per-particle weight.
7. **Integrate semi-implicitly with clamped `dt`.** Clamp velocity too.
8. **Stream positions into one interleaved buffer** and draw one batch per
   colour.
9. **Read the frame counter only inside the draw scope.**
10. **Measure on a slow device** before you add anything else.

---

## 8. Things to try in this package

Ordered roughly by difficulty; each is a real exercise in one of the ideas above.

1. Change `dampingRatio` to `1.0` and then `0.3`. Watch overshoot appear and the
   settling time change exactly as the formula predicts.
2. Replace the quadratic touch kernel with the linear one and sweep your finger
   slowly. The hard circular rim is immediately visible — that is what matching
   derivatives at the boundary buys you.
3. Swap the semi-implicit integrator to explicit Euler (`p += v*dt` first) and
   remove the velocity clamp. Time how long it survives.
4. Remove the `frame` read from inside the `Canvas` lambda and confirm the
   animation stops while the engine keeps stepping. Then move the read one level
   up into the composable body and watch recomposition counts explode in Layout
   Inspector.
5. Add a **head mask** alongside the tail mask and make the pet tilt its head
   toward your finger: rotate the head particles about the neck by an angle
   proportional to `atan2` of the finger offset, damped over time.
6. Add gravity plus a floor at `originY + fit` and make long-press a real
   collapse — particles fall into a heap and reassemble on tap.
7. Give each particle a `size[i]` from a hash of its index and use `drawPoints`
   with several width buckets, so the coat has fine and coarse hairs.
8. Add a third pet. Everything except `PetArtwork.kt` is already generic — that
   is the test of whether the decoupling was real.
