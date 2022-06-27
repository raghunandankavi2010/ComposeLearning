package com.example.composelearning.buttons

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun FilledButton(
    text: String,
    backgroundColor: Color = Color(0xFF4E617E),
    textColor: Color = Color(0xFFFFFFFF),
    cornerRadius: Dp = 5.dp,
    onClick: () -> Unit
) {
    Button(
        modifier = Modifier
            .height(40.dp),
        onClick = { onClick() },
        shape = RoundedCornerShape(cornerRadius),
        colors = ButtonDefaults.buttonColors(
            contentColor = textColor,
            backgroundColor = backgroundColor
        )
    ) {
        Text(text = text)
    }
}