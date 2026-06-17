package com.example.composelearning.audiostream

import com.example.composelearning.audiostream.domain.StreamAudioUseCase
import com.example.composelearning.audiostream.domain.StreamingState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StreamAudioUseCaseTest {

    @Test
    fun `opens client, forwards every chunk, then finishes`() = runTest {
        val chunks = listOf(byteArrayOf(1, 2), byteArrayOf(3, 4, 5), byteArrayOf(6))
        val recorder = FakeAudioRecorder(chunks)          // finite -> flow completes
        val client = FakeAudioStreamClient()

        val states = StreamAudioUseCase(recorder, client)().toList()

        assertTrue("client should be opened", client.opened)
        assertEquals("all chunks forwarded", chunks.size, client.sentChunks.size)
        assertTrue("finish() called on completion", client.finished)
        assertEquals("first state is Connecting", StreamingState.Connecting, states.first())
        assertTrue("emits a Streaming state", states.any { it is StreamingState.Streaming })
    }

    @Test
    fun `maps a connection failure to an Error state and still finishes`() = runTest {
        val recorder = FakeAudioRecorder(emptyList(), keepOpen = true) // live mic
        val client = FakeAudioStreamClient()

        val states = mutableListOf<StreamingState>()
        val job = launch { StreamAudioUseCase(recorder, client)().collect { states += it } }

        advanceUntilIdle()              // Connecting -> open() -> Connected -> Streaming
        client.emitFailure("boom")
        advanceUntilIdle()
        job.cancel()                    // user stop / ViewModel cleared
        advanceUntilIdle()

        assertTrue("Error surfaced", states.any { it is StreamingState.Error && it.message == "boom" })
        assertTrue("finish() called on teardown", client.finished)
    }
}
