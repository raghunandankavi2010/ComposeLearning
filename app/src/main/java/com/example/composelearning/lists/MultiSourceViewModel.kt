package com.example.composelearning.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import kotlinx.coroutines.flow.Flow

class MultiSourceViewModel(
    private val pagingSourceFactory: () -> PagingSource<Int, Item>
) : ViewModel() {
    val items: Flow<PagingData<Item>> = Pager(
        config = PagingConfig(
            pageSize = 10,
            enablePlaceholders = false
        ),
        pagingSourceFactory = pagingSourceFactory
    ).flow.cachedIn(viewModelScope)
}