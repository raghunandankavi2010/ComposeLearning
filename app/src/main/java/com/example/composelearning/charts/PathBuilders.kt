package com.example.composelearning.charts

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import kotlin.math.min

/** How successive points in a line chart are connected. */
enum class LineSmoothing { Linear, Cubic, Step }

/**
 * Builds a poly-line path through [points] using [smoothing].
 *
 * For Cubic we use Catmull-Rom-to-Bezier with a tension of 0.5, which produces a natural curve
 * without overshooting endpoints, suitable for chart data where overshoot would visually lie.
 *
 * The path is mutated into the supplied [path] argument so callers can reuse one instance across
 * recompositions and avoid allocation pressure inside draw scopes.
 */
fun buildLinePath(
    points: List<Offset>,
    smoothing: LineSmoothing,
    path: Path = Path(),
): Path {
    path.reset()
    if (points.isEmpty()) return path
    val first = points.first()
    path.moveTo(first.x, first.y)
    if (points.size == 1) return path

    when (smoothing) {
        LineSmoothing.Linear -> {
            for (i in 1 until points.size) {
                val p = points[i]
                path.lineTo(p.x, p.y)
            }
        }
        LineSmoothing.Step -> {
            for (i in 1 until points.size) {
                val prev = points[i - 1]
                val curr = points[i]
                val midX = (prev.x + curr.x) / 2f
                path.lineTo(midX, prev.y)
                path.lineTo(midX, curr.y)
                path.lineTo(curr.x, curr.y)
            }
        }
        LineSmoothing.Cubic -> {
            for (i in 1 until points.size) {
                val p0 = points[(i - 2).coerceAtLeast(0)]
                val p1 = points[i - 1]
                val p2 = points[i]
                val p3 = points[(i + 1).coerceAtMost(points.lastIndex)]
                val tension = 0.5f
                val c1 = Offset(
                    x = p1.x + (p2.x - p0.x) / 6f * tension,
                    y = p1.y + (p2.y - p0.y) / 6f * tension,
                )
                val c2 = Offset(
                    x = p2.x - (p3.x - p1.x) / 6f * tension,
                    y = p2.y - (p3.y - p1.y) / 6f * tension,
                )
                path.cubicTo(c1.x, c1.y, c2.x, c2.y, p2.x, p2.y)
            }
        }
    }
    return path
}

/**
 * Closes [path] into a filled area between [points] and a baseline at [baselineY].
 *
 * Caller passes the already-stroked line as the seed path; this routine appends the closing
 * segments to convert it into a polygon suitable for a gradient fill underneath the line.
 */
fun closeAreaPath(path: Path, points: List<Offset>, baselineY: Float): Path {
    if (points.isEmpty()) return path
    path.lineTo(points.last().x, baselineY)
    path.lineTo(points.first().x, baselineY)
    path.close()
    return path
}

/** Linearly interpolate [value] from [src] range into [dst] range. */
fun lerpRange(value: Float, src: AxisRange, dst: ClosedFloatingPointRange<Float>): Float {
    if (src.span == 0f) return dst.start
    val t = (value - src.min) / src.span
    return dst.start + t * (dst.endInclusive - dst.start)
}

/** Find the point in [points] closest to [touch] in x-coordinate. */
fun nearestByX(points: List<Offset>, touch: Offset): Int? {
    if (points.isEmpty()) return null
    var best = 0
    var bestDx = Float.MAX_VALUE
    for (i in points.indices) {
        val dx = min(Float.MAX_VALUE, kotlin.math.abs(points[i].x - touch.x))
        if (dx < bestDx) {
            bestDx = dx
            best = i
        }
    }
    return best
}