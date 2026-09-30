package com.example.composelearning.physiolens.domain

import com.example.composelearning.physiolens.presentation.PhysioLensContract.BiomechanicalAnalysis
import com.example.composelearning.physiolens.presentation.PhysioLensContract.JointKinematics
import com.example.composelearning.physiolens.presentation.PhysioLensContract.MuscleGroup
import com.example.composelearning.physiolens.presentation.PhysioLensContract.MuscleStrainData
import com.example.composelearning.physiolens.presentation.PhysioLensContract.PosePoint
import com.example.composelearning.physiolens.presentation.PhysioLensContract.StrainStatus
import kotlin.math.acos
import kotlin.math.abs
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * On-Device Physics & Biomechanical Engine that calculates joint angle kinematics,
 * gravitational moment arms ($r = d \cdot \sin\theta$), torque load vectors ($\tau = r \times F$),
 * lumbar L4/L5 disc shear force, and muscle activation strain percentages.
 */
object BiomechanicalEngine {

    // MediaPipe Pose Landmark Indices
    const val NOSE = 0
    const val LEFT_SHOULDER = 11
    const val RIGHT_SHOULDER = 12
    const val LEFT_ELBOW = 13
    const val RIGHT_ELBOW = 14
    const val LEFT_WRIST = 15
    const val RIGHT_WRIST = 16
    const val LEFT_HIP = 23
    const val RIGHT_HIP = 24
    const val LEFT_KNEE = 25
    const val RIGHT_KNEE = 26
    const val LEFT_ANKLE = 27
    const val RIGHT_ANKLE = 28

    private const val AVERAGE_FOREARM_MASS_KG = 2.5f
    private const val AVERAGE_TORSO_MASS_KG = 35.0f
    const val GRAVITY = 9.81f

