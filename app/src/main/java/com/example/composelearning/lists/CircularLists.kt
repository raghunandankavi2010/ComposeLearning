package com.example.composelearning.lists

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FloatSpringSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.painterResource
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

//data class CircularListConfig(
//    val contentHeight: Float = 0f,
//    val numItems: Int = 0,
//    val visibleItems: Int = 0,
//    val circularFraction: Float = 1f,
//    val overshootItems: Int = 0,
//)
//
//@Stable
//interface CircularListState {
//    val verticalOffset: Float
//    val firstVisibleItem: Int
//    val lastVisibleItem: Int
//
//    suspend fun snapTo(value: Float)
//    suspend fun decayTo(velocity: Float, value: Float)
//    suspend fun stop()
//    fun offsetFor(index: Int): IntOffset
//    fun setup(config: CircularListConfig)
//}
//
//class CircularListStateImpl(
//    currentOffset: Float = 0f,
//) : CircularListState {
//
//    private val animatable = Animatable(currentOffset)
//    private var itemHeight = 0f
//    private var config = CircularListConfig()
//    private var initialOffset = 0f
//    private val decayAnimationSpec = FloatSpringSpec(
//        dampingRatio = Spring.DampingRatioLowBouncy,
//        stiffness = Spring.StiffnessLow,
//    )
//
//    private val minOffset: Float
//        get() = -(config.numItems - 1) * itemHeight
//
//    override val verticalOffset: Float
//        get() = animatable.value
//
//    override val firstVisibleItem: Int
//        get() = ((-verticalOffset - initialOffset) / itemHeight).toInt().coerceAtLeast(0)
//
//    override val lastVisibleItem: Int
//        get() = (((-verticalOffset - initialOffset) / itemHeight).toInt() + config.visibleItems)
//            .coerceAtMost(config.numItems - 1)
//
//    override suspend fun snapTo(value: Float) {
//        val minOvershoot = -(config.numItems - 1 + config.overshootItems) * itemHeight
//        val maxOvershoot = config.overshootItems * itemHeight
//        animatable.snapTo(value.coerceIn(minOvershoot, maxOvershoot))
//    }
//
//    override suspend fun decayTo(velocity: Float, value: Float) {
//        val constrainedValue = value.coerceIn(minOffset, 0f).absoluteValue
//        val remainder = (constrainedValue / itemHeight) - (constrainedValue / itemHeight).toInt()
//        val extra = if (remainder <= 0.5f) 0 else 1
//        val target =((constrainedValue / itemHeight).toInt() + extra) * itemHeight
//        animatable.animateTo(
//            targetValue = -target,
//            initialVelocity = velocity,
//            animationSpec = decayAnimationSpec,
//        )
//    }
//
//    override suspend fun stop() {
//        animatable.stop()
//    }
//
//    override fun setup(config: CircularListConfig) {
//        this.config = config
//        itemHeight = config.contentHeight / config.visibleItems
//        initialOffset = (config.contentHeight - itemHeight) / 2f
//    }
//
//    override fun offsetFor(index: Int): IntOffset {
//        val maxOffset = config.contentHeight / 2f + itemHeight / 2f
//        val y = (verticalOffset + initialOffset + index * itemHeight)
//        val deltaFromCenter = (y - initialOffset)
//        val radius = config.contentHeight / 2f
//        val scaledY = deltaFromCenter.absoluteValue * (config.contentHeight / 2f / maxOffset)
//        val x = if (scaledY < radius) {
//            sqrt((radius * radius - scaledY * scaledY))
//        } else {
//            0f
//        }
//        return IntOffset(
//            x = (x * config.circularFraction).roundToInt(),
//            y = y.roundToInt()
//        )
//    }
//
//    override fun equals(other: Any?): Boolean {
//        if (this === other) return true
//        if (javaClass != other?.javaClass) return false
//
//        other as CircularListStateImpl
//
//        if (animatable.value != other.animatable.value) return false
//        if (itemHeight != other.itemHeight) return false
//        if (config != other.config) return false
//        if (initialOffset != other.initialOffset) return false
//        if (decayAnimationSpec != other.decayAnimationSpec) return false
//
//        return true
//    }
//
//    override fun hashCode(): Int {
//        var result = animatable.value.hashCode()
//        result = 31 * result + itemHeight.hashCode()
//        result = 31 * result + config.hashCode()
//        result = 31 * result + initialOffset.hashCode()
//        result = 31 * result + decayAnimationSpec.hashCode()
//        return result
//    }
//
//    companion object {
//        val Saver = Saver<CircularListStateImpl, List<Any>>(
//            save = { listOf(it.verticalOffset) },
//            restore = {
//                CircularListStateImpl(it[0] as Float)
//            }
//        )
//    }
//}
//
//@Composable
//fun rememberCircularListState(): CircularListState {
//    val state = rememberSaveable(saver = CircularListStateImpl.Saver) {
//        CircularListStateImpl()
//    }
//    return state
//}
//
//@Composable
//fun CircularList(
//    visibleItems: Int,
//    modifier: Modifier = Modifier,
//    state: CircularListState = rememberCircularListState(),
//    circularFraction: Float = 1f,
//    overshootItems: Int = 3,
//    content: @Composable () -> Unit,
//) {
//    check(visibleItems > 0) { "Visible items must be positive" }
//    check(circularFraction > 0f) { "Circular fraction must be positive" }
//
//    Layout(
//        modifier = modifier.clipToBounds().drag(state),
//        content = content,
//    ) { measurables, constraints ->
//        val itemHeight = constraints.maxHeight / visibleItems
//        val itemConstraints = Constraints.fixed(width = constraints.maxWidth, height = itemHeight)
//        val placeables = measurables.map { measurable -> measurable.measure(itemConstraints) }
//        state.setup(
//            CircularListConfig(
//                contentHeight = constraints.maxHeight.toFloat(),
//                numItems = placeables.size,
//                visibleItems = visibleItems,
//                circularFraction = circularFraction,
//                overshootItems = overshootItems,
//            )
//        )
//        layout(
//            width = constraints.maxWidth,
//            height = constraints.maxHeight,
//        ) {
//            for (i in state.firstVisibleItem..state.lastVisibleItem) {
//                placeables[i].placeRelative(state.offsetFor(i))
//            }
//        }
//    }
//}
//
//private fun Modifier.drag(
//    state: CircularListState,
//) = pointerInput(Unit) {
//    val decay = splineBasedDecay<Float>(this)
//    coroutineScope {
//        while (true) {
//            val pointerId = awaitPointerEventScope { awaitFirstDown().id }
//            state.stop()
//            val tracker = VelocityTracker()
//            awaitPointerEventScope {
//                verticalDrag(pointerId) { change ->
//                    val verticalDragOffset = state.verticalOffset + change.positionChange().y
//                    launch {
//                        state.snapTo(verticalDragOffset)
//                    }
//                    tracker.addPosition(change.uptimeMillis, change.position)
//                    change.consumePositionChange()
//                }
//            }
//            val velocity = tracker.calculateVelocity().y
//            val targetValue = decay.calculateTargetValue(state.verticalOffset, velocity)
//            launch {
//                state.decayTo(velocity, targetValue)
//            }
//        }
//    }
//}
//
///////////////// Preview
//
//private val colors = listOf(
//    Color.Red,
//    Color.Green,
//    Color.Blue,
//    Color.Magenta,
//    Color.Yellow,
//    Color.Cyan,
//)
//
//@Preview(showBackground = true, widthDp = 420)
//@Composable
//fun PreviewCircularList5() {
//    ComposeLearningTheme {
//        Surface {
//            CircularList(
//                visibleItems = 5,
//                circularFraction = .65f,
//                modifier = Modifier.fillMaxSize(),
//            ) {
//                for (i in 0 until 40) {
//                    ListItem(
//                        text = "Item #$i",
//                        color = colors[i % colors.size],
//                        modifier = Modifier.fillMaxSize()
//                    )
//                }
//            }
//        }
//    }
//}
//
//
//@Composable
//fun ListItem(
//    text: String,
//    color: Color,
//    modifier: Modifier = Modifier,
//) {
//    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
//        Box(
//            modifier = Modifier
//                .size(50.dp)
//                .aspectRatio(1f)
//                .padding(all = 8.dp)
//                .clip(shape = CircleShape)
//                .background(color = color)
//        )
//        Text(
//            text = text,
//            style = MaterialTheme.typography.h5,
//        )
//    }
//}
@Stable
interface CircularRowState {
    val horizontalOffset: Float
    val firstVisibleItem: Int
    val lastVisibleItem: Int
    val scaleX: Float
    val scaleY: Float
    val alphaValue: Float

