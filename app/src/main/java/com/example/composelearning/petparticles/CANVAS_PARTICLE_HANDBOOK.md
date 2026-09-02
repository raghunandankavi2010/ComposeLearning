# A handbook for interactive Canvas particle systems

**The maths, the framework internals, and the intuition — derived from first
principles, then applied.**

This is the companion to [`PETPARTICLES.md`](PETPARTICLES.md). That document is
a guided tour of *this* implementation. This one is the theory: everything you
need to design your own from scratch, on Android or anywhere else.

Nothing here is specific to a cat. Substitute a logo, a chart, a face, a map —
the pipeline and the equations are identical.

---

## Contents

**Part 0** — [How to read this](#part-0--how-to-read-this)
**Part I** — [The intuition](#part-i--the-intuition)
**Part II** — [The maths](#part-ii--the-maths)
 1. [The 2-D vector toolkit](#1-the-2-d-vector-toolkit)
 2. [From Newton to a frame loop](#2-from-newton-to-a-frame-loop)
 3. [Integrators, and why the line order matters](#3-integrators-and-why-the-line-order-matters)
 4. [Stability: the actual proof](#4-stability-the-actual-proof)
 5. [The damped spring, solved](#5-the-damped-spring-solved)
 6. [Frame-rate independence](#6-frame-rate-independence)
 7. [Force fields and falloff kernels](#7-force-fields-and-falloff-kernels)
 8. [Transforms: rotate, scale, and weight](#8-transforms-rotate-scale-and-weight)
 9. [Waves and ripples](#9-waves-and-ripples)
 10. [Sampling: turning art into points](#10-sampling-turning-art-into-points)
 11. [Colour quantisation and counting sort](#11-colour-quantisation-and-counting-sort)
 12. [Fast arithmetic](#12-fast-arithmetic)
 13. [Complexity, and when this design breaks](#13-complexity-and-when-this-design-breaks)

**Part III** — [The frameworks](#part-iii--the-frameworks)
 14. [The Android graphics pipeline](#14-the-android-graphics-pipeline)
 15. [Compose's three phases and the snapshot system](#15-composes-three-phases-and-the-snapshot-system)
 16. [The frame clock](#16-the-frame-clock)
 17. [Canvas, DrawScope, and batched draw calls](#17-canvas-drawscope-and-batched-draw-calls)
 18. [Pointer input](#18-pointer-input)
 19. [Escape hatches: when Compose is the wrong layer](#19-escape-hatches-when-compose-is-the-wrong-layer)

**Part IV** — [Design walkthrough](#part-iv--design-walkthrough)
**Part V** — [Transferring the technique](#part-v--transferring-the-technique)
**Part VI** — [Measuring and debugging](#part-vi--measuring-and-debugging)
**Appendix** — [Cheat sheet](#appendix--cheat-sheet)

---

# Part 0 — How to read this

Three things are being taught at once, and they are genuinely separable:

| Layer | Question it answers | Transfers to |
| --- | --- | --- |
| **Maths** | What equations produce this motion? | Every language, every platform, forever |
| **Architecture** | How do I organise 8 000 of anything so it's fast? | Any imperative language |
| **Framework** | How do I get Compose/Flutter/the browser to draw it 60× a second without fighting me? | That framework only |

The maths is the part with the longest shelf life; the framework details are the
part that goes stale. But the framework details are also the part that will
actually make your first attempt run at 4 fps, so don't skip them.

**Notation.** Vectors are bold: **p** = (x, y). `dt` is the timestep in seconds.
Subscript *n* is the frame index. Mass is 1 throughout, so force and
acceleration are the same number — a convention, not physics, and it saves a
division in the inner loop.

---

# Part I — The intuition

## What a particle system actually is

Strip away the vocabulary and a particle system is:

> A big array of numbers, and a function that runs once per frame turning the
> old numbers into slightly different new numbers.

That's it. The "system" is a loop. Everything interesting comes from what you
put in the array and what the function does.

The three questions that define one:

1. **Where do particles want to be?** (targets, homes, emitters)
2. **What pushes them away from that?** (forces, noise, your finger)
3. **How do you draw thousands of them without dying?** (batching)

Most tutorials answer (1) with "an emitter spits them out and they die", which
gives you fire and smoke. This handbook answers it with **"every particle has a
permanent home, sampled from a picture"**, which gives you dissolving logos,
dot-matrix portraits, and interactive creatures. The second answer is far more
useful for UI work and barely harder.

## The one idea that makes creatures possible

> **The artwork is data, not output.**

You draw a cat into a bitmap. You never show anybody that bitmap. Instead you
read its pixels and use them as the *target positions and colours* of a few
thousand dots.

Everything downstream then becomes generic. The physics engine has no idea it is
simulating a cat. Swap the artwork for a dog, an SVG, a decoded PNG, a text
glyph, a QR code, or a heat-map, and not one line of the engine changes.

```
  ART                SAMPLING              SIMULATION            RENDER
  something     →    point cloud:      →   per-particle     →    a handful of
  you can            home position         forces + an           batched draw
  rasterise          + colour + tags       integrator            calls
  ─────────          ──────────────        ─────────────         ────────────
  bespoke            generic               generic               generic
  (5% of code)       (25%)                 (50%)                 (20%)
```

Notice the last row. Only the first stage is ever bespoke. That ratio is why
learning this once pays off repeatedly.

## The second idea: springs, not animations

The instinct from UI work is to *animate* particles: "over 400 ms, tween from A
to B with an ease-out curve." Resist it. Tweens have three fatal properties for
this kind of work:

* they need a **start and an end**, so they can't respond to a finger arriving
  mid-flight;
* they need a **duration**, so simultaneous influences fight over who owns the
  value;
* they are **not composable** — you cannot add two tweens together and get
  something sensible.

Forces have none of those problems. A force is just a number you add to an
accumulator. Ten different influences? Add all ten. A finger arrives halfway
through the motion? Add its force too; the spring absorbs it. There is no
"current animation" to interrupt because there is no animation — there is a
state that is always evolving.

> Tweens describe a *path*. Forces describe a *tendency*. Interactive things
> need tendencies.

## The third idea: weights, not branches

To make the tail wag but not the body, the obvious code is:

```kotlin
if (isTail(i)) { rotate(i) }        // ← don't
```

This produces a visible seam: the tail rotates, the body doesn't, and the joint
tears. The fix is to replace the boolean with a **continuous weight** in `[0,1]`
that ramps across the boundary:

```kotlin
wag[i] = (distanceFromPivot / 0.32f).coerceIn(0f, 1f)
rotate(i, angle * wag[i])           // ← do
```

Now particles near the pivot barely move, particles at the tip swing fully, and
the transition between them is continuous. As a bonus you get *bending* for
free — a rough approximation of a jointed chain — which a rigid rotation would
never give you.

**Generalise:** every "which particles does this apply to?" question should be
answered with a float, not a bool. Head bob, breathing, glow, susceptibility to
wind — all weights.

## The fourth idea: batching is not an optimisation

It's the architecture. If your renderer is `for (p in particles) drawCircle(p)`,
no amount of tuning saves you: you have already lost. The whole data layout —
structure-of-arrays, counting sort by colour, an interleaved position buffer —
exists to serve a single constraint:

> **Draw call count must be independent of particle count.**

Design backwards from that and everything else falls into place.

---

# Part II — The maths

## 1. The 2-D vector toolkit

Everything in Part II is built from six operations. In an SoA layout you write
them out by hand on `FloatArray`s rather than allocating vector objects, so
learn them as *formulas*, not as an API.

| Operation | Formula | What it's for |
| --- | --- | --- |
| difference | **d** = **a** − **b** = (aₓ−bₓ, a_y−b_y) | "which way from b to a" |
| length | ‖**d**‖ = √(dₓ² + d_y²) | distance |
| length² | ‖**d**‖² = dₓ² + d_y² | **comparing** distances (no `sqrt`) |
| normalise | **d̂** = **d** / ‖**d**‖ | pure direction, length 1 |
| scale | s·**d** = (s·dₓ, s·d_y) | "this much, in that direction" |
| perpendicular | **d**^⊥ = (−d_y, dₓ) | rotate 90° CCW — the 2-D "curl" |

### Why length² is the single most useful trick

`sqrt` is not catastrophic on modern hardware, but it is 10–20× a multiply, and
in an inner loop over 8 000 particles it adds up. The observation:

> √ is **monotonically increasing** on non-negative reals, so for `a, b ≥ 0`:
> `a < b ⟺ a² < b²`.

Any time you only need to *compare* a distance — "is this within the radius?",
"which palette colour is nearest?", "is this faster than the speed cap?" — you
can work entirely in squared units and never call `sqrt`:

```kotlin
if (d2 < touchRadiusSq && d2 > 1e-4f) {   // reject 95% of particles, no sqrt
    val d = sqrt(d2)                       // only now, for the survivors
```

The `1e-4f` lower guard matters as much as the upper one. It is there because
the next line divides by `d`, and a particle exactly under your fingertip would
otherwise produce `0/0 = NaN` — and one NaN position poisons every subsequent
frame, because `NaN` propagates through every arithmetic operation and compares
false to everything. A single unguarded division is the classic way to make an
entire particle field silently vanish.

### The perpendicular operator

`(−d_y, dₓ)` rotates a vector 90° counter-clockwise. It is the whole of 2-D
"curl":

```
        d = (1, 0)                d^⊥ = (0, 1)
             →                          ↑
```

If **d̂** points *away* from your finger, then **d̂**^⊥ points *around* it. Adding
a little of it to a repulsion force turns "push" into "swirl" — matter flows
around the obstacle instead of straight off it. In 3-D this would be a cross
product with the axis; in 2-D it collapses to swapping components and negating
one, which costs nothing.

---

## 2. From Newton to a frame loop

### The physics

Newton's second law: **F** = m**a**. Set m = 1:

```
a = F           acceleration is just the total force
v' = a          velocity is the integral of acceleration
p' = v          position is the integral of velocity
```

We have a second-order ODE (`p'' = F(p, v, t)`) which we split into two coupled
first-order ODEs by treating **v** as its own state variable. That split is the
standard move and it's why every particle stores both position *and* velocity.

### Why you must store velocity

A common beginner design stores only position and lerps toward a target:

```kotlin
p += (target - p) * 0.1f          // exponential approach — no velocity
```

This is stable, cheap, and dead. It cannot overshoot, so it can never bounce,
never carry momentum, never be flung. It's a *filter*, not a simulation. The
moment you want a finger flick to send fur flying and have it settle back, you
need momentum, and momentum lives in **v**.

(This is exactly the difference between `animate*AsState` with a `tween` and
with a `spring` — the spring keeps velocity, which is why it can be interrupted
gracefully mid-flight and a tween can't.)

### Units, and why they should be seconds

Express every constant in **per-second** units, never per-frame:

| quantity | unit | example from the engine |
| --- | --- | --- |
| position | px | `hx[i]` |
| velocity | px/s | `maxSpeed = fit * 4.5f` |
| acceleration | px/s² | `pushStrength = 26_000f` |
| stiffness `k` | 1/s² | `340f` |
| damping `c` | 1/s | `26.55f` |

Then multiply by `dt` at integration time and the simulation behaves identically
at 60 Hz, 90 Hz, 120 Hz and during frame drops. Per-frame constants (`v *= 0.9`)
silently make your animation twice as fast on a 120 Hz phone. See §6.

A useful sanity check: `pushStrength = 26 000 px/s²` sounds enormous until you
notice it's applied for one frame (`dt ≈ 0.016 s`), giving a velocity kick of
~420 px/s, and it's multiplied by a falloff that is usually well below 1. Always
sanity-check accelerations by mentally multiplying by `dt`.

---

## 3. Integrators, and why the line order matters

We need to turn continuous ODEs into discrete steps. Start from the Taylor
expansion:

```
p(t + dt) = p(t) + dt·p'(t) + (dt²/2)·p''(t) + O(dt³)
```

Truncate after the linear term and you get **Euler's method**. Its local error
is O(dt²) per step, and over a fixed time interval (`1/dt` steps) the global
error is O(dt) — a *first-order* method. All the integrators below are first
order in accuracy; what separates them is **stability**, which matters far more.

### Explicit (forward) Euler — the wrong one

```kotlin
p += v * dt        // position first, using the OLD velocity
v += a * dt
```

### Semi-implicit (symplectic) Euler — the right one

```kotlin
v += a * dt        // velocity first…
p += v * dt        // …then position, using the NEW velocity
```

**Two lines, swapped. Same cost. Same accuracy order. Categorically different
behaviour on oscillators.** This is the highest value-per-character change in
the entire document.

### Why swapping matters — the geometric answer

Write one step as a matrix acting on the state (x, v) for the undamped spring
`a = −k·x`:

**Explicit:**
```
x_{n+1} = x_n + dt·v_n
v_{n+1} = v_n − k·dt·x_n
                              ⎡  1     dt ⎤
                        M  =  ⎢           ⎥      det M = 1 + k·dt²
                              ⎣ −k·dt   1 ⎦
```

**Semi-implicit:**
```
v_{n+1} = v_n − k·dt·x_n
x_{n+1} = x_n + dt·v_{n+1} = (1 − k·dt²)·x_n + dt·v_n
                              ⎡ 1 − k·dt²   dt ⎤
                        M  =  ⎢                ⎥   det M = 1
                              ⎣   −k·dt      1 ⎦
```

The determinant of the step matrix is the factor by which it scales areas in
phase space (the x–v plane). For a conservative system, phase-space area is a
conserved quantity — Liouville's theorem.

* Explicit Euler: **det = 1 + k·dt² > 1**. Every single step inflates phase-space
  area. Energy is injected from nowhere, the oscillation grows, and the
  simulation explodes. This happens *for any dt*, no matter how small — small
  steps only delay it.
* Semi-implicit Euler: **det = 1 exactly**. Area is preserved. This is what
  "symplectic" means. Energy is not exactly conserved, but it oscillates around
  the true value within a bounded band forever instead of drifting.

That is the whole story. It's not a heuristic or a tuning trick; it's a
structural property of the two-line ordering.

### Verlet, and when to reach for it

Position Verlet stores the previous position instead of velocity:

```
p_{n+1} = 2·p_n − p_{n−1} + a·dt²
```

It's also symplectic, second-order accurate, and beautifully suited to
*constraints* (cloth, rope, rigid links), because you can just move positions
and the velocity implicitly follows. Its weakness is that velocity is not
directly available, which makes velocity-dependent forces like damping and drag
awkward — and damping is central to this design. **Use Verlet for constraint
systems; use semi-implicit Euler for force systems.**

### RK4?

Runge–Kutta 4 is fourth-order accurate and the default in scientific computing.
For real-time graphics it is usually the wrong tool: it costs 4 force
evaluations per step, is *not* symplectic (it slowly loses energy on
oscillators), and buys accuracy you cannot see. Accuracy is not the goal here —
plausible, stable, cheap motion is.

---

## 4. Stability: the actual proof

You will hear "clamp your `dt`" as folklore. Here is where the number comes
from, so you can compute it for your own constants instead of guessing.

### Setup

Take the damped spring, mass 1:

```
a = −k·x − c·v
```

One semi-implicit step:

```
v_{n+1} = (1 − c·dt)·v_n − k·dt·x_n
x_{n+1} = x_n + dt·v_{n+1} = (1 − k·dt²)·x_n + dt·(1 − c·dt)·v_n
```

So the step matrix is

```
      ⎡ 1 − k·dt²    dt·(1 − c·dt) ⎤
M  =  ⎢                            ⎥
      ⎣   −k·dt         1 − c·dt   ⎦

  trace  T = 2 − k·dt² − c·dt
  det    D = 1 − c·dt
```

(Check `D`: (1−k·dt²)(1−c·dt) + k·dt²(1−c·dt) = (1−c·dt)·[(1−k·dt²) + k·dt²] = 1−c·dt. ✓)

### The criterion

The simulation is stable iff both eigenvalues of `M` lie inside the unit
circle. For a real quadratic `λ² − T·λ + D = 0`, the **Jury conditions** give:

```
|D| < 1          and          |T| < 1 + D
```

Expand both with our `T` and `D`:

**Condition 1:** `|1 − c·dt| < 1` ⟹ `dt < 2/c`

**Condition 2:** `|2 − k·dt² − c·dt| < 2 − c·dt`
 * upper branch: `−k·dt² < 0` — always true
 * lower branch: `k·dt² + 2c·dt − 4 < 0`, a quadratic in `dt`, positive root:

```
                −c + √(c² + 4k)
        dt  <  ─────────────────
                       k
```

### The undamped special case

Set `c = 0`: condition 1 vanishes, condition 2 gives `dt < √(4/k) = 2/√k = 2/ω`.
**This is the famous `dt < 2/ω` bound — and it is the *loosest* case.** Adding
damping makes the real limit *tighter*, not looser, which is the opposite of
most people's intuition. If you only remember `2/ω`, you will eventually ship
something that explodes.

### Plugging in this engine's numbers

`k = 340`, `ζ = 0.72`, so `ω = √340 = 18.44 rad/s` and `c = 2ζω = 26.55 /s`.

| bound | value |
| --- | --- |
| undamped `2/ω` | 0.1085 s ← the misleading one |
| condition 1, `2/c` | 0.0753 s |
| condition 2 (binding) | `(−26.55 + √(704.9 + 1360))/340` = **0.0556 s** |
| **clamp in the code** | **0.0333 s** ← 1.7× safety margin |

Verify at the clamp: `T = 2 − 340(0.0333²) − 26.55(0.0333) = 0.754`,
`D = 1 − 0.884 = 0.116`, and `|0.754| < 1.116` ✓, `|0.116| < 1` ✓. Stable, with
room to spare.

### What to actually do about it

```kotlin
val dt = ((now - last) / 1_000_000_000f).coerceIn(0f, 0.033f)
```

This is not defensive paranoia. `dt` will exceed your bound in production, and
not rarely:

* the app returns from background and the first `dt` is *seconds*;
* a debugger breakpoint;
* a GC pause or a cold JIT on the first few frames;
* the user drags the notification shade over your screen.

The clamp reinterprets a dropped frame as a moment of **slow motion** rather
than a real-time jump. For decorative animation that is always the right call —
and it is invisible, because by definition the user didn't see the frames you're
declining to catch up on.

> If you genuinely need large steps (a physics sim that must stay in sync with
> wall-clock time), the answer is **sub-stepping**: run the integrator ⌈dt/h⌉
> times with `h` inside the stability bound. Cost scales with the number of
> sub-steps, which is why UI work clamps instead.

Also clamp velocity, as a second line of defence:

```kotlin
val sp2 = nvx*nvx + nvy*nvy
if (sp2 > maxSpeedSq) { val k = maxSpeed / sqrt(sp2); nvx *= k; nvy *= k }
```

Note this preserves *direction* while capping *magnitude* — clamping the
components independently would bend the velocity toward the diagonals.

---

## 5. The damped spring, solved

This one equation carries the entire feel of the system, so it's worth knowing
completely rather than tuning by trial and error.

### The ODE and its canonical form

```
x'' + c·x' + k·x = 0
```

Rewrite with the two parameters that actually mean something:

```
x'' + 2ζω·x' + ω²·x = 0

        ω = √k          natural (undamped) angular frequency, rad/s
        ζ = c / (2√k)   damping ratio, dimensionless
   ⟹    c = 2ζ√k        ← this is the line in the code
```

```kotlin
private val damping = 2f * dampingRatio * sqrt(stiffness)
```

**ζ is the only number you should reason about.** It is dimensionless and
describes the *character* of the motion; `ω` describes the *speed*. They are
independent knobs, which is exactly what you want, and they are not independent
if you tune `k` and `c` directly.

### Solving it

Substitute `x = e^{rt}`:

```
r² + 2ζω·r + ω² = 0
r = −ζω ± ω√(ζ² − 1)
```

The sign of `ζ² − 1` gives three regimes:

| ζ | roots | behaviour | feel |
| --- | --- | --- | --- |
| `0` | ±iω | pure oscillation, forever | a bell |
| `0 < ζ < 1` | complex pair | **underdamped**: rings, decaying | bouncy, alive |
| `ζ = 1` | double real `−ω` | **critically damped**: fastest with no overshoot | crisp, mechanical |
| `ζ > 1` | two real negatives | **overdamped**: crawls in | heavy, syrupy |

For the underdamped case (the useful one):

```
x(t) = A·e^{−ζω·t}·cos(ω_d·t − φ)          where   ω_d = ω√(1 − ζ²)
       └──── envelope ────┘ └── ringing ──┘
```

Two separable pieces: an exponentially shrinking **envelope** that sets how
quickly it settles, and a **ringing** term that sets how it wobbles on the way.

### The three design formulas

These are what you actually use. Decide the feel, then solve for the constants.

```
                                        4                    ln(50)
   2 % settling time          t_s  ≈  ─────      (exactly  ──────── )
                                       ζ·ω                    ζ·ω

                                      ⎛   −π·ζ   ⎞
   overshoot fraction         M_p  =  exp⎜ ───────── ⎟
                                      ⎝  √(1−ζ²) ⎠

   damped period              T_d  =  2π / (ω√(1 − ζ²))
```

Worked, with this engine's `k = 340`, `ζ = 0.72`:

```
ω   = √340                     = 18.44 rad/s
c   = 2 · 0.72 · 18.44         = 26.55 /s
t_s = 3.912 / (0.72 · 18.44)   = 0.29 s      ← settles in about a third of a second
M_p = exp(−π·0.72 / 0.694)     = 0.038       ← 3.8 % overshoot: a hint of bounce
ω_d = 18.44 · 0.694            = 12.80 rad/s
T_d = 2π / 12.80               = 0.49 s      ← one full wobble, if it wobbled twice
```

So `stiffness = 340, dampingRatio = 0.72` is not a magic pair discovered by
fiddling. It is the answer to *"come home in about 0.3 s with a barely-visible
4 % overshoot"*. **Design in those terms and invert the formulas.**

Going the other way, if you want a target settling time `t_s` and ratio `ζ`:

```
ω = 4 / (ζ · t_s)        k = ω²        c = 2ζω
```

### Choosing ζ, in practice

| ζ | use it for |
| --- | --- |
| 0.2 – 0.4 | playful, jelly, obviously bouncy — toys, games, celebration moments |
| 0.5 – 0.8 | **most UI**: alive but not silly. Compose's `MediumBouncy` is 0.5 |
| 1.0 | crisp and mechanical, no overshoot. Compose's `NoBouncy` |
| 1.5+ | deliberately heavy, weighted, expensive-feeling |

### This is exactly Compose's animation spec

```kotlin
spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
```

Same two parameters, same meaning, same underlying ODE — Compose solves it
analytically per-value instead of integrating, but the mental model transfers
perfectly in both directions. If you understand `ζ` here, you understand
`animateFloatAsState` there, and vice versa.

### Why not just use Compose's `Animatable` for each particle?

Because you'd need 8 000 of them, each with its own coroutine, animation spec,
and snapshot state. The whole point of the SoA engine is to run the same maths
for the cost of eight float arrays. `Animatable` is the right tool for *one*
value that a *human* is watching; a hand-rolled integrator is right for
thousands that only exist in aggregate.

---

## 6. Frame-rate independence

Any time you write a multiplier that runs "each frame", you have written a bug
that only appears on hardware you don't own.

### The problem

```kotlin
v *= 0.9f      // "10 % drag per frame"
```

At 60 Hz that's `0.9^60 = 0.0018` per second. At 120 Hz it's `0.9^120 = 3.3e-6`.
Your animation is *twice as damped* on a flagship phone. Same code, different
feel, and it will be reported as "the animation feels wrong on the Pixel".

### The fix, derived

Exponential decay is `v(t) = v₀·e^{−λt}`. If the intended per-frame factor is
`r` at a reference step `h` (usually 1/60 s), then `r = e^{−λh}`, so
`λ = −ln(r)/h`, and for an arbitrary step `dt`:

```
                                    dt/h
   v(t + dt) = v(t) · e^{−λ·dt} = r
```

In code:

```kotlin
val k = Math.pow(0.86, (dt * 60f).toDouble()).toFloat()
vx *= k
```

`0.86` now means *"0.86 per 60 Hz frame"* at any actual refresh rate. The `pow`
is per-frame, not per-particle, so its cost is irrelevant.

### The same bug in disguise: lerp smoothing

```kotlin
x += (target - x) * 0.2f          // ← frame-rate dependent, same disease
```

The correct form is `α' = 1 − (1 − α)^{dt·60}`. This one is everywhere — camera
follow code, smoothed scroll indicators, colour crossfades — and it is almost
always wrong in tutorials.

### Why the spring doesn't need this treatment

Because it is expressed in per-second units and multiplied by `dt` at
integration time, the damped spring is *already* frame-rate independent by
construction. That is the deeper argument for using forces and `dt` rather than
per-frame multipliers: correctness comes for free instead of being bolted on.

The pointer velocity decay in `PointerTracker` is the one place this system uses
a per-frame multiplier, and it uses the `pow` form — because there it's a
*filter*, not physics.

---

## 7. Force fields and falloff kernels

### The general shape

Every "influence within a radius" force has the same three parts:

```kotlin
val d2 = dx*dx + dy*dy
if (d2 < R2 && d2 > eps) {         // 1. cheap rejection test, squared
    val d = sqrt(d2)
    val nx = dx/d; val ny = dy/d   // 2. direction
    val w = kernel(d / R)          // 3. weight: 1 at centre → 0 at the rim
    ax += nx * strength * w
}
```

The *direction* decides what the force does. The *kernel* decides how it feels.

### The kernel catalogue

Let `t = 1 − d/R`, so `t = 1` at the centre and `t = 0` at the rim.

| kernel | w(t) | w′ at rim | cost | character |
| --- | --- | --- | --- | --- |
| step | `1` | ∞ | free | hard edge, obviously fake |
| linear | `t` | −1/R | ~free | **visible circular rim** |
| quadratic | `t²` | 0 | 1 mul | soft, cheap — **used here** |
| cubic | `t³` | 0 | 2 mul | softer, more concentrated |
| smoothstep | `t²(3−2t)` | 0 | 3 ops | soft at *both* ends |
| Gaussian | `e^{−d²/2σ²}` | ~0 | `exp` | softest, no true cutoff |
| inverse-square | `1/(d²+ε)` | — | 1 div | physical (gravity/charge) |

### Why the derivative at the rim is what matters

This is the single most useful piece of aesthetic maths in the document.

With a **linear** kernel, at the boundary the weight is 0 but the *slope* is
−1/R. As your finger sweeps across the field, a particle crossing `d = R` goes
from receiving nothing to receiving a force that grows at a constant rate. Your
eye reads that discontinuity in the derivative as a **hard circular edge**
following your finger — even though the force itself is continuous.

With a **quadratic** kernel, `w(t) = t²`, so `w′(t) = 2t`, which is 0 at `t = 0`
(the rim). Both the value *and* the slope vanish. Influence fades in
imperceptibly and the circle disappears.

```
   linear t                      quadratic t²
   1 ┤●                          1 ┤●
     │ ╲                           │ ╲
     │  ╲                          │   ╲
     │   ╲                         │      ╲___
   0 ┤    ╲___                   0 ┤          ╲___
     └──────────  d=R              └──────────  d=R
       slope −1 at rim               slope 0 at rim
       ↑ you can SEE this            ↑ invisible
```

> **The rule:** match the derivative at the boundary, not just the value. This
> is `C¹` continuity, and it's the same reason `smoothstep` looks better than a
> linear ramp for opacity fades, and why easing curves have zero slope at their
> endpoints.

### Deriving smoothstep, since you'll want it

Find the lowest-degree polynomial with `f(0)=0, f(1)=1, f′(0)=f′(1)=0`. That's
four constraints, so a cubic `at³+bt²+ct+d`:

```
f(0)=0  ⟹ d=0
f′(0)=0 ⟹ c=0
f(1)=1  ⟹ a+b=1
f′(1)=0 ⟹ 3a+2b=0
        ⟹ a=−2, b=3

f(t) = 3t² − 2t³ = t²(3 − 2t)
```

The engine uses it for the head-bob weight, with the edges given *reversed* to
invert the ramp:

```kotlin
bob[i] = smoothstep(0.50f, 0.30f, uy)     // 1 at the top of the head, 0 below the neck
```

(`smootherstep`, `6t⁵−15t⁴+10t³`, additionally zeroes the second derivative —
useful when a smoothstep's acceleration jump is visible, e.g. in camera moves.)

### The three-force touch model

One repulsion force feels like a hole punched in cloth. Three forces feel like
fur:

```kotlin
// 1. PUSH — radial, away from the finger.  "make room"
ax += nx * pushStrength * ff
ay += ny * pushStrength * ff

// 2. SWIRL — perpendicular, scaled by finger speed.  "curl around"
ax += -ny * swirlNow * ff
ay +=  nx * swirlNow * ff

// 3. ADVECTION — along the finger's velocity.  "get carried"
ax += touchVx * advection * ff
ay += touchVy * advection * ff
```

* **Push** alone is a force field. Correct, lifeless.
* **Swirl** is the perpendicular from §1. Scaled by pointer speed
  (`min(speed/(3·fit), 1)`) so a slow touch parts the fur and a fast swipe curls
  it into a vortex. This is what makes the motion look *fluid*.
* **Advection** is the one everyone forgets, and the one that sells it. Real
  matter you push moves *in the direction you pushed*, not merely away from you.
  Without it, dragging left and dragging right produce identical results, and
  the brain notices immediately even if it can't say why.

All three share the same `ff` kernel, so they fade out together and the field
has exactly one visible boundary — a soft one.

### Filtering the pointer velocity

Raw deltas between pointer events are violently spiky (irregular event timing,
integer coordinates, finger tremor). Feeding them straight into a force
produces jitter. Use a one-pole low-pass (exponential moving average):

```kotlin
vx += ((nx - x) / dt - vx) * 0.35f
```

This is a first-order IIR filter: `y_n = y_{n−1} + α(u_n − y_{n−1})`. `α = 0.35`
trades smoothness against lag; lower is smoother and laggier. It also needs a
decay (§6), because **when a finger is held still, no events arrive at all** and
a naive tracker would report the last velocity forever.

---

## 8. Transforms: rotate, scale, and weight

Idle life — breathing, bobbing, wagging — is applied to the **home position**
before the spring is evaluated, not to the particle position. That's important:
it means the springs do all the smoothing for you, and the idle motion can be
mathematically crude without looking crude.

```
  home  ──▶ breathe ──▶ bob ──▶ wag ──▶ shimmer ──▶ live target
                                                         │
                                                    spring pulls
                                                    particle here
```

### Scaling about a pivot

```
p' = pivot + (p − pivot)·s
```

Translate to the origin, scale, translate back. Used for breathing:

```kotlin
val breath = 1f + 0.014f * sin(time * 1.65f)
tx = basePivotX + (tx - basePivotX) * breath
ty = basePivotY + (ty - basePivotY) * breath
```

Two decisions worth copying:

* **Pivot at the base, not the centre.** Scale about the centre and the pet
  appears to levitate — the feet move. Scale about the ground contact and it
  breathes.
* **1.4 % amplitude.** Deliberately tiny. Past ~3 % it stops reading as
  breathing and starts reading as a pulsing blob. Subtlety is the effect.

### Rotation about a pivot

The 2-D rotation matrix:

```
      ⎡ cos θ   −sin θ ⎤              x' = x·cos θ − y·sin θ
R  =  ⎢                ⎥              y' = x·sin θ + y·cos θ
      ⎣ sin θ    cos θ ⎦
```

About an arbitrary pivot, same translate–apply–translate sandwich:

```kotlin
val rx = tx - pivotX
val ry = ty - pivotY
tx = pivotX + rx * c - ry * s
ty = pivotY + rx * s + ry * c
```

### Weighted rotation gives you bending for free

The per-particle weight from Part I turns a rigid rotation into a bend:

```kotlin
val a = wagNow * wag[i]         // ← angle scaled per particle
```

Because `wag[i]` grows with distance from the pivot, particles near the root
barely rotate while the tip swings fully. The result approximates a jointed
chain, or a continuum beam, without simulating either. Cost: one extra multiply
and two table lookups.

This generalisation is worth internalising: **a weighted transform is a cheap
approximation of a soft body.** Ears, hair, cloth edges, antennae, flags — all
of them can be a `sin` and a weight before you reach for a real solver.

### Shimmer, and the importance of per-particle phase

```kotlin
val ph = phase[i] + time * 1.9f
tx += sin(ph)          * shimmer
ty += cos(ph * 0.87f)  * shimmer
```

Two details do all the work:

* **`phase[i]` is random per particle.** With a shared phase, every dot moves
  identically and you see the *whole image vibrating*. With random phases you
  see a *living coat*. Same equation, completely different percept.
* **`0.87` is irrational-ish.** Because the x and y frequencies aren't in a
  simple ratio, the little Lissajous orbits never close, so the motion never
  visibly repeats. A ratio of 1.0 gives diagonal lines; 2.0 gives figure-eights.

### Order of operations

Transforms don't commute. The engine applies scale → translate → rotate →
offset, and that ordering is a design choice: breathing scales the *whole* pet
including the tail, then the tail rotates within the already-scaled frame. Swap
them and the tail would wag about a pivot that isn't where the tail is, and the
root would detach on the breath.

---

## 9. Waves and ripples

A wave is a function of `(d − r)`, **not** of `d`. That single observation is
the whole implementation.

```kotlin
rippleR += rippleSpeed * dt                      // the radius grows with time
val band = 1f - abs(d - rippleR) * invBand       // triangular shell
if (band > 0f) { ax += dx/d * band * force }
```

* A function of `d` alone is a static field — it affects everything inside a
  radius, always.
* A function of `d − r(t)` is a **travelling shell**. Only particles near the
  current radius feel it. Everything else is untouched. That is what makes it
  read as a wave passing through rather than a blob expanding.

### Shell profiles

| profile | formula | notes |
| --- | --- | --- |
| triangular | `1 − |d−r|/σ` | 1 abs, 1 mul, 1 compare — **used here** |
| Gaussian | `exp(−((d−r)/σ)²)` | prettier on paper, needs `exp`, indistinguishable in motion |
| sinc / ringed | `sin(k(d−r))·e^{−...}` | gives trailing ripples, like water |

At 60 fps and 8 000 particles, the triangle is the right call — you cannot see
the corner of a triangular profile when the shell is sweeping past at
`1.9 × fit` px/s.

### Amplitude decay

```kotlin
val rippleFade = 1f - rippleR / rippleMax
```

Without this the wave would stop abruptly at the edge of its range, which reads
as a bug. Linear decay is fine; physically, a 2-D circular wave's amplitude
falls as `1/√r` (energy spread over a growing circumference), so use
`1/sqrt(r)` if you want it to look physically right rather than deliberately
choreographed.

---

## 10. Sampling: turning art into points

### The wrong instinct

> "Render the artwork at screen resolution and pick N random opaque pixels."

Three problems, all avoidable:

1. `getPixels` on a full-screen bitmap allocates ~8 MB (1080×2000 ints).
2. Uniform random sampling **clumps**.
3. A fixed `N` gives inconsistent visual density across devices.

### Fix 1 & 3: choose the resolution, not the count

Render the mask at *exactly the density you want particles at*:

```kotlin
fun maskSizeFor(fitPx: Float, targetSpacing: Float = 6.2f): Int =
    (fitPx / targetSpacing).toInt().coerceIn(90, 190)
```

Now **one opaque mask pixel = one particle**, and:

* coverage is perfectly even with *no sampling algorithm at all*;
* **visual** density is constant across devices — a 6 px gap looks the same on a
  phone and a tablet, whereas a fixed count looks sparse on the big screen;
* `getPixels` reads 129² = 16 641 ints ≈ 66 KB instead of 8 MB;
* particle count is bounded automatically by the `coerceIn`.

That last point is the one people miss. **Density is the perceptual constant;
count is a consequence.** Design in the units your eye actually measures.

### Fix 2: why uniform random clumps — with numbers

Uniform random points form a spatial **Poisson process**. Divide your area into
cells with, on average, one point each. The number of points per cell is
Poisson(1), so:

```
P(cell is empty)      = e^{−1} = 36.8 %       ← holes
P(cell has exactly 1) = e^{−1} = 36.8 %
P(cell has 2 or more) = 1 − 2e^{−1} = 26.4 %  ← clumps
```

**Over a third of your cells are empty and a quarter are doubled up.** That is
not a subtle statistical nicety — it's the visible difference between "dust" and
"a coat of fur". The mean nearest-neighbour distance is `1/(2√λ)`, but the
*distribution* has all its mass near zero: `P(nearest < r) = 1 − e^{−λπr²}`,
which is linear in `r²` near the origin. Points love to sit on top of each other.

A perfect grid has the opposite failure: too regular, visible lattice, moiré
against the pixel grid.

### The fix: jittered grid (stratified sampling)

One sample per cell, displaced by up to half a cell:

```kotlin
ux[dst] = (xs[src] + rng.nextFloat() - 0.5f) * inv
```

Now the per-cell count is *exactly* 1 — variance zero — while positions are
still random. You get grid-like uniformity with random-like appearance.

```
   uniform random          jittered grid            perfect grid
   ·  ··      ·            ·  ·  ·  ·  ·           ·  ·  ·  ·  ·
      ·   ···                ·  ·  ·  ·  ·         ·  ·  ·  ·  ·
   ··          ·           ·  ·   ·  ·  ·          ·  ·  ·  ·  ·
     ·  ·    ··              · ·  ·  ·  ·          ·  ·  ·  ·  ·
   clumps + holes          even, organic           lattice, moiré
```

This is a poor-man's **blue noise** — a point set whose Fourier spectrum has
little low-frequency energy, i.e. no clumping at any scale. Proper blue noise
(Poisson-disc via Bridson's algorithm, or void-and-cluster) is better and costs
orders of magnitude more to generate. At 6 px spacing you will not see the
difference. Reach for real Poisson-disc only when spacing is large enough that
individual gaps are legible.

### Tagging body parts

Colour identifies *material*, but not *anatomy* — the tail is the same colour as
the body. So render a second mask with only the part you care about, in flat
white, and read it as a 1-bit channel:

```kotlin
tail[n] = if ((tailPixels[row + x] ushr 24) >= ALPHA_CUTOFF) 1 else 0
```

**The occlusion trap.** In the full portrait the body is painted *over* the tail
root, so the naive tail mask also tags a slice of the flank — and rotating those
with the tail visibly shears the body. The fix is to erase the occluder from the
tag mask:

```kotlin
private fun Canvas.punchOut(shape: Path) {
    val eraser = Paint(ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }
    drawPath(shape, eraser)
}
```

Generalise: **one boolean mask per articulated part, minus whatever occludes
it.** Each becomes a float weight in the engine (§8).

### Decimating to a common count

To morph between two shapes, both clouds need the same particle count. Trim both
to `min(n₁, n₂)` by **stride decimation in raster order**:

```kotlin
private fun pick(i: Int, target: Int): Int =
    if (target >= n) i else (i.toLong() * n / target).toInt()
```

Striding spreads the thinning evenly across the body; dropping a random subset
would leave visible bald patches (same Poisson argument as above, one level up).

Note the `toLong()`. `i * n` for 20 000 candidates and 9 000 targets is 180
million — fine in `Int`, but one density bump away from silent overflow and a
corrupted cloud. Widen the intermediate whenever you multiply two
data-dependent sizes.

---

## 11. Colour quantisation and counting sort

### Nearest-palette snapping

```kotlin
val d = dr*dr + dg*dg + db*db      // squared Euclidean distance in RGB
```

Three things to know:

* **No `sqrt`** — we're comparing, not measuring (§1).
* **RGB Euclidean distance is not perceptual.** The perceptually correct answer
  is ΔE in CIE Lab space, which requires a gamma-decode and a nonlinear
  transform per pixel. Here it doesn't matter: the artwork is painted in flat
  palette colours, so the vast majority of pixels match *exactly* (distance 0)
  and only anti-aliased seams need the nearest-neighbour fallback.
* **Keep palette entries far apart.** The first draft had a pink nose `#F08A96`
  and pink inner ears `#EE9C99`. Squared distance ≈ 340 — close enough that
  anti-aliased pixels flipped between them at random, producing speckle. They
  were merged into one tone. **Rule of thumb: keep squared RGB distance above
  ~2000** (≈45 per channel) between any two palette entries.

This is also why the artwork must use **flat fills, no gradients**. A gradient
would smear particles across every bucket, which destroys the sort below and
therefore the batching.

### Counting sort — the twelve lines that buy batched rendering

Particles are stored **sorted by tone**, so tone `b` owns the contiguous slice
`[bucketStart[b], bucketStart[b+1])`, which is exactly the argument list of one
draw call.

Sorting by a small integer key needs no comparisons. Counting sort is O(n + k)
in three passes:

```kotlin
// 1. histogram
val counts = IntArray(k)
for (i in 0 until target) counts[tone[pick(i, target)]]++

// 2. prefix sum → the bucket layout
val bucketStart = IntArray(k + 1)
for (b in 0 until k) bucketStart[b + 1] = bucketStart[b] + counts[b]

// 3. scatter, with a per-bucket write head
val cursor = bucketStart.copyOf()
for (i in 0 until target) { val dst = cursor[tone[src]]++ ; … }
```

Compare: comparison sorts are Ω(n log n); with n = 9 000 and k = 9, counting
sort is ~7× fewer operations and, more importantly, **branch-free and
cache-friendly**.

Because tone membership never changes at runtime, this runs **once, off the main
thread, and the offsets stay valid forever.**

### The trade-off this creates

Because the cat and dog clouds are sorted independently, particle *i* can change
tone when you switch pets — colours pop at the instant of the switch. The demo
hides it by firing an outward burst at the same moment, so the eye is tracking
motion rather than colour.

The alternative — cross-fading colour per particle — means per-particle colour,
which means abandoning one-draw-call-per-tone. That is a bad trade for a 200 ms
transition. **Knowing why you rejected an option is worth as much as the option
you chose.**

---

## 12. Fast arithmetic

### Trig lookup tables

At 8 000 particles the loop evaluates `sin`/`cos` up to four times each, ≈ 2
million calls per second. `Math.sin` is correctly rounded to the last ulp and
you need none of that for a wobble.

```kotlin
private object Trig {
    private const val N = 2048                 // power of two
    private const val MASK = N - 1
    private val SIN = FloatArray(N) { sin(it * 2.0 * PI / N).toFloat() }
    private val TO_INDEX = N / (2f * PI.toFloat())
    private const val QUARTER = N / 4

    fun sin(a: Float) = SIN[(a * TO_INDEX).toInt() and MASK]
    fun cos(a: Float) = SIN[((a * TO_INDEX).toInt() + QUARTER) and MASK]
}
```

Each call becomes: one multiply, one truncation, one AND, one array read.

Details that matter:

* **`N` must be a power of two** so wrapping is `and MASK` instead of `%`
  (integer division is ~20× a mask).
* **Two's-complement `and` makes negative angles wrap correctly for free.**
  `(-3) and 2047 = 2045` — exactly the modular index you want, no branch.
* **`cos` is `sin` shifted a quarter table.** One table, not two.
* **Error analysis:** index truncation means the argument error is up to one
  step, `Δ = 2π/2048 = 0.00307 rad`. Since `|d(sin)/dx| ≤ 1`, the value error is
  ≤ 0.00307. For the shimmer (amplitude ≈ 2.5 px) that's 0.008 px. For the wag
  rotation at radius ≈ 210 px, an angle error of 0.003 rad is 0.64 px.
  **Sub-pixel everywhere** — invisible. If you ever do see banding, lerp between
  adjacent entries instead of growing the table.

The technique generalises: **any expensive pure function of one variable that
you call per-particle** — `exp`, `pow`, easing curves, gradient noise — can
become a table.

### Bit tricks worth using

```kotlin
val o = i shl 1                  // i * 2, for interleaved [x,y] buffers
renderBuffer[o]     = x
renderBuffer[o + 1] = y
```

Modern JITs turn `i * 2` into a shift anyway, but writing the offset once and
reusing it (rather than computing `i*2` and `i*2+1` separately) genuinely
removes an operation and reads more clearly as "this is a stride-2 buffer".

### Hoisting

Anything not indexed by `i` must be computed **outside** the loop:

```kotlin
val breath      = 1f + breathAmount * Trig.sin(time * 1.65f)
val bobNow      = Trig.sin(time * 1.15f) * bobAmount
val wagNow      = Trig.sin(time * 2.7f) * wagAmount
val tailPivotX  = originX + cloud.tailPivotX * fit
val invBand     = 1f / rippleBand          // ← turns 8 000 divisions into 1
```

That last one is the classic: **replace a per-particle division with one
reciprocal and a multiply.** Division is 4–10× a multiply and, unlike most
arithmetic, the JIT cannot hoist it for you if the divisor isn't provably loop
invariant.

### Structure of arrays vs array of structures

```kotlin
// SoA — this
val px = FloatArray(count); val py = FloatArray(count)
val vx = FloatArray(count); val vy = FloatArray(count)

// AoS — not this
class Particle(var x: Float, var y: Float, var vx: Float, var vy: Float)
val particles = List(count) { Particle(...) }
```

For 8 000 particles the object version costs you:

* **8 000 heap objects.** Each has a 12–16 byte header plus padding, so ~40 bytes
  for 16 bytes of payload — roughly 3× the memory.
* **Pointer chasing.** Every field access is an indirection to an address the
  prefetcher can't predict, because the objects are wherever the allocator put
  them. A cache line is 64 bytes; a linear `FloatArray` walk gets 16 useful
  floats per line, while scattered objects may get 1.
* **GC pressure.** The collector must trace all 8 000 references every cycle.

The SoA version is eight flat arrays walked strictly forwards — the single most
prefetcher-friendly access pattern that exists. In practice this is routinely a
3–10× difference on the update loop, and on Android it's also the difference
between allocating per frame and never allocating.

> **Rule: anything you touch once per particle per frame belongs in a
> `FloatArray`.**

The trade is readability. Mitigate it with disciplined naming and comments, not
by reaching for objects.

---

## 13. Complexity, and when this design breaks

### The cost model

| stage | complexity | in this demo |
| --- | --- | --- |
| rasterise artwork | O(mask²) | once, off-thread |
| sample + quantise | O(mask² · k) | once, off-thread |
| counting sort | O(n + k) | once, off-thread |
| **update** | **O(n)** | every frame |
| write render buffer | O(n) | fused into update |
| **draw** | **O(k) calls, O(n · dotArea) pixels** | every frame |

Two independent axes, and confusing them is the most common profiling mistake:

* **Update time scales with particle count.** Double `n`, double the CPU time.
* **Draw time scales with covered pixels — *fill rate*.** Double the *dot
  diameter* and you quadruple the pixels drawn, with `n` unchanged.

So if your frame is slow, find out which half it is *before* optimising. The
on-screen badge measures the update half; whatever is left in
`Choreographer#doFrame` is the draw half.

### The O(n²) cliff

The finger is a single point tested against every particle: **O(n)**. Fine
forever.

But the moment particles interact with *each other* — flocking, collision,
SPH fluids, cohesion — you have O(n²). At n = 8 000 that's 64 million pair tests
per frame, which is not a slow frame, it's a slideshow.

The standard fix is a **uniform spatial hash**:

1. Choose cell size = interaction radius `R`.
2. Each frame, bucket every particle into cell `(⌊x/R⌋, ⌊y/R⌋)`.
3. For each particle, test only the 3×3 block of cells around it.

Since any particle within `R` must be in one of those 9 cells, this is exact,
not approximate. Cost drops to O(n · average occupancy) ≈ O(n). Bucketing itself
is O(n) with counting sort — the *same* algorithm from §11, which is why it was
worth learning properly.

Alternatives: uniform grids beat quadtrees when density is roughly even (this
case); quadtrees/BVH win when density varies wildly across the scene.

### When to leave the CPU entirely

| symptom | move to |
| --- | --- |
| n in the tens of thousands, update-bound | GPU compute / AGSL with state in a texture |
| per-particle textures, trails, additive glow | GPU instanced rendering |
| the effect is a *function of screen position* | a fragment shader — no particles at all |

That last row deserves emphasis. Many "particle" effects — shimmer, noise
fields, dissolves, plasma — don't need particles. If the visual can be written
as `colour = f(x, y, t)`, an AGSL `RuntimeShader` computes it per pixel on the
GPU at effectively zero CPU cost. Particles earn their keep when each one has
**independent, path-dependent state** — which is exactly the case here, because
fur remembers where your finger pushed it.

---

# Part III — The frameworks

## 14. The Android graphics pipeline

Know where your code sits, because the answer to "why is this slow" is usually
"it's in the wrong box".

```
   Your @Composable
        │  composition        ← Compose runtime, main thread
        ▼
   LayoutNode tree
        │  measure / layout   ← Compose UI, main thread
        ▼
   draw: record commands      ← your DrawScope lambda runs HERE, main thread
        │
        ▼
   RenderNode / DisplayList   ← a recorded command list, not pixels
        │
        ▼   (handed to the RenderThread)
   HWUI + Skia                ← RenderThread, separate from main
        │
        ▼
   GPU (GL / Vulkan)
        │
        ▼
   SurfaceFlinger → display   ← vsync
```

Consequences you can act on:

* **Your draw lambda does not draw.** It *records commands* into a display list.
  `drawPoints` appends one command; it does not touch a pixel. That is why
  batching helps so much — you're minimising command-list entries and Skia's
  per-call setup, not pixel work.
* **Rasterisation happens on the RenderThread**, in parallel with your next
  frame's main-thread work. So "draw cost" splits into *recording* (yours) and
  *rasterising* (fill rate, theirs). The badge in this demo measures neither —
  it measures the simulation. Perfetto shows all three.
* **A `RenderNode` can be re-used without re-recording** if nothing invalidated
  it. This is why unnecessary invalidation is expensive even when the drawing
  itself is cheap.
* **`Paint` has a native peer.** Allocating one per frame allocates on both the
  Java and native heaps. Build them once (`remember`) and mutate fields.

## 15. Compose's three phases and the snapshot system

### The three phases

```
   COMPOSITION  →  LAYOUT  →  DRAW
   what to show    where      how
```

Every frame, Compose runs only the phases that were **invalidated**. The
catastrophic beginner mistake in animation is doing frame-rate work in the first
phase: if your 60 Hz ticker writes state that is read during *composition*, you
re-run composition, layout **and** draw sixty times a second, for the whole
subtree.

### How Compose knows what to re-run

`mutableStateOf` is not magic; it's an observable read/write log. When Compose
runs a phase, it wraps it in a **snapshot observer**. Every `State` read inside
that scope is recorded as a dependency of the thing currently running:

| read happens inside… | recorded against… | writing invalidates… |
| --- | --- | --- |
| a composable body | that `RecomposeScope` | composition + layout + draw |
| a `Modifier.layout` / measure block | that `LayoutNode`'s measure | layout + draw |
| a `DrawScope` lambda | that `LayoutNode`'s draw | **draw only** |

So "which phase does my animation cost?" is decided entirely by **where the read
textually happens**, not where the state is declared.

### The load-bearing line

```kotlin
var frame by remember { mutableIntStateOf(0) }

LaunchedEffect(engine) {
    while (isActive) {
        withFrameNanos { … }
        engine.update(dt, …)
        frame++                      // ← written from a coroutine
    }
}

Canvas(modifier) {
    frame                            // ← read HERE, inside DrawScope
    …
}
```

That lone `frame` expression inside the draw lambda looks like dead code and is
in fact the most important line on the screen: it is the *subscription* that
schedules the next draw. Delete it and the animation freezes even though the
engine keeps stepping perfectly.

Conversely, move the read one line up — into the composable body — and you have
re-invented the 4 fps particle system.

> **Deferred reads.** The general principle: read frame-rate state **as late as
> possible in the pipeline**. Compose's own APIs are built around this, which is
> why `Modifier.offset { IntOffset(x, 0) }` (lambda, layout phase) exists
> alongside `Modifier.offset(x.dp)` (value, composition phase). Same visual,
> different cost class.

### Corollaries

**Never pass an animating value as a composable parameter.**

```kotlin
Particles(offset = animatedOffset)      // ← reading it to build the arg IS a composition read
Particles(offset = { animatedOffset })  // ← the read moves into the callee's draw scope
```

The demo uses the lambda idiom even for the slow stats readout:

```kotlin
@Composable
private fun StatsBadge(stats: () -> FieldStats, …) {
    val value = stats()      // the read is recorded in THIS scope, not the parent's
```

so a twice-a-second update recomposes one `Surface` instead of the whole screen.

**Use the primitive state types.** `mutableIntStateOf` / `mutableFloatStateOf`
avoid autoboxing an `Int`/`Float` on every write. Sixty allocations a second is
not a crisis, but it's free to avoid.

**Keep high-frequency input out of snapshot state entirely.** Pointer events
arrive up to 240 Hz. Writing each into `mutableStateOf` schedules snapshot work
nothing needs:

```kotlin
private class PointerTracker {      // a plain class. No State anywhere.
    var x = 0f; var y = 0f
    var vx = 0f; var vy = 0f
    var active = false
}
val pointer = remember { PointerTracker() }
```

Both the pointer handler and the frame loop run on the main thread, so there is
no visibility or race concern. **`State` is for things the UI must react to.
This is data flowing into a simulation — it needs no reactivity at all.**

### Why not 8 000 composables?

Because each would be a `LayoutNode`: a slot-table entry, a modifier chain, a
measure pass, a placement, and a draw. Hundreds of bytes and several
microseconds each, times 8 000, times 60 per second. It is not a close call —
it's three orders of magnitude. **One node that draws 8 000 things is the only
viable shape.**

## 16. The frame clock

```kotlin
var last = withFrameNanos { it }
while (isActive) {
    val now = withFrameNanos { it }
    val dt = ((now - last) / 1e9f).coerceIn(0f, 0.033f)
    last = now
    …
}
```

`withFrameNanos` suspends until the next frame and resumes with that frame's
timestamp. Underneath: `MonotonicFrameClock` → on Android,
`AndroidUiFrameClock` → `Choreographer.postFrameCallback` → the vsync signal.

Why this and not a `Handler.postDelayed(16)`:

* **Vsync alignment.** You produce exactly one update per displayed frame. A
  timer drifts against vsync and will occasionally produce two updates for one
  frame or none, which is visible as micro-stutter.
* **Automatic lifecycle.** The loop lives in a `LaunchedEffect`, so leaving the
  composition cancels the coroutine and the loop stops. No manual teardown, no
  leak, no work while off-screen.
* **The frame's timestamp, not "now".** All work scheduled for a given frame
  sees the *same* time value, so `dt` reflects display cadence rather than the
  jitter of your own scheduling. Using `System.nanoTime()` instead reintroduces
  exactly the noise you're trying to avoid.
* **Refresh-rate agnostic.** 60, 90, 120 Hz — you get called at the right rate
  and `dt` reports the truth. Which only helps if your constants are per-second
  (§6).

One nuance: `withFrameNanos` resumes *during* the frame's dispatch, before
composition. So state you write in the loop is picked up by the very same
frame's draw — no one-frame lag.

## 17. Canvas, DrawScope, and batched draw calls

### What `Canvas` is

```kotlin
@Composable fun Canvas(modifier: Modifier, onDraw: DrawScope.() -> Unit) =
    Spacer(modifier.drawBehind(onDraw))
```

A layout node that occupies space and draws. `DrawScope` gives you `size`,
density conversions, and Compose-flavoured `drawRect`/`drawPath`/`drawCircle`.

### Dropping to the native canvas

`DrawScope` has no batched-points API, so:

```kotlin
drawIntoCanvas { canvas ->
    val native = canvas.nativeCanvas          // android.graphics.Canvas
    native.drawPoints(buffer, offset, count, paint)
}
```

This is not a hack — `DrawScope` is a thin convenience layer over the same
`android.graphics.Canvas`. Dropping down costs nothing and unlocks the platform
API surface (`drawPoints`, `drawBitmapMesh`, `drawVertices`, text on a path…).

### `drawPoints`, precisely

```java
void drawPoints(float[] pts, int offset, int count, Paint paint)
```

* `pts` is **interleaved** `[x0, y0, x1, y1, …]`.
* `offset` is an index **into the float array**, not a point index.
* `count` is the number of **floats** to consume, not points.

Hence the shifts:

```kotlin
val offset = from shl 1          // point index → float index
val values = n shl 1             // point count → float count
```

With a `STROKE` paint and `Cap.ROUND`, each point renders as a round dot of
diameter `strokeWidth`. (With `Cap.BUTT` you get squares, which is occasionally
what you want and is marginally cheaper.)

### The batching payoff

```kotlin
for (b in set.fill.indices) {                 // k ≈ 9 iterations
    val from = starts[b]
    val n = starts[b + 1] - from
    if (n <= 0) continue
    native.drawPoints(e.renderBuffer, from shl 1, n shl 1, set.fill[b])
}
```

**7–9 draw calls per frame, total, regardless of particle count.** Compare
8 000 `drawCircle` calls: 8 000 command-list entries, 8 000 paint state
validations, 8 000 Skia dispatches. This is the difference between 4 fps and
60 fps, and no other optimisation in this document comes close.

### Per-tone paints are free expressiveness

Because each tone already needs its own `Paint`, per-tone **dot size** and
**bloom** cost nothing extra:

```kotlin
strokeWidth = dotSize * tone.sizeScale        // whiskers 0.62×, eyes 1.0×
```

Bloom is a second pass over the *same* buffer at 3.4× width and alpha 46 — one
extra call for the two or three tones that want it. Compare
`RenderEffect.createBlurEffect` over the whole layer: a full-screen offscreen
render target, every frame.

Blink is the same trick: `paint.alpha = eyeAlpha` before the eye tones' calls.
**One field write animates 300 particles.**

### Reading the buffer while it's being written

The update loop writes `renderBuffer`; the draw lambda reads it. Both on the
main thread, strictly ordered (update in the frame callback, draw later in the
same frame), so no synchronisation is needed. If you ever move the simulation to
a background thread, you need **double buffering** — write to buffer A while
drawing buffer B, swap under a lock — or you'll get tearing where half the
particles are a frame ahead.

## 18. Pointer input

### Event dispatch

Compose delivers each pointer event in up to three passes:

| pass | direction | typical use |
| --- | --- | --- |
| `Initial` | outer → inner | parents claiming events before children (scroll containers) |
| `Main` | inner → outer | normal handling — most gesture detectors |
| `Final` | outer → inner | reacting to what was consumed |

Multiple `Modifier.pointerInput` blocks each get their own node, so this screen
can run a tap detector and a raw position tracker side by side:

```kotlin
.pointerInput(engine) {
    detectTapGestures(onTap = { engine?.purr(it.x, it.y) },
                      onLongPress = { engine?.shake() })
}
.pointerInput(engine) {
    awaitPointerEventScope {
        while (true) {
            val change = awaitPointerEvent().changes.firstOrNull()
            if (change != null && change.pressed)
                pointer.onMove(change.position.x, change.position.y, change.uptimeMillis)
            else pointer.release()
        }
    }
}
```

The tracker is *inner* in the chain, so on the `Main` pass it sees events first;
it consumes nothing, so the tap detector still works. Consumption is the thing
to watch for when composing gesture modifiers — a detector that consumes will
starve anything downstream of it.

### The suspend-based gesture API

`awaitPointerEventScope { while (true) { awaitPointerEvent() } }` reads like a
busy loop and isn't: `awaitPointerEvent()` suspends. This is the whole design of
Compose's gesture system — gestures as **sequential suspend code** rather than
state machines over callbacks. `detectTapGestures`, `detectDragGestures` and
friends are ordinary suspend functions built on exactly this primitive, and you
can write your own the same way.

### Timestamps

`change.uptimeMillis` is the event's own timestamp from the input system, not
"when my code ran". Use it for velocity, always — using `System.currentTimeMillis()`
at handling time bakes in your own scheduling jitter, which is precisely the
noise the low-pass filter (§7) then has to remove.

## 19. Escape hatches: when Compose is the wrong layer

| you need | reach for |
| --- | --- |
| per-pixel effects: shimmer, dissolve, plasma, blur, distortion | **AGSL `RuntimeShader`** (API 33+) — see this project's `shaders/`, `riveo/` |
| tens of thousands of sprites, trails, additive blending | `drawVertices` / `drawBitmapMesh`, or OpenGL/Vulkan via `SurfaceView` |
| video-rate compositing | `SurfaceView` / `TextureView` with your own render thread |
| a designed, hand-animated character | Lottie or Rive — don't simulate what an artist can key-frame |
| physically accurate stacking, joints, friction | a real 2-D physics engine |

The honest boundary for the architecture in this document is roughly **tens of
thousands of particles with independent state**. Below that, the CPU + batched
`drawPoints` design is simpler, more debuggable, and easier to make
*interactive* than any GPU approach. Above it, you are fighting the wrong fight.

The key discriminator, again: if your effect can be written as
`colour = f(x, y, t)` with no memory, it belongs in a fragment shader. If each
element carries **path-dependent state** — where it's been, what pushed it —
it belongs in a particle system.

---

# Part IV — Design walkthrough

How this specific system was derived, including the roads not taken. The
reasoning transfers even though the destination won't.

### Requirement

> A cat and a dog made of dots that look like a real animal and respond to touch
> beautifully, at 60 fps on a mid-range phone.

### Decision 1 — Where do the dots come from?

| option | verdict |
| --- | --- |
| Hand-place coordinates | Hundreds of magic numbers, unmaintainable, one shape only |
| SVG path + `PathMeasure` | Gives you the **outline**, not the interior. Fine for wireframes, wrong for a solid creature |
| Load a PNG asset | Works, but adds binary assets, no per-part tagging, no easy recolouring |
| **Rasterise Canvas primitives, read pixels** | **Chosen.** Interior for free, colour for free, tags via extra masks, zero assets, tweakable in code |

The winning property is that it produces **filled regions with colour**, which is
what makes the result look like an animal rather than a coloured outline.

### Decision 2 — How many dots, and where?

Rejected "pick N random opaque pixels" for the Poisson clumping in §10. Rejected
a fixed particle count because visual density would vary across devices.

Chose: **render the mask at the particle density itself**, one opaque pixel per
particle, jittered. Gets even coverage, device-independent density, a tiny
`getPixels`, and an automatic budget — four wins from one decision. Those are the
decisions worth hunting for.

### Decision 3 — What drives the motion?

Rejected tweens (Part I: no start/end, no composition, no interruption).
Rejected position-only lerping (no momentum, can't be flung).

Chose **damped springs to a home position**, with all other influences as
additive forces. Interruption, composition and momentum all come for free.

### Decision 4 — How is it drawn?

This was decided *first*, and everything else was designed backwards from it.
The constraint "draw call count must not depend on particle count" forces:
a small flat palette → tone buckets → counting sort → one interleaved buffer →
`drawPoints` with offsets.

Note the direction of causation: **the renderer's constraint dictated the
artwork's constraint** (flat colours, no gradients). That is normal and healthy.
When a performance requirement is genuinely hard, let it propagate all the way
back to the content.

### Decision 5 — How does it feel alive?

Rejected keyframed idle animations (would need an artist and a runtime).

Chose four cheap procedural layers — breathe, bob, wag, shimmer — applied to the
*home* position so the springs smooth them, each gated by a **per-particle
weight** rather than a branch. Total cost: about six lines in the inner loop.

### What was traded away

* **Colour pops on pet switch** (§11) — accepted, masked by the burst, because
  fixing it would cost the batching.
* **No particle–particle interaction** — not needed here; would require the
  spatial hash from §13.
* **Not perceptually-correct colour matching** — irrelevant when the source is
  flat palette fills.
* **Artwork is code, not an asset** — a designer can't edit it. Right call for a
  learning project; wrong call for a shipping app with a design team, where
  you'd swap stage 1 for an SVG or PNG pipeline and change nothing else.

---

# Part V — Transferring the technique

## The API map

| stage | Android / Compose | Flutter | Web | SwiftUI |
| --- | --- | --- | --- | --- |
| offscreen art | `Bitmap` + `android.graphics.Canvas` | `PictureRecorder` → `Image` | offscreen `<canvas>` | `ImageRenderer` / `CGContext` |
| read pixels | `Bitmap.getPixels` | `Image.toByteData` | `ctx.getImageData` | `CGContext` buffer |
| frame clock | `withFrameNanos` | `Ticker` / `AnimationController` | `requestAnimationFrame` | `TimelineView(.animation)` |
| batched draw | `Canvas.drawPoints` | `Canvas.drawRawPoints` | one `Path2D` + `fill()`, or WebGL instancing | `Canvas` + `Path`, or Metal |
| shader hatch | AGSL `RuntimeShader` | `FragmentProgram` | WebGL / WebGPU | Metal / `ShaderLibrary` |

Notes:

* **Flutter**: `drawRawPoints(PointMode.points, Float32List)` is the exact
  analogue, interleaved buffer and all. Most of the "interactive bear/creature"
  demos circulating on LinkedIn are built on precisely this. `Float32List` is
  the SoA-friendly typed array; use it, not `List<Offset>`.
* **Web**: `fillRect` per particle is slow. Accumulating all particles of one
  colour into a single `Path2D` and filling once is the same batching trick.
  Beyond ~20 k, go WebGL with instanced quads.
* **SwiftUI**: `Canvas` with `.drawingGroup()` (Metal-backed) is the closest
  match; the phase model differs but the "don't touch view state per frame"
  principle is identical.

## The universal checklist

1. Draw the source in a **unit square** with **flat colours** from a small,
   well-separated palette, layered back to front.
2. Add a **tag mask** per articulated part; punch out whatever occludes it.
3. Pick a **spacing in pixels**; derive mask resolution from available space;
   clamp it. One opaque pixel = one particle.
4. Quantise to the palette, **jitter**, **counting-sort by tone**, record bucket
   offsets. Once, off the main thread.
5. Give every particle a **home** and a damped spring to it. Choose ζ and a
   settling time first, then solve for `k` and `c`.
6. Layer forces on top, each with a **C¹ kernel** and a **per-particle weight**.
7. Integrate **semi-implicitly** with **clamped `dt`** and a velocity cap.
8. Stream positions into **one interleaved buffer**; one batched draw per colour.
9. Read the frame counter **only inside the draw scope**.
10. **Measure on a slow device** before adding anything else.

---

# Part VI — Measuring and debugging

## The instruments, cheapest first

**1. An on-screen counter.** Average the update time over 30 frames and render
it. Thirty seconds to build, always available, no tooling. This demo has one.

```kotlin
val t0 = System.nanoTime()
engine.update(…)
accumMs += (System.nanoTime() - t0) / 1_000_000f
```

**2. Layout Inspector → recomposition counts.** A correctly-built particle
screen shows a recomposition count that **stays flat** while the animation runs.
If it climbs at 60/s, your frame ticker is being read in composition. This is
the single fastest way to catch the #1 mistake.

**3. Perfetto / `systrace`.** The real picture. Look for `Choreographer#doFrame`
exceeding budget, then which slice inside it: `Composition`, `Layout`, `Draw`,
or `DrawFrame` on the RenderThread. **Any `Composition` slice during a steady
animation is a deferred-read bug.** This project has a `perfetto-trace-analysis`
skill for exactly this.

**4. Compose compiler reports.** Show which composables are skippable/restartable
and which parameters are unstable. Relevant here mainly for the surrounding UI.

**5. Memory profiler.** A sawtooth during animation means something in the loop
allocates. In a correct SoA engine the allocation rate during animation should
be **flat zero**.

## Symptom → cause

| symptom | likely cause |
| --- | --- |
| Animation frozen, engine stepping fine | No state read in the draw scope (§15) |
| 4 fps, high CPU, recomposition count climbing | Frame state read during composition (§15) |
| Everything vanishes after a background/resume | Unclamped `dt` → instant explosion (§4) |
| Particles slowly drift to infinity | Explicit instead of semi-implicit Euler (§3) |
| One particle vanishes, then all of them | NaN from a `0/0` — missing epsilon guard (§1) |
| A visible circle follows the finger | Linear falloff kernel (§7) |
| Motion faster/damper on a 120 Hz phone | Per-frame multiplier without `pow` (§6) |
| Speckled or flickering colours | Palette entries too close in RGB (§11) |
| Looks like graph paper | Grid sampling without jitter (§10) |
| Sparse on tablets, mushy on phones | Fixed particle count instead of fixed spacing (§10) |
| Whole image vibrates instead of shimmering | Shared shimmer phase instead of per-particle (§8) |
| Body tears when a limb moves | Boolean part mask instead of a weight (Part I) |
| A limb drags a slice of the body with it | Occluder not punched out of the tag mask (§10) |
| Smooth at n=1000, dies at n=8000 | Per-particle draw calls (§17) |
| Smooth at small dots, dies at large dots | Fill-rate bound, not CPU bound (§13) |

## Rules of measurement

* **Release build, R8 on, slowest supported device.** Debug builds with the
  Compose compiler's live-literal instrumentation can be 5× slower and will send
  you chasing ghosts.
* **Change one thing, measure, keep or revert.** Particle systems have many
  interacting knobs and intuition is unreliable.
* **Know which half you're optimising** (§13): update time scales with count,
  draw time scales with covered pixels.

---

# Appendix — Cheat sheet

## Formulas

```
DISTANCE
  ‖d‖² = dx² + dy²                          compare with this, never sqrt
  d̂    = d / ‖d‖                            guard ‖d‖ > ε first
  d^⊥  = (−dy, dx)                          90° CCW — the 2-D curl

INTEGRATION (semi-implicit / symplectic Euler)
  v += a · dt                               velocity FIRST
  p += v · dt                               then position, with the NEW v

SPRING
  a  = k·(home − p) − c·v
  ω  = √k                                   natural frequency, rad/s
  c  = 2·ζ·√k                                ζ = damping ratio
  t_s ≈ 4 / (ζ·ω)                            2 % settling time
  M_p = exp(−πζ / √(1−ζ²))                   overshoot fraction
  → design: ω = 4/(ζ·t_s),  k = ω²,  c = 2ζω

STABILITY (semi-implicit, damped)
  dt < (−c + √(c² + 4k)) / k        ← the real bound
  dt < 2/c                          ← also required
  dt < 2/ω                          ← undamped special case; the LOOSEST one
  ⟹ clamp dt to ~half the bound

FRAME-RATE INDEPENDENCE
  per-frame factor r at 60 Hz  →  r^(dt·60) at any rate
  lerp α at 60 Hz              →  1 − (1−α)^(dt·60)

KERNELS   (t = 1 − d/R, so t=1 at centre, t=0 at rim)
  linear      t                 slope ≠ 0 at rim → VISIBLE EDGE
  quadratic   t²                slope 0 at rim → smooth
  smoothstep  t²(3 − 2t)        slope 0 at both ends
  Gaussian    exp(−d²/2σ²)      softest, needs exp

TRANSFORMS
  scale about pivot    p' = pivot + (p − pivot)·s
  rotate about pivot   x' = px + rx·cos θ − ry·sin θ
                       y' = py + rx·sin θ + ry·cos θ
  weighted             use θ·w[i], w ∈ [0,1] — gives bending for free

WAVE
  a function of (d − r(t)), not of d
  triangular shell: 1 − |d − r|/σ
  amplitude decay:  1 − r/r_max   (or 1/√r for physical 2-D)

SAMPLING
  uniform random, 1 pt/cell expected:  36.8 % empty, 26.4 % doubled
  jittered grid: exactly 1 per cell, offset ±½ cell → even AND organic
  mask resolution = fitPx / targetSpacing, clamped
```

## This engine's constants

| constant | value | meaning |
| --- | --- | --- |
| `stiffness` k | 340 /s² | ω = 18.44 rad/s |
| `dampingRatio` ζ | 0.72 | 3.8 % overshoot, settles in 0.29 s |
| `damping` c | 26.55 /s | derived: 2ζ√k |
| `dt` clamp | 0.033 s | vs. a 0.056 s stability bound → 1.7× margin |
| `maxSpeed` | 4.5 · fit /s | direction-preserving velocity cap |
| `touchRadius` | 0.30 · fit | quadratic falloff |
| `targetSpacing` | 6.2 px | ⟹ mask 90–190 px, ⟹ ≤ 9 000 particles |
| `breathAmount` | 1.4 % | scale about the seated base |
| `wagAmount` | 0.26 rad | at full weight, ≈ 15° |
| `shimmer` | 0.0035 · fit | ≈ 2.5 px, per-particle phase |
| palette size k | 7 (cat) / 9 (dog) | ⟹ 7–9 `drawPoints` calls per frame |

## Compose rules, condensed

1. Read frame-rate state **only inside `DrawScope`** — draw-phase invalidation.
2. Pass animating values as **lambdas**, never as parameter values.
3. High-frequency input goes in a **plain class**, not `State`.
4. `mutableIntStateOf` / `mutableFloatStateOf` for primitives.
5. `withFrameNanos`, never a `Handler` or timer.
6. `remember` your `Paint`s; mutate fields, never reallocate.
7. **One** layout node draws everything. Never one node per particle.
8. Heavy setup on `Dispatchers.Default`, then publish the finished result.

---

*Companion document: [`PETPARTICLES.md`](PETPARTICLES.md) — a file-by-file tour
of the implementation these principles produced.*
