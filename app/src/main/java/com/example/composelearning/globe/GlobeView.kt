package com.example.composelearning.globe

import android.graphics.BitmapShader
import android.graphics.RuntimeShader
import android.graphics.Shader
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composelearning.globe.data.Country
import com.example.composelearning.globe.data.WorldAtlas
import com.example.composelearning.util.LocalAnimationsEnabled
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.PI
import kotlin.math.min

/** Fraction of the shorter viewport edge the sphere occupies at zoom 1. */
private const val DiscFill = 0.86f

val GlobeSpace = Color(0xFF04060E)
private val GlobeBorderInk = Color(0xFF0D1219)
private val GlobeSelectInk = Color(0xFFFFFAB8)

/** The four-colouring classes of GLOBE.md §14b, as actual colours. */
val GlobeCountryPalette = listOf(
    Color(0xFFE87361),
    Color(0xFF5CA0DE),
    Color(0xFFF5C462),
    Color(0xFF80CC91),
    Color(0xFFBD92DE),
    Color(0xFF8FD1D1)
)
private val GlobeOcean = Color(0xFF09162D)

/**
 * The rotating country globe.
 *
 * @param atlas baked country raster + table, from [WorldAtlas.load].
 * @param selected drawn highlighted; pass what [onSelect] hands you.
 * @param sunlight enables the day/night terminator of GLOBE.md §9.
 * @param background must match what is actually behind this composable: the shader outputs
 *   opaque pixels and composites the sphere, its halo and empty space onto this colour.
 */
@Composable
fun GlobeView(
    atlas: WorldAtlas,
    state: GlobeState,
    modifier: Modifier = Modifier,
    showLabels: Boolean = true,
    showGraticule: Boolean = true,
    showBorders: Boolean = true,
    sunlight: Boolean = false,
    selected: Country? = null,
    onSelect: (Country?) -> Unit = {},
    background: Color = GlobeSpace
) {
    val density = LocalDensity.current
    val shader = remember { RuntimeShader(GlobeAgsl) }
    val brush = remember(shader) { ShaderBrush(shader) }

    val indexShader = remember(atlas) {
        BitmapShader(atlas.indexBitmap, Shader.TileMode.REPEAT, Shader.TileMode.CLAMP).apply {
            // Bilinear filtering would interpolate *ids* and invent countries (GLOBE.md §7).
            setFilterMode(BitmapShader.FILTER_MODE_NEAREST)
        }
    }
    val paletteShader = remember(atlas) {
        val bitmap = atlas.buildPalette(GlobeCountryPalette, GlobeOcean)
        BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP).apply {
            setFilterMode(BitmapShader.FILTER_MODE_NEAREST)
        }
    }

    // The sun moves ~15°/hour, so recomputing it once per composition is plenty.
    val sun = remember {
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        subsolarDirection(
            dayOfYear = utc.get(Calendar.DAY_OF_YEAR),
            utcHours = utc.get(Calendar.HOUR_OF_DAY) +
                utc.get(Calendar.MINUTE) / 60f
        )
    }

    val animationsEnabled = LocalAnimationsEnabled.current
    LaunchedFrameLoop(enabled = animationsEnabled) { dt -> state.tick(dt) }

    val measurer = rememberTextMeasurer()
    val labelStyle = remember(density) {
        TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
    }
    val layoutCache = remember(labelStyle) { mutableMapOf<String, TextLayoutResult>() }
    val currentOnSelect by rememberUpdatedState(onSelect)
    val borderPx = remember(density) { with(density) { 0.4.dp.toPx() }.coerceAtLeast(0.75f) }
    // In dp, not px: otherwise a 3x phone shows far more labels than a 2x one (GLOBE.md §13).
    val minLabelExtentPx = remember(density) { with(density) { 12.dp.toPx() } }

    Canvas(
        modifier = modifier
            .onSizeChanged { size ->
                // GLOBE.md §11c: never magnify past one texel per pixel.
                val base = discRadius(Size(size.width.toFloat(), size.height.toFloat()), 1f)
                if (base > 0f) {
                    state.maxZoom = (atlas.texWidth / (2f * PI.toFloat() * base)).coerceIn(1f, 4f)
                }
            }
            .pointerInput(atlas) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    state.onGestureStart()
                    val tracker = VelocityTracker()
                    tracker.addPosition(down.uptimeMillis, down.position)
                    var travel = 0f
                    var pointer = down.position

                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.changes.none { it.pressed }) break
                        val k = discRadius(
                            Size(size.width.toFloat(), size.height.toFloat()),
                            state.zoom
                        )
                        val zoomChange = event.calculateZoom()
                        if (zoomChange > 0f && zoomChange != 1f) state.pinch(zoomChange)
                        val pan = event.calculatePan()
                        if (pan != Offset.Zero) {
                            state.drag(pan.x, pan.y, k)
                            travel += pan.getDistance()
                            pointer += pan
                            tracker.addPosition(event.changes.first().uptimeMillis, pointer)
                        }
                        event.changes.forEach { if (it.positionChanged()) it.consume() }
                    }

                    val viewport = Size(size.width.toFloat(), size.height.toFloat())
                    val k = discRadius(viewport, state.zoom)
                    if (travel <= viewConfiguration.touchSlop) {
                        state.endGesture()
                        currentOnSelect(
                            hitTest(
                                tap = down.position,
                                centre = Offset(viewport.width / 2f, viewport.height / 2f),
                                k = k,
                                rotation = GlobeRotation(state.lonRad, state.latRad),
                                atlas = atlas
                            )
                        )
                    } else {
                        val v = tracker.calculateVelocity()
                        state.fling(v.x, v.y, k)
                    }
                }
            }
    ) {
        val k = discRadius(size, state.zoom)
        val centre = Offset(size.width / 2f, size.height / 2f)
        val rotation = GlobeRotation(state.lonRad, state.latRad)

        shader.applyGlobeUniforms(
            u = GlobeUniforms(
                centre = centre,
                radiusPx = k,
                rotation = rotation,
                sun = sun,
                texWidth = atlas.texWidth,
                texHeight = atlas.texHeight,
                selectedId = selected?.id ?: -1,
                borderPx = borderPx,
                graticuleStepDeg = if (showGraticule) 15f else 0f,
                nightStrength = if (sunlight) 1f else 0f,
                bordersEnabled = showBorders,
                background = background,
                borderInk = GlobeBorderInk,
                selectInk = GlobeSelectInk
            ),
            indexShader = indexShader,
            paletteShader = paletteShader
        )
        drawRect(brush = brush)

        if (selected != null) {
            drawSelectionMarker(selected, rotation, centre, k)
        }
        if (showLabels) {
            val labels = layoutLabels(
                atlas = atlas,
                rotation = rotation,
                centre = centre,
                k = k,
                viewport = size,
                measure = { name -> layoutCache.measured(name, measurer, labelStyle).size },
                selectedId = selected?.id ?: -1,
                minExtentPx = minLabelExtentPx
            )
            labels.forEach { label ->
                drawLabel(label, layoutCache.measured(label.country.name, measurer, labelStyle))
            }
        }
    }
}

