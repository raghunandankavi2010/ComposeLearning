package com.example.composelearning.audiostream

import com.example.composelearning.audiostream.domain.AudioRecorder
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Test double for [AudioRecorder]. Emits [chunks] in order, then either completes (default)
 * or stays open until the collector is cancelled ([keepOpen] = true, mimicking a live mic).
 */
class FakeAudioRecorder(
    private val chunks: List<ByteArray>,
    private val keepOpen: Boolean = false,
) : AudioRecorder {
    override fun audioChunks(): Flow<ByteArray> = flow {
        chunks.forEach { emit(it) }
        if (keepOpen) awaitCancellation()
    }
}
