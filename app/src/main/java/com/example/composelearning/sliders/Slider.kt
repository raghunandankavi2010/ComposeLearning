package com.example.composelearning.sliders

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.example.composelearning.R
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Sliders allow users to make selections from a range of values.
 *
 * Sliders reflect a range of values along a bar, from which users may select a single value.
 * They are ideal for adjusting settings such as volume, brightness, or applying image filters.
 *
 * Use continuous sliders allow users to make meaningful selections that don’t
 * require a specific value:
 *
 * You can allow the user to choose only between predefined set of values by specifying the amount
 * of steps between min and max values:
 *
 * @param value current value of the Slider. If outside of [valueRange] provided, value will be
 * coerced to this range.
 * @param onValueChange lambda in which value should be updated
 * @param modifier modifiers for the Slider layout
 * @param enabled whether or not component is enabled and can we interacted with or not
 * @param valueRange range of values that Slider value can take. Passed [value] will be coerced to
 * this range
 * @param steps if greater than 0, specifies the amounts of discrete values, evenly distributed
 * between across the whole value range. If 0, slider will behave as a continuous slider and allow
 * to choose any value from the range specified. Must not be negative.
 * @param onValueChangeFinished lambda to be invoked when value change has ended. This callback
 * shouldn't be used to update the slider value (use [onValueChange] for that), but rather to
 * know when the user has completed selecting a new value by ending a drag or a click.
 * @param interactionSource the [MutableInteractionSource] representing the stream of
 * [Interaction]s for this Slider. You can create and pass in your own remembered
 * [MutableInteractionSource] if you want to observe [Interaction]s and customize the
 * appearance / behavior of this Slider in different [Interaction]s.
 * @param colors [SliderColors] that will be used to determine the color of the Slider parts in
 * different state. See [SliderDefaults.colors] to customize.
 */
@Composable
fun Slider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    /*@IntRange(from = 0)*/
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    colors: SliderColors = SliderDefaults.colors()
) {
    val scope = rememberCoroutineScope()
    val position = remember(valueRange, steps, scope) {
        SliderPosition(value, valueRange, steps, scope, onValueChange)
    }
    position.onValueChange = onValueChange
    position.scaledValue = value
    BoxWithConstraints(
        modifier.sliderSemantics(value, position, enabled, onValueChange, valueRange, steps)
    ) {
        val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
        val maxPx = constraints.maxWidth.toFloat()
        val minPx = 0f
        position.setBounds(minPx, maxPx)
        val gestureEndAction: (Float) -> Unit = { velocity: Float ->
            if (position.anchorsPx.isNotEmpty()) {
                val now = position.holder.value
                val point = position.anchorsPx.minByOrNull { abs(it - now) }
                val target = point ?: now
                scope.launch {
                    position.holder.animateTo(target, SliderToTickAnimation, velocity) {
                        position.onHolderValueUpdated(this.value)
                    }
                    onValueChangeFinished?.invoke()
                }
            } else {
                onValueChangeFinished?.invoke()
            }
        }
        val press = if (enabled) {
            Modifier.pointerInput(position, interactionSource, maxPx, isRtl) {
                detectTapGestures(
                    onPress = { pos ->
                        position.snapTo(if (isRtl) maxPx - pos.x else pos.x)
                        val interaction = PressInteraction.Press(pos)
                        coroutineScope {
                            launch {
                                interactionSource.emit(interaction)
                            }
                            try {
                                val success = tryAwaitRelease()
                                if (success) gestureEndAction(0f)
                                launch {
                                    interactionSource.emit(PressInteraction.Release(interaction))
                                }
                            } catch (c: CancellationException) {
                                launch {
                                    interactionSource.emit(PressInteraction.Cancel(interaction))
                                }
                            }
                        }
                    }
                )
            }
        } else {
            Modifier
        }
        val drag = Modifier.draggable(
            orientation = Orientation.Horizontal,
            reverseDirection = isRtl,
            enabled = enabled,
            interactionSource = interactionSource,
            onDragStopped = { velocity -> gestureEndAction(velocity) },
            startDragImmediately = position.holder.isRunning,
            state = rememberDraggableState {
                position.snapTo(position.holder.value + it)
            }
        )
        val coerced = value.coerceIn(position.startValue, position.endValue)
        val fraction = calcFraction(position.startValue, position.endValue, coerced)
        SliderImpl(
            enabled,
            fraction,
            position.tickFractions,
            colors,
            maxPx,
            interactionSource,
            modifier = press.then(drag)
        )
    }
}

