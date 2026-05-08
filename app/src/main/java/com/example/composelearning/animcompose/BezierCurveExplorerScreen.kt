package com.example.composelearning.animcompose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private fun lerp(a: Offset, b: Offset, t: Float): Offset =
    Offset(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t)

@Composable
fun BezierCurveExplorerScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Bezier Curves",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "A bezier curve is a smooth path defined by anchor points (where it starts and ends) and control points (which pull the curve in their direction without ever touching it). Drag the colored handles in each playground to see how the control points shape the curve.",
            style = MaterialTheme.typography.bodyMedium
        )

        SectionHeader("1. Quadratic Bezier — 1 control point")
        QuadraticPlayground()

        SectionHeader("2. Cubic Bezier — 2 control points")
        CubicPlayground()

        SectionHeader("3. From Figma to Compose")
        FigmaToComposeSection()

        SectionHeader("4. Are control points 'given' by Figma?")
        StrategiesSection()

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun QuadraticPlayground() {
    var p0 by remember { mutableStateOf(Offset(120f, 600f)) }
    var p1 by remember { mutableStateOf(Offset(450f, 100f)) }
    var p2 by remember { mutableStateOf(Offset(780f, 600f)) }
    var t by remember { mutableFloatStateOf(0.5f) }
    var dragging by remember { mutableStateOf<Int?>(null) }
    val touchRadius = 60f

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp)
                .background(Color(0xFFF5F6FA), RoundedCornerShape(12.dp))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            dragging = when {
                                (offset - p0).getDistance() < touchRadius -> 0
                                (offset - p1).getDistance() < touchRadius -> 1
                                (offset - p2).getDistance() < touchRadius -> 2
                                else -> null
                            }
                        },
                        onDrag = { change, _ ->
                            when (dragging) {
                                0 -> p0 = change.position
                                1 -> p1 = change.position
                                2 -> p2 = change.position
                            }
                        },
                        onDragEnd = { dragging = null },
                        onDragCancel = { dragging = null }
                    )
                }
        ) {
            val dash = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
            drawLine(Color.Gray, p0, p1, 2.dp.toPx(), pathEffect = dash)
            drawLine(Color.Gray, p1, p2, 2.dp.toPx(), pathEffect = dash)

            val path = Path().apply {
                moveTo(p0.x, p0.y)
                quadraticTo(p1.x, p1.y, p2.x, p2.y)
            }
            drawPath(path, Color(0xFF3B5BFE), style = Stroke(width = 4.dp.toPx()))

            val a = lerp(p0, p1, t)
            val b = lerp(p1, p2, t)
            val pointOnCurve = lerp(a, b, t)
            drawLine(Color(0xFFFFA000), a, b, 3.dp.toPx())
            drawCircle(Color(0xFFFFA000), 8.dp.toPx(), a)
            drawCircle(Color(0xFFFFA000), 8.dp.toPx(), b)
            drawCircle(Color(0xFFE53935), 12.dp.toPx(), pointOnCurve)

            drawCircle(Color(0xFF1A73E8), 14.dp.toPx(), p0)
            drawCircle(Color(0xFFEA4335), 14.dp.toPx(), p1)
            drawCircle(Color(0xFF1A73E8), 14.dp.toPx(), p2)
        }

        Text("t = ${"%.2f".format(t)}  (drag to scrub the point along the curve)")
        Slider(value = t, onValueChange = { t = it })

        InfoBlock(
            "Blue dots are anchors P0 and P2. Red dot is the control point P1. The dashed line is the 'control polygon'. The orange segment shows De Casteljau's construction at the current t: A = lerp(P0, P1, t), B = lerp(P1, P2, t), and the curve point (red) is lerp(A, B, t). Compose API: path.quadraticTo(c1x, c1y, x, y)."
        )
    }
}

@Composable
private fun CubicPlayground() {
    var p0 by remember { mutableStateOf(Offset(80f, 600f)) }
    var p1 by remember { mutableStateOf(Offset(250f, 80f)) }
    var p2 by remember { mutableStateOf(Offset(650f, 80f)) }
    var p3 by remember { mutableStateOf(Offset(820f, 600f)) }
    var t by remember { mutableFloatStateOf(0.5f) }
    var dragging by remember { mutableStateOf<Int?>(null) }
    val touchRadius = 60f

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp)
                .background(Color(0xFFF5F6FA), RoundedCornerShape(12.dp))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            dragging = when {
                                (offset - p0).getDistance() < touchRadius -> 0
                                (offset - p1).getDistance() < touchRadius -> 1
                                (offset - p2).getDistance() < touchRadius -> 2
                                (offset - p3).getDistance() < touchRadius -> 3
                                else -> null
                            }
                        },
                        onDrag = { change, _ ->
                            when (dragging) {
                                0 -> p0 = change.position
                                1 -> p1 = change.position
                                2 -> p2 = change.position
                                3 -> p3 = change.position
                            }
                        },
                        onDragEnd = { dragging = null },
                        onDragCancel = { dragging = null }
                    )
                }
        ) {
            val dash = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
            drawLine(Color.Gray, p0, p1, 2.dp.toPx(), pathEffect = dash)
            drawLine(Color.Gray, p1, p2, 2.dp.toPx(), pathEffect = dash)
            drawLine(Color.Gray, p2, p3, 2.dp.toPx(), pathEffect = dash)

            val path = Path().apply {
                moveTo(p0.x, p0.y)
                cubicTo(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y)
            }
            drawPath(path, Color(0xFF3B5BFE), style = Stroke(width = 4.dp.toPx()))

            val a = lerp(p0, p1, t)
            val b = lerp(p1, p2, t)
            val c = lerp(p2, p3, t)
            val d = lerp(a, b, t)
            val e = lerp(b, c, t)
            val point = lerp(d, e, t)

            drawLine(Color(0xFFFFA000), a, b, 3.dp.toPx())
            drawLine(Color(0xFFFFA000), b, c, 3.dp.toPx())
            drawLine(Color(0xFF8E24AA), d, e, 3.dp.toPx())
            drawCircle(Color(0xFFFFA000), 6.dp.toPx(), a)
            drawCircle(Color(0xFFFFA000), 6.dp.toPx(), b)
            drawCircle(Color(0xFFFFA000), 6.dp.toPx(), c)
            drawCircle(Color(0xFF8E24AA), 8.dp.toPx(), d)
            drawCircle(Color(0xFF8E24AA), 8.dp.toPx(), e)
            drawCircle(Color(0xFFE53935), 12.dp.toPx(), point)

            drawCircle(Color(0xFF1A73E8), 14.dp.toPx(), p0)
            drawCircle(Color(0xFFEA4335), 14.dp.toPx(), p1)
            drawCircle(Color(0xFFEA4335), 14.dp.toPx(), p2)
            drawCircle(Color(0xFF1A73E8), 14.dp.toPx(), p3)
        }

        Text("t = ${"%.2f".format(t)}")
        Slider(value = t, onValueChange = { t = it })

        InfoBlock(
            "Cubic adds a second control point, giving you S-curves and more sculpted shapes than a quadratic can express. Three lerp 'levels' run in parallel: orange between consecutive segments, purple between those, and finally the red point on the curve. Compose API: path.cubicTo(c1x, c1y, c2x, c2y, x, y)."
        )
    }
}

