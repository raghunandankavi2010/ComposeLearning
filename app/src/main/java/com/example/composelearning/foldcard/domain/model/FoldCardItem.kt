package com.example.composelearning.foldcard.domain.model

/** The card shown in the fold demo. */
data class FoldCardItem(
    val rank: String,       // e.g. "Q"
    val suit: String,       // e.g. "♠"
    val gradientStart: Long,
    val gradientEnd: Long,
)
