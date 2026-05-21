package com.example.composelearning.flight

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.launch
import kotlin.math.max

private const val MinScale = 1f
private const val MaxScale = 3.5f

/**
 * Compose port of ldoublem/FlightSeat. Visual style follows the GIF screenshots:
 * pale-blue plane (slightly lighter than the sky-blue background) with a smoothly tapered
 * nose, swept wings with sharp pointed tips, small angled engine-pylon triangles hanging
 * from each wing, smaller swept tail wings and a sharp downward-pointing vertical fin at
 * the very bottom. No outline strokes — everything is filled.
 *
 * Seats: outlined squares when available, solid blue when selecting, solid green with a
 * "row,col" label when booked. Pinch to zoom (1x..3.5x), drag to pan. A small red viewport
 * indicator on the left edge shows the visible region.
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
            drawRect(colors.sky, size = size)
            withTransform({
                translate(offsetX.value, offsetY.value)
                scale(scale.value, scale.value, Offset(size.width / 2f, size.height / 2f))
            }) {
                drawPlane(plane, colors)
                drawCabin(plane, state, colors)
            }
            // Viewport indicator (only visible when zoomed in) — drawn in canvas coords, not transformed
            if (scale.value > 1.01f) {
                drawViewportIndicator(scale.value, offsetX.value, offsetY.value, size, colors)
            }
        }
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
    val planeBody: Color,
    val planeWing: Color,
    val engine: Color,
    val galleyFill: Color,
    val galleyOutline: Color,
    val galleyText: Color,
    val seatAvailableOutline: Color,
    val seatAvailableFill: Color,
    val seatSelecting: Color,
    val seatBooked: Color,
    val viewportIndicator: Color,
)

object FlightSeatDefaults {
    fun colors() = FlightSeatColors(
        sky = Color(0xFF80BFDF),
        planeBody = Color(0xFFEFF6FA),
        planeWing = Color(0xFFD2E5EF),
        engine = Color(0xFFA9C7D8),
        galleyFill = Color(0xFFF6FAFC),
        galleyOutline = Color(0xFFD0DCE5),
        galleyText = Color(0xFF7C8B95),
        seatAvailableOutline = Color(0xFFC9D5DD),
        seatAvailableFill = Color(0xFFFFFFFF),
        seatSelecting = Color(0xFF3F8AD6),
        seatBooked = Color(0xFF3FB35F),
        viewportIndicator = Color(0xFFE0492C),
    )

    fun sections(): List<CabinSection> = listOf(
        CabinSection("First", rows = 4, columnGroups = listOf(2, 3, 2), sectionIndex = 0),
        CabinSection("Premium", rows = 3, columnGroups = listOf(2, 3, 2), sectionIndex = 1),
        CabinSection("Economy", rows = 20, columnGroups = listOf(3, 3), sectionIndex = 2),
        CabinSection("Tail", rows = 6, columnGroups = listOf(3, 3), sectionIndex = 3),
    )
}

// ---------- Layout ----------

private data class SectionLayout(
    val name: String,
    val rect: Rect,
    val seats: List<SeatHit>,
    val galleyAbove: GalleyRow,
)

/** Three side-by-side galley boxes (WC | WiFi center | WC) between cabin sections. */
private data class GalleyRow(
    val left: Rect,
    val center: Rect,
    val right: Rect,
)

private data class PlaneLayout(
    val canvasSize: Size,
    val bodyPath: Path,
    val leftWing: Path,
    val rightWing: Path,
    val leftPylons: List<Path>,
    val rightPylons: List<Path>,
    val leftTailWing: Path,
    val rightTailWing: Path,
    val verticalFin: Path,
    val bodyRect: Rect,
    val sections: List<SectionLayout>,
)

private fun PlaneLayout.allSeatHits(): List<SeatHit> = sections.flatMap { it.seats }

