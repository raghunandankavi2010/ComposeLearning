package com.example.composelearning.temples.data

import androidx.compose.ui.graphics.Color

/**
 * Shared building blocks for the temple dataset.
 *
 * The dataset is split by part of the city ([templesCity], [templesSouth], [templesNorthWest],
 * [templesEast]) purely so each file stays readable. Everything that repeats across dozens of
 * records — the "there is a hundi at the sanctum" donation channel, the Karthika Deepotsava
 * entry, the flower markets that serve half the city — lives here once.
 *
 * ## What is and is not asserted
 *
 * Identity, location, deity and history come from Wikipedia/Wikimedia and are safe to trust.
 * Timings, ritual clocks and bus stops are the volatile part: temples change them for
 * festivals, renovations and eclipses without telling anyone. That is what
 * [DataConfidence] on each record is for, and why the detail screen carries a standing
 * "verify before you travel" disclaimer.
 *
 * BMTC route *numbers* are deliberately left empty almost everywhere. They get renumbered
 * often enough that printing a stale number is worse than printing none — so each record
 * names the stop, which is stable, and [busNote] points at the live source.
 */

// ── Photos ──────────────────────────────────────────────────────────────────────────────

/**
 * Builds a [TemplePhoto] from a Wikimedia Commons file name.
 *
 * `Special:FilePath` is used rather than a direct `upload.wikimedia.org` URL: it survives
 * Commons moving the file between hash buckets, and `?width=` makes the thumbnailer resize
 * server-side so a phone never pulls a 20 MP original.
 */
internal fun commons(
    file: String,
    caption: LocalizedText,
    credit: String,
    license: String
): TemplePhoto {
    val name = file.replace(' ', '_')
    return TemplePhoto(
        url = "https://commons.wikimedia.org/wiki/Special:FilePath/$name?width=1280",
        caption = caption,
        credit = credit,
        license = license,
        sourceUrl = "https://commons.wikimedia.org/wiki/File:$name"
    )
}

// ── Accent colours ──────────────────────────────────────────────────────────────────────

/** Deity-led accents, so the list reads as colour-coded without a colour per temple. */
internal val ShivaAccent = Color(0xFF00658F)
internal val VishnuAccent = Color(0xFF6750A4)
internal val DeviAccent = Color(0xFFB3261E)
internal val HanumanAccent = Color(0xFFB35000)
internal val GaneshaAccent = Color(0xFF8C4A00)
internal val SubramanyaAccent = Color(0xFF386A20)

// ── Transport ───────────────────────────────────────────────────────────────────────────

internal val busNote = t(
    "Route numbers are renumbered often — search this stop in the Namma BMTC app for " +
        "today's routes and live arrivals.",
    hi = "रूट नंबर अक्सर बदलते हैं — आज के रूट और लाइव समय के लिए नम्मा BMTC ऐप में यह स्टॉप खोजें।",
    kn = "ಮಾರ್ಗ ಸಂಖ್ಯೆಗಳು ಆಗಾಗ ಬದಲಾಗುತ್ತವೆ — ಇಂದಿನ ಮಾರ್ಗ ಮತ್ತು ಸಮಯಕ್ಕಾಗಿ ನಮ್ಮ BMTC ಆ್ಯಪ್‌ನಲ್ಲಿ ಈ ನಿಲ್ದಾಣವನ್ನು ಹುಡುಕಿ."
)

internal fun bus(stop: String, routes: List<String> = emptyList()) =
    BusInfo(routes = routes, nearestStop = stop, note = busNote)

internal val streetParking = t(
    "On-street parking only, and it fills up early on festival days.",
    hi = "केवल सड़क किनारे पार्किंग; त्योहारों पर जल्दी भर जाती है।",
    kn = "ರಸ್ತೆ ಬದಿ ಪಾರ್ಕಿಂಗ್ ಮಾತ್ರ; ಹಬ್ಬದ ದಿನಗಳಲ್ಲಿ ಬೇಗ ತುಂಬುತ್ತದೆ."
)

internal val ownParking = t(
    "The temple has its own car and two-wheeler parking.",
    hi = "मंदिर की अपनी कार और दोपहिया पार्किंग है।",
    kn = "ದೇವಸ್ಥಾನದ ಸ್ವಂತ ಕಾರು ಮತ್ತು ದ್ವಿಚಕ್ರ ವಾಹನ ಪಾರ್ಕಿಂಗ್ ಇದೆ."
)