private fun MutableMap<String, TextLayoutResult>.measured(
    text: String,
    measurer: TextMeasurer,
    style: TextStyle
): TextLayoutResult = getOrPut(text) { measurer.measure(text, style) }

private fun DrawScope.drawLabel(label: GlobeLabel, layout: TextLayoutResult) {
    val pad = 3f
    drawRoundRect(
        color = Color.Black,
        topLeft = Offset(label.topLeft.x - pad, label.topLeft.y - pad * 0.5f),
        size = Size(label.size.width + pad * 2f, label.size.height + pad),
        cornerRadius = CornerRadius(4f, 4f),
        alpha = 0.34f * label.alpha
    )
    drawText(
        textLayoutResult = layout,
        color = if (label.selected) GlobeSelectInk else Color.White,
        topLeft = label.topLeft,
        alpha = label.alpha
    )
}

private fun DrawScope.drawSelectionMarker(
    selected: Country,
    rotation: GlobeRotation,
    centre: Offset,
    k: Float
) {
    val n = lonLatToVec(selected.lon.toRadians(), selected.lat.toRadians())
    val p = project(n, rotation, centre, k)
    if (!p.visible) return
    val alpha = smoothstep(0f, 0.2f, p.depth)
    drawCircle(
        color = GlobeSelectInk,
        radius = 5f,
        center = p.position,
        alpha = 0.9f * alpha,
        style = Stroke(width = 1.6f)
    )
}

/** Sphere radius in pixels for a viewport: `K` in GLOBE.md's notation. */
private fun discRadius(size: Size, zoom: Float): Float =
    min(size.width, size.height) / 2f * DiscFill * zoom

/**
 * Drives [onFrame] with the frame delta in seconds. Mutating float state from here and
 * reading it inside a draw block is what keeps the spin at draw-invalidation cost.
 */
@Composable
private fun LaunchedFrameLoop(enabled: Boolean, onFrame: (Float) -> Unit) {
    val callback by rememberUpdatedState(onFrame)
    LaunchedEffect(enabled) {
        if (!enabled) return@LaunchedEffect
        var last = 0L
        while (true) {
            withFrameNanos { now ->
                if (last != 0L) {
                    callback(((now - last) / 1_000_000_000.0).toFloat().coerceAtMost(0.05f))
                }
                last = now
            }
        }
    }
}
