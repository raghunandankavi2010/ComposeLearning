package com.example.composelearning.lists

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FloatSpringSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.composelearning.ui.theme.ComposeLearningTheme
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@Stable
interface CircularRowState {
    val horizontalOffset: Float
    val firstVisibleItem: Int
    val lastVisibleItem: Int
    //val currentIndex: Int

    suspend fun snapTo(value: Float)
    suspend fun decayTo(velocity: Float, value: Float)
    suspend fun stop()
    fun offsetFor(index: Int): IntOffset
    fun setup(config: CircularRowConfig)
    fun alpha(i: Int): Float
    fun scale(i: Int): Float
    fun centerItemIndex(): Int
}

data class CircularRowConfig(
    val contentWidth: Float = 0f,
    val numItems: Int = 0,
    val visibleItems: Int = 0,
    val overshootItems: Int = 0,
    val itemWidth: Int = 0,
)

class CircularRowStateImpl(
   // currIndex: Int = 0,
    currentOffset: Float = 0f,
) : CircularRowState {
    private val animatable = Animatable(currentOffset)
    private var itemWidth = 0f
    private var config = CircularRowConfig()
    private var initialOffset = 0f
    private val decayAnimationSpec = FloatSpringSpec(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow,
    )

   // private var cIndex by mutableStateOf(currIndex)

//    override val currentIndex: Int
//        get() = cIndex
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
        initialOffset = (config.contentWidth - config.itemWidth) / 2f
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
        val percentFromCenter = 1.0f - abs(deltaFromCenter) / maxOffset


        return 0.5f + (percentFromCenter * 0.5f)//1f - (1f - 0.65f) * (deltaFromCenter / maxOffset).absoluteValue
    }

    override fun centerItemIndex(): Int {


        return lastVisibleItem - config.visibleItems
    }


    override fun offsetFor(index: Int): IntOffset {
        val x = (horizontalOffset + initialOffset + (index * (itemWidth)))
        //config.visibleItems - lastVisibleItem
        println( "${  lastVisibleItem - firstVisibleItem - config.visibleItems }")
       // println( "$firstVisibleItem $lastVisibleItem ${Math.abs(x- (config.contentWidth - config.itemWidth) / 2f).toInt()}")
//        if(x == (config.contentWidth - config.itemWidth) / 2f) {
//            cIndex = index
//        }
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
                CircularRowStateImpl()
            }
        )
    }
}

@Composable
fun RowItem(
    modifier: Modifier = Modifier,
    color: Color,
) {
    Box(modifier = Modifier
        .size(55.dp)
        .clip(shape = CircleShape)
        .background(color))
//    Image(
//        painter = painterResource(id = com.example.composelearning.R.drawable.ic_launcher_background),
//        contentDescription = null,
//        modifier = Modifier
//            .size(50.dp)
//            .clip(shape = CircleShape),
//        contentScale = ContentScale.Crop
//    )
}


private fun Modifier.drag(
    state: CircularRowState
) = pointerInput(Unit) {
    val decay = splineBasedDecay<Float>(this)
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
                    if (change.positionChange() != Offset.Zero) change.consume()
                }
            }
            val velocity = tracker.calculateVelocity().x
            val targetValue = decay.calculateTargetValue(state.horizontalOffset, velocity)
            launch {
                state.decayTo(velocity, targetValue)
            }
        }
    }
}

@Composable
fun CircularList(
    itemWidthDp: Dp,
    visibleItems: Int,
    modifier: Modifier = Modifier,
    state: CircularRowState = rememberCircularRowState(),
    overshootItems: Int = 3,
    currentIndex: (Int) -> Unit,
    content: @Composable () -> Unit

) {
    check(visibleItems > 0) { "Visible items must be positive" }
    val itemWidth = with(LocalDensity.current) { itemWidthDp.toPx() }
    currentIndex(state.centerItemIndex())

    Layout(
        modifier = modifier
            .clipToBounds()
            .drag(state),
        content = content,
    ) { measurables, constraints ->
        val itemConstraints =
            Constraints.fixed(width = itemWidth.roundToInt(), height = constraints.maxHeight)
        val placeables = measurables.map { measurable -> measurable.measure(itemConstraints) }


        state.setup(
            CircularRowConfig(
                contentWidth = constraints.maxWidth.toFloat(),
                numItems = placeables.size,
                visibleItems = visibleItems,
                overshootItems = overshootItems,
                itemWidth = itemWidth.toInt()
            )
        )
        layout(
            width = constraints.maxWidth,
            height = constraints.maxHeight,
        ) {

            for (i in state.firstVisibleItem..state.lastVisibleItem) {

                placeables[i].placeRelativeWithLayer(state.offsetFor(i), layerBlock = {
                    alpha = state.alpha(i)
                    scaleX = state.scale(i)
                    scaleY = state.scale(i)
                })
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
fun PreviewCircularList() {
    ComposeLearningTheme {
        Surface {
            CircularList(
                itemWidthDp = 50.dp,
                visibleItems = 5,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(Color.Black),
                currentIndex = {

                }
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