package com.example.composelearning.wallet.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.composelearning.wallet.data.WalletRepositoryImpl
import com.example.composelearning.wallet.domain.usecase.GetWalletCardsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WalletViewModel(
    private val getCards: GetWalletCardsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(WalletState())
    val state: StateFlow<WalletState> = _state.asStateFlow()

    init {
        onIntent(WalletIntent.Load)
    }

    fun onIntent(intent: WalletIntent) {
        when (intent) {
            WalletIntent.Load -> load()
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val cards = getCards()
            _state.update { it.copy(isLoading = false, cards = cards) }
        }
    }

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = WalletViewModel(GetWalletCardsUseCase(WalletRepositoryImpl())) as T
    }
}
