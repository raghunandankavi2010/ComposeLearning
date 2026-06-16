package com.example.composelearning.adaptive

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass

/**
 * The three coarse width buckets the whole adaptive system is built on. We map
 * the raw [WindowSizeClass] into this enum so demos can `when` over it cleanly.
 */
enum class AdaptiveWidthClass(val label: String) {
    COMPACT("Compact (< 600dp)"),
    MEDIUM("Medium (600–839dp)"),
    EXPANDED("Expanded (≥ 840dp)")
}

/**
 * Reads the current window's width size class from [currentWindowAdaptiveInfo].
 *
 * The breakpoints (600dp / 840dp) are Material's standard ones, exposed as
 * constants on [WindowSizeClass]. This is the lowest-level adaptive primitive —
 * the pane scaffolds are built on top of exactly this signal.
 */
@Composable
fun rememberAdaptiveWidthClass(): AdaptiveWidthClass {
    val sizeClass = currentWindowAdaptiveInfo().windowSizeClass
    return remember(sizeClass) {
        when {
            sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) ->
                AdaptiveWidthClass.EXPANDED
            sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) ->
                AdaptiveWidthClass.MEDIUM
            else -> AdaptiveWidthClass.COMPACT
        }
    }
}

/**
 * A **detail screen that reflows itself** based on width — the manual approach for
 * when no pane scaffold fits, e.g. a single product page.
 *
 *  - **Compact**: a single scrolling column — hero on top, then info, then specs.
 *  - **Medium / Expanded**: the hero + summary move into a left column and the
 *    specs sit in a right column, so wide screens are not just a stretched phone.
 *
 * The content is identical; only the *arrangement* changes. That is the mental
 * model for all adaptive work: same data, different composition per size class.
 */
@Composable
fun WindowSizeClassDemo(modifier: Modifier = Modifier) {
    val widthClass = rememberAdaptiveWidthClass()
    val product = sampleProduct

    Surface(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CurrentSizeClassBanner(widthClass)

            if (widthClass == AdaptiveWidthClass.COMPACT) {
                // Single column: stack everything.
                ProductHero(product)
                ProductSummary(product)
                ProductSpecs(product)
            } else {
                // Two columns: hero + summary on the left, specs on the right.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ProductHero(product)
                        ProductSummary(product)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        ProductSpecs(product)
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrentSizeClassBanner(widthClass: AdaptiveWidthClass) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "currentWindowAdaptiveInfo().windowSizeClass",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AdaptiveWidthClass.entries.forEach { entry ->
                    AssistChip(
                        onClick = {},
                        enabled = entry == widthClass,
                        label = { Text(entry.name) }
                    )
                }
            }
            Text(
                "Current: ${widthClass.label}. Rotate the device, unfold, or resize the " +
                    "window in multi-window / desktop mode to cross a breakpoint.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun ProductHero(product: Product) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(product.accent, product.accent.copy(alpha = 0.6f))
                )
            ),
        contentAlignment = Alignment.BottomStart
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                product.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(product.tagline, style = MaterialTheme.typography.titleMedium, color = Color.White)
        }
    }
}

@Composable
private fun ProductSummary(product: Product) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            product.price,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            product.description,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.widthIn(max = 640.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = {}, modifier = Modifier.weight(1f)) { Text("Buy now") }
            OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) { Text("Add to cart") }
        }
    }
}

@Composable
private fun ProductSpecs(product: Product) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Specifications", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            product.specs.forEachIndexed { index, spec ->
                if (index > 0) HorizontalDivider(Modifier.padding(vertical = 8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        spec.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        spec.value,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1.4f)
                    )
                }
            }
        }
    }
}
