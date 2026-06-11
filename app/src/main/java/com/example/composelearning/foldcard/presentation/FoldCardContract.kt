package com.example.composelearning.foldcard.presentation

import com.example.composelearning.foldcard.domain.model.FoldCardItem

data class FoldCardState(
    val isLoading: Boolean = true,
    val card: FoldCardItem? = null
)

sealed interface FoldCardIntent {
    data object Load : FoldCardIntent
}
