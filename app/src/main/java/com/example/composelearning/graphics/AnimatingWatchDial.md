# Animating Watch Dial

A smooth analog watch face drawn entirely on a Compose `Canvas` —
hour / minute / second hands, `1–12` numerals, and 60 ticks. Implemented in
[`AnimatingWatchDial.kt`](./AnimatingWatchDial.kt).

The whole thing is just **trigonometry on a circle** plus a **per-frame clock**.

---

## 1. The coordinate system

Compose's `DrawScope` uses screen coordinates:

- Origin `(0, 0)` is the **top-left** corner.
- `x` grows to the **right**, `y` grows **downward** (y is flipped vs. school maths).

So we first establish the dial's **center** and **radius**:

```kotlin
val radius = min(size.width, size.height) / 2f      // largest circle that fits
val center = Offset(size.width / 2f, size.height / 2f)  // center = (x, y)
```

Everything else is positioned **relative to `center`** and **scaled by `radius`**,
so the watch looks identical at any size (40.dp or 400.dp).

---

## 2. Placing things on the circumference

A point at angle `θ` and distance `r` from the center is:

```
x = center.x + r · sin(θ)
y = center.y − r · cos(θ)
```

### Why `sin` for x and `−cos` for y?

The textbook circle uses `(r·cosθ, r·sinθ)` with `0°` pointing **right** and angles
going **counter-clockwise**. A clock instead needs:

