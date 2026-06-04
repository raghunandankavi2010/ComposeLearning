# SVG Path Morphing

Compose port of `can-it-be-done-in-react-native/bonuses/svg-path-morphing`. Drag a slider to
morph a phone silhouette across eras. The same engine also powers the play↔pause idea, and is
the kind of interpolation `react-native-redash`'s `interpolatePath` performs.

## The idea

Two SVG paths that share the **same command structure** (same sequence of `M/L/C/Q/Z`, same
number of coordinates) can be morphed by simply **interpolating every coordinate**:

```
mergedᵢ = fromᵢ + (toᵢ − fromᵢ) · t
```

No resampling library (flubber) is needed because the phone outlines are authored with a
matching structure. The screen cut-out rectangle is interpolated the same way.

## Engine (`PathInterpolator.kt`)

1. `parseSvgPath(d)` → `List<PathSegment>` — a small tokenizer reading absolute commands
   (`M L H V C Q Z`), including SVG "implicit repeats" (extra coordinate groups repeat the
   command; a repeated `M` becomes `L`).
2. `lerpSegments(from, to, t)` — per-coordinate lerp; falls back to `from` on a structure
   mismatch (so it never crashes).
3. `List<PathSegment>.toPath(map)` — rebuilds a Compose `Path`, mapping each viewBox point to
   pixels through `map`.
4. `FitBox(viewBox, dest)` — a "contain" fit (preserve aspect, centre) from the shared
   `100×300` viewBox into the canvas, used for both the path and the screen rect.

## Screen

The slider value `s ∈ [0, n−1]` is split into an integer index `i = floor(s)` and fraction
`t = s − i`; we morph `phones[i] → phones[i+1]` by `t`. Parsing happens once
(`remember`), the lerp + path build happen per slider change.

## Architecture
`domain` (PhoneShape + ScreenRect, repository, use case) → `data` (the 9 phone path strings) →
`presentation` (`PathMorphContract`, `PathMorphViewModel` + Factory, `PathMorphScreen`, and the
reusable `PathInterpolator`).
