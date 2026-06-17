package com.example.composelearning.sarvamstt

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.concurrent.thread

/**
 * Records 16 kHz mono 16-bit PCM and writes a valid WAV file — the format Sarvam STT prefers.
 *
 * Why AudioRecord and not MediaRecorder? MediaRecorder cannot emit WAV/PCM (only AMR, AAC/MP4,
 * 3GP). AudioRecord hands us raw PCM frames, which we wrap with a 44-byte WAV header here. It is
 * a standard framework audio tool, so this still satisfies the "standard Android audio" brief.
 *
 * Threading: [start] spins up one capture thread; [stop] joins it and patches the header sizes.
 */
class WavAudioRecorder {

    @Volatile private var isRecording = false
    private var recordThread: Thread? = null
    private var outputFile: File? = null

    val recording: Boolean get() = isRecording

    @SuppressLint("MissingPermission") // caller must hold RECORD_AUDIO (checked in the UI layer)
    fun start(output: File) {
        if (isRecording) return
        outputFile = output

        val minBuffer = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
        require(minBuffer != AudioRecord.ERROR && minBuffer != AudioRecord.ERROR_BAD_VALUE) {
            "AudioRecord unsupported on this device for ${SAMPLE_RATE}Hz mono PCM16"
        }
        val bufferSize = minBuffer * 2

        val recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT,
            bufferSize,
        )

        isRecording = true
        recordThread = thread(name = "wav-capture") {
            recorder.startRecording()
            output.outputStream().buffered().use { out ->
                out.write(ByteArray(WAV_HEADER_SIZE)) // reserve header; patched on stop()
                val buffer = ByteArray(bufferSize)
                while (isRecording) {
                    val read = recorder.read(buffer, 0, buffer.size)
                    if (read > 0) out.write(buffer, 0, read)
                }
            }
            recorder.stop()
            recorder.release()
        }
    }

    /** Stops capture, finalizes the WAV header, and returns the completed file (or null). */
    fun stop(): File? {
        if (!isRecording) return outputFile
        isRecording = false
        recordThread?.join()
        recordThread = null
        return outputFile?.also { writeWavHeader(it) }
    }

    /** Backfills the RIFF/data chunk sizes now that the total PCM byte count is known. */
    private fun writeWavHeader(file: File) {
        val totalAudioLen = file.length() - WAV_HEADER_SIZE
        val totalDataLen = totalAudioLen + 36
        val byteRate = SAMPLE_RATE * CHANNELS * BITS_PER_SAMPLE / 8

        val header = ByteBuffer.allocate(WAV_HEADER_SIZE).order(ByteOrder.LITTLE_ENDIAN).apply {
            put("RIFF".toByteArray())
            putInt(totalDataLen.toInt())
            put("WAVE".toByteArray())
            put("fmt ".toByteArray())
            putInt(16)                                   // PCM fmt chunk size
            putShort(1)                                  // audio format = PCM
            putShort(CHANNELS.toShort())
            putInt(SAMPLE_RATE)
            putInt(byteRate)
            putShort((CHANNELS * BITS_PER_SAMPLE / 8).toShort()) // block align
            putShort(BITS_PER_SAMPLE.toShort())
            put("data".toByteArray())
            putInt(totalAudioLen.toInt())
        }

        RandomAccessFile(file, "rw").use { raf ->
            raf.seek(0)
            raf.write(header.array())
        }
    }

    companion object {
        const val SAMPLE_RATE = 16_000
        private const val CHANNELS = 1
        private const val BITS_PER_SAMPLE = 16
        private const val WAV_HEADER_SIZE = 44
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }
}