/**
 * Object to hold defaults used by [Slider]
 */
object SliderDefaults {
    /**
     * Creates a [SliderColors] that represents the different colors used in parts of the
     * [Slider] in different states.
     *
     * For the name references below the words "active" and "inactive" are used. Active part of
     * the slider is filled with progress, so if slider's progress is 30% out of 100%, left (or
     * right in RTL) 30% of the track will be active, the rest is not active.
     *
     * @param thumbColor thumb color when enabled
     * @param disabledThumbColor thumb colors when disabled
     * @param activeTrackColor color of the track in the part that is "active", meaning that the
     * thumb is ahead of it
     * @param inactiveTrackColor color of the track in the part that is "inactive", meaning that the
     * thumb is before it
     * @param disabledActiveTrackColor color of the track in the "active" part when the Slider is
     * disabled
     * @param disabledInactiveTrackColor color of the track in the "inactive" part when the
     * Slider is disabled
     * @param activeTickColor colors to be used to draw tick marks on the active track, if `steps`
     * is specified
     * @param inactiveTickColor colors to be used to draw tick marks on the inactive track, if
     * `steps` are specified on the Slider is specified
     * @param disabledActiveTickColor colors to be used to draw tick marks on the active track
     * when Slider is disabled and when `steps` are specified on it
     * @param disabledInactiveTickColor colors to be used to draw tick marks on the inactive part
     * of the track when Slider is disabled and when `steps` are specified on it
     */
    @Composable
    fun colors(
        thumbColor: Color = MaterialTheme.colors.primary,
        disabledThumbColor: Color = MaterialTheme.colors.onSurface
            .copy(alpha = ContentAlpha.disabled)
            .compositeOver(MaterialTheme.colors.surface),
        activeTrackColor: Color = MaterialTheme.colors.primary,
        inactiveTrackColor: Color = activeTrackColor.copy(alpha = InactiveTrackAlpha),
        disabledActiveTrackColor: Color =
            MaterialTheme.colors.onSurface.copy(alpha = DisabledActiveTrackAlpha),
        disabledInactiveTrackColor: Color =
            disabledActiveTrackColor.copy(alpha = DisabledInactiveTrackAlpha),
        activeTickColor: Color = contentColorFor(activeTrackColor).copy(alpha = TickAlpha),
        inactiveTickColor: Color = activeTrackColor.copy(alpha = TickAlpha),
        disabledActiveTickColor: Color = activeTickColor.copy(alpha = DisabledTickAlpha),
        disabledInactiveTickColor: Color = disabledInactiveTrackColor
            .copy(alpha = DisabledTickAlpha)
    ): SliderColors = DefaultSliderColors(
        thumbColor = thumbColor,
        disabledThumbColor = disabledThumbColor,
        activeTrackColor = activeTrackColor,
        inactiveTrackColor = inactiveTrackColor,
        disabledActiveTrackColor = disabledActiveTrackColor,
        disabledInactiveTrackColor = disabledInactiveTrackColor,
        activeTickColor = activeTickColor,
        inactiveTickColor = inactiveTickColor,
        disabledActiveTickColor = disabledActiveTickColor,
        disabledInactiveTickColor = disabledInactiveTickColor
    )

    /**
     * Default alpha of the inactive part of the track
     */
    const val InactiveTrackAlpha = 0.24f

    /**
     * Default alpha for the track when it is disabled but active
     */
    const val DisabledInactiveTrackAlpha = 0.12f

    /**
     * Default alpha for the track when it is disabled and inactive
     */
    const val DisabledActiveTrackAlpha = 0.32f

    /**
     * Default alpha of the ticks that are drawn on top of the track
     */
    const val TickAlpha = 0.54f

    /**
     * Default alpha for tick marks when they are disabled
     */
    const val DisabledTickAlpha = 0.12f
}

/**
 * Represents the colors used by a [Slider] and its parts in different states
 *
 * See [SliderDefaults.colors] for the default implementation that follows Material
 * specifications.
 */
@Stable
interface SliderColors {
    /**
     * Represents the color used for the sliders's thumb, depending on [enabled].
     *
     * @param enabled whether the [Slider] is enabled or not
     */
    @Composable
    fun thumbColor(enabled: Boolean): State<Color>

