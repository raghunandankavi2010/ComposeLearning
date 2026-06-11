package com.example.composelearning.pathmorph.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.composelearning.pathmorph.data.PhoneShapeRepositoryImpl
import com.example.composelearning.pathmorph.domain.usecase.GetPhoneShapesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PathMorphViewModel(
    private val getShapes: GetPhoneShapesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(PathMorphState())
    val state: StateFlow<PathMorphState> = _state.asStateFlow()

    init {
        onIntent(PathMorphIntent.Load)
    }

    fun onIntent(intent: PathMorphIntent) {
        when (intent) {
            PathMorphIntent.Load -> load()
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val phones = getShapes()
            _state.update { it.copy(isLoading = false, phones = phones) }
        }
    }

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = PathMorphViewModel(GetPhoneShapesUseCase(PhoneShapeRepositoryImpl())) as T
    }
}
