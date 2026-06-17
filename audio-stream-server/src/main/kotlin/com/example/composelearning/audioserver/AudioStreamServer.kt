package com.example.composelearning.audioserver

import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import java.io.BufferedOutputStream
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Real-time duplex audio streaming server.
 *
 *   ws://<host>:8080/stream
 *
 * Protocol (matches the Android client):
 *   • Binary frames  -> raw 16 kHz / mono / 16-bit PCM chunks, appended to a .pcm file.
 *   • Text  "END"    -> client signals end-of-utterance; we close gracefully.
 *   • Socket close / failure -> we flush and close the file.
 *
 * Each connection writes to its own timestamped file under ./recordings so concurrent
 * clients never clobber each other. Play a capture back with, e.g.:
 *   ffplay -f s16le -ar 16000 -ch_layout mono recordings/<file>.pcm
 */
private const val PORT = 8080
private const val END_SIGNAL = "END"
private val RECORDINGS_DIR = File("recordings")

fun main() {
    RECORDINGS_DIR.mkdirs()
    println("Audio stream server listening on ws://0.0.0.0:$PORT/stream")
    println("Recordings -> ${RECORDINGS_DIR.absolutePath}")

    embeddedServer(CIO, port = PORT, host = "0.0.0.0") {
        install(WebSockets) {
            // Keep idle sockets alive (Long-millis properties on Ktor's WebSocketOptions).
            pingPeriodMillis = 15_000
            timeoutMillis = 30_000
            maxFrameSize = Long.MAX_VALUE
        }
        routing {
            webSocket("/stream") {
                val file = newRecordingFile()
                val out = BufferedOutputStream(file.outputStream())
                var bytesWritten = 0L
                println("▶ client connected — writing to ${file.name}")

                try {
                    for (frame in incoming) {
                        when (frame) {
                            is Frame.Binary -> {
                                val chunk = frame.data
                                out.write(chunk)
                                bytesWritten += chunk.size
                            }

                            is Frame.Text -> {
                                if (frame.readText().trim().equals(END_SIGNAL, ignoreCase = true)) {
                                    println("⏹ END received")
                                    close(CloseReason(CloseReason.Codes.NORMAL, "END"))
                                }
                            }

                            else -> Unit // Ping/Pong/Close handled by the engine
                        }
                    }
                } catch (e: Exception) {
                    // ClosedReceiveChannelException etc. — client dropped; not fatal.
                    println("⚠ stream ended: ${e.message}")
                } finally {
                    out.flush()
                    out.close()
                    val seconds = bytesWritten / 2.0 / 16_000.0 // 16-bit mono @ 16 kHz
                    println("✔ saved ${file.name} — $bytesWritten bytes (~%.1fs)".format(seconds))
                }
            }
        }
    }.start(wait = true)
}

private fun newRecordingFile(): File {
    val stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS"))
    return File(RECORDINGS_DIR, "audio-$stamp.pcm")
}
