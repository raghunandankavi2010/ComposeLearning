package com.example.composelearning.sarvamstt

import com.example.composelearning.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Builds the [SarvamSttApi] over a shared OkHttpClient (timeouts + auth interceptor) with the
 * kotlinx-serialization converter. The client is a singleton — it pools connections/threads;
 * never per-request.
 */
object SarvamSttClient {

    // ignoreUnknownKeys: the API may add fields (timestamps, diarization) we don't model.
    private val json = Json { ignoreUnknownKeys = true }

    fun create(apiKey: String = BuildConfig.SARVAM_API_KEY): SarvamSttApi {
        val okHttp = OkHttpClient.Builder()
            .addInterceptor(SarvamApiKeyInterceptor(apiKey))
            .connectTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)   // audio upload can be the slow part
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(SarvamSttApi.BASE_URL)
            .client(okHttp)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(SarvamSttApi::class.java)
    }
}
