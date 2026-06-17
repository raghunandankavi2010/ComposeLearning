package com.example.composelearning.audiostream.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch

/**
 * Orchestrates a streaming session: opens the [client], pipes every [recorder] chunk to it,
 * and surfaces a single [StreamingState] stream.
 *
 * Cold by design — collecting starts the session; cancelling the collector tears it down
 * (`finish()` flushes "END" and closes the socket; the recorder flow stops & releases).
 * Both collaborators are interfaces, so this use case is unit-testable with fakes.
 */
class StreamAudioUseCase(
    private val recorder: AudioRecorder,
    private val client: AudioStreamClient,
) {
    operator fun invoke(): Flow<StreamingState> = channelFlow {
        send(StreamingState.Connecting)

        // Forward connection lifecycle onto the state stream.
        val eventsJob = launch {
            client.events.collect { event ->
                when (event) {
                    ConnectionEvent.Connected -> send(StreamingState.Streaming())
                    is ConnectionEvent.Failure -> send(StreamingState.Error(event.message))
                    ConnectionEvent.Disconnected -> Unit
                }
            }
        }

        client.open()

        var bytesSent = 0L
        try {
            // Runs until the collector is cancelled (real recorder is infinite).
            recorder.audioChunks().collect { chunk ->
                client.send(chunk)
                bytesSent += chunk.size
                send(StreamingState.Streaming(bytesSent))
            }
        } finally {
            client.finish()
            eventsJob.cancel()
        }
    }
}
