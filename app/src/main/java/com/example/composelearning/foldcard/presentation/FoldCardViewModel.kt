package com.example.composelearning.foldcard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.composelearning.foldcard.data.FoldCardRepositoryImpl
import com.example.composelearning.foldcard.domain.usecase.GetFoldCardUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FoldCardViewModel(
    private val getCard: GetFoldCardUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(FoldCardState())
    val state: StateFlow<FoldCardState> = _state.asStateFlow()

    init {
        onIntent(FoldCardIntent.Load)
    }

    fun onIntent(intent: FoldCardIntent) {
        when (intent) {
            FoldCardIntent.Load -> viewModelScope.launch {
                val card = getCard()
                _state.update { it.copy(isLoading = false, card = card) }
            }
        }
    }

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            FoldCardViewModel(GetFoldCardUseCase(FoldCardRepositoryImpl())) as T
    }
}