@Composable
private fun FigmaToComposeSection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "When a designer hands you a curve, the most reliable workflow is: 'Copy as SVG' from Figma, read the path commands, then translate each one into a Compose Path call. Below is a curved card header — what Figma exports, what you write in Compose, and what renders on screen.",
            style = MaterialTheme.typography.bodyMedium
        )

        CodeBlock(
            title = "Figma export — SVG path (design size 360 x 200)",
            code = """
                <path d="
                  M 0   0
                  H 360
                  V 140
                  C 270 200,  90  80,  0 140
                  Z" fill="#3B5BFE"/>
            """.trimIndent()
        )

        Text(
            text = "How to read it: M = moveTo, H = horizontal line, V = vertical line, C = cubicTo with two control points then the end anchor, Z = close. The two control points sit at (270, 200) and (90, 80) — those are the values pulling the bottom edge into a wave.",
            style = MaterialTheme.typography.bodySmall
        )

        CodeBlock(
            title = "Compose code (size-relative — works at any width)",
            code = """
                val w = size.width
                val h = size.height
                val path = Path().apply {
                  moveTo(0f, 0f)            // M 0 0
                  lineTo(w, 0f)             // H 360
                  lineTo(w, h * 0.70f)      // V 140  (140/200)
                  cubicTo(
                    w * 0.75f, h,           // 270/360, 200/200
                    w * 0.25f, h * 0.40f,   //  90/360,  80/200
                    0f, h * 0.70f           // end anchor
                  )
                  close()
                }
                drawPath(path, color)
            """.trimIndent()
        )

        Text(
            text = "Live render (red dots show where the SVG control points landed):",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        FigmaCurveDemo()
    }
}

@Composable
private fun FigmaCurveDemo() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(Color(0xFFF5F6FA), RoundedCornerShape(12.dp))
    ) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(w, 0f)
            lineTo(w, h * 0.70f)
            cubicTo(
                w * 0.75f, h,
                w * 0.25f, h * 0.40f,
                0f, h * 0.70f
            )
            close()
        }
        drawPath(
            path,
            Brush.horizontalGradient(listOf(Color(0xFF3B5BFE), Color(0xFF8E24AA)))
        )

        drawCircle(Color(0xFFEA4335), 7.dp.toPx(), Offset(w * 0.75f, h))
        drawCircle(Color(0xFFEA4335), 7.dp.toPx(), Offset(w * 0.25f, h * 0.40f))
    }
}

@Composable
private fun StrategiesSection() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Strategy(
            title = "Yes — control points ARE in the SVG export.",
            body = "Each 'C cx1 cy1  cx2 cy2  x y' command in the SVG path lists the two control points and the end anchor in order. Copy the numbers, divide by the design size to get fractions of width/height, and feed them into cubicTo. This is the most accurate path, especially for organic or hand-drawn shapes."
        )
        Strategy(
            title = "Eyeball + experiment — when no SVG is available.",
            body = "If you only have a screenshot, place the anchors at the visible curve endpoints and drop control points roughly along the tangent direction at each end. Then tweak with the playgrounds above until it matches. For simple curves this is faster than it sounds."
        )
        Strategy(
            title = "Use math — for curves with a known formula.",
            body = "A wave, an arc, a smile, or a parabola has a closed-form expression. For a sine wave you can avoid bezier entirely and walk x with lineTo(x, sin(x)). For a circular arc, use arcTo. Bezier is only worth reaching for when the shape is hand-drawn or stylized."
        )
        InfoBlock(
            "Rule of thumb: ask the designer for the SVG. From a Figma file, right-click the path layer in the Layers panel and choose 'Copy/Paste as → Copy as SVG' — the clipboard contains the exact path commands shown above. If you cannot get the SVG (e.g. only a JPEG handoff), default to the eyeball approach."
        )
    }
}

@Composable
private fun Strategy(title: String, body: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(text = body, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun CodeBlock(title: String, code: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
        Surface(
            color = Color(0xFF1E1E2E),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        ) {
            Text(
                text = code,
                color = Color(0xFFE0E0F0),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Composable
private fun InfoBlock(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(12.dp)
        )
    }
}