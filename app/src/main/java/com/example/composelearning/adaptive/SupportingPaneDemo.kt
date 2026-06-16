package com.example.composelearning.adaptive

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.SupportingPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.NavigableSupportingPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberSupportingPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * The **supporting pane** pattern: one piece of primary content (here, an
 * article) plus secondary content that *supports* it (related links / metadata).
 *
 * [NavigableSupportingPaneScaffold] behaves like this:
 *  - **Expanded width**: the supporting pane sits permanently beside the main
 *    content. The "Show related" button is unnecessary but harmless.
 *  - **Compact / medium width**: only the main pane shows. Tapping "Show related"
 *    navigates to the supporting pane; back returns to the article.
 *
 * Use this (rather than list–detail) when the second pane is *auxiliary* — filters,
 * "related items", a table of contents — not a peer you navigate between.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun SupportingPaneDemo(modifier: Modifier = Modifier) {
    val navigator = rememberSupportingPaneScaffoldNavigator()
    val scope = rememberCoroutineScope()

    // Is the supporting pane already laid out next to the main pane? On expanded
    // widths it is, so we hide the "Show related" button as redundant.
    val supportingVisible =
        navigator.scaffoldValue[SupportingPaneScaffoldRole.Supporting] == PaneAdaptedValue.Expanded

    NavigableSupportingPaneScaffold(
        modifier = modifier,
        navigator = navigator,
        mainPane = {
            AnimatedPane {
                ArticlePane(
                    showSupportingButton = !supportingVisible,
                    onShowSupporting = {
                        scope.launch {
                            navigator.navigateTo(SupportingPaneScaffoldRole.Supporting)
                        }
                    }
                )
            }
        },
        supportingPane = {
            AnimatedPane {
                SupportingLinksPane()
            }
        }
    )
}

@Composable
private fun ArticlePane(
    showSupportingButton: Boolean,
    onShowSupporting: () -> Unit
) {
    Surface {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.widthIn(max = 720.dp)) {
                Text(
                    "Designing for every screen",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    "Why one layout is never enough",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    "\nUsers run your app on phones, foldables, tablets, Chromebooks and " +
                        "desktop windows that can be freely resized. Designing a single fixed " +
                        "layout means it is wrong almost everywhere except the device you " +
                        "tested on.\n\nThe adaptive libraries give you two tools. WindowSizeClass " +
                        "tells you how much space you have in coarse buckets — compact, medium, " +
                        "expanded. The pane scaffolds then turn that into concrete multi-pane " +
                        "layouts you would otherwise hand-roll with BoxWithConstraints.\n\nThe " +
                        "golden rule: react to the size you are actually given, never to the " +
                        "physical device type. A phone in landscape, a small free-form window " +
                        "and a folded foldable can all be 'compact'.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            if (showSupportingButton) {
                Button(onClick = onShowSupporting) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    Text("  Show related topics")
                }
            }
        }
    }
}

@Composable
private fun SupportingLinksPane() {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Info, contentDescription = null)
                Text("Related topics", style = MaterialTheme.typography.titleLarge)
            }
            sampleRelatedLinks.forEach { link ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    ListItem(
                        headlineContent = { Text(link.title) },
                        supportingContent = { Text(link.meta) }
                    )
                }
            }
            Text(
                "On a wide screen this pane is always visible. On a phone you reached it " +
                    "by tapping the button — press back to return to the article.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
