package com.example.composelearning.foldcard.domain.repository

import com.example.composelearning.foldcard.domain.model.FoldCardItem

interface FoldCardRepository {
    suspend fun getCard(): FoldCardItem
}
