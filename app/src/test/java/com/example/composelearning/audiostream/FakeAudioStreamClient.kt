package com.example.composelearning.audiostream

import com.example.composelearning.audiostream.domain.AudioStreamClient
import com.example.composelearning.audiostream.domain.ConnectionEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Test double for [AudioStreamClient]. Records what was sent and lets tests drive connection
 * events. `replay = 1` so a subscriber that attaches slightly after [open] still sees Connected.
 */
class FakeAudioStreamClient : AudioStreamClient {

    private val _events = MutableSharedFlow<ConnectionEvent>(replay = 1, extraBufferCapacity = 16)
    override val events: Flow<ConnectionEvent> = _events.asSharedFlow()

    val sentChunks = mutableListOf<ByteArray>()
    var opened = false
        private set
    var finished = false
        private set
    var closed = false
        private set

    fun emitFailure(message: String) = _events.tryEmit(ConnectionEvent.Failure(message))

    override fun open() {
        opened = true
        _events.tryEmit(ConnectionEvent.Connected)
    }

    override fun send(chunk: ByteArray) {
        sentChunks += chunk
    }

    override fun finish() {
        finished = true
        _events.tryEmit(ConnectionEvent.Disconnected)
    }

    override fun close() {
        closed = true
    }
}
