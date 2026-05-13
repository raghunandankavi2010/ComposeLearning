package com.example.composelearning.charts

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Shared visual language for every chart in this module.
 *
 * The defaults derive from MaterialTheme.colorScheme so charts pick up the host app's palette,
 * while every value can be overridden per call.
 */
@Immutable
data class ChartTheme(
    val gridColor: Color,
    val axisColor: Color,
    val axisLabelStyle: TextStyle,
    val titleStyle: TextStyle,
    val valueLabelStyle: TextStyle,
    val tooltipBackground: Color,
    val tooltipContent: Color,
    val palette: List<Color>,
    val surface: Color,
    val onSurface: Color,
    val contentPadding: Dp = 16.dp,
    val gridLineWidth: Dp = 1.dp,
    val axisLineWidth: Dp = 1.dp,
)

object ChartDefaults {

    @Composable
    @ReadOnlyComposable
    fun theme(
        palette: List<Color> = defaultPalette(),
    ): ChartTheme {
        val cs = MaterialTheme.colorScheme
        val type = MaterialTheme.typography
        return ChartTheme(
            gridColor = cs.outlineVariant.copy(alpha = 0.6f),
            axisColor = cs.outline,
            axisLabelStyle = type.labelSmall.copy(
                color = cs.onSurfaceVariant,
                fontSize = 11.sp,
            ),
            titleStyle = type.titleMedium.copy(
                color = cs.onSurface,
                fontWeight = FontWeight.SemiBold,
            ),
            valueLabelStyle = type.labelMedium.copy(
                color = cs.onSurface,
                fontWeight = FontWeight.Medium,
            ),
            tooltipBackground = cs.inverseSurface,
            tooltipContent = cs.inverseOnSurface,
            palette = palette,
            surface = cs.surface,
            onSurface = cs.onSurface,
        )
    }

    @Composable
    @ReadOnlyComposable
    fun defaultPalette(): List<Color> {
        val cs = MaterialTheme.colorScheme
        return listOf(
            cs.primary,
            cs.tertiary,
            cs.secondary,
            cs.error,
            cs.primaryContainer,
            cs.tertiaryContainer,
            cs.secondaryContainer,
            cs.errorContainer,
        )
    }
}