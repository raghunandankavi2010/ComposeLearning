package com.example.composelearning.temples.data

import java.time.DayOfWeek

/**
 * East and south-east — Halasuru, Domlur, Old Airport Road, Agara, Marathahalli, Kadugodi
 * and Anekal.
 *
 * The Chola foundations out here (Domlur, Agara, Kadugodi) predate Bengaluru itself; they
 * survive as small stone shrines with tech parks grown up around them.
 *
 * Several records in this file carry no coordinates. Where a reliable pin could not be
 * sourced, [Temple.location] is left null on purpose: the navigate action then hands the
 * maps app the temple's name and address to geocode, which is better than sending someone
 * to a plausible-looking but wrong point.
 */
internal val templesEast: List<Temple> = listOf(

    Temple(
        id = "halasuru-someshwara",
        name = t(
            "Halasuru Someshwara Temple",
            hi = "हलसूरु सोमेश्वर मंदिर",
            kn = "ಹಲಸೂರು ಸೋಮೇಶ್ವರ ದೇವಸ್ಥಾನ"
        ),
        deity = Deity.SHIVA,
        area = t("Halasuru (Ulsoor)", hi = "हलसूरु (उल्सूर)", kn = "ಹಲಸೂರು"),
        address = "Someshwara Temple Street, Halasuru, Bengaluru, Karnataka 560008",
        location = GeoPoint(12.97545, 77.62423),
        about = t(
            "Chola in origin and substantially extended under Hiriya Kempe Gowda II in the late " +
                "Vijayanagara period. The pillared mantapa, the carved gopura and the Tamil and " +
                "Kannada inscriptions make it the best surviving example of Vijayanagara temple " +
                "architecture inside the city.",
            hi = "मूलतः चोलकालीन, और विजयनगर काल के अंत में हिरिय केम्पे गौड़ा द्वितीय द्वारा विस्तारित। " +
                "स्तंभयुक्त मंटप, तराशा गोपुरम और तमिल-कन्नड़ शिलालेख इसे शहर का सर्वश्रेष्ठ विजयनगर मंदिर बनाते हैं।",
            kn = "ಮೂಲತಃ ಚೋಳರ ಕಾಲದ್ದು, ವಿಜಯನಗರ ಕಾಲದ ಕೊನೆಯಲ್ಲಿ ಹಿರಿಯ ಕೆಂಪೇಗೌಡ II ರಿಂದ ವಿಸ್ತರಣೆ. " +
                "ಕಂಬಗಳ ಮಂಟಪ, ಕೆತ್ತನೆಯ ಗೋಪುರ ಮತ್ತು ತಮಿಳು-ಕನ್ನಡ ಶಾಸನಗಳು ಇದನ್ನು ನಗರದ ಅತ್ಯುತ್ತಮ ವಿಜಯನಗರ ದೇವಾಲಯವಾಗಿಸಿವೆ."
        ),
        builtIn = "Chola period, extended 16th century",
        openings = listOf(window("06:00", "12:30"), window("17:30", "21:00")),
        rituals = listOf(
            morningAbhisheka("06:30"),
            middayMangalarati("12:00"),
            eveningAlankara("18:30"),
            nightMangalarati("20:30")
        ),
        festivals = listOf(mahaShivaratri, karthikaDeepotsava, ugadi, ganeshaChaturthi),
        bus = bus("Halasuru / Ulsoor Lake"),
        metro = MetroInfo("Halasuru", MetroLine.PURPLE, 8),
        parking = streetParking,
        facilities = setOf(
            Facility.SHOE_STAND,
            Facility.DRINKING_WATER,
            Facility.PRASAD_COUNTER,
            Facility.MARRIAGE_HALL,
            Facility.PHOTOGRAPHY_ALLOWED
        ),
        dressCode = traditionalDress,
        busiestDays = setOf(DayOfWeek.MONDAY),
        photos = listOf(
            commons(
                "Gopura of Someshwara temple (16th century) in Bengaluru.JPG",
                t("The 16th-century gopura", hi = "16वीं सदी का गोपुरम", kn = "16ನೇ ಶತಮಾನದ ಗೋಪುರ"),
                "Dineshkannambadi", "CC BY-SA 3.0"
            ),
            commons(
                "Ornate pillars in Someshwara temple at Bengaluru.JPG",
                t("Carved pillars in the mantapa", hi = "मंटप के तराशे स्तंभ", kn = "ಮಂಟಪದ ಕೆತ್ತನೆಯ ಕಂಬಗಳು"),
                "Dineshkannambadi", "CC BY-SA 3.0"
            ),
            commons(
                "Someshwara1.jpg",
                t("The temple", hi = "मंदिर", kn = "ದೇವಾಲಯ"),
                "Vineeta190", "CC0"
            )
        ),
        nearby = listOf(
            NearbyPlace(
                kind = NearbyKind.POOJA_ITEMS,
                name = "Halasuru market stalls",
                note = t(
                    "Flowers, coconuts and puja items from the market street beside the temple.",
                    hi = "मंदिर के पास बाज़ार की गली में फूल, नारियल और पूजा सामग्री।",
                    kn = "ದೇವಾಲಯದ ಪಕ್ಕದ ಮಾರುಕಟ್ಟೆ ಬೀದಿಯಲ್ಲಿ ಹೂವು, ತೆಂಗಿನಕಾಯಿ ಮತ್ತು ಪೂಜಾ ಸಾಮಗ್ರಿ."
                ),
                location = GeoPoint(12.9757, 77.6248)
            ),
            darshiniNearby("Halasuru darshinis", darshiniNote, GeoPoint(12.9765, 77.6255))
        ),
        donation = listOf(hundiDonation, sevaCounterDonation, muzraiTrust),
        accent = ShivaAccent,
        confidence = DataConfidence.HIGH
    ),

    Temple(
        id = "subrahmanya-halasuru",
        name = t(
            "Sri Subrahmanya Temple, Halasuru",
            hi = "श्री सुब्रह्मण्य मंदिर, हलसूरु",
            kn = "ಶ್ರೀ ಸುಬ್ರಹ್ಮಣ್ಯ ದೇವಸ್ಥಾನ, ಹಲಸೂರು"
        ),
        deity = Deity.SUBRAMANYA,
        area = t("Halasuru (Ulsoor)", hi = "हलसूरु (उल्सूर)", kn = "ಹಲಸೂರು"),
        address = "Near Halasuru metro station, Halasuru, Bengaluru, Karnataka 560008",
        location = null,
        about = t(
            "Reputed to be over 350 years old and possibly older than the Someshwara temple " +
                "next door. The deity is called Ananda Murugan here, modelled on the Murugan of " +
                "Thiruthani, with Valli and Devasena in separate shrines on either side.",
            hi = "350 वर्ष से अधिक पुराना माना जाता है, संभवतः पड़ोस के सोमेश्वर मंदिर से भी पुराना। " +
                "यहाँ देवता को आनंद मुरुगन कहा जाता है; दोनों ओर वल्ली और देवसेना के अलग मंदिर हैं।",
            kn = "350 ವರ್ಷಕ್ಕೂ ಹಳೆಯದೆಂದು ಹೇಳಲಾಗುತ್ತದೆ, ಪಕ್ಕದ ಸೋಮೇಶ್ವರ ದೇವಾಲಯಕ್ಕಿಂತಲೂ ಹಳೆಯದಿರಬಹುದು. " +
                "ಇಲ್ಲಿ ದೇವರನ್ನು ಆನಂದ ಮುರುಗನ್ ಎನ್ನುತ್ತಾರೆ; ಎರಡೂ ಬದಿ ವಳ್ಳಿ ಮತ್ತು ದೇವಸೇನಾ ಸನ್ನಿಧಿಗಳಿವೆ."
        ),
        openings = listOf(window("06:30", "12:00"), window("17:30", "20:30")),
        rituals = listOf(morningAbhisheka("07:00"), eveningAlankara("18:30")),
        festivals = listOf(skandaShashti, karthikaDeepotsava),
        bus = bus("Halasuru / Ulsoor Lake"),
        metro = MetroInfo("Halasuru", MetroLine.PURPLE, 6),
        parking = streetParking,
        facilities = setOf(Facility.SHOE_STAND, Facility.DRINKING_WATER),
        dressCode = traditionalDress,
        busiestDays = setOf(DayOfWeek.TUESDAY),
        nearby = listOf(darshiniNearby("Halasuru darshinis", darshiniNote, GeoPoint(12.9765, 77.6255))),
        donation = listOf(hundiDonation, sevaCounterDonation),
        accent = SubramanyaAccent,
        confidence = DataConfidence.MEDIUM
    ),

    Temple(
        id = "domlur-chokkanathaswamy",
        name = t(
            "Chokkanathaswamy Temple, Domlur",
            hi = "चोक्कनाथस्वामी मंदिर, डोम्लूर",
            kn = "ಚೊಕ್ಕನಾಥಸ್ವಾಮಿ ದೇವಸ್ಥಾನ, ದೊಮ್ಮಲೂರು"
        ),
        deity = Deity.VISHNU,
        area = t("Domlur", hi = "डोम्लूर", kn = "ದೊಮ್ಮಲೂರು"),
        address = "Chokkanathaswamy Temple Street, Domlur, Bengaluru, Karnataka 560071",
        location = null,
        about = t(
            "Dedicated to Chokkanathaswamy — Chokka Perumal, a form of Vishnu — and among the " +
                "very oldest temples in the city. Kannada and Tamil inscriptions recorded in " +
                "Epigraphia Carnatica date it to at least 1200 CE, four centuries before " +
                "Bengaluru was founded.",
            hi = "चोक्कनाथस्वामी — विष्णु के रूप चोक्क पेरुमाल — को समर्पित, शहर के सबसे प्राचीन मंदिरों में से। " +
                "एपिग्राफ़िया कार्नेटिका में दर्ज कन्नड़ और तमिल शिलालेख इसे कम से कम 1200 ई. का बताते हैं।",
            kn = "ಚೊಕ್ಕನಾಥಸ್ವಾಮಿ — ವಿಷ್ಣುವಿನ ರೂಪ ಚೊಕ್ಕ ಪೆರುಮಾಳ್ — ಗೆ ಸಮರ್ಪಿತ, ನಗರದ ಅತ್ಯಂತ ಪುರಾತನ " +
                "ದೇವಾಲಯಗಳಲ್ಲಿ ಒಂದು. ಶಾಸನಗಳ ಪ್ರಕಾರ ಕನಿಷ್ಠ ಕ್ರಿ.ಶ. 1200ರಷ್ಟು ಹಳೆಯದು."
        ),
        builtIn = "at least 1200 CE",
        openings = listOf(window("07:00", "11:30"), window("18:00", "20:30")),
        rituals = listOf(morningAbhisheka("08:00"), eveningAlankara("18:30")),
        festivals = listOf(vaikunthaEkadashi, ramaNavami, janmashtami),
        bus = bus("Domlur / Domlur Depot"),
        metro = MetroInfo("Indiranagar", MetroLine.PURPLE, 25),
        parking = streetParking,
        facilities = setOf(Facility.SHOE_STAND, Facility.DRINKING_WATER),
        dressCode = traditionalDress,
        nearby = listOf(
            templeGateStalls(),
            darshiniNearby("Domlur and Indiranagar darshinis", darshiniNote)
        ),
        donation = listOf(hundiDonation, muzraiTrust),
        accent = VishnuAccent,
        confidence = DataConfidence.MEDIUM
    ),

    Temple(
        id = "shivoham-shiva",
        name = t(
            "Shivoham Shiva Temple",
            hi = "शिवोहम शिव मंदिर",
            kn = "ಶಿವೋಹಂ ಶಿವ ದೇವಸ್ಥಾನ"
        ),
        deity = Deity.SHIVA,
        area = t("Old Airport Road", hi = "ओल्ड एयरपोर्ट रोड", kn = "ಹಳೆಯ ವಿಮಾನ ನಿಲ್ದಾಣ ರಸ್ತೆ"),
        address = "Old Airport Road, Kemp Fort, Bengaluru, Karnataka 560017",
        location = null,
        about = t(
            "Built in 1995 around a 65-foot seated Shiva in the open air, with a walk-through " +
                "replica of the twelve jyotirlingas beneath it. Around half a million people " +
                "come each year and up to 150,000 on Maha Shivaratri alone.",
            hi = "1995 में बना, खुले में 65 फुट ऊँची शिव प्रतिमा और उसके नीचे बारह ज्योतिर्लिंगों की " +
                "प्रतिकृतियों से होकर जाने वाला मार्ग। हर साल लगभग पाँच लाख लोग आते हैं।",
            kn = "1995ರಲ್ಲಿ ನಿರ್ಮಿತ, ಬಯಲಿನಲ್ಲಿ 65 ಅಡಿ ಎತ್ತರದ ಶಿವ ಮೂರ್ತಿ ಮತ್ತು ಕೆಳಗೆ ಹನ್ನೆರಡು " +
                "ಜ್ಯೋತಿರ್ಲಿಂಗಗಳ ಪ್ರತಿಕೃತಿಗಳ ಮಾರ್ಗ. ವರ್ಷಕ್ಕೆ ಸುಮಾರು ಐದು ಲಕ್ಷ ಜನ ಭೇಟಿ ನೀಡುತ್ತಾರೆ."
        ),
        builtIn = "1995",
        openings = listOf(window("07:00", "20:00")),
        rituals = listOf(morningAbhisheka("08:00"), eveningAlankara("18:30"), archanaSeva(100)),
        festivals = listOf(
            mahaShivaratri.copy(
                note = t(
                    "Up to 150,000 visitors in a day. The queue is managed in barricaded loops " +
                        "and can take hours — come early or very late.",
                    hi = "एक ही दिन में 1.5 लाख तक श्रद्धालु। कतार घंटों लंबी हो सकती है — जल्दी या देर से आएँ।",
                    kn = "ಒಂದೇ ದಿನದಲ್ಲಿ 1.5 ಲಕ್ಷದವರೆಗೆ ಭಕ್ತರು. ಸರತಿ ಗಂಟೆಗಟ್ಟಲೆ — ಬೇಗ ಅಥವಾ ತಡವಾಗಿ ಬನ್ನಿ."
                )
            ),
            karthikaDeepotsava
        ),
        bus = bus("Kemp Fort / Old Airport Road"),
        parking = ownParking,
        facilities = setOf(
            Facility.SHOE_STAND,
            Facility.DRINKING_WATER,
            Facility.RESTROOMS,
            Facility.PRASAD_COUNTER,
            Facility.PAID_PARKING,
            Facility.QUEUE_SHELTER,
            Facility.WHEELCHAIR,
            Facility.PHOTOGRAPHY_ALLOWED
        ),
        dressCode = traditionalDress,
        busiestDays = setOf(DayOfWeek.MONDAY, DayOfWeek.SUNDAY),
        photos = listOf(
            commons(
                "Bangalore Shiva.jpg",
                t("The 65-foot Shiva", hi = "65 फुट ऊँची शिव प्रतिमा", kn = "65 ಅಡಿ ಎತ್ತರದ ಶಿವ ಮೂರ್ತಿ"),
                "Indianhilbilly", "CC BY-SA 3.0"
            )
        ),
        nearby = listOf(
            templeGateStalls(),
            darshiniNearby("Old Airport Road eateries", darshiniNote)
        ),
        donation = listOf(hundiDonation, sevaCounterDonation),
        accent = ShivaAccent,
        confidence = DataConfidence.MEDIUM
    ),

    Temple(
        id = "jagannath-agara",
        name = t(
            "Jagannath Temple, Agara",
            hi = "जगन्नाथ मंदिर, अगरा",
            kn = "ಜಗನ್ನಾಥ ದೇವಸ್ಥಾನ, ಅಗರ"
        ),
        deity = Deity.VISHNU,
        area = t("Agara, Sarjapur Road", hi = "अगरा, सरजापुर रोड", kn = "ಅಗರ, ಸರ್ಜಾಪುರ ರಸ್ತೆ"),
        address = "Sarjapur Road, Agara, Bengaluru, Karnataka 560102",
        location = null,
        about = t(
            "Jagannath with Balabhadra and Subhadra, maintained by the Shree Jagannath Temple " +
                "Trust of Bangalore. Its Rath Yatra draws more than fifteen thousand people and " +
                "is the largest Odia festival in the city.",
            hi = "बलभद्र और सुभद्रा के साथ जगन्नाथ, श्री जगन्नाथ मंदिर ट्रस्ट बैंगलोर द्वारा संचालित। " +
                "यहाँ की रथयात्रा में पंद्रह हज़ार से अधिक लोग शामिल होते हैं।",
            kn = "ಬಲಭದ್ರ ಮತ್ತು ಸುಭದ್ರೆಯೊಂದಿಗೆ ಜಗನ್ನಾಥ, ಶ್ರೀ ಜಗನ್ನಾಥ ದೇವಾಲಯ ಟ್ರಸ್ಟ್ ನಿರ್ವಹಣೆ. " +
                "ಇಲ್ಲಿನ ರಥಯಾತ್ರೆಗೆ ಹದಿನೈದು ಸಾವಿರಕ್ಕೂ ಹೆಚ್ಚು ಜನ ಸೇರುತ್ತಾರೆ."
        ),
        openings = listOf(window("06:30", "12:00"), window("17:00", "20:30")),
        rituals = listOf(morningAbhisheka("07:00"), middayMangalarati("11:30"), eveningAlankara("18:30")),
        festivals = listOf(
            Festival(
                name = t("Rath Yatra", hi = "रथ यात्रा", kn = "ರಥ ಯಾತ್ರೆ"),
                whenApprox = t("Jun–Jul (Ashadha Shukla Dwitiya)", hi = "जून–जुलाई", kn = "ಜೂನ್–ಜುಲೈ"),
                usualMonth = 7,
                note = t(
                    "The chariots are pulled along Sarjapur Road; the stretch is closed to " +
                        "traffic for the afternoon.",
                    hi = "रथ सरजापुर रोड पर खींचे जाते हैं; दोपहर में वह हिस्सा यातायात के लिए बंद रहता है।",
                    kn = "ರಥಗಳನ್ನು ಸರ್ಜಾಪುರ ರಸ್ತೆಯಲ್ಲಿ ಎಳೆಯಲಾಗುತ್ತದೆ; ಮಧ್ಯಾಹ್ನ ಆ ಭಾಗ ಸಂಚಾರಕ್ಕೆ ಬಂದ್."
                )
            ),
            janmashtami,
            vaikunthaEkadashi
        ),
        bus = bus("Agara / Sarjapur Road"),
        parking = ownParking,
        facilities = setOf(
            Facility.SHOE_STAND,
            Facility.DRINKING_WATER,
            Facility.PRASAD_COUNTER,
            Facility.ANNADANA,
            Facility.FREE_PARKING
        ),
        dressCode = traditionalDress,
        busiestDays = setOf(DayOfWeek.SUNDAY),
        photos = listOf(
            commons(
                "Puri Jagannath Temple at Agara.JPG",
                t("The temple at Agara", hi = "अगरा में मंदिर", kn = "ಅಗರದಲ್ಲಿ ದೇವಾಲಯ"),
                "Veera.sj", "CC0"
            )
        ),
        nearby = listOf(templeGateStalls(), darshiniNearby("Sarjapur Road eateries", darshiniNote)),
        donation = listOf(hundiDonation, annadanaDonation, sevaCounterDonation),
        accent = VishnuAccent,
        confidence = DataConfidence.MEDIUM
    ),

    Temple(
        id = "someshwara-agara",
        name = t(
            "Someshwara Temple, Agara",
            hi = "सोमेश्वर मंदिर, अगरा",
            kn = "ಸೋಮೇಶ್ವರ ದೇವಸ್ಥಾನ, ಅಗರ"
        ),
        deity = Deity.SHIVA,
        area = t("Agara, Sarjapur Road", hi = "अगरा, सरजापुर रोड", kn = "ಅಗರ, ಸರ್ಜಾಪುರ ರಸ್ತೆ"),
        address = "Sarjapur Main Road, Agara, Bengaluru, Karnataka 560102",
        location = null,
        about = t(
            "A Chola-period Shiva temple with a history put at around 1,200 years, now " +
                "surrounded by the office blocks of Sarjapur Road.",
            hi = "चोलकालीन शिव मंदिर, जिसका इतिहास लगभग 1,200 वर्ष पुराना बताया जाता है; अब चारों ओर " +
                "सरजापुर रोड के दफ़्तर हैं।",
            kn = "ಚೋಳರ ಕಾಲದ ಶಿವ ದೇವಾಲಯ, ಸುಮಾರು 1,200 ವರ್ಷಗಳ ಇತಿಹಾಸ; ಈಗ ಸುತ್ತಲೂ ಸರ್ಜಾಪುರ ರಸ್ತೆಯ ಕಚೇರಿಗಳು."
        ),
        builtIn = "Chola period",
        openings = listOf(window("07:00", "11:30"), window("18:00", "20:00")),
        rituals = listOf(morningAbhisheka("08:00"), eveningAlankara("18:30")),
        festivals = listOf(mahaShivaratri, karthikaDeepotsava),
        bus = bus("Agara / Sarjapur Road"),
        parking = streetParking,
        facilities = setOf(Facility.SHOE_STAND, Facility.DRINKING_WATER),
        dressCode = traditionalDress,
        busiestDays = setOf(DayOfWeek.MONDAY),
        nearby = listOf(templeGateStalls(), darshiniNearby("Sarjapur Road eateries", darshiniNote)),
        donation = listOf(hundiDonation, muzraiTrust),
        accent = ShivaAccent,
        confidence = DataConfidence.LOW
    ),

    Temple(
        id = "someshwara-marathahalli",
        name = t(
            "Someshwara Temple, Marathahalli",
            hi = "सोमेश्वर मंदिर, मराठहल्ली",
            kn = "ಸೋಮೇಶ್ವರ ದೇವಸ್ಥಾನ, ಮಾರತಹಳ್ಳಿ"
        ),
        deity = Deity.SHIVA,
        area = t("Marathahalli", hi = "मराठहल्ली", kn = "ಮಾರತಹಳ್ಳಿ"),
        address = "Marathahalli, Bengaluru, Karnataka 560037",
        location = null,
        about = t(
            "The old village Shiva temple of Marathahalli, still the focus of the local " +
                "Shivaratri and Karthika observances even as the neighbourhood around it has " +
                "turned into an outer-ring-road suburb.",
            hi = "मराठहल्ली का पुराना ग्रामीण शिव मंदिर; आसपास का इलाक़ा भले बदल गया हो, शिवरात्रि और " +
                "कार्तिक के आयोजन आज भी यहीं केंद्रित हैं।",
            kn = "ಮಾರತಹಳ್ಳಿಯ ಹಳೆಯ ಗ್ರಾಮ ಶಿವ ದೇವಾಲಯ; ಸುತ್ತಲಿನ ಬಡಾವಣೆ ಬದಲಾದರೂ ಶಿವರಾತ್ರಿ ಮತ್ತು " +
                "ಕಾರ್ತಿಕ ಆಚರಣೆಗಳು ಇಂದಿಗೂ ಇಲ್ಲಿಯೇ."
        ),
        openings = listOf(window("07:00", "11:30"), window("18:00", "20:00")),
        rituals = listOf(morningAbhisheka("08:00"), eveningAlankara("18:30")),
        festivals = listOf(mahaShivaratri, karthikaDeepotsava),
        bus = bus("Marathahalli Bridge"),
        parking = streetParking,
        facilities = setOf(Facility.SHOE_STAND, Facility.DRINKING_WATER),
        dressCode = traditionalDress,
        busiestDays = setOf(DayOfWeek.MONDAY),
        nearby = listOf(templeGateStalls(), darshiniNearby("Marathahalli darshinis", darshiniNote)),
        donation = listOf(hundiDonation),
        accent = ShivaAccent,
        confidence = DataConfidence.LOW
    ),

    Temple(
        id = "kashi-vishweshwara-kadugodi",
        name = t(
            "Kashi Vishweshwara Temple, Kadugodi",
            hi = "काशी विश्वेश्वर मंदिर, कडुगोडी",
            kn = "ಕಾಶಿ ವಿಶ್ವೇಶ್ವರ ದೇವಸ್ಥಾನ, ಕಾಡುಗೋಡಿ"
        ),
        deity = Deity.SHIVA,
        area = t("Kadugodi, Whitefield", hi = "कडुगोडी, व्हाइटफ़ील्ड", kn = "ಕಾಡುಗೋಡಿ, ವೈಟ್‌ಫೀಲ್ಡ್"),
        address = "Kadugodi, Whitefield, Bengaluru, Karnataka 560067",
        location = null,
        about = t(
            "A Rajendra Chola-period temple that gave Kadugodi its name — 'kadu' forest, " +
                "'gudi' temple. Its architecture still shows Ganga-dynasty features, and " +
                "inscriptions on the basement name the deity as Rajadhiraja Bhangisvara. The " +
                "original linga was stolen and replaced with one brought from Varanasi.",
            hi = "राजेंद्र चोल काल का मंदिर, जिससे कडुगोडी को नाम मिला — 'काडु' वन, 'गुड़ी' मंदिर। " +
                "स्थापत्य में गंग वंश के लक्षण हैं। मूल शिवलिंग चोरी हो गया था और वाराणसी से लाया गया लिंग स्थापित हुआ।",
            kn = "ರಾಜೇಂದ್ರ ಚೋಳನ ಕಾಲದ ದೇವಾಲಯ; ಇದರಿಂದಲೇ ಕಾಡುಗೋಡಿಗೆ ಹೆಸರು — 'ಕಾಡು' ಮತ್ತು 'ಗುಡಿ'. " +
                "ವಾಸ್ತುಶಿಲ್ಪದಲ್ಲಿ ಗಂಗ ವಂಶದ ಲಕ್ಷಣಗಳಿವೆ. ಮೂಲ ಲಿಂಗ ಕಳವಾಗಿ ವಾರಾಣಸಿಯಿಂದ ತಂದ ಲಿಂಗ ಪ್ರತಿಷ್ಠಾಪಿಸಲಾಗಿದೆ."
        ),
        builtIn = "Rajendra Chola period",
        openings = listOf(window("06:30", "11:30"), window("18:00", "20:30")),
        rituals = listOf(morningAbhisheka("07:30"), eveningAlankara("18:30")),
        festivals = listOf(mahaShivaratri, karthikaDeepotsava, ugadi),
        bus = bus("Kadugodi Bus Stand / Whitefield"),
        metro = MetroInfo("Kadugodi Tree Park", MetroLine.PURPLE, 10),
        parking = streetParking,
        facilities = setOf(Facility.SHOE_STAND, Facility.DRINKING_WATER),
        dressCode = traditionalDress,
        busiestDays = setOf(DayOfWeek.MONDAY),
        photos = listOf(
            commons(
                "Temple-kadugudi.png",
                t("The temple at Kadugodi", hi = "कडुगोडी में मंदिर", kn = "ಕಾಡುಗೋಡಿಯ ದೇವಾಲಯ"),
                "Pprasanaa", "CC BY-SA 4.0"
            )
        ),
        nearby = listOf(templeGateStalls(), darshiniNearby("Whitefield darshinis", darshiniNote)),
        donation = listOf(hundiDonation, muzraiTrust),
        accent = ShivaAccent,
        confidence = DataConfidence.MEDIUM
    ),

    Temple(
        id = "madduramma-huskur",
        name = t(
            "Sri Madduramma Temple, Huskur",
            hi = "श्री मद्दुरम्मा मंदिर, हुस्कूर",
            kn = "ಶ್ರೀ ಮದ್ದೂರಮ್ಮ ದೇವಸ್ಥಾನ, ಹುಸ್ಕೂರು"
        ),
        deity = Deity.DEVI,
        area = t("Huskur, Anekal", hi = "हुस्कूर, अनेकल", kn = "ಹುಸ್ಕೂರು, ಆನೇಕಲ್"),
        address = "Huskur, Anekal Taluk, Bengaluru, Karnataka 562106",
        location = GeoPoint(12.86122, 77.70522),
        about = t(
            "A Chola-period village goddess temple on the Anekal side of the city. Its annual " +
                "jatre in March or April is the biggest event for miles around and pulls in the " +
                "surrounding villages for several days.",
            hi = "शहर के अनेकल छोर पर चोलकालीन ग्राम देवी मंदिर। मार्च–अप्रैल की वार्षिक जात्रा " +
                "आसपास के गाँवों का सबसे बड़ा आयोजन है।",
            kn = "ನಗರದ ಆನೇಕಲ್ ಭಾಗದಲ್ಲಿ ಚೋಳರ ಕಾಲದ ಗ್ರಾಮದೇವತೆ ದೇವಾಲಯ. ಮಾರ್ಚ್–ಏಪ್ರಿಲ್‌ನ ವಾರ್ಷಿಕ ಜಾತ್ರೆ " +
                "ಸುತ್ತಲಿನ ಹಳ್ಳಿಗಳ ದೊಡ್ಡ ಹಬ್ಬ."
        ),
        builtIn = "Chola period",
        openings = listOf(window("07:00", "11:30"), window("17:30", "20:00")),
        rituals = listOf(morningAbhisheka("08:00"), eveningAlankara("18:30")),
        festivals = listOf(
            Festival(
                name = t("Madduramma Jatre", hi = "मद्दुरम्मा जात्रा", kn = "ಮದ್ದೂರಮ್ಮ ಜಾತ್ರೆ"),
                whenApprox = t("Mar–Apr", hi = "मार्च–अप्रैल", kn = "ಮಾರ್ಚ್–ಏಪ್ರಿಲ್"),
                usualMonth = 4
            ),
            navaratri,
            ugadi
        ),
        bus = bus("Huskur Gate / Anekal Road"),
        parking = streetParking,
        facilities = setOf(Facility.SHOE_STAND, Facility.DRINKING_WATER, Facility.ANNADANA),
        dressCode = traditionalDress,
        busiestDays = setOf(DayOfWeek.TUESDAY, DayOfWeek.FRIDAY),
        nearby = listOf(templeGateStalls(GeoPoint(12.8613, 77.7054))),
        donation = listOf(hundiDonation, annadanaDonation, muzraiTrust),
        accent = DeviAccent,
        confidence = DataConfidence.MEDIUM
    )
)
