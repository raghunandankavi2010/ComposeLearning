package com.example.composelearning.sarvamstt

/**
 * Normalizes native spoken number words to standard digits for various Indian languages.
 * Handles single digits, tens, hundreds, thousands, and collapses them into a single numeric string.
 */
object MultiLanguageNumberNormalizer {

    private val languageMatrices = mapOf(
        // KANNADA
        "kn" to mapOf(
            "ಸೊನ್ನೆ" to "0", "ಒಂದು" to "1", "ಎರಡು" to "2", "ಮೂರು" to "3", "ನಾಲ್ಕು" to "4",
            "ಐದು" to "5", "ಆರು" to "6", "ಏಳು" to "7", "ಎಂಟು" to "8", "ಒಂಬತ್ತು" to "9",
            "ಹತ್ತು" to "10", "ಇಪ್ಪತ್ತು" to "20", "ಮೂವತ್ತು" to "30", "ನಲವತ್ತು" to "40", "ಐವತ್ತು" to "50",
            "ಅರವತ್ತು" to "60", "ಎಪ್ಪತ್ತು" to "70", "ಎಂಬತ್ತು" to "80", "ತೊಂಬತ್ತು" to "90",
            "ನೂರು" to "100", "ಸಾವಿರ" to "1000", "ಲಕ್ಷ" to "100000"
        ),
        // TELUGU
        "te" to mapOf(
            "సున్నా" to "0", "ఒకటి" to "1", "రెండు" to "2", "మూడు" to "3", "నాలుగు" to "4",
            "ఐదు" to "5", "ఆరు" to "6", "ఏడు" to "7", "ఎనిమిది" to "8", "తొమ్మిది" to "9",
            "పది" to "10", "ఇరవై" to "20", "ముప్పై" to "30", "నలభై" to "40", "యాభై" to "50",
            "అరవై" to "60", "డెబ్బై" to "70", "ఎనభై" to "80", "తొంభై" to "90",
            "వంద" to "100", "వేయి" to "1000", "లక్ష" to "100000"
        ),
        // TAMIL
        "ta" to mapOf(
            "பூஜ்ஜியம்" to "0", "சைபர்" to "0",
            "ஒன்று" to "1", "ஒன்னு" to "1",
            "இரண்டு" to "2", "ரெண்டு" to "2",
            "மூன்று" to "3", "மூணு" to "3",
            "நான்கு" to "4", "நாலு" to "4",
            "ஐந்து" to "5", "அஞ்சு" to "5",
            "ஆறு" to "6", "ஏழு" to "7", "எட்டு" to "8", "ஒன்பது" to "9",
            "பத்து" to "10", "இருபது" to "20", "முப்பது" to "30", "நாற்பது" to "40", "ஐம்பது" to "50",
            "அறுபது" to "60", "எழுபது" to "70", "எண்பது" to "80", "தொண்ணூறு" to "90",
            "நூறு" to "100", "ஆயிரம்" to "1000", "லட்சம்" to "100000"
        ),
        // MARATHI
        "mr" to mapOf(
            "शून्य" to "0", "एक" to "1", "दोन" to "2", "तीन" to "3", "चार" to "4",
            "पाच" to "5", "सहा" to "6", "सात" to "7", "आठ" to "8", "नऊ" to "9",
            "दहा" to "10", "वीस" to "20", "तीस" to "30", "चाळीस" to "40", "पन्नास" to "50",
            "साठ" to "60", "सत्तर" to "70", "ऐंशी" to "80", "नव्वद" to "90",
            "शंभर" to "100", "हजार" to "1000", "लाख" to "100000"
        ),
        // GUJARATI
        "gu" to mapOf(
            "શૂન્ય" to "0", "એક" to "1", "બે" to "2", "ત્રણ" to "3", "ચાર" to "4",
            "પાંચ" to "5", "છ" to "6", "સાત" to "7", "આઠ" to "8", "નવ" to "9",
            "દસ" to "10", "વીસ" to "20", "ત્રીસ" to "30", "ચાલીસ" to "40", "પચાસ" to "50",
            "સાઠ" to "60", "સિત્તેર" to "70", "એસી" to "80", "નેવું" to "90",
            "સો" to "100", "હજાર" to "1000", "લાખ" to "100000"
        ),
        // HINDI
        "hi" to mapOf(
            "शून्य" to "0", "जीरो" to "0", "एक" to "1", "दो" to "2", "तीन" to "3",
            "चार" to "4", "पांच" to "5", "पाँच" to "5", "छह" to "6", "छः" to "6",
            "सात" to "7", "आठ" to "8", "नौ" to "9",
            "दस" to "10", "ग्यारह" to "11", "बारह" to "12", "तेरह" to "13", "चौदह" to "14",
            "पंद्रह" to "15", "सोलह" to "16", "सत्रह" to "17", "अठारह" to "18", "उन्नीस" to "19",
            "बीस" to "20", "तीस" to "30", "चालीस" to "40", "पचास" to "50",
            "साठ" to "60", "सत्तर" to "70", "अस्सी" to "80", "नब्बे" to "90",
            "सौ" to "100", "हजार" to "1000", "लाख" to "100000"
        )
    )

    private val nativeDigitMaps = mapOf(
        "hi" to "०१२३४५६७८९",
        "mr" to "०१२३४५६७८९",
        "kn" to "೦೧೨೩೪೫೬೭೮೯",
        "te" to "౦౧౨౩౪౫౬౭౮౯",
        "ta" to "௦௧௨௩௪௫௬௭௮௯",
        "gu" to "૦૧૨૩૪૫૬૭૮૯"
    )

    fun normalize(transcript: String, languageCode: String): String {
        if (transcript.isBlank()) return transcript

        val baseLang = languageCode.split("-").first().lowercase()
        val targetMap = languageMatrices[baseLang]

        var processedText = transcript

        // 1. If we have a map for this language, replace spoken words with digits
        if (targetMap != null) {
            // Sort by length descending to match longer phrases first (e.g. "twenty five" before "five")
            val sortedWords = targetMap.keys.sortedByDescending { it.length }
            for (word in sortedWords) {
                // Use word boundary to avoid partial matches
                processedText = processedText.replace(Regex("(?i)\\b$word\\b"), targetMap[word]!!)
            }
        }

        // 2. Collapse whitespaces between ANY digits (Latin or already converted)
        processedText = processedText.replace(Regex("(?<=\\d)\\s+(?=\\d)"), "")

        // 3. Force-convert all Latin digits to Native Script digits if available
        val nativeDigits = nativeDigitMaps[baseLang]
        return if (nativeDigits != null) {
            processedText.map { char ->
                if (char in '0'..'9') {
                    nativeDigits[char - '0']
                } else {
                    char
                }
            }.joinToString("")
        } else {
            processedText
        }
    }
}
