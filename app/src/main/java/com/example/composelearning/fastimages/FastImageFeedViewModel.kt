package com.example.composelearning.fastimages

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

private const val INITIAL_DISHES = 120
private const val NEW_ARRIVALS = 4

class FastImageFeedViewModel : ViewModel() {

    private val _state = MutableStateFlow(
        FeedUiState(
            categories = DishFeedRepository.categories(),
            dishes = DishFeedRepository.dishes(INITIAL_DISHES)
        )
    )
    val state: StateFlow<FeedUiState> = _state.asStateFlow()

    fun setTuning(tuning: FeedTuning) {
        _state.update { it.copy(tuning = tuning) }
    }

    /**
     * Debug-only tier forcing. In production nothing writes to this — the tiers
     * are measured, not chosen — but being able to force them is the only way
     * to test the low-end path without owning a low-end phone.
     */
    fun setOverrides(overrides: PolicyOverrides) {
        _state.update { it.copy(overrides = overrides) }
    }

    /**
     * Prepends items, which is the operation that separates a stable `key` from
     * an index key. With stable keys Compose matches the existing items to
     * their slots and only the four new tiles do any work; with index keys
     * every slot now holds a different dish, so all of them re-bind and
     * re-request their image.
     */
    fun addNewArrivals() {
        _state.update { current ->
            current.copy(
                dishes = DishFeedRepository.dishes(NEW_ARRIVALS, current.nextDishIndex) + current.dishes,
                nextDishIndex = current.nextDishIndex + NEW_ARRIVALS
            )
        }
    }
}