    fun calculateBiomechanics(landmarks: Map<Int, PosePoint>): BiomechanicalAnalysis {
        val leftShoulder = landmarks[LEFT_SHOULDER]
        val rightShoulder = landmarks[RIGHT_SHOULDER]
        val leftElbow = landmarks[LEFT_ELBOW]
        val rightElbow = landmarks[RIGHT_ELBOW]
        val leftWrist = landmarks[LEFT_WRIST]
        val rightWrist = landmarks[RIGHT_WRIST]
        val leftHip = landmarks[LEFT_HIP]
        val rightHip = landmarks[RIGHT_HIP]
        val leftKnee = landmarks[LEFT_KNEE]
        val rightKnee = landmarks[RIGHT_KNEE]
        val leftAnkle = landmarks[LEFT_ANKLE]
        val rightAnkle = landmarks[RIGHT_ANKLE]

        val elbowAngleR = calculateAngle(rightShoulder, rightElbow, rightWrist)
        val elbowAngleL = calculateAngle(leftShoulder, leftElbow, leftWrist)
        val kneeAngleR = calculateAngle(rightHip, rightKnee, rightAnkle)
        val kneeAngleL = calculateAngle(leftHip, leftKnee, leftAnkle)
        val hipAngleR = calculateAngle(rightShoulder, rightHip, rightKnee)
        val hipAngleL = calculateAngle(leftShoulder, leftHip, leftKnee)

        // Calculate Lumbar flex angle relative to vertical spine
        val spineMidShoulderX = if (leftShoulder != null && rightShoulder != null) (leftShoulder.x + rightShoulder.x) / 2f else 0.5f
        val spineMidShoulderY = if (leftShoulder != null && rightShoulder != null) (leftShoulder.y + rightShoulder.y) / 2f else 0.2f
        val spineMidHipX = if (leftHip != null && rightHip != null) (leftHip.x + rightHip.x) / 2f else 0.5f
        val spineMidHipY = if (leftHip != null && rightHip != null) (leftHip.y + rightHip.y) / 2f else 0.6f

        val spineVectorX = spineMidShoulderX - spineMidHipX
        val spineVectorY = spineMidShoulderY - spineMidHipY
        val spineLength = sqrt(spineVectorX * spineVectorX + spineVectorY * spineVectorY)
        val lumbarFlexAngle = if (spineLength > 0.001f) {
            val dotWithVertical = abs(spineVectorY) / spineLength
            Math.toDegrees(acos(dotWithVertical.coerceIn(-1f, 1f)).toDouble()).toFloat()
        } else 0f

        // Moment arm for Biceps (horizontal distance from elbow to wrist)
        val bicepMomentArmCm = if (rightElbow != null && rightWrist != null) {
            abs(rightWrist.x - rightElbow.x) * 100f * 0.45f // Approx scale factor
        } else 18f

        // Bicep Torque = Moment Arm * Mass * Gravity
        val bicepTorqueNm = (bicepMomentArmCm / 100f) * AVERAGE_FOREARM_MASS_KG * GRAVITY
        val bicepStrain = (bicepTorqueNm / 12f * 100f).coerceIn(10f, 98f)

        // Deltoid Moment Arm (horizontal distance from shoulder to wrist)
        val deltoidMomentArmCm = if (rightShoulder != null && rightWrist != null) {
            abs(rightWrist.x - rightShoulder.x) * 100f * 0.5f
        } else 25f
        val deltoidTorqueNm = (deltoidMomentArmCm / 100f) * AVERAGE_FOREARM_MASS_KG * 1.5f * GRAVITY
        val deltoidStrain = (deltoidTorqueNm / 20f * 100f).coerceIn(15f, 95f)

        // Quad Strain (derived from knee flex angle)
        val quadStrain = if (kneeAngleR > 0f) {
            ((180f - kneeAngleR) / 100f * 100f).coerceIn(10f, 95f)
        } else 35f
        val quadMomentArmCm = (sin(Math.toRadians((180f - kneeAngleR).toDouble())).toFloat() * 32f).coerceAtLeast(5f)
        val quadTorqueNm = (quadMomentArmCm / 100f) * 40f * GRAVITY

        // Lumbar L4/L5 Shear force (N) = Spine Torque from torso tilt
        val torsoTiltRad = Math.toRadians(lumbarFlexAngle.toDouble())
        val L4L5ShearN = (AVERAGE_TORSO_MASS_KG * GRAVITY * sin(torsoTiltRad)).toFloat()
        val erectorSpinaeStrain = (L4L5ShearN / 250f * 100f).coerceIn(12f, 99f)

        val muscleStrains = listOf(
            MuscleStrainData(
                muscleGroup = MuscleGroup.BICEPS,
                strainPercentage = bicepStrain,
                momentArmCm = bicepMomentArmCm,
                torqueNm = bicepTorqueNm,
                status = getStrainStatus(bicepStrain)
            ),
            MuscleStrainData(
                muscleGroup = MuscleGroup.DELTOIDS,
                strainPercentage = deltoidStrain,
                momentArmCm = deltoidMomentArmCm,
                torqueNm = deltoidTorqueNm,
                status = getStrainStatus(deltoidStrain)
            ),
            MuscleStrainData(
                muscleGroup = MuscleGroup.QUADRICEPS,
                strainPercentage = quadStrain,
                momentArmCm = quadMomentArmCm,
                torqueNm = quadTorqueNm,
                status = getStrainStatus(quadStrain)
            ),
            MuscleStrainData(
                muscleGroup = MuscleGroup.ERECTOR_SPINAE,
                strainPercentage = erectorSpinaeStrain,
                momentArmCm = sin(torsoTiltRad).toFloat() * 40f,
                torqueNm = (L4L5ShearN * 0.35f),
                status = getStrainStatus(erectorSpinaeStrain)
            )
        )

        val formDeviations = mutableListOf<String>()
        if (lumbarFlexAngle > 25f) {
            formDeviations.add("Excessive lumbar flexion (${lumbarFlexAngle.toInt()}°) — High spinal disc shear load.")
        }
        if (abs(kneeAngleL - kneeAngleR) > 18f) {
            formDeviations.add("Asymmetric leg load balance — Shift weight evenly between left and right feet.")
        }
        if (deltoidMomentArmCm > 40f && bicepStrain > 80f) {
            formDeviations.add("Elbow flaring & hip swinging momentum detected on arm lift.")
        }

        val primaryActive = muscleStrains.maxByOrNull { it.strainPercentage }?.muscleGroup ?: MuscleGroup.BICEPS
        val maxStrain = muscleStrains.maxOfOrNull { it.strainPercentage } ?: 40f
        val overallFatigue = (maxStrain * 0.75f + lumbarFlexAngle * 0.5f).coerceIn(15f, 96f)
        val injuryRisk = (formDeviations.size * 30f + (if (L4L5ShearN > 180f) 25f else 0f)).coerceIn(5f, 95f)

        val aiCorrection = when {
            formDeviations.isNotEmpty() -> formDeviations.joinToString(" ")
            maxStrain > 85f -> "High muscular load at mechanical disadvantage. Keep elbows braced close to ribcage."
            else -> "Optimal biomechanical alignment. Kinetic chain efficiency is high."
        }

        val aiRecovery = when {
            injuryRisk > 50f -> "High L4/L5 disc tension detected. Apply 10-min cat-cow spinal decompression stretches and ice therapy."
            overallFatigue > 70f -> "Significant neuromuscular fatigue. Focus on 48-hr glycogen recovery, magnesium supplementation, and active foam rolling."
            else -> "Low physiological strain. Normal post-workout active recovery and hydration required."
        }

        return BiomechanicalAnalysis(
            jointKinematics = JointKinematics(
                elbowAngleLeft = elbowAngleL,
                elbowAngleRight = elbowAngleR,
                kneeAngleLeft = kneeAngleL,
                kneeAngleRight = kneeAngleR,
                hipAngleLeft = hipAngleL,
                hipAngleRight = hipAngleR,
                lumbarFlexionAngle = lumbarFlexAngle,
                L4L5ShearLoadN = L4L5ShearN
            ),
            muscleStrains = muscleStrains,
            overallFatigueIndex = overallFatigue,
            primaryActiveMuscle = primaryActive,
            formDeviations = formDeviations,
            injuryRiskScore = injuryRisk,
            aiCorrectionTip = aiCorrection,
            aiRecoveryProtocol = aiRecovery
        )
    }

