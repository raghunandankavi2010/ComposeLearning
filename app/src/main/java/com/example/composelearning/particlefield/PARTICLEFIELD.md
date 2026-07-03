# Particle Field — steering-behaviour text particles

A Compose port of Flutter's [`animated_particles`](https://github.com/justkawal/animated_particles).
Thousands of particles are **sampled from the pixels of text/glyphs**, then they
**arrive** into the shape, **morph** between shapes, **flee** from your finger and
**escape** off-screen — all driven by a **structure-of-arrays** engine and a single
`drawPoints` call.

- `ParticleEngine.kt` — the maths + simulation (no Compose).
- `ParticleField.kt` — the Compose driver: frame loop, gestures, rendering.

This document explains the maths and APIs, and — more usefully — **how to think
about building an animation like this from scratch**.

---

## 1. How to approach *any* particle animation

Almost every particle animation is the same four-part loop. Nail these four and
the rest is flavour:

1. **State** — what does one particle *have*? (position, velocity, a target, a
   colour, an age…). Store it in a way that's cheap to update.
2. **A clock** — a per-frame tick that gives you `dt` (seconds since last frame).
   Everything physical must be multiplied by `dt` so it runs the same on a 60Hz
   and a 120Hz screen.
3. **An update rule** — given the current state and `dt`, compute the next state.
   For "living" motion this is usually **acceleration → velocity → position**
   (Euler integration), where acceleration comes from *behaviours* (arrive, flee…).
4. **A draw** — read the state and paint it, ideally in as few draw calls as
   possible.

> **Rule of thumb:** keep *simulation* (maths on numbers) completely separate from
> *rendering* (Compose/Canvas). Here that's literally two files. The engine has no
> idea Compose exists; the composable has no idea how a particle moves. You can
> unit-test the engine, and swap the renderer, independently.

The mental model for the motion itself is Craig Reynolds' **steering behaviours**
(the "boids" paper): a particle has a *current* velocity and computes a *desired*
velocity; the difference between them is a steering force that nudges it. Different
goals (arrive here, run away from that) each produce a desired velocity; you add
the resulting forces together.

---

## 2. The clock — `withFrameNanos` and `dt`

```kotlin
var lastNanos = withFrameNanos { it }
while (isActive) {
    val now = withFrameNanos { it }
    val dt = ((now - lastNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
    lastNanos = now
    engine.update(dt, ...)
    frame++            // bump a State<Long> to request a redraw
}
```

- **`withFrameNanos`** suspends until the next display frame and hands you the frame
  time in nanoseconds. It's the Compose equivalent of a `Choreographer` /
  `requestAnimationFrame` callback — you get exactly one tick per rendered frame,
  synced to vsync.
- **`dt`** is the elapsed time in *seconds*. We divide by 1e9 (nanos → seconds).
- **`.coerceIn(0f, 0.05f)`** clamps `dt`. If the app is backgrounded or a frame is
  dropped, `now - lastNanos` can be huge; without the clamp every particle would
  "teleport" on the first frame back (position += velocity × giant dt). Clamping to
  50 ms (≈ 3 frames) keeps a hiccup from exploding the simulation.
- **`frame++`** — the trick for redrawing. A Compose `Canvas` only re-executes its
  draw lambda when a `State` it *reads* changes. The simulation mutates plain
  `FloatArray`s (not Compose state), so nothing would redraw. Reading `frame`
  (a `MutableState<Long>`) inside the draw block subscribes the Canvas to it;
  bumping it once per tick forces exactly one redraw per frame.

---

## 3. Structure of Arrays (SoA) — why the data is laid out this way

The obvious design is an **array of structs** — `List<Particle>` where
`Particle` has `x, y, vx, vy…`. It's readable but, for thousands of particles
updated 60–120×/second, it's the slow choice: each `Particle` is a heap object
(pointer-chasing, poor cache locality) and iterating allocates iterators/boxing.

Instead we use a **structure of arrays**: one flat primitive array per attribute.

