package com.example.composelearning.auth.data

import com.example.composelearning.auth.domain.TokenStorage
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Attaches `Authorization: Bearer <accessToken>` to every outbound request that needs it.
 *
 * The auth endpoints ([NO_AUTH_PATHS]) are skipped — they either establish the session (login) or
 * exist precisely because the access token is dead (refresh), so bearing one would be pointless.
 *
 * This runs on OkHttp's thread, which is why it reads the token synchronously via
 * [TokenStorage.current] rather than collecting a flow.
 */
class AccessTokenInterceptor(
    private val storage: TokenStorage,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath.trimEnd('/').substringAfterLast('/')

        if (path in NO_AUTH_PATHS) return chain.proceed(request)

        val accessToken = storage.current()?.accessToken
            ?: return chain.proceed(request) // logged out — let the server return 401

        val authorized = request.newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .build()
        return chain.proceed(authorized)
    }

    private companion object {
        val NO_AUTH_PATHS = setOf("login", "refresh")
    }
}