private fun computePlaneLayout(
    canvasSize: Size,
    sections: List<CabinSection>,
): PlaneLayout {
    val w = canvasSize.width
    val h = canvasSize.height
    val midX = w / 2f

    // --- Fuselage: smooth capsule with gradual nose taper, no separate neck ---
    val bodyHalfW = w * 0.080f
    val bodyTop = h * 0.060f      // tip of nose
    val bodyBottom = h * 0.905f   // where tail starts narrowing into fin
    val shoulderY = h * 0.155f    // body reaches full width here
    val hipY = h * 0.84f          // body still full width here
    val tailHalfW = bodyHalfW * 0.55f

    val bodyRect = Rect(midX - bodyHalfW, shoulderY, midX + bodyHalfW, hipY)

    val bodyPath = Path().apply {
        // Start at nose tip — clockwise
        moveTo(midX, bodyTop)
        cubicTo(
            midX + bodyHalfW * 0.55f, bodyTop + (shoulderY - bodyTop) * 0.25f,
            midX + bodyHalfW * 0.95f, bodyTop + (shoulderY - bodyTop) * 0.65f,
            midX + bodyHalfW, shoulderY,
        )
        lineTo(midX + bodyHalfW, hipY)
        cubicTo(
            midX + bodyHalfW * 0.95f, hipY + (bodyBottom - hipY) * 0.40f,
            midX + bodyHalfW * 0.85f, bodyBottom - 4f,
            midX + tailHalfW, bodyBottom,
        )
        // Across bottom (tail will draw the rest of the fin shape)
        lineTo(midX - tailHalfW, bodyBottom)
        cubicTo(
            midX - bodyHalfW * 0.85f, bodyBottom - 4f,
            midX - bodyHalfW * 0.95f, hipY + (bodyBottom - hipY) * 0.40f,
            midX - bodyHalfW, hipY,
        )
        lineTo(midX - bodyHalfW, shoulderY)
        cubicTo(
            midX - bodyHalfW * 0.95f, bodyTop + (shoulderY - bodyTop) * 0.65f,
            midX - bodyHalfW * 0.55f, bodyTop + (shoulderY - bodyTop) * 0.25f,
            midX, bodyTop,
        )
        close()
    }

    // --- Main wings: cranked trailing edge, sharp tips ---
    val wingRootTopY = h * 0.38f
    val wingRootBottomY = h * 0.50f
    val wingTipX = w * 0.99f
    val wingTipY = h * 0.66f       // leading-edge tip
    val wingTrailingX = w * 0.86f  // trailing edge crank
    val wingTrailingY = h * 0.65f

    val rightWing = Path().apply {
        moveTo(midX + bodyHalfW * 0.35f, wingRootTopY)
        // Leading edge — straight to sharp tip
        lineTo(wingTipX, wingTipY)
        // Tip — tiny curve so it isn't an infinitely sharp pixel
        quadraticTo(wingTipX + 4f, wingTipY + 5f, wingTipX - 4f, wingTipY + 6f)
        // Trailing edge — cranked: out to (wingTrailingX, wingTrailingY) then back to root
        lineTo(wingTrailingX, wingTrailingY)
        lineTo(midX + bodyHalfW * 0.35f, wingRootBottomY)
        close()
    }
    val leftWing = Path().apply {
        moveTo(midX - bodyHalfW * 0.35f, wingRootTopY)
        lineTo(w - wingTipX, wingTipY)
        quadraticTo(w - wingTipX - 4f, wingTipY + 5f, w - wingTipX + 4f, wingTipY + 6f)
        lineTo(w - wingTrailingX, wingTrailingY)
        lineTo(midX - bodyHalfW * 0.35f, wingRootBottomY)
        close()
    }

    // --- Engine pylons: small angled triangles hanging off each wing's trailing edge ---
    // 2 per wing, at ~40% and ~70% along the wing length.
    val pylonW = bodyHalfW * 0.55f
    val pylonH = bodyHalfW * 1.6f
    fun pylonAt(rootX: Float, rootY: Float, tipDx: Float): Path = Path().apply {
        moveTo(rootX - pylonW * 0.45f, rootY)
        lineTo(rootX + pylonW * 0.45f, rootY)
        lineTo(rootX + tipDx, rootY + pylonH)
        close()
    }
    fun engineCenter(t: Float, leftSide: Boolean): Offset {
        // t along trailing edge from root to crank
        val rootX = if (leftSide) midX - bodyHalfW * 0.35f else midX + bodyHalfW * 0.35f
        val rootY = wingRootBottomY
        val tipX = if (leftSide) w - wingTrailingX else wingTrailingX
        val tipY = wingTrailingY
        return Offset(rootX + (tipX - rootX) * t, rootY + (tipY - rootY) * t)
    }
    val leftPylons = listOf(0.35f, 0.70f).map { t ->
        val c = engineCenter(t, leftSide = true)
        pylonAt(c.x, c.y, tipDx = -pylonW * 0.35f)
    }
    val rightPylons = listOf(0.35f, 0.70f).map { t ->
        val c = engineCenter(t, leftSide = false)
        pylonAt(c.x, c.y, tipDx = pylonW * 0.35f)
    }

    // --- Tail wings: smaller swept triangles with the same cranked shape ---
    val tailWingRootTopY = h * 0.83f
    val tailWingRootBottomY = h * 0.88f
    val tailWingTipX = w * 0.78f
    val tailWingTipY = h * 0.93f
    val tailWingTrailingX = w * 0.70f
    val tailWingTrailingY = h * 0.92f
    val rightTailWing = Path().apply {
        moveTo(midX + bodyHalfW * 0.30f, tailWingRootTopY)
        lineTo(tailWingTipX, tailWingTipY)
        quadraticTo(tailWingTipX + 3f, tailWingTipY + 3f, tailWingTipX - 3f, tailWingTipY + 4f)
        lineTo(tailWingTrailingX, tailWingTrailingY)
        lineTo(midX + bodyHalfW * 0.30f, tailWingRootBottomY)
        close()
    }
    val leftTailWing = Path().apply {
        moveTo(midX - bodyHalfW * 0.30f, tailWingRootTopY)
        lineTo(w - tailWingTipX, tailWingTipY)
        quadraticTo(w - tailWingTipX - 3f, tailWingTipY + 3f, w - tailWingTipX + 3f, tailWingTipY + 4f)
        lineTo(w - tailWingTrailingX, tailWingTrailingY)
        lineTo(midX - bodyHalfW * 0.30f, tailWingRootBottomY)
        close()
    }

    // --- Vertical fin: sharp downward-pointing triangle below the tail wings ---
    val verticalFin = Path().apply {
        val finTopY = bodyBottom - 4f
        val finBottomY = h * 0.985f
        val finHalfW = bodyHalfW * 0.55f
        moveTo(midX - finHalfW, finTopY)
        lineTo(midX + finHalfW, finTopY)
        lineTo(midX, finBottomY)
        close()
    }

    // --- Cabin: galley rows + section seat grids inside body ---
    val galleyH = h * 0.026f
    val cabinTop = shoulderY + bodyHalfW * 0.7f
    val cabinBottom = hipY - bodyHalfW * 0.3f
    val totalRows = sections.sumOf { it.rows }
    val rowH = (cabinBottom - cabinTop - galleyH * (sections.size + 1)) / totalRows

    val maxCols = sections.maxOf { it.columnGroups.sum() }
    val maxAisles = sections.first { it.columnGroups.sum() == maxCols }.columnGroups.size - 1
    val cabinW = bodyHalfW * 2f * 0.84f
    val seatSize = cabinW / (maxCols + 0.7f)
    val seatGap = seatSize * 0.10f
    val aisleGap = seatSize * 0.45f

    var y = cabinTop + galleyH
    val sectionLayouts = sections.map { section ->
        val sectionH = section.rows * rowH
        val sectionRect = Rect(midX - bodyHalfW * 0.9f, y, midX + bodyHalfW * 0.9f, y + sectionH)
        val seats = mutableListOf<SeatHit>()
        val totalCols = section.columnGroups.sum()
        val numAisles = section.columnGroups.size - 1
        val rowWidth = totalCols * seatSize +
            (totalCols - 1 - numAisles) * seatGap +
            numAisles * aisleGap
        val startX = midX - rowWidth / 2f

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

        // Galley row above this section: three boxes (WC | WiFi | WC)
        val galleyBoxH = galleyH * 1.6f
        val galleyTop = y - galleyBoxH - 2f
        val sideW = bodyHalfW * 0.45f
        val centerW = bodyHalfW * 0.75f
        val gap = bodyHalfW * 0.07f
        val totalW = sideW * 2 + centerW + gap * 2
        val gStartX = midX - totalW / 2f
        val left = Rect(gStartX, galleyTop, gStartX + sideW, galleyTop + galleyBoxH)
        val center = Rect(left.right + gap, galleyTop,
            left.right + gap + centerW, galleyTop + galleyBoxH)
        val right = Rect(center.right + gap, galleyTop,
            center.right + gap + sideW, galleyTop + galleyBoxH)

        SectionLayout(
            name = section.name,
            rect = sectionRect,
            seats = seats,
            galleyAbove = GalleyRow(left, center, right),
        ).also { y = sectionRect.bottom + galleyH }
    }

    return PlaneLayout(
        canvasSize = canvasSize,
        bodyPath = bodyPath,
        leftWing = leftWing,
        rightWing = rightWing,
        leftPylons = leftPylons,
        rightPylons = rightPylons,
        leftTailWing = leftTailWing,
        rightTailWing = rightTailWing,
        verticalFin = verticalFin,
        bodyRect = bodyRect,
        sections = sectionLayouts,
    )
}

