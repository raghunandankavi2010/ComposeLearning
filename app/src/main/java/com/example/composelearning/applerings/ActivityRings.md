# Apple Activity Rings

Compose port of `can-it-be-done-in-react-native/season3/src/AppleActivity`. Three concentric
rings animate from 0 to their target fill. Some targets exceed 100%, so the arc overlaps
itself — the signature Apple look. Tap to replay.

## The maths

Each ring has a `targetTurns` (1.0 = 360° = 100%). With an animated `progress` 0→1:

```
sweep = targetTurns · 360° · progress
```

Compose's `drawArc` happily accepts `sweep > 360`, so over-100% rings just wrap. We rotate the
draw by −90° so the fill starts at the top, and stroke with `StrokeCap.Round` for rounded
ends. The gradient runs start→end via `Brush.sweepGradient`.

**Concentric radii.** The outer ring uses radius `r₀ = (minDimension − stroke)/2`; each inner
ring is one stroke-width smaller: `r = r₀ − insetSteps · stroke`.

**End-cap shadow (the overlap depth).** When `sweep > 360°` the leading cap floats above the
ring's own start. We compute the cap centre at angle `−90° + sweep`:

```
cap = center + r · (cos θ, sin θ)
```

then draw a soft dark circle slightly offset beneath it and the coloured cap on top — selling
the "the end is on top of the beginning" 3-D effect.

## Compose specifics
`progress.value` is read inside the `Canvas` draw lambda, so the 1.8 s fill animates by
redrawing only. Architecture: `domain` (RingSpec, repository, use case) → `data` (the three
ring colors/targets) → `presentation` (MVI contract + ViewModel + screen).
