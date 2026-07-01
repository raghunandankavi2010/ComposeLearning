package com.example.composelearning.auth.di

import com.example.composelearning.auth.data.AccessTokenInterceptor
import com.example.composelearning.auth.data.AuthRepositoryImpl
import com.example.composelearning.auth.data.InMemoryTokenStorage
import com.example.composelearning.auth.data.TokenAuthenticator
import com.example.composelearning.auth.data.remote.AuthApi
import com.example.composelearning.auth.data.remote.TokenRefreshApi
import com.example.composelearning.auth.domain.AuthEventLog
import com.example.composelearning.auth.domain.AuthRepository
import com.example.composelearning.auth.domain.TokenStorage
import com.example.composelearning.auth.domain.usecase.ForceExpireAccessTokenUseCase
import com.example.composelearning.auth.domain.usecase.GetProfileUseCase
import com.example.composelearning.auth.domain.usecase.LoginUseCase
import com.example.composelearning.auth.domain.usecase.LogoutUseCase
import com.example.composelearning.auth.domain.usecase.RefreshTokensUseCase
import com.example.composelearning.BuildConfig
import com.example.composelearning.auth.presentation.AuthViewModel
import java.util.concurrent.TimeUnit
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * Koin graph for the auth demo. Two Retrofit instances by design:
 *
 *  • [REFRESH_RETROFIT] — a *bare* client used ONLY inside the [TokenAuthenticator] to rotate
 *    tokens. It has no interceptor and no authenticator, so a failed refresh can never recurse.
 *  • the default one — carries the [AccessTokenInterceptor] (attaches the bearer) and the
 *    [TokenAuthenticator] (refreshes on 401). Everything the repository calls goes through here.
 *
 * [BASE_URL] points at the **host machine's LAN IP**, reached over the emulator's Wi-Fi (wlan0).
 * We deliberately avoid `10.0.2.2` here: this Wi-Fi AVD exposes two `10.0.2.0/24` interfaces
 * (eth0 + wlan0) and only one forwards `10.0.2.2` to the host, so sockets that bind the other
 * interface time out. The LAN IP has none of that ambiguity.
 *
 * Find yours with `ipconfig getifaddr en0` (macOS) / `hostname -I` (Linux) and update both this
 * constant and the matching <domain> in res/xml/network_security_config.xml. The server already
 * binds 0.0.0.0, so it accepts LAN connections out of the box.
 */
private const val BASE_URL = "http://192.168.68.103:8090/"
private const val REFRESH_RETROFIT = "auth_refresh_retrofit"
private const val AUTHED_RETROFIT = "auth_authed_retrofit"

val authModule = module {

    // ---- shared singletons ----
    single { AuthEventLog() }
    single<TokenStorage> { InMemoryTokenStorage() }
    single {
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
    }

    val jsonMediaType = "application/json".toMediaType()

    // Debug-only wire logging — prints request/response lines + bodies to Logcat (tag: "okhttp.
    // OkHttpClient") so network failures are diagnosable. NONE in release so tokens never leak.
    single {
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    // Per-phase timeouts (not a single callTimeout): a slow emulator first-hit can spend a few
    // seconds establishing the socket without tripping the read budget for the actual exchange.
    fun OkHttpClient.Builder.withTimeouts(): OkHttpClient.Builder = this
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)

    // ---- refresh transport (bare, loop-proof) ----
    single(named(REFRESH_RETROFIT)) {
        val client = OkHttpClient.Builder()
            .withTimeouts()
            .addInterceptor(get<HttpLoggingInterceptor>())
            .build()
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(get<Json>().asConverterFactory(jsonMediaType))
            .build()
    }
    single<TokenRefreshApi> { get<Retrofit>(named(REFRESH_RETROFIT)).create(TokenRefreshApi::class.java) }

    // ---- the 401 authenticator (needs the bare refresh api above) ----
    single { AccessTokenInterceptor(storage = get()) }
    single { TokenAuthenticator(storage = get(), refreshApi = get(), eventLog = get()) }

    // ---- authenticated transport (attaches bearer + auto-refreshes) ----
    single(named(AUTHED_RETROFIT)) {
        val client = OkHttpClient.Builder()
            .addInterceptor(get<AccessTokenInterceptor>()) // adds bearer first…
            .addInterceptor(get<HttpLoggingInterceptor>()) // …so logging sees the final headers
            .authenticator(get<TokenAuthenticator>())
            .withTimeouts()
            .build()
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(get<Json>().asConverterFactory(jsonMediaType))
            .build()
    }
    single<AuthApi> { get<Retrofit>(named(AUTHED_RETROFIT)).create(AuthApi::class.java) }

    // ---- domain + presentation ----
    single<AuthRepository> { AuthRepositoryImpl(api = get(), storage = get(), eventLog = get()) }

    factory { LoginUseCase(get()) }
    factory { GetProfileUseCase(get()) }
    factory { RefreshTokensUseCase(get()) }
    factory { ForceExpireAccessTokenUseCase(get()) }
    factory { LogoutUseCase(get()) }

    viewModelOf(::AuthViewModel)
}
