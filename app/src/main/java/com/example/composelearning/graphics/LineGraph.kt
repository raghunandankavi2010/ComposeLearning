import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ContinuousLineGraph() {
    val x = (0..10).map { it.toFloat() }
    val y = x.map { kotlin.math.sin(it) }

    LazyRow {
        itemsIndexed(y) { index, item ->
            if (index < y.size - 1) {
                Canvas(modifier = Modifier.width(50.dp).height(200.dp)) {
                    drawLine(
                        color = Color.Blue,
                        start = Offset(0f, 100f - item * 50),
                        end = Offset(50f, 100f - y[index + 1] * 50),
                        strokeWidth = 10f
                    )
                }
            }
        }
    }
}
