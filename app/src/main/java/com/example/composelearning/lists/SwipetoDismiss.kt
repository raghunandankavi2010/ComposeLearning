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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismiss
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
import kotlinx.coroutines.flow.MutableStateFlow


@Composable
fun SwipetoDismiss() {
    val viewModel = MyViewModel()
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

        val usersList = viewModel.listFlow.collectAsStateWithLifecycle()
        val list = usersList.value.toList()
        LazyColumn {

            items(list.size, key = { index -> list[index].id }) { user ->

                val currentItem by rememberUpdatedState(user)

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
                SwipeToDismiss(
                    state = dismissState,
                    modifier = Modifier.padding(vertical = 4.dp),
                    directions = setOf(SwipeToDismissBoxValue.StartToEnd, SwipeToDismissBoxValue.EndToStart),

                    background = {

                        val direction = dismissState.dismissDirection ?: return@SwipeToDismiss

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
                    },
                    dismissContent = {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(
                                0.dp, pressedElevation = animateDpAsState(
                                    if (dismissState.dismissDirection != null) 4.dp else 0.dp,
                                    label = ""
                                ).value, 0.dp, 0.dp, 0.dp, 0.dp
                            )
                        ) {

                            Text(modifier = Modifier.height(80.dp),
                                text= list[currentItem].name,
                                fontWeight = FontWeight.Bold)

                        }
                    }
                )
            }
        }
    }
}


class MyViewModel : ViewModel() {
    private var userList = mutableStateListOf<User>()
    var listFlow = MutableStateFlow(userList)

    fun newList() {
        val mutableList = mutableStateListOf<User>()
        for (i in 0..10) {
            mutableList.add(User(i, "User$i"))
        }

        userList = mutableList
        listFlow.value = mutableList
    }

    fun removeItem(index: Int) {
        userList.remove(userList[index])
    }
}

data class User(val id: Int, val name: String)