// ---------- Drawing ----------

private fun DrawScope.drawPlane(plane: PlaneLayout, colors: FlightSeatColors) {
    // Wings (back layer)
    drawPath(plane.leftWing, colors.planeWing)
    drawPath(plane.rightWing, colors.planeWing)
    // Engine pylons on top of wings
    plane.leftPylons.forEach { drawPath(it, colors.engine) }
    plane.rightPylons.forEach { drawPath(it, colors.engine) }
    // Tail wings
    drawPath(plane.leftTailWing, colors.planeWing)
    drawPath(plane.rightTailWing, colors.planeWing)
    // Vertical fin sticks down past the body bottom
    drawPath(plane.verticalFin, colors.planeWing)
    // Body — drawn last so it covers the wing roots cleanly
    drawPath(plane.bodyPath, colors.planeBody)
}

private fun DrawScope.drawCabin(
    plane: PlaneLayout,
    state: FlightSeatState,
    colors: FlightSeatColors,
) {
    plane.sections.forEach { section ->
        drawGalleyRow(section.galleyAbove, colors)
        section.seats.forEach { hit ->
            val s = state.stateFor(hit.key)
            drawSeat(hit.rect, s, hit.key, colors)
        }
    }
}

private fun DrawScope.drawSeat(rect: Rect, state: SeatState, key: SeatKey, colors: FlightSeatColors) {
    val radius = rect.width * 0.22f
    val path = Path().apply {
        addRoundRect(
            RoundRect(
                rect.left, rect.top, rect.right, rect.bottom,
                cornerRadius = CornerRadius(radius),
            ),
        )
    }
    when (state) {
        SeatState.Available -> {
            drawPath(path, colors.seatAvailableFill)
            drawPath(path, colors.seatAvailableOutline, style = Stroke(width = 1f))
        }
        SeatState.Selecting -> drawPath(path, colors.seatSelecting)
        SeatState.Selected -> {
            drawPath(path, colors.seatBooked)
            // Row,col label only readable when zoomed; tiny text is fine at scale 1
            drawText(
                text = "${key.row + 1},${key.column + 1}",
                x = (rect.left + rect.right) / 2f,
                y = (rect.top + rect.bottom) / 2f + rect.height * 0.18f,
                color = Color.White,
                size = rect.height * 0.45f,
                centerX = true,
                bold = true,
            )
        }
    }
}

