package com.example.composelearning.applerings.domain.model

/**
 * One activity ring.
 *
 * @param targetTurns how far the ring fills, in turns (1.0 = 100% = 360°). May exceed 1.0,
 *                    in which case the arc overlaps itself (the famous "over 100%" look).
 * @param insetSteps how many stroke-widths smaller than the outermost ring this one is.
 */
data class RingSpec(
    val startColor: Long,
    val endColor: Long,
    val trackColor: Long,
    val targetTurns: Float,
    val insetSteps: Int
)
