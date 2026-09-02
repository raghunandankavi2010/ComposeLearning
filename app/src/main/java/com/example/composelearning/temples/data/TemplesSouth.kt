package com.example.composelearning.temples.data

import java.time.DayOfWeek

/**
 * South Bengaluru — Basavanagudi, Gavipuram, Hanumanthanagar, Jayanagar, Banashankari,
 * Madiwala, Uttarahalli and Bannerghatta Road.
 *
 * This is the densest temple belt in the city: Kempe Gowda's 16th-century foundations sit a
 * few streets from the Chola-era ones, and Gandhi Bazaar supplies flowers to all of them.
 */
internal val templesSouth: List<Temple> = listOf(

    Temple(
        id = "bull-temple",
        name = t(
            "Bull Temple (Dodda Basavana Gudi)",
            hi = "बुल टेंपल (दोड्डा बसवण्णा गुड़ी)",
            kn = "ದೊಡ್ಡ ಬಸವಣ್ಣ ಗುಡಿ (ನಂದಿ ದೇವಸ್ಥಾನ)"
        ),
        deity = Deity.SHIVA,
        area = t("Basavanagudi", hi = "बसवनगुड़ी", kn = "ಬಸವನಗುಡಿ"),
        address = "Bull Temple Road, Basavanagudi, Bengaluru, Karnataka 560004",
        location = GeoPoint(12.94289, 77.56821),
        about = t(
            "Kempe Gowda I raised this temple in the 16th century around a single granite " +
                "monolith of Nandi, roughly 4.6 m tall — said to be the largest Nandi shrine " +
                "anywhere. The bull is kept glossy with fresh layers of butter, and the temple " +
                "sits inside the Bugle Rock park on a 3,000-million-year-old gneiss outcrop.",
            hi = "16वीं सदी में केम्पे गौड़ा प्रथम द्वारा बनवाया गया यह मंदिर एक ही ग्रेनाइट शिला से " +
                "तराशे गए लगभग 4.6 मीटर ऊँचे नंदी के लिए प्रसिद्ध है। नंदी पर नियमित रूप से मक्खन चढ़ाया जाता है।",
            kn = "16ನೇ ಶತಮಾನದಲ್ಲಿ ಕೆಂಪೇಗೌಡರು ನಿರ್ಮಿಸಿದ ಈ ದೇವಾಲಯದಲ್ಲಿ ಒಂದೇ ಗ್ರಾನೈಟ್ ಶಿಲೆಯಲ್ಲಿ ಕೆತ್ತಿದ " +
                "ಸುಮಾರು 4.6 ಮೀಟರ್ ಎತ್ತರದ ನಂದಿ ವಿಗ್ರಹವಿದೆ. ವಿಗ್ರಹಕ್ಕೆ ನಿತ್ಯ ಬೆಣ್ಣೆ ಲೇಪಿಸಲಾಗುತ್ತದೆ."
        ),
        builtIn = "1537",
        openings = listOf(window("06:00", "12:00"), window("17:30", "20:30")),
        rituals = listOf(
            morningAbhisheka("07:30"),
            middayMangalarati("11:30"),
            eveningAlankara("18:30"),
            archanaSeva(50)
        ),
        festivals = listOf(
            Festival(
                name = t("Kadalekai Parishe (groundnut fair)", hi = "कदलेकाई परिषे (मूँगफली मेला)", kn = "ಕಡಲೆಕಾಯಿ ಪರಿಷೆ"),
                whenApprox = t(
                    "Nov–Dec, last Monday of Karthika masa",
                    hi = "नव॰–दिस॰, कार्तिक मास का अंतिम सोमवार",
                    kn = "ನವೆಂಬರ್–ಡಿಸೆಂಬರ್, ಕಾರ್ತಿಕ ಮಾಸದ ಕೊನೆಯ ಸೋಮವಾರ"
                ),
                usualMonth = 11,
                note = t(
                    "Farmers bring the season's first groundnut crop to Nandi and the whole of " +
                        "Bull Temple Road turns into a two-day fair. Come on foot — the road closes.",
                    hi = "किसान मौसम की पहली मूँगफली की फ़सल नंदी को अर्पित करते हैं और पूरी बुल टेंपल रोड " +
                        "दो दिन का मेला बन जाती है। पैदल आएँ — सड़क बंद रहती है।",
                    kn = "ರೈತರು ಋತುವಿನ ಮೊದಲ ಕಡಲೆಕಾಯಿ ಬೆಳೆಯನ್ನು ನಂದಿಗೆ ಅರ್ಪಿಸುತ್ತಾರೆ; ಇಡೀ ರಸ್ತೆ ಎರಡು ದಿನ ಜಾತ್ರೆಯಾಗುತ್ತದೆ. ನಡೆದೇ ಬನ್ನಿ."
                )
            ),
            mahaShivaratri,
            karthikaDeepotsava
        ),
        bus = bus("National College / Bull Temple Road"),
        metro = MetroInfo("National College", MetroLine.GREEN, 20),
        parking = streetParking,
        facilities = setOf(
            Facility.SHOE_STAND,
            Facility.DRINKING_WATER,
            Facility.QUEUE_SHELTER,
            Facility.RESTROOMS,
            Facility.PHOTOGRAPHY_ALLOWED
        ),
        dressCode = traditionalDress,
        busiestDays = setOf(DayOfWeek.MONDAY, DayOfWeek.SATURDAY),
        photos = listOf(
            commons(
                "Sri Big Bull Temple, Dodda Ganeshana Gudi, Bangalore, India - 20130526-02.JPG",
                t("The monolithic Nandi", hi = "एकाश्म नंदी", kn = "ಏಕಶಿಲಾ ನಂದಿ"),
                "Smuconlaw", "CC BY-SA 3.0"
            ),
            commons(
                "Sri Big Bull Temple, Dodda Ganeshana Gudi, Bangalore, India - 20130526-04.JPG",
                t("Inside the temple", hi = "मंदिर के भीतर", kn = "ದೇವಾಲಯದ ಒಳಗೆ"),
                "Smuconlaw", "CC BY-SA 3.0"
            ),
            commons(
                "Bugle Rock Kahale.jpg",
                t("Bugle Rock, the park around the temple", hi = "मंदिर के चारों ओर बुगल रॉक पार्क", kn = "ದೇವಾಲಯದ ಸುತ್ತಲಿನ ಬ್ಯೂಗಲ್ ರಾಕ್ ಉದ್ಯಾನ"),
                "User:Sarvagnya", "CC BY 2.5"
            )
        ),
        nearby = listOf(gandhiBazaar, vidyarthiBhavan, vvPuramFoodStreet, mtr),
        donation = listOf(hundiDonation, sevaCounterDonation, muzraiTrust),
        accent = ShivaAccent,
        confidence = DataConfidence.HIGH
    ),

    Temple(
        id = "dodda-ganapathi",
        name = t(
            "Dodda Ganapathi Temple",
            hi = "दोड्डा गणपति मंदिर",
            kn = "ದೊಡ್ಡ ಗಣಪತಿ ದೇವಸ್ಥಾನ"
        ),
        deity = Deity.GANESHA,
        area = t("Basavanagudi", hi = "बसवनगुड़ी", kn = "ಬಸವನಗುಡಿ"),
        address = "Bull Temple Road, Basavanagudi, Bengaluru, Karnataka 560004",
        location = GeoPoint(12.94320, 77.56860),
        about = t(
            "A few steps downhill from the Bull Temple, this shrine holds a 5.5 m monolithic " +
                "Ganesha carved from a single black granite boulder. On Ganesha Chaturthi the " +
                "idol is covered head to foot in 100 kg of butter, which is later distributed.",
            hi = "बुल टेंपल से कुछ ही क़दम नीचे, यहाँ एक ही काले ग्रेनाइट शिला से तराशी गई 5.5 मीटर ऊँची " +
                "गणेश प्रतिमा है। गणेश चतुर्थी पर प्रतिमा को लगभग 100 किलो मक्खन से ढका जाता है।",
            kn = "ಬುಲ್ ಟೆಂಪಲ್‌ನಿಂದ ಕೆಲವೇ ಹೆಜ್ಜೆ ಕೆಳಗೆ, ಒಂದೇ ಕಪ್ಪು ಗ್ರಾನೈಟ್ ಶಿಲೆಯಲ್ಲಿ ಕೆತ್ತಿದ 5.5 ಮೀಟರ್ " +
                "ಎತ್ತರದ ಗಣೇಶ ಮೂರ್ತಿ ಇದೆ. ಗಣೇಶ ಚತುರ್ಥಿಯಂದು ಮೂರ್ತಿಗೆ ಸುಮಾರು 100 ಕೆ.ಜಿ. ಬೆಣ್ಣೆ ಅಲಂಕಾರ ಮಾಡಲಾಗುತ್ತದೆ."
        ),
        builtIn = "16th century",
        openings = listOf(window("06:00", "12:00"), window("17:30", "20:30")),
        rituals = listOf(morningAbhisheka("07:00"), eveningAlankara("18:30"), archanaSeva(30)),
        festivals = listOf(
            ganeshaChaturthi.copy(
                note = t(
                    "Benne alankara — the whole idol is dressed in butter, then given away as prasada.",
                    hi = "बेन्ने अलंकार — पूरी प्रतिमा मक्खन से सजाई जाती है, फिर प्रसाद रूप में बाँटी जाती है।",
                    kn = "ಬೆಣ್ಣೆ ಅಲಂಕಾರ — ಇಡೀ ಮೂರ್ತಿಗೆ ಬೆಣ್ಣೆ ಲೇಪಿಸಿ ನಂತರ ಪ್ರಸಾದವಾಗಿ ಹಂಚಲಾಗುತ್ತದೆ."
                )
            ),
            karthikaDeepotsava
        ),
        bus = bus("National College / Bull Temple Road"),
        metro = MetroInfo("National College", MetroLine.GREEN, 20),
        parking = streetParking,
        facilities = setOf(Facility.SHOE_STAND, Facility.DRINKING_WATER, Facility.PRASAD_COUNTER),
        dressCode = traditionalDress,
        busiestDays = setOf(DayOfWeek.TUESDAY, DayOfWeek.FRIDAY),
        photos = listOf(
            commons(
                "Dodda Ganeshana Gudi Hindu temple, Basavanagudi, Karnataka, India.jpg",
                t("The monolithic Ganesha", hi = "एकाश्म गणेश प्रतिमा", kn = "ಏಕಶಿಲಾ ಗಣೇಶ ಮೂರ್ತಿ"),
                "Rkrish67", "Public domain"
            )
        ),
        nearby = listOf(gandhiBazaar, vidyarthiBhavan, vvPuramFoodStreet),
        donation = listOf(hundiDonation, sevaCounterDonation),
        accent = GaneshaAccent,
        confidence = DataConfidence.HIGH
    ),

    Temple(
        id = "gavi-gangadhareshwara",
        name = t(
            "Gavi Gangadhareshwara Cave Temple",
            hi = "गवी गंगाधरेश्वर गुफा मंदिर",
            kn = "ಗವಿ ಗಂಗಾಧರೇಶ್ವರ ಗುಹಾ ದೇವಸ್ಥಾನ"
        ),
        deity = Deity.SHIVA,
        area = t("Gavipuram", hi = "गवीपुरम", kn = "ಗವಿಪುರಂ"),
        address = "Gavipuram, Kempegowda Nagar, Bengaluru, Karnataka 560019",
        location = GeoPoint(12.94819, 77.56300),
        about = t(
            "A rock-cut temple built into a natural cave by Kempe Gowda I in the 16th century. " +
                "Four monolithic granite pillars stand in the forecourt, and on Makara Sankranti " +
                "the setting sun lines up so that its light passes between Nandi's horns and " +
                "falls on the linga inside the cave for a few minutes.",
            hi = "16वीं सदी में केम्पे गौड़ा प्रथम द्वारा प्राकृतिक गुफा में बनाया गया शैलकृत मंदिर। " +
                "मकर संक्रांति पर डूबते सूर्य की किरणें नंदी के सींगों के बीच से होकर गुफा के शिवलिंग पर पड़ती हैं।",
            kn = "16ನೇ ಶತಮಾನದಲ್ಲಿ ಕೆಂಪೇಗೌಡರು ನೈಸರ್ಗಿಕ ಗುಹೆಯಲ್ಲಿ ನಿರ್ಮಿಸಿದ ಶಿಲಾ ದೇವಾಲಯ. ಮಕರ ಸಂಕ್ರಾಂತಿಯಂದು " +
                "ಸೂರ್ಯನ ಕಿರಣಗಳು ನಂದಿಯ ಕೊಂಬುಗಳ ನಡುವೆ ಹಾದು ಗುಹೆಯೊಳಗಿನ ಲಿಂಗದ ಮೇಲೆ ಬೀಳುತ್ತವೆ."
        ),
        builtIn = "16th century",
        openings = listOf(window("06:00", "12:30"), window("17:00", "20:30")),
        rituals = listOf(morningAbhisheka("06:30"), eveningAlankara("18:00"), nightMangalarati("20:00")),
        festivals = listOf(
            makaraSankranti.copy(
                note = t(
                    "The sun-through-the-horns alignment. Get there well before sunset — the " +
                        "courtyard is small and fills up hours early.",
                    hi = "सूर्य-किरण का नंदी के सींगों से गुज़रना। सूर्यास्त से काफ़ी पहले पहुँचें — आँगन छोटा है।",
                    kn = "ಸೂರ್ಯ ಕಿರಣ ನಂದಿಯ ಕೊಂಬುಗಳ ನಡುವೆ ಹಾದುಹೋಗುವ ದೃಶ್ಯ. ಸೂರ್ಯಾಸ್ತಕ್ಕೆ ಮೊದಲೇ ತಲುಪಿ."
                )
            ),
            mahaShivaratri,
            karthikaDeepotsava
        ),
        bus = bus("Gavipuram / Ramakrishna Ashrama"),
        metro = MetroInfo("National College", MetroLine.GREEN, 18),
        parking = streetParking,
        facilities = setOf(
            Facility.SHOE_STAND,
            Facility.DRINKING_WATER,
            Facility.NO_PHOTOGRAPHY,
            Facility.QUEUE_SHELTER
        ),
        dressCode = strictTraditionalDress,
        busiestDays = setOf(DayOfWeek.MONDAY),
        photos = listOf(
            commons(
                "Gavi Gangadhareshwara temple, Bengaluru.jpg",
                t("The cave temple", hi = "गुफा मंदिर", kn = "ಗುಹಾ ದೇವಾಲಯ"),
                "Dineshkannambadi", "CC BY-SA 4.0"
            ),
            commons(
                "A trident outside Gavigangadareswara temple in Bangalore.jpg",
                t("The monolithic trishula in the forecourt", hi = "आँगन में एकाश्म त्रिशूल", kn = "ಪ್ರಾಂಗಣದಲ್ಲಿ ಏಕಶಿಲಾ ತ್ರಿಶೂಲ"),
                "Nvvchar", "CC BY-SA 3.0"
            ),
            commons(
                "Gangadhareshwara Temple hill.jpg",
                t("The rock the temple is cut into", hi = "वह शिला जिसमें मंदिर तराशा गया है", kn = "ದೇವಾಲಯ ಕೆತ್ತಿದ ಬಂಡೆ"),
                "Avnishmsingh", "CC BY-SA 4.0"
            )
        ),
        nearby = listOf(templeGateStalls(GeoPoint(12.9483, 77.5632)), gandhiBazaar, mtr),
        donation = listOf(hundiDonation, sevaCounterDonation, muzraiTrust),
        accent = ShivaAccent,
        confidence = DataConfidence.HIGH
    ),

    Temple(
        id = "pralayakala-veerabhadra",
        name = t(
            "Pralayakala Veerabhadra Temple",
            hi = "प्रलयकाल वीरभद्र मंदिर",
            kn = "ಪ್ರಳಯಕಾಲ ವೀರಭದ್ರ ದೇವಸ್ಥಾನ"
        ),
        deity = Deity.SHIVA,
        area = t("Gavipuram Guttahalli", hi = "गवीपुरम गुट्टहल्ली", kn = "ಗವಿಪುರಂ ಗುಟ್ಟಹಳ್ಳಿ"),
        address = "Gavipuram Guttahalli, Bengaluru, Karnataka 560019",
        // No verified pin for this one; the maps hand-off geocodes name + address instead.
        location = null,
        about = t(
            "A small, old shrine to Veerabhadra — the fierce form Shiva takes at the end of an " +
                "age — a short walk from the Gavi cave temple. Quiet on most days, which is " +
                "part of its appeal.",
            hi = "गवी गुफा मंदिर से थोड़ी दूर वीरभद्र का एक छोटा, प्राचीन मंदिर — शिव का प्रलयकालीन उग्र रूप। " +
                "अधिकतर दिन शांत रहता है।",
            kn = "ಗವಿ ಗುಹಾ ದೇವಾಲಯದಿಂದ ಸ್ವಲ್ಪ ದೂರದಲ್ಲಿ ವೀರಭದ್ರನ ಸಣ್ಣ, ಪುರಾತನ ದೇವಾಲಯ — ಪ್ರಳಯಕಾಲದಲ್ಲಿ ಶಿವನ ಉಗ್ರ ರೂಪ."
        ),
        openings = listOf(window("07:00", "11:30"), window("18:00", "20:00")),
        rituals = listOf(morningAbhisheka("08:00"), eveningAlankara("18:30")),
        festivals = listOf(mahaShivaratri, karthikaDeepotsava),
        bus = bus("Gavipuram Guttahalli"),
        metro = MetroInfo("National College", MetroLine.GREEN, 18),
        parking = streetParking,
        facilities = setOf(Facility.SHOE_STAND),
        dressCode = traditionalDress,
        nearby = listOf(gandhiBazaar, vvPuramFoodStreet),
        donation = listOf(hundiDonation),
        accent = ShivaAccent,
        confidence = DataConfidence.LOW
    ),

    Temple(
        id = "kumaraswamy-hanumanthanagar",
        name = t(
            "Sri Kumaraswamy Temple",
            hi = "श्री कुमारस्वामी मंदिर",
            kn = "ಶ್ರೀ ಕುಮಾರಸ್ವಾಮಿ ದೇವಸ್ಥಾನ"
        ),
        deity = Deity.SUBRAMANYA,
        area = t("Hanumanthanagar", hi = "हनुमंतनगर", kn = "ಹನುಮಂತನಗರ"),
        address = "Kumaraswamy Temple Road, Hanumanthanagar, Bengaluru, Karnataka 560019",
        location = GeoPoint(12.94047, 77.56258),
        about = t(
            "Dedicated to Kartikeya — Subrahmanya, Kumaraswamy, Murugan — on a rise in " +
                "Hanumanthanagar. The Shashti days of each lunar fortnight and Skanda Shashti " +
                "in Karthika masa are the temple's busiest.",
            hi = "हनुमंतनगर की ऊँचाई पर कार्तिकेय — सुब्रह्मण्य, कुमारस्वामी, मुरुगन — को समर्पित मंदिर। " +
                "षष्ठी तिथियों और कार्तिक की स्कंद षष्ठी पर सबसे अधिक भीड़ रहती है।",
            kn = "ಹನುಮಂತನಗರದ ಎತ್ತರದ ಜಾಗದಲ್ಲಿ ಕಾರ್ತಿಕೇಯ — ಸುಬ್ರಹ್ಮಣ್ಯ, ಕುಮಾರಸ್ವಾಮಿ, ಮುರುಗ — ನಿಗೆ ಸಮರ್ಪಿತ. " +
                "ಷಷ್ಠಿ ದಿನಗಳಲ್ಲಿ ಮತ್ತು ಕಾರ್ತಿಕದ ಸ್ಕಂದ ಷಷ್ಠಿಯಂದು ಹೆಚ್ಚು ಜನ ಸೇರುತ್ತಾರೆ."
        ),
        openings = listOf(window("06:30", "12:00"), window("17:30", "20:30")),
        rituals = listOf(morningAbhisheka("07:00"), eveningAlankara("18:30"), archanaSeva(50)),
        festivals = listOf(skandaShashti, ganeshaChaturthi, karthikaDeepotsava),
        bus = bus("Hanumanthanagar / Kumaraswamy Temple"),
        metro = MetroInfo("National College", MetroLine.GREEN, 22),
        parking = streetParking,
        facilities = setOf(Facility.SHOE_STAND, Facility.DRINKING_WATER, Facility.PRASAD_COUNTER),
        dressCode = traditionalDress,
        busiestDays = setOf(DayOfWeek.TUESDAY),
        photos = listOf(
            commons(
                "LongStandingGopuram.jpg",
                t("The gopura", hi = "गोपुरम", kn = "ಗೋಪುರ"),
                "Shruthi V", "CC BY-SA 4.0"
            )
        ),
        nearby = listOf(templeGateStalls(), gandhiBazaar, vvPuramFoodStreet),
        donation = listOf(hundiDonation, sevaCounterDonation),
        accent = SubramanyaAccent,
        confidence = DataConfidence.MEDIUM
    ),

    Temple(
        id = "shringeri-shankara-math",
        name = t(
            "Shringeri Shankara Math",
            hi = "शृंगेरी शंकर मठ",
            kn = "ಶೃಂಗೇರಿ ಶಂಕರ ಮಠ"
        ),
        deity = Deity.DEVI,
        area = t("Shankarapuram", hi = "शंकरपुरम", kn = "ಶಂಕರಪುರಂ"),
        address = "Shankara Math Road, Shankarapuram, Basavanagudi, Bengaluru, Karnataka 560004",
        location = GeoPoint(12.94900, 77.57290),
        about = t(
            "The Bengaluru branch of the Sringeri Sharada Peetham, with shrines to Sharadamba " +
                "and Adi Shankara. Navaratri here is a nine-day programme of veda parayana and " +
                "evening concerts rather than a crowd event.",
            hi = "शृंगेरी शारदा पीठम की बेंगलुरु शाखा, शारदांबा और आदि शंकराचार्य के मंदिरों के साथ। " +
                "नवरात्रि में नौ दिन वेद पारायण और सांध्य संगीत कार्यक्रम होते हैं।",
            kn = "ಶೃಂಗೇರಿ ಶಾರದಾ ಪೀಠದ ಬೆಂಗಳೂರು ಶಾಖೆ; ಶಾರದಾಂಬೆ ಮತ್ತು ಆದಿ ಶಂಕರರ ಸನ್ನಿಧಿ ಇದೆ. " +
                "ನವರಾತ್ರಿಯಲ್ಲಿ ಒಂಬತ್ತು ದಿನ ವೇದ ಪಾರಾಯಣ ಮತ್ತು ಸಂಜೆ ಸಂಗೀತ ಕಾರ್ಯಕ್ರಮಗಳು."
        ),
        openings = listOf(window("06:30", "12:00"), window("17:00", "20:30")),
        rituals = listOf(
            morningAbhisheka("07:30"),
            middayMangalarati("12:00"),
            eveningAlankara("18:30")
        ),
        festivals = listOf(navaratri, ugadi, mahaShivaratri),
        bus = bus("Shankarapuram / National College"),
        metro = MetroInfo("National College", MetroLine.GREEN, 12),
        parking = streetParking,
        facilities = setOf(
            Facility.SHOE_STAND,
            Facility.DRINKING_WATER,
            Facility.RESTROOMS,
            Facility.BOOKSTORE,
            Facility.ANNADANA,
            Facility.MARRIAGE_HALL
        ),
        dressCode = strictTraditionalDress,
        nearby = listOf(gandhiBazaar, vidyarthiBhavan, mtr),
        donation = listOf(hundiDonation, sevaCounterDonation, annadanaDonation),
        accent = DeviAccent,
        confidence = DataConfidence.MEDIUM
    ),

    Temple(
        id = "karanji-anjaneya",
        name = t(
            "Karanji Anjaneya Temple",
            hi = "करंजी अंजनेय मंदिर",
            kn = "ಕರಂಜಿ ಆಂಜನೇಯ ದೇವಸ್ಥಾನ"
        ),
        deity = Deity.HANUMAN,
        area = t("Basavanagudi", hi = "बसवनगुड़ी", kn = "ಬಸವನಗುಡಿ"),
        address = "Hanumantha Nagar Main Road, Basavanagudi, Bengaluru, Karnataka 560019",
        // No verified pin for this one; the maps hand-off geocodes name + address instead.
        location = null,
        about = t(
            "A neighbourhood Anjaneya temple that fills up on Saturdays, when devotees do " +
                "pradakshina and offer vada malas. Traditionally associated with the Vyasaraja " +
                "line of Hanuman consecrations across old Bengaluru.",
            hi = "मोहल्ले का अंजनेय मंदिर, शनिवार को भक्तों से भरा रहता है — प्रदक्षिणा और वड़ा माला अर्पण होता है।",
            kn = "ಬಡಾವಣೆಯ ಆಂಜನೇಯ ದೇವಸ್ಥಾನ; ಶನಿವಾರ ಭಕ್ತರಿಂದ ತುಂಬಿರುತ್ತದೆ — ಪ್ರದಕ್ಷಿಣೆ ಮತ್ತು ವಡೆ ಮಾಲೆ ಸಮರ್ಪಣೆ."
        ),
        openings = listOf(window("06:00", "12:00"), window("17:00", "20:30")),
        rituals = listOf(
            ritual(t("Vada mala seva", hi = "वड़ा माला सेवा", kn = "ವಡೆ ಮಾಲೆ ಸೇವೆ"), priceInr = 300),
            eveningAlankara("18:30")
        ),
        festivals = listOf(hanumanJayanti, ramaNavami),
        bus = bus("Hanumanthanagar"),
        metro = MetroInfo("National College", MetroLine.GREEN, 15),
        parking = streetParking,
        facilities = setOf(Facility.SHOE_STAND, Facility.DRINKING_WATER),
        dressCode = traditionalDress,
        busiestDays = setOf(DayOfWeek.SATURDAY),
        nearby = listOf(gandhiBazaar, vidyarthiBhavan),
        donation = listOf(hundiDonation, sevaCounterDonation),
        accent = HanumanAccent,
        confidence = DataConfidence.LOW
    ),

    Temple(
        id = "ragigudda-anjaneya",
        name = t(
            "Ragigudda Sri Prasanna Anjaneyaswamy Temple",
            hi = "रागीगुड्डा श्री प्रसन्न अंजनेयस्वामी मंदिर",
            kn = "ರಾಗಿಗುಡ್ಡ ಶ್ರೀ ಪ್ರಸನ್ನ ಆಂಜನೇಯಸ್ವಾಮಿ ದೇವಸ್ಥಾನ"
        ),
        deity = Deity.HANUMAN,
        area = t("Jayanagar", hi = "जयनगर", kn = "ಜಯನಗರ"),
        address = "Ragigudda, 9th Block, Jayanagar, Bengaluru, Karnataka 560069",
        location = GeoPoint(12.91424, 77.59320),
        about = t(
            "Five acres on a rocky hillock — Anjaneya at the top, and a Shivalinga, Rama-Sita-" +
                "Lakshmana, Ganesha, the Navagrahas, Rajarajeshwari and a Trimurti panel carved " +
                "into the rock along the way up. One of the busiest Hanuman temples in the city " +
                "on Saturdays.",
            hi = "पाँच एकड़ की चट्टानी पहाड़ी — ऊपर अंजनेय, और रास्ते में शिवलिंग, राम-सीता-लक्ष्मण, गणेश, " +
                "नवग्रह, राजराजेश्वरी और चट्टान में तराशा त्रिमूर्ति पट। शनिवार को अत्यधिक भीड़।",
            kn = "ಐದು ಎಕರೆ ಬಂಡೆಗುಡ್ಡ — ಮೇಲೆ ಆಂಜನೇಯ, ದಾರಿಯಲ್ಲಿ ಶಿವಲಿಂಗ, ರಾಮ-ಸೀತಾ-ಲಕ್ಷ್ಮಣ, ಗಣೇಶ, " +
                "ನವಗ್ರಹ, ರಾಜರಾಜೇಶ್ವರಿ ಮತ್ತು ಬಂಡೆಯಲ್ಲಿ ಕೆತ್ತಿದ ತ್ರಿಮೂರ್ತಿ. ಶನಿವಾರ ಅತಿ ಹೆಚ್ಚು ಜನ."
        ),
        openings = listOf(window("06:00", "12:30"), window("16:30", "20:30")),
        rituals = listOf(
            morningAbhisheka("06:30"),
            ritual(t("Vada mala seva", hi = "वड़ा माला सेवा", kn = "ವಡೆ ಮಾಲೆ ಸೇವೆ"), priceInr = 500),
            eveningAlankara("18:30"),
            nightMangalarati("20:15")
        ),
        festivals = listOf(hanumanJayanti, ramaNavami, ganeshaChaturthi, karthikaDeepotsava),
        bus = bus("Ragigudda / Jayanagar 9th Block"),
        metro = MetroInfo("Ragigudda", MetroLine.YELLOW, 5),
        parking = ownParking,
        facilities = setOf(
            Facility.SHOE_STAND,
            Facility.DRINKING_WATER,
            Facility.RESTROOMS,
            Facility.PRASAD_COUNTER,
            Facility.ANNADANA,
            Facility.FREE_PARKING,
            Facility.QUEUE_SHELTER,
            Facility.MARRIAGE_HALL,
            Facility.WHEELCHAIR
        ),
        dressCode = traditionalDress,
        busiestDays = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
        photos = listOf(
            commons(
                "Ragigudda Anjaneya Temple.jpg",
                t("The temple on the hillock", hi = "पहाड़ी पर मंदिर", kn = "ಗುಡ್ಡದ ಮೇಲಿನ ದೇವಾಲಯ"),
                "Rkrish67", "Public domain"
            )
        ),
        nearby = listOf(
            templeGateStalls(GeoPoint(12.9143, 77.5934)),
            darshiniNearby(
                "Jayanagar 9th Block darshinis",
                darshiniNote,
                GeoPoint(12.9160, 77.5900)
            )
        ),
        donation = listOf(
            hundiDonation,
            sevaCounterDonation,
            annadanaDonation,
            onlineDonation("https://ragigudda.org")
        ),
        website = "https://ragigudda.org",
        accent = HanumanAccent,
        confidence = DataConfidence.HIGH
    ),

    Temple(
        id = "banashankari-amma",
        name = t(
            "Banashankari Amma Temple",
            hi = "बनशंकरी अम्मा मंदिर",
            kn = "ಬನಶಂಕರಿ ಅಮ್ಮ ದೇವಸ್ಥಾನ"
        ),
        deity = Deity.DEVI,
        area = t("Banashankari", hi = "बनशंकरी", kn = "ಬನಶಂಕರಿ"),
        address = "Banashankari Temple Road, Banashankari 1st Stage, Bengaluru, Karnataka 560050",
        location = GeoPoint(12.92500, 77.57320),
        about = t(
            "The city's best-known Banashankari shrine, and the temple the whole neighbourhood " +
                "is named after. Rahukala puja on Tuesday and Friday evenings is the draw — " +
                "devotees light lemon lamps during the inauspicious hour, which here is treated " +
                "as the most powerful time to ask the goddess for something.",
            hi = "शहर का सबसे प्रसिद्ध बनशंकरी मंदिर, जिसके नाम पर पूरा इलाक़ा है। मंगलवार और शुक्रवार " +
                "शाम की राहुकाल पूजा में भक्त नींबू के दीये जलाते हैं।",
            kn = "ನಗರದ ಅತ್ಯಂತ ಪ್ರಸಿದ್ಧ ಬನಶಂಕರಿ ದೇವಸ್ಥಾನ; ಇಡೀ ಬಡಾವಣೆಗೆ ಇದೇ ಹೆಸರು. ಮಂಗಳವಾರ ಮತ್ತು " +
                "ಶುಕ್ರವಾರ ಸಂಜೆಯ ರಾಹುಕಾಲ ಪೂಜೆಯಲ್ಲಿ ಭಕ್ತರು ನಿಂಬೆ ದೀಪ ಹಚ್ಚುತ್ತಾರೆ."
        ),
        openings = listOf(window("06:00", "13:00"), window("16:00", "21:00")),
        rituals = listOf(
            morningAbhisheka("07:00"),
            ritual(
                t("Rahukala puja (lemon lamps)", hi = "राहुकाल पूजा (नींबू दीप)", kn = "ರಾಹುಕಾಲ ಪೂಜೆ (ನಿಂಬೆ ದೀಪ)"),
                "16:30"
            ),
            eveningAlankara("19:00"),
            archanaSeva(50)
        ),
        festivals = listOf(navaratri, ugadi, karthikaDeepotsava),
        bus = bus("Banashankari Temple / Banashankari TTMC"),
        metro = MetroInfo("Banashankari", MetroLine.GREEN, 12),
        parking = streetParking,
        facilities = setOf(
            Facility.SHOE_STAND,
            Facility.DRINKING_WATER,
            Facility.RESTROOMS,
            Facility.PRASAD_COUNTER,
            Facility.QUEUE_SHELTER,
            Facility.ANNADANA
        ),
        dressCode = traditionalDress,
        busiestDays = setOf(DayOfWeek.TUESDAY, DayOfWeek.FRIDAY),
        nearby = listOf(
            NearbyPlace(
                kind = NearbyKind.POOJA_ITEMS,
                name = "Banashankari Temple Road stalls",
                note = t(
                    "Rows of stalls selling lemons, oil lamps, flowers and kumkuma specifically " +
                        "for the Rahukala puja.",
                    hi = "राहुकाल पूजा के लिए नींबू, दीये, फूल और कुमकुम बेचने वाली दुकानें।",
                    kn = "ರಾಹುಕಾಲ ಪೂಜೆಗೆ ಬೇಕಾದ ನಿಂಬೆ, ದೀಪ, ಹೂವು ಮತ್ತು ಕುಂಕುಮ ಮಾರುವ ಅಂಗಡಿಗಳು."
                ),
                location = GeoPoint(12.9252, 77.5734)
            ),
            darshiniNearby("Banashankari darshinis", darshiniNote, GeoPoint(12.9255, 77.5740))
        ),
        donation = listOf(hundiDonation, sevaCounterDonation, annadanaDonation, muzraiTrust),
        accent = DeviAccent,
        confidence = DataConfidence.MEDIUM
    ),

    Temple(
        id = "old-madiwala-someshwara",
        name = t(
            "Old Madiwala Someshwara Temple",
            hi = "पुराना मडीवाला सोमेश्वर मंदिर",
            kn = "ಹಳೆಯ ಮಡಿವಾಳ ಸೋಮೇಶ್ವರ ದೇವಸ್ಥಾನ"
        ),
        deity = Deity.SHIVA,
        area = t("Madiwala", hi = "मडीवाला", kn = "ಮಡಿವಾಳ"),
        address = "Old Madiwala, Madiwala, Bengaluru, Karnataka 560068",
        location = GeoPoint(12.91800, 77.61850),
        about = t(
            "A Chola-period temple with a swayambhu linga, carrying inscriptions dated 1247 CE " +
                "and 1365 CE on its basement — among the oldest dated records anywhere in the " +
                "city. Unusually for a temple this old in Bengaluru, it is still in good repair.",
            hi = "चोलकालीन मंदिर, स्वयंभू लिंग के साथ; इसकी नींव पर 1247 ई. और 1365 ई. के शिलालेख हैं — " +
                "शहर के सबसे पुराने तिथियुक्त अभिलेखों में से।",
            kn = "ಚೋಳರ ಕಾಲದ ದೇವಾಲಯ, ಸ್ವಯಂಭೂ ಲಿಂಗ; ಇದರ ತಳಪಾಯದಲ್ಲಿ ಕ್ರಿ.ಶ. 1247 ಮತ್ತು 1365ರ " +
                "ಶಾಸನಗಳಿವೆ — ನಗರದ ಅತ್ಯಂತ ಹಳೆಯ ದಿನಾಂಕಿತ ದಾಖಲೆಗಳಲ್ಲಿ ಒಂದು."
        ),
        builtIn = "1247 CE",
        openings = listOf(window("06:30", "12:00"), window("17:30", "20:30")),
        rituals = listOf(morningAbhisheka("07:30"), eveningAlankara("18:30")),
        festivals = listOf(mahaShivaratri, karthikaDeepotsava, ugadi),
        bus = bus("Madiwala Market / Madiwala Check Post"),
        metro = MetroInfo("BTM Layout", MetroLine.YELLOW, 20),
        parking = streetParking,
        facilities = setOf(Facility.SHOE_STAND, Facility.DRINKING_WATER),
        dressCode = traditionalDress,
        busiestDays = setOf(DayOfWeek.MONDAY),
        photos = listOf(
            commons(
                "Text of the 1247CE Inscriptions at the Madiwala Someshwara Temple, Bangalore.jpg",
                t(
                    "The 1247 CE inscription, as recorded in Epigraphia Carnatica",
                    hi = "एपिग्राफ़िया कार्नेटिका में दर्ज 1247 ई. का शिलालेख",
                    kn = "ಎಪಿಗ್ರಾಫಿಯಾ ಕರ್ನಾಟಿಕಾದಲ್ಲಿ ದಾಖಲಾದ ಕ್ರಿ.ಶ. 1247ರ ಶಾಸನ"
                ),
                "Rice, Benjamin Lewis", "Public domain"
            )
        ),
        nearby = listOf(
            templeGateStalls(),
            darshiniNearby("Madiwala Market darshinis", darshiniNote, GeoPoint(12.9185, 77.6180))
        ),
        donation = listOf(hundiDonation, muzraiTrust),
        accent = ShivaAccent,
        confidence = DataConfidence.MEDIUM
    ),

    Temple(
        id = "vasantha-vallabharaya",
        name = t(
            "Vasantha Vallabharaya Temple",
            hi = "वसंत वल्लभराय मंदिर",
            kn = "ವಸಂತ ವಲ್ಲಭರಾಯ ದೇವಸ್ಥಾನ"
        ),
        deity = Deity.VISHNU,
        area = t("Vasanthapura", hi = "वसंतपुरा", kn = "ವಸಂತಪುರ"),
        address = "Vasanthapura, Uttarahalli, Bengaluru, Karnataka 560061",
        location = GeoPoint(12.89546, 77.55187),
        about = t(
            "A Chola-era Vishnu temple on the southern edge of the city, at the foot of the " +
                "Turahalli scrub forest. The annual Brahmotsava in Chaitra draws villages from " +
                "the whole Uttarahalli belt.",
            hi = "शहर के दक्षिणी छोर पर, तुराहल्ली वन के पास चोलकालीन विष्णु मंदिर। चैत्र के " +
                "ब्रह्मोत्सव में आसपास के पूरे इलाक़े से लोग आते हैं।",
            kn = "ನಗರದ ದಕ್ಷಿಣ ತುದಿಯಲ್ಲಿ, ತುರಹಳ್ಳಿ ಕಾಡಿನ ಬುಡದಲ್ಲಿ ಚೋಳರ ಕಾಲದ ವಿಷ್ಣು ದೇವಾಲಯ. " +
                "ಚೈತ್ರ ಮಾಸದ ಬ್ರಹ್ಮೋತ್ಸವಕ್ಕೆ ಸುತ್ತಲಿನ ಹಳ್ಳಿಗಳಿಂದ ಜನ ಬರುತ್ತಾರೆ."
        ),
        builtIn = "Chola period",
        openings = listOf(window("07:00", "11:30"), window("18:00", "20:00")),
        rituals = listOf(morningAbhisheka("08:00"), eveningAlankara("18:30")),
        festivals = listOf(
            Festival(
                name = t("Brahmotsava", hi = "ब्रह्मोत्सव", kn = "ಬ್ರಹ್ಮೋತ್ಸವ"),
                whenApprox = t("Mar–Apr (Chaitra)", hi = "मार्च–अप्रैल (चैत्र)", kn = "ಮಾರ್ಚ್–ಏಪ್ರಿಲ್ (ಚೈತ್ರ)"),
                usualMonth = 4
            ),
            vaikunthaEkadashi
        ),
        bus = bus("Vasanthapura / Uttarahalli"),
        metro = MetroInfo("Vajarahalli", MetroLine.GREEN, 20),
        parking = streetParking,
        facilities = setOf(Facility.SHOE_STAND, Facility.DRINKING_WATER),
        dressCode = traditionalDress,
        nearby = listOf(templeGateStalls()),
        donation = listOf(hundiDonation, muzraiTrust),
        accent = VishnuAccent,
        confidence = DataConfidence.MEDIUM
    ),

    Temple(
        id = "hulimavu-cave",
        name = t(
            "Hulimavu Cave Temple",
            hi = "हुलिमावु गुफा मंदिर",
            kn = "ಹುಳಿಮಾವು ಗುಹಾ ದೇವಸ್ಥಾನ"
        ),
        deity = Deity.SHIVA,
        area = t("Hulimavu", hi = "हुलिमावु", kn = "ಹುಳಿಮಾವು"),
        address = "Hulimavu, Bannerghatta Road, Bengaluru, Karnataka 560076",
        location = GeoPoint(12.87701, 77.59967),
        about = t(
            "A natural cave off Bannerghatta Road with a Shivalinga at the centre and Devi and " +
                "Ganesha on either side, plus an old dhyana mantapa and the samadhi of Sri " +
                "Ramanand Swamiji. Administered by the Sri Sri Bala Gangadharaswami Mutt.",
            hi = "बन्नेरघट्टा रोड के पास एक प्राकृतिक गुफा — बीच में शिवलिंग, दोनों ओर देवी और गणेश, " +
                "साथ ही पुराना ध्यान मंटप और श्री रामानंद स्वामीजी की समाधि।",
            kn = "ಬನ್ನೇರುಘಟ್ಟ ರಸ್ತೆಯ ಬಳಿ ನೈಸರ್ಗಿಕ ಗುಹೆ — ಮಧ್ಯದಲ್ಲಿ ಶಿವಲಿಂಗ, ಎರಡೂ ಬದಿ ದೇವಿ ಮತ್ತು ಗಣೇಶ, " +
                "ಜೊತೆಗೆ ಹಳೆಯ ಧ್ಯಾನ ಮಂಟಪ ಮತ್ತು ಶ್ರೀ ರಾಮಾನಂದ ಸ್ವಾಮೀಜಿಯವರ ಸಮಾಧಿ."
        ),
        openings = listOf(window("06:30", "12:00"), window("17:00", "20:00")),
        rituals = listOf(morningAbhisheka("07:30"), eveningAlankara("18:30")),
        festivals = listOf(mahaShivaratri, karthikaDeepotsava),
        bus = bus("Hulimavu / Meenakshi Temple, Bannerghatta Road"),
        parking = ownParking,
        facilities = setOf(
            Facility.SHOE_STAND,
            Facility.DRINKING_WATER,
            Facility.FREE_PARKING,
            Facility.NO_PHOTOGRAPHY
        ),
        dressCode = traditionalDress,
        photos = listOf(
            commons(
                "Cave Temple Entrance.jpg",
                t("The cave entrance", hi = "गुफा का प्रवेश", kn = "ಗುಹೆಯ ಪ್ರವೇಶ"),
                "Chaitanya", "CC BY-SA 4.0"
            )
        ),
        nearby = listOf(
            templeGateStalls(),
            darshiniNearby("Bannerghatta Road darshinis", darshiniNote, GeoPoint(12.8790, 77.5990))
        ),
        donation = listOf(hundiDonation),
        accent = ShivaAccent,
        confidence = DataConfidence.MEDIUM
    )
)