```kotlin
val px = FloatArray(count)   // x positions
val py = FloatArray(count)   // y positions
val vx = FloatArray(count)   // x velocity
val vy = FloatArray(count)   // y velocity
val tx = FloatArray(count)   // target x (where this particle wants to be)
val ty = FloatArray(count)   // target y
```

Particle *i* is the tuple `(px[i], py[i], vx[i], vy[i], tx[i], ty[i])`. Updating
the field is one `for (i in 0 until count)` loop over contiguous memory — the CPU
prefetcher loves it, there's **zero per-frame allocation**, and it maps directly
onto the flat buffer the renderer needs (§7). This is the "tight structure-of-arrays
engine" from the package description.

---

## 4. Sampling a shape from pixels

How do we turn the word "Compose" into a cloud of target points? **Rasterise it,
then read the pixels.**

```kotlin
val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
val canvas = android.graphics.Canvas(bmp)
val paint = Paint().apply { color = WHITE; textAlign = CENTER; typeface = BOLD }
// …scale textSize so the glyph fills ~78% of the screen…
canvas.drawText(text, width / 2f, baselineY, paint)

val pixels = IntArray(width * height)
bmp.getPixels(pixels, 0, width, 0, 0, width, height)
```

We draw white text onto an offscreen bitmap, then pull all pixels into an `IntArray`
(each `Int` is `ARGB`). A pixel is "part of the glyph" if its **alpha** is high:

```kotlin
val alpha = pixels[y * width + x] ushr 24 and 0xFF   // top 8 bits
if (alpha > 128) { /* this (x,y) is inside the letter */ }
```

We scan on a **grid step** (every 2–6 px, tuned to screen size) so we don't collect
a million samples — that gives a list of candidate `(x, y)` positions covering the
letterforms.

**Fitting the text.** `Paint.getTextBounds` gives the glyph's pixel bounds at the
current `textSize`. Text size scales linearly, so to make the shape fill a target
width we just scale the font:

```
newTextSize = oldTextSize × (targetWidth / measuredWidth)
```

Then re-measure at the new size to centre it: the vertical centre of the drawn text
is placed at the canvas centre using `bounds.exactCenterY()`.

**Mapping candidates → particles.** We have a fixed particle count `N`, but each
shape yields a *different* number of candidate pixels. So:

- If there are **fewer** candidates than particles, several particles share a
  candidate — we add a small random **jitter** (± one grid step) so they don't
  stack perfectly on one pixel.
- If there are **more** candidates than particles, we *stride* through them
  (`idx = i × candidateCount / N`) so the chosen subset spreads evenly over the
  whole glyph instead of clumping in one corner.

```kotlin
for (i in 0 until count) {
    val idx = (i.toLong() * n / count).toInt() % n
    tx[i] = candidateX[idx] + (rng.nextFloat() - 0.5f) * jitter
    ty[i] = candidateY[idx] + (rng.nextFloat() - 0.5f) * jitter
}
```

