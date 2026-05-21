package com.example.composelearning.flight

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

private const val MinScale = 1f
private const val MaxScale = 3.5f

/**
 * Compose port of ldoublem/FlightSeat (top-down plane seat selection).
 *
 * Visual: a stylized airliner — cockpit nose, fuselage, swept wings with engine pods, tail
 * stabilizers and vertical fin. Cabin is divided into First / Business / Economy / Tail
 * sections with galleys between them.
 *
 * Interaction: pinch to zoom (1x..3.5x), drag to pan when zoomed in, tap a seat to toggle
 * Available <-> Selecting. [FlightSeatState.confirmSelection] locks selecting seats. Section
 * jump buttons animate scroll. A minimap shows the whole plane with a viewport rectangle.
 */
@Composable
fun FlightSeatView(
    state: FlightSeatState,
    modifier: Modifier = Modifier,
    colors: FlightSeatColors = FlightSeatDefaults.colors(),
    maxSelections: Int = 10,
) {
    val scope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }

    BoxWithConstraints(modifier) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }
        val plane = remember(widthPx, heightPx, state.sections) {
            computePlaneLayout(Size(widthPx, heightPx), state.sections)
        }
        val seatHits = remember(plane) { plane.allSeatHits() }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(seatHits, maxSelections) {
                    detectTapGestures { tap ->
                        val s = scale.value
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val planeTap = Offset(
                            (tap.x - cx - offsetX.value) / s + cx,
                            (tap.y - cy - offsetY.value) / s + cy,
                        )
                        seatHits.firstOrNull { it.rect.contains(planeTap) }
                            ?.let { state.toggleSeat(it.key, maxSelections) }
                    }
                }
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        val newScale = (scale.value * zoom).coerceIn(MinScale, MaxScale)
                        val maxPanX = max(0f, (newScale - 1f) * size.width / 2f)
                        val maxPanY = max(0f, (newScale - 1f) * size.height / 2f)
                        val newX = (offsetX.value + pan.x).coerceIn(-maxPanX, maxPanX)
                        val newY = (offsetY.value + pan.y).coerceIn(-maxPanY, maxPanY)
                        scope.launch {
                            scale.snapTo(newScale)
                            offsetX.snapTo(newX)
                            offsetY.snapTo(newY)
                        }
                    }
                },
        ) {
            withTransform({
                translate(offsetX.value, offsetY.value)
                scale(scale.value, scale.value, Offset(size.width / 2f, size.height / 2f))
            }) {
                drawPlane(plane, colors)
                drawSections(plane, state, colors)
            }
            drawMinimap(
                plane = plane,
                state = state,
                scale = scale.value,
                offset = Offset(offsetX.value, offsetY.value),
                canvas = size,
                colors = colors,
            )
        }

        // Floating controls — section jumps + zoom reset
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            state.sections.forEachIndexed { idx, section ->
                FilterChip(
                    selected = false,
                    onClick = {
                        animateToSection(
                            scope = scope,
                            plane = plane,
                            sectionIndex = idx,
                            scale = scale,
                            offsetX = offsetX,
                            offsetY = offsetY,
                            canvasSize = Size(widthPx, heightPx),
                        )
                    },
                    label = { Text(section.name) },
                )
            }
            FilterChip(
                selected = false,
                onClick = {
                    scope.launch {
                        scale.animateTo(1f, tween(300))
                        offsetX.animateTo(0f, tween(300))
                        offsetY.animateTo(0f, tween(300))
                    }
                },
                label = { Text("Fit") },
            )
        }
    }
}

