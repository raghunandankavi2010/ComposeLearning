package com.example.composelearning.lists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow

@Composable
fun DummyList(viewModel: ListViewModel, updateItem: (DummyItem) -> Unit) {
    val dummyItems = viewModel.getDummyItems().collectAsLazyPagingItems()

    Column {
        LazyColumn {
            items(count = dummyItems.itemCount,key = { index -> dummyItems[index]?.id ?: 0 }) {
                 index ->
                    val dummyItem = dummyItems[index]
                    if (dummyItem != null) {
                        Text(dummyItem.content, modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clickable {
                                updateItem(dummyItem)
                            })
                        Text(dummyItem.content, modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clickable {
                                updateItem(dummyItem)
                            })

                    }
                }
            }
        }
    }


data class DummyItem(val id: Int, val content: String)

sealed class UIActions {
    data class Update(val dummyItem: DummyItem) : UIActions()
}

class ListViewModel : ViewModel() {

    private val getDummyUseCase = GetDummyUseCase()

    private var currentPestAndDiseaseRiskList: Flow<PagingData<DummyItem>>? = null

    private val modificationEvents = MutableStateFlow<List<UIActions>>(emptyList())
    fun getDummyItems(): Flow<PagingData<DummyItem>> {
        val lastResult = currentPestAndDiseaseRiskList
        if (lastResult != null) {
            return lastResult
        }
        modificationEvents.value = emptyList()
        val newResult: Flow<PagingData<DummyItem>> =
            getDummyUseCase.invoke().cachedIn(viewModelScope)
                .combine(modificationEvents) { pagingData, modifications ->
                    modifications.fold(pagingData) { acc, event ->
                        applyEvents(acc, event)
                    }
                }
        currentPestAndDiseaseRiskList = newResult
        return newResult
    }

    fun updateItems(uiActions: UIActions) {
        modificationEvents.value += uiActions
    }

    private fun applyEvents(
        paging: PagingData<DummyItem>,
        uiActions: UIActions
    ): PagingData<DummyItem> {
        return when (uiActions) {
            is UIActions.Update -> {
                paging
                    .map {
                        if (uiActions.dummyItem.id == it.id) return@map it.copy(content = "Item Changed to ABC")
                        else return@map it
                    }
            }
        }
    }
}

class GetDummyUseCase {
    operator fun invoke(): Flow<PagingData<DummyItem>> {
        return flow {
            emit(PagingData.from(createDummyItems()))
        }
    }

    private fun createDummyItems(): List<DummyItem> {
        val list = mutableListOf<DummyItem>()
        repeat(200) {
            list.add(DummyItem(it, "Item $it"))
        }
        return list
    }
}