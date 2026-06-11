package com.example.composelearning.gradients

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.VertexMode
import androidx.compose.ui.graphics.Vertices
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

/* -------------------------------------------------------------------------- */
/*  Mesh gradient modifier                                                    */
/*  Adapted from                                                              */
/*  https://gist.github.com/sinasamaki/05725557c945c5329fdba4a3494aaecb       */
/* -------------------------------------------------------------------------- */

/**
 * Draws a mesh gradient behind the content.
 *
 * A mesh gradient is defined by a grid of control points, where every point
 * carries a position (inside the 0..1 unit square) and a [Color]. The grid is
 * triangulated — each cell is split into two triangles, so a square of four
 * points becomes two triangles — and the GPU interpolates colors across each
 * triangle's three vertices. With higher [resolutionX]/[resolutionY] the
 * control points are joined with cubic bezier curves and extra vertices are
 * sampled along them, producing the smooth, fluid look instead of hard facets.
 *
 * @param points rows of (position, color) control points. Each inner list is a
 *  row; all rows must have the same length.
 * @param resolutionX interpolated steps between control points horizontally.
 * @param resolutionY interpolated steps between control points vertically.
 * @param showPoints draws the resulting vertex grid as white dots (debugging).
 * @param indicesModifier optionally rewrite the triangle index list.
 */
@Composable
fun Modifier.meshGradient(
    points: List<List<Pair<Offset, Color>>>,
    resolutionX: Int = 1,
    resolutionY: Int = 1,
    showPoints: Boolean = false,
    indicesModifier: (List<Int>) -> List<Int> = { it }
): Modifier {
    val pointData by remember(points, resolutionX, resolutionY) {
        derivedStateOf {
            PointData(points, resolutionX, resolutionY)
        }
    }

    return drawBehind {
        drawIntoCanvas { canvas ->
            scale(
                scaleX = size.width,
                scaleY = size.height,
                pivot = Offset.Zero
            ) {
                canvas.drawVertices(
                    vertices = Vertices(
                        vertexMode = VertexMode.Triangles,
                        positions = pointData.offsets,
                        textureCoordinates = pointData.offsets,
                        colors = pointData.colors,
                        indices = indicesModifier(pointData.indices)
                    ),
                    blendMode = BlendMode.Dst,
                    paint = meshPaint
                )
            }
            if (showPoints) {
                val flattenedPaint = Paint()
                flattenedPaint.color = Color.White.copy(alpha = .9f)
                flattenedPaint.strokeWidth = 4f * .001f
                flattenedPaint.strokeCap = StrokeCap.Round
                flattenedPaint.blendMode = BlendMode.SrcOver

                scale(
                    scaleX = size.width,
                    scaleY = size.height,
                    pivot = Offset.Zero
                ) {
                    canvas.drawPoints(
                        pointMode = PointMode.Points,
                        points = pointData.offsets,
                        paint = flattenedPaint
                    )
                }
            }
        }
    }
}

private val meshPaint = Paint()