// ── Dress code ──────────────────────────────────────────────────────────────────────────

internal val traditionalDress = t(
    "Modest dress. Footwear comes off at the shoe stand outside the outer wall.",
    hi = "शालीन वस्त्र पहनें। बाहरी दीवार के बाहर चप्पल-जूते उतारकर रखें।",
    kn = "ಸಭ್ಯ ಉಡುಪು ಧರಿಸಿ. ಹೊರಗಿನ ಆವರಣದ ಬಳಿ ಪಾದರಕ್ಷೆಗಳನ್ನು ಬಿಡಬೇಕು."
)

internal val strictTraditionalDress = t(
    "Traditional dress expected — dhoti or trousers for men, saree or salwar for women. " +
        "Shorts and sleeveless tops are turned away at the gate.",
    hi = "पारंपरिक वस्त्र अपेक्षित हैं — पुरुषों के लिए धोती या पैंट, महिलाओं के लिए साड़ी या सलवार। " +
        "शॉर्ट्स और स्लीवलेस कपड़ों में प्रवेश नहीं मिलता।",
    kn = "ಸಾಂಪ್ರದಾಯಿಕ ಉಡುಪು ಅಗತ್ಯ — ಪುರುಷರಿಗೆ ಪಂಚೆ ಅಥವಾ ಪ್ಯಾಂಟ್, ಮಹಿಳೆಯರಿಗೆ ಸೀರೆ ಅಥವಾ ಸಲ್ವಾರ್. " +
        "ಶಾರ್ಟ್ಸ್ ಮತ್ತು ತೋಳಿಲ್ಲದ ಉಡುಪುಗಳಿಗೆ ಪ್ರವೇಶವಿಲ್ಲ."
)

// ── Donation channels ───────────────────────────────────────────────────────────────────

internal val hundiDonation = DonationChannel(
    kind = DonationKind.HUNDI,
    label = t("Hundi at the sanctum", hi = "गर्भगृह के पास हुंडी", kn = "ಗರ್ಭಗುಡಿಯ ಬಳಿ ಹುಂಡಿ"),
    detail = t(
        "Cash offering box beside the sanctum. Ask at the counter for a receipt.",
        hi = "गर्भगृह के पास नकद दान पेटी। रसीद के लिए काउंटर पर पूछें।",
        kn = "ಗರ್ಭಗುಡಿಯ ಪಕ್ಕದಲ್ಲಿ ನಗದು ಕಾಣಿಕೆ ಪೆಟ್ಟಿಗೆ. ರಸೀದಿಗಾಗಿ ಕೌಂಟರ್‌ನಲ್ಲಿ ಕೇಳಿ."
    )
)

internal val sevaCounterDonation = DonationChannel(
    kind = DonationKind.SEVA_BOOKING,
    label = t("Seva counter", hi = "सेवा काउंटर", kn = "ಸೇವಾ ಕೌಂಟರ್"),
    detail = t(
        "Archana, abhisheka and alankara sevas are booked in person at the counter; " +
            "carry the receipt to the sanctum.",
        hi = "अर्चना, अभिषेक और अलंकार सेवाएँ काउंटर पर बुक होती हैं; रसीद गर्भगृह तक ले जाएँ।",
        kn = "ಅರ್ಚನೆ, ಅಭಿಷೇಕ ಮತ್ತು ಅಲಂಕಾರ ಸೇವೆಗಳನ್ನು ಕೌಂಟರ್‌ನಲ್ಲಿ ಕಾಯ್ದಿರಿಸಬಹುದು; ರಸೀದಿಯನ್ನು ಗರ್ಭಗುಡಿಗೆ ತೆಗೆದುಕೊಂಡು ಹೋಗಿ."
    )
)

internal val annadanaDonation = DonationChannel(
    kind = DonationKind.ANNADANA,
    label = t("Annadana fund", hi = "अन्नदान निधि", kn = "ಅನ್ನದಾನ ನಿಧಿ"),
    detail = t(
        "Sponsors the free meal served to devotees. Contributions are taken at the office.",
        hi = "भक्तों को दिए जाने वाले नि:शुल्क भोजन के लिए। योगदान कार्यालय में स्वीकार किए जाते हैं।",
        kn = "ಭಕ್ತರಿಗೆ ನೀಡುವ ಉಚಿತ ಊಟಕ್ಕಾಗಿ. ಕಚೇರಿಯಲ್ಲಿ ಕಾಣಿಕೆ ಸ್ವೀಕರಿಸಲಾಗುತ್ತದೆ."
    )
)

