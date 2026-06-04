package com.example.composelearning.applerings.presentation

import com.example.composelearning.applerings.domain.model.RingSpec

data class ActivityRingsState(
    val isLoading: Boolean = true,
    val rings: List<RingSpec> = emptyList(),
)

sealed interface ActivityRingsIntent {
    data object Load : ActivityRingsIntent
}
