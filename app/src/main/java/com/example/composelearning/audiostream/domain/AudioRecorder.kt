package com.example.composelearning.audiostream.domain

import kotlinx.coroutines.flow.Flow

/**
 * Captures raw PCM audio. Pure abstraction — the data layer backs it with `AudioRecord`,
 * tests back it with a fake that emits canned chunks.
 */
interface AudioRecorder {
    /**
     * Cold flow of raw PCM chunks (16 kHz / mono / 16-bit). Collecting **starts** capture;
     * cancelling the collector **stops and releases** the underlying recorder.
     */
    fun audioChunks(): Flow<ByteArray>
}
