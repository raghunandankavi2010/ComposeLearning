# Spinning Wheel (Canvas)

A fortune / prize "spin the wheel" built entirely with Compose `Canvas`. Tap **SPIN**,
the wheel accelerates then decelerates to a stop under a fixed pointer, and the section
that lands under the pointer is announced — exactly like a physical spinning wheel.

## Files
- `WheelSection.kt` — data model + default demo sections.
- `SpinningWheel.kt` — the pure Canvas drawing composable (stateless; takes a rotation angle).
- `SpinningWheelScreen.kt` — Route → Screen → Content. Owns the spin animation & result state.

## Visual anatomy
```
            ▼  ← pointer / pivot (fixed, 12 o'clock)
        ╭───────╮
       │  slices │   ← N colored pie sections (drawArc, useCenter = true)
       │    ●    │   ← center hub
        ╰───────╯
         ○ ○ ○ ○     ← decorative rim "LED" pegs
```
Layers drawn (outer → inner):
1. Drop-shadow circle (soft offset).
2. Outer rim ring + evenly spaced peg dots.
3. Pie sections via `drawArc(useCenter = true)`, alternating palette colors.
4. Section dividers (thin lines) for crisp edges.
5. Radial labels — canvas rotated to each section's mid-angle, text drawn with native `Paint`.
6. Center hub (filled circle + ring).
7. Fixed pointer triangle at the top, NOT rotated with the wheel.

## Animation & physics
- A single `Animatable<Float>` holds the wheel rotation in **degrees**.
- On spin:
  - `base   = current - (current mod 360)`        (drop the fractional turn, keep continuity)
  - `spins  = 360 * random(4..6)`                 (several full revolutions)
  - `extra  = random(0..360)`                     (random landing point ⇒ random result)
  - `target = base + spins + extra`
- Animated with `tween(durationMillis ≈ 4200, easing = decelerate)` where
  `decelerate = CubicBezierEasing(0.1f, 0.85f, 0.2f, 1f)` → fast launch, gentle friction stop.
- `isSpinning` gates the button so it can't be re-triggered mid-spin.

## Winner detection
Canvas angles: `0°` = East (3 o'clock), increasing **clockwise**. The pointer is at the
top = `270°`. Section `i` is originally drawn over `[i*sweep, (i+1)*sweep)` and the whole
wheel is rotated clockwise by `rotation`. A section originally at angle `θ` appears at
`θ + rotation`, so the slice under the pointer satisfies `θ + rotation ≡ 270 (mod 360)`:

```
sweep       = 360 / sectionCount
finalAngle  = target mod 360
pointerRel  = (270 - finalAngle) mod 360
winnerIndex = floor(pointerRel / sweep)
```

The result is computed from where the wheel actually stops, so it is a fair random draw.

## Reuse / API
`SpinningWheel(rotation, sections, modifier)` is stateless and previewable on its own.
The screen hoists all state and exposes the typical `Route -> Screen -> Content` shape used
across this project.
