package com.example.composelearning.pathmorph.presentation

import com.example.composelearning.pathmorph.domain.model.PhoneShape

data class PathMorphState(
    val isLoading: Boolean = true,
    val phones: List<PhoneShape> = emptyList()
)

sealed interface PathMorphIntent {
    data object Load : PathMorphIntent
}