    /**
     * Represents the color used for the sliders's track, depending on [enabled] and [active].
     *
     * Active part is filled with progress, so if sliders progress is 30% out of 100%, left (or
     * right in RTL) 30% of the track will be active, the rest is not active.
     *
     * @param enabled whether the [Slider] is enabled or not
     * @param active whether the part of the track is active of not
     */
    @Composable
    fun trackColor(enabled: Boolean, active: Boolean): State<Color>

    /**
     * Represents the color used for the sliders's tick which is the dot separating steps, if
     * they are set on the slider, depending on [enabled] and [active].
     *
     * Active tick is the tick that is in the part of the track filled with progress, so if
     * sliders progress is 30% out of 100%, left (or right in RTL) 30% of the track and the ticks
     * in this 30% will be active, the rest is not active.
     *
     * @param enabled whether the [Slider] is enabled or not
     * @param active whether the part of the track this tick is in is active of not
     */
    @Composable
    fun tickColor(enabled: Boolean, active: Boolean): State<Color>
}

@Composable
private fun SliderImpl(
    enabled: Boolean,
    positionFraction: Float,
    tickFractions: List<Float>,
    colors: SliderColors,
    width: Float,
    interactionSource: MutableInteractionSource,
    modifier: Modifier
) {
    val widthDp = with(LocalDensity.current) {
        width.toDp()
    }
    Box(modifier.then(DefaultSliderConstraints)) {
        val thumbSize = 32.dp//ThumbRadius * 2
        val offset =  (widthDp - thumbSize) * positionFraction
        val center = Modifier.align(Alignment.CenterStart)
        val trackStrokeWidth: Float
        val thumbPx: Float
        with(LocalDensity.current) {
            trackStrokeWidth = TrackHeight.toPx()
            thumbPx = (thumbSize / 2).toPx()//ThumbRadius.toPx()
        }
        Track(
            center.fillMaxSize(),
            colors,
            enabled,
            positionFraction,
            tickFractions,
            thumbPx,
            trackStrokeWidth
        )
        Box(center.padding(start = offset).size(thumbSize)) {
            val interactions = remember { mutableStateListOf<Interaction>() }

            LaunchedEffect(interactionSource) {
                interactionSource.interactions.collect { interaction ->
                    when (interaction) {
                        is PressInteraction.Press -> interactions.add(interaction)
                        is PressInteraction.Release -> interactions.remove(interaction.press)
                        is PressInteraction.Cancel -> interactions.remove(interaction.press)
                        is DragInteraction.Start -> interactions.add(interaction)
                        is DragInteraction.Stop -> interactions.remove(interaction.start)
                        is DragInteraction.Cancel -> interactions.remove(interaction.start)
                    }
                }
            }
            val hasInteraction = interactions.isNotEmpty()
            val elevation = if (hasInteraction) {
                ThumbPressedElevation
            } else {
                ThumbDefaultElevation
            }
            Surface(
                elevation = if (enabled) elevation else 0.dp,
                modifier = Modifier
                    .size(thumbSize, thumbSize)
                    .focusable(interactionSource = interactionSource)
                    .indication(
                        interactionSource = interactionSource,
                        indication = rememberRipple(
                            bounded = false,
                            radius = ThumbRippleRadius
                        )
                    )
            ) {
                Image(
                    painter = painterResource(R.drawable.thumb),
                    contentDescription = "Thumb",
                    modifier = Modifier
                        .size(thumbSize, thumbSize)
                        .align(Alignment.Center),
                    contentScale = ContentScale.FillWidth
                )
                Spacer(Modifier.size(thumbSize, thumbSize))
            }
        }
    }
}

