package com.example.composelearning.cropdoctor.domain

import com.example.composelearning.cropdoctor.domain.model.DiseaseInfo
import com.example.composelearning.cropdoctor.domain.model.Severity

/**
 * Curated, farmer-facing knowledge for every PlantVillage class the model can output.
 *
 * Keyed by the exact (trimmed) label line in `assets/plant_labels.txt`. The advice is intentionally
 * hand-written and conservative — it never prescribes specific chemical dosages, and always points
 * the farmer to the local agriculture extension officer for approved products. This is safer and
 * more reliable for non-expert users than model-generated text.
 *
 * [lookup] always returns something: an unknown label degrades to a sensible generic entry derived
 * from the label text, so the UI never crashes on a label/knowledge mismatch.
 */
object DiseaseKnowledge {

    private val healthyActions = listOf(
        "No disease detected — the leaf looks healthy.",
        "Keep monitoring weekly, especially after rain or heavy dew.",
        "Maintain good spacing and airflow, and water at the base (not on leaves).",
    )

    private fun healthy(crop: String) = DiseaseInfo(
        crop = crop,
        condition = "Healthy",
        isHealthy = true,
        severity = Severity.NONE,
        summary = "This $crop leaf appears healthy. No action needed.",
        symptoms = "Uniform colour, no spots, lesions, mould or curling.",
        actions = healthyActions
    )

