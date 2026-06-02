# Premium Circular Progress Indicator

A high-performance, fluid indeterminate circular loader built with Jetpack Compose Canvas.

## Architecture

### 1. Performance
- **Zero Recomposition**: The component reads `rotation`, `tailAngle`, and `sweep` strictly inside the `Canvas` DrawScope. This bypasses the Compose recomposition phase, ensuring the UI thread is free for layout and other tasks. Only the drawing phase is invalidated at 60/120fps.
- **Single Frame Clock**: Instead of two independent `infiniteRepeatable` transitions, the loader is driven by one continuously-accumulating clock inside a `LaunchedEffect` + `withFrameNanos` loop (the same technique as `SmoothProgressBar`). This is what makes the motion seamless — see [Continuity](#4-continuity-no-restart).
- **Respects `LocalAnimationsEnabled`**: When animations are disabled (e.g. in tests), the frame loop exits early and the arc stays static.

### 2. Mathematics

The loader composes two motions, both derived from the same `elapsedMs` clock:

#### Global Rotation
- **Range**: 0° to 360° (wrapped via `% 360`)
- **Period**: `rotationPeriodMillis` (default 2000ms)
- **Easing**: Linear (constant angular velocity)
- **Purpose**: Provides the base continuous spinning motion.
- **Formula**: `rotation = (elapsedMs / rotationPeriodMillis * 360) % 360`

#### Grow / Shrink (Head & Tail) Cycle
- **Period**: `cyclePeriodMillis` (default 1200ms)
- **Easing**: `FastOutSlowInEasing` (gives the elastic "momentum" feel)
- The cycle position is `p ∈ [0, 1)` and the number of fully completed cycles is `completed = floor(elapsedMs / cyclePeriodMillis)`.

The arc is described by a **tail** (start angle) and a **sweep** (length). `stretch = maxSweep - minSweep = 240°`.

- **Expansion (`p < 0.5`)** — the head races forward, tail anchored:
    - `headDelta = FastOutSlowInEasing.transform(p * 2) * stretch`
    - `tailDelta = 0`
- **Contraction (`p ≥ 0.5`)** — the head holds, the tail catches up:
    - `headDelta = stretch`
    - `tailDelta = FastOutSlowInEasing.transform((p - 0.5) * 2) * stretch`

Final angle state:
- `sweep = minSweep + headDelta - tailDelta`
- `tailAngle = (completed * stretch + tailDelta) % 360`
- `finalStartAngle = -90f + rotation + tailAngle`

The `* 2` and `0.5` phase-split mean each phase is mapped from its half-window back onto a full `0..1` range, so growth reaches 100% exactly at the cycle midpoint and contraction completes exactly at the end.

#### Visual Math Reference

![Visualization Progress](../../../../../../../../visualization_progress.jpeg)

| Phase | Local $p$ | headDelta | tailDelta | Sweep Angle | Visual Action |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Start** | 0.0 | $0^\circ$ | $0^\circ$ | $30^\circ$ | Small segment at start position. |
| **Mid-Expand** | 0.25 | $120^\circ$ | $0^\circ$ | $150^\circ$ | Head racing forward, growing. |
| **Peak** | 0.5 | $240^\circ$ | $0^\circ$ | $270^\circ$ | Maximum length reached. |
| **Mid-Contract** | 0.75 | $240^\circ$ | $120^\circ$ | $150^\circ$ | Head holds, tail catching up, shrinking. |
| **End** | 1.0 | $240^\circ$ | $240^\circ$ | $30^\circ$ | Back to minimum size, tail caught up. |

So `sweep` breathes **30° → 270° → 30°** every cycle: expand, then contract.

### 3. Coordinate Orientation
The base angle is offset by **-90 degrees**. This ensures that the progress arc's expansion begins exactly at the 12 o'clock position, which is the standard expectation for circular indicators.

### 4. Continuity (No "Restart")

The earlier version ran the rotation and the grow/shrink driver as **two separate** `infiniteRepeatable` transitions. The grow/shrink driver used `RepeatMode.Restart`, so at the end of every cycle its progress snapped `1f → 0f`, which yanked the arc's tail **~240° backwards** in a single frame. Because the two periods (2000ms vs 1200ms) never aligned, this snap landed in a different place each loop — the visible "restart every cycle".

The fix is the `completed * stretch` term in `tailAngle`. Within a cycle `tailDelta` ramps `0 → stretch`; at the seam it resets to `0`, but `completed` simultaneously increments by 1, adding exactly `stretch` back. The two cancel, so the tail angle is **continuous across the seam**:

```
end of cycle N:   completed = N,   tailDelta → 240   ⇒ N*240 + 240 = (N+1)*240
start of cycle N+1: completed = N+1, tailDelta = 0    ⇒ (N+1)*240 + 0 = (N+1)*240   ✓
```

The head is continuous for the same reason (`head = tailAngle + sweep`). The result keeps the breathing expand/contract motion while removing the backward jump. (`tailAngle` is taken `% 360` and `elapsedMs` is a `Double` so the accumulation stays precise over long runs.)

## Specifications

| Parameter | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `modifier` | `Modifier` | `Modifier` | Standard modifier to control size and layout. |
| `strokeWidth` | `Dp` | `8.dp` | Thickness of the track and progress arc. |
| `trackColor` | `Color` | `LightGray 20%` | Color of the background static circle. |
| `brush` | `Brush` | `SweepGradient` | A gradient brush applied to the progress arc. |
| `rotationPeriodMillis` | `Int` | `2000` | Time for one full base rotation (360°). |
| `cyclePeriodMillis` | `Int` | `1200` | Time for one full grow + shrink breathing cycle. |

## Implementation Details

- **Round Caps**: Uses `StrokeCap.Round` to give the "liquid" segment rounded, organic ends.
- **Inner Padding**: The drawing logic automatically accounts for `strokeWidth`. It calculates the arc diameter as `size - strokeWidth` to ensure no part of the stroke is clipped by the canvas bounds.
- **Hollow Center**: Uses `useCenter = false` and `Stroke` style for a modern, ring-like appearance.
