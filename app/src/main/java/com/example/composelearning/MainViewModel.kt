package com.example.composelearning

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainViewModel: ViewModel() {


    private val _searchWidgetVisibility = MutableStateFlow<Boolean>(false)
    val searchWidgetVisibility: StateFlow<Boolean> get() = _searchWidgetVisibility

    fun updateSearchWidgetVisibility(newValue:Boolean) {
        _searchWidgetVisibility.value = newValue
    }
}