private fun animateToSection(
    scope: kotlinx.coroutines.CoroutineScope,
    plane: PlaneLayout,
    sectionIndex: Int,
    scale: Animatable<Float, *>,
    offsetX: Animatable<Float, *>,
    offsetY: Animatable<Float, *>,
    canvasSize: Size,
) {
    val section = plane.sections[sectionIndex]
    val sectionCenter = Offset(
        (section.rect.left + section.rect.right) / 2f,
        (section.rect.top + section.rect.bottom) / 2f,
    )
    val canvasCenter = Offset(canvasSize.width / 2f, canvasSize.height / 2f)
    val targetScale = 2.4f
    // To move planeCenter onto canvasCenter under scale s pivoting at canvasCenter:
    // screen = canvasCenter + (planeCenter - canvasCenter) * s + offset
    // want screen == canvasCenter ⇒ offset = -(planeCenter - canvasCenter) * s
    val targetOffset = Offset(
        -(sectionCenter.x - canvasCenter.x) * targetScale,
        -(sectionCenter.y - canvasCenter.y) * targetScale,
    )
    val maxPanX = (targetScale - 1f) * canvasSize.width / 2f
    val maxPanY = (targetScale - 1f) * canvasSize.height / 2f
    val clampedX = targetOffset.x.coerceIn(-maxPanX, maxPanX)
    val clampedY = targetOffset.y.coerceIn(-maxPanY, maxPanY)
    scope.launch {
        scale.animateTo(targetScale, tween(450))
    }
    scope.launch {
        offsetX.animateTo(clampedX, tween(450))
    }
    scope.launch {
        offsetY.animateTo(clampedY, tween(450))
    }
}

// ---------- State ----------

@Stable
class FlightSeatState(initialSections: List<CabinSection>) {
    val sections: List<CabinSection> = initialSections
    private val seatStates = mutableStateOf<Map<SeatKey, SeatState>>(emptyMap())

    val selectingCount: Int get() = seatStates.value.count { it.value == SeatState.Selecting }
    val selectedCount: Int get() = seatStates.value.count { it.value == SeatState.Selected }

    fun stateFor(key: SeatKey): SeatState = seatStates.value[key] ?: SeatState.Available

    fun toggleSeat(key: SeatKey, maxSelections: Int) {
        val current = seatStates.value
        when (current[key] ?: SeatState.Available) {
            SeatState.Selected -> return
            SeatState.Selecting -> seatStates.value = current - key
            SeatState.Available -> {
                if (current.count { it.value == SeatState.Selecting } >= maxSelections) return
                seatStates.value = current + (key to SeatState.Selecting)
            }
        }
    }

    fun confirmSelection() {
        seatStates.value = seatStates.value.mapValues { (_, s) ->
            if (s == SeatState.Selecting) SeatState.Selected else s
        }
    }

    fun reset() {
        seatStates.value = emptyMap()
    }
}

@Composable
fun rememberFlightSeatState(
    sections: List<CabinSection> = FlightSeatDefaults.sections(),
): FlightSeatState = remember(sections) { FlightSeatState(sections) }

@Immutable
data class CabinSection(
    val name: String,
    val rows: Int,
    val columnGroups: List<Int>,
    val sectionIndex: Int,
)

@Immutable
data class SeatKey(val sectionIndex: Int, val row: Int, val column: Int)

enum class SeatState { Available, Selecting, Selected }

private data class SeatHit(val key: SeatKey, val rect: Rect)

@Immutable
data class FlightSeatColors(
    val sky: Color,
    val fuselage: Color,
    val fuselageHighlight: Color,
    val outline: Color,
    val window: Color,
    val galley: Color,
    val galleyText: Color,
    val engine: Color,
    val seatAvailable: Color,
    val seatSelecting: Color,
    val seatSelected: Color,
    val minimapBackground: Color,
    val minimapPlane: Color,
    val minimapViewport: Color,
    val minimapDot: Color,
)

object FlightSeatDefaults {
    fun colors() = FlightSeatColors(
        sky = Color(0xFFEAF0F6),
        fuselage = Color(0xFFFFFFFF),
        fuselageHighlight = Color(0xFFF1F4F8),
        outline = Color(0xFF3D4654),
        window = Color(0xFF7DB8E8),
        galley = Color(0xFFCBD2DC),
        galleyText = Color(0xFF2A3140),
        engine = Color(0xFF6E7787),
        seatAvailable = Color(0xFF9098A4),
        seatSelecting = Color(0xFF4FC3F7),
        seatSelected = Color(0xFFFF7043),
        minimapBackground = Color(0xCCFFFFFF),
        minimapPlane = Color(0xFFB8BEC7),
        minimapViewport = Color(0xFFEF5350),
        minimapDot = Color(0xFFFF7043),
    )

