package com.example.composelearning

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SecondViewModel: ViewModel() {


    private val _redirect = MutableStateFlow<Boolean>(false)
    val redirect: StateFlow<Boolean> get() = _redirect

    fun redirectOnClick(newValue:Boolean) {
        _redirect.value = newValue
    }
}
