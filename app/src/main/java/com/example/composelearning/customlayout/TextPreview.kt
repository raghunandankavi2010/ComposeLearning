package com.example.composelearning.customlayout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

@Preview
@Composable
fun SubComposeLayoutDemo() {
    ResizeWidthColumn(Modifier.fillMaxWidth(), true) {
        Text(
            text = "Looooooooooooooooooong text",
            modifier = Modifier
                .background(Color.Yellow)
                .padding(20.dp)
        )

        Text(
            text = "Short text",
            modifier = Modifier
                .background(Color.Cyan)
                .padding(20.dp)
        )
    }
}

@Composable
fun ResizeWidthColumn(modifier: Modifier, resize: Boolean, mainContent: @Composable () -> Unit) {
    SubcomposeLayout(modifier) { constraints ->
        val mainPlaceables = subcompose(SlotsEnum.Main, mainContent).map {
            // Here we measure the width/height of the child Composables
            it.measure(Constraints())
        }

        //Here we find the max width/height of the child Composables
        val maxSize = mainPlaceables.fold(IntSize.Zero) { currentMax, placeable ->
            IntSize(
                width = maxOf(currentMax.width, placeable.width),
                height = maxOf(currentMax.height, placeable.height)
            )
        }

        val resizedPlaceables: List<Placeable> =
            subcompose(SlotsEnum.Dependent, mainContent).map {
                if (resize) {
                    /** Here we rewrite the child Composables to have the width of
                     * widest Composable
                     */
                    it.measure(
                        Constraints(
                            minWidth = maxSize.width
                        )
                    )
                } else {
                    // Ask the child for its preferred size.
                    it.measure(Constraints())
                }
            }

        /**
         * We can place the Composables on the screen
         * with layout() and the place() functions
         */

        layout(constraints.maxWidth, constraints.maxHeight) {
            resizedPlaceables.forEachIndexed { index, placeable ->
                val widthStart = resizedPlaceables.take(index).sumOf { it.measuredHeight }
                placeable.place(0, widthStart)
            }
        }
    }
}


enum class SlotsEnum {
    Main,
    Dependent
}

@Preview
@Composable
fun EqualWidthTexts() {
    MeasureUnconstrainedViewWidth(
        viewToMeasure = {
            Text(
                text = "Looooooooooooooooooong text",
                modifier = Modifier.padding(20.dp)
            )
        }
    ) { measuredWidth ->
        Column(
            modifier = Modifier
                .background(Color.Gray)
                .padding(8.dp)
        ) {
            Text(
                text = "Looooooooooooooooooong text",
                modifier = Modifier
                    .width(measuredWidth)
                    .background(Color.Yellow)
                    .padding(20.dp)
            )
            Text(
                text = "Short text",
                modifier = Modifier
                    .width(measuredWidth)
                    .background(Color.Cyan)
                    .padding(20.dp)
            )
        }
    }
}

@Composable
fun MeasureUnconstrainedViewWidth(
    viewToMeasure: @Composable () -> Unit,
    content: @Composable (measuredWidth: Dp) -> Unit,
) {
    SubcomposeLayout { constraints ->
        val measuredWidth = subcompose("viewToMeasure", viewToMeasure)[0]
            .measure(Constraints()).width.toDp()

        val contentPlaceable = subcompose("content") {
            content(measuredWidth)
        }[0].measure(constraints)
        layout(contentPlaceable.width, contentPlaceable.height) {
            contentPlaceable.place(0, 0)
        }
    }
}