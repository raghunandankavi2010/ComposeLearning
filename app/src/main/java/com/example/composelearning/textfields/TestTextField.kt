package com.example.composelearning.textfields

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.composelearning.ui.theme.AppFontFamilyBlack


@Preview(widthDp = 300, showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun AmountTextField(
) {
    Text(
        text = "మీ పొలాన్ని\nఎంచుకోండి",
        style = TextStyle(
            fontSize = 32.sp,
            lineHeight = 38.sp,
            fontFamily = AppFontFamilyBlack,
            fontWeight = FontWeight(900),
            color = Color(0xFF141414),
        )
    )
}
