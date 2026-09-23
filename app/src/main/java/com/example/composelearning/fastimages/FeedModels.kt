package com.example.composelearning.fastimages

import androidx.compose.runtime.Immutable

/**
 * The only two aspect ratios the catalog is allowed to use.
 *
 * Aspect ratio gets **bucketed for the same reason width does**. If every photo
 * declared its natural ratio, the CDN would derive a different `w x h` pair per
 * image and the derivative cache would explode. Real catalogs pin content to a
 * small set of crops at upload time.
 */
const val ASPECT_LANDSCAPE = 4f / 3f
const val ASPECT_SQUARE = 1f

/**
 * A single grid cell.
 *
 * Three fields here exist purely to kill layout shift and empty space, and all
 * three are **server-provided metadata, not client guesses**:
 *
 *  - [aspectRatio] — the tile can take its final size before any byte arrives.
 *    Guessing this on the client is how you get a feed that jumps as it loads.
 *  - [accentArgb] — the photo's dominant colour, ~4 bytes, so the tile is never
 *    blank and never white.
 *  - [thumbHash] — a ~36-character blurred preview. See [MicroThumb].
 *
 * `@Immutable` tells the Compose compiler every field is final, so a tile handed
 * the same [DishCard] instance is skipped outright during a scroll-triggered
 * recomposition.
 */
@Immutable
data class DishCard(
    val id: String,
    val imageSeed: String,
    val name: String,
    val restaurant: String,
    val priceLabel: String,
    val etaLabel: String,
    val rating: String,
    val aspectRatio: Float,
    val accentArgb: Long,
    val thumbHash: String
)

/** A circular category chip in the horizontal strip above the grid. */
@Immutable
data class DishCategory(
    val id: String,
    val imageSeed: String,
    val label: String,
    val accentArgb: Long
)

/**
 * The knobs the demo can turn at runtime. Everything here is ON in "turbo"
 * mode; switching one off reproduces a specific real-world mistake so the
 * metrics panel can show what it costs.
 */
@Immutable
data class FeedTuning(
    /** Ask the CDN for a bucketed WebP thumbnail instead of the full master. */
    val cdnResize: Boolean = true,
    /** Warm images just outside the viewport in the direction of travel. */
    val prefetch: Boolean = true,
    /** Give `LazyVerticalGrid` a stable `key` per item instead of the index. */
    val stableKeys: Boolean = true,
    /** Paint the [MicroThumb] preview under the photo instead of a flat colour. */
    val thumbHashPlaceholder: Boolean = true,
    /** Overlay the requested variant on each tile. */
    val showVariant: Boolean = false
)

@Immutable
data class FeedUiState(
    val categories: List<DishCategory>,
    val dishes: List<DishCard>,
    val tuning: FeedTuning = FeedTuning(),
    val overrides: PolicyOverrides = PolicyOverrides(),
    val nextDishIndex: Int = dishes.size
)

/**
 * Deterministic fake catalog. Every field is derived from the item index, so
 * the same index always yields the same dish *and* the same image seed — which
 * is what makes cache behaviour reproducible between runs.
 *
 * Treat this as the shape of the API response: IDs and metadata, **never
 * finished image URLs**. The client owns variant selection because only the
 * client knows its layout, its density, its device tier and its link speed.
 */
object DishFeedRepository {

    private val NAMES = listOf(
        "Paneer Butter Masala", "Chicken Biryani", "Masala Dosa", "Veg Hakka Noodles",
        "Butter Chicken", "Margherita Pizza", "Chole Bhature", "Mutton Rogan Josh",
        "Idli Sambar", "Pav Bhaji", "Tandoori Platter", "Prawn Curry",
        "Rajma Chawal", "Egg Fried Rice", "Mysore Masala Dosa", "Hyderabadi Haleem",
        "Malai Kofta", "Kadai Mushroom", "Fish Tikka", "Dal Makhani",
        "Schezwan Momos", "Cheese Garlic Bread", "Filter Coffee", "Gulab Jamun"
    )

    private val RESTAURANTS = listOf(
        "Empire Restaurant", "Meghana Foods", "CTR Malleshwaram", "Truffles",
        "Nagarjuna", "Vidyarthi Bhavan", "Bhagini", "Shivaji Military Hotel",
        "Corner House", "Brahmin's Coffee Bar", "Rameshwaram Cafe", "Punjabi Rasoi"
    )

    private val RATINGS = listOf("4.1", "4.6", "3.9", "4.4", "4.8", "4.2", "4.5", "3.7")

    private val ACCENTS = longArrayOf(
        0xFF6D4C41, 0xFF37474F, 0xFF4E342E, 0xFF1B5E20,
        0xFF880E4F, 0xFF3E2723, 0xFF0D47A1, 0xFF4A148C,
        0xFFBF360C, 0xFF263238, 0xFF01579B, 0xFF33691E
    )

    private val CATEGORY_LABELS = listOf(
        "Biryani", "Pizza", "Dosa", "Rolls", "Cakes",
        "Chinese", "Thali", "Momos", "Coffee", "Sweets"
    )

    fun categories(): List<DishCategory> = CATEGORY_LABELS.mapIndexed { index, label ->
        DishCategory(
            id = "category-$index",
            imageSeed = "cat-$index",
            label = label,
            accentArgb = ACCENTS[index % ACCENTS.size]
        )
    }

    fun dishes(count: Int, startIndex: Int = 0): List<DishCard> = List(count) { offset ->
        val index = startIndex + offset
        val seed = "dish-$index"
        val accent = ACCENTS[index % ACCENTS.size]
        DishCard(
            id = "dish-$index",
            imageSeed = seed,
            name = NAMES[index % NAMES.size],
            restaurant = RESTAURANTS[index % RESTAURANTS.size],
            priceLabel = "₹${129 + (index * 17) % 260}",
            etaLabel = "${14 + index % 24} min",
            rating = RATINGS[index % RATINGS.size],
            // Two crops, not twelve — see ASPECT_LANDSCAPE.
            aspectRatio = if (index % 5 == 3) ASPECT_SQUARE else ASPECT_LANDSCAPE,
            accentArgb = accent,
            // In production this string is computed once, server-side, when the
            // photo is uploaded, and shipped with the catalog row.
            thumbHash = MicroThumb.synthesise(seed, accent)
        )
    }
}
