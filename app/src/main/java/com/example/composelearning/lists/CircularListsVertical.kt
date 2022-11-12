package com.example.composelearning.lists


import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FloatSpringSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.verticalDrag
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.composelearning.ui.theme.ComposeLearningTheme
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.math.sqrt

data class CircularListConfig(
    val contentHeight: Float = 0f,
    val numItems: Int = 0,
    val visibleItems: Int = 0,
    val circularFraction: Float = 1f,
    val overshootItems: Int = 0,
    val itemHeight: Int = 0
)

@Stable
interface CircularListState {
    val verticalOffset: Float
    val firstVisibleItem: Int
    val lastVisibleItem: Int

    suspend fun snapTo(value: Float)
    suspend fun decayTo(velocity: Float, value: Float)
    suspend fun stop()
    fun offsetFor(index: Int): IntOffset
    fun alpha(i: Int): Float
    fun scale(i: Int): Float
    fun setup(config: CircularListConfig)
}

class CircularListStateImpl(
    currentOffset: Float = 0f,
) : CircularListState {
    private val animatable = Animatable(currentOffset)
    private var itemHeight = 0f
    private var config = CircularListConfig()
    private var initialOffset = 0f
    private val decayAnimationSpec = FloatSpringSpec(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow,
    )
    private val minOffset: Float
        get() = -(config.numItems - 1) * itemHeight
    override val verticalOffset: Float
        get() = animatable.value
    override val firstVisibleItem: Int
        get() = ((-verticalOffset - initialOffset) / itemHeight).toInt().coerceAtLeast(0)
    override val lastVisibleItem: Int
        get() = (((-verticalOffset - initialOffset) / itemHeight).toInt() + config.visibleItems)
            .coerceAtMost(config.numItems - 1)

    override suspend fun snapTo(value: Float) {
        val minOvershoot = -(config.numItems - 1 + config.overshootItems) * itemHeight
        val maxOvershoot = config.overshootItems * itemHeight
        animatable.snapTo(value.coerceIn(minOvershoot, maxOvershoot))
        Log.d("CircularListVertical","$firstVisibleItem $lastVisibleItem")
    }

    override fun alpha(i: Int): Float {
        val maxOffset = config.contentHeight / 2f
        val y = (verticalOffset + initialOffset + i * itemHeight)
        val deltaFromCenter = abs(y - initialOffset)

        return 1.0f - abs(deltaFromCenter) / maxOffset
    }

    override fun scale(i: Int): Float {
        val maxOffset = config.contentHeight / 2f
        val y = (verticalOffset + initialOffset + i * itemHeight)
        val deltaFromCenter = (y - initialOffset)
        val percentFromCenter = 1.0f -abs(deltaFromCenter) / maxOffset
       return .5f + (percentFromCenter * 0.5f)
    }

    override suspend fun decayTo(velocity: Float, value: Float) {
        val constrainedValue = value.coerceIn(minOffset, 0f).absoluteValue
        val remainder = (constrainedValue / itemHeight) - (constrainedValue / itemHeight).toInt()
        val extra = if (remainder <= 0.5f) 0 else 1
        val target = ((constrainedValue / itemHeight).toInt() + extra) * itemHeight
        animatable.animateTo(
            targetValue = -target,
            initialVelocity = velocity,
            animationSpec = decayAnimationSpec,
        )
    }

    override suspend fun stop() {
        animatable.stop()
    }

    override fun setup(config: CircularListConfig) {
        this.config = config
        itemHeight = config.contentHeight / config.visibleItems
        initialOffset = (config.contentHeight - config.itemHeight) / 2f
    }

    override fun offsetFor(index: Int): IntOffset {

        val maxOffset = config.contentHeight / 2f + itemHeight / 2f
        val y = (verticalOffset + initialOffset + index * itemHeight)
        val deltaFromCenter = (y - initialOffset)
        val radius = config.contentHeight / 2f
        val scaledY = deltaFromCenter.absoluteValue * (config.contentHeight / 2f / maxOffset)
        val x = if (scaledY < radius) {
            sqrt((radius * radius - scaledY * scaledY))
        } else {
            0f
        }
        return IntOffset(
            x = (x * config.circularFraction).roundToInt(),
            y = y.roundToInt()
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CircularListStateImpl

        if (animatable.value != other.animatable.value) return false
        if (itemHeight != other.itemHeight) return false
        if (config != other.config) return false
        if (initialOffset != other.initialOffset) return false
        if (decayAnimationSpec != other.decayAnimationSpec) return false

        return true
    }

    override fun hashCode(): Int {
        var result = animatable.value.hashCode()
        result = 31 * result + itemHeight.hashCode()
        result = 31 * result + config.hashCode()
        result = 31 * result + initialOffset.hashCode()
        result = 31 * result + decayAnimationSpec.hashCode()
        return result
    }

    companion object {
        val Saver = Saver<CircularListStateImpl, List<Any>>(
            save = { listOf(it.verticalOffset) },
            restore = {
                CircularListStateImpl(it[0] as Float)
            }
        )
    }
}

@Composable
fun rememberCircularListState(): CircularListState {
    val state = rememberSaveable(saver = CircularListStateImpl.Saver) {
        CircularListStateImpl()
    }
    return state
}

@Composable
fun CircularListVertical(
    visibleItems: Int,
    modifier: Modifier = Modifier,
    state: CircularListState = rememberCircularListState(),
    circularFraction: Float = 1f,
    overshootItems: Int = 3,
    content: @Composable () -> Unit,
) {
    check(visibleItems > 0) { "Visible items must be positive" }
    check(circularFraction > 0f) { "Circular fraction must be positive" }

    Layout(
        modifier = modifier
            .clipToBounds()
            .drag(state).graphicsLayer {

            },
        content = content,
    ) { measurables, constraints ->
        val itemHeight = constraints.maxHeight / visibleItems
        val itemConstraints = Constraints.fixed(width = constraints.maxWidth, height = itemHeight)
        val placeables = measurables.map { measurable -> measurable.measure(itemConstraints) }

        state.setup(
            CircularListConfig(
                contentHeight = constraints.maxHeight.toFloat(),
                numItems = placeables.size,
                visibleItems = visibleItems,
                circularFraction = circularFraction,
                overshootItems = overshootItems,
                itemHeight = 50.dp.toPx().toInt()
            )
        )
        layout(
            width = constraints.maxWidth,
            height = constraints.maxHeight,
        ) {
            for (i in state.firstVisibleItem..state.lastVisibleItem) {
                placeables[i].placeRelativeWithLayer(state.offsetFor(i), layerBlock = {
                    alpha = state.alpha(i)
                   // scaleX = state.scale(i)
                   // scaleY = state.scale(i)


                })
            }
        }
    }
}

private fun Modifier.drag(
    state: CircularListState,
) = pointerInput(Unit) {
    val decay = splineBasedDecay<Float>(this)
    coroutineScope {
        while (true) {
            val pointerId = awaitPointerEventScope { awaitFirstDown().id }
            state.stop()
            val tracker = VelocityTracker()
            awaitPointerEventScope {
                verticalDrag(pointerId) { change ->
                    val verticalDragOffset = state.verticalOffset + change.positionChange().y
                    launch {
                        state.snapTo(verticalDragOffset)
                    }
                    tracker.addPosition(change.uptimeMillis, change.position)
                    change.consumePositionChange()
                }
            }
            val velocity = tracker.calculateVelocity().y
            val targetValue = decay.calculateTargetValue(state.verticalOffset, velocity)
            launch {
                state.decayTo(velocity, targetValue)
            }
        }
    }
}
/////////////// Preview
private val colors = listOf(
    Color.Red,
    Color.Green,
    Color.Blue,
    Color.Magenta,
    Color.Yellow,
    Color.Cyan,
)

@Preview(showBackground = true, widthDp = 420)
@Composable
fun PreviewCircularListVertical() {
    ComposeLearningTheme {
        Surface {
            CircularListVertical(
                visibleItems = 5,
                circularFraction = .65f,
                modifier = Modifier.fillMaxSize(),
            ) {
                for (i in 0 until 40) {
                    ListItem(
                        text = "Item #$i",
                        color = colors[i % colors.size],
                        modifier = Modifier.size(50.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ListItem(
    modifier: Modifier = Modifier,
    text: String = "",
    color: Color

) {
    Row(modifier = modifier.width(50.dp)) {
       Box(

            modifier = Modifier
                .size(50.dp)
                .clip(shape = CircleShape)
                .background(color = color),
        )
//        Text(modifier = Modifier.wrapContentSize(Alignment.TopStart, false),
//            text = text,
//            style = MaterialTheme.typography.h5,
//        )
    }
}