    fun sections(): List<CabinSection> = listOf(
        CabinSection("First", rows = 3, columnGroups = listOf(2, 2), sectionIndex = 0),
        CabinSection("Business", rows = 4, columnGroups = listOf(2, 2, 2), sectionIndex = 1),
        CabinSection("Economy", rows = 16, columnGroups = listOf(3, 3), sectionIndex = 2),
        CabinSection("Tail", rows = 5, columnGroups = listOf(2, 2), sectionIndex = 3),
    )
}

// ---------- Layout ----------

private data class SectionLayout(
    val name: String,
    val rect: Rect,
    val seats: List<SeatHit>,
    val galleyAbove: Rect,
    val galleyLabel: String,
)

private data class PlaneLayout(
    val canvasSize: Size,
    val noseTip: Offset,
    val bodyRect: Rect,
    val leftWing: Path,
    val rightWing: Path,
    val leftEngine: Rect,
    val rightEngine: Rect,
    val leftTailWing: Path,
    val rightTailWing: Path,
    val verticalFin: Path,
    val tailCone: Path,
    val cockpitWindow: Path,
    val sections: List<SectionLayout>,
    val minimapRect: Rect,
    val cabinRect: Rect,
) {
    fun allSeatHits(): List<SeatHit> = sections.flatMap { it.seats }
}

