package com.example.composelearning.adaptive

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Static sample data shared by every adaptive demo. Kept deterministic (no
 * network, no random) so the demos render identically across window sizes and
 * configuration changes — which is exactly what you want when you are learning
 * to reason about layout, not data.
 */

@Immutable
data class Email(
    val id: Int,
    val sender: String,
    val subject: String,
    val preview: String,
    val body: String,
    val avatarColor: Color
)

val sampleEmails: List<Email> = listOf(
    Email(
        1,
        "Maya Chen",
        "Q3 design review notes",
        "Thanks everyone for joining — here is the summary of what we agreed…",
        "Thanks everyone for joining the Q3 design review.\n\nWe agreed to ship the new onboarding flow behind a flag, " +
            "revisit the empty states next sprint, and align on the adaptive layout breakpoints (compact / medium / " +
            "expanded) before handing off to engineering.\n\nAction items are in the shared doc.",
        Color(0xFF6750A4)
    ),
    Email(
        2,
        "GitHub",
        "[composelearning] PR #482 ready for review",
        "Raghunandan opened a pull request: Add adaptive layout samples…",
        "Raghunandan opened a pull request.\n\nAdd adaptive layout samples covering list-detail, supporting pane, " +
            "adaptive grids and a reflowing detail screen driven by WindowSizeClass.\n\n6 files changed, +740 −0.",
        Color(0xFF1F2328)
    ),
    Email(
        3,
        "Priya Nair",
        "Lunch tomorrow?",
        "Are you free around 1pm? There is a new place near the office I wanted to try…",
        "Are you free around 1pm tomorrow?\n\nThere is a new place near the office I wanted to try — supposedly great " +
            "filter coffee. Let me know and I will book a table.",
        Color(0xFFB3261E)
    ),
    Email(
        4,
        "Android Developers",
        "What's new in Compose — adaptive layouts",
        "Build apps that look great on phones, foldables, tablets and desktop with a single codebase…",
        "Build apps that look great on phones, foldables, tablets and Chromebooks with one codebase.\n\nThe " +
            "material3-adaptive libraries give you ListDetailPaneScaffold, SupportingPaneScaffold and " +
            "WindowSizeClass so you can react to the space you actually have.",
        Color(0xFF3DDC84)
    ),
    Email(
        5,
        "Banking",
        "Your monthly statement is ready",
        "Your statement for June is now available to view in the app…",
        "Your statement for June is now available.\n\nNo action is required. You can view the full breakdown of " +
            "transactions, balances and upcoming payments in the app.",
        Color(0xFF00658F)
    ),
    Email(
        6,
        "Liam O'Brien",
        "Re: Tablet layout feedback",
        "On the expanded width the detail pane feels a little cramped — could we…",
        "On the expanded width the detail pane feels a little cramped — could we give it a bit more horizontal " +
            "padding and cap the text measure at ~70 characters? Reads much better on a 12\" tablet.",
        Color(0xFF7D5260)
    ),
    Email(
        7,
        "Calendar",
        "Reminder: Sprint planning at 10:00",
        "Sprint planning starts in 30 minutes in the Olympus room…",
        "Sprint planning starts in 30 minutes in the Olympus room.\n\nAgenda: review the adaptive layout spike, " +
            "estimate the foldable support work, and confirm the release cut date.",
        Color(0xFF386A20)
    ),
    Email(
        8,
        "Sofia Rossi",
        "Photos from the offsite",
        "Shared an album with you — 24 photos from the team offsite last week…",
        "Shared an album with you — 24 photos from the team offsite last week.\n\nThe group shot on the rooftop came " +
            "out really well. Feel free to add your own and tag people.",
        Color(0xFF984061)
    )
)

@Immutable
data class Photo(
    val id: Int,
    val title: String,
    val color: Color,
    val aspect: Float
)

/** A small gallery. Colors stand in for real images so the demo needs no network. */
val samplePhotos: List<Photo> = List(30) { index ->
    val palette = listOf(
        Color(0xFF6750A4), Color(0xFF00658F), Color(0xFFB3261E), Color(0xFF386A20),
        Color(0xFF7D5260), Color(0xFF984061), Color(0xFF8C4A00), Color(0xFF006A60)
    )
    Photo(
        id = index,
        title = "Frame ${index + 1}",
        color = palette[index % palette.size],
        aspect = listOf(1f, 0.75f, 1.33f, 1f)[index % 4]
    )
}

