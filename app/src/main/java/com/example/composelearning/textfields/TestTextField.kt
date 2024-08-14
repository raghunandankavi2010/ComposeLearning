package com.example.composelearning.textfields

import android.os.Build
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
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
//    val ascents = remember { mutableFloatStateOf(0f)  }
//
//    Text(
//        "మీ పొలాన్ని\nఎంచుకోండి",
//        style =  TextStyle(
//            fontSize = 32.sp,
//            lineHeight = 32.sp,
//            fontFamily = displayLargeFontFamily,
//            fontWeight = FontWeight(900),
//            color = Color(0xFF141414),
//        ),
//        onTextLayout = { textLayoutResult ->
//            ascents.floatValue = textLayoutResult.getLineBottom(0) - textLayoutResult.layoutInput.run {
//                with(density) {
//                    style.fontSize.toPx()
//                }
//            }
//        },
//        modifier = Modifier
//            .layout { measurable, constraints ->
//                val placeable = measurable.measure(constraints)
//                val maxAscent = ascents
//                val ascent = ascents
//                val yOffset =  ascent.floatValue.toInt()
//                layout(placeable.width, placeable.height + yOffset) {
//                    placeable.place(0, yOffset)
//                }
//            }
//    )
    Text(
        text =  "మీ పొలాన్ని\nఎంచుకోండి",
        style =  TextStyle(
            fontSize = 32.sp,
            lineHeight = 38.sp,
            fontFamily = displayLargeFontFamily,
            fontWeight = FontWeight(900),
            color = Color(0xFF141414),
        )
    )

}



fun ascenderHeight(ascenderHeight: Float): FontVariation.Setting {
    return FontVariation.Setting("YTAS", ascenderHeight)
}

