package com.example.composelearning.styles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * ---------------------------------------------------------------------------
 * LAYER 3 — THE PREVIEWABLE SHOWCASE SCREEN
 * ---------------------------------------------------------------------------
 *
 * Demonstrates the Styles API: the three global variants, an enabled/disabled
 * state, and a fully ad-hoc override built with `.copy(...)` to prove the API
 * is open for extension without touching the design system.
 *
 * The screen is intentionally stateless (it only takes an [onBack] callback),
 * which keeps it trivially previewable and testable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StylesShowcaseScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Styles API Showcase") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            Section(title = "Global variants") {
                // Each button opts into a specific variant explicitly.
                CustomButton(
                    text = "Primary",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    style = AppButtonStyles.Primary,
                )
                CustomButton(
                    text = "Secondary",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    style = AppButtonStyles.Secondary,
                )
                CustomButton(
                    text = "Danger",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    style = AppButtonStyles.Danger,
                )
            }

            Section(title = "Theme default (no style passed)") {
                // No `style` argument → reads AppTheme.styles.button from the
                // nearest CustomTheme. Swap the provided AppStyles to restyle
                // every default button at once.
                CustomButton(
                    text = "Uses AppTheme.styles.button",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Section(title = "Disabled states") {
                CustomButton(
                    text = "Primary (disabled)",
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    style = AppButtonStyles.Primary,
                )
                CustomButton(
                    text = "Danger (disabled)",
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    style = AppButtonStyles.Danger,
                )
            }

            Section(title = "Ad-hoc override via copy()") {
                CustomButton(
                    text = "Pill-shaped primary",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    style = AppButtonStyles.Primary.copy(
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
                    ),
                )
            }
        }
    }
}

@Composable
private fun Section(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        content()
    }
}

/**
 * Isolated preview — wrapped in [CustomTheme] so `AppTheme.styles` resolves and
 * the screen renders exactly as it would at runtime, directly in Android Studio.
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun StylesShowcaseScreenPreview() {
    CustomTheme {
        StylesShowcaseScreen(onBack = {})
    }
}
