package com.example.composelearning.adaptive

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Static sample data shared by every adaptive demo. Kept deterministic (no
 * network, no random) so the demos render identically across window sizes and
 * configuration changes — which is exactly what you want when you are learning
 * to reason about layout, not data.
 */

@Immutable
data class Email(
    val id: Int,
    val sender: String,
    val subject: String,
    val preview: String,
    val body: String,
    val avatarColor: Color
)

val sampleEmails: List<Email> = listOf(
    Email(
        1,
        "Maya Chen",
        "Q3 design review notes",
        "Thanks everyone for joining — here is the summary of what we agreed…",
        "Thanks everyone for joining the Q3 design review.\n\nWe agreed to ship the new onboarding flow behind a flag, " +
            "revisit the empty states next sprint, and align on the adaptive layout breakpoints (compact / medium / " +
            "expanded) before handing off to engineering.\n\nAction items are in the shared doc.",
        Color(0xFF6750A4)
    ),
    Email(
        2,
        "GitHub",
        "[composelearning] PR #482 ready for review",
        "Raghunandan opened a pull request: Add adaptive layout samples…",
        "Raghunandan opened a pull request.\n\nAdd adaptive layout samples covering list-detail, supporting pane, " +
            "adaptive grids and a reflowing detail screen driven by WindowSizeClass.\n\n6 files changed, +740 −0.",
        Color(0xFF1F2328)
    ),
    Email(
        3,
        "Priya Nair",
        "Lunch tomorrow?",
        "Are you free around 1pm? There is a new place near the office I wanted to try…",
        "Are you free around 1pm tomorrow?\n\nThere is a new place near the office I wanted to try — supposedly great " +
            "filter coffee. Let me know and I will book a table.",
        Color(0xFFB3261E)
    ),
    Email(
        4,
        "Android Developers",
        "What's new in Compose — adaptive layouts",
        "Build apps that look great on phones, foldables, tablets and desktop with a single codebase…",
        "Build apps that look great on phones, foldables, tablets and Chromebooks with one codebase.\n\nThe " +
            "material3-adaptive libraries give you ListDetailPaneScaffold, SupportingPaneScaffold and " +
            "WindowSizeClass so you can react to the space you actually have.",
        Color(0xFF3DDC84)
    ),
    Email(
        5,
        "Banking",
        "Your monthly statement is ready",
        "Your statement for June is now available to view in the app…",
        "Your statement for June is now available.\n\nNo action is required. You can view the full breakdown of " +
            "transactions, balances and upcoming payments in the app.",
        Color(0xFF00658F)
    ),
    Email(
        6,
        "Liam O'Brien",
        "Re: Tablet layout feedback",
        "On the expanded width the detail pane feels a little cramped — could we…",
        "On the expanded width the detail pane feels a little cramped — could we give it a bit more horizontal " +
            "padding and cap the text measure at ~70 characters? Reads much better on a 12\" tablet.",
        Color(0xFF7D5260)
    ),
    Email(
        7,
        "Calendar",
        "Reminder: Sprint planning at 10:00",
        "Sprint planning starts in 30 minutes in the Olympus room…",
        "Sprint planning starts in 30 minutes in the Olympus room.\n\nAgenda: review the adaptive layout spike, " +
            "estimate the foldable support work, and confirm the release cut date.",
        Color(0xFF386A20)
    ),
    Email(
        8,
        "Sofia Rossi",
        "Photos from the offsite",
        "Shared an album with you — 24 photos from the team offsite last week…",
        "Shared an album with you — 24 photos from the team offsite last week.\n\nThe group shot on the rooftop came " +
            "out really well. Feel free to add your own and tag people.",
        Color(0xFF984061)
    )
)

@Immutable
data class Photo(
    val id: Int,
    val title: String,
    val color: Color,
    val aspect: Float
)

/** A small gallery. Colors stand in for real images so the demo needs no network. */
val samplePhotos: List<Photo> = List(30) { index ->
    val palette = listOf(
        Color(0xFF6750A4), Color(0xFF00658F), Color(0xFFB3261E), Color(0xFF386A20),
        Color(0xFF7D5260), Color(0xFF984061), Color(0xFF8C4A00), Color(0xFF006A60)
    )
    Photo(
        id = index,
        title = "Frame ${index + 1}",
        color = palette[index % palette.size],
        aspect = listOf(1f, 0.75f, 1.33f, 1f)[index % 4]
    )
}

@Immutable
data class ProductSpec(val label: String, val value: String)

@Immutable
data class Product(
    val name: String,
    val tagline: String,
    val price: String,
    val accent: Color,
    val description: String,
    val specs: List<ProductSpec>
)

val sampleProduct = Product(
    name = "Aurora Foldable",
    tagline = "One device. Every size class.",
    price = "₹1,29,999",
    accent = Color(0xFF6750A4),
    description = "A foldable that goes from a 6.2\" cover display (compact) to a 7.6\" inner screen (expanded) — the " +
        "perfect device to test adaptive layouts on. Your UI should reflow as the user unfolds it, without losing " +
        "scroll position or selection state.",
    specs = listOf(
        ProductSpec("Cover display", "6.2\" • 120Hz • compact width"),
        ProductSpec("Inner display", "7.6\" • 120Hz • expanded width"),
        ProductSpec("Fold posture", "Reported via WindowAdaptiveInfo"),
        ProductSpec("Chip", "Snapdragon 8 Elite"),
        ProductSpec("RAM", "12 GB"),
        ProductSpec("Storage", "512 GB"),
        ProductSpec("Battery", "4400 mAh"),
        ProductSpec("Weight", "239 g")
    )
)

@Immutable
data class RelatedLink(val title: String, val meta: String)

val sampleRelatedLinks: List<RelatedLink> = listOf(
    RelatedLink("ListDetailPaneScaffold", "Canonical two-pane pattern"),
    RelatedLink("SupportingPaneScaffold", "Primary + supporting content"),
    RelatedLink("WindowSizeClass", "Compact / Medium / Expanded breakpoints"),
    RelatedLink("currentWindowAdaptiveInfo()", "Size class + fold posture"),
    RelatedLink("GridCells.Adaptive", "Column count from available width")
)
