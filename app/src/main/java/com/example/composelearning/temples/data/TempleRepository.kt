package com.example.composelearning.temples.data

import androidx.compose.runtime.Immutable
import java.time.LocalDateTime
import java.time.Month

/**
 * Read side of the feature. The dataset is a compiled-in constant, so there is no loading
 * state, no cache, and no failure mode — the whole guide works offline. Only photos and
 * the maps hand-off need the network.
 */
object TempleRepository {

    val temples: List<Temple> get() = bengaluruTemples

    fun byId(id: String): Temple? = bengaluruTemples.firstOrNull { it.id == id }

    /** Neighbourhoods present in the data, for the area filter. Sorted for a stable UI. */
    val areas: List<LocalizedText> by lazy {
        bengaluruTemples.map { it.area }.distinctBy { it.en }.sortedBy { it.en }
    }

    /** Deities that actually occur, so the filter row never offers an empty facet. */
    val deities: List<Deity> by lazy {
        Deity.entries.filter { deity -> bengaluruTemples.any { it.deity == deity } }
    }

    /**
     * Every festival across every temple, ordered as a year-round calendar starting from
     * the current month — so "what's coming up" is the first thing on screen.
     */
    fun festivalCalendar(from: LocalDateTime): List<FestivalEntry> {
        val currentMonth = from.monthValue
        return bengaluruTemples
            .flatMap { temple -> temple.festivals.map { FestivalEntry(temple, it) } }
            .sortedBy { entry ->
                val month = entry.festival.usualMonth ?: 13
                // Rotate the year so months already past this year sort to the back.
                if (month >= currentMonth) month - currentMonth else month - currentMonth + 12
            }
    }
}

/** A festival paired with the temple that celebrates it. */
@Immutable
data class FestivalEntry(val temple: Temple, val festival: Festival) {
    val monthName: String? get() = festival.usualMonth?.let { Month.of(it).name }
}
