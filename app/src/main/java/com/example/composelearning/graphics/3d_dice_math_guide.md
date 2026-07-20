# 3D Graphics Mathematics in Jetpack Compose Canvas
## A Comprehensive Guide to Building a 3D Dice Roller

When building a 3D object like a dice on a natively 2D platform like Jetpack Compose UI, you essentially act as your own minimal 3D engine. This document breaks down the foundational geometry, matrix transformations, projections, and culling algorithms required to turn 3D coordinates into interactive 2D vectors on a canvas.

---

## 1. Defining the 3D Vector Space

A 3D object is defined by a set of points called **vertices** in a 3-axis coordinate system $(X, Y, Z)$:
* **X-axis**: Horizontal position (Left / Right)
* **Y-axis**: Vertical position (Up / Down)
* **Z-axis**: Depth position (Forward / Backward, relative to the camera viewpoint)

For a symmetrical cube centered perfectly at the origin $(0, 0, 0)$ with a side length of $2L$, the 8 core vertices are mapped explicitly using permutations of $(\pm L, \pm L, \pm L)$:

| Vertex ID | X Coordinate | Y Coordinate | Z Coordinate |
|:---:|:---:|:---:|:---:|
| **0** | $-L$ | $-L$ | $-L$ |
| **1** | $+L$ | $-L$ | $-L$ |
| **2** | $+L$ | $+L$ | $-L$ |
| **3** | $-L$ | $+L$ | $-L$ |
| **4** | $-L$ | $-L$ | $+L$ |
| **5** | $+L$ | $-L$ | $+L$ |
| **6** | $+L$ | $+L$ | $+L$ |
| **7** | $-L$ | $+L$ | $+L$ |

Faces are defined by connecting these vertices chronologically. For instance, the **Front Face** is composed of indices `[0, 1, 2, 3]`.

---

## 2. 3D Rotation Matrices

To spin the dice dynamically, vertices must rotate around the axes. In linear algebra, rotation is achieved by multiplying the coordinate vector by a rotation matrix.

### Rotation Around the X-Axis (Pitch)
Rotating around the X-axis leaves the $X$ value unchanged, while transforming $Y$ and $Z$ using the rotation angle $lpha$:
$$egin{aligned}
x' &= x \
y' &= y \cdot \cos(lpha) - z \cdot \sin(lpha) \
z' &= y \cdot \sin(lpha) + z \cdot \cos(lpha)
\end{aligned}$$

### Rotation Around the Y-Axis (Yaw)
Rotating around the Y-axis leaves the $Y$ value unchanged, while transforming $X$ and $Z$ using the rotation angle $eta$:
$$egin{aligned}
x' &= x \cdot \cos(eta) + z \cdot \sin(eta) \
y' &= y \
z' &= -x \cdot \sin(eta) + z \cdot \cos(eta)
\end{aligned}$$

### Kotlin Implementation Example
When applying sequential rotations, you chain the output coordinates through the trigonometric transforms:

```kotlin
fun rotateVertex(vertex: Point3D, angleX: Float, angleY: Float): Point3D {
    // 1. X-Axis Rotation
    val cosX = cos(angleX)
    val sinX = sin(angleX)
    val y1 = vertex.y * cosX - vertex.z * sinX
    val z1 = vertex.y * sinX + vertex.z * cosX

    // 2. Y-Axis Rotation (using updated y1 and z1)
    val cosY = cos(angleY)
    val sinY = sin(angleY)
    val x2 = vertex.x * cosY + z1 * sinY
    val z2 = -vertex.x * sinY + z1 * cosY

    return Point3D(x2, y1, z2)
}
```

---

## 3. Perspective Projection (3D to 2D)

Once the 3D coordinate $(X_r, Y_r, Z_r)$ has been rotated, it must be mapped to a flat 2D coordinate $(X_s, Y_s)$ on the screen. 

To achieve a true **Perspective Projection** (where parts of the cube that are farther away appear physically smaller), we divide the coordinate by its depth $Z$ combined with a virtual **Camera Distance** factor ($D$).

### The Projection Formula

$$x_{screen} = x_{center} + \left( rac{x_r \cdot D}{z_r + D} ight)$$
$$y_{screen} = y_{center} + \left( rac{y_r \cdot D}{z_r + D} ight)$$

* **$D$ (Camera Distance)**: Controls the field of view focal depth. A smaller value creates an ultra-wide angle fish-eye effect, whereas a large value diminishes perspective distortion toward isometric representation.
* **$(x_{center}, y_{center})$**: Shifts the origin point from the top-left $(0,0)$ of the screen layout to the true physical midpoint of your custom Canvas container.

---

## 4. Depth Buffering (Painter's Algorithm)

A major challenge when rendering a full 3D shape is avoiding transparency clipping issues (ensuring background faces do not render directly over foreground layers). To solve this efficiently in a lightweight Canvas environment, use the **Painter’s Algorithm**.

1. Calculate the average $Z$ value for each individual polygon face:
   $$Z_{average} = rac{z_1 + z_2 + z_3 + z_4}{4}$$
2. Sort the list of geometric faces by their $Z_{average}$ values in **descending order**.
3. Render the paths sequentially. Faces furthest from the camera (most negative or deep $Z$ value) are painted first, allowing foreground faces to naturally overlay over them cleanly.

---

## 5. Locating and Orienting Dice Pips (Dots)

To make it look like a real dice, you cannot just write flat text on top of the screen; the dots (pips) must transform synchronously with the faces.

### The Math of Face Planes
Every pip can be treated as an offset vector localized to a 2D coordinate system matching the face itself. 
1. Determine the exact 3D position of the center of a face. For the front face, it is $(0, 0, -L)$.
2. Define the pips relative to this position. For example, the center pip of a value **5** dice resides at localized coordinates $(0, 0)$, while corner pips reside at positions like $(\pm 0.5L, \pm 0.5L)$.
3. Treat each pip as its own micro `Point3D` point, apply the identical **Rotation Matrix**, pass it through the **Perspective Projection Formula**, and paint the result using `drawCircle()`. This guarantees the dots scale down, flatten, and spin seamlessly along with the cube geometry.
