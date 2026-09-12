package com.example.composelearning.globe

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntSize
import com.example.composelearning.globe.data.Country
import com.example.composelearning.globe.data.WorldAtlas
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * The CPU half of the globe maths: everything in GLOBE.md §2–§4 and §12–§13.
 * The shader re-implements §2–§5 per pixel; this side handles labels and taps, and the two
 * agree because they use the identical formulas.
 */

/** A point on (or a direction from) the unit sphere in the world frame. */
data class Vec3(val x: Float, val y: Float, val z: Float)

private const val TwoPi = 6.2831855f

/** GLOBE.md §2: geodetic → unit vector, with +y through the pole and +z toward the camera. */
fun lonLatToVec(lonRad: Float, latRad: Float): Vec3 {
    val cosLat = cos(latRad)
    return Vec3(cosLat * sin(lonRad), sin(latRad), cosLat * cos(lonRad))
}

/** GLOBE.md §2: unit vector → (longitude, latitude) in radians. */
fun Vec3.toLonLat(): Pair<Float, Float> =
    atan2(x, z) to asin(y.coerceIn(-1f, 1f))

/** Wrap an angle into (−π, π]. */
fun wrapLongitude(lonRad: Float): Float {
    val m = (lonRad + PI.toFloat()).mod(TwoPi)
    return m - PI.toFloat()
}

/**
 * The world→view rotation `R = Rx(φc) · Ry(−λc)` of GLOBE.md §3, stored as its three rows.
 *
 * `row2` is `n(λc, φc)` — the view direction — so `row2 · n` is the cosine of a point's
 * angular distance from the centre of the disc. Visibility, label fade and area
 * foreshortening are all that one dot product.
 */
class GlobeRotation(val lonRad: Float, val latRad: Float) {
    private val cl = cos(lonRad)
    private val sl = sin(lonRad)
    private val cp = cos(latRad)
    private val sp = sin(latRad)

    val row0 = Vec3(cl, 0f, -sl)
    val row1 = Vec3(-sp * sl, cp, -sp * cl)
    val row2 = Vec3(cp * sl, sp, cp * cl)

    /** `R · n` — world to view. */
    fun toView(n: Vec3) = Vec3(
        row0.x * n.x + row0.y * n.y + row0.z * n.z,
        row1.x * n.x + row1.y * n.y + row1.z * n.z,
        row2.x * n.x + row2.y * n.y + row2.z * n.z
    )

    /** `Rᵗ · (u,v,w)` — view to world; R is orthonormal so the inverse is the transpose. */
    fun toWorld(u: Float, v: Float, w: Float) = Vec3(
        row0.x * u + row1.x * v + row2.x * w,
        row0.y * u + row1.y * v + row2.y * w,
        row0.z * u + row1.z * v + row2.z * w
    )

    /** Cosine of the angular distance from the disc centre; ≥ 0 means facing the camera. */
    fun depthOf(n: Vec3): Float = row2.x * n.x + row2.y * n.y + row2.z * n.z
}

/** Result of GLOBE.md §4: where a surface point lands, and whether it faces us. */
data class Projected(val position: Offset, val depth: Float) {
    val visible: Boolean get() = depth >= 0f
}

/** GLOBE.md §4: orthographic projection — drop z, flip screen y. */
fun project(n: Vec3, rotation: GlobeRotation, centre: Offset, k: Float): Projected {
    val v = rotation.toView(n)
    return Projected(Offset(centre.x + k * v.x, centre.y - k * v.y), v.z)
}

/**
 * GLOBE.md §5: inverse orthographic projection. Returns the world-frame surface point under
 * a pixel, or null when the pixel misses the sphere.
 */
fun unproject(point: Offset, centre: Offset, k: Float, rotation: GlobeRotation): Vec3? {
    val u = (point.x - centre.x) / k
    val v = (centre.y - point.y) / k
    val r2 = u * u + v * v
    if (r2 > 1f) return null
    return rotation.toWorld(u, v, sqrt(1f - r2))
}

/**
 * GLOBE.md §12: tap → country. The tap is inverse-projected and the id read straight out of
 * the raster; a miss searches outward in a spiral so that single-texel microstates
 * (Vatican City, Monaco, Nauru) are still reachable with a fingertip.
 */
fun hitTest(
    tap: Offset,
    centre: Offset,
    k: Float,
    rotation: GlobeRotation,
    atlas: WorldAtlas,
    searchRadiusPx: Int = 12
): Country? {
    idOf(tap, centre, k, rotation, atlas)?.let { return it }
    for (r in 1..searchRadiusPx) {
        val steps = 8 * r
        for (i in 0 until steps) {
            val a = TwoPi * i / steps
            val p = Offset(tap.x + r * cos(a), tap.y + r * sin(a))
            idOf(p, centre, k, rotation, atlas)?.let { return it }
        }
    }
    return null
}

private fun idOf(
    point: Offset,
    centre: Offset,
    k: Float,
    rotation: GlobeRotation,
    atlas: WorldAtlas
): Country? {
    val n = unproject(point, centre, k, rotation) ?: return null
    val (lon, lat) = n.toLonLat()
    return atlas.country(atlas.idAt(lon, lat))
}

/** A label that survived visibility, size and collision tests. */
data class GlobeLabel(
    val country: Country,
    val anchor: Offset,
    val topLeft: Offset,
    val size: IntSize,
    val alpha: Float,
    val selected: Boolean
)

