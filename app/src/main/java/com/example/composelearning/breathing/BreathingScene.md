# Breathing (Headspace)

Compose port of `can-it-be-done-in-react-native/season5/src/Headspace`. Tap anywhere to
play/pause. Three things animate: a layered wavy gradient, an organic blob, and a play↔pause
button morph. Everything is driven by one per-frame clock (`withFrameNanos`, ms since start).

## 1. Wavy gradient background

Three "hills" are stacked over a base fill. Each hill is a quadratic crest filled to the
bottom:

```
moveTo(0, start) ; quadraticBezierTo(w/2, start − h, w, start) ; → down to corners ; close
```

`start` (baseline) and `h` (crest height) swell between two values using a **triangle
ping-pong** run through an ease-in-out curve:

```
p          = (t mod dur) / dur
goingBack  = floor(t/dur) is even
progress   = goingBack ? 1 − p : p
eased      = cubicBezier(0.37, 0, 0.63, 1)(progress)
value      = mix(eased, a, b)          // a → b as eased 0 → 1
```

The three hills use different periods (4100 / 4000 / 3800 ms), so they drift out of sync and
the surface looks alive.

## 2. The breathing blob

A near-circle built from **four cubic béziers**, using the circle constant
`C ≈ 0.5523`. Each quadrant's control distance wobbles independently:

```
Cᵢ = C + WOBBLE · noiseᵢ(t)
```

`noise` here is a cheap stand-in for 2D simplex (two slow sines out of phase), giving an
organic, non-repeating wobble. The whole path also rotates slowly (`angle = t/2000` rad)
about its centre via `DrawScope.rotate`.

## 3. Play ↔ pause morph (parametric)

Rather than morphing SVG strings, the icon is built from **two quads** whose 4 vertices each
`lerp` between the play shape (`p = 0`) and a pause bar (`p = 1`):

- left quad: left half of the triangle → left bar,
- right quad: triangle tip → right bar.

`p` is an `Animatable` that animates to `1` (pause) when playing and `0` (play) when paused,
over 450 ms — matching the original's toggle timing.

## Architecture
`domain` (BreathingSession + repository + use case) → `data` (palette/copy) → `presentation`
(`BreathingContract` with `isPlaying`, `BreathingViewModel` + Factory, `BreathingScreen`).
The clock and morph are view-local; the ViewModel owns only `isPlaying` and the session
config. The clock value and morph value are read **inside the `Canvas` draw lambda**, so each
frame redraws without recomposing.
