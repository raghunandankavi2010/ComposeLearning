package com.example.composelearning.foldcard.data

import com.example.composelearning.foldcard.domain.model.FoldCardItem
import com.example.composelearning.foldcard.domain.repository.FoldCardRepository

class FoldCardRepositoryImpl : FoldCardRepository {
    override suspend fun getCard(): FoldCardItem =
        FoldCardItem(rank = "Q", suit = "♠", gradientStart = 0xFF1D2671, gradientEnd = 0xFFC33764)
}
