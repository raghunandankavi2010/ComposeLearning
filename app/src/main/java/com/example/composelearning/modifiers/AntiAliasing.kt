package com.example.composelearning.modifiers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview
@Composable
fun AntiAliasToggleTest() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        repeat(4) { UiTest(disableAntiAliasing = true) }
    }
}

private val DisableAntiAliasFilter =
    android.graphics.PaintFlagsDrawFilter(
        /* clearBits = */ android.graphics.Paint.ANTI_ALIAS_FLAG,
        /* setBits = */ 0
    )

@Composable
private fun UiTest(disableAntiAliasing: Boolean) {
    Box(
        modifier = Modifier
            .width(200.dp)
            .height(400.dp)
            .run {
                if (disableAntiAliasing) {
                    drawWithContent {
                        val native = drawContext.canvas.nativeCanvas
                        native.drawFilter = DisableAntiAliasFilter
                        drawContent()
                        native.drawFilter = null
                    }
                } else {
                    this
                }
            }
            .background(
                color = Color.Red,
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(24.dp)
                )
        ) {}
    }
}