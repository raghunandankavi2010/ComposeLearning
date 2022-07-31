package com.example.composelearning.lists

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FloatSpringSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.composelearning.ui.theme.ComposeLearningTheme
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

private val BarWidth = 2.dp
private val BarHeight = 24.dp
private const val MinAlpha = .25f

@Stable
interface PodcastSliderState {
    val currentValue: Float
    val range: ClosedRange<Int>

    suspend fun snapTo(value: Float)
    suspend fun decayTo(velocity: Float, value: Float)
    suspend fun stop()
}

class PodcastSliderStateImpl(
    currentValue: Float,
    override val range: ClosedRange<Int>,
) : PodcastSliderState {

    private val floatRange = range.start.toFloat()..range.endInclusive.toFloat()
    private val animatable = Animatable(currentValue)
    private val decayAnimationSpec = FloatSpringSpec(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow,
    )

    override val currentValue: Float
        get() = animatable.value

    override suspend fun stop() {
        animatable.stop()
    }

    override suspend fun snapTo(value: Float) {
        animatable.snapTo(value.coerceIn(floatRange))
    }

    override suspend fun decayTo(velocity: Float, value: Float) {
        val target = value.roundToInt().coerceIn(range).toFloat()
        animatable.animateTo(
            targetValue = target,
            initialVelocity = velocity,
            animationSpec = decayAnimationSpec,
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PodcastSliderStateImpl

        if (range != other.range) return false
        if (floatRange != other.floatRange) return false
        if (animatable != other.animatable) return false
        if (decayAnimationSpec != other.decayAnimationSpec) return false

        return true
    }

    override fun hashCode(): Int {
        var result = range.hashCode()
        result = 31 * result + floatRange.hashCode()
        result = 31 * result + animatable.hashCode()
        result = 31 * result + decayAnimationSpec.hashCode()
        return result
    }

    companion object {
        val Saver = Saver<PodcastSliderStateImpl, List<Any>>(
            save = { listOf(it.currentValue, it.range.start, it.range.endInclusive) },
            restore = {
                PodcastSliderStateImpl(
                    currentValue = it[0] as Float,
                    range = (it[1] as Int)..(it[2] as Int)
                )
            }
        )
    }
}

@Composable
fun rememberPodcastSliderState(
    currentValue: Float = 12f,
    range: ClosedRange<Int> = 5..30,
): PodcastSliderState {
    val state = rememberSaveable(saver = PodcastSliderStateImpl.Saver) {
        PodcastSliderStateImpl(currentValue, range)
    }
    LaunchedEffect(key1 = Unit) {
        state.snapTo(state.currentValue.roundToInt().toFloat())
    }
    return state
}

@Composable
fun PodcastSlider(
    modifier: Modifier = Modifier,
    state: PodcastSliderState = rememberPodcastSliderState(),
    numSegments: Int = 12,
    barColor: Color = MaterialTheme.colors.onSurface,
    currentValueLabel: @Composable (Int) -> Unit = { value -> Text(value.toString()) },
    indicatorLabel: @Composable (Int) -> Unit = { value -> Text(value.toString()) },
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        currentValueLabel(state.currentValue.roundToInt())
        Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .drag(state, numSegments),
            contentAlignment = Alignment.TopCenter,
        ) {
            val segmentWidth = maxWidth / numSegments
            val segmentWidthPx = constraints.maxWidth.toFloat() / numSegments.toFloat()
            val halfSegments = (numSegments + 1) / 2
            val start = (state.currentValue - halfSegments).toInt()
                .coerceAtLeast(state.range.start)
            val end = (state.currentValue + halfSegments).toInt()
                .coerceAtMost(state.range.endInclusive)

            val maxOffset = constraints.maxWidth / 2f
            for (i in start..end) {
                val offsetX = (i - state.currentValue) * segmentWidthPx
                // indicator at center is at 1f, indicators at edges are at 0.25f
                val alpha = 1f - (1f - MinAlpha) * (offsetX / maxOffset).absoluteValue
                Column(
                    modifier = Modifier
                        .width(segmentWidth)
                        .graphicsLayer(
                            alpha = alpha,
                            translationX = offsetX
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .width(BarWidth)
                            .height(BarHeight)
                            .background(barColor)
                    )
                    indicatorLabel(i)
                }
            }
        }
    }
}

private fun Modifier.drag(
    state: PodcastSliderState,
    numSegments: Int,
) = pointerInput(Unit) {
    val decay = splineBasedDecay<Float>(this)
    val segmentWidthPx = size.width / numSegments
    coroutineScope {
        while (true) {
            val pointerId = awaitPointerEventScope { awaitFirstDown().id }
            state.stop()
            val tracker = VelocityTracker()
            awaitPointerEventScope {
                horizontalDrag(pointerId) { change ->
                    val horizontalDragOffset =
                        state.currentValue - change.positionChange().x / segmentWidthPx
                    launch {
                        state.snapTo(horizontalDragOffset)
                    }
                    tracker.addPosition(change.uptimeMillis, change.position)
                    change.consumePositionChange()
                }
            }
            val velocity = tracker.calculateVelocity().x / numSegments
            val targetValue = decay.calculateTargetValue(state.currentValue, -velocity)
            launch {
                state.decayTo(velocity, targetValue)
            }
        }
    }
}


@Preview(widthDp = 420)
@Composable
fun PodcastSliderPreview() {
    ComposeLearningTheme() {
        Surface(modifier = Modifier.fillMaxWidth()) {
            PodcastSlider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                currentValueLabel = { value ->
                    Text(
                        text = "${(value / 10)}.${(value % 10)}x",
                        style = MaterialTheme.typography.h6
                    )
                },
                indicatorLabel = { value ->
                    if (value % 5 == 0) {
                        Text(
                            text = "${(value / 10)}.${(value % 10)}",
                            style = MaterialTheme.typography.body2,
                        )
                    }
                }
            )

        }
    }
}