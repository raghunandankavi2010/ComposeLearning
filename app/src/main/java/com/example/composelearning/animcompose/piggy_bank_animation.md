# 🐷 Realistic Piggy Bank & 3D Flipping Coin Canvas Animation

A comprehensive guide on creating a photorealistic, physically interactive **Piggy Bank with 3D Coin Flipping & Drop Animation** using **Jetpack Compose Canvas** and Kotlin.

---

## 📌 Table of Contents
1. [Overview & Visual Features](#1-overview--visual-features)
2. [Canvas Drawing Architecture](#2-canvas-drawing-architecture)
3. [3D Coin Flipping & Projection Math](#3-3d-coin-flipping--projection-math)
4. [Parabolic Trajectory & Slot Clipping Logic](#4-parabolic-trajectory--slot-clipping-logic)
5. [Piggy Bank Spring Dynamics (Squash & Stretch)](#5-piggy-bank-spring-dynamics-squash--stretch)
6. [Sparkle Particles & Savings Counter](#6-sparkle-particles--savings-counter)
7. [App Navigation Integration (Nav3)](#7-app-navigation-integration-nav3)

---

## 1. Overview & Visual Features

To make a piggy bank animation look **authentic and engaging**, the implementation combines procedural 2D Canvas geometry with pseudo-3D physics:

* 🐷 **Shaded 3D Piggy Bank**: Procedural organic pig body with multi-stop radial highlights, cute ears, curly tail, 3D snout, and dark coin slot.
* 🪙 **Dual-Axis 3D Coin Spin**: Realistic cylindrical coin projection with scale matrices ($Scale_X = |\cos\theta_y|$, $Scale_Y = |\cos\theta_x|$), extruded metallic edge depth, inner medallion, and specular light streak.
* 🪂 **Parabolic Drop Arc**: Projectile path following gravity equations:
  $$Y(t) = Y_{start} + (Y_{slot} - Y_{start}) \cdot t - h_{arc} \cdot \sin(\pi t)$$
* 🕳️ **Slot Entry Clipping**: As the coin approaches the slot ($t \ge 0.82$), a dynamic `clipRect` mask hides the portion of the coin entering inside the piggy bank!
* 💥 **Squash & Stretch Impact Response**: Spring physics (`Animatable`) squashes the piggy bank down ($0.84\times$) and rebounds bouncy on coin entry.
* ✨ **Sparkle Particle Explosion**: Gold particles and $+1.00$ floating savings indicators burst upon entry.

---

## 2. Canvas Drawing Architecture

The Piggy Bank is drawn procedurally in `DrawScope.drawPiggyBank()` using layered paths and gradient brushes:

```kotlin
// 1. Body Radial Highlight (Gives a 3D ceramic look)
drawOval(
    brush = Brush.radialGradient(
        colors = listOf(theme.highlight, theme.primary, theme.darkShade),
        center = Offset(centerX - bodyWidth * 0.15f, centerY - bodyHeight * 0.25f),
        radius = bodyWidth * 0.7f
    ),
    topLeft = Offset(bodyLeft, bodyTop),
    size = Size(bodyWidth, bodyHeight)
)

// 2. Curly Spiral Tail
val tailPath = Path().apply {
    moveTo(startTail.x, startTail.y)
    cubicTo(
        startTail.x - 30f, startTail.y - 25f,
        startTail.x - 45f, startTail.y + 15f,
        startTail.x - 20f, startTail.y + 5f
    )
}
drawPath(tailPath, color = theme.darkShade, style = Stroke(width = 8f))
```

---

## 3. 3D Coin Flipping & Projection Math

To simulate a 3D coin flipping on a 2D Canvas without heavy 3D engine overhead:

### 1. Dual-Axis Scale Matrix
When a coin spins about the Y-axis by angle $\theta_y$ and X-axis by $\theta_x$:
* **Width Ratio**: $Scale_X = |\cos(\theta_y)|$
* **Height Ratio**: $Scale_Y = |\cos(\theta_x)|$

```kotlin
val scaleX = abs(cos(coin.spinY)).coerceAtLeast(0.06f)
val scaleY = abs(cos(coin.spinX)).coerceAtLeast(0.15f)

withTransform({
    translate(left = pos.x, top = pos.y)
    scale(scaleX = scaleX, scaleY = scaleY, pivot = Offset.Zero)
}) {
    // Render circular coin faces, inner medallion, and text
}
```

### 2. Extruded Cylinder Rim (3D Depth)
The thickness of the coin's metallic edge is proportional to $|\sin(\theta_y)|$:

$$\text{Extrusion Depth} = |\sin(\theta_y)| \times R_{coin} \times 0.35$$

```kotlin
val edgeDepth = abs(sin(coin.spinY)) * (radius * 0.35f)
if (edgeDepth > 1f) {
    val edgeOffset = if (isFrontFace) edgeDepth else -edgeDepth
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(type.darkEdge, Color.Black),
            center = Offset(edgeOffset * 0.5f, 0f),
            radius = radius * 1.2f
        ),
        radius = radius,
        center = Offset(edgeOffset, 0f)
    )
}
```

---

## 4. Parabolic Trajectory & Slot Clipping Logic

### 1. Parabolic Trajectory Calculation
```kotlin
fun currentPosition(): Offset {
    val currX = startPos.x + (slotPos.x - startPos.x) * progress
    val linearY = startPos.y + (slotPos.y - startPos.y) * progress
    val arcY = sin(progress * PI.toFloat()) * arcHeight
    return Offset(currX, linearY - arcY)
}
```

### 2. Slot Mask Clipping
When the coin reaches $t \ge 0.82$, we clip everything below the piggy slot line (`clipLimitY`):

```kotlin
if (isNearSlot) {
    val clipLimitY = slotY + 6f
    clipRect(
        left = pos.x - radius * 2f,
        top = pos.y - radius * 2f,
        right = pos.x + radius * 2f,
        bottom = clipLimitY
    ) {
        drawBlock() // Only top half of coin is rendered as it enters slot
    }
}
```

---

## 5. Piggy Bank Spring Dynamics (Squash & Stretch)

When a coin enters the slot, the piggy bank deforms vertically (`squashY`) and expands horizontally (`squashX`):

```kotlin
scope.launch {
    piggySquashY.snapTo(0.84f) // Instant compress
    piggySquashX.snapTo(1.14f)

    launch { 
        piggySquashY.animateTo(
            1.0f, 
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        ) 
    }
    launch { 
        piggySquashX.animateTo(
            1.0f, 
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        ) 
    }
}
```

---

## 6. Sparkle Particles & Savings Counter

When the coin finishes its drop:
1. `totalSavings += coin.coinType.value` updates total saved amount.
2. 16 gold sparkle particles burst outward with random velocities.
3. Haptic vibration feedback (`vibrator.vibrate(...)`) plays.
4. Floating text `+$1.00` rises and fades out.

---

## 7. App Navigation Integration (Nav3)

### Step 1: Declare `AnimScreen.PiggyBankCoin` in `AppNavigation.kt`
```kotlin
@Serializable
data object PiggyBankCoin : AnimScreen
```

### Step 2: Add Entry Route in `entryProvider`
```kotlin
entry<AnimScreen.PiggyBankCoin> {
    com.example.composelearning.animcompose.PiggyBankCoinScreen(
        onBack = { navigator.goBack() }
    )
}
```

### Step 3: Register in `FeatureCatalog.kt`
```kotlin
AnimationCategory(
    "Piggy Bank Coin Drop",
    "Realistic 3D coin flip physics and piggy bank drop using Canvas",
    AnimScreen.PiggyBankCoin,
    FeatureGroup.CANVAS_GRAPHICS
)
```

---

*Enjoy building delightful, interactive Canvas animations in Jetpack Compose! 🐷🪙*
