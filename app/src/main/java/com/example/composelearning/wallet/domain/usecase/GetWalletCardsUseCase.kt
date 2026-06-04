package com.example.composelearning.wallet.domain.usecase

import com.example.composelearning.wallet.domain.model.WalletCard
import com.example.composelearning.wallet.domain.repository.WalletRepository

class GetWalletCardsUseCase(private val repository: WalletRepository) {
    suspend operator fun invoke(): List<WalletCard> = repository.getCards()
}
