package com.example.composelearning.styles

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * ---------------------------------------------------------------------------
 * LAYER 1 — THE STYLES API & CUSTOM THEME
 * ---------------------------------------------------------------------------
 *
 * A tiny, self-contained "Styles API" for the design system. The idea:
 *
 *  - A [ButtonStyle] is an *immutable* value object describing how a button
 *    should look (never *how it behaves*).
 *  - [AppStyles] bundles every component style so the whole design system
 *    travels down the tree as a single CompositionLocal value.
 *  - [CustomTheme] provides those styles, and [AppTheme.styles] is the
 *    ergonomic read-side accessor (mirrors how `MaterialTheme.colorScheme`
 *    reads from a CompositionLocal).
 *
 * Everything here is annotated [Immutable] so Compose can treat the values as
 * stable, keeping recomposition scopes tight and composables skippable.
 */

/**
 * Immutable description of a button's visual appearance.
 *
 * All types used here are themselves stable ([Color], [Shape],
 * [BorderStroke], [TextStyle], [PaddingValues]), so the whole class is a
 * legitimate `@Immutable` — a change to any field produces a *new* instance
 * and Compose can skip recomposition whenever the reference is unchanged.
 */
@Immutable
data class ButtonStyle(
    val backgroundColor: Color,
    val contentColor: Color,
    val disabledBackgroundColor: Color,
    val disabledContentColor: Color,
    val shape: Shape,
    val border: BorderStroke?,
    val textStyle: TextStyle,
    val contentPadding: PaddingValues,
)

/**
 * The full set of component styles for the design system. Add new component
 * styles here (e.g. `card: CardStyle`) as the system grows — every consumer
 * keeps working because they read through [AppTheme].
 */
@Immutable
data class AppStyles(
    val button: ButtonStyle,
)

/* ----------------------------- Design tokens ----------------------------- */

/**
 * Brand palette for the design system. Kept private to this file so styles are
 * the single public surface — consumers theme via [ButtonStyle], not raw colors.
 */
private val BrandPrimary = Color(0xFF3D5AFE)
private val BrandSecondary = Color(0xFFE8EAF6)
private val BrandDanger = Color(0xFFD32F2F)
private val BrandOnPrimary = Color(0xFFFFFFFF)
private val BrandOnSecondary = Color(0xFF1A237E)
private val BrandDisabled = Color(0xFFBDBDBD)
private val BrandOnDisabled = Color(0xFF757575)

private val ButtonTextStyle = TextStyle(
    fontWeight = FontWeight.SemiBold,
    fontSize = 15.sp,
)

private val ButtonShape = RoundedCornerShape(14.dp)
private val ButtonPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)

/**
 * Standard, globally-available button variants. These are plain values (not
 * `@Composable`) so they can be referenced anywhere — inside previews, tests,
 * or non-composable code — and remain trivially stable.
 */
object AppButtonStyles {

    val Primary = ButtonStyle(
        backgroundColor = BrandPrimary,
        contentColor = BrandOnPrimary,
        disabledBackgroundColor = BrandDisabled,
        disabledContentColor = BrandOnDisabled,
        shape = ButtonShape,
        border = null,
        textStyle = ButtonTextStyle,
        contentPadding = ButtonPadding,
    )

    val Secondary = ButtonStyle(
        backgroundColor = BrandSecondary,
        contentColor = BrandOnSecondary,
        disabledBackgroundColor = BrandDisabled.copy(alpha = 0.4f),
        disabledContentColor = BrandOnDisabled,
        shape = ButtonShape,
        border = BorderStroke(1.dp, BrandOnSecondary.copy(alpha = 0.25f)),
        textStyle = ButtonTextStyle,
        contentPadding = ButtonPadding,
    )

    val Danger = ButtonStyle(
        backgroundColor = BrandDanger,
        contentColor = BrandOnPrimary,
        disabledBackgroundColor = BrandDisabled,
        disabledContentColor = BrandOnDisabled,
        shape = ButtonShape,
        border = null,
        textStyle = ButtonTextStyle,
        contentPadding = ButtonPadding,
    )
}

/** The default styles used when no [CustomTheme] is present above a consumer. */
fun defaultAppStyles(): AppStyles = AppStyles(
    button = AppButtonStyles.Primary,
)

/* --------------------------- CompositionLocal ---------------------------- */

/**
 * `staticCompositionLocalOf` (not `compositionLocalOf`) is the right choice
 * here: the styles rarely change, so we don't want Compose tracking reads of
 * this local. When the value *does* change, the entire content lambda under
 * the provider recomposes — which is exactly what a theme swap should do.
 */
val LocalAppStyles = staticCompositionLocalOf { defaultAppStyles() }

/**
 * Ergonomic, read-only accessor object — the design-system equivalent of
 * `MaterialTheme`. Usage: `AppTheme.styles.button`.
 */
object AppTheme {
    val styles: AppStyles
        @Composable
        @ReadOnlyComposable
        get() = LocalAppStyles.current
}

/**
 * Theme wrapper that pushes [styles] down the tree via [CompositionLocalProvider].
 *
 * Defaults to inheriting whatever styles are already in scope (or the static
 * default at the root), so nesting is safe and callers can override just a
 * subset by passing a `.copy(...)`-ed [AppStyles].
 */
@Composable
fun CustomTheme(
    styles: AppStyles = LocalAppStyles.current,
    content: @Composable () -> Unit,
) {
    // Also inherits MaterialTheme so material components (ripples, text
    // defaults, etc.) still work inside our custom-themed subtree.
    MaterialTheme {
        CompositionLocalProvider(
            LocalAppStyles provides styles,
            content = content,
        )
    }
}
