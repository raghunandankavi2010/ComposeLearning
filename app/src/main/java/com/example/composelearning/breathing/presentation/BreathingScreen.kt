package com.example.composelearning.breathing.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.composelearning.breathing.domain.model.BreathingSession
import kotlin.math.floor
import kotlin.math.sin

// Ease-in-out used by the original to swell the waves up and down.
private val WaveEasing = CubicBezierEasing(0.37f, 0f, 0.63f, 1f)

// Cubic-bezier constant that turns four cubics into a circle.
private const val CIRCLE_C = 0.55228474983079f
private const val WOBBLE = 0.18f // how much the blob's control points breathe (orig A = 0.2)

@Composable
fun BreathingScreen(
    viewModel: BreathingViewModel = viewModel(factory = BreathingViewModel.Factory())
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val session = state.session
    if (state.isLoading || session == null) {
        Box(Modifier.fillMaxSize().background(Color(0xFF60D1B9)), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color.White)
        }
        return
    }
    BreathingScene(
        session = session,
        isPlaying = state.isPlaying,
        onToggle = { viewModel.onIntent(BreathingIntent.TogglePlay) }
    )
}

@Composable
private fun BreathingScene(
    session: BreathingSession,
    isPlaying: Boolean,
    onToggle: () -> Unit
) {
    val clock = rememberClockMillis()

    // play (0) ↔ pause (1) morph for the centre button.
    val morph = remember { Animatable(0f) }
    androidx.compose.runtime.LaunchedEffect(isPlaying) {
        morph.animateTo(if (isPlaying) 1f else 0f, tween(450))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) { detectTapGestures { onToggle() } }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val t = clock.value
            drawWaves(session, t)
            drawBlob(t)
            drawPlayPause(morph.value)
        }

        Text(
            text = session.title,
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 96.dp)
        )
    }
}

// ---- drawing ---------------------------------------------------------------

private fun DrawScope.drawWaves(session: BreathingSession, t: Float) {
    drawRect(Color(session.baseColor))
    // Three layered hills, each swelling on its own period.
    drawHill(Color(session.wave1), start = mix(eased(t, 4100f), center().y - 300f, 200f), h = mix(eased(t, 4100f), 50f, 60f))
    drawHill(Color(session.wave2), start = mix(eased(t, 4000f), center().y - 150f, center().y), h = mix(eased(t, 4000f), 40f, 60f))
    drawHill(Color(session.wave3), start = mix(eased(t, 3800f), center().y + 75f, center().y + 225f), h = mix(eased(t, 3800f), 30f, 50f))
}

/** One wave: a quadratic crest at `start - h`, filled down to the bottom of the canvas. */
private fun DrawScope.drawHill(color: Color, start: Float, h: Float) {
    val w = size.width
    val path = Path().apply {
        moveTo(0f, start)
        quadraticBezierTo(w / 2f, start - h, w, start)
        lineTo(w, size.height)
        lineTo(0f, size.height)
        close()
    }
    drawPath(path, color)
}

/** The dark, gently morphing + rotating blob behind the play button. */
private fun DrawScope.drawBlob(t: Float) {
    val c = center()
    val r = size.minDimension * 0.13f
    // Four control constants wobble out of phase (a cheap stand-in for 2D noise).
    val c1 = CIRCLE_C + WOBBLE * noise(t, 0f)
    val c2 = CIRCLE_C + WOBBLE * noise(t, 1.7f)
    val c3 = CIRCLE_C + WOBBLE * noise(t, 3.1f)
    val c4 = CIRCLE_C + WOBBLE * noise(t, 4.6f)

    val path = Path().apply {
        moveTo(c.x, c.y - r)
        cubicTo(c.x + c1 * r, c.y - r, c.x + r, c.y - c1 * r, c.x + r, c.y)
        cubicTo(c.x + r, c.y + c2 * r, c.x + c2 * r, c.y + r, c.x, c.y + r)
        cubicTo(c.x - c3 * r, c.y + r, c.x - r, c.y + c3 * r, c.x - r, c.y)
        cubicTo(c.x - r, c.y - c4 * r, c.x - c4 * r, c.y - r, c.x, c.y - r)
        close()
    }
    val angleDeg = Math.toDegrees((t / 2000.0)).toFloat()
    rotate(degrees = angleDeg, pivot = c) {
        drawPath(path, Color(0xFF3B3A3A))
    }
}

/**
 * Parametric play↔pause morph (no SVG strings). The triangle is split into a left and a
 * right quad; each quad lerps its 4 vertices between the play shape (`p=0`) and a pause bar
 * (`p=1`). Drawn white, centred on the blob.
 */
private fun DrawScope.drawPlayPause(p: Float) {
    val c = center()
    val s = size.minDimension * 0.10f // half-size of the icon box
    val w = s * 2f
    val h = s * 2f
    val ox = c.x - s
    val oy = c.y - s
    fun pt(x: Float, y: Float) = Offset(ox + x * w, oy + y * h)
    fun lerp(a: Offset, b: Offset) = Offset(a.x + (b.x - a.x) * p, a.y + (b.y - a.y) * p)

    // Left piece: play left-half → left pause bar.
    val left = listOf(
        lerp(pt(0.16f, 0.08f), pt(0.16f, 0f)),
        lerp(pt(0.50f, 0.29f), pt(0.40f, 0f)),
        lerp(pt(0.50f, 0.71f), pt(0.40f, 1f)),
        lerp(pt(0.16f, 0.92f), pt(0.16f, 1f))
    )
    // Right piece: play right-half (triangle tip) → right pause bar.
    val right = listOf(
        lerp(pt(0.50f, 0.29f), pt(0.60f, 0f)),
        lerp(pt(0.92f, 0.50f), pt(0.84f, 0f)),
        lerp(pt(0.92f, 0.50f), pt(0.84f, 1f)),
        lerp(pt(0.50f, 0.71f), pt(0.60f, 1f))
    )
    drawQuad(left)
    drawQuad(right)
}

private fun DrawScope.drawQuad(points: List<Offset>) {
    val path = Path().apply {
        moveTo(points[0].x, points[0].y)
        for (i in 1 until points.size) lineTo(points[i].x, points[i].y)
        close()
    }
    drawPath(path, Color.White)
}

// ---- maths helpers ---------------------------------------------------------

private fun DrawScope.center() = Offset(size.width / 2f, size.height / 2f)

/** Skia-style mix: returns a → b as `value` goes 0 → 1. */
private fun mix(value: Float, a: Float, b: Float) = a + (b - a) * value

/** Triangle ping-pong 0→1→0 over `dur` ms, then run through the ease-in-out curve. */
private fun eased(t: Float, dur: Float): Float {
    val p = (t % dur) / dur
    val iteration = floor(t / dur).toInt()
    val goingBack = iteration % 2 == 0
    val progress = if (goingBack) 1f - p else p
    return WaveEasing.transform(progress)
}

/** Cheap pseudo-noise in [-1, 1]: two slow sines out of phase. */
private fun noise(t: Float, phase: Float): Float {
    val a = sin(t / 1700f + phase)
    val b = sin(t / 2600f + phase * 1.7f + 1.3f)
    return (a + b) / 2f
}

@Composable
private fun rememberClockMillis(): State<Float> = produceState(0f) {
    val start = withFrameNanos { it }
    while (true) {
        withFrameNanos { now -> value = (now - start) / 1_000_000f }
    }
}
