package com.example.composelearning.temples.data

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.example.composelearning.R
import java.time.DayOfWeek
import java.time.LocalTime

/**
 * Domain model for the Temple Showcase feature.
 *
 * Two localisation mechanisms live side by side here, on purpose:
 *
 *  * **Fixed UI vocabulary** — section titles, facility names, deity names, day names.
 *    These are a small closed set, so they are ordinary Android string resources
 *    (`values/`, `values-hi/`, `values-kn/`) referenced through [StringRes] ids on enums.
 *  * **Per-temple prose** — names, descriptions, notes. There are hundreds of these and they
 *    only make sense as data, so they travel with the temple as [LocalizedText].
 *
 * Both are driven by the *same* source of truth: the active app locale. Switching language
 * through [com.example.composelearning.temples.platform.AppLanguage] flips the resource
 * folder and the [LocalizedText.resolve] lookup together.
 *
 * All data is static and offline — the feature works in airplane mode apart from photos.
 */

/** A string carried in the three languages this feature ships. Falls back to English. */
@Immutable
data class LocalizedText(
    val en: String,
    val hi: String? = null,
    val kn: String? = null
) {
    /** [languageTag] is a two-letter ISO code such as `en`, `hi`, `kn`. */
    fun resolve(languageTag: String): String = when (languageTag) {
        "hi" -> hi ?: en
        "kn" -> kn ?: en
        else -> en
    }

    override fun toString(): String = en
}

/** Terse constructor so the dataset below stays readable: `t("Bull Temple", kn = "...")`. */
fun t(en: String, hi: String? = null, kn: String? = null) = LocalizedText(en, hi, kn)

@Immutable
data class GeoPoint(val lat: Double, val lng: Double)

/**
 * How well-sourced a temple record is. Surfaced in the UI so a user knows when to
 * double-check timings before travelling — inventing precision would be worse than admitting it.
 */
enum class DataConfidence(@StringRes val labelRes: Int) {
    HIGH(R.string.temple_confidence_high),
    MEDIUM(R.string.temple_confidence_medium),
    LOW(R.string.temple_confidence_low)
}

/** Primary deity — doubles as the main filter facet on the discover screen. */
enum class Deity(@StringRes val labelRes: Int) {
    SHIVA(R.string.deity_shiva),
    VISHNU(R.string.deity_vishnu),
    KRISHNA(R.string.deity_krishna),
    GANESHA(R.string.deity_ganesha),
    HANUMAN(R.string.deity_hanuman),
    DEVI(R.string.deity_devi),
    SUBRAMANYA(R.string.deity_subramanya),
    AYYAPPA(R.string.deity_ayyappa),
    NAVAGRAHA(R.string.deity_navagraha),
    OTHER(R.string.deity_other)
}

/** A single open window. A temple that shuts for the afternoon simply has two of these. */
@Immutable
data class OpeningWindow(
    val days: Set<DayOfWeek>,
    val open: LocalTime,
    val close: LocalTime
)

/** All seven days — the common case, since most temples keep the same hours daily. */
val AllDays: Set<DayOfWeek> = DayOfWeek.entries.toSet()

/** Convenience for the dataset: `open("06:00", "12:30")`. */
fun window(open: String, close: String, days: Set<DayOfWeek> = AllDays) =
    OpeningWindow(days, LocalTime.parse(open), LocalTime.parse(close))

/** A daily ritual, arati or bookable seva. */
@Immutable
data class Ritual(
    val name: LocalizedText,
    /** Wall-clock time as published by the temple, e.g. `04:30`. Null for on-request sevas. */
    val time: LocalTime? = null,
    /** Published ticket price in rupees, when the temple lists one. */
    val priceInr: Int? = null
)

fun ritual(name: LocalizedText, time: String? = null, priceInr: Int? = null) =
    Ritual(name, time?.let(LocalTime::parse), priceInr)

@Immutable
data class Festival(
    val name: LocalizedText,
    /** Human-readable window, e.g. "Nov–Dec (Karthika masa)" — lunar dates shift yearly. */
    val whenApprox: LocalizedText,
    /** Gregorian month it usually lands in, used only to order the festival calendar. */
    val usualMonth: Int? = null,
    val note: LocalizedText? = null
)

/** BMTC bus access. Route numbers are the single most useful thing for a first-time visitor. */
@Immutable
data class BusInfo(
    val routes: List<String>,
    val nearestStop: String,
    val note: LocalizedText? = null
)

