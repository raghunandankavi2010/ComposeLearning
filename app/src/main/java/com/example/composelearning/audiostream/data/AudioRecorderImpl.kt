package com.example.composelearning.audiostream.data

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.example.composelearning.audiostream.domain.AudioRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive

/**
 * Native `AudioRecord` capture — 16 kHz, mono, 16-bit PCM. No third-party audio SDK.
 *
 * Implemented as a cold [flow] on [Dispatchers.IO]: the blocking `read` loop runs on a
 * background thread, and the `finally` block guarantees the recorder is stopped and released
 * the instant the collector is cancelled (ViewModel stop / `onCleared`).
 *
 * The caller must hold `RECORD_AUDIO`; the Compose layer requests it before collection starts.
 */
class AudioRecorderImpl : AudioRecorder {

    @SuppressLint("MissingPermission")
    override fun audioChunks(): Flow<ByteArray> = flow {
        val minBuffer = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
        require(minBuffer != AudioRecord.ERROR && minBuffer != AudioRecord.ERROR_BAD_VALUE) {
            "AudioRecord unsupported for ${SAMPLE_RATE}Hz mono PCM16 on this device"
        }
        val bufferSize = minBuffer * 2

        val record = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT,
            bufferSize,
        )
        try {
            record.startRecording()
            val buffer = ByteArray(bufferSize)
            while (currentCoroutineContext().isActive) {
                val read = record.read(buffer, 0, buffer.size)
                if (read > 0) emit(buffer.copyOf(read))
            }
        } finally {
            record.stop()
            record.release()
        }
    }.flowOn(Dispatchers.IO)

    companion object {
        const val SAMPLE_RATE = 16_000
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }
}
