package com.example.composelearning.sarvamlid

/**
 * The six Indic languages this feature targets, paired with the codes Sarvam returns.
 *
 * NOTE: There is no official Sarvam Android/Edge SDK — language identification is a cloud
 * REST API (POST https://api.sarvam.ai/text-lid). To stay "offline-first" we resolve the
 * language from the Unicode script on-device and only call the cloud when the script is
 * ambiguous (Hindi vs Marathi both use Devanagari).
 */
enum class IndicLanguage(
    val sarvamCode: String,
    val displayName: String,
    val nativeName: String,
) {
    HINDI("hi-IN", "Hindi", "हिन्दी"),
    MARATHI("mr-IN", "Marathi", "मराठी"),
    GUJARATI("gu-IN", "Gujarati", "ગુજરાતી"),
    TELUGU("te-IN", "Telugu", "తెలుగు"),
    TAMIL("ta-IN", "Tamil", "தமிழ்"),
    KANNADA("kn-IN", "Kannada", "ಕನ್ನಡ");

    companion object {
        /** Null for any code outside our six (e.g. en-IN, bn-IN, ml-IN). */
        fun fromSarvamCode(code: String?): IndicLanguage? =
            entries.firstOrNull { it.sarvamCode.equals(code, ignoreCase = true) }
    }
}

/** Where the answer came from — surfaced as a UI badge and useful for analytics. */
enum class DetectionSource { ON_DEVICE, CLOUD }

/** Single exhaustive result type. Every UI state maps to exactly one of these. */
sealed interface DetectionResult {
    data class Success(
        val language: IndicLanguage,
        val source: DetectionSource,
        val scriptCode: String?,
    ) : DetectionResult

    /** Input was blank / whitespace only. */
    data object EmptyInput : DetectionResult

    /** A real language was detected, but not one of our six (e.g. English, Bengali). */
    data class Unsupported(val sarvamCode: String?) : DetectionResult

    /** Network or API failure — carries a user-safe message. */
    data class Failure(val message: String) : DetectionResult
}
