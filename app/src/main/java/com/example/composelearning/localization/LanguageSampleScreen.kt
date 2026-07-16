package com.example.composelearning.localization

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.composelearning.R
import com.example.composelearning.ui.theme.ComposeLearningTheme
import java.util.*
import kotlinx.coroutines.launch

/**
 * Data class representing supported languages.
 */
data class Language(val name: String, val code: String)

val SupportedLanguages = listOf(
    Language("English", "en"),
    Language("हिंदी", "hi"),
    Language("తెలుగు", "te"),
    Language("ಕನ್ನಡ", "kn"),
    Language("मराठी", "mr"),
    Language("ગુજરાતી", "gu"),
)

/**
 * A helper composable that overrides the LocalContext with a specific locale.
 * This allows stringResource(id) to resolve to the selected language dynamically
 * within this scope.
 */
@Composable
fun LocaleWrapper(localeCode: String, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val locale = Locale.forLanguageTag(localeCode)

    // We create a new configuration based on the current one and set the desired locale
    val newConfiguration = Configuration(configuration).apply {
        setLocale(locale)
    }

    // Create a new context with the overridden configuration
    val localeContext = context.createConfigurationContext(newConfiguration)

    CompositionLocalProvider(LocalContext provides localeContext) {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSampleScreen(onBack: () -> Unit) {
    var currentLanguage by remember { mutableStateOf(SupportedLanguages[0]) }
    var name by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    ComposeLearningTheme {
        // Wrap the entire UI in LocaleWrapper so all stringResource calls are reactive to currentLanguage
        LocaleWrapper(currentLanguage.code) {
            val successMessage = stringResource(id = R.string.success_message)

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(stringResource(id = R.string.app_title)) },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                },
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Language Selection Section
                    Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Select Language / भाषा चुनें",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        // Row of chips for language selection
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SupportedLanguages.forEach { language ->
                                FilterChip(
                                    selected = currentLanguage == language,
                                    onClick = { currentLanguage = language },
                                    label = { Text(language.name) }
                                )
                            }
                        }
                    }

                    HorizontalDivider()

                    // Input Section
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(stringResource(id = R.string.input_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Action Section
                    Button(
                        onClick = {
                            scope.launch {
                                snackbarHostState.showSnackbar(successMessage)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = stringResource(id = R.string.button_submit),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        maxItemsInEachRow = 3,
        content = { content() }
    )
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun LanguageSampleScreenPreview() {
    LanguageSampleScreen(onBack = {})
}
