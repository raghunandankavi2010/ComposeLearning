package com.example.composelearning.sarvamlid

import java.lang.Character.UnicodeBlock

/**
 * Pure, on-device language detection via Unicode script blocks.
 *
 * Runs synchronously, fully offline, with no API key spent. For 4 of our 6 languages the
 * script is unique, so this is the only step needed. Devanagari is shared by Hindi and
 * Marathi, so it returns [Outcome.Ambiguous] to signal that a cloud lookup is required.
 */
object ScriptDetector {

    sealed interface Outcome {
        data class Resolved(val language: IndicLanguage, val scriptCode: String) : Outcome
        data class Ambiguous(val scriptCode: String) : Outcome   // Devanagari → Hindi vs Marathi
        data object Unknown : Outcome                            // no target Indic script present
    }

    private val TARGET_BLOCKS = setOf(
        UnicodeBlock.DEVANAGARI,
        UnicodeBlock.GUJARATI,
        UnicodeBlock.TELUGU,
        UnicodeBlock.TAMIL,
        UnicodeBlock.KANNADA,
    )

    fun detect(text: String): Outcome {
        // Tally only the scripts we care about; spaces, digits and punctuation are ignored.
        // codePoints() correctly handles surrogate pairs in supplementary planes.
        val counts = HashMap<UnicodeBlock, Int>()
        text.trim().codePoints().forEach { cp ->
            val block = runCatching { UnicodeBlock.of(cp) }.getOrNull() ?: return@forEach
            if (block in TARGET_BLOCKS) counts[block] = (counts[block] ?: 0) + 1
        }

        val dominant = counts.maxByOrNull { it.value }?.key ?: return Outcome.Unknown

        return when (dominant) {
            UnicodeBlock.DEVANAGARI -> Outcome.Ambiguous(scriptCode = "Deva")
            UnicodeBlock.GUJARATI -> Outcome.Resolved(IndicLanguage.GUJARATI, "Gujr")
            UnicodeBlock.TELUGU -> Outcome.Resolved(IndicLanguage.TELUGU, "Telu")
            UnicodeBlock.TAMIL -> Outcome.Resolved(IndicLanguage.TAMIL, "Taml")
            UnicodeBlock.KANNADA -> Outcome.Resolved(IndicLanguage.KANNADA, "Knda")
            else -> Outcome.Unknown
        }
    }
}
