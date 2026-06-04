# Apple Wallet — Collapsing Card Stack

Compose port of `can-it-be-done-in-react-native/the-10-min/src/Wallet`. A vertical list of
cards where, as you scroll, cards **pile up at the top**, shrinking and fading as they stack —
and slide back down as you scroll up.

## The maths

Each card lives in a fixed slot of height `CH = cardHeight + 2·margin`. Let `y` be the scroll
offset (px), `index` the card's position, and `H` the viewport height.

```
position = index·CH − y        // distance of the card from the top of the viewport
```

Four reference points along `position`:

| name          | value      | meaning                              |
|---------------|------------|--------------------------------------|
| disappearing  | `−CH`      | fully piled / about to vanish at top |
| top           | `0`        | resting at the top edge              |
| bottom        | `H − CH`   | resting at the bottom edge           |
| appearing     | `H`        | just entering from the bottom        |

**Sticky translation.** A card scrolls normally until `y` reaches its slot, then pins to the
top:

```
stick = y − min(y, index·CH)        //  0 while y ≤ index·CH, then (y − index·CH)
```

Because the scrolling `Column` already moves the card by `−y`, adding `stick` as
`translationY` keeps it on screen at `position` until it reaches the top, then holds it at 0.
A small `bottom` nudge (`0 → −CH/4` over `[bottom, appearing]`) eases cards in from the edge.

**Scale & alpha** both interpolate `0.5 → 1 → 1 → 0.5` across
`[disappearing, top, bottom, appearing]`, so cards are full-size in the middle band and shrink
+ fade as they pile at the top or wait at the bottom — the classic wallet collapse.

## Compose specifics

- `y` is read as `scroll.value` **inside each card's `graphicsLayer` block**, so scrolling
  invalidates only the draw layers — no recomposition.
- `interp` / `interp4` are clamped linear interpolations mirroring Reanimated's `interpolate`
  with `extrapolate: "clamp"`.
- Cards are gradient `Box`es drawn in Compose (no image assets).

## Architecture
`domain` (WalletCard, repository, use case) → `data` (in-memory cards) → `presentation`
(`WalletContract`, `WalletViewModel` + Factory, `WalletScreen`). The scroll position is view
state; the ViewModel only owns the card list.
