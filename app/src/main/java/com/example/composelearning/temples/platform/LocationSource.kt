package com.example.composelearning.temples.platform

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.CancellationSignal
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.core.content.ContextCompat
import com.example.composelearning.R
import com.example.composelearning.temples.data.GeoPoint
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/** Everything the UI needs to know about where the user is, including why we don't know. */
@Immutable
sealed interface LocationState {
    /** Not asked yet — the discover screen shows a "find temples near me" prompt. */
    data object Idle : LocationState

    /** The user has not granted a location permission (or revoked it). */
    data object PermissionRequired : LocationState

    /** A fix is being acquired. */
    data object Locating : LocationState

    data class Ready(
        val point: GeoPoint,
        val accuracyMeters: Float?,
        /** True when this is a cached last-known fix rather than a fresh one. */
        val stale: Boolean
    ) : LocationState

    data class Unavailable(@StringRes val reasonRes: Int) : LocationState
}

/**
 * Current location via the platform [LocationManager].
 *
 * Deliberately *not* play-services-location: this feature only needs a coarse city-scale
 * fix to sort temples by distance, and the framework's fused provider (API 31+) delivers
 * that without adding a Google Play dependency to the project. Turn-by-turn navigation is
 * handed off to whichever maps app the user has installed — see [TempleIntents].
 */
class DeviceLocationSource(private val context: Context) {

    private val manager: LocationManager? =
        context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    fun hasPermission(): Boolean =
        PERMISSIONS.any {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

    /**
     * Best provider available on this device, most accurate first. `FUSED_PROVIDER` blends
     * GPS/wifi/cell and is the one to prefer; the others are the fallback on stripped ROMs.
     */
    private fun preferredProvider(): String? {
        val available = manager?.allProviders ?: return null
        return listOf(
            LocationManager.FUSED_PROVIDER,
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER
        ).firstOrNull { it in available }
    }

    /**
     * A cached fix, returned instantly so the list can sort by distance without a wait.
     * Callers should still request [current] to refresh it.
     */
    fun lastKnown(): LocationState {
        if (!hasPermission()) return LocationState.PermissionRequired
        val lm = manager ?: return LocationState.Unavailable(R.string.location_error_unavailable)
        val best = PROVIDER_ORDER
            .filter { it in lm.allProviders }
            .mapNotNull { runCatching { lm.getLastKnownLocation(it) }.getOrNull() }
            .maxByOrNull { it.time }
            ?: return LocationState.Idle
        return LocationState.Ready(best.toGeoPoint(), best.accuracyOrNull(), stale = true)
    }

    /**
     * Requests one fresh fix.
     *
     * Uses the single-shot `getCurrentLocation` rather than registering a listener: we want
     * a location *now*, not a stream, and the platform tears the request down for us — no
     * chance of leaking an update subscription past the caller's scope.
     */
    suspend fun current(): LocationState {
        if (!hasPermission()) return LocationState.PermissionRequired
        val lm = manager ?: return LocationState.Unavailable(R.string.location_error_unavailable)
        if (!lm.isLocationEnabled) return LocationState.Unavailable(R.string.location_error_disabled)
        val provider = preferredProvider()
            ?: return LocationState.Unavailable(R.string.location_error_unavailable)

        val signal = CancellationSignal()
        val location: Location? = try {
            suspendCancellableCoroutine { continuation ->
                continuation.invokeOnCancellation { signal.cancel() }
                lm.getCurrentLocation(provider, signal, context.mainExecutor) { result ->
                    if (continuation.isActive) continuation.resume(result)
                }
            }
        } catch (security: SecurityException) {
            // Permission revoked between the check above and the call.
            return LocationState.PermissionRequired
        }

        return location?.let {
            LocationState.Ready(it.toGeoPoint(), it.accuracyOrNull(), stale = false)
        } ?: lastKnown().takeIf { it is LocationState.Ready }
            ?: LocationState.Unavailable(R.string.location_error_no_fix)
    }

    private fun Location.toGeoPoint() = GeoPoint(latitude, longitude)

    private fun Location.accuracyOrNull(): Float? = if (hasAccuracy()) accuracy else null

    companion object {
        val PERMISSIONS = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        private val PROVIDER_ORDER = listOf(
            LocationManager.FUSED_PROVIDER,
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        )
    }
}
