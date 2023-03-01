package com.example.composelearning.dropdown

import androidx.compose.material3.*
import androidx.compose.runtime.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDropdownMenu() {
    val options = listOf("Option 1", "Option 2", "Option 3", "Option 4", "Option 5")
    var exp by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf("") }
    ExposedDropdownMenuBox(expanded = exp, onExpandedChange = { exp = !exp }) {
        TextField(
            value = selectedOption,
            onValueChange = {
                selectedOption = it
            },
            label = { Text("Label") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = exp)
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors()
        )
        // filter options based on text field value (i.e. crude autocomplete)
        val filterOpts = options.filter { it.contains(selectedOption, ignoreCase = true) }
        if (filterOpts.isNotEmpty()) {
            ExposedDropdownMenu(expanded = exp, onDismissRequest = { exp = false }) {
                filterOpts.forEach { option ->
                    DropdownMenuItem(
                        onClick = {
                            selectedOption = option
                            exp = false
                        }
                    , text = { Text(text = option) }
                    )
                }
            }
        }
    }
}


