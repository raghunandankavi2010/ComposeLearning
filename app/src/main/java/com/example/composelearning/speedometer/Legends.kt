package com.example.composelearning.speedometer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composelearning.R

@Composable
fun Legend(color: Color, legendText: String, alpha: Boolean) {
    val (alphaValue,colorValue) = if (alpha) 0.5f to Color(0xFFB5B5B5) else 1f to Color(0xFF141414)

    Row {
        Box(modifier = Modifier
            .clip(CircleShape)
            .size((13.2).dp)
            .background(color.copy(alpha = alphaValue)))
        Text(
            modifier = Modifier.padding(start = 5.36.dp),
            text = legendText,
            style = TextStyle(
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontFamily = FontFamily(Font(R.font.jio_type_medium)),
                fontWeight = FontWeight(700),
                color = colorValue,
            )
        )
    }
}