private fun computePlaneLayout(
    canvasSize: Size,
    sections: List<CabinSection>,
): PlaneLayout {
    val w = canvasSize.width
    val h = canvasSize.height

    // Body proportions — long and narrow
    val bodyWidth = w * 0.22f
    val midX = w * 0.46f // shift slightly left so minimap sits on right
    val bodyLeft = midX - bodyWidth / 2f
    val bodyRight = midX + bodyWidth / 2f

    val noseHeight = h * 0.08f
    val tailHeight = h * 0.10f
    val bodyTop = noseHeight
    val bodyBottom = h - tailHeight

    val bodyRect = Rect(bodyLeft, bodyTop, bodyRight, bodyBottom)
    val noseTip = Offset(midX, 0f)

    // Cockpit window — a curved rect inside the nose region
    val cockpitWindow = Path().apply {
        val top = noseHeight * 0.35f
        val bot = bodyTop + bodyWidth * 0.15f
        moveTo(midX - bodyWidth * 0.25f, bot)
        quadraticTo(midX, top, midX + bodyWidth * 0.25f, bot)
        close()
    }

    // Tail cone — narrowing
    val tailCone = Path().apply {
        moveTo(bodyLeft + bodyWidth * 0.08f, bodyBottom)
        lineTo(midX - bodyWidth * 0.08f, h * 0.99f)
        lineTo(midX + bodyWidth * 0.08f, h * 0.99f)
        lineTo(bodyRight - bodyWidth * 0.08f, bodyBottom)
        close()
    }

    // Main wings — swept-back. Wing root at ~38% body height.
    val wingRootY = bodyTop + (bodyBottom - bodyTop) * 0.38f
    val wingSpan = bodyWidth * 2.2f
    val wingRootH = (bodyBottom - bodyTop) * 0.18f
    val wingTipH = wingRootH * 0.25f
    val wingSweep = wingRootH * 2.6f // how much further down the tip is

    val rightWing = Path().apply {
        moveTo(bodyRight - 2f, wingRootY - wingRootH * 0.5f)
        lineTo(bodyRight + wingSpan, wingRootY + wingSweep - wingTipH * 0.5f)
        quadraticTo(
            bodyRight + wingSpan + 12f, wingRootY + wingSweep,
            bodyRight + wingSpan - 4f, wingRootY + wingSweep + wingTipH * 0.5f,
        )
        lineTo(bodyRight - 2f, wingRootY + wingRootH * 0.7f)
        close()
    }
    val leftWing = Path().apply {
        moveTo(bodyLeft + 2f, wingRootY - wingRootH * 0.5f)
        lineTo(bodyLeft - wingSpan, wingRootY + wingSweep - wingTipH * 0.5f)
        quadraticTo(
            bodyLeft - wingSpan - 12f, wingRootY + wingSweep,
            bodyLeft - wingSpan + 4f, wingRootY + wingSweep + wingTipH * 0.5f,
        )
        lineTo(bodyLeft + 2f, wingRootY + wingRootH * 0.7f)
        close()
    }

    // Engines — small ovals hanging in front of wings
    val engineW = bodyWidth * 0.32f
    val engineH = bodyWidth * 0.55f
    val engineYCenter = wingRootY + wingSweep * 0.55f
    val leftEngine = Rect(
        bodyLeft - wingSpan * 0.55f - engineW / 2f, engineYCenter - engineH / 2f,
        bodyLeft - wingSpan * 0.55f + engineW / 2f, engineYCenter + engineH / 2f,
    )
    val rightEngine = Rect(
        bodyRight + wingSpan * 0.55f - engineW / 2f, engineYCenter - engineH / 2f,
        bodyRight + wingSpan * 0.55f + engineW / 2f, engineYCenter + engineH / 2f,
    )

    // Tail stabilizers — smaller wings
    val tailWingY = bodyBottom + tailHeight * 0.15f
    val tailWingSpan = bodyWidth * 1.0f
    val tailWingRootH = tailHeight * 0.35f
    val tailWingTipH = tailWingRootH * 0.4f
    val tailWingSweep = tailWingRootH * 1.4f

    val rightTailWing = Path().apply {
        moveTo(bodyRight - bodyWidth * 0.10f, tailWingY - tailWingRootH * 0.5f)
        lineTo(bodyRight + tailWingSpan, tailWingY + tailWingSweep - tailWingTipH * 0.5f)
        quadraticTo(
            bodyRight + tailWingSpan + 8f, tailWingY + tailWingSweep,
            bodyRight + tailWingSpan - 4f, tailWingY + tailWingSweep + tailWingTipH * 0.5f,
        )
        lineTo(bodyRight - bodyWidth * 0.10f, tailWingY + tailWingRootH * 0.5f)
        close()
    }
    val leftTailWing = Path().apply {
        moveTo(bodyLeft + bodyWidth * 0.10f, tailWingY - tailWingRootH * 0.5f)
        lineTo(bodyLeft - tailWingSpan, tailWingY + tailWingSweep - tailWingTipH * 0.5f)
        quadraticTo(
            bodyLeft - tailWingSpan - 8f, tailWingY + tailWingSweep,
            bodyLeft - tailWingSpan + 4f, tailWingY + tailWingSweep + tailWingTipH * 0.5f,
        )
        lineTo(bodyLeft + bodyWidth * 0.10f, tailWingY + tailWingRootH * 0.5f)
        close()
    }

    // Vertical fin — top-down it's a thin elongated diamond above the tail stabilizers
    val verticalFin = Path().apply {
        moveTo(midX, bodyBottom - tailHeight * 0.05f)
        lineTo(midX + bodyWidth * 0.18f, tailWingY)
        lineTo(midX, tailWingY + tailHeight * 0.35f)
        lineTo(midX - bodyWidth * 0.18f, tailWingY)
        close()
    }

    // Cabin layout — divide bodyRect among sections
    val galleyH = h * 0.014f
    val cabinTop = bodyTop + bodyWidth * 0.22f // below cockpit windows
    val cabinBottom = bodyBottom - bodyWidth * 0.10f
    val cabinRect = Rect(bodyLeft, cabinTop, bodyRect.right, cabinBottom)
    val totalRows = sections.sumOf { it.rows }
    val rowH = (cabinBottom - cabinTop - galleyH * (sections.size + 1)) / totalRows

    val maxCols = sections.maxOf { it.columnGroups.sum() }
    val maxAisles = sections.first { it.columnGroups.sum() == maxCols }.columnGroups.size - 1
    val seatSize = (bodyWidth * 0.74f) / maxCols
    val seatGap = (bodyWidth - seatSize * maxCols) / (maxCols - 1 - maxAisles + maxAisles * 2.2f + 2f).coerceAtLeast(1f)
    val aisleGap = seatGap * 2.2f

    var y = cabinTop + galleyH
    val sectionLayouts = sections.map { section ->
        val sectionH = section.rows * rowH
        val sectionRect = Rect(bodyLeft, y, bodyRect.right, y + sectionH)
        val seats = mutableListOf<SeatHit>()
        val totalCols = section.columnGroups.sum()
        val numAisles = section.columnGroups.size - 1
        val rowWidth = totalCols * seatSize +
            (totalCols - 1 - numAisles) * seatGap +
            numAisles * aisleGap
        val startX = bodyLeft + (bodyWidth - rowWidth) / 2f

        for (row in 0 until section.rows) {
            val seatY = sectionRect.top + row * rowH + (rowH - seatSize) / 2f
            var x = startX
            var colIndex = 0
            section.columnGroups.forEachIndexed { groupIdx, groupCount ->
                for (c in 0 until groupCount) {
                    val rect = Rect(x, seatY, x + seatSize, seatY + seatSize)
                    seats += SeatHit(SeatKey(section.sectionIndex, row, colIndex), rect)
                    x += seatSize
                    colIndex++
                    if (c < groupCount - 1) x += seatGap
                }
                if (groupIdx < section.columnGroups.size - 1) x += aisleGap
            }
        }

        val galleyAbove = Rect(
            bodyLeft + bodyWidth * 0.08f, y - galleyH - 1f,
            bodyRect.right - bodyWidth * 0.08f, y - 1f,
        )
        SectionLayout(
            name = section.name,
            rect = sectionRect,
            seats = seats,
            galleyAbove = galleyAbove,
            galleyLabel = if (section.sectionIndex % 2 == 0) "WC" else "✈",
        ).also { y = sectionRect.bottom + galleyH }
    }

    // Minimap on right edge
    val mmW = w * 0.13f
    val mmMargin = w * 0.025f
    val mmRect = Rect(
        left = w - mmMargin - mmW,
        top = noseHeight * 0.4f,
        right = w - mmMargin,
        bottom = h - tailHeight * 0.4f,
    )

    return PlaneLayout(
        canvasSize = canvasSize,
        noseTip = noseTip,
        bodyRect = bodyRect,
        leftWing = leftWing,
        rightWing = rightWing,
        leftEngine = leftEngine,
        rightEngine = rightEngine,
        leftTailWing = leftTailWing,
        rightTailWing = rightTailWing,
        verticalFin = verticalFin,
        tailCone = tailCone,
        cockpitWindow = cockpitWindow,
        sections = sectionLayouts,
        minimapRect = mmRect,
        cabinRect = cabinRect,
    )
}

