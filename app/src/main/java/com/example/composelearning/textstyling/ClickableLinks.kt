package com.example.composelearning.textstyling

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink

@Composable
fun LinkAnnotationExample() {
    Text(buildAnnotatedString {
        append("Some other text ")
        withLink(
            LinkAnnotation.Url(
                // Added the sample Url here.
                url = "https://developer.android.com/jetpack/compose",
                styles = TextLinkStyles(
                    style = SpanStyle(color = Color.Blue),
                    hoveredStyle = SpanStyle(
                        color = Color.Red,
                        textDecoration = TextDecoration.Underline
                    ),
                    focusedStyle = SpanStyle(
                        color = Color.Red,
                        textDecoration = TextDecoration.LineThrough
                    ),
                    pressedStyle = SpanStyle(
                        color = Color.Green,
                        textDecoration = TextDecoration.LineThrough
                    ),
                )
            )
        ) {
            append("Jetpack Compose")
        }
    })
}