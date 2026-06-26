package com.example.composelearning.promotions

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * In-memory [DealStore] for tests. Optionally pre-seeded to simulate a value that survived process
 * death, and records how many times a save happened so tests can assert persistence behaviour.
 */
class FakeDealStore(initial: Long? = null) : DealStore {

    private val state = MutableStateFlow(initial)
    override val targetEndTime: Flow<Long?> = state

    var saveCount = 0
        private set

    val savedValue: Long?
        get() = state.value

    override suspend fun saveTargetEndTime(timestamp: Long) {
        saveCount++
        state.value = timestamp
    }
}