internal val muzraiTrust = DonationChannel(
    kind = DonationKind.TRUST,
    label = t(
        "Muzrai (state endowment) temple",
        hi = "मुज़राई (राज्य धर्मादाय) मंदिर",
        kn = "ಮುಜರಾಯಿ (ರಾಜ್ಯ ಧರ್ಮಾದಾಯ) ದೇವಸ್ಥಾನ"
    ),
    detail = t(
        "Managed by the Karnataka Hindu Religious Institutions & Charitable Endowments " +
            "Department — donations go through the temple office and are receipted.",
        hi = "कर्नाटक हिंदू धार्मिक संस्थान एवं धर्मादाय विभाग द्वारा संचालित — दान मंदिर कार्यालय से होता है।",
        kn = "ಕರ್ನಾಟಕ ಹಿಂದೂ ಧಾರ್ಮಿಕ ಸಂಸ್ಥೆಗಳ ಇಲಾಖೆ ನಿರ್ವಹಿಸುತ್ತದೆ — ದೇಣಿಗೆ ದೇವಸ್ಥಾನದ ಕಚೇರಿಯ ಮೂಲಕ."
    )
)

internal fun onlineDonation(url: String) = DonationChannel(
    kind = DonationKind.ONLINE,
    label = t("Donate online", hi = "ऑनलाइन दान करें", kn = "ಆನ್‌ಲೈನ್ ದೇಣಿಗೆ"),
    url = url,
    detail = t(
        "Official page — opens in your browser.",
        hi = "आधिकारिक पृष्ठ — आपके ब्राउज़र में खुलेगा।",
        kn = "ಅಧಿಕೃತ ಪುಟ — ನಿಮ್ಮ ಬ್ರೌಸರ್‌ನಲ್ಲಿ ತೆರೆಯುತ್ತದೆ."
    )
)

// ── Rituals that most temples in the city keep ──────────────────────────────────────────

internal fun morningAbhisheka(time: String = "07:00") = ritual(
    t("Abhisheka", hi = "अभिषेक", kn = "ಅಭಿಷೇಕ"), time
)

internal fun middayMangalarati(time: String = "12:00") = ritual(
    t("Madhyahna Mangalarati", hi = "मध्याह्न मंगलारती", kn = "ಮಧ್ಯಾಹ್ನ ಮಂಗಳಾರತಿ"), time
)

internal fun eveningAlankara(time: String = "18:30") = ritual(
    t("Alankara & Deeparadhane", hi = "अलंकार एवं दीपाराधना", kn = "ಅಲಂಕಾರ ಮತ್ತು ದೀಪಾರಾಧನೆ"), time
)

internal fun nightMangalarati(time: String = "20:00") = ritual(
    t("Ratri Mangalarati", hi = "रात्रि मंगलारती", kn = "ರಾತ್ರಿ ಮಂಗಳಾರತಿ"), time
)

internal fun archanaSeva(priceInr: Int) = ritual(
    t("Archana (name & nakshatra)", hi = "अर्चना (नाम व नक्षत्र)", kn = "ಅರ್ಚನೆ (ಹೆಸರು ಮತ್ತು ನಕ್ಷತ್ರ)"),
    priceInr = priceInr
)

// ── Festivals shared across many temples ────────────────────────────────────────────────

internal val mahaShivaratri = Festival(
    name = t("Maha Shivaratri", hi = "महाशिवरात्रि", kn = "ಮಹಾ ಶಿವರಾತ್ರಿ"),
    whenApprox = t("Feb–Mar (Magha Krishna Chaturdashi)", hi = "फ़र॰–मार्च", kn = "ಫೆಬ್ರವರಿ–ಮಾರ್ಚ್"),
    usualMonth = 2,
    note = t(
        "Open through the night with four jaava abhishekas. Expect the longest queue of the year.",
        hi = "रातभर खुला रहता है, चार जावा अभिषेक होते हैं। साल की सबसे लंबी कतार।",
        kn = "ರಾತ್ರಿಯಿಡೀ ತೆರೆದಿರುತ್ತದೆ, ನಾಲ್ಕು ಜಾವದ ಅಭಿಷೇಕ. ವರ್ಷದ ಅತಿ ಉದ್ದದ ಸರತಿ."
    )
)

