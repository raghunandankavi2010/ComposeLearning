package com.example.composelearning.temples.data

/**
 * The dataset for the Temple Showcase feature.
 *
 * [bengaluruTemples] is a compiled-in constant, which is what lets the whole guide work in
 * airplane mode — only photos and the maps hand-off touch the network. The records are split
 * across four files by part of the city so no single file becomes unreadable:
 *
 *  * [templesCity] — the old walled city: Pete, Avenue Road, Nagarathpet, Shivajinagar.
 *  * [templesSouth] — Basavanagudi, Gavipuram, Jayanagar, Banashankari, Bannerghatta Road.
 *  * [templesNorthWest] — Malleshwaram, Rajajinagar, Hebbal, Mysore Road, Kengeri.
 *  * [templesEast] — Halasuru, Domlur, Old Airport Road, Sarjapur Road, Whitefield, Anekal.
 *
 * Sources: identity, history, deity and coordinates come from Wikipedia and Wikimedia
 * Commons; every photo is a freely licensed Commons file with its author and licence carried
 * on the record and shown in the UI. Timings, ritual clocks, bus stops and nearby shops are
 * the softer half of the data — see [DataConfidence] and the shared notes in
 * `TempleDataCommon.kt` for how that is handled.
 *
 * Adding a temple: append to the file for its part of the city. The id must be unique and
 * stable — it is what the saved/visited preferences are keyed on, so renaming one silently
 * drops a user's saved list.
 */
val bengaluruTemples: List<Temple> =
    templesCity + templesSouth + templesNorthWest + templesEast
