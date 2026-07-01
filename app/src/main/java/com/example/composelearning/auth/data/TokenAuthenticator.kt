package com.example.composelearning.auth.data

import com.example.composelearning.auth.data.remote.RefreshRequestDto
import com.example.composelearning.auth.data.remote.TokenRefreshApi
import com.example.composelearning.auth.data.remote.toDomain
import com.example.composelearning.auth.domain.AuthEventLog
import com.example.composelearning.auth.domain.TokenStorage
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

/**
 * The heart of the demo: OkHttp calls this **only when a request comes back 401**. It refreshes the
 * access token behind the scenes and hands OkHttp a new request to retry — so the app's suspend
 * `api.profile()` call never even sees the 401; it just eventually succeeds.
 *
 * Three things make it correct under real conditions:
 *  1. **No loops.** Refresh runs against [TokenRefreshApi] on a *bare* client (no interceptor, no
 *     authenticator). And if the retried request ALSO 401s, [responseCount] caps us so we give up.
 *  2. **No stampede.** Many requests can 401 at once (they all carried the same dead token). The
 *     `synchronized` block means the first thread refreshes; the rest wake up, notice the stored
 *     token already changed, and simply retry with it instead of refreshing again.
 *  3. **Clean give-up.** If the refresh token itself is dead, we clear the session and return
 *     `null` — OkHttp then surfaces the 401 to the caller, which the ViewModel treats as "re-login".
 */
class TokenAuthenticator(
    private val storage: TokenStorage,
    private val refreshApi: TokenRefreshApi,
    private val eventLog: AuthEventLog,
    private val now: () -> Long = System::currentTimeMillis,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val failedToken = response.request.header("Authorization")
            ?.removePrefix(BEARER_PREFIX)?.trim()

        synchronized(lock) {
            val latest = storage.current() ?: return null // logged out; nothing to refresh with

            // Another thread already refreshed while we waited on the lock — just use the new token.
            if (failedToken != null && latest.accessToken != failedToken) {
                eventLog.emit("↻ Token already refreshed by another request — retrying")
                return response.retryWith(latest.accessToken)
            }

            if (responseCount(response) >= MAX_ATTEMPTS) {
                eventLog.emit("❌ Still 401 after refresh — giving up")
                return null
            }

            eventLog.emit("🔒 401 Unauthorized — access token rejected; refreshing silently…")

            val refreshed = runCatching {
                refreshApi.refresh(RefreshRequestDto(latest.refreshToken)).execute()
            }.getOrNull()

            val body = refreshed?.takeIf { it.isSuccessful }?.body()
            if (body == null) {
                eventLog.emit("❌ Refresh failed (${refreshed?.code() ?: "no response"}) — session cleared, please log in")
                storage.clear()
                return null
            }

            val newTokens = body.toDomain(now())
            storage.save(newTokens)
            eventLog.emit("✅ Silent refresh succeeded — retrying the original request")
            return response.retryWith(newTokens.accessToken)
        }
    }

    private fun Response.retryWith(accessToken: String): Request =
        request.newBuilder()
            .header("Authorization", "$BEARER_PREFIX$accessToken")
            .build()

    /** How many times OkHttp has already tried this request (walks the prior-response chain). */
    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }

    private companion object {
        const val BEARER_PREFIX = "Bearer "
        const val MAX_ATTEMPTS = 2
        val lock = Any()
    }
}