    private fun getStrainStatus(strain: Float): StrainStatus = when {
        strain < 45f -> StrainStatus.OPTIMAL
        strain < 70f -> StrainStatus.MODERATE
        strain < 88f -> StrainStatus.HIGH_STRAIN
        else -> StrainStatus.OVERLOAD
    }

    private fun calculateAngle(a: PosePoint?, b: PosePoint?, c: PosePoint?): Float {
        if (a == null || b == null || c == null) return 180f
        val abX = a.x - b.x
        val abY = a.y - b.y
        val cbX = c.x - b.x
        val cbY = c.y - b.y

        val dot = abX * cbX + abY * cbY
        val magAb = sqrt(abX * abX + abY * abY)
        val magCb = sqrt(cbX * cbX + cbY * cbY)

        if (magAb * magCb < 0.0001f) return 180f
        val cosTheta = (dot / (magAb * magCb)).coerceIn(-1f, 1f)
        return Math.toDegrees(acos(cosTheta.toDouble())).toFloat()
    }

    fun generateSimulatedLandmarks(elapsedTimeMs: Long): Map<Int, PosePoint> {
        val phase = (elapsedTimeMs % 3000) / 3000f * 2 * Math.PI
        val curlProgress = sin(phase).toFloat() // -1..1

        val shoulderX = 0.5f
        val shoulderY = 0.3f
        val elbowX = 0.52f
        val elbowY = 0.50f

        // Wrist moves in curl arc
        val wristX = 0.52f + 0.15f * (1f - (curlProgress + 1f) / 2f)
        val wristY = 0.50f - 0.22f * ((curlProgress + 1f) / 2f)

        return mapOf(
            NOSE to PosePoint(0.5f, 0.18f),
            LEFT_SHOULDER to PosePoint(0.40f, 0.30f),
            RIGHT_SHOULDER to PosePoint(shoulderX, shoulderY),
            LEFT_ELBOW to PosePoint(0.38f, 0.50f),
            RIGHT_ELBOW to PosePoint(elbowX, elbowY),
            LEFT_WRIST to PosePoint(0.38f, 0.65f),
            RIGHT_WRIST to PosePoint(wristX, wristY),
            LEFT_HIP to PosePoint(0.42f, 0.65f),
            RIGHT_HIP to PosePoint(0.50f, 0.65f),
            LEFT_KNEE to PosePoint(0.42f, 0.82f),
            RIGHT_KNEE to PosePoint(0.50f, 0.82f),
            LEFT_ANKLE to PosePoint(0.42f, 0.95f),
            RIGHT_ANKLE to PosePoint(0.50f, 0.95f)
        )
    }
}