internal val ganeshaChaturthi = Festival(
    name = t("Ganesha Chaturthi", hi = "गणेश चतुर्थी", kn = "ಗಣೇಶ ಚತುರ್ಥಿ"),
    whenApprox = t("Aug–Sep (Bhadrapada Shukla Chaturthi)", hi = "अग॰–सित॰", kn = "ಆಗಸ್ಟ್–ಸೆಪ್ಟೆಂಬರ್"),
    usualMonth = 9
)

internal val navaratri = Festival(
    name = t("Navaratri & Dasara", hi = "नवरात्रि एवं दशहरा", kn = "ನವರಾತ್ರಿ ಮತ್ತು ದಸರಾ"),
    whenApprox = t("Sep–Oct (nine nights)", hi = "सित॰–अक्तू॰ (नौ रातें)", kn = "ಸೆಪ್ಟೆಂಬರ್–ಅಕ್ಟೋಬರ್ (ಒಂಬತ್ತು ರಾತ್ರಿ)"),
    usualMonth = 10,
    note = t(
        "A different alankara for the goddess on each of the nine nights.",
        hi = "नौ रातों में हर रात देवी का अलग अलंकार।",
        kn = "ಒಂಬತ್ತು ರಾತ್ರಿಗಳಲ್ಲಿ ಪ್ರತಿದಿನ ದೇವಿಗೆ ಬೇರೆ ಅಲಂಕಾರ."
    )
)

internal val karthikaDeepotsava = Festival(
    name = t("Karthika Deepotsava", hi = "कार्तिक दीपोत्सव", kn = "ಕಾರ್ತಿಕ ದೀಪೋತ್ಸವ"),
    whenApprox = t("Nov–Dec (Karthika masa)", hi = "नव॰–दिस॰ (कार्तिक मास)", kn = "ನವೆಂಬರ್–ಡಿಸೆಂಬರ್ (ಕಾರ್ತಿಕ ಮಾಸ)"),
    usualMonth = 11,
    note = t(
        "The whole prakara is lit with rows of oil lamps after sunset.",
        hi = "सूर्यास्त के बाद पूरा प्रकार दीपों की पंक्तियों से जगमगाता है।",
        kn = "ಸೂರ್ಯಾಸ್ತದ ನಂತರ ಇಡೀ ಪ್ರಾಕಾರ ದೀಪಗಳ ಸಾಲಿನಿಂದ ಬೆಳಗುತ್ತದೆ."
    )
)

internal val ugadi = Festival(
    name = t("Ugadi", hi = "उगादि", kn = "ಯುಗಾದಿ"),
    whenApprox = t("Mar–Apr (Kannada new year)", hi = "मार्च–अप्रैल (कन्नड़ नववर्ष)", kn = "ಮಾರ್ಚ್–ಏಪ್ರಿಲ್ (ಕನ್ನಡ ಹೊಸ ವರ್ಷ)"),
    usualMonth = 4,
    note = t(
        "Panchanga shravana — the year's almanac is read out in the temple.",
        hi = "पंचांग श्रवण — मंदिर में वर्ष का पंचांग पढ़ा जाता है।",
        kn = "ಪಂಚಾಂಗ ಶ್ರವಣ — ದೇವಸ್ಥಾನದಲ್ಲಿ ವರ್ಷದ ಪಂಚಾಂಗ ಓದಲಾಗುತ್ತದೆ."
    )
)

internal val hanumanJayanti = Festival(
    name = t("Hanuma Jayanti", hi = "हनुमान जयंती", kn = "ಹನುಮ ಜಯಂತಿ"),
    whenApprox = t("Dec–Jan (Margashira Shukla Trayodashi)", hi = "दिस॰–जन॰", kn = "ಡಿಸೆಂಬರ್–ಜನವರಿ"),
    usualMonth = 12,
    note = t(
        "Karnataka keeps Hanuma Jayanti in Margashira, not the northern Chaitra date.",
        hi = "कर्नाटक में हनुमान जयंती मार्गशीर्ष में मनाई जाती है, उत्तर भारत की चैत्र तिथि पर नहीं।",
        kn = "ಕರ್ನಾಟಕದಲ್ಲಿ ಹನುಮ ಜಯಂತಿಯನ್ನು ಮಾರ್ಗಶಿರ ಮಾಸದಲ್ಲಿ ಆಚರಿಸಲಾಗುತ್ತದೆ."
    )
)

