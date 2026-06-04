# Clear To-Do — Pinch to Create

Compose port of the signature interaction in
`can-it-be-done-in-react-native/season2/clear`: **pinch two rows apart** and a "Create a new
Task" row unfolds in the gap. Release past a threshold to insert it. (The task list uses
Clear's red→gold gradient; the fold reuses the same 3-D crease idea as the Fold Card sample.)

## The maths

A two-finger pinch reports an incremental `zoom` each frame and a `centroid`:

```
focal  = round(centroid.y / taskHeight)          // which boundary to open (set once, at start)
raw   *= zoom                                      // reconstruct total spread
open   = clamp(raw − 1, 0, 1)                      // 0 = closed, 1 = one row-height gap
```

The list opens symmetrically around the focal boundary. With `half = open · taskHeight / 2`:

```
row.translationY = (index < focal) ?  −half  :  +half
```

so rows above move up and rows below move down, opening a gap of `2·half` centred on
`focal · taskHeight`.

**The unfolding create row** fills that gap with two faces folding about the crease (identical
to the Fold Card maths, but driven by `unfold = open`):

| face   | pivot                | rotation              |
|--------|----------------------|-----------------------|
| top    | bottom edge `(0.5,1)`| `90° · (1 − unfold)`  |
| bottom | top edge `(0.5,0)`   | `−90° · (1 − unfold)` |

At `open = 1` the faces are flat (a full row) and the label is fully opaque; at `open → 0` they
fold to ±90° (edge-on, invisible).

**Commit.** On release, if `open ≥ 0.6` we dispatch `CreateTaskAt(focal)` (the ViewModel
inserts a task); either way `open` animates back to 0.

## Compose specifics
The pinch uses `awaitEachGesture` + `calculateZoom` + `calculateCentroid`, driving an
`Animatable`. Architecture: `domain` (TaskItem, repository, use case) → `data` →
`presentation` (MVI contract with `CreateTaskAt`, ViewModel, screen).
