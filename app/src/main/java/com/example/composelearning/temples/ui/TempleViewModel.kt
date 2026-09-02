package com.example.composelearning.temples.ui

import android.app.Application
import androidx.compose.runtime.Immutable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.composelearning.temples.data.Deity
import com.example.composelearning.temples.data.Facility
import com.example.composelearning.temples.data.GeoPoint
import com.example.composelearning.temples.data.LocalizedText
import com.example.composelearning.temples.data.OpenStatus
import com.example.composelearning.temples.data.Temple
import com.example.composelearning.temples.data.TemplePreferences
import com.example.composelearning.temples.data.TempleRepository
import com.example.composelearning.temples.data.distanceKm
import com.example.composelearning.temples.data.openStatusAt
import com.example.composelearning.temples.platform.DeviceLocationSource
import com.example.composelearning.temples.platform.LocationState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime

enum class TempleSort { NAME, DISTANCE, AREA }

/** Everything the discover screen lets a user narrow the list by. */
@Immutable
data class TempleFilters(
    val query: String = "",
    val deity: Deity? = null,
    /** Keyed on the English area name — a stable id that does not move with the locale. */
    val area: String? = null,
    val openNow: Boolean = false,
    val wheelchair: Boolean = false,
    val nearMetro: Boolean = false,
    val annadana: Boolean = false,
    val favouritesOnly: Boolean = false,
    val sort: TempleSort = TempleSort.NAME
) {
    val isActive: Boolean
        get() = query.isNotBlank() || deity != null || area != null ||
            openNow || wheelchair || nearMetro || annadana
}

/** A temple plus everything that depends on *right now* — distance, open state, user flags. */
@Immutable
data class TempleListItem(
    val temple: Temple,
    val distanceKm: Double?,
    val status: OpenStatus,
    val now: LocalDateTime,
    val isFavourite: Boolean,
    val isVisited: Boolean
)

@Immutable
data class TempleUiState(
    /** Every temple, decorated but unfiltered. The detail pane and saved trail read this. */
    val allItems: List<TempleListItem> = emptyList(),
    /** [allItems] after the discover-tab filters and sort. What the list pane shows. */
    val items: List<TempleListItem> = emptyList(),
    val filters: TempleFilters = TempleFilters(),
    val location: LocationState = LocationState.Idle,
    val now: LocalDateTime = LocalDateTime.now()
) {
    val totalCount: Int get() = allItems.size

    /**
     * Looks a temple up for the detail pane.
     *
     * Reads [allItems], not [items]: a temple opened before the user tightened a filter
     * must stay on screen rather than blanking out from under them.
     */
    fun itemFor(templeId: String?): TempleListItem? =
        templeId?.let { id -> allItems.firstOrNull { it.temple.id == id } }

    /** Saved temples ordered nearest-first — the "temple trail" route. */
    val savedByDistance: List<TempleListItem>
        get() = allItems.filter { it.isFavourite }
            .sortedWith(compareBy(nullsLast<Double>()) { it.distanceKm })
}

/**
 * Single ViewModel for the whole feature.
 *
 * The dataset is static, so this class exists to combine four *live* inputs into one list:
 * the user's filters, their location, their saved/visited sets, and the clock. The clock
 * matters more than it looks — "open now" and "closes in 20 min" are wrong the moment they
 * stop ticking, so a minute-resolution timer feeds recomposition rather than a one-shot read.
 */
class TempleViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = TemplePreferences(application)
    private val locationSource = DeviceLocationSource(application)

    private val filters = MutableStateFlow(TempleFilters())
    private val locationState = MutableStateFlow<LocationState>(LocationState.Idle)

    /**
     * Emits at the top of every minute so open/closed labels stay honest without burning
     * a frame-rate timer on something that changes 1440 times a day.
     */
    private val clock = flow {
        while (true) {
            val now = LocalDateTime.now()
            emit(now)
            delay(60_000L - now.second * 1_000L - now.nano / 1_000_000L)
        }
    }

    val uiState: StateFlow<TempleUiState> = combine(
        filters,
        locationState,
        preferences.favourites,
        preferences.visited,
        clock
    ) { activeFilters, location, favourites, visited, now ->
        val origin = (location as? LocationState.Ready)?.point
        val decorated = decorate(
            temples = TempleRepository.temples,
            origin = origin,
            now = now,
            favourites = favourites,
            visited = visited
        )
        TempleUiState(
            allItems = decorated,
            items = decorated.applyFilters(activeFilters),
            filters = activeFilters,
            location = location,
            now = now
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TempleUiState()
    )

    /** Exposed separately so the language screen can read it without the temple list. */
    val currentLocation: StateFlow<LocationState> = locationState.asStateFlow()

    init {
        // A cached fix costs nothing and lets the list open already sorted by distance.
        val cached = locationSource.lastKnown()
        if (cached is LocationState.Ready) locationState.value = cached
    }

    fun onQueryChange(query: String) = filters.update { it.copy(query = query) }

    fun onDeitySelected(deity: Deity?) = filters.update {
        it.copy(deity = if (it.deity == deity) null else deity)
    }

    fun onAreaSelected(area: String?) = filters.update {
        it.copy(area = if (it.area == area) null else area)
    }

    fun onToggleOpenNow() = filters.update { it.copy(openNow = !it.openNow) }
    fun onToggleWheelchair() = filters.update { it.copy(wheelchair = !it.wheelchair) }
    fun onToggleNearMetro() = filters.update { it.copy(nearMetro = !it.nearMetro) }
    fun onToggleAnnadana() = filters.update { it.copy(annadana = !it.annadana) }
    fun onSortChange(sort: TempleSort) = filters.update { it.copy(sort = sort) }

    fun onClearFilters() = filters.update { TempleFilters(sort = it.sort) }

    fun onToggleFavourite(templeId: String) = viewModelScope.launch {
        preferences.toggleFavourite(templeId)
    }

    fun onToggleVisited(templeId: String) = viewModelScope.launch {
        preferences.toggleVisited(templeId)
    }

    /** Called by the permission flow and the refresh button. */
    fun requestLocation() {
        if (!locationSource.hasPermission()) {
            locationState.value = LocationState.PermissionRequired
            return
        }
        locationState.value = LocationState.Locating
        viewModelScope.launch {
            locationState.value = locationSource.current()
            // Sorting by distance is the whole point of asking, so switch to it once.
            if (locationState.value is LocationState.Ready && filters.value.sort == TempleSort.NAME) {
                filters.update { it.copy(sort = TempleSort.DISTANCE) }
            }
        }
    }

    /** Records a permission denial so the UI can offer the settings deep-link instead. */
    fun onLocationPermissionDenied() {
        locationState.value = LocationState.PermissionRequired
    }
}

/**
 * Attaches the *right now* facts — distance, open state, saved/visited — to every temple.
 *
 * Split from filtering so distance is computed once per temple per tick rather than once per
 * filter change, and so a unit test can pin either half without a Looper, an Application,
 * or a DataStore.
 */
internal fun decorate(
    temples: List<Temple>,
    origin: GeoPoint?,
    now: LocalDateTime,
    favourites: Set<String>,
    visited: Set<String>
): List<TempleListItem> = temples.map { temple ->
    TempleListItem(
        temple = temple,
        distanceKm = origin?.let { from ->
            temple.location?.let { to -> distanceKm(from, to) }
        },
        status = temple.openStatusAt(now),
        now = now,
        isFavourite = temple.id in favourites,
        isVisited = temple.id in visited
    )
}

/** Pure filter + sort over decorated items. */
internal fun List<TempleListItem>.applyFilters(filters: TempleFilters): List<TempleListItem> {
    val filtered = asSequence()
        .filter { it.temple.matchesQuery(filters.query) }
        .filter { filters.deity == null || it.temple.deity == filters.deity }
        .filter { filters.area == null || it.temple.area.en == filters.area }
        .filter { !filters.wheelchair || Facility.WHEELCHAIR in it.temple.facilities }
        .filter { !filters.nearMetro || it.temple.metro != null }
        .filter { !filters.annadana || Facility.ANNADANA in it.temple.facilities }
        .filter { !filters.favouritesOnly || it.isFavourite }
        .filter { !filters.openNow || it.status is OpenStatus.Open }
        .toList()

    return when (filters.sort) {
        TempleSort.NAME -> filtered.sortedBy { it.temple.name.en }
        TempleSort.AREA -> filtered.sortedWith(
            compareBy({ it.temple.area.en }, { it.temple.name.en })
        )
        // Temples with no verified coordinates sink to the bottom rather than pretending
        // to be at distance zero.
        TempleSort.DISTANCE -> filtered.sortedWith(
            compareBy(nullsLast<Double>()) { it.distanceKm }
        )
    }
}

/**
 * Matches across every language we hold, so a Kannada speaker typing "ಬಸವನಗುಡಿ" and an
 * English speaker typing "basavanagudi" both find the same temple.
 */
private fun Temple.matchesQuery(query: String): Boolean {
    if (query.isBlank()) return true
    val needle = query.trim()
    val haystack = buildList {
        addAll(name.allForms())
        addAll(area.allForms())
        add(address)
        add(deity.name)
        bus?.let { add(it.nearestStop); addAll(it.routes) }
        metro?.let { add(it.station) }
    }
    return haystack.any { it.contains(needle, ignoreCase = true) }
}

private fun LocalizedText.allForms(): List<String> = listOfNotNull(en, hi, kn)
