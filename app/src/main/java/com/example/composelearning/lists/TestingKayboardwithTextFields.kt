package com.example.composelearning.lists

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun TestTextFieldList() {
    Scaffold(
        content = { paddingValues ->
            val state = rememberLazyListState()
            val textFieldValues = remember { mutableStateMapOf<Int, String>() }

            LazyColumn(
                state = state,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(12) { item ->
                    YourTextField(index = item, textFieldValues = textFieldValues)
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    )
}

//YourTextField
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun YourTextField(index: Int, textFieldValues: MutableMap<Int, String>) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()
    var text by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    Column {
        TextField(
            value = textFieldValues[index] ?: text,
            onValueChange = {
                textFieldValues[index] = it
                text = it
            },
            modifier = Modifier
                .focusRequester(focusRequester)
                .bringIntoViewRequester(bringIntoViewRequester)
                .onFocusEvent { focusState ->
                    if (focusState.isFocused) {
                        coroutineScope.launch {
                            bringIntoViewRequester.bringIntoView()
                        }
                    }
                }
        )
        Text(text = "Value: ${textFieldValues[index] ?: ""}")
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }
}