package com.example.composelearning.sarvamlid

import java.io.IOException

/**
 * Offline-first orchestration:
 *  1. Resolve from on-device Unicode script (no network) — covers Gujarati, Telugu, Tamil, Kannada.
 *  2. Only when the script is Devanagari (Hindi vs Marathi) do we call the Sarvam cloud API.
 *  3. Map every outcome — including unsupported languages and network errors — onto [DetectionResult].
 *
 * [service] is nullable so the on-device path works with no API key configured at all; a
 * Devanagari input in that case degrades gracefully to a [DetectionResult.Failure].
 */
class SarvamLidRepository(
    private val service: SarvamLidService?,
    private val maxInputChars: Int = 1000,
) {
    suspend fun detect(rawText: String): DetectionResult {
        val text = rawText.trim()
        if (text.isEmpty()) return DetectionResult.EmptyInput

        return when (val outcome = ScriptDetector.detect(text)) {
            is ScriptDetector.Outcome.Resolved ->
                DetectionResult.Success(outcome.language, DetectionSource.ON_DEVICE, outcome.scriptCode)

            is ScriptDetector.Outcome.Ambiguous ->
                disambiguateViaCloud(text, outcome.scriptCode)

            ScriptDetector.Outcome.Unknown ->
                disambiguateViaCloud(text, scriptHint = null)
        }
    }

    /** The cloud is consulted for Devanagari (Hindi/Marathi) and for scripts we can't classify locally. */
    private suspend fun disambiguateViaCloud(text: String, scriptHint: String?): DetectionResult {
        val api = service ?: return DetectionResult.Failure(
            "Offline detection couldn't resolve this text and no API key is configured."
        )

        // Sarvam caps input at 1000 chars; a script signal is fully present in a prefix.
        val clipped = if (text.length > maxInputChars) text.substring(0, maxInputChars) else text

        return try {
            val response = api.identify(clipped)
            when (val language = IndicLanguage.fromSarvamCode(response.languageCode)) {
                null -> DetectionResult.Unsupported(response.languageCode)
                else -> DetectionResult.Success(
                    language = language,
                    source = DetectionSource.CLOUD,
                    scriptCode = response.scriptCode ?: scriptHint,
                )
            }
        } catch (e: SarvamApiException) {
            DetectionResult.Failure(
                when (e.code) {
                    401, 403 -> "Authentication failed — check your Sarvam API key."
                    429 -> "Rate limit reached. Please try again in a moment."
                    in 500..599 -> "Sarvam service is unavailable. Try again later."
                    else -> "Detection failed (HTTP ${e.code})."
                }
            )
        } catch (e: IOException) {
            DetectionResult.Failure("No network connection. Connect and retry.${e.printStackTrace()}")
        }
    }
}
