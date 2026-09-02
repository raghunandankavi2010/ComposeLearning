package com.example.composelearning.temples.platform

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import com.example.composelearning.temples.data.GeoPoint

/**
 * Every hand-off out of the app lives here.
 *
 * The rule this file follows: **never hard-code a target app.** Directions go out as a
 * `geo:` URI through a chooser, so Google Maps, Organic Maps, OsmAnd, Waze, MapmyIndia or
 * anything else the user has installed can claim it. That is what "navigate using any maps
 * app on the phone" actually requires — an implicit intent, not a package name.
 *
 * Each function returns `false` when nothing on the device can handle the intent, so the
 * caller can show a message instead of the app dying on [ActivityNotFoundException].
 */
object TempleIntents {

    /**
     * Opens the location in a maps app, letting the user pick which one.
     *
     * `geo:<lat>,<lng>?q=<lat>,<lng>(<label>)` is the widely-supported form: the path pins
     * the map, and the duplicated `q=` gives the pin a name. A bare `geo:0,0?q=<address>`
     * fallback is used when we have no verified coordinates for the temple.
     */
    fun openMap(context: Context, point: GeoPoint?, label: String, fallbackQuery: String): Boolean {
        val uri = if (point != null) {
            Uri.parse("geo:${point.lat},${point.lng}?q=${point.lat},${point.lng}(${Uri.encode(label)})")
        } else {
            Uri.parse("geo:0,0?q=${Uri.encode(fallbackQuery)}")
        }
        return context.launchChooser(Intent(Intent.ACTION_VIEW, uri), "Open in maps")
    }

    /**
     * Asks for turn-by-turn directions.
     *
     * Tries the `google.navigation:` scheme first because it starts guidance immediately,
     * then falls back to a plain map pin — a device without Google Maps still gets somewhere
     * useful rather than a crash.
     */
    fun navigate(context: Context, point: GeoPoint?, label: String, fallbackQuery: String): Boolean {
        if (point != null) {
            val navigation = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("google.navigation:q=${point.lat},${point.lng}&mode=d")
            )
            if (context.launch(navigation)) return true
        }
        return openMap(context, point, label, fallbackQuery)
    }

    /**
     * Searches for something *around* a point — "flower shop", "vegetarian restaurant",
     * "parking". This is how the Nearby tab works without us shipping a paid Places key:
     * the user's own maps app already has live, current listings, so we send the query there.
     */
    fun searchNear(context: Context, point: GeoPoint?, query: String): Boolean {
        val uri = if (point != null) {
            Uri.parse("geo:${point.lat},${point.lng}?q=${Uri.encode(query)}")
        } else {
            Uri.parse("geo:0,0?q=${Uri.encode(query)}")
        }
        return context.launchChooser(Intent(Intent.ACTION_VIEW, uri), "Search nearby")
    }

    /** Opens the dialler pre-filled — never places the call itself. */
    fun dial(context: Context, phone: String): Boolean =
        context.launch(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(phone)}")))

    /** Official temple / trust / donation pages open in the user's browser. */
    fun openUrl(context: Context, url: String): Boolean =
        context.launch(Intent(Intent.ACTION_VIEW, Uri.parse(url)))

    fun share(context: Context, subject: String, text: String): Boolean {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        return context.launchChooser(intent, subject)
    }

    /**
     * Drops a festival into the user's calendar as an all-day event.
     *
     * `ACTION_INSERT` opens the calendar app's own editor, so we need no calendar
     * permission and the user confirms before anything is written.
     */
    fun addEventToCalendar(
        context: Context,
        title: String,
        description: String,
        location: String,
        startMillis: Long
    ): Boolean {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, title)
            putExtra(CalendarContract.Events.DESCRIPTION, description)
            putExtra(CalendarContract.Events.EVENT_LOCATION, location)
            putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
        }
        return context.launch(intent)
    }

    /** Deep-links to this app's own entry in Settings, for re-granting a denied permission. */
    fun openAppSettings(context: Context): Boolean {
        val intent = Intent(
            android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null)
        )
        return context.launch(intent)
    }

    /** Opens system location settings so the user can switch location on. */
    fun openLocationSettings(context: Context): Boolean =
        context.launch(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))

    private fun Context.launchChooser(intent: Intent, title: String): Boolean =
        launch(Intent.createChooser(intent, title))

    private fun Context.launch(intent: Intent): Boolean {
        // A non-Activity context (e.g. application context from a preview) cannot start an
        // activity in the caller's task, so give it one of its own.
        if (this !is Activity) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            startActivity(intent)
            true
        } catch (notFound: ActivityNotFoundException) {
            false
        }
    }
}
