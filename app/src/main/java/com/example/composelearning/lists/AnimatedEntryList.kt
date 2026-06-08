package com.example.composelearning.lists

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * A [LazyColumn] where each item animates onto its position the first time it
 * appears on screen (fade + slide up). The very first on-screen batch is
 * staggered so the rows cascade in one after another instead of all at once.
 *
 * The animation is driven per-item and remembered against the item's [key], so:
 *  - rows already shown are not re-animated on recomposition / scroll, and
 *  - new rows scrolled into view still get their own entrance animation.
 */
@Composable
fun AnimatedEntryList(
    items: List<String>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(
            items = items,
            key = { _, item -> item }, // stable identity so animation state survives recomposition
        ) { index, item ->
            AnimatedEntry(index = index) {
                ListRow(text = item, modifier = Modifier.animateItem())
            }
        }
    }
}

/**
 * Wraps [content] in an entrance animation that runs once, the first time this
 * item is composed. [index] adds a small staggered delay (capped) so the initial
 * batch cascades in rather than appearing simultaneously.
 */
@Composable
private fun AnimatedEntry(
    index: Int,
    content: @Composable () -> Unit,
) {
    // rememberSaveable keeps a row from re-animating after it has already appeared:
    // once `visible` is true it stays true, and AnimatedVisibility won't replay the
    // enter animation when it re-enters composition already visible (e.g. on rotation).
    var visible by rememberSaveable { mutableStateOf(false) }
    val staggerDelay = remember(index) { (index * 60L).coerceAtMost(300L) }

    LaunchedEffect(Unit) {
        if (!visible) {
            delay(staggerDelay)
            visible = true
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 400)) +
            slideInVertically(
                animationSpec = tween(durationMillis = 400),
                initialOffsetY = { it / 2 },
            ),
    ) {
        content()
    }
}

@Composable
private fun ListRow(
    text: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AnimatedEntryListPreview() {
    AnimatedEntryList(
        items = List(20) { "Item #${it + 1}" },
    )
}