@Immutable
data class ProductSpec(val label: String, val value: String)

@Immutable
data class Product(
    val name: String,
    val tagline: String,
    val price: String,
    val accent: Color,
    val description: String,
    val specs: List<ProductSpec>
)

val sampleProduct = Product(
    name = "Aurora Foldable",
    tagline = "One device. Every size class.",
    price = "₹1,29,999",
    accent = Color(0xFF6750A4),
    description = "A foldable that goes from a 6.2\" cover display (compact) to a 7.6\" inner screen (expanded) — the " +
        "perfect device to test adaptive layouts on. Your UI should reflow as the user unfolds it, without losing " +
        "scroll position or selection state.",
    specs = listOf(
        ProductSpec("Cover display", "6.2\" • 120Hz • compact width"),
        ProductSpec("Inner display", "7.6\" • 120Hz • expanded width"),
        ProductSpec("Fold posture", "Reported via WindowAdaptiveInfo"),
        ProductSpec("Chip", "Snapdragon 8 Elite"),
        ProductSpec("RAM", "12 GB"),
        ProductSpec("Storage", "512 GB"),
        ProductSpec("Battery", "4400 mAh"),
        ProductSpec("Weight", "239 g")
    )
)

@Immutable
data class ReachInfo(
    val road: String,
    val transport: String,
    val ownVehicle: String
)

@Immutable
data class Facilities(
    val pooja: String,
    val prasad: String,
    val general: List<String>
)

@Immutable
data class Temple(
    val id: Int,
    val name: String,
    val location: String,
    val timings: String,
    val reach: ReachInfo,
    val facilities: Facilities,
    val description: String,
    val accentColor: Color
)