// ---------- Drawing ----------

private fun DrawScope.drawPlane(plane: PlaneLayout, colors: FlightSeatColors) {
    // Background
    drawRect(colors.sky, size = size)

    // Wings (drawn first — body sits on top)
    drawPath(plane.leftWing, colors.fuselageHighlight)
    drawPath(plane.leftWing, colors.outline, style = Stroke(width = 2f))
    drawPath(plane.rightWing, colors.fuselageHighlight)
    drawPath(plane.rightWing, colors.outline, style = Stroke(width = 2f))

    // Engines
    drawOval(colors.engine, topLeft = plane.leftEngine.topLeft,
        size = Size(plane.leftEngine.width, plane.leftEngine.height))
    drawOval(colors.outline, topLeft = plane.leftEngine.topLeft,
        size = Size(plane.leftEngine.width, plane.leftEngine.height),
        style = Stroke(width = 2f))
    drawOval(colors.engine, topLeft = plane.rightEngine.topLeft,
        size = Size(plane.rightEngine.width, plane.rightEngine.height))
    drawOval(colors.outline, topLeft = plane.rightEngine.topLeft,
        size = Size(plane.rightEngine.width, plane.rightEngine.height),
        style = Stroke(width = 2f))

    // Tail stabilizers
    drawPath(plane.leftTailWing, colors.fuselageHighlight)
    drawPath(plane.leftTailWing, colors.outline, style = Stroke(width = 2f))
    drawPath(plane.rightTailWing, colors.fuselageHighlight)
    drawPath(plane.rightTailWing, colors.outline, style = Stroke(width = 2f))

    // Tail cone
    drawPath(plane.tailCone, colors.fuselage)
    drawPath(plane.tailCone, colors.outline, style = Stroke(width = 2.5f))

    // Vertical fin (top-down it's a thin elongated diamond)
    drawPath(plane.verticalFin, colors.fuselageHighlight)
    drawPath(plane.verticalFin, colors.outline, style = Stroke(width = 2f))

    // Nose — curved triangle joining noseTip to body top corners
    val nose = Path().apply {
        moveTo(plane.bodyRect.left, plane.bodyRect.top)
        quadraticTo(
            (plane.bodyRect.left + plane.noseTip.x) / 2f, plane.bodyRect.top * 0.25f,
            plane.noseTip.x, plane.noseTip.y,
        )
        quadraticTo(
            (plane.bodyRect.right + plane.noseTip.x) / 2f, plane.bodyRect.top * 0.25f,
            plane.bodyRect.right, plane.bodyRect.top,
        )
        close()
    }
    drawPath(nose, colors.fuselage)
    drawPath(nose, colors.outline, style = Stroke(width = 2.5f))

    // Fuselage body
    val body = Path().apply {
        addRoundRect(
            RoundRect(
                plane.bodyRect.left, plane.bodyRect.top,
                plane.bodyRect.right, plane.bodyRect.bottom,
                cornerRadius = CornerRadius(plane.bodyRect.width * 0.18f),
            ),
        )
    }
    drawPath(body, colors.fuselage)
    drawPath(body, colors.outline, style = Stroke(width = 2.5f))

    // Cockpit window
    drawPath(plane.cockpitWindow, colors.window)
    drawPath(plane.cockpitWindow, colors.outline, style = Stroke(width = 2f))
}

