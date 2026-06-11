package com.example.composelearning.ui.theme

import android.graphics.Typeface
import androidx.compose.ui.text.font.FontFamily

/**
 * App typography backed by Roboto, Android's system font family. The named platform typefaces
 * (`sans-serif`, `sans-serif-medium`, `sans-serif-black`) are Roboto weights, so this gives a
 * consistent Roboto look with no bundled font binaries and no network/provider setup.
 */
val AppFontFamily = FontFamily(Typeface.SANS_SERIF)

val AppFontFamilyMedium = FontFamily(Typeface.create("sans-serif-medium", Typeface.NORMAL))
val AppFontFamilyBold = FontFamily(Typeface.create("sans-serif", Typeface.BOLD))
val AppFontFamilyBlack = FontFamily(Typeface.create("sans-serif-black", Typeface.NORMAL))
