package com.example.composelearning.temples.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.composelearning.R
import com.example.composelearning.temples.data.TempleRepository
import com.example.composelearning.temples.platform.TempleIntents
import com.example.composelearning.temples.platform.local
import java.time.LocalDateTime
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

/**
 * A year-round festival calendar across every temple in the dataset, rotated to start at the
 * current month.
 *
 * Dates are given as windows ("Nov–Dec, Karthika masa") rather than days on purpose: these
 * are lunar-calendar festivals whose Gregorian date moves each year, and a hard-coded date
 * would be confidently wrong every year but one.
 */
@Composable
fun FestivalsScreen(
    now: LocalDateTime,
    onOpenTemple: (String) -> Unit,
    onMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val noCalendar = stringResource(R.string.error_no_calendar)
    val entries = remember(now.monthValue) { TempleRepository.festivalCalendar(now) }
    // Month names follow the app locale, not the system one, so they match the rest of the UI.
    val locale: Locale = LocalConfiguration.current.locales[0]

    Surface(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(key = "intro") {
                Text(
                    stringResource(R.string.festivals_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            items(entries, key = { "${it.temple.id}-${it.festival.name.en}" }) { entry ->
                val templeName = entry.temple.name.local()
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenTemple(entry.temple.id) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Celebration,
                            contentDescription = null,
                            tint = entry.temple.accent,
                            modifier = Modifier.size(24.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                entry.festival.name.local(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                entry.festival.whenApprox.local(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                stringResource(R.string.festivals_at, templeName),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            entry.festival.usualMonth?.let { month ->
                                Text(
                                    Month.of(month)
                                        .getDisplayName(TextStyle.FULL_STANDALONE, locale),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                        val festivalName = entry.festival.name.local()
                        val festivalWhen = entry.festival.whenApprox.local()
                        TextButton(
                            onClick = {
                                val added = TempleIntents.addEventToCalendar(
                                    context = context,
                                    title = "$festivalName — $templeName",
                                    description = festivalWhen,
                                    location = entry.temple.address,
                                    startMillis = entry.festival.approximateStartMillis()
                                )
                                if (!added) onMessage(noCalendar)
                            }
                        ) {
                            Text(stringResource(R.string.action_add_to_calendar))
                        }
                    }
                }
            }
        }
    }
}
