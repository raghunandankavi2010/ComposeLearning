# Riveo Page Curl — The Maths & Shader

A Compose/AGSL port of William Candillon's *"Page Curl — Can it be done in React Native?"*
([`season5/src/Riveo`](https://github.com/wcandillon/can-it-be-done-in-react-native)).
The original is built with React-Native-Skia + an SkSL `RuntimeShader`. This document
explains **every calculation** in the shader and how Compose feeds it.

---

## 1. The idea

A flat page (the card photo) is dragged from right to left. Instead of just sliding, the
right part of the page **wraps over an invisible cylinder** of fixed radius `R`, so you see:

- the **front** of the page curving away and getting darker near the fold,
- the **back / underside** of the page coming back toward you,
- a **drop shadow** cast on whatever is behind the page (here: a red "delete" panel),
- the **flat** remainder of the page, untouched, on the left.

Everything is computed **per pixel** in a fragment shader. There is no geometry, no mesh —
just a function that, for each output pixel `xy`, decides *what colour to show* by sampling
the original image at a cleverly chosen source coordinate.

```
   flat page        curling band        gone (shadow)
 |............|=================(  ( (  |              |
 left        fold(x)         cylinder   x+R          right edge
                              radius R
```

---

## 2. Inputs (uniforms)

| Uniform        | Meaning                                                        |
|----------------|---------------------------------------------------------------|
| `image`        | the rasterized card content (a *sampler*; read with `.eval`)  |
| `pointer`      | current drag X position, in pixels                            |
| `origin`       | X position where the drag started, in pixels                 |
| `container`    | the inner card rect `(left, top, right, bottom)` in pixels    |
| `cornerRadius` | rounded-corner radius of that rect, in pixels                 |
| `resolution`   | the layer size `(width, height)` in pixels                    |
| `R` (const)    | radius of the curl cylinder = `150` px                        |

`pointer` and `origin` are the **only** things that change while you drag. When
`pointer == origin` the page is perfectly flat — that's the resting state.

---

## 3. The fold line

```glsl
float dx = origin - pointer;   // how far the finger has moved left (>0 when dragging left)
float x  = container.z - dx;   // X of the vertical fold line (container.z = right edge)
float d  = xy.x - x;           // signed horizontal distance of THIS pixel from the fold
```

- At rest `dx = 0`, so `x = right edge`, and `d ≤ 0` for every pixel in the card → all flat.
- Dragging left increases `dx`, moving the fold line `x` leftwards.
- `d` is the master discriminator:
  - `d ≤ 0` → pixel is **left of the fold** → flat page.
  - `0 < d ≤ R` → pixel is **on the curl** (wrapped over the cylinder).
  - `d > R` → pixel is **past the curl** → empty space / shadow.

---

## 4. The cylinder model (`0 < d ≤ R`)

Think of the page bending over a horizontal cylinder of radius `R`. Look at it edge-on: the
cross-section is a circle of radius `R`. A pixel at horizontal distance `d` from the fold
corresponds to a point on that circle whose angle from the top is:

```glsl
float theta = asin(d / R);     // 0 at the fold, π/2 at the far side of the cylinder
```

`d / R` is the normalized horizontal offset (0…1), and `asin` converts that horizontal
position on the circle into the **wrap angle** `theta ∈ [0, π/2]`.

The page is a strip wrapped on the cylinder, so the same vertical screen column maps to
**two** points along the page's arc length:

```glsl
float d1 = theta * R;          // arc length to the FRONT-facing point
float d2 = (PI - theta) * R;   // arc length to the BACK-facing point (the underside)
```

Arc length = radius × angle. The front of the curl is reached after sweeping `theta`; the
underside is reached after sweeping `π − theta` (the rest of the half-turn). We convert
those arc lengths back into source X positions by adding them to the fold line `x`:

```glsl
p1.x = x + d1;     // where to sample the image for the FRONT of the curl
p2.x = x + d2;     // where to sample the image for the BACK / underside
```

### 4.1 Perspective fake (the scale-about-centre projection)

A real curl also foreshortens vertically and the underside looks slightly larger. The
original does this with a tiny uniform scale about the layer centre, applied to the Y
coordinate:

```glsl
// front
float2 s1 = float2(1.0 + (1.0 - sin(PI/2 + theta)) * 0.1);
// back
float2 s2 = float2(1.1 + sin(PI/2 + theta) * 0.1);
```

`sin(PI/2 + theta) = cos(theta)`, which runs 1 → 0 as `theta` goes 0 → π/2. So:
- front scale `s1` runs `1.0 → 1.1` across the curl,
- back scale `s2` runs `1.2 → 1.1`.

These scales are applied through `project`, then we keep only the **Y** component
(`uv.y`) for the sample point — the X already came from the arc length.

#### The `project` simplification (matrices → one line)

The original `ShaderLib/Core.ts` builds 3×3 affine matrices and inverts them:

```glsl
mat3 translate(vec2 p);                 // translation matrix
mat3 scale(vec2 s, vec2 p) = T(p) · S(s) · T(-p);   // scale about pivot p
vec2 project(vec2 p, mat3 m) = (inverse(m) · vec3(p,1)).xy;   // un-project
```

`project` un-projects a screen point back into the un-scaled texture space, i.e. it applies
`M⁻¹`. But `M` here is only ever a **uniform scale `s` about a pivot `c`**:

```
M(q)   = c + s · (q − c)
M⁻¹(p) = c + (p − c) / s
```

So the entire matrix/`inverse()` machinery collapses to a single exact expression:

```glsl
float2 project(float2 p, float2 s, float2 c) {
    return c + (p - c) / s;     // identical to inverse(scale(s,c)) · p
}
```

The Compose port uses this form: smaller, faster, and it sidesteps any AGSL matrix-inverse
quirks. The result is bit-for-bit the same because the scale is uniform.

### 4.2 Choosing the colour

With both candidate source points computed, pick what's visible (back occludes front, front
occludes the empty area):

```glsl
if (inRect(p2, container)) {
    color = image.eval(p2);                 // underside is on top → draw it
} else if (inRect(p1, container)) {
    color = image.eval(p1);                 // otherwise the front face
    color.rgb *= pow(clamp((R - d) / R, 0, 1), 0.2);   // shade it darker toward the fold
} else if (inRect(xy, container)) {
    color = TRANSPARENT; color.a = 0.5;     // faint shadow on the card area
}
```

The shading term `pow(clamp((R - d)/R, 0, 1), 0.2)`:
- `(R - d)/R` runs 1 → 0 from the fold (`d=0`) to the cylinder edge (`d=R`),
- `pow(t, 0.2)` is a gentle curve (the 5th root) so the darkening is subtle, not a hard ramp,
- multiplying `rgb` (not `a`) darkens the front face as it turns away from the light.

---

## 5. Past the curl (`d > R`)

Beyond the cylinder the page no longer exists — show whatever is behind it (transparent),
plus a fading shadow on the card footprint:

```glsl
color = TRANSPARENT;
if (inRect(xy, container)) {
    color.a = mix(0.5, 0.0, (d - R) / R);   // shadow strongest at the curl, gone by d = 2R
}
```

`(d - R)/R` runs 0 → 1 over the band `R … 2R`, so `mix(0.5, 0, …)` fades the shadow alpha
from `0.5` down to `0`. Past `2R` it's fully transparent and the red delete panel shows.

---

## 6. Flat page (`d ≤ 0`)

Left of the fold the page is untouched, so we just echo the original image:

```glsl
float2 s   = float2(1.2);
float2 uv  = project(xy, s, center);
float2 p   = float2(x + abs(d) + PI*R, uv.y);
color = inRect(p, container) ? image.eval(p) : image.eval(xy);
```

`p.x = x + |d| + π·R` is pushed far to the right (≈ `+471` px), so `inRect(p)` is essentially
always false here and the branch falls through to `image.eval(xy)` — the flat, original
pixel. (The shifted-sample branch only matters for an extreme wrap-around that the layout
never reaches.)

---

## 7. `inRect` — rounded-rectangle test

Sampling outside the card must read "nothing". `inRect` first does the plain rectangle test,
then rejects each of the four rounded corners with a circle test:

```glsl
// inside the bounding box?
bool inside = p.x > L && p.x < R_ && p.y > T && p.y < B;
// in a corner quadrant? must be within `cornerRadius` of that corner's centre.
length(p - cornerCentre) < cornerRadius
```

This keeps the curl edges rounded exactly like the card.

---

## 8. SkSL → AGSL translation notes

| Skia SkSL                                | Android AGSL (this port)                          |
|------------------------------------------|---------------------------------------------------|
| `vec4 main(float2 xy)`                   | `half4 main(float2 xy)`                            |
| `uniform shader image;` + `image.eval()` | same; sampler bound via `createRuntimeShaderEffect(shader, "image")` |
| sampler coords                           | **pixel** coordinates (not normalized UVs)        |
| `mat3` + `inverse()` in `project`        | replaced by `c + (p - c)/s` (see §4.1)            |
| `Core.ts` `Paint`/SDF/blend helpers      | **not ported** — unused by the page curl          |
| `vec2/vec4`                              | `float2`/`half4` (AGSL types)                      |
| host multiplies uniforms by `PixelRatio` | not needed — Compose `graphicsLayer` works in px  |

The Kotlin side lives in
[`presentation/PageCurlShader.kt`](presentation/PageCurlShader.kt):
`createPageCurlShader()` builds the `RuntimeShader`, and `Modifier.pageCurl(...)` attaches it
as a `RenderEffect` on a `graphicsLayer`.

### Feeding uniforms & performance

```kotlin
Modifier.graphicsLayer {
    shader.setFloatUniform("resolution", size.width, size.height)
    shader.setFloatUniform("pointer", pointer())     // read inside the layer block
    shader.setFloatUniform("origin", origin())
    shader.setFloatUniform("container", padPx, padPx, size.width - padPx, size.height - padPx)
    shader.setFloatUniform("cornerRadius", radiusPx)
    renderEffect = RenderEffect.createRuntimeShaderEffect(shader, "image").asComposeRenderEffect()
}
```

`pointer`/`origin` are passed as **lambdas** and read *inside* the `graphicsLayer` block.
Compose then scopes the per-frame drag invalidation to **this draw layer only** — the
composable never recomposes while you drag. (Same rule the project's other AGSL demos in
`shaders/ShaderExample.kt` follow.)

`container` and `cornerRadius` use `dp.toPx()` because `GraphicsLayerScope` is a `Density`.

---

## 9. Gesture & animation

In [`presentation/PageCurlCard.kt`](presentation/PageCurlCard.kt):

| Event         | Action                                                            |
|---------------|------------------------------------------------------------------|
| `onDragStart` | `origin = pointer = touch.x` (begin the fold at the finger)       |
| `onDrag`      | `pointer = change.position.x` (fold follows the finger)           |
| `onDragEnd`   | animate `pointer → origin` over **450 ms**, `FastOutSlowInEasing` |

`pointer` is an `Animatable<Float>`; `origin` is plain state. When they're equal, `dx = 0`,
so the page is flat. That's why the spring-back simply animates `pointer` back to `origin`.

---

## 10. Architecture (clean + MVI)

```
riveo/
├── domain/   model.Project · repository.ProjectRepository · usecase.GetProjectsUseCase
├── data/     ProjectRepositoryImpl (in-memory, picsum URLs)
└── presentation/
    ├── RiveoContract.kt   (RiveoState, RiveoIntent)
    ├── RiveoViewModel.kt  (StateFlow + manual Factory, no Hilt)
    ├── RiveoScreen.kt     (Route → Content; collectAsStateWithLifecycle)
    ├── PageCurlCard.kt    (gesture + curl layer + delete background)
    └── PageCurlShader.kt  (AGSL string + Modifier.pageCurl)
```

The ViewModel owns only the **durable** state (the project list). The **transient** curl/drag
values stay local to the card as animation state — pushing 60fps drag updates through the
ViewModel would be an anti-pattern and would defeat the draw-layer invalidation scoping.
```