@Composable
private fun Track(
    modifier: Modifier,
    colors: SliderColors,
    enabled: Boolean,
    positionFraction: Float,
    tickFractions: List<Float>,
    thumbPx: Float,
    trackStrokeWidth: Float
) {
    val inactiveTrackColor = colors.trackColor(enabled, active = false)
    val activeTrackColor = colors.trackColor(enabled, active = true)
    val inactiveTickColor = colors.tickColor(enabled, active = false)
    val activeTickColor = colors.tickColor(enabled, active = true)
    Canvas(modifier) {
        val height = size.height
        val rectY = (height / 2) - (TrackHeight.toPx() / 2).toInt()
        val isRtl = layoutDirection == LayoutDirection.Rtl
        val sliderLeft = Offset(thumbPx, center.y)
        val sliderRight = Offset(size.width - thumbPx, center.y)
        val sliderStart = if (isRtl) sliderRight else sliderLeft
        val sliderEnd = if (isRtl) sliderLeft else sliderRight

        drawRoundRect(
            color = inactiveTrackColor.value,
            topLeft = Offset(sliderStart.x / 2, rectY),
            size = Size(width = sliderEnd.x, height = TrackHeight.toPx()),
            cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx()),
        )
//        drawLine(
//            inactiveTrackColor.value,
//            sliderStart,
//            sliderEnd,
//            trackStrokeWidth,
//            StrokeCap.Butt
//        )
        val sliderValue = Offset(
            sliderStart.x + (sliderEnd.x - sliderStart.x) * positionFraction,
            center.y
        )


        drawRoundRect(
            color = activeTrackColor.value,
            topLeft = Offset(sliderStart.x / 2, rectY),
            size = Size(width = sliderValue.x, height = TrackHeight.toPx()),
            cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx()),
        )
//        drawLine(
//            activeTrackColor.value,
//            sliderStart,
//            sliderValue,
//            trackStrokeWidth,
//            StrokeCap.Butt
//        )
        tickFractions.groupBy { it > positionFraction }.forEach { (afterFraction, list) ->
            drawPoints(
                list.map {
                    Offset(lerp(sliderStart, sliderEnd, it).x, center.y)
                },
                PointMode.Points,
                (if (afterFraction) inactiveTickColor else activeTickColor).value,
                trackStrokeWidth,
                StrokeCap.Round
            )
        }
    }
}

// Scale x1 from a1..b1 range to a2..b2 range
private fun scale(a1: Float, b1: Float, x1: Float, a2: Float, b2: Float) =
    lerp(a2, b2, calcFraction(a1, b1, x1))

// Calculate the 0..1 fraction that `pos` value represents between `a` and `b`
private fun calcFraction(a: Float, b: Float, pos: Float) =
    (if (b - a == 0f) 0f else (pos - a) / (b - a)).coerceIn(0f, 1f)

private fun Modifier.sliderSemantics(
    value: Float,
    position: SliderPosition,
    enabled: Boolean,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0
): Modifier {
    val coerced = value.coerceIn(position.startValue, position.endValue)
    return semantics(mergeDescendants = true) {
        if (!enabled) disabled()
        setProgress(
            action = { targetValue ->
                val newValue = targetValue.coerceIn(position.startValue, position.endValue)
                val resolvedValue = if (steps > 0) {
                    position.tickFractions
                        .map { lerp(position.startValue, position.endValue, it) }
                        .minByOrNull { abs(it - newValue) } ?: newValue
                } else {
                    newValue
                }
                // This is to keep it consistent with AbsSeekbar.java: return false if no
                // change from current.
                if (resolvedValue == coerced) {
                    false
                } else {
                    onValueChange(resolvedValue)
                    true
                }
            }
        )
    }.progressSemantics(value, valueRange, steps)
}

/**
 * Internal state for [Slider] that represents the Slider value, its bounds and optional amount of
 * steps evenly distributed across the Slider range.
 *
 * @param initial initial value for the Slider when created. If outside of range provided,
 * initial position will be coerced to this range
 * @param valueRange range of values that Slider value can take
 * @param steps if greater than 0, specifies the amounts of discrete values, evenly distributed
 * between across the whole value range. If 0, slider will behave as a continuous slider and allow
 * to choose any value from the range specified. Must not be negative.
 */
