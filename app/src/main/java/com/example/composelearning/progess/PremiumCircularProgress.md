# Premium Circular Progress Indicator

A high-performance, fluid indeterminate circular loader built with Jetpack Compose Canvas.

## Architecture

### 1. Performance
- **Zero Recomposition**: The component reads `animationProgress` and `globalRotation` strictly inside the `Canvas` DrawScope. This bypasses the Compose recomposition phase, ensuring the UI thread is free for layout and other tasks. Only the drawing phase is invalidated at 60/120fps.
- **Infinite Transition**: Leverages `rememberInfiniteTransition` to synchronize the global spin and the elastic sweep.

### 2. Mathematics

The loader uses two distinct animations working in tandem:

#### Global Rotation
- **Range**: 0° to 360°
- **Duration**: 2000ms
- **Easing**: `LinearEasing`
- **Purpose**: Provides the base continuous spinning motion.

- **Final Angle Calculation**:
    - **Base Angle (`-90f`)**: Sets the starting position to the top of the circle (12 o'clock).
    - **`rotation`**: Constant angular velocity (360° every 2 seconds) applied to the entire system.
    - **`startOffset`**: The "Tail Chase" offset. In Phase 2, this value increases as the sweep decreases, shifting the start of the arc forward to create the contraction effect.
    - **Formula**: `finalStartAngle = -90f + rotation + startOffset`

#### Visual Math Reference

![Visualization Progress](../../../../../../../../visualization_progress.jpeg)

| Phase | Progress | Local $p$ | Sweep Angle | Start Offset | Visual Action |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Start** | 0.0 | 0.0 | $30^\circ$ | $0^\circ$ | Small segment at start position. |
| **Mid-Expand** | 0.25 | 0.5 | $150^\circ$ | $0^\circ$ | Segment growing forward. |
| **Peak** | 0.5 | 1.0 | $270^\circ$ | $0^\circ$ | Maximum length reached. |
| **Mid-Contract** | 0.75 | 0.5 | $150^\circ$ | $120^\circ$ | Tail moving forward, shrinking segment. |
| **End** | 1.0 | 1.0 | $30^\circ$ | $240^\circ$ | Back to minimum size, tail caught up. |
- **Range**: 0.0 to 1.0 (normalized)
- **Duration**: 1200ms
- **Easing**: `FastOutSlowInEasing`
- **Phase Normalization**:
    - **`0.5f` (Timeline Splitting)**: Divides the animation cycle into two equal windows. This ensures visual symmetry and a rhythmic pulse, where growth and contraction happen at the same relative speed.
    - **`2` (Scaling Factor)**: Because each phase occupies only half ($0.5$) of the total $0..1$ timeline, we multiply by $2$ to map that sub-window back to a full $0..1$ range ($p$). 
    - **Normalization Math**: $\text{Scaling Factor} = \frac{\text{Target Range Max}}{\text{Current Window Max}} = \frac{1.0}{0.5} = \mathbf{2}$.
    - **Benefit**: This allows the drawing equations to reach $100\%$ completion (full expansion or full contraction) exactly at the midpoint of the total animation duration.
- **Logic**:
    - **Expansion Phase (Progress 0.0 → 0.5)**: 
        - `p = progress * 2`
        - `sweep = minSweep + (maxSweep - minSweep) * p`
        - The "head" of the arc moves forward while the "tail" stays anchored.
    - **Contraction Phase (Progress 0.5 → 1.0)**:
        - `p = (progress - 0.5) * 2`
        - `sweep = maxSweep - (maxSweep - minSweep) * p`
        - `startOffset = (maxSweep - minSweep) * p`
        - The "tail" accelerates forward to catch up with the "head", reducing the sweep length back to minimum.

### 3. Coordinate Orientation
The base angle is offset by **-90 degrees**. This ensures that the progress arc's expansion begins exactly at the 12 o'clock position, which is the standard expectation for circular indicators.

## Specifications

| Parameter | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `modifier` | `Modifier` | `Modifier` | Standard modifier to control size and layout. |
| `strokeWidth` | `Dp` | `8.dp` | Thickness of the track and progress arc. |
| `trackColor` | `Color` | `LightGray 20%` | Color of the background static circle. |
| `brush` | `Brush` | `SweepGradient` | A gradient brush applied to the progress arc. |

## Implementation Details

- **Round Caps**: Uses `StrokeCap.Round` to give the "liquid" segment rounded, organic ends.
- **Inner Padding**: The drawing logic automatically accounts for `strokeWidth`. It calculates the arc diameter as `size - strokeWidth` to ensure no part of the stroke is clipped by the canvas bounds.
- **Hollow Center**: Uses `useCenter = false` and `Stroke` style for a modern, ring-like appearance.
