package com.example.composelearning.wallet.domain.model

/** A payment card shown in the collapsing wallet stack. */
data class WalletCard(
    val id: Int,
    val holder: String,
    val last4: String,
    val network: String,        // e.g. "VISA", "Mastercard"
    val gradientStart: Long,    // ARGB
    val gradientEnd: Long,      // ARGB
)
