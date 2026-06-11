package com.example.composelearning.ipodwheel.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.composelearning.ipodwheel.data.SongRepositoryImpl
import com.example.composelearning.ipodwheel.domain.usecase.GetSongsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class IpodViewModel(
    private val getSongs: GetSongsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(IpodState())
    val state: StateFlow<IpodState> = _state.asStateFlow()

    init {
        onIntent(IpodIntent.Load)
    }

    fun onIntent(intent: IpodIntent) {
        when (intent) {
            IpodIntent.Load -> viewModelScope.launch {
                val songs = getSongs()
                _state.update { it.copy(isLoading = false, songs = songs) }
            }

            is IpodIntent.Select -> _state.update { it.copy(nowPlaying = intent.index) }
        }
    }

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = IpodViewModel(GetSongsUseCase(SongRepositoryImpl())) as T
    }
}
