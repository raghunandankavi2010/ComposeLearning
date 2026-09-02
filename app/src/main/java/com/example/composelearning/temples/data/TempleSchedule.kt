package com.example.composelearning.temples.data

import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * "Is it open right now, and if not, when?" — computed from the structured
 * [OpeningWindow]s rather than stored as prose, so it stays correct at 4am on a Tuesday.
 */
sealed interface OpenStatus {
    /** Open now. [closesIn] lets the UI warn "closes in 20 min" before someone sets out. */
    data class Open(val closesAt: LocalTime, val closesIn: Duration) : OpenStatus

    /** Shut now. [opensAt]/[opensIn] describe the next window, which may be on [opensOn]. */
    data class Closed(
        val opensAt: LocalTime,
        val opensOn: DayOfWeek,
        val opensIn: Duration,
        /** True when the next window is later the same calendar day (the midday break). */
        val sameDay: Boolean
    ) : OpenStatus

    /** We have no verified timings for this temple; better to say so than to guess. */
    data object Unknown : OpenStatus
}

/** Minutes below which the UI nudges the user that the window is about to end. */
const val CLOSING_SOON_MINUTES = 45L

/**
 * A window whose close time is not after its open time is read as spanning midnight
 * (e.g. Shivaratri all-night darshan, `21:00 → 05:00`).
 */
private fun OpeningWindow.endsNextDay(): Boolean = !close.isAfter(open)

private fun OpeningWindow.contains(time: LocalTime): Boolean =
    if (endsNextDay()) !time.isBefore(open) || time.isBefore(close)
    else !time.isBefore(open) && time.isBefore(close)

/**
 * Resolves the temple's open state at [now].
 *
 * Scans today first, then walks forward up to seven days to find the next opening, so a
 * temple that only opens on Fridays still reports a useful "opens Friday 6:00 AM".
 */
fun Temple.openStatusAt(now: LocalDateTime): OpenStatus {
    if (openings.isEmpty()) return OpenStatus.Unknown

    val today = now.dayOfWeek
    val timeNow = now.toLocalTime()

    // Currently inside a window? Also covers a window opened yesterday that runs past midnight.
    openings.firstOrNull { it.days.contains(today) && it.contains(timeNow) }?.let { active ->
        val closesToday = if (active.endsNextDay() && !timeNow.isBefore(active.open)) {
            // Opened today, closes after midnight.
            Duration.between(now, now.toLocalDate().plusDays(1).atTime(active.close))
        } else {
            Duration.between(timeNow, active.close)
        }
        return OpenStatus.Open(active.close, closesToday)
    }
    openings.firstOrNull { it.endsNextDay() && it.days.contains(today - 1) && timeNow.isBefore(it.close) }
        ?.let { overnight ->
            return OpenStatus.Open(overnight.close, Duration.between(timeNow, overnight.close))
        }

    // Closed. Find the earliest window that starts from here on.
    for (dayOffset in 0..7) {
        val date = now.toLocalDate().plusDays(dayOffset.toLong())
        val candidates = openings
            .filter { it.days.contains(date.dayOfWeek) }
            .map { it.open }
            .filter { dayOffset > 0 || it.isAfter(timeNow) }
        val next = candidates.minOrNull() ?: continue
        return OpenStatus.Closed(
            opensAt = next,
            opensOn = date.dayOfWeek,
            opensIn = Duration.between(now, date.atTime(next)),
            sameDay = dayOffset == 0
        )
    }
    return OpenStatus.Unknown
}

/** The next arati/abhisheka from [now], for the "next ritual" countdown on the detail screen. */
fun Temple.nextRitualAfter(now: LocalTime): Ritual? =
    rituals.filter { it.time != null && it.time.isAfter(now) }.minByOrNull { it.time!! }
        ?: rituals.filter { it.time != null }.minByOrNull { it.time!! }

/** Today's opening windows, for the "Timings" card. Empty means closed all day. */
fun Temple.windowsOn(day: DayOfWeek): List<OpeningWindow> =
    openings.filter { it.days.contains(day) }.sortedBy { it.open }
