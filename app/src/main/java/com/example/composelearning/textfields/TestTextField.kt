package com.example.composelearning.textfields

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AmountTextField(
    modifier: Modifier,
) {
    var textState by remember { mutableStateOf(TextFieldValue("Hello World")) }
    BasicTextField(modifier = Modifier.widthIn(250.dp), value = textState, onValueChange = {
        textState = it
    })
}

