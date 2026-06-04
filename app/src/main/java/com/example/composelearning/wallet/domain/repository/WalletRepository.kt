package com.example.composelearning.wallet.domain.repository

import com.example.composelearning.wallet.domain.model.WalletCard

interface WalletRepository {
    suspend fun getCards(): List<WalletCard>
}
