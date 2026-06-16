package com.example.composelearning.adaptive

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.example.composelearning.ui.theme.ComposeLearningTheme

/**
 * Android Studio previews for the whole adaptive module.
 *
 * ### How to read these
 * The adaptive APIs (`currentWindowAdaptiveInfo()`, the pane scaffolds and the
 * navigation suite) react to the **size the preview reports**, so each adaptive
 * screen has at least two previews:
 *  - a **Compact** one (`widthDp = 360`) — the phone / single-pane state
 *  - an **Expanded** one (`widthDp = 1100`) — the tablet / two-pane / drawer state
 *
 * Because they are size-driven, you must set **both** `widthDp` and `heightDp`
 * (the screens are `fillMaxSize`, so without a height they would render empty).
 *
 * [AllScreenSizesPreview] uses the `@PreviewScreenSizes` *multipreview* — one
 * annotation that renders phone, foldable, tablet and desktop together, which is
 * the quickest way to eyeball an adaptive screen across form factors.
 *
 * Everything is wrapped in [ComposeLearningTheme] so colors/typography match the app.
 */

// Reusable sizes so the previews stay consistent and easy to tweak.
private const val COMPACT_W = 360
private const val MEDIUM_W = 720
private const val EXPANDED_W = 1100
private const val PREVIEW_H = 820

@Composable
private fun PreviewHost(content: @Composable () -> Unit) {
    ComposeLearningTheme { content() }
}

// ── Hub catalog ─────────────────────────────────────────────────────────────
@Preview(name = "Hub catalog", widthDp = COMPACT_W, heightDp = PREVIEW_H, showBackground = true)
@Composable
private fun HubPreview() = PreviewHost { AdaptiveLayoutsHub() }

// ── 1 · List–Detail ─────────────────────────────────────────────────────────
@Preview(name = "1 List–Detail · Compact", widthDp = COMPACT_W, heightDp = PREVIEW_H, showBackground = true)
@Composable
private fun ListDetailCompactPreview() = PreviewHost { ListDetailDemo(Modifier.fillMaxSize()) }

@Preview(name = "1 List–Detail · Expanded", widthDp = EXPANDED_W, heightDp = PREVIEW_H, showBackground = true)
@Composable
private fun ListDetailExpandedPreview() = PreviewHost { ListDetailDemo(Modifier.fillMaxSize()) }

// ── 2 · Supporting Pane ───────────────────────────────────────────────────────
@Preview(name = "2 Supporting · Compact", widthDp = COMPACT_W, heightDp = PREVIEW_H, showBackground = true)
@Composable
private fun SupportingCompactPreview() = PreviewHost { SupportingPaneDemo(Modifier.fillMaxSize()) }

@Preview(name = "2 Supporting · Expanded", widthDp = EXPANDED_W, heightDp = PREVIEW_H, showBackground = true)
@Composable
private fun SupportingExpandedPreview() = PreviewHost { SupportingPaneDemo(Modifier.fillMaxSize()) }

// ── 3 · Adaptive Grid ─────────────────────────────────────────────────────────
@Preview(name = "3 Grid · Compact", widthDp = COMPACT_W, heightDp = PREVIEW_H, showBackground = true)
@Composable
private fun GridCompactPreview() = PreviewHost { AdaptiveGridDemo(Modifier.fillMaxSize()) }

@Preview(name = "3 Grid · Expanded", widthDp = EXPANDED_W, heightDp = PREVIEW_H, showBackground = true)
@Composable
private fun GridExpandedPreview() = PreviewHost { AdaptiveGridDemo(Modifier.fillMaxSize()) }

// ── 4 · Reflowing Detail (WindowSizeClass) ─────────────────────────────────────
@Preview(name = "4 Reflow · Compact", widthDp = COMPACT_W, heightDp = PREVIEW_H, showBackground = true)
@Composable
private fun ReflowCompactPreview() = PreviewHost { WindowSizeClassDemo(Modifier.fillMaxSize()) }

@Preview(name = "4 Reflow · Expanded", widthDp = EXPANDED_W, heightDp = PREVIEW_H, showBackground = true)
@Composable
private fun ReflowExpandedPreview() = PreviewHost { WindowSizeClassDemo(Modifier.fillMaxSize()) }

// ── 5 · Navigation Suite (bar → rail → drawer) ─────────────────────────────────
@Preview(name = "5 NavSuite · Compact (bar)", widthDp = COMPACT_W, heightDp = PREVIEW_H, showBackground = true)
@Composable
private fun NavSuiteCompactPreview() = PreviewHost { AdaptiveNavigationSuiteDemo(Modifier.fillMaxSize()) }

@Preview(name = "5 NavSuite · Medium (rail)", widthDp = MEDIUM_W, heightDp = PREVIEW_H, showBackground = true)
@Composable
private fun NavSuiteMediumPreview() = PreviewHost { AdaptiveNavigationSuiteDemo(Modifier.fillMaxSize()) }

@Preview(name = "5 NavSuite · Expanded (drawer)", widthDp = EXPANDED_W, heightDp = PREVIEW_H, showBackground = true)
@Composable
private fun NavSuiteExpandedPreview() = PreviewHost { AdaptiveNavigationSuiteDemo(Modifier.fillMaxSize()) }

// ── 6 · Adaptive Drawer (modal ↔ permanent) ────────────────────────────────────
@Preview(name = "6 Drawer · Compact (modal)", widthDp = COMPACT_W, heightDp = PREVIEW_H, showBackground = true)
@Composable
private fun DrawerModalPreview() = PreviewHost { AdaptiveDrawerDemo(Modifier.fillMaxSize()) }

@Preview(name = "6 Drawer · Expanded (permanent)", widthDp = EXPANDED_W, heightDp = PREVIEW_H, showBackground = true)
@Composable
private fun DrawerPermanentPreview() = PreviewHost { AdaptiveDrawerDemo(Modifier.fillMaxSize()) }

// ── Multipreview: every reference device at once ───────────────────────────────
@PreviewScreenSizes
@Composable
private fun AllScreenSizesPreview() = PreviewHost { ListDetailDemo(Modifier.fillMaxSize()) }
