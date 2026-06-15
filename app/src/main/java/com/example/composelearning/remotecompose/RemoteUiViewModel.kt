package com.example.composelearning.remotecompose

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

@Immutable
sealed interface RemoteUiState {
    data object Idle : RemoteUiState
    data object Loading : RemoteUiState

    /**
     * The raw RemoteCompose document the server built. We keep the bytes (not a
     * parsed document) in state so the ViewModel stays free of player types; the
     * screen turns these into a CoreDocument and hands them to the player.
     */
    data class Success(val documentBytes: ByteArray, val variant: Int) : RemoteUiState {
        override fun equals(other: Any?) = this === other ||
            (other is Success && variant == other.variant && documentBytes.contentEquals(other.documentBytes))
        override fun hashCode() = 31 * documentBytes.contentHashCode() + variant
    }

    data class Error(val message: String) : RemoteUiState
}

/**
 * Downloads a RemoteCompose document from the desktop server. The ONLY thing the
 * client decides is *which* variant to ask for; the server authors the entire
 * layout and ships it as bytes. There is no UI code here that knows what the
 * document looks like.
 */
class RemoteUiRepository(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()
) {
    suspend fun fetchDocument(baseUrl: String, variant: Int): ByteArray = withContext(Dispatchers.IO) {
        val url = baseUrl.trimEnd('/') + "/remoteui?variant=$variant"
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Server returned HTTP ${response.code}")
            response.body?.bytes() ?: throw IOException("Empty response body")
        }
    }
}

class RemoteUiViewModel(
    private val repository: RemoteUiRepository = RemoteUiRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<RemoteUiState>(RemoteUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun load(baseUrl: String, variant: Int) {
        _uiState.value = RemoteUiState.Loading
        viewModelScope.launch {
            runCatching { repository.fetchDocument(baseUrl, variant) }
                .onSuccess { _uiState.value = RemoteUiState.Success(it, variant) }
                .onFailure { _uiState.value = RemoteUiState.Error(it.message ?: "Failed to reach server") }
        }
    }
}
