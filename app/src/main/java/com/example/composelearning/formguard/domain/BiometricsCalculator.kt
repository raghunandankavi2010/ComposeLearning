package com.example.composelearning.formguard.domain

import com.example.composelearning.formguard.domain.model.PoseLandmark
import kotlin.math.abs
import kotlin.math.atan2

/**
 * Pure trigonometric helpers for resolving joint angles from normalized landmark coordinates.
 *
 * Stateless and allocation-free (operates on primitives / existing [PoseLandmark]s), so it can be
 * called on the per-frame analysis path without adding GC pressure, and unit-tested on a plain JVM.
 */
object BiometricsCalculator {

    /**
     * Interior angle, in **degrees** `[0, 180]`, of the corner `a → vertex → c` measured *at*
     * [vertex] — e.g. the knee flexion angle from Hip → Knee → Ankle.
     *
     * Uses the two-argument arctangent (numerically stable, no division-by-zero on vertical
     * segments) and folds the signed result into `[0, 180]` so left/right legs report the same
     * value regardless of which way the corner opens.
     *
     * ```
     * θ = | atan2(c.y − v.y, c.x − v.x) − atan2(a.y − v.y, a.x − v.x) |  → folded to [0, 180]
     * ```
     */
    fun jointAngleDegrees(a: PoseLandmark, vertex: PoseLandmark, c: PoseLandmark): Float {
        val radians = atan2(c.y - vertex.y, c.x - vertex.x) -
            atan2(a.y - vertex.y, a.x - vertex.x)
        var degrees = abs(Math.toDegrees(radians.toDouble())).toFloat()
        if (degrees > 180f) degrees = 360f - degrees
        return degrees
    }

    /** Horizontal (x-axis) gap between two landmarks in normalized units; used for valgus checks. */
    fun horizontalGap(a: PoseLandmark, b: PoseLandmark): Float = abs(a.x - b.x)
}
