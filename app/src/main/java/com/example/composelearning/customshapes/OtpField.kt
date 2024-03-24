package com.example.composelearning.customshapes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
fun OTPTextField(
    value: String,
    length: Int,
    modifier: Modifier = Modifier,
    boxWidth: Dp = 72.dp,
    boxHeight: Dp = 50.dp,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default.copy(
        keyboardType = KeyboardType.Number
    ),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    onValueChange: (String) -> Unit,
) {
    val spaceBetweenBoxes = 8.dp
    val gapBetweenLineAndText = 8.dp

    BasicTextField(
        modifier = modifier
            .fillMaxWidth(),
        value = value,
        singleLine = true,
        onValueChange = {
            val newValue = it.take(length)
            if (value != newValue) {
                onValueChange(newValue)
            }
        },
        enabled = enabled,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        decorationBox = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(boxHeight),
                horizontalArrangement = Arrangement.Center,
            ) {
                repeat(length) { index ->
                    val color = if (index == value.length) Color.Black else Color.Gray
                    Box(
                        modifier = Modifier
                            .width(boxWidth)
                            .height(boxHeight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = value.getOrNull(index)?.toString() ?: "",
                            textAlign = TextAlign.Center,

                            color = Color.Black
                        )

                        Box(
                            Modifier
                                .align(Alignment.BottomCenter)
                                .padding(horizontal = 8.dp)
                                .height(2.dp)
                                .fillMaxWidth()
                                .background(color)
                        )
                    }
                }
            }
        }
    )
}

