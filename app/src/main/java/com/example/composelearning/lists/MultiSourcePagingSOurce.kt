package com.example.composelearning.lists

import androidx.paging.PagingSource
import androidx.paging.PagingState

data class Item(val id: Int, val value: String)

class MultiSourcePagingSource(
    private val dataSources: List<List<Item>> // Simulate multiple data sources
) : PagingSource<Int, Item>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Item> {
        val page = params.key ?: 0
        val pageSize = 10

        // Fetch items from multiple data sources for the page
        val items = mutableListOf<Item>()
        for (i in 0 until pageSize) {
            val sourceIndex = i % dataSources.size // Rotate through data sources
            val sourceItems = dataSources[sourceIndex]
            val itemIndex = (page * pageSize) + i

            if (itemIndex < sourceItems.size) {
                items.add(sourceItems[itemIndex])
            }
        }

        return LoadResult.Page(
            data = items,
            prevKey = if (page > 0) page - 1 else null,
            nextKey = if (items.isNotEmpty()) page + 1 else null
        )
    }

    override fun getRefreshKey(state: PagingState<Int, Item>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val closestPage = state.closestPageToPosition(anchorPosition)
            closestPage?.prevKey?.plus(1) ?: closestPage?.nextKey?.minus(1)
        }
    }
}

// Generate dummy data
fun generateDummyData(): List<List<Item>> {
    val sources = mutableListOf<List<Item>>()
    repeat(5) { sourceIndex -> // 5 data sources
        val items = mutableListOf<Item>()
        repeat(100) { itemIndex -> // Each source has 50 items
            items.add(Item(id = itemIndex, value = "Source $sourceIndex Item $itemIndex"))
        }
        sources.add(items)
    }
    return sources
}