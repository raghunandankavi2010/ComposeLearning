package com.example.composelearning.auth.data.remote

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * The everyday API used by the repository. [profile] carries no token argument — the
 * [com.example.composelearning.auth.data.AccessTokenInterceptor] injects the `Authorization`
 * header, and the [com.example.composelearning.auth.data.TokenAuthenticator] refreshes it on 401.
 */
interface AuthApi {
    @POST("login")
    suspend fun login(@Body request: LoginRequestDto): TokenResponseDto

    @GET("profile")
    suspend fun profile(): ProfileResponseDto

    @POST("refresh")
    suspend fun refresh(@Body request: RefreshRequestDto): TokenResponseDto

    // Response<Unit> so Retrofit skips body conversion on the server's 204 No Content.
    @POST("logout")
    suspend fun logout(@Body request: LogoutRequestDto): Response<Unit>

    // Demo-only: asks the server to expire the *current* access token (bearer attached by the
    // interceptor) so the auto-refresh path can be triggered without waiting out the TTL.
    @POST("debug/expire")
    suspend fun forceExpire(): Response<Unit>
}

/**
 * A **separate**, deliberately minimal refresh endpoint used only inside the OkHttp
 * [com.example.composelearning.auth.data.TokenAuthenticator]. It returns a blocking [Call] because
 * an `Authenticator` runs synchronously on an OkHttp thread (no coroutine scope available), and it
 * lives on a *bare* client with no interceptor/authenticator so a failing refresh can never loop.
 */
interface TokenRefreshApi {
    @POST("refresh")
    fun refresh(@Body request: RefreshRequestDto): Call<TokenResponseDto>
}