private fun DrawScope.drawSections(
    plane: PlaneLayout,
    state: FlightSeatState,
    colors: FlightSeatColors,
) {
    plane.sections.forEach { section ->
        // Galley/WC band
        drawGalley(section.galleyAbove, section.galleyLabel, colors)
        // Section label — small, on left edge
        drawLabel(
            text = section.name,
            x = plane.bodyRect.left + plane.bodyRect.width * 0.04f,
            y = section.rect.top + 18f,
            color = colors.outline,
            size = 18f,
            bold = true,
        )
        section.seats.forEach { hit ->
            val s = state.stateFor(hit.key)
            drawSeat(hit.rect, s, colors)
        }
    }
}

private fun DrawScope.drawSeat(rect: Rect, state: SeatState, colors: FlightSeatColors) {
    val color = when (state) {
        SeatState.Available -> colors.seatAvailable
        SeatState.Selecting -> colors.seatSelecting
        SeatState.Selected -> colors.seatSelected
    }
    val r = rect.width * 0.22f
    val backrest = RoundRect(
        rect.left, rect.top,
        rect.right, rect.top + rect.height * 0.45f,
        cornerRadius = CornerRadius(r),
    )
    drawPath(Path().apply { addRoundRect(backrest) }, color)
    val base = RoundRect(
        rect.left + rect.width * 0.08f,
        rect.top + rect.height * 0.40f,
        rect.right - rect.width * 0.08f,
        rect.bottom,
        cornerRadius = CornerRadius(r),
    )
    drawPath(Path().apply { addRoundRect(base) }, color)
    // Subtle outline
    drawPath(
        Path().apply { addRoundRect(base) },
        colors.outline.copy(alpha = 0.25f),
        style = Stroke(width = 0.8f),
    )
}

private fun DrawScope.drawGalley(rect: Rect, label: String, colors: FlightSeatColors) {
    val rounded = RoundRect(
        rect.left, rect.top, rect.right, rect.bottom,
        cornerRadius = CornerRadius(rect.height * 0.4f),
    )
    drawPath(Path().apply { addRoundRect(rounded) }, colors.galley)
    drawLabel(
        text = label,
        x = (rect.left + rect.right) / 2f - 7f,
        y = (rect.top + rect.bottom) / 2f + 4f,
        color = colors.galleyText,
        size = 11f,
        bold = true,
    )
}

private fun DrawScope.drawLabel(
    text: String, x: Float, y: Float, color: Color, size: Float = 14f, bold: Boolean = false,
) {
    drawContext.canvas.nativeCanvas.drawText(
        text, x, y,
        android.graphics.Paint().apply {
            this.color = color.toArgbInt()
            isAntiAlias = true
            textSize = size
            typeface = if (bold) android.graphics.Typeface.DEFAULT_BOLD
            else android.graphics.Typeface.DEFAULT
        },
    )
}

private fun Color.toArgbInt(): Int = android.graphics.Color.argb(
    (alpha * 255).toInt(), (red * 255).toInt(), (green * 255).toInt(), (blue * 255).toInt(),
)

