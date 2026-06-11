package com.example.composelearning.wallet.presentation

import com.example.composelearning.wallet.domain.model.WalletCard

data class WalletState(
    val isLoading: Boolean = true,
    val cards: List<WalletCard> = emptyList()
)

sealed interface WalletIntent {
    data object Load : WalletIntent
}