private class SliderPosition(
    initial: Float = 0f,
    val valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    /*@IntRange(from = 0)*/
    steps: Int = 0,
    val scope: CoroutineScope,
    var onValueChange: (Float) -> Unit
) {
    val startValue: Float = valueRange.start
    val endValue: Float = valueRange.endInclusive

    init {
        require(steps >= 0) {
            "steps should be >= 0"
        }
    }

    var scaledValue: Float = initial
        set(value) {
            val scaled = scale(startValue, endValue, value, startPx, endPx)
            // floating point error due to rescaling
            if ((scaled - holder.value) > floatPointMistakeCorrection) {
                snapTo(scaled)
            }
        }
    private val floatPointMistakeCorrection = (valueRange.endInclusive - valueRange.start) / 100
    private var endPx = Float.MAX_VALUE
    private var startPx = Float.MIN_VALUE

    fun setBounds(min: Float, max: Float) {
        if (startPx == min && endPx == max) return
        val newValue = scale(startPx, endPx, holder.value, min, max)
        startPx = min
        endPx = max
        holder.updateBounds(min, max)
        anchorsPx = tickFractions.map {
            lerp(startPx, endPx, it)
        }
        snapTo(newValue)
    }

    val tickFractions: List<Float> =
        if (steps == 0) emptyList() else List(steps + 2) { it.toFloat() / (steps + 1) }
    var anchorsPx: List<Float> = emptyList()
        private set
    val holder = Animatable(scale(startValue, endValue, initial, startPx, endPx))

    fun snapTo(newValue: Float) {
        scope.launch {
            holder.snapTo(newValue)
            onHolderValueUpdated(holder.value)
        }
    }

    val onHolderValueUpdated: (value: Float) -> Unit = {
        onValueChange(scale(startPx, endPx, it, startValue, endValue))
    }
}

@Immutable
private class DefaultSliderColors(
    private val thumbColor: Color,
    private val disabledThumbColor: Color,
    private val activeTrackColor: Color,
    private val inactiveTrackColor: Color,
    private val disabledActiveTrackColor: Color,
    private val disabledInactiveTrackColor: Color,
    private val activeTickColor: Color,
    private val inactiveTickColor: Color,
    private val disabledActiveTickColor: Color,
    private val disabledInactiveTickColor: Color
) : SliderColors {
    @Composable
    override fun thumbColor(enabled: Boolean): State<Color> {
        return rememberUpdatedState(if (enabled) thumbColor else disabledThumbColor)
    }

    @Composable
    override fun trackColor(enabled: Boolean, active: Boolean): State<Color> {
        return rememberUpdatedState(
            if (enabled) {
                if (active) activeTrackColor else inactiveTrackColor
            } else {
                if (active) disabledActiveTrackColor else disabledInactiveTrackColor
            }
        )
    }

    @Composable
    override fun tickColor(enabled: Boolean, active: Boolean): State<Color> {
        return rememberUpdatedState(
            if (enabled) {
                if (active) activeTickColor else inactiveTickColor
            } else {
                if (active) disabledActiveTickColor else disabledInactiveTickColor
            }
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as DefaultSliderColors

        if (thumbColor != other.thumbColor) return false
        if (disabledThumbColor != other.disabledThumbColor) return false
        if (activeTrackColor != other.activeTrackColor) return false
        if (inactiveTrackColor != other.inactiveTrackColor) return false
        if (disabledActiveTrackColor != other.disabledActiveTrackColor) return false
        if (disabledInactiveTrackColor != other.disabledInactiveTrackColor) return false
        if (activeTickColor != other.activeTickColor) return false
        if (inactiveTickColor != other.inactiveTickColor) return false
        if (disabledActiveTickColor != other.disabledActiveTickColor) return false
        if (disabledInactiveTickColor != other.disabledInactiveTickColor) return false

        return true
    }

    override fun hashCode(): Int {
        var result = thumbColor.hashCode()
        result = 31 * result + disabledThumbColor.hashCode()
        result = 31 * result + activeTrackColor.hashCode()
        result = 31 * result + inactiveTrackColor.hashCode()
        result = 31 * result + disabledActiveTrackColor.hashCode()
        result = 31 * result + disabledInactiveTrackColor.hashCode()
        result = 31 * result + activeTickColor.hashCode()
        result = 31 * result + inactiveTickColor.hashCode()
        result = 31 * result + disabledActiveTickColor.hashCode()
        result = 31 * result + disabledInactiveTickColor.hashCode()
        return result
    }
}

// Internal to be referred to in tests
//internal val ThumbRadius = 10.dp
private val ThumbRippleRadius = 30.dp
private val ThumbDefaultElevation = 1.dp
private val ThumbPressedElevation = 6.dp

// Internal to be referred to in tests
internal val TrackHeight = 20.dp
private val SliderHeight = 48.dp
private val SliderMinWidth = 144.dp // TODO: clarify min width
private val DefaultSliderConstraints =
    Modifier
        .widthIn(min = SliderMinWidth)
        .heightIn(max = SliderHeight)
private val SliderToTickAnimation = TweenSpec<Float>(durationMillis = 100)