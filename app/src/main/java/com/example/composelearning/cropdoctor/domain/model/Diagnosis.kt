package com.example.composelearning.cropdoctor.domain.model

/** How urgently the farmer needs to act. Drives the colour + tone of the result card. */
enum class Severity { NONE, LOW, MODERATE, HIGH }

/**
 * Farmer-facing knowledge about one plant condition (a single PlantVillage class).
 * Curated, vetted text — deliberately *not* model-generated — so advice is safe and consistent.
 */
data class DiseaseInfo(
    val crop: String,
    /** "Apple Scab", "Late Blight", or "Healthy". */
    val condition: String,
    val isHealthy: Boolean,
    val severity: Severity,
    /** One plain sentence: what this is. */
    val summary: String,
    /** What the farmer should see on the plant. */
    val symptoms: String,
    /** Short, ordered, actionable steps in plain language. */
    val actions: List<String>
)

/** A single class prediction, mapped to farmer-facing advice. */
data class Prediction(
    val info: DiseaseInfo,
    val confidence: Float,
    val rawLabel: String
)

/**
 * The full result of diagnosing one photo.
 *
 * @param best the most likely condition.
 * @param alternates next-most-likely classes (shown small, for transparency).
 * @param isConfident false when even the top class is below the trust threshold — the UI then asks
 *   for a clearer photo instead of showing a possibly-wrong diagnosis.
 */
data class Diagnosis(
    val best: Prediction,
    val alternates: List<Prediction>,
    val isConfident: Boolean
)
