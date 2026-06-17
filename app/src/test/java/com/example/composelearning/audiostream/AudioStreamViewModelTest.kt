package com.example.composelearning.audiostream

import com.example.composelearning.audiostream.domain.StreamAudioUseCase
import com.example.composelearning.audiostream.domain.StreamingState
import com.example.composelearning.audiostream.presentation.AudioStreamViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AudioStreamViewModelTest {

    @Before
    fun setUp() = Dispatchers.setMain(StandardTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(client: FakeAudioStreamClient): AudioStreamViewModel {
        val recorder = FakeAudioRecorder(listOf(byteArrayOf(1, 2, 3)), keepOpen = true)
        return AudioStreamViewModel(StreamAudioUseCase(recorder, client))
    }

    @Test
    fun `toggle starts streaming and opens the client`() = runTest {
        val client = FakeAudioStreamClient()
        val vm = viewModel(client)

        vm.onToggleStreaming()
        advanceUntilIdle()

        assertTrue("client opened", client.opened)
        assertTrue("UI shows an active session", vm.uiState.value.isActive)
        assertTrue("chunk was streamed", client.sentChunks.isNotEmpty())
    }

    @Test
    fun `stop cancels the session, finishes the client, and reports Stopped`() = runTest {
        val client = FakeAudioStreamClient()
        val vm = viewModel(client)

        vm.onToggleStreaming()   // start
        advanceUntilIdle()
        vm.onToggleStreaming()   // toggle again -> stop
        advanceUntilIdle()

        assertTrue("finish() called (END + close)", client.finished)
        assertFalse("no longer active", vm.uiState.value.isActive)
        assertEquals(StreamingState.Stopped, vm.uiState.value.state)
    }

    @Test
    fun `onCleared tears down an active stream`() = runTest {
        val client = FakeAudioStreamClient()
        val vm = viewModel(client)

        vm.onToggleStreaming()
        advanceUntilIdle()
        vm.callOnCleared()
        advanceUntilIdle()

        assertTrue("finish() called on ViewModel clear", client.finished)
    }
}

/** `onCleared` is protected; this test-only bridge invokes it via reflection. */
private fun AudioStreamViewModel.callOnCleared() {
    val m = androidx.lifecycle.ViewModel::class.java.getDeclaredMethod("onCleared")
    m.isAccessible = true
    m.invoke(this)
}
