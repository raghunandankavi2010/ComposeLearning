# TriColorCircularProgressIndicator

A variant of [`PremiumCircularProgressIndicator`](PremiumCircularProgress.kt) that renders
the indeterminate progress arc as **three equal solid-colour segments** instead of a single
sweep gradient.

## What it does

It is an indeterminate (looping) circular spinner. A single arc grows and shrinks while the
whole thing rotates — exactly like the premium indicator. The only change is **how the arc is
painted**:

- Take the arc's current total length (its sweep, e.g. `75°`).
- Divide it into 3 equal parts → `75 / 3 = 25°` per part.
- Paint each third with its own solid colour (head → middle → tail).

So instead of one gradient flowing across the whole head/tail, you get three distinct colour
bands. The colour boundaries always sit at **1/3** and **2/3** of the arc.

## Why the bands stay correct while animating

The arc is not a fixed length — during each cycle the *head* races ahead (arc grows) and then
the *tail* catches up (arc shrinks), sweeping between `minSweep = 30°` and `maxSweep = 270°`.

Because each segment is `sweep / 3`, the split is **proportional**: when the arc is short all
three bands are short, and when it's long all three are long. The three colours therefore grow
and contract together and the boundaries never drift — there's always exactly one-third of the
visible arc per colour.

## Animation model (identical to the premium indicator)

| Aspect            | Behaviour                                                                 |
|-------------------|---------------------------------------------------------------------------|
| Clock             | A single continuously-accumulating clock via `withFrameNanos` (never resets) |
| Rotation          | Constant spin, one full turn per `rotationPeriodMillis` (default 2000 ms) |
| Grow / shrink     | One cycle per `cyclePeriodMillis` (default 1200 ms), `FastOutSlowInEasing` |
| Tail continuity   | `completed * stretch` is carried forward so the tail never jumps at a cycle seam |
| Min / max sweep   | `30°` … `270°`                                                            |

Drawing order: the tail segment is drawn first and the head last, so the head's rounded cap
stays on top and the leading edge looks clean as the arc stretches.

## Parameters

| Parameter              | Default                                  | Meaning                                  |
|------------------------|------------------------------------------|------------------------------------------|
| `modifier`             | `Modifier`                               | Standard modifier; set the size here.    |
| `strokeWidth`          | `8.dp`                                    | Thickness of the arc.                    |
| `colors`               | purple / blue / teal                      | **Exactly 3** colours, head → tail.      |
| `rotationPeriodMillis` | `2000`                                    | Time for one full rotation.              |
| `cyclePeriodMillis`    | `1200`                                    | Time for one grow + shrink cycle.        |

> `colors` must contain exactly 3 entries (enforced with `require`).

## Usage

```kotlin
// Default 3 colours
TriColorCircularProgressIndicator(modifier = Modifier.size(64.dp))

// Custom 3 colours + thicker stroke
TriColorCircularProgressIndicator(
    modifier = Modifier.size(64.dp),
    strokeWidth = 12.dp,
    colors = listOf(Color.Red, Color(0xFFFFBB33), Color(0xFF99CC00)),
)
```

A live demo is in `SmoothProgressBarScreen` under the
**"TriColorCircularProgressIndicator (3 solid segments)"** heading.

## Difference vs. PremiumCircularProgressIndicator

|                         | Premium (gradient)        | TriColor (3 segments)              |
|-------------------------|---------------------------|------------------------------------|
| Paint                   | One `Brush` sweep gradient | Three solid `Color` arcs           |
| Colour count            | Any (via brush stops)     | Exactly 3                          |
| Colour transition       | Smooth blend              | Hard edges at 1/3 and 2/3          |
| Motion / total progress | identical                 | identical                          |