package com.example.composelearning.temples.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.composelearning.R
import com.example.composelearning.temples.data.TempleRepository
import com.example.composelearning.temples.platform.AppLanguage
import com.example.composelearning.temples.platform.LanguagePreference
import com.example.composelearning.temples.platform.TempleIntents
import com.example.composelearning.temples.platform.currentLanguageTag

/**
 * Language picker plus the credits this feature is legally and factually obliged to show.
 *
 * Selecting a language calls into [LanguagePreference], which writes to the platform's
 * per-app locale store. The framework then recreates the activity, and every
 * `values-*` lookup plus every `LocalizedText.resolve` call flips together — there is no
 * separate in-app translation state to keep in sync.
 */
@Composable
fun LanguageScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val preference = remember(context) { LanguagePreference(context) }
    val activeTag = currentLanguageTag()
    val active = AppLanguage.fromTag(activeTag)

    // Photo attribution is a licence condition, not a nicety: CC BY / CC BY-SA require the
    // author to be named wherever the image is shown, so we also list every source in one place.
    val photoCount = remember { TempleRepository.temples.sumOf { it.photos.size } }
    val licenses = remember {
        TempleRepository.temples
            .flatMap { it.photos }
            .map { it.license }
            .distinct()
            .sorted()
    }

    Surface(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "language") {
                SectionCard(
                    title = stringResource(R.string.language_picker_title),
                    icon = Icons.Default.Translate
                ) {
                    Text(
                        stringResource(R.string.language_picker_body),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    AppLanguage.entries.forEach { language ->
                        LanguageRow(
                            language = language,
                            selected = language == active,
                            onSelect = { preference.set(language) }
                        )
                    }
                    HorizontalDivider()
                    TextButton(onClick = { preference.clear() }) {
                        Text(stringResource(R.string.language_follow_system))
                    }
                    Text(
                        stringResource(R.string.language_partial_note),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            item(key = "photo_credits") {
                SectionCard(
                    title = stringResource(R.string.section_photos),
                    icon = Icons.Default.PhotoCamera
                ) {
                    Text(
                        stringResource(R.string.credits_photos_body, photoCount),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        stringResource(
                            R.string.credits_licences,
                            licenses.joinToString(", ")
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(
                        onClick = {
                            TempleIntents.openUrl(
                                context,
                                "https://commons.wikimedia.org/wiki/Category:Hindu_temples_in_Bangalore"
                            )
                        }
                    ) {
                        Text(stringResource(R.string.credits_browse_source))
                    }
                }
            }

            item(key = "data_note") {
                SectionCard(
                    title = stringResource(R.string.label_data_confidence),
                    icon = Icons.Default.Info
                ) {
                    Text(
                        stringResource(R.string.disclaimer),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        stringResource(R.string.credits_data_body),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item(key = "spacer") { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun LanguageRow(
    language: AppLanguage,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Column(modifier = Modifier.weight(1f)) {
            // Endonym first: a Kannada speaker looking for their language scans for
            // "ಕನ್ನಡ", not for the word "Kannada" written in English.
            Text(
                language.endonym,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
            Text(
                stringResource(language.labelRes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (selected) {
            Icon(
                Icons.Default.Translate,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