internal val janmashtami = Festival(
    name = t("Krishna Janmashtami", hi = "कृष्ण जन्माष्टमी", kn = "ಕೃಷ್ಣ ಜನ್ಮಾಷ್ಟಮಿ"),
    whenApprox = t("Aug–Sep (Shravana Krishna Ashtami)", hi = "अग॰–सित॰", kn = "ಆಗಸ್ಟ್–ಸೆಪ್ಟೆಂಬರ್"),
    usualMonth = 8
)

internal val ramaNavami = Festival(
    name = t("Rama Navami", hi = "राम नवमी", kn = "ರಾಮ ನವಮಿ"),
    whenApprox = t("Mar–Apr (Chaitra Shukla Navami)", hi = "मार्च–अप्रैल", kn = "ಮಾರ್ಚ್–ಏಪ್ರಿಲ್"),
    usualMonth = 4,
    note = t(
        "Panaka and kosambari are distributed through the day.",
        hi = "दिनभर पानक और कोसंबरी बाँटी जाती है।",
        kn = "ದಿನವಿಡೀ ಪಾನಕ ಮತ್ತು ಕೋಸಂಬರಿ ಹಂಚಲಾಗುತ್ತದೆ."
    )
)

internal val skandaShashti = Festival(
    name = t("Skanda Shashti", hi = "स्कंद षष्ठी", kn = "ಸ್ಕಂದ ಷಷ್ಠಿ"),
    whenApprox = t("Oct–Nov (Karthika Shukla Shashti)", hi = "अक्तू॰–नव॰", kn = "ಅಕ್ಟೋಬರ್–ನವೆಂಬರ್"),
    usualMonth = 11
)

internal val makaraSankranti = Festival(
    name = t("Makara Sankranti", hi = "मकर संक्रांति", kn = "ಮಕರ ಸಂಕ್ರಾಂತಿ"),
    whenApprox = t("14–15 January", hi = "14–15 जनवरी", kn = "ಜನವರಿ 14–15"),
    usualMonth = 1
)

internal val vaikunthaEkadashi = Festival(
    name = t("Vaikuntha Ekadashi", hi = "वैकुंठ एकादशी", kn = "ವೈಕುಂಠ ಏಕಾದಶಿ"),
    whenApprox = t("Dec–Jan (Dhanurmasa)", hi = "दिस॰–जन॰", kn = "ಡಿಸೆಂಬರ್–ಜನವರಿ"),
    usualMonth = 12,
    note = t(
        "The Vaikuntha Dwara is opened before dawn and devotees pass through it once.",
        hi = "भोर से पहले वैकुंठ द्वार खोला जाता है और भक्त उससे होकर जाते हैं।",
        kn = "ಬೆಳಗಿನ ಜಾವ ವೈಕುಂಠ ದ್ವಾರ ತೆರೆಯಲಾಗುತ್ತದೆ, ಭಕ್ತರು ಅದರ ಮೂಲಕ ಹಾದುಹೋಗುತ್ತಾರೆ."
    )
)

// ── Nearby places that serve a whole part of the city ───────────────────────────────────

internal val krMarketFlowers = NearbyPlace(
    kind = NearbyKind.POOJA_ITEMS,
    name = "KR Market flower market",
    note = t(
        "The city's wholesale flower market — garlands, loose flowers, plantain stems and " +
            "coconuts at a fraction of temple-gate prices. Busiest before dawn.",
        hi = "शहर का थोक फूल बाज़ार — मालाएँ, फूल, केले के तने और नारियल, मंदिर के बाहर के दाम से बहुत सस्ते।",
        kn = "ನಗರದ ಸಗಟು ಹೂವಿನ ಮಾರುಕಟ್ಟೆ — ಹಾರ, ಬಿಡಿ ಹೂವು, ಬಾಳೆಕಂಬ ಮತ್ತು ತೆಂಗಿನಕಾಯಿ ಕಡಿಮೆ ದರದಲ್ಲಿ."
    ),
    location = GeoPoint(12.9629, 77.5772)
)