private class PointData(
    private val points: List<List<Pair<Offset, Color>>>,
    private val stepsX: Int,
    private val stepsY: Int
) {
    val offsets: MutableList<Offset>
    val colors: MutableList<Color>
    val indices: List<Int>

    private val xLength: Int = (points[0].size * stepsX) - (stepsX - 1)
    private val yLength: Int = (points.size * stepsY) - (stepsY - 1)
    private val measure = PathMeasure()

    init {
        offsets = MutableList(xLength * yLength) { Offset(0f, 0f) }
        colors = MutableList(xLength * yLength) { Color.Transparent }

        // Build the triangle index list: two triangles per grid cell.
        // For a cell with corners a(top-left) b(top-right) c(bottom-left)
        // d(bottom-right) we emit triangles (a, c, d) and (a, b, d).
        indices = buildList {
            for (y in 0..yLength - 2) {
                for (x in 0..xLength - 2) {
                    val a = (y * xLength) + x
                    val b = a + 1
                    val c = ((y + 1) * xLength) + x
                    val d = c + 1
                    add(a)
                    add(c)
                    add(d)
                    add(a)
                    add(b)
                    add(d)
                }
            }
        }

        generateInterpolatedOffsets()
    }

    private fun generateInterpolatedOffsets() {
        // Place the control points and interpolate along each row (x axis).
        for (y in 0..points.lastIndex) {
            for (x in 0..points[y].lastIndex) {
                setOffset(x * stepsX, y * stepsY, points[y][x].first)
                setColor(x * stepsX, y * stepsY, points[y][x].second)

                if (x != points[y].lastIndex) {
                    val path = cubicPathX(
                        point1 = points[y][x].first,
                        point2 = points[y][x + 1].first,
                        position = when (x) {
                            0 -> 0
                            points[y].lastIndex - 1 -> 2
                            else -> 1
                        }
                    )
                    measure.setPath(path, false)

                    for (i in 1..<stepsX) {
                        val pos = measure.getPosition(i / stepsX.toFloat() * measure.length)
                        setOffset((x * stepsX) + i, y * stepsY, Offset(pos.x, pos.y))
                        setColor(
                            (x * stepsX) + i,
                            y * stepsY,
                            lerp(
                                points[y][x].second,
                                points[y][x + 1].second,
                                i / stepsX.toFloat()
                            )
                        )
                    }
                }
            }
        }

        // Interpolate down each column (y axis) between the rows placed above.
        for (y in 0..<points.lastIndex) {
            for (x in 0..<xLength) {
                val path = cubicPathY(
                    point1 = getOffset(x, y * stepsY),
                    point2 = getOffset(x, (y + 1) * stepsY),
                    position = when (y) {
                        0 -> 0
                        points.lastIndex - 1 -> 2
                        else -> 1
                    }
                )
                measure.setPath(path, false)

                for (i in 1..<stepsY) {
                    val pos = measure.getPosition(i / stepsY.toFloat() * measure.length)
                    setOffset(x, (y * stepsY) + i, Offset(pos.x, pos.y))
                    setColor(
                        x,
                        (y * stepsY) + i,
                        lerp(
                            getColor(x, y * stepsY),
                            getColor(x, (y + 1) * stepsY),
                            i / stepsY.toFloat()
                        )
                    )
                }
            }
        }
    }

    private fun getOffset(x: Int, y: Int): Offset = offsets[(y * xLength) + x]
    private fun getColor(x: Int, y: Int): Color = colors[(y * xLength) + x]
    private fun setOffset(x: Int, y: Int, offset: Offset) {
        offsets[(y * xLength) + x] = Offset(offset.x, offset.y)
    }
    private fun setColor(x: Int, y: Int, color: Color) {
        colors[(y * xLength) + x] = color
    }
}

private fun cubicPathX(point1: Offset, point2: Offset, position: Int): Path = Path().apply {
    moveTo(point1.x, point1.y)
    val delta = (point2.x - point1.x) * .5f
    when (position) {
        0 -> cubicTo(point1.x, point1.y, point2.x - delta, point2.y, point2.x, point2.y)
        2 -> cubicTo(point1.x + delta, point1.y, point2.x, point2.y, point2.x, point2.y)
        else -> cubicTo(point1.x + delta, point1.y, point2.x - delta, point2.y, point2.x, point2.y)
    }
    lineTo(point2.x, point2.y)
}

private fun cubicPathY(point1: Offset, point2: Offset, position: Int): Path = Path().apply {
    moveTo(point1.x, point1.y)
    val delta = (point2.y - point1.y) * .5f
    when (position) {
        0 -> cubicTo(point1.x, point1.y, point2.x, point2.y - delta, point2.x, point2.y)
        2 -> cubicTo(point1.x, point1.y + delta, point2.x, point2.y, point2.x, point2.y)
        else -> cubicTo(point1.x, point1.y + delta, point2.x, point2.y - delta, point2.x, point2.y)
    }
    lineTo(point2.x, point2.y)
}

/* -------------------------------------------------------------------------- */
/*  Interactive demo — drag the control points                                */
/* -------------------------------------------------------------------------- */

private data class MeshPoint(val offset: Offset, val color: Color)

private val palette = listOf(
    Color(0xFF6A00F4), Color(0xFF8900F2), Color(0xFFA100F2),
    Color(0xFFB100E8), Color(0xFFBC00DD), Color(0xFFD100D1),
    Color(0xFFDB00B6), Color(0xFFE500A4), Color(0xFFF20089),
    Color(0xFFFF4D6D), Color(0xFFFF7900), Color(0xFFFFD000)
)

/**
 * Builds a [rows] x [cols] grid evenly spread across the unit square. The
 * corners sit exactly on the edges so the gradient always fills the box.
 */
private fun initialGrid(rows: Int, cols: Int): List<List<MeshPoint>> = List(rows) { y ->
    List(cols) { x ->
        MeshPoint(
            offset = Offset(
                x = x / (cols - 1).toFloat(),
                y = y / (rows - 1).toFloat()
            ),
            color = palette[(y * cols + x) % palette.size]
        )
    }
}

