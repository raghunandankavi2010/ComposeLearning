package com.example.composelearning.lists

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.composelearning.ui.theme.ComposeLearningTheme
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Inspired from https://fvilarino.medium.com/recreating-google-podcasts-speed-selector-in-jetpack-compose-7623203a009d
 */

private val colors = listOf(
    Color.Red,
    Color.Green,
    Color.Blue,
    Color.Magenta,
    Color.Yellow,
    Color.Cyan,
)


@Stable
interface CarouselState {
    val currentValue: Float
    val range: ClosedRange<Int>

    suspend fun snapTo(value: Float)
    suspend fun scrollTo(value: Int)
    suspend fun decayTo(velocity: Float, value: Float)
    suspend fun stop()
}

class CarouselStateImpl(
    currentValue: Float,
    override val range: ClosedRange<Int>,
) : CarouselState {

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

    override suspend fun scrollTo(value: Int) {
        animatable.snapTo(value.toFloat().coerceIn(floatRange))
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

        other as CarouselStateImpl

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
        val Saver = Saver<CarouselStateImpl, List<Any>>(
            save = { listOf(it.currentValue, it.range.start, it.range.endInclusive) },
            restore = {
                CarouselStateImpl(
                    currentValue = it[0] as Float,
                    range = (it[1] as Int)..(it[2] as Int)
                )
            }
        )
    }
}

@Composable
fun rememberCarouselState(
    currentValue: Float = 0f,
    range: ClosedRange<Int> = 0..40,
): CarouselState {
    val state = rememberSaveable(saver = CarouselStateImpl.Saver) {
        CarouselStateImpl(currentValue, range)
    }
    LaunchedEffect(key1 = Unit) {
        state.snapTo(state.currentValue.roundToInt().toFloat())
    }
    return state
}

@Composable
fun InstagramCarousel(
    modifier: Modifier = Modifier,
    state: CarouselState = rememberCarouselState(),
    numSegments: Int = 5,
    circleColor: Color = MaterialTheme.colors.onSurface,
    currentValueLabel: @Composable (Int) -> Unit = { value -> Text(value.toString()) },
    indicatorLabel: @Composable (Int) -> Unit = { value -> Text(value.toString()) },
) {
    val context = LocalContext.current
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        currentValueLabel(state.currentValue.roundToInt())
        Icon(Icons.Filled.ArrowDropDown, contentDescription = null)

        val scope = rememberCoroutineScope()

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
                // alpha
                val deltaFromCenter = (offsetX)
                val percentFromCenter = 1.0f - abs(deltaFromCenter) / maxOffset
                val alpha = 0.25f + (percentFromCenter * 0.75f)
                // scale
                val deltaFromCenterScale = (offsetX)
                val percentFromCenterScale = 1.0f - abs(deltaFromCenterScale) / maxOffset
                val scale = 0.5f + (percentFromCenterScale * 0.5f)

                Column(
                    modifier = Modifier
                        .width(segmentWidth)
                        .graphicsLayer(
                            translationX = offsetX,
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .width(55.dp)
                            .height(55.dp)
                            .graphicsLayer(
                                alpha = alpha,
                                scaleY = scale,
                                scaleX = scale
                            )
                            .clip(CircleShape)
                            .background(colors[i % colors.size])
                            .clickable {
                                scope.launch {
                                    state.scrollTo(i)
                                }
                                Toast
                                    .makeText(context, "$i", Toast.LENGTH_SHORT)
                                    .show()
                            }

                    )
                    indicatorLabel(i)
                }
            }
        }
    }
}

@SuppressLint("ReturnFromAwaitPointerEventScope", "MultipleAwaitPointerEventScopes")
private fun Modifier.drag(
    state: CarouselState,
    numSegments: Int,
) = pointerInput(Unit) {
    val decay = splineBasedDecay<Float>(this)
    val segmentWidthPx = size.width / numSegments
    coroutineScope {
        while (true) {
            val pointerId = awaitPointerEventScope { awaitFirstDown(pass = PointerEventPass.Initial).id }
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
                    if (change.positionChange() != Offset.Zero) change.consume()
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
fun InstagramCarouselPreview() {
    ComposeLearningTheme() {
        Surface(modifier = Modifier.fillMaxWidth()) {
            InstagramCarousel(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                    }
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