internal val gandhiBazaar = NearbyPlace(
    kind = NearbyKind.POOJA_ITEMS,
    name = "Gandhi Bazaar Main Road",
    note = t(
        "Flower, fruit and puja-kit stalls run the length of the street — camphor, kumkuma, " +
            "betel leaves and ready-made archana kits.",
        hi = "पूरी सड़क पर फूल, फल और पूजा-सामग्री की दुकानें — कपूर, कुमकुम, पान और तैयार अर्चना किट।",
        kn = "ಬೀದಿಯುದ್ದಕ್ಕೂ ಹೂವು, ಹಣ್ಣು ಮತ್ತು ಪೂಜಾ ಸಾಮಗ್ರಿ ಅಂಗಡಿಗಳು — ಕರ್ಪೂರ, ಕುಂಕುಮ, ವೀಳ್ಯದೆಲೆ."
    ),
    location = GeoPoint(12.9450, 77.5720)
)

internal val vidyarthiBhavan = NearbyPlace(
    kind = NearbyKind.EAT,
    name = "Vidyarthi Bhavan",
    note = t(
        "Gandhi Bazaar institution since 1943, famous for its crisp benne masala dosa. " +
            "Pure vegetarian; queues run long on weekend mornings.",
        hi = "1943 से गांधी बाज़ार की पहचान, कुरकुरे बेन्ने मसाला दोसे के लिए प्रसिद्ध। शुद्ध शाकाहारी।",
        kn = "1943ರಿಂದ ಗಾಂಧಿ ಬಜಾರಿನ ಹೆಸರಾಂತ ತಾಣ, ಬೆಣ್ಣೆ ಮಸಾಲ ದೋಸೆಗೆ ಪ್ರಸಿದ್ಧ. ಸಂಪೂರ್ಣ ಸಸ್ಯಾಹಾರಿ."
    ),
    location = GeoPoint(12.9445, 77.5726)
)

internal val mtr = NearbyPlace(
    kind = NearbyKind.EAT,
    name = "Mavalli Tiffin Rooms (MTR)",
    note = t(
        "Lalbagh Road landmark since 1924. Rava idli, and a full South Indian meal at lunch. " +
            "Pure vegetarian.",
        hi = "1924 से लालबाग रोड की पहचान। रवा इडली और दोपहर में दक्षिण भारतीय थाली। शुद्ध शाकाहारी।",
        kn = "1924ರಿಂದ ಲಾಲ್‌ಬಾಗ್ ರಸ್ತೆಯ ಹೆಸರಾಂತ ತಾಣ. ರವೆ ಇಡ್ಲಿ ಮತ್ತು ಮಧ್ಯಾಹ್ನ ಊಟ. ಸಂಪೂರ್ಣ ಸಸ್ಯಾಹಾರಿ."
    ),
    location = GeoPoint(12.9528, 77.5850)
)

internal val vvPuramFoodStreet = NearbyPlace(
    kind = NearbyKind.EAT,
    name = "VV Puram Food Street (Thindi Beedi)",
    note = t(
        "An evening street of vegetarian snack carts — holige, akki roti, congress bun, " +
            "and the city's best-known rabdi. Opens around 18:00.",
        hi = "शाम की शाकाहारी स्ट्रीट फ़ूड गली — होलिगे, अक्की रोटी, कांग्रेस बन और मशहूर रबड़ी। लगभग 18:00 से।",
        kn = "ಸಂಜೆಯ ಸಸ್ಯಾಹಾರಿ ತಿಂಡಿ ಬೀದಿ — ಹೋಳಿಗೆ, ಅಕ್ಕಿ ರೊಟ್ಟಿ, ಕಾಂಗ್ರೆಸ್ ಬನ್ ಮತ್ತು ರಬ್ಡಿ. ಸಂಜೆ 6 ರಿಂದ."
    ),
    location = GeoPoint(12.9498, 77.5750)
)

internal val ctrMalleshwaram = NearbyPlace(
    kind = NearbyKind.EAT,
    name = "CTR / Shri Sagar, Margosa Road",
    note = t(
        "Malleshwaram's benne masala dosa counter, a short walk from the temple cluster. " +
            "Pure vegetarian.",
        hi = "मल्लेश्वरम का प्रसिद्ध बेन्ने मसाला दोसा, मंदिर समूह से थोड़ी दूर। शुद्ध शाकाहारी।",
        kn = "ಮಲ್ಲೇಶ್ವರದ ಪ್ರಸಿದ್ಧ ಬೆಣ್ಣೆ ಮಸಾಲ ದೋಸೆ, ದೇವಸ್ಥಾನಗಳಿಂದ ನಡೆದು ಹೋಗುವ ದೂರ. ಸಸ್ಯಾಹಾರಿ."
    ),
    location = GeoPoint(12.9982, 77.5709)
)

