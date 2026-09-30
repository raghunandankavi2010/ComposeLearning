package com.example.composelearning.physiolens.presentation

import androidx.compose.runtime.Immutable

/**
 * Contract for PhysioLens AI Biomechanical Strain & Kinetic Engine.
 */
object PhysioLensContract {

    @Immutable
    data class PosePoint(
        val x: Float, // 0..1 normalized
        val y: Float, // 0..1 normalized
        val z: Float = 0f,
        val visibility: Float = 1f
    )

    enum class MuscleGroup(val displayName: String) {
        BICEPS("Biceps Brachii"),
        TRICEPS("Triceps Brachii"),
        DELTOIDS("Anterior/Lateral Deltoid"),
        QUADRICEPS("Quadriceps Femoris"),
        HAMSTRINGS("Hamstrings"),
        GLUTES("Gluteus Maximus"),
        ERECTOR_SPINAE("Erector Spinae (Lower Back)")
    }

    @Immutable
    data class MuscleStrainData(
        val muscleGroup: MuscleGroup,
        val strainPercentage: Float, // 0..100%
        val momentArmCm: Float, // Lever distance in cm
        val torqueNm: Float, // Estimated Torque in Newton-meters
        val status: StrainStatus
    )

    enum class StrainStatus {
        OPTIMAL, // Green
        MODERATE, // Yellow
        HIGH_STRAIN, // Red
        OVERLOAD // Critical
    }

    @Immutable
    data class JointKinematics(
        val elbowAngleLeft: Float,
        val elbowAngleRight: Float,
        val kneeAngleLeft: Float,
        val kneeAngleRight: Float,
        val hipAngleLeft: Float,
        val hipAngleRight: Float,
        val lumbarFlexionAngle: Float,
        val L4L5ShearLoadN: Float // Shear load in Newtons
    )

    @Immutable
    data class BiomechanicalAnalysis(
        val jointKinematics: JointKinematics,
        val muscleStrains: List<MuscleStrainData>,
        val overallFatigueIndex: Float, // 0..100%
        val primaryActiveMuscle: MuscleGroup,
        val formDeviations: List<String>,
        val injuryRiskScore: Float, // 0..100%
        val aiCorrectionTip: String,
        val aiRecoveryProtocol: String
    )

    @Immutable
    data class State(
        val isCameraPermissionGranted: Boolean = false,
        val isAnalyzing: Boolean = true,
        val isSimulationMode: Boolean = false,
        val posePoints: Map<Int, PosePoint> = emptyMap(),
        val imageWidth: Int = 1080,
        val imageHeight: Int = 1920,
        val analysis: BiomechanicalAnalysis? = null,
        val selectedTab: Int = 0 // 0: Live HUD, 1: Muscle Heatmap, 2: AI Recovery Advice
    )

    sealed interface Intent {
        data class PermissionResult(val isGranted: Boolean) : Intent
        data object ToggleSimulationMode : Intent
        data class SelectTab(val index: Int) : Intent
        data object ResetAnalysis : Intent
    }
}