private fun DrawScope.drawGalleyRow(row: GalleyRow, colors: FlightSeatColors) {
    drawGalleyBox(row.left, "WC", colors)
    drawGalleyWifi(row.center, colors)
    drawGalleyBox(row.right, "WC", colors)
}

private fun DrawScope.drawGalleyBox(rect: Rect, label: String, colors: FlightSeatColors) {
    val r = rect.height * 0.18f
    val rounded = Path().apply {
        addRoundRect(
            RoundRect(rect.left, rect.top, rect.right, rect.bottom, cornerRadius = CornerRadius(r)),
        )
    }
    drawPath(rounded, colors.galleyFill)
    drawPath(rounded, colors.galleyOutline, style = Stroke(width = 1f))
    drawText(
        text = label,
        x = (rect.left + rect.right) / 2f,
        y = (rect.top + rect.bottom) / 2f + rect.height * 0.18f,
        color = colors.galleyText,
        size = rect.height * 0.42f,
        centerX = true,
        bold = true,
    )
}

private fun DrawScope.drawGalleyWifi(rect: Rect, colors: FlightSeatColors) {
    val r = rect.height * 0.18f
    val rounded = Path().apply {
        addRoundRect(
            RoundRect(rect.left, rect.top, rect.right, rect.bottom, cornerRadius = CornerRadius(r)),
        )
    }
    drawPath(rounded, colors.galleyFill)
    drawPath(rounded, colors.galleyOutline, style = Stroke(width = 1f))
    // Wifi icon — 3 nested arcs + dot. Drawn via native canvas for arcs.
    val cx = (rect.left + rect.right) / 2f
    val cy = rect.top + rect.height * 0.62f
    val paint = android.graphics.Paint().apply {
        color = colors.galleyText.toArgbInt()
        isAntiAlias = true
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = max(1f, rect.height * 0.07f)
        strokeCap = android.graphics.Paint.Cap.ROUND
    }
    val nc = drawContext.canvas.nativeCanvas
    val maxR = rect.height * 0.32f
    for (i in 0 until 3) {
        val rr = maxR * (1f - i * 0.3f)
        nc.drawArc(
            android.graphics.RectF(cx - rr, cy - rr, cx + rr, cy + rr),
            -135f, 90f, false, paint,
        )
    }
    paint.style = android.graphics.Paint.Style.FILL
    nc.drawCircle(cx, cy + rect.height * 0.06f, rect.height * 0.05f, paint)
}