enum class MetroLine(@StringRes val labelRes: Int, val brand: Color) {
    PURPLE(R.string.metro_line_purple, Color(0xFF6A1B9A)),
    GREEN(R.string.metro_line_green, Color(0xFF2E7D32)),
    YELLOW(R.string.metro_line_yellow, Color(0xFFF9A825)),
    PINK(R.string.metro_line_pink, Color(0xFFC2185B)),
    BLUE(R.string.metro_line_blue, Color(0xFF1565C0))
}

@Immutable
data class MetroInfo(
    val station: String,
    val line: MetroLine,
    val walkMinutes: Int
)

/** Amenities, rendered as a chip grid. Enum + string res so they localise for free. */
enum class Facility(@StringRes val labelRes: Int) {
    WHEELCHAIR(R.string.facility_wheelchair),
    RESTROOMS(R.string.facility_restrooms),
    DRINKING_WATER(R.string.facility_drinking_water),
    SHOE_STAND(R.string.facility_shoe_stand),
    CLOAK_ROOM(R.string.facility_cloak_room),
    PRASAD_COUNTER(R.string.facility_prasad_counter),
    ANNADANA(R.string.facility_annadana),
    FREE_PARKING(R.string.facility_free_parking),
    PAID_PARKING(R.string.facility_paid_parking),
    QUEUE_SHELTER(R.string.facility_queue_shelter),
    MARRIAGE_HALL(R.string.facility_marriage_hall),
    BOOKSTORE(R.string.facility_bookstore),
    ELEVATOR(R.string.facility_elevator),
    PHOTOGRAPHY_ALLOWED(R.string.facility_photography_allowed),
    NO_PHOTOGRAPHY(R.string.facility_no_photography)
}

/** Kinds of practical place a visitor hunts for around a temple. */
enum class NearbyKind(@StringRes val labelRes: Int) {
    POOJA_ITEMS(R.string.nearby_pooja_items),
    EAT(R.string.nearby_eat),
    PARKING(R.string.nearby_parking),
    ATM(R.string.nearby_atm),
    STAY(R.string.nearby_stay)
}

@Immutable
data class NearbyPlace(
    val kind: NearbyKind,
    val name: String,
    val note: LocalizedText,
    /** Present only when we have a verified pin; otherwise the UI searches by name instead. */
    val location: GeoPoint? = null
)

enum class DonationKind(@StringRes val labelRes: Int) {
    HUNDI(R.string.donation_hundi),
    ONLINE(R.string.donation_online),
    SEVA_BOOKING(R.string.donation_seva),
    ANNADANA(R.string.donation_annadana),
    TRUST(R.string.donation_trust)
}

@Immutable
data class DonationChannel(
    val kind: DonationKind,
    val label: LocalizedText,
    /** Official donation/seva page. Opened in a browser; never handled in-app. */
    val url: String? = null,
    val detail: LocalizedText? = null
)

@Immutable
data class TemplePhoto(
    /** Wikimedia Commons `Special:FilePath` URL — stable, and resizes server-side. */
    val url: String,
    val caption: LocalizedText,
    /** Author string the licence obliges us to display. */
    val credit: String,
    /** Short licence name, e.g. "CC BY-SA 4.0". */
    val license: String,
    /** Commons file page, so a user can reach the full licence text. */
    val sourceUrl: String
)

@Immutable
data class Temple(
    val id: String,
    val name: LocalizedText,
    val deity: Deity,
    val area: LocalizedText,
    /** Postal address in English — also the fallback query when we have no coordinates. */
    val address: String,
    val location: GeoPoint?,
    val about: LocalizedText,
    val builtIn: String? = null,
    val openings: List<OpeningWindow> = emptyList(),
    val rituals: List<Ritual> = emptyList(),
    val festivals: List<Festival> = emptyList(),
    val bus: BusInfo? = null,
    val metro: MetroInfo? = null,
    val parking: LocalizedText? = null,
    val facilities: Set<Facility> = emptySet(),
    val dressCode: LocalizedText? = null,
    val busiestDays: Set<DayOfWeek> = emptySet(),
    val photos: List<TemplePhoto> = emptyList(),
    val nearby: List<NearbyPlace> = emptyList(),
    val donation: List<DonationChannel> = emptyList(),
    val phone: String? = null,
    val website: String? = null,
    val accent: Color = Color(0xFF8C4A00),
    val confidence: DataConfidence = DataConfidence.MEDIUM
) {
    /** Places selling flowers, coconuts, camphor and puja kits. */
    val poojaItemShops: List<NearbyPlace> get() = nearby.filter { it.kind == NearbyKind.POOJA_ITEMS }

    /** Eateries — the darshini next door matters as much as the darshan for most visitors. */
    val eateries: List<NearbyPlace> get() = nearby.filter { it.kind == NearbyKind.EAT }
}
