package com.example.composelearning.audiostream.data

import com.example.composelearning.audiostream.domain.AudioStreamClient
import com.example.composelearning.audiostream.domain.ConnectionEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString.Companion.toByteString

/**
 * Streams audio over a WebSocket using OkHttp. Binary frames carry PCM chunks; a final
 * text frame "END" signals end-of-stream. All sends are async and non-blocking — OkHttp
 * enqueues them on its own dispatcher, so the UI thread is never touched.
 *
 * [client] is injected so tests/timeouts/cleartext config live in one place.
 */
class OkHttpAudioStreamClient(
    private val client: OkHttpClient,
    private val url: String,
) : AudioStreamClient {

    private val _events = MutableSharedFlow<ConnectionEvent>(extraBufferCapacity = 16)
    override val events: Flow<ConnectionEvent> = _events.asSharedFlow()

    @Volatile
    private var webSocket: WebSocket? = null

    override fun open() {
        val request = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                _events.tryEmit(ConnectionEvent.Connected)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(NORMAL_CLOSURE, null)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                _events.tryEmit(ConnectionEvent.Disconnected)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _events.tryEmit(ConnectionEvent.Failure(t.message ?: "WebSocket failure"))
            }
        })
    }

    override fun send(chunk: ByteArray) {
        // Binary frame — OkHttp queues it; returns false only if the socket is closed/closing.
        webSocket?.send(chunk.toByteString())
    }

    override fun finish() {
        webSocket?.send(END_SIGNAL)
        webSocket?.close(NORMAL_CLOSURE, END_SIGNAL)
        webSocket = null
    }

    override fun close() {
        webSocket?.cancel()
        webSocket = null
    }

    private companion object {
        const val NORMAL_CLOSURE = 1000
        const val END_SIGNAL = "END"
    }
}
