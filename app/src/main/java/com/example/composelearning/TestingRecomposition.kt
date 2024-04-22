package com.example.composelearning

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun CreateAlertDialog() {
//    println( "Composing Screen Initial")
//    Column {
//        val openDialog = remember { mutableStateOf(false) }
//        println( "Composing Screen")
//
//        ButtonComposable {
//            openDialog.value = true
//        }
//
//        if (openDialog.value) {
//            BasicAlertDialog(
//                onDismissRequest = {
//                    openDialog.value = false
//                },
//                modifier = Modifier.padding(start = 32.dp, end = 32.dp)
//            ) {
//                Text(text = "Dialog Title")
//            }
//        }
//    }
//}
//
//@Composable
//fun ButtonComposable(openDialog: () -> Unit) {
//
//   println("Button Composable")
//
//    Button(
//        onClick = {
//            openDialog()
//        },
//        modifier = Modifier
//            .padding(16.dp)
//            .fillMaxWidth()
//    ) {
//        println("Inside Button Composable")
//        Text(text = "Open Dialog")
//    }
//
//}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAlertDialog() {
    val openDialog = remember { mutableStateOf(false) }

    Column {
        ButtonComposable {
            openDialog.value = true
        }
        MiddleText()

        if (openDialog.value) {
            BasicAlertDialog(
                onDismissRequest = { openDialog.value = false },
                modifier = Modifier.padding(start = 32.dp, end = 32.dp)
            ) {
                Text(text = "Dialog Title")
            }
        }
    }
}

@Composable
fun MiddleText() {

    Text(text = "Middle Text")
}

@Composable
fun ButtonComposable(openDialog: () -> Unit) {

    println("Button Composable")

    Box(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .clickable {
                openDialog()
            }
    ) {
        println("Inside Button Composable")
        Text(text = "Open Dialog")
    }

}