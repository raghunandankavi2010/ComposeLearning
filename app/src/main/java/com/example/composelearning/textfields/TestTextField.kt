package com.example.composelearning.textfields

import android.os.Build
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composelearning.R

@Preview(widthDp = 300, showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun AmountTextField(
) {
// In Typography.kt
val default  = FontFamily(Font(R.font.jio_type_black))

// [START android_compose_variable_font_custom_axis_usage]
@OptIn(ExperimentalTextApi::class)
val displayLargeFontFamily = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    FontFamily(
        Font(
            R.font.jio_type_black,
            variationSettings = FontVariation.Settings(
                ascenderHeight(200f),
            )
        )
    )
} else {
    default
}
    Text(
        text =  "Hello World",//"మీ పొలాన్ని\nఎంచుకోండి",
        style =  TextStyle(
            fontSize = 32.sp,
            lineHeight = 32.sp,
            fontFamily = displayLargeFontFamily,
            fontWeight = FontWeight(900),
            color = Color(0xFF141414),
        )
    )

}



fun ascenderHeight(ascenderHeight: Float): FontVariation.Setting {
    return FontVariation.Setting("YTAS", ascenderHeight)
}

