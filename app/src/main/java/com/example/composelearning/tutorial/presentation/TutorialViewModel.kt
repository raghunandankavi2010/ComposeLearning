package com.example.composelearning.tutorial.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.composelearning.tutorial.data.CardsRepositoryImpl
import com.example.composelearning.tutorial.data.TutorialRepositoryImpl
import com.example.composelearning.tutorial.domain.usecase.GetCardsUseCase
import com.example.composelearning.tutorial.domain.usecase.GetTutorialStepsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class TutorialViewModel(
    private val getCards: GetCardsUseCase,
    private val getSteps: GetTutorialStepsUseCase,
) : ViewModel() {

    private val tutorialState = MutableStateFlow<TutorialUiState.TutorialState>(
        TutorialUiState.TutorialState.Idle
    )

    private val cardsState = getCards()
        .map<_, TutorialUiState.CardsState> { TutorialUiState.CardsState.Ready(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = TutorialUiState.CardsState.Loading,
        )

    val uiState: StateFlow<TutorialUiState> = combine(cardsState, tutorialState) { cards, tutorial ->
        TutorialUiState(cards = cards, tutorial = tutorial)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = TutorialUiState(),
    )

    fun onEvent(event: TutorialEvent) {
        when (event) {
            TutorialEvent.Start -> startTutorial()
            TutorialEvent.Next -> advance(+1)
            TutorialEvent.Previous -> advance(-1)
            TutorialEvent.Skip -> tutorialState.value = TutorialUiState.TutorialState.Idle
        }
    }

    private fun startTutorial() {
        val steps = getSteps()
        if (steps.isEmpty()) return
        tutorialState.value = TutorialUiState.TutorialState.Running(steps, currentIndex = 0)
    }

    private fun advance(delta: Int) {
        val current = tutorialState.value as? TutorialUiState.TutorialState.Running ?: return
        val next = current.currentIndex + delta
        tutorialState.value = when {
            next < 0 -> current
            next > current.steps.lastIndex -> TutorialUiState.TutorialState.Idle
            else -> current.copy(currentIndex = next)
        }
    }

    companion object {
        private const val STOP_TIMEOUT_MS = 5_000L

        fun factory(): androidx.lifecycle.ViewModelProvider.Factory =
            object : androidx.lifecycle.ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val cardsRepo = CardsRepositoryImpl()
                    val tutorialRepo = TutorialRepositoryImpl()
                    return TutorialViewModel(
                        getCards = GetCardsUseCase(cardsRepo),
                        getSteps = GetTutorialStepsUseCase(tutorialRepo),
                    ) as T
                }
            }
    }
}