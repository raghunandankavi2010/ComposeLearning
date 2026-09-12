package com.example.composelearning.globe

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlin.math.exp

/**
 * Orientation, zoom and inertia of the globe — GLOBE.md §11.
 *
 * Every value lives in a `mutableFloatStateOf` that the render pass reads inside its draw
 * block, so the 60 Hz spin invalidates *draw* only: no recomposition per frame.
 */
class GlobeState(
    initialLonDeg: Float = 20f,
    initialLatDeg: Float = 18f,
    /** Idle rotation rate. 6°/s ⇒ one revolution per minute. */
    val spinDegPerSec: Float = 6f,
    /** Fling time constant τ: a flick adds (ω₀ − ω_spin)·τ radians of extra sweep. */
    val flingTau: Float = 0.9f
) {
    var lonRad by mutableFloatStateOf(initialLonDeg.toRadians())
        private set
    var latRad by mutableFloatStateOf(initialLatDeg.toRadians())
        private set
    var zoom by mutableFloatStateOf(1f)
        private set

    var spinEnabled by mutableStateOf(true)
    var isInteracting by mutableStateOf(false)
        private set

    /** Upper zoom bound, derived from the texture width by GLOBE.md §11c. */
    var maxZoom by mutableFloatStateOf(2f)

    private var yawOmega = 0f   // rad/s
    private var pitchOmega = 0f

    val rotation: GlobeRotation get() = GlobeRotation(lonRad, latRad)

    private val spinOmega: Float
        get() = if (spinEnabled) -spinDegPerSec.toRadians() else 0f

    fun onGestureStart() {
        isInteracting = true
        yawOmega = 0f
        pitchOmega = 0f
    }

    /**
     * §11a: at the disc centre a pixel of drag is exactly `1/K` radians of rotation, because
     * the on-screen radius *is* K. Pitch is clamped short of the pole, where yaw would stop
     * doing anything the user can see.
     */
    fun drag(dx: Float, dy: Float, k: Float) {
        if (k <= 0f) return
        lonRad = wrapLongitude(lonRad - dx / k)
        latRad = (latRad + dy / k).coerceIn(-MaxPitchRad, MaxPitchRad)
    }

    fun pinch(scaleChange: Float) {
        zoom = (zoom * scaleChange).coerceIn(1f, maxZoom)
    }

    /** §11b: hand the release velocity over as an angular rate. */
    fun fling(velocityX: Float, velocityY: Float, k: Float) {
        isInteracting = false
        if (k <= 0f) return
        yawOmega = -velocityX / k
        pitchOmega = velocityY / k
    }

    fun endGesture() {
        isInteracting = false
    }

    /**
     * §11b: exponential decay of the yaw rate toward the idle spin (not toward zero — the
     * globe never dead-stops, it resumes its slow turn), and of the pitch rate toward zero.
     * `exp(−Δt/τ)` rather than a fixed per-frame factor keeps the motion identical at 60,
     * 90 and 120 Hz.
     */
    fun tick(dtSec: Float) {
        if (isInteracting) return
        val decay = exp(-dtSec / flingTau)
        val target = spinOmega
        yawOmega = target + (yawOmega - target) * decay
        pitchOmega *= decay
        lonRad = wrapLongitude(lonRad + yawOmega * dtSec)
        if (pitchOmega != 0f) {
            latRad = (latRad + pitchOmega * dtSec).coerceIn(-MaxPitchRad, MaxPitchRad)
        }
    }

    fun snapTo(lonDeg: Float, latDeg: Float) {
        lonRad = wrapLongitude(lonDeg.toRadians())
        latRad = latDeg.toRadians().coerceIn(-MaxPitchRad, MaxPitchRad)
        yawOmega = 0f
        pitchOmega = 0f
    }

    fun resetZoom() {
        zoom = 1f
    }
}

@Composable
fun rememberGlobeState(
    initialLonDeg: Float = 20f,
    initialLatDeg: Float = 18f
): GlobeState = remember { GlobeState(initialLonDeg, initialLatDeg) }
