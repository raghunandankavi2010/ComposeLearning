package com.example.composelearning.formguard.domain

import com.example.composelearning.formguard.domain.model.PoseFrame
import com.example.composelearning.formguard.domain.model.PoseLandmark
import com.example.composelearning.formguard.domain.model.PoseLandmarks

/**
 * The squat **state machine + rep counter + form checker**. Stateful but pure (no Android /
 * MediaPipe types): feed it one [PoseFrame] per analysed frame and it returns the current
 * [SquatAssessment]. All thresholds are in the squat's natural units (degrees / normalized width)
 * so the whole thing is trivially unit-testable.
 *
 * Single-threaded by contract: it is driven only from the ViewModel's pose callback (MediaPipe's
 * result-listener thread), so the mutable phase/rep fields need no synchronization.
 */
class SquatFormEvaluator {

    /** Coarse phase of the current rep, derived from the knee angle. */
    enum class Phase { STANDING, DESCENDING, BOTTOM }

    private var phase = Phase.STANDING
    private var repCount = 0
    private var reachedDepth = false

    /**
     * Process one frame. Returns a small immutable [SquatAssessment]; the only per-frame allocation
     * on this path, and it runs on the listener thread (not the camera-buffer thread).
     */
    fun evaluate(frame: PoseFrame): SquatAssessment {
        if (frame.landmarks.size < PoseLandmarks.COUNT) return notVisible()

        val l = frame.landmarks
        val leftVisible = visible(l[PoseLandmarks.LEFT_HIP], l[PoseLandmarks.LEFT_KNEE], l[PoseLandmarks.LEFT_ANKLE])
        val rightVisible = visible(l[PoseLandmarks.RIGHT_HIP], l[PoseLandmarks.RIGHT_KNEE], l[PoseLandmarks.RIGHT_ANKLE])

        if (!leftVisible && !rightVisible) return notVisible()

        // Average the angle over whichever legs are confidently visible.
        var angleSum = 0f
        var sides = 0
        if (leftVisible) {
            angleSum += BiometricsCalculator.jointAngleDegrees(
                l[PoseLandmarks.LEFT_HIP], l[PoseLandmarks.LEFT_KNEE], l[PoseLandmarks.LEFT_ANKLE],
            )
            sides++
        }
        if (rightVisible) {
            angleSum += BiometricsCalculator.jointAngleDegrees(
                l[PoseLandmarks.RIGHT_HIP], l[PoseLandmarks.RIGHT_KNEE], l[PoseLandmarks.RIGHT_ANKLE],
            )
            sides++
        }
        val kneeAngle = angleSum / sides

        val caving = detectKneeValgus(l, leftVisible && rightVisible, kneeAngle)
        advanceStateMachine(kneeAngle)

        return SquatAssessment(
            personDetected = true,
            kneeAngle = kneeAngle,
            repCount = repCount,
            phase = phase,
            isKneesCaving = caving,
            feedbackMessage = feedbackFor(kneeAngle, caving),
        )
    }

    /** Resets reps and phase (e.g. when the user taps "reset"). */
    fun reset() {
        phase = Phase.STANDING
        repCount = 0
        reachedDepth = false
    }

    /**
     * Hysteresis-based transitions so a single rep is counted exactly once:
     *  - arm `reachedDepth` only once the knee bends to/under [DEPTH_ANGLE],
     *  - count the rep (and disarm) only when the knee straightens back past [STAND_ANGLE].
     */
    private fun advanceStateMachine(kneeAngle: Float) {
        when {
            kneeAngle <= DEPTH_ANGLE -> {
                reachedDepth = true
                phase = Phase.BOTTOM
            }
            kneeAngle >= STAND_ANGLE -> {
                if (reachedDepth) {
                    repCount++
                    reachedDepth = false
                }
                phase = Phase.STANDING
            }
            else -> {
                // Mid-range: descending unless we've already touched depth and are on the way up.
                phase = if (reachedDepth) Phase.BOTTOM else Phase.DESCENDING
            }
        }
    }

    /**
     * Knee valgus = the knees drawing inward relative to the ankles while the legs are bent.
     * Flags when `kneeWidth / ankleWidth` collapses below [VALGUS_RATIO] and the user is not
     * standing tall. Requires both legs visible to compare widths.
     */
    private fun detectKneeValgus(
        l: List<PoseLandmark>,
        bothLegsVisible: Boolean,
        kneeAngle: Float,
    ): Boolean {
        if (!bothLegsVisible || kneeAngle >= STAND_ANGLE) return false
        val kneeWidth = BiometricsCalculator.horizontalGap(l[PoseLandmarks.LEFT_KNEE], l[PoseLandmarks.RIGHT_KNEE])
        val ankleWidth = BiometricsCalculator.horizontalGap(l[PoseLandmarks.LEFT_ANKLE], l[PoseLandmarks.RIGHT_ANKLE])
        if (ankleWidth <= 0f) return false
        return kneeWidth / ankleWidth < VALGUS_RATIO
    }

    private fun feedbackFor(kneeAngle: Float, caving: Boolean): String? = when {
        caving -> "Knees caving in — push them out"
        phase == Phase.DESCENDING && kneeAngle < DEPTH_COACH_ANGLE -> "Go deeper"
        phase == Phase.BOTTOM -> "Good depth — drive up"
        phase == Phase.STANDING && repCount > 0 -> "Rep $repCount ✓"
        else -> null
    }

    private fun notVisible(): SquatAssessment = SquatAssessment(
        personDetected = false,
        kneeAngle = 0f,
        repCount = repCount,
        phase = phase,
        isKneesCaving = false,
        feedbackMessage = "Step fully into frame",
    )

    private fun visible(vararg points: PoseLandmark): Boolean =
        points.all { it.visibility >= MIN_VISIBILITY }

    private companion object {
        /** Knee straight enough to count as standing / complete the rep. */
        const val STAND_ANGLE = 160f

        /** Knee bent enough to count as a valid-depth squat. */
        const val DEPTH_ANGLE = 100f

        /** Below this (while descending) we nudge the user to squat deeper. */
        const val DEPTH_COACH_ANGLE = 140f

        /** knee-width / ankle-width below this during the bend ⇒ knees caving in. */
        const val VALGUS_RATIO = 0.60f

        /** Minimum landmark visibility to trust a leg for analysis. */
        const val MIN_VISIBILITY = 0.5f
    }
}

/**
 * Immutable result of evaluating one frame.
 *
 * @property personDetected false when no leg is confidently visible (the maths is meaningless).
 * @property kneeAngle      averaged knee flexion in degrees (`[0, 180]`).
 * @property repCount       completed squats so far.
 * @property phase          coarse phase of the current rep.
 * @property isKneesCaving  true while a valgus fault is detected.
 * @property feedbackMessage short coaching/status line, or null when nothing to say.
 */
data class SquatAssessment(
    val personDetected: Boolean,
    val kneeAngle: Float,
    val repCount: Int,
    val phase: SquatFormEvaluator.Phase,
    val isKneesCaving: Boolean,
    val feedbackMessage: String?,
)
