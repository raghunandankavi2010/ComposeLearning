package com.example.composelearning.audiostream.domain

import kotlinx.coroutines.flow.Flow

/** Connection lifecycle signals surfaced by an [AudioStreamClient]. */
sealed interface ConnectionEvent {
    data object Connected : ConnectionEvent
    data object Disconnected : ConnectionEvent
    data class Failure(val message: String) : ConnectionEvent
}

/**
 * Transport abstraction for streaming audio to the server. The data layer backs it with an
 * OkHttp WebSocket; tests back it with a fake that records sent chunks and emits events.
 */
interface AudioStreamClient {
    /** Hot stream of connection lifecycle events. */
    val events: Flow<ConnectionEvent>

    /** Open the connection. [events] emits [ConnectionEvent.Connected] once established. */
    fun open()

    /** Send one binary audio chunk. No-op if not connected. */
    fun send(chunk: ByteArray)

    /** Signal end-of-stream ("END") and close gracefully. */
    fun finish()

    /** Hard-cancel and release the connection (lifecycle teardown). */
    fun close()
}
