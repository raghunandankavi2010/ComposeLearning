package com.example.composelearning.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColorScheme(
    primary = Purple200,
    primaryContainer = Purple700,
    secondary = Teal200
)

private val LightColorPalette = lightColorScheme(
    primary = Purple500,
    primaryContainer = Purple700,
    secondary = Teal200,
    background = GREY094

    /* Other default colors to override
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
     */
)

@Composable
fun ComposeLearningTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

val Black900 = Color(0xFF121212) // Example dark grey
val Black700 = Color(0xFF282828) // Example slightly lighter dark grey
val White800 = Color(0xFFCCCCCC) // Example off-white

private val GoogleCalendarLightColors = lightColorScheme(
    primary = GCalBlue,
    onPrimary = Color.White,
    primaryContainer = GCalBlueDark,
    onPrimaryContainer = Color.White,
    background = GCalBackgroundLight,
    onBackground = GCalOnSurfaceLight,
    surface = GCalSurfaceLight,
    onSurface = GCalOnSurfaceLight,
    surfaceVariant = GCalBackgroundLight,
    onSurfaceVariant = GCalOnSurfaceVariantLight,
    outline = GCalOutlineLight
)

private val GoogleCalendarDarkColors = darkColorScheme(
    primary = GCalBlueLight,
    onPrimary = GCalOnSurfaceLight,
    primaryContainer = GCalBlueDark,
    onPrimaryContainer = Color.White,
    background = GCalBackgroundDark,
    onBackground = GCalOnSurfaceDark,
    surface = GCalSurfaceDark,
    onSurface = GCalOnSurfaceDark,
    surfaceVariant = GCalSurfaceDark,
    onSurfaceVariant = GCalOnSurfaceVariantDark,
    outline = GCalOutlineDark
)

@Composable
fun GoogleCalendarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) GoogleCalendarDarkColors else GoogleCalendarLightColors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
