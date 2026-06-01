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

#### Elastic Sweep (The "Liquid" Feel)
- **Range**: 0.0 to 1.0 (normalized)
- **Duration**: 1200ms
- **Easing**: `FastOutSlowInEasing`
- **Logic**:
    - **Expansion Phase (Progress 0.0 → 0.5)**: 
        - `sweep = minSweep + (maxSweep - minSweep) * (progress * 2)`
        - The "head" of the arc moves forward while the "tail" stays anchored.
    - **Contraction Phase (Progress 0.5 → 1.0)**:
        - `sweep = maxSweep - (maxSweep - minSweep) * ((progress - 0.5) * 2)`
        - `startOffset = (maxSweep - minSweep) * ((progress - 0.5) * 2)`
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
