package com.example.composelearning.graphics

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

data class Point3D(val x: Float, val y: Float, val z: Float)

data class DicePolygon(
    val vertexIndices: List<Int>,
    val value: String? = null,
    val isMainFace: Boolean = false,
    val aoFactor: Float = 1f
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreeDDiceRoller(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()

    val angleX = remember { Animatable(0.5f) }
    val angleY = remember { Animatable(0.5f) }
    val scale = remember { Animatable(1f) }
    val shakeOffset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }

    val diceSize = 160f
    val B = diceSize * 0.82f // Chamfer boundary
    val pipOffset = diceSize * 0.45f
    val cameraDistance = 1600f

    // Natural bone-white dice material: a bright base, a warm mid shadow,
    // and a deep recess tone used for chamfered edges / ambient occlusion.
    val diceHighlight = Color(0xFFFFFFFF)
    val diceBase = Color(0xFFF3ECDC)   // warm ivory (lit)
    val diceShadow = Color(0xFFBDB39C) // soft warm shadow (unlit)
    val diceAmbient = Color(0xFF6C6250) // deep recess tone

    // 24 vertices: 3 per corner
    val baseVertices = remember {
        val list = mutableListOf<Point3D>()
        for (z in listOf(-1f, 1f)) {
            for (y in listOf(-1f, 1f)) {
                for (x in listOf(-1f, 1f)) {
                    // Each corner (xS, yS, zS) gets 3 vertices slightly offset
                    list.add(Point3D(x * B, y * diceSize, z * diceSize)) // v_x
                    list.add(Point3D(x * diceSize, y * B, z * diceSize)) // v_y
                    list.add(Point3D(x * diceSize, y * diceSize, z * B)) // v_z
                }
            }
        }
        list
    }

    val allPolygons = remember { generateChamferedPolygons() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tactile 3D Roller") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 48.dp, start = 32.dp, end = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            launch { scale.animateTo(0.75f, tween(150)) }

                            val targetX = angleX.value + (Random.nextFloat() * 18f + 12f)
                            val targetY = angleY.value + (Random.nextFloat() * 18f + 12f)

                            launch {
                                angleX.animateTo(targetX, tween(1300, easing = FastOutSlowInEasing))
                            }
                            launch {
                                angleY.animateTo(targetY, tween(1300, easing = FastOutSlowInEasing))
                            }

                            delay(1200)
                            // Impact with Camera Shake
                            launch {
                                repeat(4) {
                                    val intensity = 15f / (it + 1)
                                    shakeOffset.animateTo(Offset(Random.nextFloat() * intensity * 2 - intensity, Random.nextFloat() * intensity * 2 - intensity), tween(40))
                                }
                                shakeOffset.animateTo(Offset.Zero, tween(100))
                            }
                            scale.animateTo(1.25f, spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMediumLow))
                            scale.animateTo(1f, spring(stiffness = Spring.StiffnessLow))
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 12.dp)
                ) {
                    Text("ROLL DICE", style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.radialGradient(
                        0.0f to Color(0xFF2B2E36),
                        0.55f to Color(0xFF15171C),
                        1.0f to Color(0xFF090A0D)
                    )
                )
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            scope.launch {
                                angleY.snapTo(angleY.value + dragAmount.x * 0.006f)
                                angleX.snapTo(angleX.value - dragAmount.y * 0.006f)
                            }
                        }
                    }
            ) {
                val center = Offset(size.width / 2, size.height / 2) + shakeOffset.value
                // Single dominant key light from the upper-left toward the viewer,
                // plus a soft fill from the opposite side so shadows never go pure black.
                val light1 = normalize(Point3D(-0.6f, -1f, 1.3f)) // key light
                val light2 = normalize(Point3D(0.7f, 0.5f, 0.4f)) // soft fill

                // --- FLOOR REFLECTION ---
                drawReflection(baseVertices, allPolygons, angleX.value, angleY.value, scale.value, cameraDistance, center, diceSize)

                // --- DYNAMIC GROUND SHADOW ---
                val altitude = (1f - scale.value).coerceAtLeast(0f) * 300f
                val shadowAlpha = (0.35f - altitude / 400f).coerceIn(0f, 0.35f)
                val shadowBlur = (diceSize * (1.2f + altitude / 80f))
                drawOval(
                    color = Color.Black.copy(alpha = shadowAlpha),
                    topLeft = center.copy(x = center.x - shadowBlur, y = center.y + diceSize * 1.6f),
                    size = androidx.compose.ui.geometry.Size(shadowBlur * 2, diceSize * 0.5f)
                )

                // --- DICE RENDERING ---
                val rotatedVertices = baseVertices.map { v ->
                    rotate3D(Point3D(v.x * scale.value, v.y * scale.value, v.z * scale.value), angleX.value, angleY.value)
                }
                val projectedPoints = rotatedVertices.map { rv ->
                    val s = cameraDistance / (cameraDistance + rv.z)
                    Offset(center.x + rv.x * s, center.y + rv.y * s)
                }

                val sortedPolygons = allPolygons.sortedByDescending { poly ->
                    poly.vertexIndices.map { rotatedVertices[it].z }.average()
                }

                sortedPolygons.forEach { poly ->
                    val v0 = rotatedVertices[poly.vertexIndices[0]]
                    val v1 = rotatedVertices[poly.vertexIndices[1]]
                    val v2 = rotatedVertices[poly.vertexIndices[2]]
                    val normal = calculateNormal(v0, v1, v2)

                    if (normal.z < 0) {
                        val ndl = max(0f, normal.x * light1.x + normal.y * light1.y + normal.z * light1.z)
                        val fill = max(0f, normal.x * light2.x + normal.y * light2.y + normal.z * light2.z)
                        val spec = calculateSpecular(normal, light1, Point3D(0f, 0f, -1f), 40f)

                        // Diffuse: shadow -> base driven by the key light, with a gentle fill lift.
                        val litColor = lerp(diceShadow, diceBase, (ndl * 0.85f + fill * 0.25f).coerceIn(0f, 1f))
                        // Ambient occlusion pulls chamfers and edge strips toward the recess tone.
                        val finalFaceColor = lerp(diceAmbient, litColor, poly.aoFactor)

                        val path = Path().apply {
                            val start = projectedPoints[poly.vertexIndices[0]]
                            moveTo(start.x, start.y)
                            poly.vertexIndices.forEach { lineTo(projectedPoints[it].x, projectedPoints[it].y) }
                            close()
                        }

                        drawPath(path, finalFaceColor)
                        // A single crisp, neutral highlight keeps the surface reading as matte ivory.
                        if (spec > 0.04f) drawPath(path, diceHighlight.copy(alpha = (spec * 0.55f).coerceAtMost(0.55f)))

                        if (poly.isMainFace && poly.value != null) {
                            renderPips(poly.value, normal, angleX.value, angleY.value, scale.value, diceSize, pipOffset, cameraDistance, center)
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawReflection(
    baseVertices: List<Point3D>,
    polygons: List<DicePolygon>,
    ax: Float, ay: Float, s: Float,
    camDist: Float, center: Offset, diceSize: Float
) {
    val reflectionYOffset = diceSize * 3.2f
    val rotated = baseVertices.map { v ->
        val rp = rotate3D(Point3D(v.x * s, v.y * s, v.z * s), ax, ay)
        Point3D(rp.x, -rp.y + reflectionYOffset, rp.z) // Inverted Y
    }

    val projected = rotated.map { rv ->
        val sc = camDist / (camDist + rv.z)
        Offset(center.x + rv.x * sc, center.y + rv.y * sc)
    }

    polygons.sortedByDescending { it.vertexIndices.map { idx -> rotated[idx].z }.average() }.forEach { poly ->
        val v0 = rotated[poly.vertexIndices[0]]
        val v1 = rotated[poly.vertexIndices[1]]
        val v2 = rotated[poly.vertexIndices[2]]
        val normal = calculateNormal(v0, v1, v2)

        if (normal.z < 0) {
            val path = Path().apply {
                moveTo(projected[poly.vertexIndices[0]].x, projected[poly.vertexIndices[0]].y)
                poly.vertexIndices.forEach { lineTo(projected[it].x, projected[it].y) }
                close()
            }
            drawPath(path, Color.White.copy(alpha = 0.08f))
        }
    }
}

private fun DrawScope.renderPips(
    value: String, normal: Point3D, ax: Float, ay: Float, s: Float,
    diceSize: Float, pipOffset: Float, camDist: Float, center: Offset
) {
    val localPips = when (value) {
        "1" -> listOf(Point3D(0f, 0f, diceSize))
        "2" -> listOf(Point3D(-pipOffset, -pipOffset, diceSize), Point3D(pipOffset, pipOffset, diceSize))
        "3" -> listOf(Point3D(-pipOffset, -pipOffset, diceSize), Point3D(0f, 0f, diceSize), Point3D(pipOffset, pipOffset, diceSize))
        "4" -> listOf(Point3D(-pipOffset, -pipOffset, diceSize), Point3D(pipOffset, -pipOffset, diceSize), Point3D(-pipOffset, pipOffset, diceSize), Point3D(pipOffset, pipOffset, diceSize))
        "5" -> listOf(Point3D(-pipOffset, -pipOffset, diceSize), Point3D(pipOffset, -pipOffset, diceSize), Point3D(0f, 0f, diceSize), Point3D(-pipOffset, pipOffset, diceSize), Point3D(pipOffset, pipOffset, diceSize))
        "6" -> listOf(Point3D(-pipOffset, -pipOffset, diceSize), Point3D(pipOffset, -pipOffset, diceSize), Point3D(-pipOffset, 0f, diceSize), Point3D(pipOffset, 0f, diceSize), Point3D(-pipOffset, pipOffset, diceSize), Point3D(pipOffset, pipOffset, diceSize))
        else -> emptyList()
    }

    localPips.forEach { p ->
        val oriented = when {
            normal.z > 0.8f -> p
            normal.z < -0.8f -> Point3D(p.x, p.y, -diceSize)
            normal.x > 0.8f -> Point3D(diceSize, p.y, -p.x)
            normal.x < -0.8f -> Point3D(-diceSize, p.y, p.x)
            normal.y > 0.8f -> Point3D(p.x, diceSize, -p.y)
            normal.y < -0.8f -> Point3D(p.x, -diceSize, p.y)
            else -> p
        }
        val rp = rotate3D(Point3D(oriented.x * s, oriented.y * s, oriented.z * s), ax, ay)
        val sc = camDist / (camDist + rp.z)
        val pc = Offset(center.x + rp.x * sc, center.y + rp.y * sc)
        val r = 15f * sc

        // Recessed, drilled-out pip: a solid dark bowl that is darkest toward the
        // top-inner edge (in shadow) and slightly lifted at the bottom lip that
        // catches the light — this sells the concave "engraved" look.
        drawCircle(
            brush = Brush.radialGradient(
                0.0f to Color(0xFF2A2622),
                0.7f to Color(0xFF120F0C),
                1.0f to Color(0xFF060504),
                center = pc.copy(y = pc.y + r * 0.28f), // shade origin low -> dark top
                radius = r * 1.15f
            ),
            radius = r, center = pc
        )
        // Soft top-inner shadow to deepen the cavity.
        drawCircle(
            color = Color.Black.copy(alpha = 0.35f),
            radius = r * 0.72f,
            center = pc.copy(y = pc.y - r * 0.22f)
        )
        // Thin lower rim highlight where the surface bevels into the hole.
        drawCircle(
            color = Color.White.copy(alpha = 0.18f),
            radius = r,
            center = pc.copy(y = pc.y + 0.8f * sc),
            style = Stroke(width = 1.2f * sc)
        )
    }
}

private fun rotate3D(p: Point3D, ax: Float, ay: Float): Point3D {
    val cx = cos(ax); val sx = sin(ax)
    val y1 = p.y * cx - p.z * sx; val z1 = p.y * sx + p.z * cx
    val cy = cos(ay); val sy = sin(ay)
    val x2 = p.x * cy + z1 * sy; val z2 = -p.x * sy + z1 * cy
    return Point3D(x2, y1, z2)
}

private fun calculateNormal(v0: Point3D, v1: Point3D, v2: Point3D): Point3D {
    val ax = v1.x - v0.x; val ay = v1.y - v0.y; val az = v1.z - v0.z
    val bx = v2.x - v0.x; val by = v2.y - v0.y; val bz = v2.z - v0.z
    val nx = ay * bz - az * by; val ny = az * bx - ax * bz; val nz = ax * by - ay * bx
    val l = sqrt(nx * nx + ny * ny + nz * nz).coerceAtLeast(0.0001f)
    return Point3D(nx / l, ny / l, nz / l)
}

private fun calculateSpecular(n: Point3D, l: Point3D, v: Point3D, sh: Float): Float {
    val dot = n.x * l.x + n.y * l.y + n.z * l.z
    val rx = l.x - 2 * dot * n.x; val ry = l.y - 2 * dot * n.y; val rz = l.z - 2 * dot * n.z
    val sDot = max(0f, -(rx * v.x + ry * v.y + rz * v.z))
    return sDot.pow(sh)
}

private fun normalize(p: Point3D): Point3D {
    val l = sqrt(p.x * p.x + p.y * p.y + p.z * p.z)
    return Point3D(p.x / l, p.y / l, p.z / l)
}

private fun lerp(s: Color, e: Color, f: Float): Color {
    val t = f.coerceIn(0f, 1f)
    return Color(s.red + t * (e.red - s.red), s.green + t * (e.green - s.green), s.blue + t * (e.blue - s.blue), s.alpha + t * (e.alpha - s.alpha))
}

private suspend fun delay(ms: Long) = kotlinx.coroutines.delay(ms.milliseconds)

private fun generateChamferedPolygons(): List<DicePolygon> {
    val faces = mutableListOf<DicePolygon>()

    // --- 6 Octagonal Main Faces ---
    // Front (z=S)
    faces.add(DicePolygon(listOf(12, 15, 16, 22, 21, 18, 19, 13), "1", true))
    // Back (z=-S)
    faces.add(DicePolygon(listOf(3, 0, 1, 7, 6, 9, 10, 4), "6", true))
    // Top (y=S)
    faces.add(DicePolygon(listOf(19, 22, 23, 11, 8, 20), "3", true)) // Needs fix
    // Actually let's redefine carefully.
    return generateCorrectPolygons()
}

private fun generateCorrectPolygons(): List<DicePolygon> {
    val faces = mutableListOf<DicePolygon>()

    // 1. Octagons (Corrected per plane analysis)
    faces.add(DicePolygon(listOf(12, 15, 16, 22, 21, 18, 19, 13), "1", true)) // Front (z=S)
    faces.add(DicePolygon(listOf(3, 0, 1, 7, 6, 9, 10, 4), "6", true))       // Back (z=-S)
    faces.add(DicePolygon(listOf(18, 21, 23, 11, 9, 6, 8, 20), "3", true))  // Top (y=S)

    // Bottom (y=-S):
    // 12:(-B,-S,S), 15:(B,-S,S), 17:(S,-S,B), 5:(S,-S,-B), 3:(B,-S,-S), 0:(-B,-S,-S), 2:(-S,-S,-B), 14:(-S,-S,B)
    faces.add(DicePolygon(listOf(12, 15, 17, 5, 3, 0, 2, 14), "4", true))

    // Left (x=-S):
    // 13:(-S,-B,S), 1:(-S,-B,-S), 2:(-S,-S,-B), 8:(-S,S,-B), 7:(-S,B,-S), 19:(-S,B,S), 20:(-S,S,B), 14:(-S,-S,B)
    faces.add(DicePolygon(listOf(13, 1, 2, 8, 7, 19, 20, 14), "2", true))

    // Right (x=S):
    // 16:(S,-B,S), 4:(S,-B,-S), 5:(S,-S,-B), 11:(S,S,-B), 10:(S,B,-S), 22:(S,B,S), 23:(S,S,B), 17:(S,-S,B)
    faces.add(DicePolygon(listOf(16, 4, 5, 11, 10, 22, 23, 17), "5", true))

    // 2. Corner Triangles (3 vertices per corner)
    for (i in 0..7) faces.add(DicePolygon(listOf(3 * i, 3 * i + 1, 3 * i + 2), aoFactor = 0.5f))

    // 3. Edge Rectangles
    // Front edges
    faces.add(DicePolygon(listOf(18, 21, 22, 19), aoFactor = 0.8f)) // Front-Top
    faces.add(DicePolygon(listOf(15, 12, 13, 16), aoFactor = 0.8f)) // Front-Bottom
    faces.add(DicePolygon(listOf(12, 18, 20, 14), aoFactor = 0.8f)) // Front-Left
    faces.add(DicePolygon(listOf(21, 15, 17, 23), aoFactor = 0.8f)) // Front-Right

    // Back edges
    faces.add(DicePolygon(listOf(9, 6, 7, 10), aoFactor = 0.8f))   // Back-Top
    faces.add(DicePolygon(listOf(0, 3, 4, 1), aoFactor = 0.8f))     // Back-Bottom
    faces.add(DicePolygon(listOf(6, 0, 2, 8), aoFactor = 0.8f))     // Back-Left
    faces.add(DicePolygon(listOf(3, 9, 11, 5), aoFactor = 0.8f))    // Back-Right

    // Side edges
    faces.add(DicePolygon(listOf(19, 7, 8, 20), aoFactor = 0.8f))  // Top-Left
    faces.add(DicePolygon(listOf(10, 22, 23, 11), aoFactor = 0.8f)) // Top-Right
    faces.add(DicePolygon(listOf(1, 13, 14, 2), aoFactor = 0.8f))   // Bottom-Left
    faces.add(DicePolygon(listOf(4, 16, 17, 5), aoFactor = 0.8f))   // Bottom-Right

    return faces
}
