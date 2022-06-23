package com.example.composelearning.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ButtonWithBorder(
    text: String,
    backgroundColor : Color = Color(0xFFF0F0F0),
    borderColor: Color =  Color(0xFF4E617E),
    textColor: Color =  Color(0xFF4E617E),
    cornerRadius: Dp = 5.dp,
    strokeWidth: Dp = 1.dp,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = { onClick() },
        border = BorderStroke(strokeWidth, borderColor),
        shape = RoundedCornerShape(
            topStart = cornerRadius,
            topEnd = cornerRadius,
            bottomStart = cornerRadius,
            bottomEnd = cornerRadius
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            backgroundColor = backgroundColor,
            contentColor = textColor)
    ) {
        Text(text = text)
    }
}