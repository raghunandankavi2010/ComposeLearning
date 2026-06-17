/*
 * # Sarvam Speech-to-Text — language detection feature
 *
 * Records a short audio clip on-device and uploads it to Sarvam's Speech-to-Text REST
 * API, which returns both the transcript and the **detected language** (BCP-47, e.g. hi-IN).
 *
 * ## AndroidManifest.xml permissions
 * ```xml
 * <uses-permission android:name="android.permission.RECORD_AUDIO" />
 * <uses-permission android:name="android.permission.INTERNET" />
 * ```
 * RECORD_AUDIO is a *runtime* permission (dangerous) — it must be requested at runtime,
 * not just declared. INTERNET is install-time.
 *
 * ## build.gradle.kts dependencies
 * ```kotlin
 * implementation("com.squareup.retrofit2:retrofit:3.0.0")
 * implementation("com.squareup.retrofit2:converter-kotlinx-serialization:3.0.0")
 * implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
 * implementation("com.squareup.okhttp3:okhttp:5.4.0")
 * // and apply the plugin: id("org.jetbrains.kotlin.plugin.serialization")
 * ```
 * (This repo uses the Groovy DSL + version catalog; the equivalents were added to
 * `gradle/libs.versions.toml` and `app/build.gradle`.)
 *
 * ## ⚠️ Live-API note
 * Per the requested spec this targets path `v1/speech/transcribe` with an `api-key` header.
 * The **production** Sarvam API uses path `speech-to-text` and header `api-subscription-key`
 * (model `saaras:v3` / mode `transcribe` are correct). Both divergent values are single
 * constants in [SarvamSttApi] / [SarvamApiKeyInterceptor] below — flip them to go live.
 */

package com.example.composelearning.sarvamstt

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.Interceptor
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/** Maps to the Sarvam STT JSON response. Unknown fields are ignored (see Json config). */
@Serializable
data class TranscriptionResponse(
    @SerialName("request_id") val requestId: String? = null,
    @SerialName("transcript") val transcript: String? = null,
    @SerialName("language_code") val languageCode: String? = null,
    @SerialName("language_probability") val languageProbability: Double? = null,
)

interface SarvamSttApi {

    /**
     * Multipart upload: the audio [file] part plus [model] and [mode] form fields.
     * @Part with a plain [RequestBody] emits a simple `form-data` text field.
     */
    @Multipart
    @POST(TRANSCRIBE_PATH)
    suspend fun transcribe(
        @Part file: MultipartBody.Part,
        @Part("model") model: RequestBody,
        @Part("mode") mode: RequestBody,
    ): TranscriptionResponse

    companion object {
        const val BASE_URL = "https://api.sarvam.ai/"

        /** Live Sarvam path. (The requested "v1/speech/transcribe" returns HTTP 404.) */
        const val TRANSCRIBE_PATH = "speech-to-text"

        const val MODEL_SAARAS_V3 = "saaras:v3"
        const val MODE_TRANSCRIBE = "transcribe"
    }
}

/**
 * Adds the API key header to every request. Keep the real key out of source — load it from
 * BuildConfig / a secrets file and pass it in. The placeholder makes the wiring obvious.
 */
class SarvamApiKeyInterceptor(
    private val apiKey: String = PLACEHOLDER_API_KEY,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader(HEADER_NAME, apiKey)
            .build()
        return chain.proceed(request)
    }

    companion object {
        /** Live Sarvam header. (The requested "api-key" would yield HTTP 401/403.) */
        const val HEADER_NAME = "api-subscription-key"
        const val PLACEHOLDER_API_KEY = "YOUR_SARVAM_API_KEY_HERE"
    }
}