val sampleTemples: List<Temple> = listOf(
    Temple(
        id = 1,
        name = "ISKCON Temple Bangalore",
        location = "Hare Krishna Hill, Chord Road, Rajajinagar",
        timings = "Mon–Fri: 7:15 AM – 1:00 PM, 4:15 PM – 8:20 PM; Sat, Sun & Holidays: 7:15 AM – 8:30 PM",
        reach = ReachInfo(
            road = "Well connected via Chord Road and West of Chord Road.",
            transport = "Mahalakshmi Metro Station (Green Line) is 500m away. Direct BMTC buses from Majestic and Shivajinagar.",
            ownVehicle = "Paid multi-level parking is available within the temple complex."
        ),
        facilities = Facilities(
            pooja = "Regular Aratis including Mangala, Rajbhog, and Sandhya Arati. Special Sevas can be booked.",
            prasad = "Free Kichadi Prasad is distributed daily to all visitors. Higher-end Sattvic food at Govinda's restaurant.",
            general = listOf("Wheelchair access", "Elevators", "Restrooms", "Drinking water", "Bookstore")
        ),
        description = "One of the largest ISKCON centers in the world, famous for its neo-classical architecture and spiritual vibrancy.",
        accentColor = Color(0xFF6750A4)
    ),
    Temple(
        id = 2,
        name = "Bull Temple (Dodda Basavana Gudi)",
        location = "Bull Temple Road, Basavanagudi",
        timings = "6:00 AM – 8:00 PM daily",
        reach = ReachInfo(
            road = "Centrally located in Basavanagudi, easy access from South Bangalore.",
            transport = "South End Circle Metro is ~2km away. Multiple BMTC buses (210 series) stop right outside.",
            ownVehicle = "Street parking available on Bull Temple Road and near Bugle Rock Park."
        ),
        facilities = Facilities(
            pooja = "Special Abhisheka for the Nandi idol. Annual Kadalekai Parishe (Groundnut Fair) is a major event.",
            prasad = "Peanuts offered to Nandi are distributed as prasad during the fair. Regular kumkum/vibhuti prasad.",
            general = listOf("Park adjacent", "Historic site", "Ganesha temple nearby", "Shoe stand")
        ),
        description = "Features a massive 4.5m tall monolithic Nandi idol carved out of a single granite rock, built in the 16th century.",
        accentColor = Color(0xFFB3261E)
    ),
    Temple(
        id = 3,
        name = "Sri Banashankari Amma Temple",
        location = "Kanakapura Road, Banashankari",
        timings = "6:00 AM – 1:30 PM, 4:30 PM – 9:00 PM",
        reach = ReachInfo(
            road = "Located on the busy Kanakapura main road.",
            transport = "Banashankari Metro Station is a 5-min walk. Major BMTC hub with buses to all parts of the city.",
            ownVehicle = "Limited parking on side streets; can be difficult on Tuesdays and Fridays."
        ),
        facilities = Facilities(
            pooja = "Famous for Rahu Kala Deepotsava (lamps in lemon peels). Tuesdays, Fridays, and Sundays are special.",
            prasad = "Free Annadana (Lunch) is served on Fridays for devotees.",
            general = listOf("Shoe stand", "Flower stalls", "Busy market area", "Restrooms")
        ),
        description = "A unique temple where the deity is worshipped during Rahu Kala, traditionally considered inauspicious.",
        accentColor = Color(0xFF386A20)
    ),
    Temple(
        id = 4,
        name = "Gavi Gangadhareshwara Temple",
        location = "Gavipuram Extention, Kempegowda Nagar",
        timings = "6:00 AM – 12:30 PM, 5:00 PM – 8:30 PM",
        reach = ReachInfo(
            road = "Near Basavanagudi, accessible via KR Road.",
            transport = "National College Metro Station is ~2km away. Buses to Gavipuram stop nearby.",
            ownVehicle = "Street parking available in the residential Gavipuram area."
        ),
        facilities = Facilities(
            pooja = "Ancient Shiva temple. Makar Sankranti sees natural light illuminating the Linga through the cave.",
            prasad = "Vibhuti and Kumkum prasad provided.",
            general = listOf("Cave experience", "Archaeological site", "Monolithic pillars")
        ),
        description = "A 16th-century cave temple famous for its astronomical precision and monolithic stone discs.",
        accentColor = Color(0xFF00658F)
    ),
    Temple(
        id = 5,
        name = "Puri Jagannath Temple (HSR)",
        location = "Agara Village, 1st Sector, HSR Layout",
        timings = "6:00 AM – 12:30 PM, 4:00 PM – 9:00 PM",
        reach = ReachInfo(
            road = "Near Agara Flyover, easily accessible from Outer Ring Road.",
            transport = "Agara Bus Stop is right next to the temple. Nearest Metro: RV Road (Green Line).",
            ownVehicle = "Limited parking near the entrance; very busy during weekends."
        ),
        facilities = Facilities(
            pooja = "Traditional Odiya style rituals. Famous for the annual Rath Yatra (Chariot Festival).",
            prasad = "Mahaprasad is served daily between 1:00 PM – 3:00 PM (pre-booking required).",
            general = listOf("Community hall", "Puja stalls", "Odiya cultural hub")
        ),
        description = "A beautiful sandstone replica of the iconic Jagannath Temple in Puri, serving as a spiritual home for the Odiya community.",
        accentColor = Color(0xFF8C4A00)
    ),
    Temple(
        id = 6,
        name = "Kadu Malleshwara Temple",
        location = "15th Cross, Malleswaram",
        timings = "7:00 AM – 12:00 PM, 5:30 PM – 9:00 PM",
        reach = ReachInfo(
            road = "Located in the heart of Malleswaram, near 15th Cross.",
            transport = "Malleswaram Metro Station (Green Line) is nearby. Well connected by BMTC.",
            ownVehicle = "Limited street parking in the residential Malleswaram area."
        ),
        facilities = Facilities(
            pooja = "Ancient Shiva temple. The Nandishwara Teertha opposite features a unique natural spring.",
            prasad = "Regular vibhuti and kumkum prasad.",
            general = listOf("Historic site", "Meditation space", "Quiet environment")
        ),
        description = "A 17th-century temple built by Venkoji, the stepbrother of Shivaji. It gives Malleswaram its name.",
        accentColor = Color(0xFF006A60)
    ),
    Temple(
        id = 7,
        name = "Someshwara Temple (Halasuru)",
        location = "Ulsoor Road, Someshwarpura, Halasuru",
        timings = "6:00 AM – 12:30 PM, 5:30 PM – 9:00 PM",
        reach = ReachInfo(
            road = "Easy access from MG Road and Old Madras Road.",
            transport = "Halasuru Metro Station (Purple Line) is 600m away.",
            ownVehicle = "Street parking available but limited due to proximity to MG Road."
        ),
        facilities = Facilities(
            pooja = "Historic Chola and Vijayanagara era temple rituals. Grand Mahashivaratri celebrations.",
            prasad = "Traditional vibhuti prasad.",
            general = listOf("Heritage site", "Intricate carvings", "Tall gopuram")
        ),
        description = "One of the oldest temples in Bangalore, showcasing stunning Chola architecture and historic stone inscriptions.",
        accentColor = Color(0xFF6750A4)
    ),
    Temple(
        id = 8,
        name = "Dodda Ganapathi Temple",
        location = "Bull Temple Road, Basavanagudi",
        timings = "6:30 AM – 1:00 PM, 4:30 PM – 8:00 PM",
        reach = ReachInfo(
            road = "Adjacent to the Bull Temple in Basavanagudi.",
            transport = "National College Metro Station (Green Line) + 2km by auto.",
            ownVehicle = "Parking shared with Bull Temple and Bugle Rock Park."
        ),
        facilities = Facilities(
            pooja = "Famous for 'Benne Alankara' (butter coating) for the massive Ganesha idol.",
            prasad = "Modaka and other sweets offered to Ganesha.",
            general = listOf("Shoe stand", "Gift stalls", "Park nearby")
        ),
        description = "Houses a giant 18-foot monolithic Ganesha idol, highly revered for granting wishes.",
        accentColor = Color(0xFFB3261E)
    ),
    Temple(
        id = 9,
        name = "Shivoham Shiva Temple",
        location = "Old Airport Road, Murugeshpalya",
        timings = "Open 24 Hours",
        reach = ReachInfo(
            road = "Behind Kemp Fort Mall on Old Airport Road.",
            transport = "BMTC buses towards HAL/Marathahalli stop nearby. Nearest Metro: Indiranagar.",
            ownVehicle = "Parking available in Kemp Fort Mall parking lot."
        ),
        facilities = Facilities(
            pooja = "Special Shiva Puja, Jyotirlinga Yatra replica, and light & sound shows.",
            prasad = "Sacred water from the 'Ganges' replica.",
            general = listOf("24/7 Access", "Light show", "Souvenir shop", "Wheelchair accessible")
        ),
        description = "Known for its magnificent 65-foot-tall white statue of Lord Shiva seated in a meditative pose.",
        accentColor = Color(0xFF00658F)
    ),
    Temple(
        id = 10,
        name = "Ragigudda Anjaneya Temple",
        location = "Jayanagar 9th Block",
        timings = "6:00 AM – 12:00 PM, 4:30 PM – 8:30 PM",
        reach = ReachInfo(
            road = "Situated on a hillock in Jayanagar 9th Block.",
            transport = "Short auto ride from Jayanagar Metro Station. Many buses stop at 9th Block.",
            ownVehicle = "Spacious dedicated parking area at the base of the hill."
        ),
        facilities = Facilities(
            pooja = "Dedicated to Lord Hanuman. Features large rock carvings of the Hindu Trinity.",
            prasad = "Light snacks and traditional prasad served in the dining area.",
            general = listOf("Amphitheater", "Hilltop view", "Drinking water", "Large hall")
        ),
        description = "A vast hilltop temple complex offering a serene atmosphere and panoramic views of South Bangalore.",
        accentColor = Color(0xFF386A20)
    ),
    Temple(
        id = 11,
        name = "Shrungagiri Shanmukha Temple",
        location = "Rajarajeshwari Nagar",
        timings = "6:30 AM – 12:30 PM, 4:30 PM – 9:00 PM",
        reach = ReachInfo(
            road = "Located on a hill in RR Nagar, off Mysore Road.",
            transport = "Rajarajeshwari Nagar Metro Station (Purple Line) + short auto ride.",
            ownVehicle = "Parking available along the approach road and at the hilltop."
        ),
        facilities = Facilities(
            pooja = "Unique tower with six faces of Shanmukha. Special Skanda Sashti celebrations.",
            prasad = "Traditional South Indian temple prasad.",
            general = listOf("Crystal dome", "Panoramic views", "Well-maintained steps")
        ),
        description = "A striking modern temple known for its crystal dome and the six-faced tower of Lord Shanmukha.",
        accentColor = Color(0xFF7D5260)
    ),
    Temple(
        id = 12,
        name = "Chokkanathaswamy Temple",
        location = "5th Cross Road, Domlur",
        timings = "6:00 AM – 11:00 AM, 5:45 PM – 8:30 PM",
        reach = ReachInfo(
            road = "In the Domlur inner area, opposite Sony World.",
            transport = "Indiranagar or Trinity Metro Station + auto. Domlur bus stand is close.",
            ownVehicle = "Very difficult; street parking is almost non-existent."
        ),
        facilities = Facilities(
            pooja = "Ancient Chola rituals. Known for 12 'Pranic' energy points for meditation.",
            prasad = "Vibhuti and Kumkum prasad.",
            general = listOf("Heritage site", "Tamil inscriptions", "Meditation spots")
        ),
        description = "A 10th-century Chola temple dedicated to Lord Vishnu, one of the oldest in the city.",
        accentColor = Color(0xFF984061)
    ),
    Temple(
        id = 13,
        name = "Dharmaraya Swamy Temple",
        location = "Old Taluk Cutchery Road, Nagarathpete",
        timings = "5:30 AM – 11:30 AM, 4:00 PM – 8:30 PM",
        reach = ReachInfo(
            road = "In the heart of the old city (Pete area).",
            transport = "KR Market or Chickpet Metro Station. Accessible by walk from Town Hall.",
            ownVehicle = "Impossible; use public transport as roads are extremely narrow."
        ),
        facilities = Facilities(
            pooja = "Only temple in India dedicated to the Pandavas and Draupadi. Hub of Bangalore Karaga.",
            prasad = "Traditional sweet pongal prasad during festivals.",
            general = listOf("Historic center", "Karaga festival site", "Busy market surroundings")
        ),
        description = "The epicenter of the historic Bangalore Karaga festival, uniquely dedicated to the Pandavas.",
        accentColor = Color(0xFF8C4A00)
    ),
    Temple(
        id = 14,
        name = "Kote Venkataramana Temple",
        location = "KR Road, Kalasipalya",
        timings = "8:00 AM – 12:00 PM, 6:00 PM – 8:30 PM",
        reach = ReachInfo(
            road = "Next to Tipu Sultan's Summer Palace.",
            transport = "KR Market Metro Station (Green Line) is a 5-min walk.",
            ownVehicle = "Limited; visitors often park near KR Market or Victoria Hospital."
        ),
        facilities = Facilities(
            pooja = "Vijayanagara style worship. Vaikuntha Ekadashi is the biggest annual event.",
            prasad = "Sacred teertha and tulsi prasad.",
            general = listOf("Heritage site", "Stone carvings", "Palace adjacent")
        ),
        description = "A 17th-century masterpiece of Dravidian stone carving, miraculously survived many sieges.",
        accentColor = Color(0xFF006A60)
    ),
    Temple(
        id = 15,
        name = "Madiwala Ayyappa Temple",
        location = "1st Stage, BTM Layout",
        timings = "6:30 AM – 11:30 AM, 5:30 PM – 8:30 PM",
        reach = ReachInfo(
            road = "Near Madiwala Checkpost and Silk Board.",
            transport = "Extremely well connected by BMTC buses from across the city.",
            ownVehicle = "Limited street parking available in the inner BTM streets."
        ),
        facilities = Facilities(
            pooja = "Kerala style Ayyappa rituals. Special Padi Puja during Mandala season.",
            prasad = "Aravana Payasam and Appam (during peak season).",
            general = listOf("Kerala architecture", "Devotional bookstore", "Puja stalls")
        ),
        description = "Built in typical Kerala style, this temple is a major hub for Ayyappa devotees in South Bangalore.",
        accentColor = Color(0xFF6750A4)
    ),
    Temple(
        id = 16,
        name = "Sugreeva Venkateshwara Temple",
        location = "Balepete Main Road",
        timings = "6:00 AM – 11:30 AM, 3:00 PM – 8:30 PM",
        reach = ReachInfo(
            road = "In the Balepete market area, near Majestic.",
            transport = "Nadaprabhu Kempegowda (Majestic) Station is within walking distance.",
            ownVehicle = "No car parking; strictly use public transport or walk from Majestic."
        ),
        facilities = Facilities(
            pooja = "Rare temple dedicated to Sugreeva. Unique 6-foot idol with fangs.",
            prasad = "Vibhuti and Kumkum.",
            general = listOf("Rare deity", "Historic Pete area", "Market location")
        ),
        description = "A unique shrine dedicated to the monkey king Sugreeva, featuring a rare and powerful idol.",
        accentColor = Color(0xFFB3261E)
    ),
    Temple(
        id = 17,
        name = "Dakshinamukha Nandi Tirtha",
        location = "15th Cross, Malleswaram",
        timings = "7:30 AM – 12:00 PM, 5:00 PM – 8:30 PM",
        reach = ReachInfo(
            road = "Directly opposite the Kadu Malleshwara temple.",
            transport = "Same as Kadu Malleshwara (Malleswaram Metro).",
            ownVehicle = "Street parking shared with Kadu Malleshwara."
        ),
        facilities = Facilities(
            pooja = "Unique site where water flows from a Nandi idol's mouth onto a Shiva Linga.",
            prasad = "Teertha (sacred water) from the natural spring.",
            general = listOf("Natural spring", "Archaeological site", "Peaceful")
        ),
        description = "A small but fascinating temple where a natural underground spring feeds a continuous flow over the Linga.",
        accentColor = Color(0xFF00658F)
    ),
    Temple(
        id = 18,
        name = "Sri Raja Rajeshwari Temple",
        location = "Rajarajeshwari Nagar",
        timings = "6:00 AM – 12:30 PM, 4:30 PM – 8:30 PM",
        reach = ReachInfo(
            road = "Off Mysore Road, in the heart of RR Nagar.",
            transport = "RR Nagar Metro Station + short auto ride.",
            ownVehicle = "Dedicated parking area available near the temple complex."
        ),
        facilities = Facilities(
            pooja = "Grand Dravidian rituals. Navaratri is celebrated with extreme fervor.",
            prasad = "Daily lunch (Annadana) for devotees.",
            general = listOf("Intricate carvings", "Large complex", "Restrooms")
        ),
        description = "A majestic temple known for its traditional Dravidian architecture and the beautiful idol of the Goddess.",
        accentColor = Color(0xFF386A20)
    ),
    Temple(
        id = 19,
        name = "Nimishamba Devi Temple",
        location = "Ideal Homes, RR Nagar",
        timings = "6:30 AM – 1:00 PM, 4:00 PM – 8:30 PM",
        reach = ReachInfo(
            road = "Located in the quiet Ideal Homes area of RR Nagar.",
            transport = "Auto from RR Nagar Metro Station.",
            ownVehicle = "Street parking available in the wide residential streets."
        ),
        facilities = Facilities(
            pooja = "Believed to answer prayers in a minute ('Nimisha'). Special lemon garland offerings.",
            prasad = "Saffron-colored kumkum and sacred thread.",
            general = listOf("Quiet neighborhood", "Well maintained", "Small stalls")
        ),
        description = "A popular local replica of the famous Srirangapatna shrine, dedicated to the 'minute' goddess.",
        accentColor = Color(0xFF7D5260)
    ),
    Temple(
        id = 20,
        name = "Dwadasha Jyotirlinga Temple",
        location = "Omkar Hills, Srinivasapura",
        timings = "7:00 AM – 12:30 PM, 4:30 PM – 8:00 PM",
        reach = ReachInfo(
            road = "Near Kengeri, on the scenic Omkar Hills.",
            transport = "Auto from Kengeri Metro Station. Few buses reach the base of the hill.",
            ownVehicle = "Ample parking available at the temple premises."
        ),
        facilities = Facilities(
            pooja = "Houses replicas of all 12 Jyotirlingas. Giant tower clock.",
            prasad = "Traditional Shaivite prasad.",
            general = listOf("Hilltop view", "Giant clock", "12 Shrines", "Peaceful")
        ),
        description = "A unique spiritual site on Omkar Hills featuring replicas of all 12 Jyotirlingas from across India.",
        accentColor = Color(0xFF984061)
    ),
    Temple(
        id = 21,
        name = "Vasantha Vallabharaya Temple",
        location = "Vasanthapura, South Bangalore",
        timings = "7:00 AM – 12:00 PM, 5:30 PM – 8:30 PM",
        reach = ReachInfo(
            road = "Off Kanakapura Road, near Banashankari.",
            transport = "Auto from Banashankari or Doddakallasandra Metro Station.",
            ownVehicle = "Parking available at the base of the hilltop temple."
        ),
        facilities = Facilities(
            pooja = "Ancient Chola era rituals for Lord Vishnu. Features a sacred temple tank (Kalyani).",
            prasad = "Puliyogare and Sakkare Pongal.",
            general = listOf("Hilltop temple", "Ancient Kalyani", "Quiet area")
        ),
        description = "A historic hilltop temple with a beautiful sacred tank, dating back to the Chola and Vijayanagara periods.",
        accentColor = Color(0xFF8C4A00)
    ),
    Temple(
        id = 22,
        name = "Suryanarayana Temple",
        location = "HAL 2nd Stage, Domlur",
        timings = "6:00 AM – 12:30 PM, 5:00 PM – 8:30 PM",
        reach = ReachInfo(
            road = "Near Domlur Bridge, HAL 2nd Stage.",
            transport = "Indiranagar Metro + auto. Well connected by buses to HAL.",
            ownVehicle = "Limited parking in the surrounding residential lanes."
        ),
        facilities = Facilities(
            pooja = "Rare temple dedicated to the Sun God. Special Surya Namaskara sessions.",
            prasad = "Wheat-based offerings to the Sun.",
            general = listOf("Chola style", "Unique deity", "Well maintained")
        ),
        description = "One of the few temples in Bangalore dedicated to Lord Suryanarayana, featuring elegant stone architecture.",
        accentColor = Color(0xFF006A60)
    ),
    Temple(
        id = 23,
        name = "Annamma Devi Temple",
        location = "Subedar Chatram Road, Gandhinagar",
        timings = "6:00 AM – 9:00 PM",
        reach = ReachInfo(
            road = "Near Majestic Bus Stand and Railway Station.",
            transport = "Walkable from Majestic (Nadaprabhu Kempegowda) Metro Station.",
            ownVehicle = "Extremely difficult; use the multi-level parking at Freedom Park or nearby malls."
        ),
        facilities = Facilities(
            pooja = "Dedicated to the guardian deity of Bangalore. Highly powerful rituals.",
            prasad = "Vermilion and sacred turmeric.",
            general = listOf("Central location", "Highly crowded", "Historical significance")
        ),
        description = "The shrine of the presiding deity of Bangalore city, a place of immense faith for locals.",
        accentColor = Color(0xFF6750A4)
    ),
    Temple(
        id = 24,
        name = "Sri Ranganathaswamy Temple",
        location = "Rangswamy Temple St, Chickpet",
        timings = "7:00 AM – 12:00 PM, 5:30 PM – 8:30 PM",
        reach = ReachInfo(
            road = "In the dense Chickpet/Balepete area.",
            transport = "Chickpet or Majestic Metro Station followed by a walk.",
            ownVehicle = "Strictly no cars; best reached by walk or two-wheeler."
        ),
        facilities = Facilities(
            pooja = "16th-century Vijayanagara rituals. Famous for its tall and ancient Garuda Stambha.",
            prasad = "Traditional Vaishnava prasad.",
            general = listOf("Ancient pillars", "Market hub", "Quiet interior")
        ),
        description = "A hidden gem of Vijayanagara architecture in the heart of the city's oldest market district.",
        accentColor = Color(0xFFB3261E)
    ),
    Temple(
        id = 25,
        name = "Begur Nageshwara Temple",
        location = "Begur, South Bangalore",
        timings = "6:00 AM – 12:00 PM, 5:30 PM – 8:30 PM",
        reach = ReachInfo(
            road = "Off Hosur Road, near Bommanahalli.",
            transport = "Buses to Begur from Majestic or Market. Nearest Metro: Bommanahalli (upcoming).",
            ownVehicle = "Street parking available in the Begur village area."
        ),
        facilities = Facilities(
            pooja = "9th-century rituals. Houses the inscription that first mentions 'Bengaluru'.",
            prasad = "Traditional vibhuti.",
            general = listOf("Archaeological site", "Pancha Lingas", "Ancient inscriptions")
        ),
        description = "A site of immense historical value, housing the 9th-century stone inscription naming the city.",
        accentColor = Color(0xFF00658F)
    ),
    Temple(
        id = 26,
        name = "Hulimavu Cave Temple",
        location = "Hulimavu, Bannerghatta Road",
        timings = "6:30 AM – 12:30 PM, 5:00 PM – 8:30 PM",
        reach = ReachInfo(
            road = "Near Meenakshi Mall on Bannerghatta Road.",
            transport = "BMTC buses towards Bannerghatta/Jigani stop nearby.",
            ownVehicle = "Limited parking available near the cave entrance."
        ),
        facilities = Facilities(
            pooja = "Monolithic cave temple with shrines for Shiva, Ganesha, and Agastya.",
            prasad = "Vibhuti and Kumkum.",
            general = listOf("Cave experience", "Quiet sanctuary", "Ancient atmosphere")
        ),
        description = "A 400-year-old monolithic cave temple offering a unique underground spiritual experience.",
        accentColor = Color(0xFF386A20)
    ),
    Temple(
        id = 27,
        name = "Matsya Narayana Temple",
        location = "Gottigere, near NICE Road",
        timings = "7:00 AM – 12:00 PM, 5:00 PM – 8:30 PM",
        reach = ReachInfo(
            road = "Off Bannerghatta Road, near the NICE Road junction.",
            transport = "Auto from Bannerghatta Road bus stops.",
            ownVehicle = "Parking available within the serene temple grounds."
        ),
        facilities = Facilities(
            pooja = "Only temple in Bangalore for Vishnu's first avatar (Matsya).",
            prasad = "Teertha and Sakkare Pongal.",
            general = listOf("Lake view", "Peaceful", "Unique deity")
        ),
        description = "A serene temple dedicated to the fish avatar of Lord Vishnu, located near a quiet lake.",
        accentColor = Color(0xFF7D5260)
    ),
    Temple(
        id = 28,
        name = "Prasanna Veeranjaneya Temple",
        location = "Mahalakshmi Layout",
        timings = "6:00 AM – 12:30 PM, 4:30 PM – 8:30 PM",
        reach = ReachInfo(
            road = "Perched on a hill in Mahalakshmi Layout.",
            transport = "Mahalakshmi Metro Station + short auto ride.",
            ownVehicle = "Parking available at the hilltop and base."
        ),
        facilities = Facilities(
            pooja = "22-foot high Hanuman idol. Famous for its peaceful park-like setting.",
            prasad = "Traditional Hanuman prasad.",
            general = listOf("Panoramic view", "Meditation hall", "Park")
        ),
        description = "A popular hilltop temple featuring one of the largest Hanuman idols in the city.",
        accentColor = Color(0xFF984061)
    ),
    Temple(
        id = 29,
        name = "Sri Rama Mandira",
        location = "Malleswaram",
        timings = "7:00 AM – 12:00 PM, 5:30 PM – 8:30 PM",
        reach = ReachInfo(
            road = "Located in the quiet residential blocks of Malleswaram.",
            transport = "Malleswaram Metro Station + auto.",
            ownVehicle = "Street parking available in the residential lanes."
        ),
        facilities = Facilities(
            pooja = "Traditional Rama worship. Famous for Carnatic music concerts during Rama Navami.",
            prasad = "Panakam and Kosambari (during festivals).",
            general = listOf("Cultural hub", "Historic", "Quiet")
        ),
        description = "A culturally significant temple known for its deep connection to classical music and arts.",
        accentColor = Color(0xFF8C4A00)
    ),
    Temple(
        id = 30,
        name = "Mahalakshmi Temple",
        location = "Goraguntepalya, North Bangalore",
        timings = "6:00 AM – 1:00 PM, 4:00 PM – 9:00 PM",
        reach = ReachInfo(
            road = "On the service road near Goraguntepalya flyover.",
            transport = "Goraguntepalya Metro Station is a 5-min walk.",
            ownVehicle = "Limited parking on the service road; can be very busy."
        ),
        facilities = Facilities(
            pooja = "Dedicated to the Goddess of Wealth. Varamahalakshmi festival is a massive event.",
            prasad = "Sacred vermilion and sweets.",
            general = listOf("Metro access", "Highly crowded during festivals", "Market nearby")
        ),
        description = "A grand and popular temple dedicated to Goddess Mahalakshmi, conveniently located near the metro.",
        accentColor = Color(0xFF006A60)
    )
)

@Immutable
data class RelatedLink(val title: String, val meta: String)

val sampleRelatedLinks: List<RelatedLink> = listOf(
    RelatedLink("ListDetailPaneScaffold", "Canonical two-pane pattern"),
    RelatedLink("SupportingPaneScaffold", "Primary + supporting content"),
    RelatedLink("WindowSizeClass", "Compact / Medium / Expanded breakpoints"),
    RelatedLink("currentWindowAdaptiveInfo()", "Size class + fold posture"),
    RelatedLink("GridCells.Adaptive", "Column count from available width")
)
