package com.example.composelearning.temples.data

import java.time.DayOfWeek

/**
 * The old walled city — Pete, Avenue Road, Nagarathpet, Balepet, Kalasipalya and
 * Shivajinagar.
 *
 * These are the temples Kempe Gowda's Bengaluru was laid out around, and several of them
 * are older than the city itself. They are also the hardest to drive to: everything here is
 * a five-minute walk from a metro station and a nightmare to park at.
 */
internal val templesCity: List<Temple> = listOf(

    Temple(
        id = "dharmaraya-swamy",
        name = t(
            "Sri Dharmaraya Swamy Temple",
            hi = "श्री धर्मराय स्वामी मंदिर",
            kn = "ಶ್ರೀ ಧರ್ಮರಾಯ ಸ್ವಾಮಿ ದೇವಸ್ಥಾನ"
        ),
        deity = Deity.DEVI,
        area = t("Nagarathpet", hi = "नगरथपेट", kn = "ನಗರತಪೇಟೆ"),
        address = "Thigalarapete, Nagarathpet, Bengaluru, Karnataka 560002",
        location = GeoPoint(12.96540, 77.58340),
        about = t(
            "One of the oldest temples in the city, dedicated to Dharmaraya (Yudhishthira) and " +
                "Draupadi. It is the starting point of the Bengaluru Karaga — the nine-day " +
                "festival of the Thigala community in which a priest carries a flower-decked " +
                "pot through the old city overnight, drawing lakhs onto the streets.",
            hi = "शहर के सबसे प्राचीन मंदिरों में से एक, धर्मराय (युधिष्ठिर) और द्रौपदी को समर्पित। " +
                "यहीं से बेंगलुरु करगा शुरू होता है — नौ दिन का उत्सव जिसमें पुजारी फूलों से सजा घड़ा " +
                "लेकर रातभर पुराने शहर में निकलते हैं।",
            kn = "ನಗರದ ಅತ್ಯಂತ ಪುರಾತನ ದೇವಾಲಯಗಳಲ್ಲಿ ಒಂದು, ಧರ್ಮರಾಯ (ಯುಧಿಷ್ಠಿರ) ಮತ್ತು ದ್ರೌಪದಿಗೆ ಸಮರ್ಪಿತ. " +
                "ಬೆಂಗಳೂರು ಕರಗ ಇಲ್ಲಿಂದಲೇ ಆರಂಭವಾಗುತ್ತದೆ — ತಿಗಳ ಸಮುದಾಯದ ಒಂಬತ್ತು ದಿನಗಳ ಉತ್ಸವ."
        ),
        openings = listOf(window("06:30", "12:00"), window("17:30", "20:30")),
        rituals = listOf(morningAbhisheka("07:30"), eveningAlankara("18:30"), archanaSeva(50)),
        festivals = listOf(
            Festival(
                name = t("Bengaluru Karaga", hi = "बेंगलुरु करगा", kn = "ಬೆಂಗಳೂರು ಕರಗ"),
                whenApprox = t(
                    "Mar–Apr, full moon of Chaitra (nine days)",
                    hi = "मार्च–अप्रैल, चैत्र पूर्णिमा (नौ दिन)",
                    kn = "ಮಾರ್ಚ್–ಏಪ್ರಿಲ್, ಚೈತ್ರ ಹುಣ್ಣಿಮೆ (ಒಂಬತ್ತು ದಿನ)"
                ),
                usualMonth = 4,
                note = t(
                    "The main procession runs overnight through Pete and returns at dawn. " +
                        "Roads close from evening — come by metro and expect to stand for hours.",
                    hi = "मुख्य जुलूस रातभर पेटे से होकर भोर में लौटता है। शाम से सड़कें बंद — मेट्रो से आएँ।",
                    kn = "ಮುಖ್ಯ ಮೆರವಣಿಗೆ ರಾತ್ರಿಯಿಡೀ ಪೇಟೆಯಲ್ಲಿ ಸಾಗಿ ಬೆಳಗಿನ ಜಾವ ಮರಳುತ್ತದೆ. ಸಂಜೆಯಿಂದ ರಸ್ತೆ ಬಂದ್."
                )
            ),
            ugadi,
            navaratri
        ),
        bus = bus("KR Market / Kalasipalya"),
        metro = MetroInfo("Chickpete", MetroLine.GREEN, 10),
        parking = streetParking,
        facilities = setOf(
            Facility.SHOE_STAND,
            Facility.DRINKING_WATER,
            Facility.ANNADANA,
            Facility.QUEUE_SHELTER
        ),
        dressCode = traditionalDress,
        busiestDays = setOf(DayOfWeek.TUESDAY, DayOfWeek.FRIDAY),
        photos = listOf(
            commons(
                "Dharmaraya Swamy Temple Bangalore edit1.jpg",
                t("The temple in Thigalarapete", hi = "थिगलारापेट में मंदिर", kn = "ತಿಗಳರಪೇಟೆಯಲ್ಲಿ ದೇವಾಲಯ"),
                "Muhammad Mahdi Karim", "GFDL 1.2"
            ),
            commons(
                "Shri Dharmaraya Swamy Temple.JPG",
                t("Temple frontage", hi = "मंदिर का अग्रभाग", kn = "ದೇವಾಲಯದ ಮುಂಭಾಗ"),
                "Thigala4u", "CC BY-SA 2.0"
            )
        ),
        nearby = listOf(krMarketFlowers, avenueRoadStalls),
        donation = listOf(hundiDonation, sevaCounterDonation, annadanaDonation),
        accent = DeviAccent,
        confidence = DataConfidence.HIGH
    ),

    Temple(
        id = "kote-venkataramana",
        name = t(
            "Kote Venkataramana Temple",
            hi = "कोटे वेंकटरमण मंदिर",
            kn = "ಕೋಟೆ ವೆಂಕಟರಮಣ ದೇವಸ್ಥಾನ"
        ),
        deity = Deity.VISHNU,
        area = t("Kalasipalya", hi = "कलासीपाल्या", kn = "ಕಲಾಸಿಪಾಳ್ಯ"),
        address = "Krishnarajendra Road, near Tipu Sultan's Summer Palace, Bengaluru, Karnataka 560002",
        location = GeoPoint(12.95952, 77.57431),
        about = t(
            "Built in 1689 by Chikka Devaraja Wodeyar of Mysore, in Dravidian and Vijayanagara " +
                "style, inside what was then the Bangalore Fort. It stands next to Tipu Sultan's " +
                "summer palace, and the two are usually visited together.",
            hi = "1689 में मैसूर के चिक्कदेवराज वोडेयार द्वारा द्रविड़ और विजयनगर शैली में बनवाया गया, " +
                "तत्कालीन बैंगलोर क़िले के भीतर। टीपू सुल्तान के ग्रीष्म महल के ठीक बगल में।",
            kn = "1689ರಲ್ಲಿ ಮೈಸೂರಿನ ಚಿಕ್ಕದೇವರಾಜ ಒಡೆಯರ್ ಅವರು ದ್ರಾವಿಡ ಮತ್ತು ವಿಜಯನಗರ ಶೈಲಿಯಲ್ಲಿ " +
                "ಬೆಂಗಳೂರು ಕೋಟೆಯ ಒಳಗೆ ನಿರ್ಮಿಸಿದರು. ಟಿಪ್ಪು ಸುಲ್ತಾನನ ಬೇಸಿಗೆ ಅರಮನೆಯ ಪಕ್ಕದಲ್ಲಿದೆ."
        ),
        builtIn = "1689",
        openings = listOf(window("07:00", "12:00"), window("17:30", "20:30")),
        rituals = listOf(morningAbhisheka("07:30"), middayMangalarati("11:45"), eveningAlankara("18:30")),
        festivals = listOf(vaikunthaEkadashi, ramaNavami, ugadi),
        bus = bus("Kalasipalya / KR Market"),
        metro = MetroInfo("Krishna Rajendra Market", MetroLine.GREEN, 8),
        parking = streetParking,
        facilities = setOf(Facility.SHOE_STAND, Facility.DRINKING_WATER, Facility.NO_PHOTOGRAPHY),
        dressCode = traditionalDress,
        photos = listOf(
            commons(
                "Gopura (tower) over shrine in Kote Venkataramana Swamy Temple (17th century) at Bengaluru.JPG",
                t("The 17th-century gopura", hi = "17वीं सदी का गोपुरम", kn = "17ನೇ ಶತಮಾನದ ಗೋಪುರ"),
                "Dineshkannambadi", "CC BY-SA 3.0"
            )
        ),
        nearby = listOf(krMarketFlowers, avenueRoadStalls),
        donation = listOf(hundiDonation, sevaCounterDonation, muzraiTrust),
        accent = VishnuAccent,
        confidence = DataConfidence.HIGH
    ),

    Temple(
        id = "ranganathaswamy-balepet",
        name = t(
            "Ranganathaswamy Temple, Chickpete",
            hi = "रंगनाथस्वामी मंदिर, चिक्कपेट",
            kn = "ರಂಗನಾಥಸ್ವಾಮಿ ದೇವಸ್ಥಾನ, ಚಿಕ್ಕಪೇಟೆ"
        ),
        deity = Deity.VISHNU,
        area = t("Chickpete", hi = "चिक्कपेट", kn = "ಚಿಕ್ಕಪೇಟೆ"),
        address = "Balepet Main Road, Chickpete, Bengaluru, Karnataka 560053",
        location = GeoPoint(12.97026, 77.57933),
        about = t(
            "A 16th-century Vaikhanasa temple in the middle of the Pete market, built in " +
                "Vijayanagara style with carved granite pillars that show Hoysala influence. " +
                "Vaikuntha Ekadashi here means a queue that runs down Balepet Main Road.",
            hi = "पेटे बाज़ार के बीच 16वीं सदी का वैखानस मंदिर, विजयनगर शैली में — तराशे ग्रेनाइट स्तंभों पर " +
                "होयसल प्रभाव दिखता है। वैकुंठ एकादशी पर लंबी कतार लगती है।",
            kn = "ಪೇಟೆ ಮಾರುಕಟ್ಟೆಯ ನಡುವೆ 16ನೇ ಶತಮಾನದ ವೈಖಾನಸ ದೇವಾಲಯ, ವಿಜಯನಗರ ಶೈಲಿ; ಕೆತ್ತನೆಯ " +
                "ಗ್ರಾನೈಟ್ ಕಂಬಗಳಲ್ಲಿ ಹೊಯ್ಸಳ ಪ್ರಭಾವ ಕಾಣುತ್ತದೆ."
        ),
        builtIn = "16th century",
        openings = listOf(window("06:30", "12:00"), window("17:30", "20:30")),
        rituals = listOf(morningAbhisheka("07:00"), eveningAlankara("18:30"), archanaSeva(50)),
        festivals = listOf(vaikunthaEkadashi, ramaNavami, janmashtami, ugadi),
        bus = bus("Chickpete / Kempegowda Bus Station (Majestic)"),
        metro = MetroInfo("Chickpete", MetroLine.GREEN, 5),
        parking = streetParking,
        facilities = setOf(Facility.SHOE_STAND, Facility.DRINKING_WATER, Facility.PRASAD_COUNTER),
        dressCode = traditionalDress,
        photos = listOf(
            commons(
                "Sri Ranganatha Swamy Utsava Murthy Panchaloha Idols ,Bangalore.jpg",
                t(
                    "The panchaloha utsava murtis",
                    hi = "पंचलोह उत्सव मूर्तियाँ",
                    kn = "ಪಂಚಲೋಹ ಉತ್ಸವ ಮೂರ್ತಿಗಳು"
                ),
                "ASG Balaji", "CC BY-SA 3.0"
            )
        ),
        nearby = listOf(avenueRoadStalls, krMarketFlowers),
        donation = listOf(hundiDonation, sevaCounterDonation),
        accent = VishnuAccent,
        confidence = DataConfidence.MEDIUM
    ),

    Temple(
        id = "kote-jalakantheshwara",
        name = t(
            "Kote Jalakantheshwara Temple",
            hi = "कोटे जलकंठेश्वर मंदिर",
            kn = "ಕೋಟೆ ಜಲಕಂಠೇಶ್ವರ ದೇವಸ್ಥಾನ"
        ),
        deity = Deity.SHIVA,
        area = t("Kalasipalya", hi = "कलासीपाल्या", kn = "ಕಲಾಸಿಪಾಳ್ಯ"),
        address = "Near Kalasipalya Bus Stand, Bengaluru, Karnataka 560002",
        location = GeoPoint(12.96142, 77.57649),
        about = t(
            "A Chola-era temple later renovated by Kempe Gowda, unusual for having three " +
                "sanctums side by side — Jalakantheshwara, Parvati and Kailasanatha. Easy to " +
                "walk straight past in the noise of the Kalasipalya bus stand.",
            hi = "चोलकालीन मंदिर जिसका जीर्णोद्धार केम्पे गौड़ा ने कराया; इसकी विशेषता तीन गर्भगृह हैं — " +
                "जलकंठेश्वर, पार्वती और कैलासनाथ।",
            kn = "ಚೋಳರ ಕಾಲದ ದೇವಾಲಯ, ನಂತರ ಕೆಂಪೇಗೌಡರಿಂದ ಜೀರ್ಣೋದ್ಧಾರ; ಮೂರು ಗರ್ಭಗುಡಿಗಳಿರುವುದು ವಿಶೇಷ — " +
                "ಜಲಕಂಠೇಶ್ವರ, ಪಾರ್ವತಿ ಮತ್ತು ಕೈಲಾಸನಾಥ."
        ),
        builtIn = "Chola period",
        openings = listOf(window("06:30", "12:00"), window("17:30", "20:00")),
        rituals = listOf(morningAbhisheka("07:30"), eveningAlankara("18:30")),
        festivals = listOf(mahaShivaratri, karthikaDeepotsava),
        bus = bus("Kalasipalya Bus Stand"),
        metro = MetroInfo("Krishna Rajendra Market", MetroLine.GREEN, 8),
        parking = streetParking,
        facilities = setOf(Facility.SHOE_STAND, Facility.DRINKING_WATER),
        dressCode = traditionalDress,
        busiestDays = setOf(DayOfWeek.MONDAY),
        photos = listOf(
            commons(
                "Kote Jalakantheshwara Temple.jpg",
                t("The temple", hi = "मंदिर", kn = "ದೇವಾಲಯ"),
                "Siddhartha Sahu", "CC BY 3.0"
            )
        ),
        nearby = listOf(krMarketFlowers, avenueRoadStalls),
        donation = listOf(hundiDonation, muzraiTrust),
        accent = ShivaAccent,
        confidence = DataConfidence.MEDIUM
    ),

    Temple(
        id = "yelahanka-gate-anjaneya",
        name = t(
            "Yelahanka Gate Anjaneya Temple",
            hi = "येलहंका गेट अंजनेय मंदिर",
            kn = "ಯಲಹಂಕ ಗೇಟ್ ಆಂಜನೇಯ ದೇವಸ್ಥಾನ"
        ),
        deity = Deity.HANUMAN,
        area = t("Avenue Road", hi = "एवेन्यू रोड", kn = "ಅವೆನ್ಯೂ ರಸ್ತೆ"),
        address = "Avenue Road, Bengaluru, Karnataka 560002",
        location = GeoPoint(12.96860, 77.57790),
        about = t(
            "Built by Kempe Gowda at the Yelahanka gate of the old Bangalore fort — the " +
                "northern entrance to his new city — and named after it. The gate is long gone; " +
                "the temple is still open on Avenue Road.",
            hi = "केम्पे गौड़ा द्वारा पुराने बैंगलोर क़िले के येलहंका द्वार पर बनवाया गया, उसी के नाम पर। " +
                "द्वार अब नहीं रहा; मंदिर आज भी एवेन्यू रोड पर है।",
            kn = "ಕೆಂಪೇಗೌಡರು ಹಳೆಯ ಬೆಂಗಳೂರು ಕೋಟೆಯ ಯಲಹಂಕ ಬಾಗಿಲಿನ ಬಳಿ ನಿರ್ಮಿಸಿದರು, ಅದೇ ಹೆಸರು ಉಳಿದಿದೆ. " +
                "ಬಾಗಿಲು ಈಗಿಲ್ಲ; ದೇವಾಲಯ ಇಂದಿಗೂ ಅವೆನ್ಯೂ ರಸ್ತೆಯಲ್ಲಿದೆ."
        ),
        builtIn = "16th century",
        openings = listOf(window("06:30", "12:00"), window("17:00", "20:30")),
        rituals = listOf(eveningAlankara("18:30"), archanaSeva(30)),
        festivals = listOf(hanumanJayanti, ramaNavami),
        bus = bus("Avenue Road / KR Market"),
        metro = MetroInfo("Chickpete", MetroLine.GREEN, 6),
        parking = streetParking,
        facilities = setOf(Facility.SHOE_STAND),
        dressCode = traditionalDress,
        busiestDays = setOf(DayOfWeek.SATURDAY),
        photos = listOf(
            commons(
                "Temple at entrance to Avensue road of the Old Bangaluru Pete.JPG",
                t(
                    "The temple at the entrance to Avenue Road",
                    hi = "एवेन्यू रोड के प्रवेश पर मंदिर",
                    kn = "ಅವೆನ್ಯೂ ರಸ್ತೆಯ ಪ್ರವೇಶದಲ್ಲಿ ದೇವಾಲಯ"
                ),
                "Nvvchar", "CC BY-SA 3.0"
            )
        ),
        nearby = listOf(avenueRoadStalls, krMarketFlowers),
        donation = listOf(hundiDonation),
        accent = HanumanAccent,
        confidence = DataConfidence.MEDIUM
    ),

    Temple(
        id = "kaalikaamba-kamateshwara",
        name = t(
            "Kaalikaamba Kamateshwara Temple",
            hi = "कालिकांबा कामटेश्वर मंदिर",
            kn = "ಕಾಳಿಕಾಂಬ ಕಾಮಟೇಶ್ವರ ದೇವಸ್ಥಾನ"
        ),
        deity = Deity.DEVI,
        area = t("Nagarathpet", hi = "नगरथपेट", kn = "ನಗರತಪೇಟೆ"),
        address = "Nagarathpet Main Road, Bengaluru, Karnataka 560002",
        location = GeoPoint(12.96720, 77.57920),
        about = t(
            "Close to 800 years old, dating to the Chola period, dedicated jointly to " +
                "Kaalikaamba and Kamateshwara — the goddess and Shiva in one precinct. Rahukala " +
                "puja on Friday evening is the busiest slot of the week.",
            hi = "लगभग 800 वर्ष पुराना, चोलकालीन मंदिर — कालिकांबा और कामटेश्वर दोनों को समर्पित। " +
                "शुक्रवार शाम की राहुकाल पूजा में सबसे अधिक भीड़।",
            kn = "ಸುಮಾರು 800 ವರ್ಷ ಹಳೆಯ ಚೋಳರ ಕಾಲದ ದೇವಾಲಯ — ಕಾಳಿಕಾಂಬ ಮತ್ತು ಕಾಮಟೇಶ್ವರ ಇಬ್ಬರಿಗೂ ಸಮರ್ಪಿತ. " +
                "ಶುಕ್ರವಾರ ಸಂಜೆಯ ರಾಹುಕಾಲ ಪೂಜೆಗೆ ಹೆಚ್ಚು ಜನ."
        ),
        builtIn = "Chola period",
        openings = listOf(window("06:30", "12:30"), window("17:00", "20:30")),
        rituals = listOf(
            morningAbhisheka("07:30"),
            ritual(t("Rahukala puja", hi = "राहुकाल पूजा", kn = "ರಾಹುಕಾಲ ಪೂಜೆ"), "16:30"),
            eveningAlankara("18:30")
        ),
        festivals = listOf(navaratri, ugadi, mahaShivaratri),
        bus = bus("Nagarathpet / KR Market"),
        metro = MetroInfo("Chickpete", MetroLine.GREEN, 9),
        parking = streetParking,
        facilities = setOf(Facility.SHOE_STAND, Facility.DRINKING_WATER),
        dressCode = traditionalDress,
        busiestDays = setOf(DayOfWeek.FRIDAY, DayOfWeek.TUESDAY),
        nearby = listOf(krMarketFlowers, avenueRoadStalls),
        donation = listOf(hundiDonation, sevaCounterDonation),
        accent = DeviAccent,
        confidence = DataConfidence.MEDIUM
    ),

    Temple(
        id = "dandu-mariamman",
        name = t(
            "Sree Dandu Mariamman Temple",
            hi = "श्री दंडु मारीअम्मन मंदिर",
            kn = "ಶ್ರೀ ದಂಡು ಮಾರಿಯಮ್ಮ ದೇವಸ್ಥಾನ"
        ),
        deity = Deity.DEVI,
        area = t("Shivajinagar", hi = "शिवाजीनगर", kn = "ಶಿವಾಜಿನಗರ"),
        address = "Shivajinagar, Bengaluru, Karnataka 560051",
        location = GeoPoint(12.98476, 77.60151),
        about = t(
            "The Mariamman temple of the old Cantonment — 'dandu' is the Kannada word for the " +
                "cantonment itself. The annual Karaga here, distinct from the Pete one, is the " +
                "big event of the Shivajinagar calendar.",
            hi = "पुराने छावनी क्षेत्र का मारीअम्मन मंदिर — कन्नड़ में 'दंडु' का अर्थ छावनी है। " +
                "यहाँ का वार्षिक करगा शिवाजीनगर का सबसे बड़ा आयोजन है।",
            kn = "ಹಳೆಯ ದಂಡು ಪ್ರದೇಶದ ಮಾರಿಯಮ್ಮ ದೇವಾಲಯ — ಕನ್ನಡದಲ್ಲಿ 'ದಂಡು' ಎಂದರೆ ಸೇನಾ ನೆಲೆ. " +
                "ಇಲ್ಲಿನ ವಾರ್ಷಿಕ ಕರಗ ಶಿವಾಜಿನಗರದ ದೊಡ್ಡ ಹಬ್ಬ."
        ),
        openings = listOf(window("06:30", "12:30"), window("17:00", "20:30")),
        rituals = listOf(morningAbhisheka("07:30"), eveningAlankara("18:30")),
        festivals = listOf(
            Festival(
                name = t("Dandu Karaga", hi = "दंडु करगा", kn = "ದಂಡು ಕರಗ"),
                whenApprox = t("Mar–Apr (Chaitra)", hi = "मार्च–अप्रैल (चैत्र)", kn = "ಮಾರ್ಚ್–ಏಪ್ರಿಲ್ (ಚೈತ್ರ)"),
                usualMonth = 4
            ),
            navaratri,
            ugadi
        ),
        bus = bus("Shivajinagar Bus Station"),
        metro = MetroInfo("Cubbon Park", MetroLine.PURPLE, 20),
        parking = streetParking,
        facilities = setOf(Facility.SHOE_STAND, Facility.DRINKING_WATER, Facility.ANNADANA),
        dressCode = traditionalDress,
        busiestDays = setOf(DayOfWeek.TUESDAY, DayOfWeek.FRIDAY),
        nearby = listOf(
            templeGateStalls(GeoPoint(12.9848, 77.6017)),
            darshiniNearby("Commercial Street area darshinis", darshiniNote, GeoPoint(12.9829, 77.6090))
        ),
        donation = listOf(hundiDonation, annadanaDonation),
        accent = DeviAccent,
        confidence = DataConfidence.MEDIUM
    )
)