/**
 * GLOBE.md §13: choose which country names to draw.
 *
 * Priority is the projected linear extent `ℓ = K·√(A·w)`, which falls straight out of the
 * orthographic area Jacobian (§4) — one expression that makes Russia beat Luxembourg *and*
 * makes a country near the limb lose to one facing the camera. Placement is greedy in that
 * order, which keeps the choice stable frame to frame instead of flickering as a global
 * optimiser would.
 */
fun layoutLabels(
    atlas: WorldAtlas,
    rotation: GlobeRotation,
    centre: Offset,
    k: Float,
    viewport: Size,
    measure: (String) -> IntSize,
    selectedId: Int = -1,
    maxLabels: Int = 24,
    minExtentPx: Float = 34f,
    minDepth: Float = 0.12f,
    fadeInDepth: Float = 0.35f,
    padding: Float = 3f
): List<GlobeLabel> {
    data class Candidate(val country: Country, val depth: Float, val extent: Float)

    val candidates = ArrayList<Candidate>(64)
    for (c in atlas.countries) {
        val n = lonLatToVec(c.lon.toRadians(), c.lat.toRadians())
        val depth = rotation.depthOf(n)
        if (depth <= minDepth) continue
        val extent = k * sqrt(c.areaSr * depth)
        if (extent < minExtentPx && c.id != selectedId) continue
        candidates += Candidate(c, depth, extent)
    }
    // Selected first so it always gets a name, then largest projected extent.
    candidates.sortWith(
        compareByDescending<Candidate> { it.country.id == selectedId }
            .thenByDescending { it.extent }
    )

    val placed = ArrayList<GlobeLabel>(maxLabels)
    val takenL = FloatArray(maxLabels)
    val takenT = FloatArray(maxLabels)
    val takenR = FloatArray(maxLabels)
    val takenB = FloatArray(maxLabels)
    var count = 0

    for (cand in candidates) {
        if (count == maxLabels) break
        val n = lonLatToVec(cand.country.lon.toRadians(), cand.country.lat.toRadians())
        val p = project(n, rotation, centre, k)
        val size = measure(cand.country.name)
        val left = p.position.x - size.width / 2f
        val top = p.position.y - size.height / 2f
        val right = left + size.width
        val bottom = top + size.height
        if (left < 0f || top < 0f || right > viewport.width || bottom > viewport.height) continue

        var overlaps = false
        for (i in 0 until count) {
            if (left - padding < takenR[i] && right + padding > takenL[i] &&
                top - padding < takenB[i] && bottom + padding > takenT[i]
            ) {
                overlaps = true
                break
            }
        }
        if (overlaps) continue

        takenL[count] = left
        takenT[count] = top
        takenR[count] = right
        takenB[count] = bottom
        count++
        placed += GlobeLabel(
            country = cand.country,
            anchor = p.position,
            topLeft = Offset(left, top),
            size = size,
            alpha = smoothstep(minDepth, fadeInDepth, cand.depth),
            selected = cand.country.id == selectedId
        )
    }
    return placed
}

/**
 * GLOBE.md §9: the subsolar point — where the sun is directly overhead — from UTC time,
 * via the NOAA declination and equation-of-time series. This is what makes the terminator
 * lean correctly for today's date instead of standing vertical.
 */
fun subsolarDirection(dayOfYear: Int, utcHours: Float): Vec3 {
    val gamma = TwoPi / 365f * (dayOfYear - 1 + (utcHours - 12f) / 24f)
    val declination = (0.006918
        - 0.399912 * cos(gamma) + 0.070257 * sin(gamma)
        - 0.006758 * cos(2f * gamma) + 0.000907 * sin(2f * gamma)
        - 0.002697 * cos(3f * gamma) + 0.001480 * sin(3f * gamma)).toFloat()
    val eqTimeMinutes = (229.18 * (0.000075
        + 0.001868 * cos(gamma) - 0.032077 * sin(gamma)
        - 0.014615 * cos(2f * gamma) - 0.040849 * sin(2f * gamma))).toFloat()
    val lonDeg = -15f * (utcHours + eqTimeMinutes / 60f - 12f)
    return lonLatToVec(wrapLongitude(lonDeg.toRadians()), declination)
}

fun Float.toRadians(): Float = (this * PI / 180.0).toFloat()

fun Float.toDegrees(): Float = (this * 180.0 / PI).toFloat()

/** Clamp of the pitch angle; at ±90° yaw stops changing anything visible (GLOBE.md §11a). */
val MaxPitchRad = 85f.toRadians()

internal fun smoothstep(edge0: Float, edge1: Float, x: Float): Float {
    val t = ((x - edge0) / (edge1 - edge0)).coerceIn(0f, 1f)
    return t * t * (3f - 2f * t)
}

/** Formats a country area the way the selection card wants it. */
fun formatArea(areaKm2: Long): String = when {
    areaKm2 >= 1_000_000 -> "${(areaKm2 / 100_000L) / 10f} M km²"
    areaKm2 >= 1_000 -> "${(areaKm2 / 1_000L)}k km²"
    else -> "$areaKm2 km²"
}

/** Formats a lat/lon pair as e.g. `19.0°N 72.9°E`. */
fun formatLatLon(latDeg: Float, lonDeg: Float): String {
    val ns = if (latDeg >= 0) "N" else "S"
    val ew = if (lonDeg >= 0) "E" else "W"
    return "${(abs(latDeg) * 10).roundToInt() / 10f}°$ns ${(abs(lonDeg) * 10).roundToInt() / 10f}°$ew"
}
