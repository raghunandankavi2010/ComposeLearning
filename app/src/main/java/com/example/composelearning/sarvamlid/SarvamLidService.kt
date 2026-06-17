package com.example.composelearning.sarvamlid

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Serializable
private data class LidRequest(val input: String)

@Serializable
data class LidResponse(
    @SerialName("request_id") val requestId: String? = null,
    @SerialName("language_code") val languageCode: String? = null,
    @SerialName("script_code") val scriptCode: String? = null,
)

/** Thrown for any non-2xx HTTP response; [code] lets callers map 401/429/5xx to messages. */
class SarvamApiException(val code: Int, message: String) : IOException(message)

/**
 * Thin OkHttp client for Sarvam's Language Identification endpoint.
 *
 *   POST https://api.sarvam.ai/text-lid
 *   header  api-subscription-key: <key>
 *   body    { "input": "<text, max 1000 chars>" }
 *   200     { "request_id", "language_code", "script_code" }
 *
 * We use raw OkHttp + kotlinx.serialization (both already in the catalog) rather than pulling
 * in Retrofit for a single endpoint. The OkHttpClient is shared/singleton on purpose — it
 * pools connections and threads; never create one per request.
 */
class SarvamLidService(
    private val apiKey: String,
    private val client: OkHttpClient = defaultClient(),
    private val json: Json = Json { ignoreUnknownKeys = true },
) {
    suspend fun identify(text: String): LidResponse = withContext(Dispatchers.IO) {
        val payload = json.encodeToString(LidRequest.serializer(), LidRequest(text))
        val request = Request.Builder()
            .url(ENDPOINT)
            .addHeader("api-subscription-key", apiKey)
            .post(payload.toRequestBody(JSON_MEDIA_TYPE))
            .build()

        client.newCall(request).await().use { response ->
            val body = response.body.string().orEmpty()
            if (!response.isSuccessful) {
                throw SarvamApiException(response.code, "Sarvam API error ${response.code}")
            }
            json.decodeFromString(LidResponse.serializer(), body)
        }
    }

    /** Suspends on OkHttp's async API and cancels the in-flight call on coroutine cancellation. */
    private suspend fun Call.await(): Response = suspendCancellableCoroutine { cont ->
        enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) = cont.resume(response)
            override fun onFailure(call: Call, e: IOException) {
                if (!cont.isCancelled) cont.resumeWithException(e)
            }
        })
        cont.invokeOnCancellation { runCatching { cancel() } }
    }

    companion object {
        private const val ENDPOINT = "https://api.sarvam.ai/text-lid"
        private val JSON_MEDIA_TYPE = "application/json".toMediaType()

        fun defaultClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }
}
