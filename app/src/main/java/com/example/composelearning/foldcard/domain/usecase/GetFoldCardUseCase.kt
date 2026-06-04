package com.example.composelearning.foldcard.domain.usecase

import com.example.composelearning.foldcard.domain.model.FoldCardItem
import com.example.composelearning.foldcard.domain.repository.FoldCardRepository

class GetFoldCardUseCase(private val repository: FoldCardRepository) {
    suspend operator fun invoke(): FoldCardItem = repository.getCard()
}
