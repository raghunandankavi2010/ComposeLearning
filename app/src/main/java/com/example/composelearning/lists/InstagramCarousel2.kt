package com.example.composelearning.lists

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FloatSpringSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.composelearning.ui.theme.ComposeLearningTheme
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Inspired from https://fvilarino.medium.com/recreating-google-podcasts-speed-selector-in-jetpack-compose-7623203a009d
 * https://docs.flutter.dev/cookbook/effects/photo-filter-carousel
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
interface CarouselState2 {
    val currentValue: Float
    val range: ClosedRange<Int>

    suspend fun snapTo(value: Float)
    suspend fun scrollTo(value: Int)
    suspend fun decayTo(velocity: Float, value: Float)
    suspend fun stop()

    // New callback for selection
    var onSelectionFinished: ((Int) -> Unit)?
}

class CarouselStateImpl2(
    currentValue: Float,
    override val range: ClosedRange<Int>,
    override var onSelectionFinished: ((Int) -> Unit)?, // Initialize the callback
) : CarouselState2 {
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
        // If snapping to a value, consider it a selection if it's a whole number
        if (value.roundToInt().toFloat() == value) {
            onSelectionFinished?.invoke(value.roundToInt())
        }
    }

    override suspend fun scrollTo(value: Int) {
        animatable.animateTo(
            targetValue = value.toFloat().coerceIn(floatRange),
            animationSpec = decayAnimationSpec // Use spring for programmatic scrolls too
        )
        onSelectionFinished?.invoke(value)
    }

    override suspend fun decayTo(velocity: Float, value: Float) {
        val target = value.roundToInt().coerceIn(range).toFloat()
        animatable.animateTo(
            targetValue = target,
            initialVelocity = velocity,
            animationSpec = decayAnimationSpec,
        )
        // Trigger the callback after the animation finishes
        onSelectionFinished?.invoke(target.roundToInt())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CarouselStateImpl2

        if (range != other.range) return false
        if (floatRange != other.floatRange) return false
        if (animatable != other.animatable) return false
        if (decayAnimationSpec != other.decayAnimationSpec) return false
        if (onSelectionFinished != other.onSelectionFinished) return false // Include callback in equals

        return true
    }

    override fun hashCode(): Int {
        var result = range.hashCode()
        result = 31 * result + floatRange.hashCode()
        result = 31 * result + animatable.hashCode()
        result = 31 * result + decayAnimationSpec.hashCode()
        result = 31 * result + (onSelectionFinished?.hashCode() ?: 0) // Include callback in hashCode
        return result
    }

    companion object {
        val Saver = Saver<CarouselStateImpl2, List<Any>>(
            save = { listOf(it.currentValue, it.range.start, it.range.endInclusive) },
            restore = {
                CarouselStateImpl2(
                    currentValue = it[0] as Float,
                    range = (it[1] as Int)..(it[2] as Int),
                    onSelectionFinished = null // Callback cannot be saved, needs to be re-set
                )
            }
        )
    }
}

@Composable
fun rememberCarouselState2(
    currentValue: Float = 0f,
    range: ClosedRange<Int> = 0..40,
    onSelectionFinished: ((Int) -> Unit)? = null, // Add callback parameter
): CarouselState2 {
    val state = rememberSaveable(saver = CarouselStateImpl2.Saver) {
        CarouselStateImpl2(currentValue, range, onSelectionFinished)
    }
    LaunchedEffect(key1 = Unit) {
        state.snapTo(state.currentValue.roundToInt().toFloat())
    }
    // Update the callback if it changes
    LaunchedEffect(onSelectionFinished) {
        state.onSelectionFinished = onSelectionFinished
    }
    return state
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun InstagramCarousel2(
    modifier: Modifier = Modifier,
    state: CarouselState2 = rememberCarouselState2(),
    numSegments: Int = 5,
    circleColor: Color = MaterialTheme.colorScheme.onSurface, // This parameter is unused, can be removed.
    currentValueLabel: @Composable (Int) -> Unit = { value -> Text(value.toString()) },
) {
    val context = LocalContext.current
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        currentValueLabel(state.currentValue.roundToInt())
        //Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
        val scope = rememberCoroutineScope()

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .drag(state, numSegments),
            contentAlignment = Alignment.Center,
        ) {
            CenterCircle2(
                modifier = Modifier.align(Alignment.Center),
                fillColor = Color(android.graphics.Color.parseColor("#4DB6AC")),
                strokeWidth = 5.dp,
            )
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
                        .wrapContentHeight(Alignment.CenterVertically)
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
                }
            }
        }
    }
}

// CenterCircle composable from your original code (assuming it exists or is simple)
@Composable
fun CenterCircle2(
    modifier: Modifier = Modifier,
    fillColor: Color,
    strokeWidth: Dp,
) {
    Box(
        modifier = modifier
            .size(70.dp) // Example size, adjust as needed
            .background(fillColor, CircleShape)
        // If you want a stroke, you'd typically draw it with Canvas or Border
        // .border(strokeWidth, MaterialTheme.colorScheme.onSurface, CircleShape)
    )
}

@SuppressLint("ReturnFromAwaitPointerEventScope", "MultipleAwaitPointerEventScopes")
private fun Modifier.drag(
    state: CarouselState2,
    numSegments: Int,
) = pointerInput(Unit) {
    val decay = splineBasedDecay<Float>(this)
    val segmentWidthPx = size.width / numSegments
    coroutineScope {
        while (true) {
            val pointerId =
                awaitPointerEventScope {
                    awaitFirstDown(true).id
                }
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
fun InstagramCarouselPreview2() {
    ComposeLearningTheme {
        Surface(modifier = Modifier.fillMaxWidth()) {
            // Demonstrate usage of the new callback
            val context = LocalContext.current
            val carouselState = rememberCarouselState2(
                onSelectionFinished = { selectedIndex ->
                    Toast.makeText(context, "Selected index: $selectedIndex", Toast.LENGTH_SHORT).show()
                }
            )
            InstagramCarousel2(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                state = carouselState,
                currentValueLabel = { value ->
                    Text(
                        text = "${(value / 10)}.${(value % 10)}x",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            )
        }
    }
}