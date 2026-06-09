# Unique Paths — DFS + Backtracking Visualizer

An animated Compose screen that *replays* a depth-first search counting the number
of unique paths from the top-left to the bottom-right of a grid, moving in 4
directions without revisiting a cell. Implemented in
[`UniquePathsVisualizer.kt`](./UniquePathsVisualizer.kt).

The core idea: **the algorithm and the animation are decoupled.** We run the real
DFS once up front to record a flat list of *steps*, then a coroutine plays those
steps back at a controllable speed. This keeps the recursion correct and the UI
trivially "scrubbable" (play / pause / single-step / reset).

---

## 1. The algorithm being visualized

It is the exact algorithm from the DSAlgo project's `programs.neetcode.uniquePaths`:

```text
dfs(r, c):
    if out of bounds, or cell is an obstacle, or cell already visited -> return
    if cell is the target (bottom-right)                              -> count++ ; return
    mark cell visited (grid[r][c] = 2)
    dfs(down); dfs(up); dfs(right); dfs(left)
    unmark cell (grid[r][c] = 0)   // backtrack
```

- `0` = open, `1` = obstacle, `2` = visited **on the current path**.
- The start cell is marked visited and recursed from; the **target is never marked**
  (it returns as soon as it's reached), so the same target can be reached again via
  a different path.
- The `grid[r][c] = 2` … `grid[r][c] = 0` pair is classic **backtracking**: a cell
  is "owned" by the current path while we explore below it, then released so other
  paths can use it.

---

## 2. Recording steps instead of just counting

`buildDfsSteps()` is the same DFS, but instead of returning an `Int` it appends an
event every time the search changes the grid:

```kotlin
private sealed interface DfsStep {
    data class Enter(val r: Int, val c: Int) : DfsStep  // push cell onto the path
    data class Leave(val r: Int, val c: Int) : DfsStep  // backtrack / pop cell
    data object PathFound : DfsStep                       // target reached -> count++
}
```

The mapping from algorithm to events:

| Algorithm moment                    | Recorded step        |
|-------------------------------------|----------------------|
| successfully step into a cell       | `Enter(r, c)`        |
| reach the target                    | `Enter` → `PathFound` → `Leave` |
| finish a cell and backtrack         | `Leave(r, c)`        |
| hit a wall / OOB / visited cell     | *(nothing recorded)* |

Because it walks the identical recursion in the identical order
(`down → up → right → left`), the number of `PathFound` events **equals** the value
`uniquePaths()` would return. That count is shown as the "total" in the header.

> Why pre-record? The recursion is synchronous and finishes in microseconds — you
> can't "watch" it. Flattening it into a list lets the UI advance one event per
> frame/tick and even step backwards conceptually (via Reset + replay).

---

## 3. Playback state

All playback state is **keyed to `steps`** with `remember(steps)`, so editing the
grid (which produces a new `steps` list) automatically resets everything:

```kotlin
val steps      = remember(grid)  { buildDfsSteps(grid) }
val pathStack  = remember(steps) { mutableStateListOf<Int>() } // cells on the path, in order
var stepIndex  by remember(steps) { mutableIntStateOf(0) }     // how far we've replayed
var pathsFound by remember(steps) { mutableIntStateOf(0) }
var justFound  by remember(steps) { mutableStateOf(false) }    // true only on a PathFound frame
var isPlaying  by remember(steps) { mutableStateOf(false) }
```

`pathStack` holds the encoded indices (`r * cols + c`) of the cells currently on the
recursion stack — i.e. the path being explored right now. Its **last element is the
head** (the cell DFS is "standing on").

Applying a step is a pure state mutation:

```kotlin
Enter -> pathStack.add(idx)                 ; justFound = false
Leave -> pathStack.removeAt(lastIndex)      ; justFound = false
PathFound -> pathsFound++                    ; justFound = true
```

On a `PathFound` frame nothing is popped yet, so `pathStack` still contains the whole
discovered path — which is exactly what we flash green.

---

## 4. Mapping state → cell color

Each cell computes a target color from the current state, then animates to it:

```kotlin
val target = when {
    isObstacle              -> ObstacleColor   // dark slate, "✕"
    justFound && onPath     -> FoundColor      // green flash for the completed path
    isHead                  -> HeadColor       // orange = currently visiting
    onPath                  -> PathColor       // blue   = on the current path
    isStart                 -> StartColor      // "S"
    isEnd                   -> EndColor         // "E"
    else                    -> OpenColor
}
val color by animateColorAsState(target, tween(180))
```

- `onPath  = pathStack.contains(idx)`
- `isHead  = pathStack.lastOrNull() == idx`

`animateColorAsState` is what makes transitions glide instead of snapping, and the
head cell additionally scales up slightly (`animateFloatAsState`) with a border, so
your eye can follow the search.

---

## 5. The play loop

A single `LaunchedEffect` keyed on `isPlaying`/`steps` drives auto-play:

```kotlin
LaunchedEffect(isPlaying, steps) {
    if (!isPlaying) return@LaunchedEffect
    while (isPlaying && stepIndex < steps.size) {
        applyStep(steps[stepIndex]); stepIndex++
        delay(speedMs.toLong())          // read fresh each iteration -> live speed slider
    }
    if (stepIndex >= steps.size) isPlaying = false   // auto-stop at the end
}
```

- **Pause** flips `isPlaying`, which cancels the coroutine (the effect re-launches and
  returns early). **Play** resumes from the current `stepIndex`.
- **Step** calls `applyStep` once for frame-by-frame inspection.
- **Reset** clears the stack, index and counters.
- The **speed slider** is read *inside* the loop, so dragging it changes the cadence
  immediately without restarting playback. (It's inverted so right = faster = smaller
  delay.)

---

## 6. Interaction & presets

- **Tap a cell while paused** to toggle a wall. This rebuilds `grid` immutably
  (`grid.mapIndexed { ... }`), which makes `steps` recompute and — because the
  playback state is `remember(steps)`-keyed — resets the animation automatically.
  Start/End cells are not toggleable.
- **Presets:** "Example 4×4 → 2" (matches the DSAlgo `main()` output) and
  "Open 3×3 → 12" (the number of self-avoiding corner-to-corner paths in a 3×3 grid).

---

## 7. Performance notes

Unlike a continuous animation (e.g. the watch dial), this is **event-driven**: state
changes only once per `delay(speedMs)` tick, so recompositions happen a handful of
times per second on a tiny grid — there is no per-frame recomposition loop. When
paused, the coroutine is cancelled and nothing recomposes at all.

`pathStack.contains(idx)` / `lastOrNull()` are read per cell each recomposition; on
grids this size that's negligible. For a *large* grid you'd switch the membership
check to a `Set`/`mutableStateMapOf` and the cell grid to a `LazyVerticalGrid`.

---

## 8. Preview

`UniquePathsVisualizerPreview` renders the whole screen (tall, `heightDp = 800`).
The preview is a static snapshot at step 0 — run the app and press **Play** to watch
the search explore, backtrack, and flash each completed path green while the counter
climbs to the total.
