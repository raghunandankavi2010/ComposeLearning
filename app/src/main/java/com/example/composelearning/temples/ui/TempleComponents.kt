package com.example.composelearning.temples.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessible
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Elevator
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NoPhotography
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Wc
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.composelearning.R
import com.example.composelearning.temples.data.Facility
import com.example.composelearning.temples.data.OpenStatus
import com.example.composelearning.temples.data.TemplePhoto
import com.example.composelearning.temples.data.formatDistance
import com.example.composelearning.temples.platform.local

/** Material icon for each facility. Lives in the UI layer so the data model stays icon-free. */
val Facility.icon: ImageVector
    get() = when (this) {
        Facility.WHEELCHAIR -> Icons.Default.Accessible
        Facility.RESTROOMS -> Icons.Default.Wc
        Facility.DRINKING_WATER -> Icons.Default.WaterDrop
        Facility.SHOE_STAND -> Icons.Default.Storefront
        Facility.CLOAK_ROOM -> Icons.Default.Storefront
        Facility.PRASAD_COUNTER -> Icons.Default.Restaurant
        Facility.ANNADANA -> Icons.Default.Restaurant
        Facility.FREE_PARKING -> Icons.Default.LocalParking
        Facility.PAID_PARKING -> Icons.Default.LocalParking
        Facility.QUEUE_SHELTER -> Icons.Default.DirectionsBus
        Facility.MARRIAGE_HALL -> Icons.Default.Place
        Facility.BOOKSTORE -> Icons.Default.MenuBook
        Facility.ELEVATOR -> Icons.Default.Elevator
        Facility.PHOTOGRAPHY_ALLOWED -> Icons.Default.PhotoCamera
        Facility.NO_PHOTOGRAPHY -> Icons.Default.NoPhotography
    }

/**
 * Open / closed pill.
 *
 * Colour carries the state and text repeats it — never colour alone, since a red-green
 * colour-blind user (about 1 in 12 men) would otherwise see two identical grey pills.
 */
@Composable
fun OpenStatusPill(status: OpenStatus, modifier: Modifier = Modifier) {
    val closingSoon = status is OpenStatus.Open &&
        status.closesIn.toMinutes() <= com.example.composelearning.temples.data.CLOSING_SOON_MINUTES
    val target = when {
        closingSoon -> Color(0xFFB26A00)
        status is OpenStatus.Open -> Color(0xFF2E7D32)
        status is OpenStatus.Closed -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.outline
    }
    val tint by animateColorAsState(target, label = "openStatusTint")

    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(tint.copy(alpha = 0.14f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(tint))
        Text(
            status.headlineLabel(),
            style = MaterialTheme.typography.labelMedium,
            color = tint,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * One row in the temple list.
 *
 * Everything on this card answers a travel decision: is it open, how far, and what is it —
 * which is why distance and open state sit above the description rather than below it.
 */
@Composable
fun TempleRow(
    item: TempleListItem,
    selected: Boolean,
    onClick: () -> Unit,
    onToggleFavourite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val temple = item.temple
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            }
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TempleThumbnail(
                photo = temple.photos.firstOrNull(),
                accent = temple.accent,
                contentDescription = temple.name.local()
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    temple.name.local(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    temple.area.local(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OpenStatusPill(item.status)
                    item.distanceKm?.let { km ->
                        Text(
                            stringResource(R.string.label_distance_away, formatDistance(km)),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                if (item.isVisited) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = stringResource(R.string.action_visited),
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * 64dp list thumbnail. Falls back to a tinted glyph rather than an empty grey box, so a
 * temple without a free-licensed photo still reads as a deliberate design.
 */
@Composable
private fun TempleThumbnail(
    photo: TemplePhoto?,
    accent: Color,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(64.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(accent.copy(alpha = 0.25f)),
        contentAlignment = Alignment.Center
    ) {
        if (photo != null) {
            AsyncImage(
                model = photo.url,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(Icons.Default.Place, contentDescription = null, tint = accent)
        }
    }
}

/**
 * Swipeable photo strip with the licence credit burnt into the frame.
 *
 * The credit is not optional decoration: every image here is CC BY / CC BY-SA / public
 * domain from Wikimedia Commons, and the BY licences *require* attribution to be shown
 * wherever the photo appears. Tapping it opens the Commons file page with the full terms.
 */
@Composable
fun PhotoCarousel(
    photos: List<TemplePhoto>,
    onOpenLicense: (TemplePhoto) -> Unit,
    modifier: Modifier = Modifier
) {
    if (photos.isEmpty()) {
        Surface(
            modifier = modifier.fillMaxWidth().aspectRatio(16f / 9f),
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    stringResource(R.string.photo_none),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    val pagerState = rememberPagerState(pageCount = { photos.size })
    Column(modifier = modifier) {
        Box {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)
            ) { page ->
                val photo = photos[page]
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 2.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                ) {
                    AsyncImage(
                        model = photo.url,
                        contentDescription = photo.caption.local(),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Scrim so white credit text stays legible over a bright gopuram.
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    0.6f to Color.Transparent,
                                    1f to Color.Black.copy(alpha = 0.65f)
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .clickable { onOpenLicense(photo) }
                            .padding(12.dp)
                    ) {
                        Text(
                            photo.caption.local(),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            stringResource(R.string.photo_credit, photo.credit, photo.license),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }
            if (photos.size > 1) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.45f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        "${pagerState.currentPage + 1}/${photos.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/** Titled block used for every section of the detail screen. */
@Composable
fun SectionCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            trailing?.invoke()
        }
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                content = content
            )
        }
    }
}

/** Label above value — the densest readable way to show a field on a narrow screen. */
@Composable
fun LabelledValue(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.SemiBold
        )
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FacilityChips(facilities: Set<Facility>, modifier: Modifier = Modifier) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        facilities.forEach { facility ->
            SuggestionChip(
                onClick = {},
                enabled = false,
                label = { Text(stringResource(facility.labelRes)) },
                icon = {
                    Icon(
                        facility.icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurface,
                    disabledIconContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

/** BMTC route numbers as chips — the one detail a first-time visitor actually copies down. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BusRouteChips(routes: List<String>, modifier: Modifier = Modifier) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        routes.forEach { route ->
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    route,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

/** Centred icon + message, used for every empty and error state in the feature. */
@Composable
fun EmptyState(
    icon: ImageVector,
    message: String,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Text(
                message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            action?.invoke()
        }
    }
}

/** Chip that hands a "what's around here" query off to the user's own maps app. */
@Composable
fun NearbySearchChip(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedAssistChip(
        onClick = onClick,
        modifier = modifier,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp)) }
    )
}

/** Icons for the "find X nearby" chips. Grouped here so the set stays visually coherent. */
object NearbyIcons {
    val PoojaItems = Icons.Default.LocalFlorist
    val Food = Icons.Default.Restaurant
    val Parking = Icons.Default.LocalParking
    val Atm = Icons.Default.LocalAtm
    val DressCode = Icons.Default.Checkroom
    val Shop = Icons.Default.Storefront
}

/** Thin vertical rule used to visually connect the trip-planner legs. */
@Composable
fun PlannerConnector(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(2.dp)
            .height(20.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}