@Composable
fun InteractiveMeshGradientScreen() {
    val rows = 3
    val cols = 3
    var grid by remember { mutableStateOf(initialGrid(rows, cols)) }
    var resolution by remember { mutableIntStateOf(16) }
    var showHandles by remember { mutableStateOf(true) }
    var showVertices by remember { mutableStateOf(false) }

    // The mesh modifier wants List<List<Pair<Offset, Color>>>.
    val meshPoints by remember(grid) {
        derivedStateOf { grid.map { row -> row.map { it.offset to it.color } } }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Drag the dots to reshape the mesh",
            style = MaterialTheme.typography.titleMedium
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.Black)
                .meshGradient(
                    points = meshPoints,
                    resolutionX = resolution,
                    resolutionY = resolution,
                    showPoints = showVertices
                )
        ) {
            val w = constraints.maxWidth.toFloat()
            val h = constraints.maxHeight.toFloat()
            val handleSize = 28.dp

            if (showHandles) {
                grid.forEachIndexed { y, row ->
                    row.forEachIndexed { x, point ->
                        Box(
                            modifier = Modifier
                                .offset {
                                    val half = (handleSize.toPx() / 2f).roundToInt()
                                    IntOffset(
                                        x = (point.offset.x * w).roundToInt() - half,
                                        y = (point.offset.y * h).roundToInt() - half
                                    )
                                }
                                .size(handleSize)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.35f))
                                .border(2.dp, Color.White, CircleShape)
                                .pointerInput(x, y, w, h) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        grid = grid.mapIndexed { yy, r ->
                                            r.mapIndexed { xx, p ->
                                                if (xx == x && yy == y) {
                                                    p.copy(
                                                        offset = Offset(
                                                            x = (p.offset.x + dragAmount.x / w)
                                                                .coerceIn(0f, 1f),
                                                            y = (p.offset.y + dragAmount.y / h)
                                                                .coerceIn(0f, 1f)
                                                        )
                                                    )
                                                } else {
                                                    p
                                                }
                                            }
                                        }
                                    }
                                }
                        )
                    }
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Smoothness")
            Slider(
                value = resolution.toFloat(),
                onValueChange = { resolution = it.roundToInt() },
                valueRange = 1f..30f,
                modifier = Modifier.weight(1f)
            )
            Text("$resolution")
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Handles")
                Switch(checked = showHandles, onCheckedChange = { showHandles = it })
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Vertices")
                Switch(checked = showVertices, onCheckedChange = { showVertices = it })
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { grid = initialGrid(rows, cols) }) { Text("Reset") }
            OutlinedButton(onClick = {
                grid = grid.map { row -> row.map { it.copy(color = palette.random()) } }
            }) { Text("Shuffle colors") }
        }
    }
}

/* -------------------------------------------------------------------------- */
/*  Animated sine-wave mesh gradient                                          */
/* -------------------------------------------------------------------------- */

/**
 * A grid whose control points are pushed around by two sine waves, giving a
 * flowing, liquid look. The grid is deliberately drawn slightly larger than
 * the unit square (from -[overshoot] to 1 + [overshoot]) so that as the edge
 * points wobble inward they never expose the background behind the mesh.
 */
@Composable
fun SineWaveMeshGradientScreen() {
    val rows = 5
    val cols = 5
    val overshoot = 0.25f
    val amplitude = 0.12f

    val waveColors = remember {
        listOf(
            Color(0xFF3A0CA3),
            Color(0xFF4361EE),
            Color(0xFF4CC9F0),
            Color(0xFF4895EF),
            Color(0xFF560BAD)
        )
    }

    val transition = rememberInfiniteTransition(label = "sine")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val meshPoints by remember(phase) {
        derivedStateOf {
            List(rows) { y ->
                List(cols) { x ->
                    // base position spread across [-overshoot, 1 + overshoot]
                    val baseX = -overshoot + (x / (cols - 1).toFloat()) * (1f + 2 * overshoot)
                    val baseY = -overshoot + (y / (rows - 1).toFloat()) * (1f + 2 * overshoot)

                    val dx = amplitude * sin(phase + baseY * 4f + x)
                    val dy = amplitude * sin(phase + baseX * 4f + y)

                    val color = lerp(
                        waveColors[y % waveColors.size],
                        waveColors[(y + 1) % waveColors.size],
                        x / (cols - 1).toFloat()
                    )
                    Offset(baseX + dx, baseY + dy) to color
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Sine wave mesh gradient",
            style = MaterialTheme.typography.titleMedium
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(24.dp))
                .meshGradient(
                    points = meshPoints,
                    resolutionX = 20,
                    resolutionY = 20
                )
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Two sine waves drive a 5×5 control grid that is drawn beyond the " +
                "edges so the wobbling corners never expose a gap.",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
