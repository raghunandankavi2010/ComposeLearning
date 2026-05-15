package com.example.composelearning.lists

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow


@Composable
fun SwipetoDismiss() {
    val viewModel: MyViewModel = viewModel()
    TutorialContent(viewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TutorialContent(viewModel: MyViewModel) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = {
            viewModel.newList()
        }) {
            Text(text = "Generate User List")
        }

        val usersList by viewModel.listFlow.collectAsStateWithLifecycle()
        val list = usersList.toList()
        LazyColumn {

            itemsIndexed(list, key = { _, user -> user.id }) { index, user ->

                val currentItem by rememberUpdatedState(index)

                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { dismissValue ->
                        when (dismissValue) {

                            SwipeToDismissBoxValue.StartToEnd -> {
                                viewModel.removeItem(currentItem)
                                true
                            }
                            SwipeToDismissBoxValue.EndToStart ->{
                                viewModel.removeItem(currentItem)
                                true
                            }
                            else -> {
                                false
                            }
                        }
                    }
                )
                SwipeToDismissBox(
                    state = dismissState,
                    modifier = Modifier.padding(vertical = 4.dp),
                    backgroundContent = {

                        val direction = dismissState.dismissDirection

                        val color by animateColorAsState(

                            when (dismissState.targetValue) {
                                SwipeToDismissBoxValue.Settled -> Color.LightGray
                                SwipeToDismissBoxValue.StartToEnd  -> Color.Green
                                SwipeToDismissBoxValue.EndToStart -> Color.Red
                            }, label = ""
                        )
                        val alignment = when (direction) {
                            SwipeToDismissBoxValue.StartToEnd  -> Alignment.CenterStart
                            SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                            else -> {
                                Alignment.CenterStart
                            }
                        }
                        val icon = when (direction) {
                            SwipeToDismissBoxValue.StartToEnd  -> Icons.Default.Done
                            SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                            else -> {
                                Icons.Default.Done
                            }
                        }
                        val scale by animateFloatAsState(
                            if (dismissState.targetValue == SwipeToDismissBoxValue.Settled) 0.75f else 1f,
                            label = ""
                        )

                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(color)
                                .padding(horizontal = 20.dp),
                            contentAlignment = alignment
                        ) {
                            Icon(
                                icon,
                                contentDescription = "Localized description",
                                modifier = Modifier.scale(scale)
                            )
                        }
                    }
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 0.dp,
                            pressedElevation = animateDpAsState(
                                if (dismissState.dismissDirection != SwipeToDismissBoxValue.Settled) 4.dp else 0.dp,
                                label = ""
                            ).value
                        )
                    ) {

                        Text(modifier = Modifier.height(80.dp),
                            text= user.name,
                            fontWeight = FontWeight.Bold)

                    }
                }
            }
        }
    }
}


class MyViewModel : ViewModel() {
    private var userList = mutableStateListOf<User>()
    var listFlow = MutableStateFlow<List<User>>(userList)

    fun newList() {
        userList.clear()
        for (i in 0..10) {
            userList.add(User(i, "User$i"))
        }
        listFlow.value = userList.toList()
    }

    fun removeItem(index: Int) {
        if (index in userList.indices) {
            userList.removeAt(index)
            listFlow.value = userList.toList()
        }
    }
}

data class User(val id: Int, val name: String)
