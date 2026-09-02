package com.example.composelearning.temples.data

import java.time.DayOfWeek

/**
 * North and west — Malleshwaram, Rajajinagar, Hebbal, Mysore Road, Rajarajeshwari Nagar,
 * Omkar Hills and Kengeri.
 *
 * Malleshwaram alone is worth a morning: Kadu Malleshwara, the Gangamma temple and the
 * rediscovered Nandi Tirtha stand within a few hundred metres of each other on 2nd Temple
 * Street, with CTR and Veena Stores in between.
 */
internal val templesNorthWest: List<Temple> = listOf(

    Temple(
        id = "kadu-malleshwara",
        name = t(
            "Kadu Malleshwara Temple",
            hi = "काडु मल्लेश्वर मंदिर",
            kn = "ಕಾಡು ಮಲ್ಲೇಶ್ವರ ದೇವಸ್ಥಾನ"
        ),
        deity = Deity.SHIVA,
        area = t("Malleshwaram", hi = "मल्लेश्वरम", kn = "ಮಲ್ಲೇಶ್ವರಂ"),
        address = "2nd Temple Street, Malleshwaram, Bengaluru, Karnataka 560003",
        location = GeoPoint(13.00440, 77.57000),
        about = t(
            "A 17th-century Shiva temple, and the reason the neighbourhood is called " +
                "Malleshwaram. 'Kadu' means forest — the temple stood in thick greenery when " +
                "Ekoji, brother of Shivaji, is said to have endowed it.",
            hi = "17वीं सदी का शिव मंदिर, और इसी के नाम पर पूरा इलाक़ा मल्लेश्वरम कहलाता है। " +
                "'काडु' का अर्थ है वन — तब यह घने पेड़ों के बीच था।",
            kn = "17ನೇ ಶತಮಾನದ ಶಿವ ದೇವಾಲಯ; ಇದೇ ಕಾರಣಕ್ಕೆ ಬಡಾವಣೆಗೆ ಮಲ್ಲೇಶ್ವರಂ ಎಂಬ ಹೆಸರು. " +
                "'ಕಾಡು' ಎಂದರೆ ಅರಣ್ಯ — ಆಗ ಇದು ದಟ್ಟ ಹಸಿರಿನ ನಡುವೆ ಇತ್ತು."
        ),
        builtIn = "17th century",
        openings = listOf(window("06:00", "12:30"), window("17:00", "20:30")),
        rituals = listOf(morningAbhisheka("06:30"), middayMangalarati("12:00"), eveningAlankara("18:30")),
        festivals = listOf(mahaShivaratri, karthikaDeepotsava, ugadi),
        bus = bus("Malleshwaram 8th Cross / Mantri Square"),
        metro = MetroInfo("Mantri Square Sampige Road", MetroLine.GREEN, 10),
        parking = streetParking,
        facilities = setOf(
            Facility.SHOE_STAND,
            Facility.DRINKING_WATER,
            Facility.PRASAD_COUNTER,
            Facility.QUEUE_SHELTER
        ),
        dressCode = traditionalDress,
        busiestDays = setOf(DayOfWeek.MONDAY),
        photos = listOf(
            commons(
                "Kadu Malleshwara Temple.jpg",
                t("The temple", hi = "मंदिर", kn = "ದೇವಾಲಯ"),
                "Mythic Society Inscriptions 3D Scanning Project", "CC BY-SA 4.0"
            )
        ),
        nearby = listOf(mallesharamSampigeRoad, ctrMalleshwaram, veenaStores),
        donation = listOf(hundiDonation, sevaCounterDonation, muzraiTrust),
        accent = ShivaAccent,
        confidence = DataConfidence.MEDIUM
    ),

    Temple(
        id = "nandi-tirtha",
        name = t(
            "Sri Dakshinamukha Nandi Tirtha Kalyani Kshetra",
            hi = "श्री दक्षिणमुख नंदी तीर्थ कल्याणी क्षेत्र",
            kn = "ಶ್ರೀ ದಕ್ಷಿಣಮುಖ ನಂದಿ ತೀರ್ಥ ಕಲ್ಯಾಣಿ ಕ್ಷೇತ್ರ"
        ),
        deity = Deity.SHIVA,
        area = t("Malleshwaram", hi = "मल्लेश्वरम", kn = "ಮಲ್ಲೇಶ್ವರಂ"),
        address = "2nd Temple Street, Malleshwaram, Bengaluru, Karnataka 560003",
        location = GeoPoint(13.00505, 77.57316),
        about = t(
            "Buried and forgotten for some four hundred years, this small temple was " +
                "rediscovered in 1997 when a plot was being cleared. Water flows continuously " +
                "from the mouth of a south-facing Nandi onto the linga below and collects in a " +
                "stepped kalyani — the source is still not fully explained.",
            hi = "लगभग चार सौ वर्ष दबा रहा यह छोटा मंदिर 1997 में ज़मीन साफ़ करते समय फिर मिला। " +
                "दक्षिणमुखी नंदी के मुख से लगातार जल शिवलिंग पर गिरता है और सीढ़ीदार कल्याणी में जमा होता है।",
            kn = "ಸುಮಾರು ನಾನ್ನೂರು ವರ್ಷ ಮಣ್ಣಿನಡಿ ಮರೆಯಾಗಿದ್ದ ಈ ಸಣ್ಣ ದೇವಾಲಯ 1997ರಲ್ಲಿ ಮತ್ತೆ ಪತ್ತೆಯಾಯಿತು. " +
                "ದಕ್ಷಿಣಮುಖ ನಂದಿಯ ಬಾಯಿಂದ ನಿರಂತರ ನೀರು ಲಿಂಗದ ಮೇಲೆ ಬಿದ್ದು ಕಲ್ಯಾಣಿಯಲ್ಲಿ ಸಂಗ್ರಹವಾಗುತ್ತದೆ."
        ),
        builtIn = "Rediscovered 1997",
        openings = listOf(window("06:30", "12:00"), window("17:30", "20:00")),
        rituals = listOf(morningAbhisheka("07:30"), eveningAlankara("18:30")),
        festivals = listOf(mahaShivaratri, karthikaDeepotsava),
        bus = bus("Malleshwaram 8th Cross / Mantri Square"),
        metro = MetroInfo("Mantri Square Sampige Road", MetroLine.GREEN, 12),
        parking = streetParking,
        facilities = setOf(Facility.SHOE_STAND, Facility.DRINKING_WATER, Facility.PHOTOGRAPHY_ALLOWED),
        dressCode = traditionalDress,
        photos = listOf(
            commons(
                "Nandi-Tirtha-Temple-Malleswaram-Bangalore (1).jpg",
                t("The kalyani and the Nandi", hi = "कल्याणी और नंदी", kn = "ಕಲ್ಯಾಣಿ ಮತ್ತು ನಂದಿ"),
                "Masterzatak", "CC BY-SA 4.0"
            )
        ),
        nearby = listOf(mallesharamSampigeRoad, ctrMalleshwaram, veenaStores),
        donation = listOf(hundiDonation),
        accent = ShivaAccent,
        confidence = DataConfidence.HIGH
    ),

    Temple(
        id = "gangamma-devi",
        name = t(
            "Sri Gangamma Devi Temple",
            hi = "श्री गंगम्मा देवी मंदिर",
            kn = "ಶ್ರೀ ಗಂಗಮ್ಮ ದೇವಿ ದೇವಸ್ಥಾನ"
        ),
        deity = Deity.DEVI,
        area = t("Malleshwaram", hi = "मल्लेश्वरम", kn = "ಮಲ್ಲೇಶ್ವರಂ"),
        address = "2nd Temple Street, Malleshwaram, Bengaluru, Karnataka 560003",
        location = GeoPoint(13.00445, 77.56950),
        about = t(
            "The Gangamma shrine that faces the Nandi Tirtha, a few doors from Kadu " +
                "Malleshwara. The goddess here is Ganga, and the three temples are usually done " +
                "in one walk.",
            hi = "नंदी तीर्थ के सामने और काडु मल्लेश्वर से कुछ ही दूर गंगम्मा का मंदिर। यहाँ देवी गंगा हैं; " +
                "तीनों मंदिर एक ही पैदल चक्कर में देखे जाते हैं।",
            kn = "ನಂದಿ ತೀರ್ಥದ ಎದುರು, ಕಾಡು ಮಲ್ಲೇಶ್ವರದಿಂದ ಕೆಲವೇ ಹೆಜ್ಜೆ ದೂರದಲ್ಲಿ ಗಂಗಮ್ಮನ ದೇವಾಲಯ. " +
                "ಮೂರೂ ದೇವಾಲಯಗಳನ್ನು ಒಂದೇ ನಡಿಗೆಯಲ್ಲಿ ನೋಡಬಹುದು."
        ),
        openings = listOf(window("06:30", "12:00"), window("17:30", "20:00")),
        rituals = listOf(morningAbhisheka("07:30"), eveningAlankara("18:30")),
        festivals = listOf(navaratri, ugadi),
        bus = bus("Malleshwaram 8th Cross / Mantri Square"),
        metro = MetroInfo("Mantri Square Sampige Road", MetroLine.GREEN, 12),
        parking = streetParking,
        facilities = setOf(Facility.SHOE_STAND, Facility.DRINKING_WATER),
        dressCode = traditionalDress,
        busiestDays = setOf(DayOfWeek.FRIDAY),
        photos = listOf(
            commons(
                "Gangamma-devi-temple.jpg",
                t("The temple on 2nd Temple Street", hi = "2nd टेंपल स्ट्रीट पर मंदिर", kn = "2ನೇ ಟೆಂಪಲ್ ಸ್ಟ್ರೀಟ್‌ನಲ್ಲಿ ದೇವಾಲಯ"),
                "Masterzatak", "CC BY-SA 4.0"
            )
        ),
        nearby = listOf(mallesharamSampigeRoad, ctrMalleshwaram, veenaStores),
        donation = listOf(hundiDonation),
        accent = DeviAccent,
        confidence = DataConfidence.MEDIUM
    ),

    Temple(
        id = "iskcon-bangalore",
        name = t(
            "ISKCON Sri Radha Krishna Temple",
            hi = "इस्कॉन श्री राधा कृष्ण मंदिर",
            kn = "ಇಸ್ಕಾನ್ ಶ್ರೀ ರಾಧಾ ಕೃಷ್ಣ ದೇವಸ್ಥಾನ"
        ),
        deity = Deity.KRISHNA,
        area = t("Rajajinagar", hi = "राजाजीनगर", kn = "ರಾಜಾಜಿನಗರ"),
        address = "Hare Krishna Hill, Chord Road, Rajajinagar, Bengaluru, Karnataka 560010",
        location = GeoPoint(13.00981, 77.55109),
        about = t(
            "One of the largest Krishna temples in the world, opened in 1997 on a hill in " +
                "Rajajinagar. It is as much a cultural complex as a temple — a long ramped " +
                "walkway to the darshan hall, a multimedia gallery, a bookstore, a prasada " +
                "food court and the Akshaya Patra mid-day meal kitchen.",
            hi = "1997 में राजाजीनगर की पहाड़ी पर खुला, विश्व के सबसे बड़े कृष्ण मंदिरों में से एक। " +
                "यह मंदिर के साथ-साथ एक सांस्कृतिक परिसर भी है — रैंप वाला दर्शन मार्ग, पुस्तकालय, " +
                "प्रसाद फ़ूड कोर्ट और अक्षय पात्र रसोई।",
            kn = "1997ರಲ್ಲಿ ರಾಜಾಜಿನಗರದ ಗುಡ್ಡದ ಮೇಲೆ ತೆರೆದ, ವಿಶ್ವದ ಅತಿ ದೊಡ್ಡ ಕೃಷ್ಣ ದೇವಾಲಯಗಳಲ್ಲಿ ಒಂದು. " +
                "ದರ್ಶನ ಮಾರ್ಗ, ಪುಸ್ತಕ ಮಳಿಗೆ, ಪ್ರಸಾದ ಫುಡ್ ಕೋರ್ಟ್ ಮತ್ತು ಅಕ್ಷಯ ಪಾತ್ರ ಅಡುಗೆಮನೆ ಇಲ್ಲಿವೆ."
        ),
        builtIn = "1997",
        openings = listOf(
            window("04:15", "05:00"),
            window("07:15", "13:00"),
            window("16:15", "20:20")
        ),
        rituals = listOf(
            ritual(t("Mangala Arati", hi = "मंगल आरती", kn = "ಮಂಗಳ ಆರತಿ"), "04:15"),
            ritual(t("Sringar Darshan Arati", hi = "श्रृंगार दर्शन आरती", kn = "ಶೃಂಗಾರ ದರ್ಶನ ಆರತಿ"), "07:15"),
            ritual(t("Raj Bhoga Arati", hi = "राज भोग आरती", kn = "ರಾಜ ಭೋಗ ಆರತಿ"), "12:15"),
            ritual(t("Sandhya Arati", hi = "संध्या आरती", kn = "ಸಂಧ್ಯಾ ಆರತಿ"), "18:30"),
            ritual(t("Shayana Arati", hi = "शयन आरती", kn = "ಶಯನ ಆರತಿ"), "20:15")
        ),
        festivals = listOf(
            janmashtami.copy(
                note = t(
                    "The single busiest day of the year — the complex stays open through the " +
                        "night and crowd control extends onto Chord Road.",
                    hi = "साल का सबसे व्यस्त दिन — परिसर रातभर खुला रहता है और भीड़ कॉर्ड रोड तक फैलती है।",
                    kn = "ವರ್ಷದ ಅತ್ಯಂತ ಜನಸಂದಣಿಯ ದಿನ — ರಾತ್ರಿಯಿಡೀ ತೆರೆದಿರುತ್ತದೆ."
                )
            ),
            Festival(
                name = t("Rathayatra", hi = "रथयात्रा", kn = "ರಥಯಾತ್ರೆ"),
                whenApprox = t("Jun–Jul (Ashadha)", hi = "जून–जुलाई (आषाढ़)", kn = "ಜೂನ್–ಜುಲೈ (ಆಷಾಢ)"),
                usualMonth = 7
            ),
            Festival(
                name = t("Gita Jayanti", hi = "गीता जयंती", kn = "ಗೀತಾ ಜಯಂತಿ"),
                whenApprox = t("Nov–Dec (Margashira Shukla Ekadashi)", hi = "नव॰–दिस॰", kn = "ನವೆಂಬರ್–ಡಿಸೆಂಬರ್"),
                usualMonth = 12
            ),
            vaikunthaEkadashi
        ),
        bus = bus("ISKCON Temple / Mahalakshmi Layout"),
        metro = MetroInfo("Mahalakshmi", MetroLine.GREEN, 15),
        parking = ownParking,
        facilities = setOf(
            Facility.WHEELCHAIR,
            Facility.ELEVATOR,
            Facility.RESTROOMS,
            Facility.DRINKING_WATER,
            Facility.SHOE_STAND,
            Facility.CLOAK_ROOM,
            Facility.PRASAD_COUNTER,
            Facility.ANNADANA,
            Facility.PAID_PARKING,
            Facility.QUEUE_SHELTER,
            Facility.BOOKSTORE,
            Facility.NO_PHOTOGRAPHY
        ),
        dressCode = strictTraditionalDress,
        busiestDays = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
        photos = listOf(
            commons(
                "ISKCON Banglaore Temple.jpg",
                t("The temple on Hare Krishna Hill", hi = "हरे कृष्ण हिल पर मंदिर", kn = "ಹರೇ ಕೃಷ್ಣ ಬೆಟ್ಟದ ಮೇಲಿನ ದೇವಾಲಯ"),
                "Svpdasa", "CC BY 3.0"
            ),
            commons(
                "ISKCON Temple Bangalore (3475538274).jpg",
                t("Gopura and dhwaja stambha", hi = "गोपुरम और ध्वज स्तंभ", kn = "ಗೋಪುರ ಮತ್ತು ಧ್ವಜಸ್ತಂಭ"),
                "Ashwin Kumar", "CC BY-SA 2.0"
            ),
            commons(
                "Khichdi Prasadam in Donna (Iskcon Bangalore).jpg",
                t("Khichdi prasada served in a donne", hi = "दोन्ने में खिचड़ी प्रसाद", kn = "ದೊನ್ನೆಯಲ್ಲಿ ಖಿಚಡಿ ಪ್ರಸಾದ"),
                "Yuv103m", "CC BY-SA 4.0"
            )
        ),
        nearby = listOf(
            NearbyPlace(
                kind = NearbyKind.EAT,
                name = "Govinda's / prasada food court, inside the complex",
                note = t(
                    "Full vegetarian meals and snacks inside the temple complex — no onion or " +
                        "garlic. Convenient if you have queued through a meal time.",
                    hi = "मंदिर परिसर के भीतर शुद्ध शाकाहारी भोजन और नाश्ता — प्याज़-लहसुन रहित।",
                    kn = "ದೇವಾಲಯದ ಆವರಣದೊಳಗೆ ಸಂಪೂರ್ಣ ಸಸ್ಯಾಹಾರಿ ಊಟ ಮತ್ತು ತಿಂಡಿ — ಈರುಳ್ಳಿ, ಬೆಳ್ಳುಳ್ಳಿ ಇಲ್ಲದೆ."
                ),
                location = GeoPoint(13.0098, 77.5511)
            ),
            NearbyPlace(
                kind = NearbyKind.POOJA_ITEMS,
                name = "Temple gift and puja counter",
                note = t(
                    "Tulasi malas, incense, books and puja items are sold inside; outside " +
                        "stalls on the approach road sell flowers and garlands.",
                    hi = "भीतर तुलसी माला, अगरबत्ती, पुस्तकें और पूजा सामग्री; बाहर की दुकानों पर फूल और मालाएँ।",
                    kn = "ಒಳಗೆ ತುಳಸಿ ಮಾಲೆ, ಅಗರಬತ್ತಿ, ಪುಸ್ತಕ ಮತ್ತು ಪೂಜಾ ಸಾಮಗ್ರಿ; ಹೊರಗೆ ಹೂವು ಮತ್ತು ಹಾರ."
                ),
                location = GeoPoint(13.0098, 77.5511)
            ),
            NearbyPlace(
                kind = NearbyKind.PARKING,
                name = "Temple multi-level parking",
                note = t(
                    "Paid parking on the complex. It fills up by mid-morning at weekends.",
                    hi = "परिसर में सशुल्क पार्किंग। सप्ताहांत पर दोपहर से पहले भर जाती है।",
                    kn = "ಆವರಣದಲ್ಲಿ ಶುಲ್ಕದ ಪಾರ್ಕಿಂಗ್. ವಾರಾಂತ್ಯದಲ್ಲಿ ಬೆಳಗ್ಗೆಯೇ ತುಂಬುತ್ತದೆ."
                ),
                location = GeoPoint(13.0098, 77.5511)
            )
        ),
        donation = listOf(
            hundiDonation,
            onlineDonation("https://iskconbangalore.org/seva"),
            DonationChannel(
                kind = DonationKind.ANNADANA,
                label = t("Akshaya Patra mid-day meals", hi = "अक्षय पात्र मध्याह्न भोजन", kn = "ಅಕ್ಷಯ ಪಾತ್ರ ಮಧ್ಯಾಹ್ನ ಊಟ"),
                url = "https://iskconbangalore.org/annadana",
                detail = t(
                    "The temple kitchen cooks school mid-day meals; sponsorship is per child " +
                        "per year.",
                    hi = "मंदिर की रसोई स्कूलों के लिए मध्याह्न भोजन बनाती है; प्रायोजन प्रति बच्चा प्रति वर्ष।",
                    kn = "ದೇವಾಲಯದ ಅಡುಗೆಮನೆ ಶಾಲಾ ಮಧ್ಯಾಹ್ನದ ಊಟ ತಯಾರಿಸುತ್ತದೆ; ಪ್ರತಿ ಮಗುವಿಗೆ ವಾರ್ಷಿಕ ಪ್ರಾಯೋಜಕತ್ವ."
                )
            ),
            DonationChannel(
                kind = DonationKind.SEVA_BOOKING,
                label = t("Book a seva online", hi = "ऑनलाइन सेवा बुक करें", kn = "ಆನ್‌ಲೈನ್ ಸೇವೆ ಕಾಯ್ದಿರಿಸಿ"),
                url = "https://iskconbangalore.org/seva",
                detail = t(
                    "Archana, tulasi archana and abhisheka sevas can be booked ahead.",
                    hi = "अर्चना, तुलसी अर्चना और अभिषेक सेवाएँ पहले से बुक की जा सकती हैं।",
                    kn = "ಅರ್ಚನೆ, ತುಳಸಿ ಅರ್ಚನೆ ಮತ್ತು ಅಭಿಷೇಕ ಸೇವೆಗಳನ್ನು ಮೊದಲೇ ಕಾಯ್ದಿರಿಸಬಹುದು."
                )
            )
        ),
        website = "https://iskconbangalore.org",
        accent = VishnuAccent,
        confidence = DataConfidence.HIGH
    ),

    Temple(
        id = "ananda-lingeshwara",
        name = t(
            "Sri Ananda Lingeshwara Temple",
            hi = "श्री आनंद लिंगेश्वर मंदिर",
            kn = "ಶ್ರೀ ಆನಂದ ಲಿಂಗೇಶ್ವರ ದೇವಸ್ಥಾನ"
        ),
        deity = Deity.SHIVA,
        area = t("Hebbal", hi = "हेब्बल", kn = "ಹೆಬ್ಬಾಳ"),
        address = "Anandagiri Hill, near Hebbal, Bengaluru, Karnataka 560024",
        location = GeoPoint(13.03797, 77.59494),
        about = t(
            "A 13th-century Chola-era shrine on the small Anandagiri hill near Hebbal, " +
                "renovated in stages through the 2000s. A Kala Bhairaveshwara idol was " +
                "installed in a cave on the east face in 2013.",
            hi = "हेब्बल के पास आनंदगिरि पहाड़ी पर 13वीं सदी का चोलकालीन मंदिर, 2000 के दशक में " +
                "चरणबद्ध जीर्णोद्धार हुआ। 2013 में पूर्वी गुफा में काल भैरवेश्वर की प्रतिमा स्थापित हुई।",
            kn = "ಹೆಬ್ಬಾಳದ ಬಳಿ ಆನಂದಗಿರಿ ಗುಡ್ಡದ ಮೇಲೆ 13ನೇ ಶತಮಾನದ ಚೋಳರ ಕಾಲದ ದೇವಾಲಯ. " +
                "2013ರಲ್ಲಿ ಪೂರ್ವದ ಗುಹೆಯಲ್ಲಿ ಕಾಲ ಭೈರವೇಶ್ವರ ಮೂರ್ತಿ ಪ್ರತಿಷ್ಠಾಪನೆಯಾಯಿತು."
        ),
        builtIn = "13th century",
        openings = listOf(window("06:30", "12:00"), window("17:30", "20:00")),
        rituals = listOf(morningAbhisheka("07:30"), eveningAlankara("18:30")),
        festivals = listOf(mahaShivaratri, karthikaDeepotsava, ganeshaChaturthi),
        bus = bus("Hebbal / Anandagiri"),
        parking = streetParking,
        facilities = setOf(Facility.SHOE_STAND, Facility.DRINKING_WATER),
        dressCode = traditionalDress,
        busiestDays = setOf(DayOfWeek.MONDAY),
        photos = listOf(
            commons(
                "Idol of Sri Ananda Lingeshwara.jpg",
                t("The presiding linga", hi = "मुख्य लिंग", kn = "ಮೂಲ ಲಿಂಗ"),
                "Arunodayaappu", "CC BY-SA 3.0"
            )
        ),
        nearby = listOf(templeGateStalls()),
        donation = listOf(hundiDonation),
        accent = ShivaAccent,
        confidence = DataConfidence.MEDIUM
    ),

    Temple(
        id = "gali-anjaneya",
        name = t(
            "Gali Anjaneya Temple",
            hi = "गाली अंजनेय मंदिर",
            kn = "ಗಾಳಿ ಆಂಜನೇಯ ದೇವಸ್ಥಾನ"
        ),
        deity = Deity.HANUMAN,
        area = t("Mysore Road", hi = "मैसूर रोड", kn = "ಮೈಸೂರು ರಸ್ತೆ"),
        address = "Gali Anjaneya Temple Road, Mysore Road, Bengaluru, Karnataka 560026",
        location = null,
        about = t(
            "One of the oldest and most-visited Hanuman temples on the Mysore Road side of the " +
                "city, traditionally counted among the Anjaneya consecrations attributed to " +
                "Vyasaraja. Saturdays and Hanuma Jayanti bring queues onto the main road.",
            hi = "शहर के मैसूर रोड की ओर सबसे पुराने और सबसे अधिक देखे जाने वाले हनुमान मंदिरों में से एक, " +
                "परंपरा में व्यासराज द्वारा प्रतिष्ठित अंजनेय मंदिरों में गिना जाता है।",
            kn = "ನಗರದ ಮೈಸೂರು ರಸ್ತೆ ಭಾಗದ ಅತ್ಯಂತ ಹಳೆಯ ಮತ್ತು ಹೆಚ್ಚು ಭೇಟಿ ನೀಡುವ ಆಂಜನೇಯ ದೇವಾಲಯಗಳಲ್ಲಿ ಒಂದು; " +
                "ವ್ಯಾಸರಾಜರು ಪ್ರತಿಷ್ಠಾಪಿಸಿದ ಆಂಜನೇಯ ದೇವಾಲಯಗಳಲ್ಲಿ ಒಂದೆಂದು ಪರಂಪರೆ ಹೇಳುತ್ತದೆ."
        ),
        openings = listOf(window("06:00", "12:30"), window("16:30", "20:30")),
        rituals = listOf(
            ritual(t("Vada mala seva", hi = "वड़ा माला सेवा", kn = "ವಡೆ ಮಾಲೆ ಸೇವೆ"), priceInr = 300),
            eveningAlankara("18:30")
        ),
        festivals = listOf(hanumanJayanti, ramaNavami),
        bus = bus("Gali Anjaneya Temple, Mysore Road"),
        metro = MetroInfo("Mysuru Road", MetroLine.PURPLE, 12),
        parking = streetParking,
        facilities = setOf(
            Facility.SHOE_STAND,
            Facility.DRINKING_WATER,
            Facility.PRASAD_COUNTER,
            Facility.QUEUE_SHELTER
        ),
        dressCode = traditionalDress,
        busiestDays = setOf(DayOfWeek.SATURDAY),
        nearby = listOf(templeGateStalls(), darshiniNearby("Mysore Road darshinis", darshiniNote)),
        donation = listOf(hundiDonation, sevaCounterDonation),
        accent = HanumanAccent,
        confidence = DataConfidence.LOW
    ),

    Temple(
        id = "jnanakshi-rajarajeshwari",
        name = t(
            "Jnanakshi Rajarajeshwari Temple",
            hi = "ज्ञानाक्षी राजराजेश्वरी मंदिर",
            kn = "ಜ್ಞಾನಾಕ್ಷಿ ರಾಜರಾಜೇಶ್ವರಿ ದೇವಸ್ಥಾನ"
        ),
        deity = Deity.DEVI,
        area = t("Rajarajeshwari Nagar", hi = "राजराजेश्वरी नगर", kn = "ರಾಜರಾಜೇಶ್ವರಿ ನಗರ"),
        address = "Rajarajeshwari Nagar, Bengaluru, Karnataka 560098",
        location = GeoPoint(12.92995, 77.53601),
        about = t(
            "Built in 1978 and dedicated to Rajarajeshwari, a form of Tripura Sundari. The " +
                "temple gives its name to the whole township around it, and Navaratri here runs " +
                "as a full nine-day programme.",
            hi = "1978 में बना, त्रिपुर सुंदरी के रूप राजराजेश्वरी को समर्पित। पूरे उपनगर का नाम इसी मंदिर " +
                "पर है, और यहाँ नवरात्रि नौ दिन का पूरा कार्यक्रम होता है।",
            kn = "1978ರಲ್ಲಿ ನಿರ್ಮಿತ, ತ್ರಿಪುರ ಸುಂದರಿಯ ರೂಪವಾದ ರಾಜರಾಜೇಶ್ವರಿಗೆ ಸಮರ್ಪಿತ. ಇಡೀ ಬಡಾವಣೆಗೆ " +
                "ಇದೇ ಹೆಸರು; ಇಲ್ಲಿ ನವರಾತ್ರಿ ಒಂಬತ್ತು ದಿನಗಳ ಪೂರ್ಣ ಕಾರ್ಯಕ್ರಮ."
        ),
        builtIn = "1978",
        openings = listOf(window("06:30", "12:30"), window("17:00", "20:30")),
        rituals = listOf(
            morningAbhisheka("07:00"),
            ritual(t("Kumkumarchane", hi = "कुंकुमार्चन", kn = "ಕುಂಕುಮಾರ್ಚನೆ"), priceInr = 100),
            eveningAlankara("18:30")
        ),
        festivals = listOf(navaratri, ugadi, mahaShivaratri),
        bus = bus("Rajarajeshwari Nagar Arch / BEML Layout"),
        metro = MetroInfo("Rajarajeshwari Nagar", MetroLine.PURPLE, 20),
        parking = ownParking,
        facilities = setOf(
            Facility.SHOE_STAND,
            Facility.DRINKING_WATER,
            Facility.RESTROOMS,
            Facility.PRASAD_COUNTER,
            Facility.FREE_PARKING,
            Facility.ANNADANA,
            Facility.MARRIAGE_HALL,
            Facility.WHEELCHAIR
        ),
        dressCode = traditionalDress,
        busiestDays = setOf(DayOfWeek.FRIDAY, DayOfWeek.SUNDAY),
        photos = listOf(
            commons(
                "Sri Raja Rajeshwari Temple 02.jpg",
                t("The temple", hi = "मंदिर", kn = "ದೇವಾಲಯ"),
                "Harvinder Chandigarh", "CC BY 4.0"
            )
        ),
        nearby = listOf(templeGateStalls(), darshiniNearby("RR Nagar darshinis", darshiniNote)),
        donation = listOf(hundiDonation, sevaCounterDonation, annadanaDonation),
        accent = DeviAccent,
        confidence = DataConfidence.MEDIUM
    ),

    Temple(
        id = "dwadasha-jyotirlinga",
        name = t(
            "Sri Dwadasha Jyotirlinga Temple, Omkar Hills",
            hi = "श्री द्वादश ज्योतिर्लिंग मंदिर, ओंकार हिल्स",
            kn = "ಶ್ರೀ ದ್ವಾದಶ ಜ್ಯೋತಿರ್ಲಿಂಗ ದೇವಸ್ಥಾನ, ಓಂಕಾರ ಹಿಲ್ಸ್"
        ),
        deity = Deity.SHIVA,
        area = t("Omkar Hills", hi = "ओंकार हिल्स", kn = "ಓಂಕಾರ ಹಿಲ್ಸ್"),
        address = "Omkar Ashram, Omkar Hills, Srinivasapura, Bengaluru, Karnataka 560060",
        location = null,
        about = t(
            "All twelve jyotirlingas reproduced on one hilltop in the Omkar Ashram, so that a " +
                "pilgrimage that would otherwise cross the country can be done in a morning. " +
                "Omkar Hills is among the highest points in Bengaluru and the view is part of " +
                "the visit.",
            hi = "ओंकार आश्रम की पहाड़ी पर बारहों ज्योतिर्लिंगों की प्रतिकृतियाँ — पूरे देश की यात्रा " +
                "एक सुबह में। ओंकार हिल्स बेंगलुरु के सबसे ऊँचे स्थानों में से है।",
            kn = "ಓಂಕಾರ ಆಶ್ರಮದ ಬೆಟ್ಟದ ಮೇಲೆ ಹನ್ನೆರಡೂ ಜ್ಯೋತಿರ್ಲಿಂಗಗಳ ಪ್ರತಿಕೃತಿಗಳಿವೆ. " +
                "ಓಂಕಾರ ಹಿಲ್ಸ್ ಬೆಂಗಳೂರಿನ ಅತಿ ಎತ್ತರದ ಸ್ಥಳಗಳಲ್ಲಿ ಒಂದು."
        ),
        openings = listOf(window("07:00", "12:00"), window("16:00", "19:30")),
        rituals = listOf(morningAbhisheka("08:00"), eveningAlankara("18:00")),
        festivals = listOf(mahaShivaratri, karthikaDeepotsava),
        bus = bus("Omkar Hills / Kadarenahalli"),
        parking = ownParking,
        facilities = setOf(
            Facility.SHOE_STAND,
            Facility.DRINKING_WATER,
            Facility.RESTROOMS,
            Facility.FREE_PARKING,
            Facility.PHOTOGRAPHY_ALLOWED
        ),
        dressCode = traditionalDress,
        photos = listOf(
            commons(
                "Sri Dwadasha Jyothirlinga Devasthana 32.jpg",
                t("The temple complex", hi = "मंदिर परिसर", kn = "ದೇವಾಲಯ ಸಂಕೀರ್ಣ"),
                "Gpkp", "CC BY-SA 4.0"
            )
        ),
        nearby = listOf(templeGateStalls()),
        donation = listOf(hundiDonation, onlineDonation("https://www.omkarhills.org")),
        website = "https://www.omkarhills.org",
        accent = ShivaAccent,
        confidence = DataConfidence.MEDIUM
    ),

    Temple(
        id = "matsya-narayana",
        name = t(
            "Sri Matsya Narayana Temple, Omkar Hills",
            hi = "श्री मत्स्य नारायण मंदिर, ओंकार हिल्स",
            kn = "ಶ್ರೀ ಮತ್ಸ್ಯ ನಾರಾಯಣ ದೇವಸ್ಥಾನ, ಓಂಕಾರ ಹಿಲ್ಸ್"
        ),
        deity = Deity.VISHNU,
        area = t("Omkar Hills", hi = "ओंकार हिल्स", kn = "ಓಂಕಾರ ಹಿಲ್ಸ್"),
        address = "Omkar Ashram, Omkar Hills, Srinivasapura, Bengaluru, Karnataka 560060",
        location = null,
        about = t(
            "Vishnu in his Matsya avatara, in the same Omkar Ashram complex as the Dwadasha " +
                "Jyotirlinga temple and the Vanadurga shrine — the three are done in one visit.",
            hi = "मत्स्य अवतार में विष्णु, उसी ओंकार आश्रम परिसर में जहाँ द्वादश ज्योतिर्लिंग और " +
                "वनदुर्गा मंदिर हैं — तीनों एक ही यात्रा में।",
            kn = "ಮತ್ಸ್ಯ ಅವತಾರದಲ್ಲಿ ವಿಷ್ಣು; ದ್ವಾದಶ ಜ್ಯೋತಿರ್ಲಿಂಗ ಮತ್ತು ವನದುರ್ಗಾ ಸನ್ನಿಧಿಯ ಜೊತೆಗೆ ಒಂದೇ ಆವರಣದಲ್ಲಿ."
        ),
        openings = listOf(window("07:00", "12:00"), window("16:00", "19:30")),
        rituals = listOf(morningAbhisheka("08:00"), eveningAlankara("18:00")),
        festivals = listOf(vaikunthaEkadashi, ramaNavami),
        bus = bus("Omkar Hills / Kadarenahalli"),
        parking = ownParking,
        facilities = setOf(Facility.SHOE_STAND, Facility.DRINKING_WATER, Facility.FREE_PARKING),
        dressCode = traditionalDress,
        photos = listOf(
            commons(
                "Sri Matsya Narayana Temple and Sri Vanadurga Devi Temple 29.jpg",
                t(
                    "Matsya Narayana and Vanadurga shrines",
                    hi = "मत्स्य नारायण और वनदुर्गा मंदिर",
                    kn = "ಮತ್ಸ್ಯ ನಾರಾಯಣ ಮತ್ತು ವನದುರ್ಗಾ ಸನ್ನಿಧಿ"
                ),
                "Gpkp", "CC BY-SA 4.0"
            )
        ),
        nearby = listOf(templeGateStalls()),
        donation = listOf(hundiDonation, onlineDonation("https://www.omkarhills.org")),
        website = "https://www.omkarhills.org",
        accent = VishnuAccent,
        confidence = DataConfidence.MEDIUM
    ),

    Temple(
        id = "eshwara-kengeri",
        name = t(
            "Eshwara (Prasanna Someshwara) Temple, Kengeri",
            hi = "ईश्वर (प्रसन्न सोमेश्वर) मंदिर, केंगेरी",
            kn = "ಈಶ್ವರ (ಪ್ರಸನ್ನ ಸೋಮೇಶ್ವರ) ದೇವಸ್ಥಾನ, ಕೆಂಗೇರಿ"
        ),
        deity = Deity.SHIVA,
        area = t("Kengeri", hi = "केंगेरी", kn = "ಕೆಂಗೇರಿ"),
        address = "Kengeri, Bengaluru, Karnataka 560060",
        location = null,
        about = t(
            "A Chola temple from the reign of Rajendra Chola, around 1050 CE, near the Kote " +
                "Anjaneya Swamy temple in old Kengeri — one of a handful of surviving Chola-era " +
                "shrines inside the modern city.",
            hi = "लगभग 1050 ई., राजेंद्र चोल के काल का मंदिर, पुराने केंगेरी में कोटे अंजनेय स्वामी मंदिर के पास — " +
                "आधुनिक शहर में बचे कुछ चोलकालीन मंदिरों में से एक।",
            kn = "ಸುಮಾರು ಕ್ರಿ.ಶ. 1050, ರಾಜೇಂದ್ರ ಚೋಳನ ಕಾಲದ ದೇವಾಲಯ, ಹಳೆಯ ಕೆಂಗೇರಿಯಲ್ಲಿ ಕೋಟೆ ಆಂಜನೇಯ " +
                "ದೇವಾಲಯದ ಬಳಿ — ಇಂದಿನ ನಗರದಲ್ಲಿ ಉಳಿದಿರುವ ಕೆಲವೇ ಚೋಳ ದೇವಾಲಯಗಳಲ್ಲಿ ಒಂದು."
        ),
        builtIn = "c. 1050 CE",
        openings = listOf(window("07:00", "11:30"), window("18:00", "20:00")),
        rituals = listOf(morningAbhisheka("08:00"), eveningAlankara("18:30")),
        festivals = listOf(mahaShivaratri, karthikaDeepotsava),
        bus = bus("Kengeri Bus Terminal / Kengeri Satellite Town"),
        metro = MetroInfo("Kengeri", MetroLine.PURPLE, 12),
        parking = streetParking,
        facilities = setOf(Facility.SHOE_STAND),
        dressCode = traditionalDress,
        nearby = listOf(templeGateStalls()),
        donation = listOf(hundiDonation, muzraiTrust),
        accent = ShivaAccent,
        confidence = DataConfidence.LOW
    )
)
