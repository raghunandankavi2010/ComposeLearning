package com.example.composelearning.temples.data

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

private const val EARTH_RADIUS_KM = 6371.0088

/**
 * Great-circle distance in kilometres (haversine).
 *
 * Good enough for "how far is this temple" — within Bengaluru the error against a
 * geodesic is centimetres, and we are rounding to 100 m for display anyway. Road distance
 * will always be longer; the maps app the user hands off to computes that properly.
 */
fun distanceKm(from: GeoPoint, to: GeoPoint): Double {
    val dLat = Math.toRadians(to.lat - from.lat)
    val dLng = Math.toRadians(to.lng - from.lng)
    val a = sin(dLat / 2).pow(2) +
        cos(Math.toRadians(from.lat)) * cos(Math.toRadians(to.lat)) * sin(dLng / 2).pow(2)
    return 2 * EARTH_RADIUS_KM * asin(sqrt(a.coerceIn(0.0, 1.0)))
}

/** "800 m" under a kilometre, "4.2 km" up to 10, then whole kilometres. */
fun formatDistance(km: Double): String = when {
    km < 1.0 -> "${(km * 1000 / 50).roundToInt() * 50} m"
    km < 10.0 -> String.format("%.1f km", km)
    else -> "${km.roundToInt()} km"
}
