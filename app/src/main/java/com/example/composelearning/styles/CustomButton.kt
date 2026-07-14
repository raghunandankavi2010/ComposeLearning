package com.example.composelearning.styles

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role

/**
 * ---------------------------------------------------------------------------
 * LAYER 2 — THE COMPONENT & VARIANT IMPLEMENTATION
 * ---------------------------------------------------------------------------
 *
 * [CustomButton] is the design-system button. By default it consumes
 * `AppTheme.styles.button` (whatever the nearest [CustomTheme] provides), but
 * a caller can hand it an explicit [style] to opt into a specific variant such
 * as [AppButtonStyles.Danger].
 *
 * Design (`style`) is fully decoupled from behavior (`onClick`, `enabled`),
 * which is what makes the variant system composable and testable.
 */
@Composable
fun CustomButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    // Default read happens here — the button auto-adopts the themed style.
    style: ButtonStyle = AppTheme.styles.button,
) {
    val backgroundColor = if (enabled) style.backgroundColor else style.disabledBackgroundColor
    val contentColor = if (enabled) style.contentColor else style.disabledContentColor

    // A single MutableInteractionSource, remembered so ripple state survives
    // recomposition rather than being re-created each pass.
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .clip(style.shape)
            .background(backgroundColor, style.shape)
            .then(
                if (style.border != null) {
                    Modifier.border(style.border, style.shape)
                } else {
                    Modifier
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = contentColor),
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(style.contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        // Provide the content color so any richer slot content (icons, etc.)
        // inherits it — mirrors how Material components propagate content color.
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            Text(
                text = text,
                style = style.textStyle,
                color = contentColor,
            )
        }
    }
}
