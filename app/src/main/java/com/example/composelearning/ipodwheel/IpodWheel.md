# iPod Click Wheel

Compose port of `can-it-be-done-in-react-native/season3/src/iPod` (the click-wheel navigator).
Drag your finger **around** the wheel to scroll/highlight the song list; the centre button
selects.

## The maths

The wheel converts a finger position to a polar **angle** about the wheel centre:

```
θ = atan2(y − cy, x − cx)
```

Each drag frame produces an **angular delta** versus the previous frame, with wrap-around
handled so crossing the ±π seam doesn't jump:

```
Δ = θ − θ_prev
if Δ >  π : Δ −= 2π
if Δ < −π : Δ += 2π
rotation += Δ            // accumulated, clamped ≥ 0
```

The highlighted row is derived from the accumulated rotation, one step per `ANGLE_PER_ITEM`
(here `π/5` ≈ 36°, so ~10 songs per full turn):

```
highlight = floor(rotation / ANGLE_PER_ITEM)   (clamped to the list)
```

The list slides so the highlighted row sits centred (`centerOffset − itemHeight · highlight`),
animated with `animateDpAsState` for smoothness between discrete steps.

(The original accumulated raw radians and mapped them 1:1 to pixels; we pick an explicit
`ANGLE_PER_ITEM` so the feel is tuned for touch.)

## Compose specifics
`detectDragGestures` tracks the angle; the centre `Box` is `clickable` for select. Highlight is
view-local; the ViewModel owns the song list and the `nowPlaying` selection. Architecture:
`domain` (Song, repository, use case) → `data` → `presentation` (MVI contract + ViewModel +
screen).
