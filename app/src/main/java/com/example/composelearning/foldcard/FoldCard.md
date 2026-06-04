# Fold Card

Compose port of `can-it-be-done-in-react-native/bonuses/fold-card`. Pinch vertically to bend a
card in half in 3-D; release and it springs flat.

## The maths

A pinch reports a `scale` that runs `1 → 0.5`. We map it to a fold factor:

```
f = (1 − scale) / 0.5          (clamped to [0,1])   // 0 = flat, 1 = fully folded
```

The card is split into a **top** and **bottom** half. Each half pivots about the **shared
crease**, so they stay joined:

| half   | pivot (`transformOrigin`) | rotation        |
|--------|---------------------------|-----------------|
| top    | bottom edge `(0.5, 1)`    | `+90° · f`      |
| bottom | top edge `(0.5, 0)`       | `−90° · f`      |

A perspective camera (`cameraDistance`) makes the receding free edges foreshorten, and a black
overlay fades in (`alpha = 0.6 · f`) to shade the crease. Because each half pivots exactly on
the crease, no translation compensation is needed (the original pivoted at the centre and added
a `translateZ` term to reconnect the halves).

Each half shows the correct slice of the card by clipping a full-size `CardFace` to a
half-height box (the bottom half offsets its face up by `−H/2`).

## Compose specifics
The pinch is read with `awaitEachGesture` + `calculateZoom`, accumulating into an `Animatable`
`scale`; on release it springs back to 1 (`Spring.DampingRatioMediumBouncy`). Architecture:
`domain` (FoldCardItem, repository, use case) → `data` → `presentation` (MVI + screen).