    private val map: Map<String, DiseaseInfo> = buildMap {
        // ── Apple ──────────────────────────────────────────────────────────────
        put(
            "apple apple scab", DiseaseInfo(
                "Apple", "Apple Scab", false, Severity.MODERATE,
                "A fungal disease (Venturia inaequalis) common in cool, wet springs.",
                "Olive-green to brown velvety spots on leaves and fruit; leaves may curl and drop early.",
                listOf(
                    "Remove and destroy fallen leaves and infected fruit to cut the spore source.",
                    "Prune to open the canopy so leaves dry faster after rain.",
                    "Apply an approved fungicide at green-tip and repeat through wet spring weather.",
                    "Ask your local extension officer which fungicide and schedule suit your area.",
                )
            )
        )
        put(
            "apple black rot", DiseaseInfo(
                "Apple", "Black Rot", false, Severity.MODERATE,
                "A fungal disease causing leaf spots, fruit rot and branch cankers.",
                "Purple-edged 'frog-eye' leaf spots; sunken black rotting fruit; cankered branches.",
                listOf(
                    "Prune out and burn cankered, dead wood well below the infected area.",
                    "Remove mummified fruit left on the tree and on the ground.",
                    "Keep trees healthy; avoid wounds where the fungus enters.",
                    "Apply an approved fungicide from bloom if the disease is established.",
                )
            )
        )
        put(
            "apple cedar apple rust", DiseaseInfo(
                "Apple", "Cedar Apple Rust", false, Severity.MODERATE,
                "A fungus needing both apple and nearby cedar/juniper trees to complete its cycle.",
                "Bright yellow-orange spots on upper leaves; later, small tubes form underneath.",
                listOf(
                    "Where practical, remove nearby cedar/juniper hosts within a few hundred metres.",
                    "Plant rust-resistant apple varieties for future trees.",
                    "Apply an approved fungicide from pink-bud through early summer.",
                    "Consult your extension officer for the local spray window.",
                )
            )
        )
        put("apple healthy", healthy("Apple"))

        // ── Blueberry ────────────────────────────────────────────────────────────
        put("blueberry healthy", healthy("Blueberry"))

        // ── Cherry ─────────────────────────────────────────────────────────────
        put(
            "cherry including sour powdery mildew", DiseaseInfo(
                "Cherry", "Powdery Mildew", false, Severity.MODERATE,
                "A fungus that thrives in warm, dry days with humid nights.",
                "White powdery patches on leaves and shoots; new growth may curl or stunt.",
                listOf(
                    "Prune for better airflow and remove heavily infected shoots.",
                    "Avoid excess nitrogen fertiliser, which fuels susceptible new growth.",
                    "Apply an approved fungicide or sulphur spray at first signs.",
                    "Check the underside of leaves regularly so you catch it early.",
                )
            )
        )
        put("cherry including sour healthy", healthy("Cherry"))

        // ── Corn (Maize) ─────────────────────────────────────────────────────────
        put(
            "corn maize cercospora leaf spot gray leaf spot", DiseaseInfo(
                "Corn (Maize)", "Gray Leaf Spot", false, Severity.MODERATE,
                "A fungal disease favoured by warm, humid weather and dense stands.",
                "Long, narrow, rectangular grey-to-tan lesions running along the leaf veins.",
                listOf(
                    "Rotate to a non-host crop for one to two seasons.",
                    "Plough in or remove old corn residue that carries the fungus.",
                    "Choose resistant hybrids and avoid over-dense planting.",
                    "Use an approved fungicide at early disease onset if pressure is high.",
                )
            )
        )
        put(
            "corn maize common rust", DiseaseInfo(
                "Corn (Maize)", "Common Rust", false, Severity.LOW,
                "A fungal rust spread by wind-borne spores in cool, moist conditions.",
                "Small reddish-brown powdery pustules scattered on both leaf surfaces.",
                listOf(
                    "Plant resistant hybrids — the most reliable control.",
                    "Most fields tolerate light rust without yield loss; monitor severity.",
                    "Apply an approved fungicide only if pustules spread rapidly before tasseling.",
                    "Avoid working in fields while leaves are wet to limit spread.",
                )
            )
        )
        put(
            "corn maize northern leaf blight", DiseaseInfo(
                "Corn (Maize)", "Northern Leaf Blight", false, Severity.HIGH,
                "A fungal disease that can cause serious yield loss in wet seasons.",
                "Long, cigar-shaped grey-green to tan lesions, often starting on lower leaves.",
                listOf(
                    "Plant resistant hybrids and rotate away from corn for a season.",
                    "Bury or remove infected residue after harvest.",
                    "Apply an approved fungicide around tasseling if lesions reach the upper leaves.",
                    "Act early — losses rise sharply once it reaches the ear leaf.",
                )
            )
        )
        put("corn maize healthy", healthy("Corn (Maize)"))

        // ── Grape ──────────────────────────────────────────────────────────────
        put(
            "grape black rot", DiseaseInfo(
                "Grape", "Black Rot", false, Severity.HIGH,
                "A fungal disease that can destroy entire bunches in warm, wet weather.",
                "Tan leaf spots with dark borders; berries shrivel into hard black 'mummies'.",
                listOf(
                    "Remove and destroy all mummified berries and infected leaves.",
                    "Prune for airflow and keep the canopy open and dry.",
                    "Begin an approved fungicide programme from early shoot growth.",
                    "Sanitation over winter is critical — clean up the vineyard floor.",
                )
            )
        )
        put(
            "grape esca black measles", DiseaseInfo(
                "Grape", "Esca (Black Measles)", false, Severity.HIGH,
                "A complex trunk disease caused by wood-rotting fungi.",
                "Tiger-stripe yellow/red bands between leaf veins; dark spots ('measles') on berries.",
                listOf(
                    "Remove and burn severely affected vines and prunings.",
                    "Prune late in dry weather and protect large pruning wounds.",
                    "Avoid water stress; keep vines vigorous.",
                    "There is no cure once advanced — focus on prevention and clean tools.",
                )
            )
        )
        put(
            "grape leaf blight isariopsis leaf spot", DiseaseInfo(
                "Grape", "Leaf Blight (Isariopsis)", false, Severity.MODERATE,
                "A fungal leaf-spot disease that develops in warm, humid conditions.",
                "Irregular dark brown blotches on leaves; heavy spotting causes early leaf drop.",
                listOf(
                    "Improve canopy airflow through pruning and leaf removal.",
                    "Clear fallen leaves that harbour the fungus.",
                    "Apply an approved fungicide when spots first appear.",
                    "Avoid overhead irrigation that keeps leaves wet.",
                )
            )
        )
        put("grape healthy", healthy("Grape"))

        // ── Orange ─────────────────────────────────────────────────────────────
        put(
            "orange haunglongbing citrus greening", DiseaseInfo(
                "Orange", "Citrus Greening (HLB)", false, Severity.HIGH,
                "A serious bacterial disease spread by the Asian citrus psyllid insect. No cure.",
                "Blotchy asymmetric yellowing; small, lopsided, bitter, partly-green fruit.",
                listOf(
                    "Report suspected cases to your agriculture department — it is a notifiable disease.",
                    "Remove and destroy infected trees to protect neighbouring groves.",
                    "Control the psyllid insect that spreads it.",
                    "Plant only certified disease-free nursery stock.",
                )
            )
        )

        // ── Peach ──────────────────────────────────────────────────────────────
        put(
            "peach bacterial spot", DiseaseInfo(
                "Peach", "Bacterial Spot", false, Severity.MODERATE,
                "A bacterial disease worsened by wind, sand abrasion and wet weather.",
                "Small angular purple-brown leaf spots that drop out ('shot-hole'); cracked fruit spots.",
                listOf(
                    "Plant resistant varieties and provide windbreaks.",
                    "Avoid overhead irrigation; keep foliage dry.",
                    "Apply approved copper-based sprays on the recommended schedule.",
                    "Do not over-fertilise with nitrogen.",
                )
            )
        )
        put("peach healthy", healthy("Peach"))

        // ── Pepper (Bell) ──────────────────────────────────────────────────────
        put(
            "pepper bell bacterial spot", DiseaseInfo(
                "Bell Pepper", "Bacterial Spot", false, Severity.MODERATE,
                "A bacterial disease spread by splashing water and infected seed.",
                "Small water-soaked spots turning brown with yellow halos; scabby fruit lesions.",
                listOf(
                    "Use certified disease-free seed and transplants.",
                    "Rotate away from peppers and tomatoes for 2–3 years.",
                    "Avoid working among wet plants; water at the base.",
                    "Apply approved copper sprays preventively in wet weather.",
                )
            )
        )
        put("pepper bell healthy", healthy("Bell Pepper"))

        // ── Potato ─────────────────────────────────────────────────────────────
        put(
            "potato early blight", DiseaseInfo(
                "Potato", "Early Blight", false, Severity.MODERATE,
                "A fungal disease (Alternaria) that hits older leaves and stressed plants first.",
                "Dark brown spots with concentric rings ('target' pattern) on lower, older leaves.",
                listOf(
                    "Remove infected lower leaves and keep plants well-nourished.",
                    "Rotate crops and avoid overhead watering late in the day.",
                    "Mulch to stop soil-borne spores splashing onto leaves.",
                    "Apply an approved fungicide when spots first appear.",
                )
            )
        )
        put(
            "potato late blight", DiseaseInfo(
                "Potato", "Late Blight", false, Severity.HIGH,
                "A fast, devastating disease (Phytophthora) — the cause of historic famines.",
                "Large dark, water-soaked blotches; white fuzzy growth under leaves in humid weather.",
                listOf(
                    "Act immediately — it can destroy a field in days during cool, wet spells.",
                    "Remove and destroy infected plants; do not compost them.",
                    "Apply an approved protectant fungicide before and during wet weather.",
                    "Harvest only in dry conditions and never store infected tubers.",
                )
            )
        )
        put("potato healthy", healthy("Potato"))

        // ── Raspberry / Soybean ──────────────────────────────────────────────────
        put("raspberry healthy", healthy("Raspberry"))
        put("soybean healthy", healthy("Soybean"))

        // ── Squash ─────────────────────────────────────────────────────────────
        put(
            "squash powdery mildew", DiseaseInfo(
                "Squash", "Powdery Mildew", false, Severity.MODERATE,
                "A very common fungus in warm weather with humid nights.",
                "White powdery coating on leaves and stems; leaves yellow, dry and die early.",
                listOf(
                    "Space plants well and remove the worst-affected leaves.",
                    "Water at the base in the morning so foliage dries.",
                    "Plant resistant varieties next season.",
                    "Apply an approved fungicide, sulphur, or a tested milk/baking-soda spray early.",
                )
            )
        )

        // ── Strawberry ───────────────────────────────────────────────────────────
        put(
            "strawberry leaf scorch", DiseaseInfo(
                "Strawberry", "Leaf Scorch", false, Severity.LOW,
                "A fungal leaf disease that builds up in dense, damp plantings.",
                "Many small dark-purple spots that merge; leaf edges look scorched and dry.",
                listOf(
                    "Thin runners and old leaves to improve airflow.",
                    "Renovate beds after harvest by removing old foliage.",
                    "Water at the base, not overhead.",
                    "Apply an approved fungicide if scorch is severe.",
                )
            )
        )
        put("strawberry healthy", healthy("Strawberry"))

        // ── Tomato ─────────────────────────────────────────────────────────────
        put(
            "tomato bacterial spot", DiseaseInfo(
                "Tomato", "Bacterial Spot", false, Severity.MODERATE,
                "A bacterial disease spread by splashing water, tools and infected seed.",
                "Small dark, greasy-looking spots with yellow halos on leaves; scabby fruit spots.",
                listOf(
                    "Use certified clean seed; rotate away from tomato/pepper for 2–3 years.",
                    "Avoid handling or watering plants when wet.",
                    "Remove badly infected plants; disinfect tools.",
                    "Apply approved copper sprays preventively in wet weather.",
                )
            )
        )
        put(
            "tomato early blight", DiseaseInfo(
                "Tomato", "Early Blight", false, Severity.MODERATE,
                "A common fungal disease (Alternaria) starting on the oldest leaves.",
                "Brown spots with concentric 'target' rings on lower leaves; yellowing around them.",
                listOf(
                    "Remove and destroy infected lower leaves.",
                    "Mulch the soil and water at the base to stop splash.",
                    "Stake plants for airflow and rotate crops yearly.",
                    "Apply an approved fungicide at first symptoms.",
                )
            )
        )
        put(
            "tomato late blight", DiseaseInfo(
                "Tomato", "Late Blight", false, Severity.HIGH,
                "A fast-moving, destructive disease (Phytophthora) in cool, wet weather.",
                "Large grey-green water-soaked blotches; white mould underneath; fruit rots quickly.",
                listOf(
                    "Act immediately — it can wipe out plants within days.",
                    "Remove and destroy infected plants; do not compost.",
                    "Apply an approved protectant fungicide before wet spells.",
                    "Avoid overhead watering and improve drainage.",
                )
            )
        )
        put(
            "tomato leaf mold", DiseaseInfo(
                "Tomato", "Leaf Mold", false, Severity.MODERATE,
                "A fungal disease common in humid greenhouses and tunnels.",
                "Pale yellow patches on upper leaves; olive-green to brown velvety mould beneath.",
                listOf(
                    "Increase ventilation and lower humidity; space plants out.",
                    "Water at the base and remove infected leaves.",
                    "Grow resistant varieties under cover.",
                    "Apply an approved fungicide if it keeps spreading.",
                )
            )
        )
        put(
            "tomato septoria leaf spot", DiseaseInfo(
                "Tomato", "Septoria Leaf Spot", false, Severity.MODERATE,
                "A fungal disease that defoliates plants from the bottom up.",
                "Many small circular spots with dark borders and grey centres on lower leaves.",
                listOf(
                    "Remove infected lower leaves promptly.",
                    "Mulch and water at the base to stop soil splash.",
                    "Rotate crops and clear all debris at season's end.",
                    "Apply an approved fungicide early to protect upper leaves.",
                )
            )
        )
        put(
            "tomato spider mites two spotted spider mite", DiseaseInfo(
                "Tomato", "Two-Spotted Spider Mites", false, Severity.MODERATE,
                "Tiny sap-sucking pests (not a disease) that explode in hot, dry weather.",
                "Fine pale stippling/speckling on leaves; faint webbing; leaves bronze and dry.",
                listOf(
                    "Spray plants forcefully with water to knock mites off undersides.",
                    "Keep plants well-watered — drought stress worsens mites.",
                    "Encourage or release natural predators (ladybirds, predatory mites).",
                    "Use an approved miticide or insecticidal soap if numbers stay high.",
                )
            )
        )
        put(
            "tomato target spot", DiseaseInfo(
                "Tomato", "Target Spot", false, Severity.MODERATE,
                "A fungal disease (Corynespora) affecting leaves, stems and fruit.",
                "Brown spots with concentric rings on leaves; sunken circular lesions on fruit.",
                listOf(
                    "Improve airflow with staking and pruning.",
                    "Remove infected leaves and fruit; water at the base.",
                    "Rotate crops and clear debris.",
                    "Apply an approved fungicide when symptoms begin.",
                )
            )
        )
        put(
            "tomato tomato yellow leaf curl virus", DiseaseInfo(
                "Tomato", "Yellow Leaf Curl Virus", false, Severity.HIGH,
                "A virus spread by whiteflies — there is no cure once a plant is infected.",
                "Upward-curling, yellowing, cupped small leaves; severely stunted plants; little fruit.",
                listOf(
                    "Remove and destroy infected plants to protect the rest.",
                    "Control whiteflies — the insect that spreads the virus.",
                    "Use whitefly netting/screens and reflective mulch.",
                    "Plant resistant varieties next season.",
                )
            )
        )
        put(
            "tomato tomato mosaic virus", DiseaseInfo(
                "Tomato", "Mosaic Virus", false, Severity.HIGH,
                "A very contagious virus spread by hands, tools and contact. No cure.",
                "Mottled light/dark green mosaic on leaves; distorted, fern-like or stringy growth.",
                listOf(
                    "Remove and destroy infected plants immediately.",
                    "Wash hands and disinfect tools; avoid tobacco use near plants.",
                    "Do not save seed from infected plants.",
                    "Plant resistant varieties and certified clean seed.",
                )
            )
        )
        put("tomato healthy", healthy("Tomato"))
    }

    /** Always returns a usable entry; falls back to a generic one derived from the label text. */
    fun lookup(rawLabel: String): DiseaseInfo {
        val key = rawLabel.trim().lowercase()
        map[key]?.let { return it }
        // Defensive fallback: derive a readable crop/condition from the label words.
        val healthy = key.endsWith("healthy")
        val crop = key.split(" ").firstOrNull()?.replaceFirstChar { it.uppercase() } ?: "Plant"
        return if (healthy) {
            healthy(crop)
        } else {
            DiseaseInfo(
                crop = crop,
                condition = rawLabel.trim().replaceFirstChar { it.uppercase() },
                isHealthy = false,
                severity = Severity.MODERATE,
                summary = "A possible disease was detected on this $crop leaf.",
                symptoms = "Compare the leaf against a reference for this condition.",
                actions = listOf(
                    "Remove and destroy clearly infected leaves.",
                    "Improve airflow and avoid wetting the foliage.",
                    "Consult your local agriculture extension officer for confirmation and treatment.",
                )
            )
        }
    }
}
