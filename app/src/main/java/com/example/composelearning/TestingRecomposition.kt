package com.example.composelearning

import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.*
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TextWidth(modifier: Modifier = Modifier) {

    val headingSmall: TextStyle = TextStyle(
        fontSize = 32.sp,
        lineHeight = 32.sp,
        fontFamily = FontFamily(Font(R.font.jio_type_black)),
        fontWeight = FontWeight.Black
    )

    Row {
        BoxWithConstraints(modifier = Modifier.weight(1f)) {
        constraints.minWidth
            val text = "Hello World. this is a very long text.............................."
            val measuredText = text.measureTextWidth(headingSmall)
            val width = DpToInt(measuredText)
            if(width <= constraints.maxWidth) {
                Text(text, maxLines = 1)
            } else {
                Text(text, maxLines = 1, modifier = Modifier.basicMarquee())
            }
        }


        Image(painter = painterResource(id = R.drawable.ic_launcher_background), contentDescription = "")

    }

}

@Composable
fun DpToInt(dpValue: Dp): Int {
    val density = LocalDensity.current
    return with(density) { dpValue.toPx() }.toInt()
}


@Composable
fun String.measureTextWidth(style: TextStyle): Dp {
    val textMeasurer = rememberTextMeasurer()
    val widthInPixels = textMeasurer.measure(this, style).size.width
    return with(LocalDensity.current) { widthInPixels.toDp() }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAlertDialog() {
    val openDialog = remember { mutableStateOf(false) }
        println( "Composing Screen Initial")
    Column {
        println( "Composing Screen")

        ButtonComposable {
            openDialog.value = true
        }
    }

    if (openDialog.value) {
        BasicAlertDialog(
            onDismissRequest = {
                openDialog.value = false
            },
            modifier = Modifier.padding(start = 32.dp, end = 32.dp)
        ) {
            Text(text = "Dialog Title")
        }
    }
}

@Composable
fun ButtonComposable(openDialog: () -> Unit) {

   println("Button Composable")

    Button(
        onClick = {
            openDialog()
        },
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        println("Inside Button Composable")
        Text(text = "Open Dialog")
    }
}

//@Composable
//fun ParentComposable() {
//    val openDialog = remember { mutableStateOf(false) }
//
//    CreateAlertDialog(openDialog.value) {
//        openDialog.value = it
//    }
//}

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun CreateAlertDialog() {
//    var openDialog by remember { mutableStateOf(false) }
//    Column {
//        LogCompositions(tag = "Surface", msg = "Column recomposing")
//            Button(
//        onClick = {
//            openDialog = true
//        },
//        modifier = Modifier
//            .padding(16.dp)
//            .fillMaxWidth()
//    ) {
//        println("Inside Button Composable")
//        Text(text = "Open Dialog")
//    }
//        Text(text = "Middle Text")
//
//        if (openDialog) {
//            BasicAlertDialog(
//                onDismissRequest = { openDialog = false},
//                modifier = Modifier.padding(start = 32.dp, end = 32.dp)
//            ) {
//                Text(text = "Dialog Title")
//            }
//        }
//    }
//}

@Composable
fun MiddleText() {
    Text(text = "Middle Text")
}

//@Composable
//inline fun ButtonComposable(crossinline openDialog: () -> Unit) {
//
//    LogCompositions(tag = "Surface", msg = "Button recomposing")
//
//    Box(
//        modifier = Modifier
//            .padding(16.dp)
//            .fillMaxWidth()
//            .clickable {
//                openDialog()
//            }
//    ) {
//        LogCompositions(tag = "Surface", msg = "Text recomposing")
//        Text(text = "Open Dialog")
//    }
//
//}

@Composable
fun CounterApp() {
    var counter by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Counter Demo Start", fontSize = 24.sp)
        CounterDisplay(counter)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { counter++ }) {
            Text(text = "Increment")
        }
    }
}

@Composable
fun CounterDisplay(count: Int) {
    Text(text = "Counter: $count", fontSize = 24.sp)
    Text(text = "Counter Demo End ", fontSize = 24.sp)
}