- `0°` to point **up** (12 o'clock), and
- angles to increase **clockwise**.

Swapping to `x = sinθ`, `y = −cosθ` rotates the circle so `θ = 0` lands at the top,
and the **minus** on `y` accounts for the downward y-axis so increasing angle moves
clockwise. Quick check:

| θ      | sinθ | −cosθ | position    |
|--------|------|-------|-------------|
| 0°     | 0    | −1    | top (12)    |
| 90°    | +1   | 0     | right (3)   |
| 180°   | 0    | +1    | bottom (6)  |
| 270°   | −1   | 0     | left (9)    |

---

## 3. Dividing the dial into ticks

> We divide the dial by `360 / number_of_ticks`.

For `N` evenly spaced marks, the angular step is `360° / N`, so tick `i` sits at:

```
angleᵢ = i · (360 / N)
```

For the watch `N = 60`, giving **6° per tick**:

```kotlin
for (i in 0 until 60) {
    val isHour = i % 5 == 0                 // every 5th tick = an hour mark
    val angleRad = Math.toRadians(i * 6.0)  // 360 / 60 = 6° per tick
    val sin = sin(angleRad).toFloat()
    val cos = cos(angleRad).toFloat()

    val inner = outer - tickLength
    val start = Offset(center.x + sin * inner, center.y - cos * inner)
    val end   = Offset(center.x + sin * outer, center.y - cos * outer)
    drawLine(...)
}
```

Each tick is a line between **two radii** (`inner` → `outer`) along the same angle.
Hour ticks (`i % 5 == 0`) are drawn longer, thicker and brighter than minute ticks.

### Numerals

The `1–12` numbers are the same idea with `N = 12` (`360 / 12 = 30°` per number),
drawn on a slightly smaller radius so they sit inside the ticks:

```kotlin
for (hour in 1..12) {
    val angleRad = Math.toRadians(hour * 30.0)   // 30° per hour
    val x = center.x + sin(angleRad) * numeralRadius
    val y = center.y - cos(angleRad) * numeralRadius
    // measure the text and offset by half its width/height so it is centered on (x, y)
}
```

`TextMeasurer` + `drawText` are used to lay out the glyphs; we subtract half the
measured width/height from `(x, y)` so the number is **centered** on the point
rather than starting there.

---

## 4. Animating with the current milliseconds

> We animate using the current milliseconds.

The face is redrawn **every frame**, reading the wall clock each time:

```kotlin
val timeState = remember { mutableLongStateOf(System.currentTimeMillis()) }
LaunchedEffect(Unit) {
    while (true) {
        withFrameMillis { timeState.longValue = System.currentTimeMillis() }
    }
}
```

`withFrameMillis` suspends until the next display frame (~16ms at 60Hz), so the
loop runs in lock-step with the screen refresh — no manual timers, no jank.

### Fractional time → silky sweep

The trick to a *smooth* (non-ticking) second hand is to use the **milliseconds**
field, not just whole seconds:

```kotlin
val fractionalSecond = SECOND + MILLISECOND / 1000f   // e.g. 23.476
val fractionalMinute = MINUTE + fractionalSecond / 60f
val fractionalHour   = (HOUR % 12) + fractionalMinute / 60f
```

Because `fractionalSecond` changes a little on every frame, the second hand moves a
little on every frame instead of jumping once per second. Carrying the fraction up
the chain also makes the minute hand creep as the seconds pass, and the hour hand
creep as the minutes pass — exactly like a real mechanical watch.

### Angles for the hands

Convert each fractional unit into degrees (`0°` = 12 o'clock, clockwise):

| Hand   | Full sweep | Degrees per unit | Formula                  |
|--------|------------|------------------|--------------------------|
| Second | 60 s       | `360 / 60 = 6`   | `fractionalSecond * 6`   |
| Minute | 60 min     | `360 / 60 = 6`   | `fractionalMinute * 6`   |
| Hour   | 12 h       | `360 / 12 = 30`  | `fractionalHour * 30`    |

Each hand is then drawn straight **up** from the center and rotated into place:

```kotlin
rotate(degrees = angleDegrees, pivot = center) {
    drawLine(
        start = Offset(center.x, center.y + tail),   // short counterweight tail
        end   = Offset(center.x, center.y - length), // up toward 12 o'clock
        ...
    )
}
```

Using `rotate(...)` is equivalent to computing the endpoint with the
`sin/−cos` formulas above, but it's cleaner and lets us give each hand a tail past
the hub.

---

## 5. Performance — never recompose, cache the static dial

A naïve version reads `timeMillis` in the composable body and computes the angles
there. That makes Compose **recompose the whole composable every single frame**
(the Layout Inspector shows recomposition counts in the *thousands*). Two rules
fix it:

### 5.1 Read time in the *draw* phase, not composition

Compose has three phases — **composition → layout → draw**. A snapshot-state read
is tracked by whichever phase reads it, and a write only invalidates *that* phase.
So if the only place we read `timeState` is inside the draw block, a new frame
invalidates **drawing only** — composition stays at **1**.

```kotlin
Spacer(
    modifier = modifier.drawWithCache {
        ...
        onDrawBehind {
            calendar.timeInMillis = timeState.longValue   // <-- read in DRAW phase
            // compute angles + draw hands here
        }
    }
)
```

> Rule of thumb: for rapidly-changing values (animation, scroll, time), read the
> state as **late** as possible — in `Modifier.drawBehind` / `drawWithCache` /
> `graphicsLayer { }` — to skip recomposition (and often layout) entirely.

### 5.2 Cache the static dial in an `ImageBitmap`

The bezel, ticks and 12 numerals never move, yet redrawing 60 lines + measuring 12
strings every frame is wasted work. `drawWithCache` runs its build block **only
when the size changes**, so we render the static parts once into an `ImageBitmap`
and just blit it each frame:

```kotlin
modifier.drawWithCache {
    val staticDial = ImageBitmap(size.width.toInt(), size.height.toInt())
    CanvasDrawScope().draw(this, LayoutDirection.Ltr, Canvas(staticDial), size) {
        drawFace(center, radius)
        drawTicks(center, radius)
        drawNumerals(center, radius, textMeasurer)   // built ONCE
    }
    onDrawBehind {
        drawImage(staticDial)                          // cheap blit every frame
        // ...draw the 3 hands + hub
    }
}
```

Net result: **recomposition count = 1**, and each frame only does one image draw
plus three lines and two small circles.

### 5.3 Side-by-side: the one change that matters

The cache in 5.2 is a bonus. The change that actually killed the 1004
recompositions is *where the time is read* — body vs. draw lambda:

<table>
<tr>
<th>❌ Before — recomposes every frame</th>
<th>✅ After — redraws every frame</th>
</tr>
<tr>
<td valign="top">

```kotlin
@Composable
fun AnimatingWatchDial(modifier: Modifier) {
    var timeMillis by remember {
        mutableLongStateOf(System.currentTimeMillis())
    }
    LaunchedEffect(Unit) {
        while (true) withFrameMillis {
            timeMillis = System.currentTimeMillis()
        }
    }
    val calendar = remember { Calendar.getInstance() }

    // ❌ read in the COMPOSABLE BODY
    calendar.timeInMillis = timeMillis
    val secondAngle = /* … */   // computed during
    val minuteAngle = /* … */   // COMPOSITION
    val hourAngle   = /* … */

    Canvas(modifier) {
        // lambda captured the angles above
        drawFace(center, radius)
        drawTicks(center, radius)
        drawNumerals(center, radius, tm)
        drawHand(hourAngle,   /* … */)
        drawHand(minuteAngle, /* … */)
        drawHand(secondAngle, /* … */)
    }
}
// read subscribes COMPOSITION
// → every frame recomposes  (≈1004)
```

</td>
<td valign="top">

```kotlin
@Composable
fun AnimatingWatchDial(modifier: Modifier) {
    val timeState = remember {
        mutableLongStateOf(System.currentTimeMillis())
    }
    LaunchedEffect(Unit) {
        while (true) withFrameMillis {
            timeState.longValue = System.currentTimeMillis()
        }
    }
    val calendar = remember { Calendar.getInstance() }

    Canvas(modifier) {
        // ✅ read in the DRAW lambda
        calendar.timeInMillis = timeState.longValue
        val secondAngle = /* … */   // computed during
        val minuteAngle = /* … */   // DRAW
        val hourAngle   = /* … */

        drawFace(center, radius)
        drawTicks(center, radius)
        drawNumerals(center, radius, tm)
        drawHand(hourAngle,   /* … */)
        drawHand(minuteAngle, /* … */)
        drawHand(secondAngle, /* … */)
    }
}
// read subscribes DRAW
// → every frame only redraws  (recompose = 1)
```

</td>
</tr>
</table>

|                       | ❌ Before                     | ✅ After                          |
|-----------------------|-------------------------------|-----------------------------------|
| Time read happens in  | composable body               | `Canvas {}` draw lambda           |
| Phase subscribed      | **Composition**               | **Draw**                          |
| Work per frame        | recompose → layout → draw     | draw only                         |
| Recompositions (~16s) | **≈1004**                     | **1**                             |

> The fix is a one-line move, not a rewrite: the `Canvas { … }` draw block was
> already there — the only thing that changed is that the time read now lives
> *inside* it instead of above it.

---

## 6. Draw order (back to front)

Painters' algorithm — later draws sit on top:

1. **Cached static dial** — bezel + radial-gradient face, ticks, numerals (blitted)
2. **Hour → Minute → Second** hands
3. **Center hub** (red cap + dark dot) so the hands' pivot looks clean

---

## 7. Customization knobs

- **Tick count** — change the `60` / `6°` step to make a different dial (e.g. 12 ticks).
- **Colors** — the `private val` palette at the top of the file (`FaceCenter`,
  `Bezel`, `SecondHand`, …).
- **Hand proportions** — the `length`, `tail`, `strokeWidth` multipliers (all
  expressed as fractions of `radius`).
- **Numeral size / radius** — `radius * 0.13f` font size, `radius * 0.72f` ring.

---

## 8. Preview

`AnimatingWatchDialPreview` renders the dial square (`aspectRatio(1f)`) on a dark
background. The Android Studio preview is a **static snapshot** of the current
time; run the app to see the hands sweep.
