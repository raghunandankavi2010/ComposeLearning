package com.example.composelearning.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
        modifier = Modifier
            .height(40.dp),
        onClick = { onClick() },
        border = BorderStroke(strokeWidth, borderColor),
        shape = RoundedCornerShape(
            topStart = cornerRadius,
            topEnd = cornerRadius,
            bottomStart = cornerRadius,
            bottomEnd = cornerRadius
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = backgroundColor,
            contentColor = textColor)
    ) {
        Text(text = text)
    }
}