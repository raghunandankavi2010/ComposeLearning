package com.example.composelearning.lists

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

data class Message(
    val id: Int,
    val message: String,
)

fun getList(): MutableList<Message> {
    val list = mutableListOf<Message>()
    for (i in 0..100) {
        val message = Message(i, "$i Message")
        list.add(message)
    }
    return list
}

@Composable
fun SingleSelectableItem(messages : MutableList<Message>) {
    val listState = rememberLazyListState()
    var selectedIndex by remember { mutableStateOf(-1) }

    val onItemClick = { index: Int -> selectedIndex = index}

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ){
        itemsIndexed(messages){ index,message, ->
            ItemView(
                message = message.message,
                selected = selectedIndex == index,
                onClick = onItemClick,
                index = index
            )
        }
    }
}

@Composable
fun ItemView(message: String, selected: Boolean, onClick: (Int) -> Unit, index: Int){
    Text(
        text = message,
        modifier = Modifier
            .clickable {
                onClick.invoke(index)
            }
            .background(if (selected) MaterialTheme.colorScheme.secondary else Color.Transparent)
            .fillMaxWidth()
            .padding(12.dp)
    )
}