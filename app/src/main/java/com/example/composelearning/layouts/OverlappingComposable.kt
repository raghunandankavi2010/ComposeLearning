package com.example.composelearning.layouts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun OverlapLayout(
    modifier: Modifier = Modifier,
    overlapPercentage: Float = 0.5f,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        require(measurables.size == 2) { "OverlapLayout requires exactly 2 children" }

        val placeables = measurables.map { it.measure(constraints) }
        val firstPlaceable = placeables[0]
        val secondPlaceable = placeables[1]

        val overlapOffset = (firstPlaceable.width * overlapPercentage).toInt()

        layout(firstPlaceable.width, firstPlaceable.height) {
            firstPlaceable.placeRelative(0, 0)
            secondPlaceable.placeRelative(overlapOffset, 0)
        }
    }
}

@Composable
fun OverlappingComposables() {
    OverlapLayout(
        overlapPercentage = 0.3f,
        modifier = Modifier.size(200.dp)
    ) {
        Box(modifier = Modifier.size(100.dp).background(MaterialTheme.colorScheme.primary))
        Box(modifier = Modifier.size(100.dp).background(MaterialTheme.colorScheme.secondary))
    }
}

@Preview(showBackground = true)
@Composable
fun OverlappingComposablesPreview() {
    OverlappingComposables()

}
