package com.example.composelearning.audiostream.di

import com.example.composelearning.audiostream.data.AudioRecorderImpl
import com.example.composelearning.audiostream.data.OkHttpAudioStreamClient
import com.example.composelearning.audiostream.domain.AudioRecorder
import com.example.composelearning.audiostream.domain.AudioStreamClient
import com.example.composelearning.audiostream.domain.StreamAudioUseCase
import com.example.composelearning.audiostream.presentation.AudioStreamViewModel
import okhttp3.OkHttpClient
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Koin graph for the audiostream feature. Bound to interfaces so every layer is swappable
 * (real impls here; fakes in tests). Recorder/client are `factory` so each session gets a
 * fresh instance; the [OkHttpClient] is a `single` (it pools threads/connections).
 *
 * `10.0.2.2` is the Android emulator's alias for the host machine's localhost — cleartext to
 * it is already permitted by res/xml/network_security_config.xml. On a physical device, swap
 * in your machine's LAN IP.
 */
private const val SERVER_URL = "ws://10.0.2.2:8080/stream"

val audioStreamModule = module {
    single { OkHttpClient() }
    factory<AudioRecorder> { AudioRecorderImpl() }
    factory<AudioStreamClient> { OkHttpAudioStreamClient(client = get(), url = SERVER_URL) }
    factory { StreamAudioUseCase(recorder = get(), client = get()) }
    viewModelOf(::AudioStreamViewModel)
}