private fun DrawScope.drawMinimap(
    plane: PlaneLayout,
    state: FlightSeatState,
    scale: Float,
    offset: Offset,
    canvas: Size,
    colors: FlightSeatColors,
) {
    val mm = plane.minimapRect
    val bg = RoundRect(mm.left, mm.top, mm.right, mm.bottom, CornerRadius(8f))
    drawPath(Path().apply { addRoundRect(bg) }, colors.minimapBackground)

    // Scaled plane silhouette inside minimap
    val scaleX = mm.width * 0.85f / (plane.bodyRect.width * 1.8f) // rough fit
    val scaleY = (mm.height * 0.92f) / plane.canvasSize.height
    val sx = min(scaleX, scaleY)
    val planeW = plane.bodyRect.width * sx
    val planeH = plane.canvasSize.height * sx
    val pLeft = mm.left + (mm.width - planeW) / 2f
    val pTop = mm.top + (mm.height - planeH) / 2f

    // Tiny plane body
    val miniBody = RoundRect(
        pLeft, pTop + planeH * 0.06f,
        pLeft + planeW, pTop + planeH * 0.92f,
        cornerRadius = CornerRadius(3f),
    )
    drawPath(Path().apply { addRoundRect(miniBody) }, colors.minimapPlane)

    // Selecting/Selected dots on the minimap (positioned within sections)
    plane.sections.forEach { section ->
        val sectionBodyTop = pTop + (section.rect.top / plane.canvasSize.height) * planeH
        val sectionBodyH = (section.rect.height / plane.canvasSize.height) * planeH
        section.seats.forEach { hit ->
            val s = state.stateFor(hit.key)
            if (s == SeatState.Available) return@forEach
            // Map seat rect center to minimap
            val cx = pLeft + ((hit.rect.left + hit.rect.width / 2f - plane.bodyRect.left) /
                plane.bodyRect.width) * planeW
            val ratioY = (hit.rect.top + hit.rect.height / 2f - section.rect.top) / section.rect.height
            val cy = sectionBodyTop + ratioY * sectionBodyH
            drawCircle(
                color = if (s == SeatState.Selecting) colors.seatSelecting else colors.minimapDot,
                radius = max(1.4f, sx * hit.rect.width * 0.6f),
                center = Offset(cx, cy),
            )
        }
    }

    // Viewport indicator — the portion of the plane currently visible in the main canvas
    val canvasCenter = Offset(canvas.width / 2f, canvas.height / 2f)
    val visibleHalfW = (canvas.width / 2f) / scale
    val visibleHalfH = (canvas.height / 2f) / scale
    val visibleCenter = Offset(
        canvasCenter.x - offset.x / scale,
        canvasCenter.y - offset.y / scale,
    )
    val viewportLeft = visibleCenter.x - visibleHalfW
    val viewportTop = visibleCenter.y - visibleHalfH
    val viewportRight = visibleCenter.x + visibleHalfW
    val viewportBottom = visibleCenter.y + visibleHalfH

    // Map plane-space viewport rect to minimap space (only y axis matters for vertical scroll)
    val vMinLeft = pLeft + ((viewportLeft - plane.bodyRect.left) / plane.bodyRect.width)
        .coerceIn(0f, 1f) * planeW
    val vMinRight = pLeft + ((viewportRight - plane.bodyRect.left) / plane.bodyRect.width)
        .coerceIn(0f, 1f) * planeW
    val vMinTop = pTop + (viewportTop / plane.canvasSize.height).coerceIn(0f, 1f) * planeH
    val vMinBottom = pTop + (viewportBottom / plane.canvasSize.height).coerceIn(0f, 1f) * planeH

    drawPath(
        Path().apply {
            addRoundRect(RoundRect(vMinLeft, vMinTop, vMinRight, vMinBottom, CornerRadius(2f)))
        },
        colors.minimapViewport.copy(alpha = 0.15f),
    )
    drawPath(
        Path().apply {
            addRoundRect(RoundRect(vMinLeft, vMinTop, vMinRight, vMinBottom, CornerRadius(2f)))
        },
        colors.minimapViewport,
        style = Stroke(width = 1.5f),
    )

    // Outline
    drawPath(
        Path().apply { addRoundRect(bg) },
        colors.outline,
        style = Stroke(width = 1.5f),
    )
}