    suspend fun snapTo(value: Float)
    suspend fun decayTo(velocity: Float, value: Float)
    suspend fun stop()
    fun offsetFor(index: Int): IntOffset
    fun setup(config: CircularRowConfig)
    fun alpha(i: Int): Float
    fun scale(i: Int): Float
}

data class CircularRowConfig(
    val contentWidth: Float = 0f,
    val numItems: Int = 0,
    val visibleItems: Int = 0,
    val overshootItems: Int = 0,
    val itemWidth: Int = 0,
)

class CircularRowStateImpl(
    currentOffset: Float = 0f
) : CircularRowState {
    private val animatable = Animatable(currentOffset)
    private var itemWidth = 0f
    private var config = CircularRowConfig()
    private var initialOffset = 0f
    private val decayAnimationSpec = FloatSpringSpec(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow,
    )
    override val alphaValue: Float
        get() = (1 - (abs(horizontalOffset) / (config.contentWidth / 2))).coerceIn(0f,1f)
    override val scaleX: Float
        get() = horizontalOffset
    override val scaleY: Float
        get() = horizontalOffset
    private val minOffset: Float
        get() = -(config.numItems - 1) * itemWidth
    override val horizontalOffset: Float
        get() = animatable.value
    override val firstVisibleItem: Int
        get() = ((-horizontalOffset - initialOffset) / itemWidth).toInt().coerceAtLeast(0)
    override val lastVisibleItem: Int
        get() = (((-horizontalOffset - initialOffset) / itemWidth).toInt() + config.visibleItems)
            .coerceAtMost(config.numItems - 1)

    override suspend fun snapTo(value: Float) {
        val minOvershoot = -(config.numItems - 1 + config.overshootItems) * itemWidth
        val maxOvershoot = config.overshootItems * itemWidth
        animatable.snapTo(value.coerceIn(minOvershoot, maxOvershoot))
    }

    override suspend fun decayTo(velocity: Float, value: Float) {
        val constrainedValue = value.coerceIn(minOffset, 0f).absoluteValue
        val remainder = (constrainedValue / itemWidth) - (constrainedValue / itemWidth).toInt()
        val extra = if (remainder <= 0.5f) 0 else 1
        val target = ((constrainedValue / itemWidth).toInt() + extra) * itemWidth
        animatable.animateTo(
            targetValue = -target,
            initialVelocity = velocity,
            animationSpec = decayAnimationSpec,
        )
    }

    override suspend fun stop() {
        animatable.stop()
    }

    override fun setup(config: CircularRowConfig) {
        this.config = config
        itemWidth = config.contentWidth / config.visibleItems
        initialOffset = (config.contentWidth) / 2f - (itemWidth / 2f - 25f)
    }

    override fun alpha(i: Int): Float {
        val maxOffset = config.contentWidth / 2f
        val x = (horizontalOffset + initialOffset + i * itemWidth)
        val deltaFromCenter = (x - initialOffset)
        val percentFromCenter = 1.0f - abs(deltaFromCenter) / maxOffset

        return 0.25f + (percentFromCenter * 0.75f)
    }

    override fun scale(i: Int): Float {
        val maxOffset = config.contentWidth / 2f
        val x = (horizontalOffset + initialOffset + i * itemWidth)
        val deltaFromCenter = (x - initialOffset)
        val percentFromCenter = 1.0f - abs(deltaFromCenter )/ maxOffset

        return 0.5f + (percentFromCenter * 0.5f)//1f - (1f - 0.65f) * (deltaFromCenter / maxOffset).absoluteValue
    }


    override fun offsetFor(index: Int): IntOffset {

        val x = (horizontalOffset + initialOffset + (index * (itemWidth)))
        val y = 0
        return IntOffset(
            x = x.roundToInt(),
            y = y
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CircularRowStateImpl

        if (animatable.value != other.animatable.value) return false
        if (itemWidth != other.itemWidth) return false
        if (config != other.config) return false
        if (initialOffset != other.initialOffset) return false
        if (decayAnimationSpec != other.decayAnimationSpec) return false

        return true
    }

    override fun hashCode(): Int {
        var result = animatable.value.hashCode()
        result = 31 * result + itemWidth.hashCode()
        result = 31 * result + config.hashCode()
        result = 31 * result + initialOffset.hashCode()
        result = 31 * result + decayAnimationSpec.hashCode()
        return result
    }

    companion object {
        val Saver = Saver<CircularRowStateImpl, List<Any>>(
            save = { listOf(it.horizontalOffset) },
            restore = {
                CircularRowStateImpl(it[0] as Float)
            }
        )
    }
}

@Composable
fun RowItem(
    color: Color,
) {

    Image(
        painter = painterResource(id = com.example.composelearning.R.drawable.ic_launcher_background),
        contentDescription = null,
        modifier = Modifier
            .size(50.dp)
            .clip(shape = CircleShape),
        contentScale = ContentScale.Crop
    )
//    Box(
//        modifier =
//        Modifier
//            .size(50.dp)
//            .clip(shape = CircleShape)
//            .background(color = color)
//    )
}


private fun Modifier.drag(
    state: CircularRowState,
) = pointerInput(Unit) {
    val decay = splineBasedDecay<Float>(this)
    val itemWidthPx = size.width / 5
    coroutineScope {
        while (true) {
            val pointerId = awaitPointerEventScope { awaitFirstDown().id }
            state.stop()
            val tracker = VelocityTracker()
            awaitPointerEventScope {
                horizontalDrag(pointerId) { change ->
                    val horizontalDragOffset = state.horizontalOffset + change.positionChange().x
                    launch {
                        state.snapTo(horizontalDragOffset)
                    }
                    tracker.addPosition(change.uptimeMillis, change.position)
                    change.consumePositionChange()
                }
            }
            val velocity = tracker.calculateVelocity().y
            val targetValue = decay.calculateTargetValue(state.horizontalOffset, velocity)
            launch {
                state.decayTo(velocity, targetValue)
            }
        }
    }
}

@Composable
fun CircularList(
    visibleItems: Int,
    modifier: Modifier = Modifier,
    state: CircularRowState = rememberCircularRowState(),
    overshootItems: Int = 3,
    content: @Composable () -> Unit,
) {
    check(visibleItems > 0) { "Visible items must be positive" }

    Layout(
        modifier = modifier
            .clipToBounds()
            .drag(state),
        content = content,
    ) { measurables, constraints ->
        val itemWidth = constraints.maxWidth / visibleItems
        val itemConstraints =
            Constraints.fixed(width = 50.dp.toPx().toInt(), height = constraints.maxHeight)
        val placeables = measurables.map { measurable -> measurable.measure(itemConstraints) }
        state.setup(
            CircularRowConfig(
                contentWidth = constraints.maxWidth.toFloat(),
                numItems = placeables.size,
                visibleItems = visibleItems,
                overshootItems = overshootItems,
                itemWidth = 50.dp.toPx().toInt()
            )
        )
        layout(
            width = constraints.maxWidth,
            height = constraints.maxHeight,
        ) {
            for (i in state.firstVisibleItem..state.lastVisibleItem) {
                placeables[i].placeRelativeWithLayer(state.offsetFor(i), layerBlock = {
                    val centerIndex =  state.firstVisibleItem + (state.lastVisibleItem - state.firstVisibleItem ) / 2
                   // alpha = state.alpha(i,centerIndex)
                    println("index = $i")
                    alpha =  state.alpha(i)
                    scaleX = state.scale(i)
                    scaleY = state.scale(i)

                } )
            }
        }
    }
}

@Composable
fun rememberCircularRowState(): CircularRowState {
    val state = rememberSaveable(saver = CircularRowStateImpl.Saver) {
        CircularRowStateImpl()
    }
    return state
}

private val colors = listOf(
    Color.Red,
    Color.Green,
    Color.Blue,
    Color.Magenta,
    Color.Yellow,
    Color.Cyan,
)

@Preview(showBackground = true)
@Composable
fun PreviewCircularList5() {
    ComposeLearningTheme {
        Surface {
            CircularList(
                visibleItems = 5,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(Color.Black),
            ) {
                for (i in 0 until 40) {
                    RowItem(
                        color = colors[i % colors.size],
                    )
                }
            }
        }
    }
}