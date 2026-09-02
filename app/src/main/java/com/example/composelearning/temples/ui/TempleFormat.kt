package com.example.composelearning.temples.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.example.composelearning.R
import com.example.composelearning.temples.data.OpenStatus
import com.example.composelearning.temples.data.OpeningWindow
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/**
 * Locale-aware formatting for the temple screens.
 *
 * Times go through [DateTimeFormatter.ofLocalizedTime] rather than a hard-coded
 * `"h:mm a"` pattern, so a Kannada or Hindi locale gets its own clock conventions
 * instead of English AM/PM glued onto Indic digits.
 */

@Composable
fun rememberTimeFormatter(): DateTimeFormatter {
    val locale: Locale = LocalConfiguration.current.locales[0]
    return remember(locale) {
        DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale)
    }
}

@Composable
fun LocalTime.formatted(): String = format(rememberTimeFormatter())

/** Short weekday name from our own resources, so it matches the rest of the UI exactly. */
val DayOfWeek.labelResId: Int
    get() = when (this) {
        DayOfWeek.MONDAY -> R.string.day_monday
        DayOfWeek.TUESDAY -> R.string.day_tuesday
        DayOfWeek.WEDNESDAY -> R.string.day_wednesday
        DayOfWeek.THURSDAY -> R.string.day_thursday
        DayOfWeek.FRIDAY -> R.string.day_friday
        DayOfWeek.SATURDAY -> R.string.day_saturday
        DayOfWeek.SUNDAY -> R.string.day_sunday
    }

@Composable
fun DayOfWeek.shortLabel(): String = stringResource(labelResId)

/** "6:00 AM – 12:30 PM" for one opening window. */
@Composable
fun OpeningWindow.rangeLabel(): String {
    val formatter = rememberTimeFormatter()
    return "${open.format(formatter)} – ${close.format(formatter)}"
}

/**
 * The one-line status a user reads before deciding to set out.
 *
 * Deliberately front-loads the actionable part: "Closes in 20 min" beats "Open now" when
 * the temple is about to shut, because only one of those changes the decision.
 */
@Composable
fun OpenStatus.summaryLabel(): String {
    val formatter = rememberTimeFormatter()
    return when (this) {
        is OpenStatus.Open -> {
            val minutes = closesIn.toMinutes()
            if (minutes <= com.example.composelearning.temples.data.CLOSING_SOON_MINUTES) {
                stringResource(R.string.status_closing_soon, minutes.toInt())
            } else {
                stringResource(R.string.status_closes_at, closesAt.format(formatter))
            }
        }
        is OpenStatus.Closed ->
            if (sameDay) {
                stringResource(R.string.status_opens_at, opensAt.format(formatter))
            } else {
                stringResource(
                    R.string.status_opens_on,
                    opensOn.shortLabel(),
                    opensAt.format(formatter)
                )
            }
        OpenStatus.Unknown -> stringResource(R.string.status_timings_unknown)
    }
}

/** The short "Open now" / "Closed now" headline that pairs with [summaryLabel]. */
@Composable
fun OpenStatus.headlineLabel(): String = stringResource(
    when (this) {
        is OpenStatus.Open -> R.string.status_open
        is OpenStatus.Closed -> R.string.status_closed
        OpenStatus.Unknown -> R.string.status_timings_unknown
    }
)
