package com.example.composelearning.textfields

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun ConditionalText(modifier: Modifier = Modifier) {

    val context = LocalContext.current
    val minimumLineLength = 2   //Change this to your desired value
    //Adding States
    var expandedState by remember { mutableStateOf(false) }
    var showReadMoreButtonState by remember { mutableStateOf(false) }
    // ue this to expand and collpase
    //val maxLines = if (expandedState) 200 else minimumLineLength

    Column(modifier = Modifier.padding(24.dp)) {
        Text(
            text = "This is a very long text which goes over two lines. We show a ellipse and also show ReadMore text." +
                    " When user clicks on Read More we take him to a new screen. What do you think of the implementation?",
            style = MaterialTheme.typography.bodySmall,
            overflow = TextOverflow.Ellipsis,   //Make sure to add this line
            maxLines = 2,
            onTextLayout = { textLayoutResult: TextLayoutResult ->
                if (textLayoutResult.lineCount > minimumLineLength - 1) { //Adding this check to avoid ArrayIndexOutOfBounds Exception
                    if (textLayoutResult.isLineEllipsized(minimumLineLength - 1)) showReadMoreButtonState =
                        true
                }
            }
        )
        if (showReadMoreButtonState) {
            Text(
                text = if (expandedState) "Read Less" else "Read More",
                color = Color.Gray,

                modifier = Modifier.clickable {
                    Toast.makeText(context.applicationContext,"Clicked Read More", Toast.LENGTH_SHORT).show()
                   // expandedState = !expandedState
                },

                style = MaterialTheme.typography.bodySmall

            )
        }

    }
}