internal val veenaStores = NearbyPlace(
    kind = NearbyKind.EAT,
    name = "Veena Stores, Malleshwaram",
    note = t(
        "Standing-room idli counter on 15th Cross. Cash, quick, and finished by mid-morning.",
        hi = "15वीं क्रॉस पर खड़े होकर खाने वाला इडली काउंटर। नकद, तेज़, और दोपहर से पहले ख़त्म।",
        kn = "15ನೇ ಕ್ರಾಸ್‌ನಲ್ಲಿ ನಿಂತು ತಿನ್ನುವ ಇಡ್ಲಿ ಅಂಗಡಿ. ನಗದು ಮಾತ್ರ, ಬೆಳಗ್ಗೆಯೇ ಮುಗಿಯುತ್ತದೆ."
    ),
    location = GeoPoint(12.9997, 77.5717)
)

internal val mallesharamSampigeRoad = NearbyPlace(
    kind = NearbyKind.POOJA_ITEMS,
    name = "Sampige Road & 8th Cross stalls",
    note = t(
        "Flower and puja-item stalls line the cross streets right outside the temple gates.",
        hi = "मंदिर के बाहर की गलियों में फूल और पूजा-सामग्री की दुकानें।",
        kn = "ದೇವಸ್ಥಾನದ ಹೊರಗಿನ ಅಡ್ಡ ರಸ್ತೆಗಳಲ್ಲಿ ಹೂವು ಮತ್ತು ಪೂಜಾ ಸಾಮಗ್ರಿ ಅಂಗಡಿಗಳು."
    ),
    location = GeoPoint(12.9995, 77.5705)
)

internal val avenueRoadStalls = NearbyPlace(
    kind = NearbyKind.POOJA_ITEMS,
    name = "Avenue Road puja stores",
    note = t(
        "Old-city wholesale row for lamps, brass vessels, incense and puja kits.",
        hi = "पुराने शहर की थोक गली — दीये, पीतल के बर्तन, अगरबत्ती और पूजा किट।",
        kn = "ಹಳೆಯ ಪೇಟೆಯ ಸಗಟು ಸಾಲು — ದೀಪ, ಹಿತ್ತಾಳೆ ಪಾತ್ರೆ, ಅಗರಬತ್ತಿ, ಪೂಜಾ ಕಿಟ್."
    ),
    location = GeoPoint(12.9686, 77.5779)
)

internal fun templeGateStalls(location: GeoPoint? = null) = NearbyPlace(
    kind = NearbyKind.POOJA_ITEMS,
    name = "Stalls at the temple gate",
    note = t(
        "Flowers, coconuts, camphor and archana tickets from the row of stalls outside.",
        hi = "बाहर की दुकानों से फूल, नारियल, कपूर और अर्चना टिकट।",
        kn = "ಹೊರಗಿನ ಅಂಗಡಿಗಳಲ್ಲಿ ಹೂವು, ತೆಂಗಿನಕಾಯಿ, ಕರ್ಪೂರ ಮತ್ತು ಅರ್ಚನೆ ಟಿಕೆಟ್."
    ),
    location = location
)

internal fun darshiniNearby(name: String, note: LocalizedText, location: GeoPoint? = null) =
    NearbyPlace(kind = NearbyKind.EAT, name = name, note = note, location = location)

internal val darshiniNote = t(
    "Neighbourhood darshini — idli, vada, dosa and filter coffee, pure vegetarian, open early.",
    hi = "मोहल्ले का दर्शिनी — इडली, वड़ा, दोसा और फ़िल्टर कॉफ़ी, शुद्ध शाकाहारी, सुबह जल्दी खुलता है।",
    kn = "ಬಡಾವಣೆಯ ದರ್ಶಿನಿ — ಇಡ್ಲಿ, ವಡೆ, ದೋಸೆ ಮತ್ತು ಫಿಲ್ಟರ್ ಕಾಫಿ, ಸಸ್ಯಾಹಾರಿ, ಬೆಳಗ್ಗೆ ಬೇಗ ತೆರೆಯುತ್ತದೆ."
)
