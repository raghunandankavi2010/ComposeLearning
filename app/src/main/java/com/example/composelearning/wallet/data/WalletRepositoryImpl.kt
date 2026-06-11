package com.example.composelearning.wallet.data

import com.example.composelearning.wallet.domain.model.WalletCard
import com.example.composelearning.wallet.domain.repository.WalletRepository

/** Six gradient cards (drawn in Compose, no image assets) for the wallet stack. */
class WalletRepositoryImpl : WalletRepository {
    override suspend fun getCards(): List<WalletCard> = listOf(
        WalletCard(1, "Ada Lovelace", "1011", "VISA", 0xFF6A11CB, 0xFF2575FC),
        WalletCard(2, "Alan Turing", "4242", "Mastercard", 0xFFEB3349, 0xFFF45C43),
        WalletCard(3, "Grace Hopper", "7781", "VISA", 0xFF11998E, 0xFF38EF7D),
        WalletCard(4, "Linus Torvalds", "9003", "Amex", 0xFF373B44, 0xFF4286F4),
        WalletCard(5, "Margaret Hamilton", "5567", "Mastercard", 0xFFDA22FF, 0xFF9733EE),
        WalletCard(6, "Dennis Ritchie", "2389", "VISA", 0xFFF7971E, 0xFFFFD200)
    )
}
