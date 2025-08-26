package com.example.composelearning.lists

// ListViewModel.kt
import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.ViewModel

class MyListViewModel : ViewModel() {
    // We will store the first visible item index and its offset
    var firstVisibleItemIndex: Int = 0
    var firstVisibleItemScrollOffset: Int = 0

    fun saveScrollPosition(listState: LazyListState) {
        firstVisibleItemIndex = listState.firstVisibleItemIndex
        firstVisibleItemScrollOffset = listState.firstVisibleItemScrollOffset
    }
}