The same `sampleText` works for a single glyph like `★` (that's your "icon"), and
the identical idea works for an arbitrary `Bitmap` — skip the `drawText` and read
its alpha directly, which is how you'd sample an image or an SVG/vector rendered to
a bitmap.

---

## 5. The maths of motion — steering behaviours

All motion uses **semi-implicit Euler integration**:

```
acceleration a  (computed from behaviours, below)
velocity     v += a · dt
position     p += v · dt
```

Each behaviour contributes to `a`. We just add them up.

### 5a. Arrive (the "assemble into the shape" behaviour)

A naïve "seek" always moves at full speed toward the target and overshoots,
orbiting forever. **Arrive** ramps the *desired speed* down to zero as you get close,
so particles ease into place.

Let `d = target − position`, and `dist = |d|`. Desired speed:

```
desiredSpeed = maxSpeed                        if dist ≥ slowRadius
desiredSpeed = maxSpeed · (dist / slowRadius)  if dist <  slowRadius   ← linear ramp to 0
```

Desired velocity is that speed along the direction to the target
(`d / dist` is the unit direction):

```
desiredVel = (d / dist) · desiredSpeed
```

The steering acceleration pushes current velocity *toward* the desired velocity:

```kotlin
ax += (desiredVel.x - v.x) * arriveResponse
ay += (desiredVel.y - v.y) * arriveResponse
```

`arriveResponse` (units: 1/second) is the stiffness. Because acceleration is
proportional to the *velocity error*, this is a first-order exponential approach:
the velocity relaxes toward the desired one with a time constant of roughly
`1 / arriveResponse`. Larger = snappier, smaller = floatier. It's the reason the
motion feels alive rather than mechanical.

### 5b. Flee (run away from the finger)

Within a radius of the pointer, push *directly away*, strongest at the centre and
fading to nothing at the edge:

```
f = pointerDir_away · fleeStrength · (1 − dist / fleeRadius)
```

where `pointerDir_away = (position − pointer) / dist`. This carves a moving "hole"
in the shape around your finger; the arrive force immediately fights to refill it,
so dragging feels like pushing through sand that flows back.

### 5c. Escape (blow the field off-screen)

A phase switch. While escaping, we ignore the target entirely and give every
particle a desired velocity pointing **outward from the centre of the screen**:

```
outward   = (position − screenCentre) / |·|
desiredVel = outward · escapeSpeed
a          = (desiredVel − v) · escapeResponse
```

After ~1.15 s (`escapeDuration`) the particles are gone; we respawn them just
outside a random screen edge, advance to the next shape, and flip back to arrive —
so they stream back in and reassemble as a *different* word.

### 5d. Drag / damping, made frame-rate independent

We multiply velocity by a `drag` factor < 1 each step so the flee/escape impulses
don't accumulate forever. But a fixed per-frame multiply would damp twice as hard at
120 fps as at 60 fps. To make it independent of frame rate we raise it to the power
of "how many 60 fps frames this dt represents":

```kotlin
val frameDrag = drag.pow(dt * 60f)
v *= frameDrag
```

At exactly 60 fps (`dt = 1/60`) this is just `drag`; at 120 fps it's `√drag` applied
twice — same total damping per second either way. This "`x^(dt·60)`" pattern is the
general trick for any per-frame decay you want to be frame-rate independent.

---

## 6. Morphing between shapes

Morphing is almost free because of how targets are stored. All shapes are
**pre-sampled once** into a list of `(tx, ty)` arrays, all of length `N`. To morph,
we just `System.arraycopy` shape *k*'s target arrays into the live `tx/ty`:

```kotlin
fun nextShape() {
    shapeIndex = (shapeIndex + 1) % shapes.size
    copyTargetsFrom(shapeIndex)   // arraycopy into tx, ty
}
```

Particle *i* now steers from wherever it currently is toward its *new* slot *i* in
the next shape — the arrive behaviour turns that instantly-swapped goal into a
smooth flowing morph, no interpolation code required. Particle *i*'s slot in shape A
has no relationship to its slot in shape B (both are arbitrary sample orderings),
which is exactly what gives the pleasing "swirl and reform" look.

Pre-sampling happens **off the main thread** (`withContext(Dispatchers.Default)`)
because rasterising several bitmaps and scanning millions of pixels would jank the
UI if done inline.

---

## 7. Rendering — one `drawPoints` call

The whole point of SoA is that the positions are *already* in the layout the drawing
API wants. Android's `Canvas.drawPoints(float[] pts, Paint)` takes a flat array of
interleaved coordinates `[x0, y0, x1, y1, …]` and draws every point in one native
call. So each frame we stream positions into a reusable buffer:

```kotlin
val renderBuffer = FloatArray(count * 2)
// in update():
var k = 0
for (i in 0 until count) { renderBuffer[k++] = px[i]; renderBuffer[k++] = py[i] }
```

and draw it via the native canvas (Compose's `DrawScope` has `drawPoints(List<Offset>)`,
but the raw-`FloatArray` overload avoids building 5000 `Offset` objects per frame):

```kotlin
drawIntoCanvas { canvas ->
    canvas.nativeCanvas.drawPoints(engine.renderBuffer, paint)
}
```

The `Paint` is a **single** stroke paint with `strokeCap = ROUND`, so each "point"
renders as a round dot of `strokeWidth` diameter:

```kotlin
Paint().apply {
    style = Paint.Style.STROKE
    strokeCap = Paint.Cap.ROUND
    strokeWidth = pointSize
    color = /* one colour */
}
```

One paint + one array = **one draw call for the entire field**, regardless of how
many particles there are. That's the "single `drawRawPoints`" property. The trade-off
is uniformity: all points share a colour and size. If you want per-particle colour
you'd batch by colour (a few `drawPoints` calls) or move to a mesh/shader.

---

## 8. Wiring it into Compose

Three Compose concerns, kept minimal:

- **Size** — `Modifier.onSizeChanged { canvasSize = it }` gives the pixel size.
  The engine is `remember(canvasSize)`-keyed, so it (re)builds when the size is
  known/changes. Particle count scales with area: `area / 2600`, clamped to
  1500–5000.
- **Redraw** — the `frame` counter described in §2.
- **Gestures** — two `pointerInput` modifiers:
  - `detectTapGestures(onTap = nextShape, onLongPress = triggerEscape)`.
  - a raw `awaitPointerEventScope` loop that records the pressed pointer position
    into `pointer` state, which the frame loop feeds to the flee behaviour (and
    clears to "no touch" on release, encoded here as a negative coordinate).

Callbacks and the frame loop all run on the main thread, so they can poke the
engine's plain fields without synchronisation. Respecting `LocalAnimationsEnabled`
(the project's global "reduce motion" switch) simply skips starting the loop.

---

## 9. Tuning knobs (where to fiddle)

| Knob | File | Effect |
|------|------|--------|
| `maxSpeed` | engine | top travel speed (px/s) |
| `slowRadius` | engine | how early particles ease in — bigger = softer landing |
| `arriveResponse` | engine | arrive stiffness; big = snappy, small = floaty |
| `fleeRadius` / `fleeStrength` | engine | size / force of the touch "hole" |
| `escapeSpeed` / `escapeDuration` | engine | how hard/long the burst throws them |
| `drag` | engine | residual damping (frame-rate independent, §5d) |
| `count` formula (`area / 2600`) | composable | density vs. performance |
| `pointSize` | composable | dot diameter |
| `autoMorphSeconds` | composable | idle morph cadence (≤0 disables) |
| `shapes` | composable | the words/glyphs to cycle |

---

## 10. Performance notes

- **O(N) per frame.** Every behaviour here is per-particle, no pair-wise work — so
  5000 particles is ~5000 iterations/frame, trivial. (Contrast the *constellation*
  effect, which is O(N²) because it connects every pair.)
- **Zero per-frame allocation** in the hot path: flat arrays, primitive locals, one
  reused render buffer.
- **Sampling is the only expensive step**, done once up-front and off the main
  thread. Bitmaps are recycled immediately.
- The single `drawPoints` keeps the draw side cheap; the GPU cost is basically the
  number of pixels covered by dots.

---

## 11. Extending it

- **Images / icons:** feed a decoded `Bitmap` to an alpha-scan variant of
  `sampleText` — same candidate→particle mapping.
- **Per-particle colour:** add a `col: IntArray`, bucket particles by colour, and
  emit one `drawPoints` per bucket.
- **Nearest-slot morph** (less swirl, more "letters slide into place"): match each
  particle to the closest target of the next shape (e.g. a greedy or Hungarian
  assignment) instead of index-for-index.
- **More behaviours:** wander (random walk on a heading), gravity, orbit — each is
  just another additive contribution to `(ax, ay)` before integration.

---

## TL;DR recipe for your own version

1. Decide particle state; store it as **SoA `FloatArray`s**, not objects.
2. Run a **`withFrameNanos` loop**; compute a **clamped `dt`**; bump a `State` to
   redraw.
3. Each frame, per particle: sum up **behaviour accelerations** → `v += a·dt` →
   `p += v·dt`; apply **frame-rate-independent drag**.
4. Get target positions by **rasterising a shape and scanning alpha pixels**.
5. **Morph** by swapping the target arrays.
6. Draw everything in **one `drawPoints`** over an interleaved buffer.
