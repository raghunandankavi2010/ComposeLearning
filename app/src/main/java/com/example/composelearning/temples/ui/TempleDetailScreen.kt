package com.example.composelearning.temples.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Accessible
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.composelearning.R
import com.example.composelearning.temples.data.DonationChannel
import com.example.composelearning.temples.data.Festival
import com.example.composelearning.temples.data.NearbyPlace
import com.example.composelearning.temples.data.Ritual
import com.example.composelearning.temples.data.Temple
import com.example.composelearning.temples.data.formatDistance
import com.example.composelearning.temples.data.nextRitualAfter
import com.example.composelearning.temples.data.windowsOn
import com.example.composelearning.temples.platform.TempleIntents
import com.example.composelearning.temples.platform.local
import java.time.LocalDate
import java.time.ZoneId

/**
 * The temple page.
 *
 * Ordered by what a visitor needs, in the order they need it: can I go now (status), how do
 * I get there (buses, metro), what happens when I arrive (poojas, dress code), and what is
 * around it (pooja stalls, food, donations). The prose "About" section deliberately sits
 * below the practical answers.
 *
 * Every outward action is an implicit intent handed to another app — see [TempleIntents].
 */
@Composable
fun TempleDetailScreen(
    item: TempleListItem,
    showBack: Boolean,
    onBack: () -> Unit,
    onToggleFavourite: () -> Unit,
    onToggleVisited: () -> Unit,
    onMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val temple = item.temple
    val context = LocalContext.current

    // Resolved up front: these are needed inside click lambdas, where composition is over.
    val noMaps = stringResource(R.string.error_no_maps_app)
    val noDialler = stringResource(R.string.error_no_dialler)
    val noBrowser = stringResource(R.string.error_no_browser)
    val noCalendar = stringResource(R.string.error_no_calendar)
    val templeName = temple.name.local()
    val mapsFallbackQuery = "$templeName, ${temple.address}"

    Surface(modifier = modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item(key = "header") {
                Box {
                    PhotoCarousel(
                        photos = temple.photos,
                        onOpenLicense = { photo ->
                            if (!TempleIntents.openUrl(context, photo.sourceUrl)) onMessage(noBrowser)
                        }
                    )
                    if (showBack) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .padding(8.dp)
                                .background(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.action_back)
                            )
                        }
                    }
                }
            }

            item(key = "title") {
                TempleTitleBlock(
                    item = item,
                    onToggleFavourite = onToggleFavourite,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }

            item(key = "actions") {
                PrimaryActions(
                    temple = temple,
                    isVisited = item.isVisited,
                    onToggleVisited = onToggleVisited,
                    onNavigate = {
                        if (!TempleIntents.navigate(
                                context, temple.location, templeName, mapsFallbackQuery
                            )
                        ) onMessage(noMaps)
                    },
                    onCall = { phone ->
                        if (!TempleIntents.dial(context, phone)) onMessage(noDialler)
                    },
                    onWebsite = { url ->
                        if (!TempleIntents.openUrl(context, url)) onMessage(noBrowser)
                    },
                    onShare = {
                        val body = buildShareText(temple, templeName)
                        if (!TempleIntents.share(context, templeName, body)) onMessage(noBrowser)
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item(key = "timings") {
                DetailSection {
                    TimingsSection(item = item)
                }
            }

            if (temple.rituals.isNotEmpty()) {
                item(key = "rituals") {
                    DetailSection {
                        SectionCard(
                            stringResource(R.string.section_rituals),
                            Icons.Default.AccessTime
                        ) {
                            temple.rituals.forEach { RitualRow(it) }
                        }
                    }
                }
            }

            item(key = "reach") {
                DetailSection {
                    ReachSection(
                        item = item,
                        onNavigate = {
                            if (!TempleIntents.navigate(
                                    context, temple.location, templeName, mapsFallbackQuery
                                )
                            ) onMessage(noMaps)
                        },
                        onShowOnMap = {
                            if (!TempleIntents.openMap(
                                    context, temple.location, templeName, mapsFallbackQuery
                                )
                            ) onMessage(noMaps)
                        }
                    )
                }
            }

            if (temple.facilities.isNotEmpty() || temple.dressCode != null) {
                item(key = "facilities") {
                    DetailSection {
                        SectionCard(
                            stringResource(R.string.section_facilities),
                            Icons.Default.Accessible
                        ) {
                            if (temple.facilities.isNotEmpty()) FacilityChips(temple.facilities)
                            temple.dressCode?.let { code ->
                                if (temple.facilities.isNotEmpty()) HorizontalDivider()
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        Icons.Default.Checkroom,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                    LabelledValue(
                                        stringResource(R.string.section_dress_code),
                                        code.local()
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item(key = "pooja_items") {
                DetailSection {
                    NearbySection(
                        title = stringResource(R.string.section_pooja_items),
                        icon = Icons.Default.LocalFlorist,
                        places = temple.poojaItemShops,
                        searchLabel = stringResource(R.string.nearby_find_pooja_items),
                        searchIcon = NearbyIcons.PoojaItems,
                        query = stringResource(R.string.nearby_query_pooja_items),
                        temple = temple,
                        onMessage = onMessage,
                        noMapsMessage = noMaps
                    )
                }
            }

            item(key = "eat") {
                DetailSection {
                    NearbySection(
                        title = stringResource(R.string.section_eat),
                        icon = Icons.Default.Restaurant,
                        places = temple.eateries,
                        searchLabel = stringResource(R.string.nearby_find_food),
                        searchIcon = NearbyIcons.Food,
                        query = stringResource(R.string.nearby_query_food),
                        temple = temple,
                        onMessage = onMessage,
                        noMapsMessage = noMaps
                    )
                }
            }

            if (temple.donation.isNotEmpty()) {
                item(key = "donate") {
                    DetailSection {
                        DonationSection(
                            channels = temple.donation,
                            onOpenUrl = { url ->
                                if (!TempleIntents.openUrl(context, url)) onMessage(noBrowser)
                            }
                        )
                    }
                }
            }

            if (temple.festivals.isNotEmpty()) {
                item(key = "festivals") {
                    DetailSection {
                        SectionCard(
                            stringResource(R.string.section_festivals),
                            Icons.Default.Celebration
                        ) {
                            temple.festivals.forEach { festival ->
                                val festivalName = festival.name.local()
                                val festivalWhen = festival.whenApprox.local()
                                FestivalRow(
                                    festival = festival,
                                    onAddReminder = {
                                        val added = TempleIntents.addEventToCalendar(
                                            context = context,
                                            title = "$festivalName — $templeName",
                                            description = festivalWhen,
                                            location = temple.address,
                                            startMillis = festival.approximateStartMillis()
                                        )
                                        if (!added) onMessage(noCalendar)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            item(key = "about") {
                DetailSection {
                    SectionCard(
                        stringResource(R.string.section_about),
                        Icons.AutoMirrored.Filled.MenuBook
                    ) {
                        Text(temple.about.local(), style = MaterialTheme.typography.bodyMedium)
                        temple.builtIn?.let {
                            LabelledValue(stringResource(R.string.label_built), it)
                        }
                        LabelledValue(stringResource(R.string.label_address), temple.address)
                    }
                }
            }

            item(key = "disclaimer") {
                DetailSection {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            stringResource(R.string.disclaimer),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "${stringResource(R.string.label_data_confidence)}: " +
                                stringResource(temple.confidence.labelRes),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            item(key = "bottom_spacer") { Spacer(Modifier.height(24.dp)) }
        }
    }
}

/** Uniform gutter for every detail section, applied in one place. */
@Composable
private fun DetailSection(content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) { content() }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TempleTitleBlock(
    item: TempleListItem,
    onToggleFavourite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val temple = item.temple
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    temple.name.local(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    temple.area.local(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onToggleFavourite) {
                Icon(
                    if (item.isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = stringResource(
                        if (item.isFavourite) R.string.action_unsave else R.string.action_save
                    ),
                    tint = if (item.isFavourite) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.outline
                    }
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OpenStatusPill(item.status)
            AssistChip(
                onClick = {},
                enabled = false,
                label = { Text(stringResource(temple.deity.labelRes)) }
            )
            item.distanceKm?.let { km ->
                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = {
                        Text(stringResource(R.string.label_distance_away, formatDistance(km)))
                    }
                )
            }
        }
    }
}

/**
 * The action row. "Navigate" is a filled button and everything else is tonal/outlined,
 * because getting there is the action nine out of ten visitors came for.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PrimaryActions(
    temple: Temple,
    isVisited: Boolean,
    onToggleVisited: () -> Unit,
    onNavigate: () -> Unit,
    onCall: (String) -> Unit,
    onWebsite: (String) -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(onClick = onNavigate) {
            Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(6.dp))
            Text(stringResource(R.string.action_navigate))
        }
        temple.phone?.let { phone ->
            FilledTonalButton(onClick = { onCall(phone) }) {
                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(6.dp))
                Text(stringResource(R.string.action_call))
            }
        }
        temple.website?.let { url ->
            OutlinedButton(onClick = { onWebsite(url) }) {
                Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(6.dp))
                Text(stringResource(R.string.action_website))
            }
        }
        OutlinedButton(onClick = onShare) {
            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(6.dp))
            Text(stringResource(R.string.action_share))
        }
        OutlinedButton(onClick = onToggleVisited) {
            Text(
                stringResource(
                    if (isVisited) R.string.action_visited else R.string.action_mark_visited
                )
            )
        }
    }
}

@Composable
private fun TimingsSection(item: TempleListItem) {
    val temple = item.temple
    val today = item.now.dayOfWeek
    val windows = temple.windowsOn(today)
    val nextRitual = temple.nextRitualAfter(item.now.toLocalTime())
    val formatter = rememberTimeFormatter()

    SectionCard(stringResource(R.string.section_timings), Icons.Default.AccessTime) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OpenStatusPill(item.status)
            Text(item.status.summaryLabel(), style = MaterialTheme.typography.bodyMedium)
        }
        HorizontalDivider()
        if (windows.isEmpty()) {
            Text(
                stringResource(R.string.timings_closed_today),
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    today.shortLabel(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Column {
                    windows.forEach { window ->
                        Text(window.rangeLabel(), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
        if (nextRitual?.time != null) {
            val ritualName = nextRitual.name.local()
            Text(
                stringResource(
                    R.string.status_next_ritual,
                    ritualName,
                    nextRitual.time.format(formatter)
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        if (temple.busiestDays.isNotEmpty()) {
            val sortedDays = temple.busiestDays.sorted()
            val labels = mutableListOf<String>()
            for (day in sortedDays) {
                labels.add(day.shortLabel())
            }
            val busyDays = labels.joinToString(", ")
            Text(
                "${stringResource(R.string.label_busiest_days)}: $busyDays",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RitualRow(ritual: Ritual) {
    val formatter = rememberTimeFormatter()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            ritual.time?.format(formatter) ?: "—",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            ritual.name.local(),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        ritual.priceInr?.let { price ->
            Text(
                "₹$price",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun ReachSection(
    item: TempleListItem,
    onNavigate: () -> Unit,
    onShowOnMap: () -> Unit
) {
    val temple = item.temple
    SectionCard(stringResource(R.string.section_reach), Icons.Default.Place) {
        temple.bus?.let { bus ->
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.DirectionsBus,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        stringResource(R.string.label_bus_routes),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (bus.routes.isNotEmpty()) BusRouteChips(bus.routes)
                LabelledValue(stringResource(R.string.label_nearest_stop), bus.nearestStop)
                bus.note?.let {
                    Text(
                        it.local(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        temple.metro?.let { metro ->
            HorizontalDivider()
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Train,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = metro.line.brand
                )
                Text(
                    stringResource(
                        R.string.label_metro_walk,
                        metro.station,
                        stringResource(metro.line.labelRes),
                        metro.walkMinutes
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        temple.parking?.let {
            HorizontalDivider()
            LabelledValue(stringResource(R.string.label_parking), it.local())
        }
        HorizontalDivider()
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onNavigate) {
                Icon(
                    Icons.Default.Navigation,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.size(6.dp))
                Text(stringResource(R.string.action_navigate))
            }
            OutlinedButton(onClick = onShowOnMap) {
                Text(stringResource(R.string.action_map))
            }
        }
    }
}

/**
 * A "what's around the temple" block.
 *
 * Curated entries come from the dataset; the chip below them hands the same question to the
 * user's maps app, which has live opening hours and new shops we could never keep current.
 */
@Composable
private fun NearbySection(
    title: String,
    icon: ImageVector,
    places: List<NearbyPlace>,
    searchLabel: String,
    searchIcon: ImageVector,
    query: String,
    temple: Temple,
    onMessage: (String) -> Unit,
    noMapsMessage: String
) {
    val context = LocalContext.current
    SectionCard(title, icon) {
        places.forEach { place ->
            Column {
                Text(
                    place.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    place.note.local(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (places.isNotEmpty()) HorizontalDivider()
        NearbySearchChip(
            label = searchLabel,
            icon = searchIcon,
            onClick = {
                val searched = TempleIntents.searchNear(context, temple.location, query)
                if (!searched) onMessage(noMapsMessage)
            }
        )
        Text(
            stringResource(R.string.nearby_maps_note),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
private fun DonationSection(
    channels: List<DonationChannel>,
    onOpenUrl: (String) -> Unit
) {
    SectionCard(stringResource(R.string.section_donate), Icons.Default.VolunteerActivism) {
        channels.forEach { channel ->
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    stringResource(channel.kind.labelRes),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(channel.label.local(), style = MaterialTheme.typography.bodyMedium)
                channel.detail?.let {
                    Text(
                        it.local(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                channel.url?.let { url ->
                    TextButton(
                        onClick = { onOpenUrl(url) },
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                    ) {
                        Text(stringResource(R.string.donate_open_link))
                    }
                }
            }
        }
        HorizontalDivider()
        Text(
            stringResource(R.string.donate_disclaimer),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FestivalRow(festival: Festival, onAddReminder: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                festival.name.local(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                festival.whenApprox.local(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            festival.note?.let {
                Text(
                    it.local(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        TextButton(onClick = onAddReminder) {
            Text(stringResource(R.string.action_add_to_calendar))
        }
    }
}

/**
 * A rough start date for the calendar reminder: the 1st of the month this festival usually
 * falls in, this year if still ahead, next year otherwise.
 *
 * Deliberately approximate — these are lunar-calendar festivals whose Gregorian date moves
 * every year, and the calendar app lets the user drag it to the real date once the panchanga
 * is out. Guessing a precise day would be false precision.
 */
internal fun Festival.approximateStartMillis(): Long {
    val today = LocalDate.now()
    val month = usualMonth ?: today.monthValue
    val year = if (month >= today.monthValue) today.year else today.year + 1
    return LocalDate.of(year, month, 1)
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
}

/** Share text: name, area, timings hint and a maps link anyone can open. */
private fun buildShareText(temple: Temple, localisedName: String): String = buildString {
    appendLine(localisedName)
    appendLine(temple.address)
    temple.location?.let {
        appendLine("https://www.google.com/maps/search/?api=1&query=${it.lat},${it.lng}")
    }
    temple.website?.let { appendLine(it) }
}