private fun DrawScope.drawViewportIndicator(
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    canvas: Size,
    colors: FlightSeatColors,
) {
    // Strip on left edge showing where the viewport sits relative to the whole plane.
    val stripW = canvas.width * 0.022f
    val stripLeft = canvas.width * 0.015f
    val stripTop = canvas.height * 0.08f
    val stripBottom = canvas.height * 0.90f
    val stripHeight = stripBottom - stripTop
    // Map current vertical viewport position (in plane-space) to strip.
    val visibleH = canvas.height / scale
    val visibleCenterY = canvas.height / 2f - offsetY / scale
    val visibleTop = (visibleCenterY - visibleH / 2f).coerceIn(0f, canvas.height)
    val visibleBottom = (visibleCenterY + visibleH / 2f).coerceIn(0f, canvas.height)
    val indTop = stripTop + (visibleTop / canvas.height) * stripHeight
    val indBottom = stripTop + (visibleBottom / canvas.height) * stripHeight
    drawPath(
        Path().apply {
            addRoundRect(
                RoundRect(
                    stripLeft, indTop, stripLeft + stripW, indBottom,
                    cornerRadius = CornerRadius(2f),
                ),
            )
        },
        colors.viewportIndicator,
        style = Stroke(width = 2f),
    )
}

private fun DrawScope.drawText(
    text: String,
    x: Float,
    y: Float,
    color: Color,
    size: Float,
    centerX: Boolean = false,
    bold: Boolean = false,
) {
    val paint = android.graphics.Paint().apply {
        this.color = color.toArgbInt()
        isAntiAlias = true
        textSize = size
        typeface = if (bold) android.graphics.Typeface.DEFAULT_BOLD
        else android.graphics.Typeface.DEFAULT
        if (centerX) textAlign = android.graphics.Paint.Align.CENTER
    }
    drawContext.canvas.nativeCanvas.drawText(text, x, y, paint)
}

private fun Color.toArgbInt(): Int = android.graphics.Color.argb(
    (alpha * 255).toInt(),
    (red * 255).toInt(),
    (green * 255).toInt(),
    (blue * 255).toInt(),
)