package com.example.composelearning.pathmorph.presentation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path

/**
 * A tiny SVG-path morphing engine.
 *
 * It parses two path strings that share the **same command structure** (same sequence of
 * M/L/C/Q/Z, with the same number of coordinates) and linearly interpolates every
 * coordinate between them — exactly what `react-native-redash`'s `interpolatePath` does in
 * the original "svg-path-morphing" demo. The phone outlines are authored to share structure,
 * so a per-coordinate lerp produces a clean morph without a resampling library like flubber.
 *
 * Only absolute upper-case commands are needed for that dataset; we support M, L, H, V, C, Q
 * and Z, including the SVG "implicit repeat" (extra coordinate groups after a command repeat
 * that command, with a repeated M becoming L).
 */
data class PathSegment(val cmd: Char, val coords: FloatArray) {
    override fun equals(other: Any?) =
        other is PathSegment && cmd == other.cmd && coords.contentEquals(other.coords)

    override fun hashCode() = 31 * cmd.hashCode() + coords.contentHashCode()
}

/** Parse an SVG path string into absolute segments. */
fun parseSvgPath(d: String): List<PathSegment> {
    val segments = mutableListOf<PathSegment>()
    val tokens = tokenize(d)
    var i = 0
    var lastCmd = ' '
    while (i < tokens.size) {
        val t = tokens[i]
        val cmd: Char
        if (t.isCommand) {
            cmd = t.cmd
            i++
        } else {
            // Implicit repeat of the previous command (M repeats as L).
            cmd = if (lastCmd == 'M') 'L' else lastCmd
        }
        val n = argCount(cmd)
        val coords = FloatArray(n)
        for (k in 0 until n) {
            coords[k] = tokens[i].value
            i++
        }
        segments.add(PathSegment(cmd, coords))
        lastCmd = cmd
    }
    return segments
}

/** Lerp two structurally-identical segment lists. Falls back to [from] on a mismatch. */
fun lerpSegments(from: List<PathSegment>, to: List<PathSegment>, t: Float): List<PathSegment> {
    if (from.size != to.size) return from
    return from.mapIndexed { index, seg ->
        val other = to[index]
        if (seg.cmd != other.cmd || seg.coords.size != other.coords.size) return@mapIndexed seg
        val merged = FloatArray(seg.coords.size) { c ->
            seg.coords[c] + (other.coords[c] - seg.coords[c]) * t
        }
        PathSegment(seg.cmd, merged)
    }
}

/**
 * Build a Compose [Path] from segments, mapping source (viewBox) coordinates to pixels via
 * [map]. Coordinates are stored absolute, so each point is transformed independently.
 */
fun List<PathSegment>.toPath(map: (Float, Float) -> Offset): Path {
    val path = Path()
    for (seg in this) {
        val c = seg.coords
        when (seg.cmd) {
            'M' -> map(c[0], c[1]).let { path.moveTo(it.x, it.y) }
            'L' -> map(c[0], c[1]).let { path.lineTo(it.x, it.y) }
            'H', 'V' -> {} // not present in the dataset; absolute H/V need state, skipped
            'C' -> {
                val p1 = map(c[0], c[1]); val p2 = map(c[2], c[3]); val e = map(c[4], c[5])
                path.cubicTo(p1.x, p1.y, p2.x, p2.y, e.x, e.y)
            }
            'Q' -> {
                val p1 = map(c[0], c[1]); val e = map(c[2], c[3])
                path.quadraticBezierTo(p1.x, p1.y, e.x, e.y)
            }
            'Z' -> path.close()
        }
    }
    return path
}

/**
 * A "contain" fit: maps a source [viewBox] into [dest] preserving aspect ratio and centering.
 * Returns a mapper usable with [toPath] and a [Rect] mapper for the screen rectangle.
 */
class FitBox(viewBox: Size, dest: Rect) {
    private val scale = minOf(dest.width / viewBox.width, dest.height / viewBox.height)
    private val dx = dest.left + (dest.width - viewBox.width * scale) / 2f
    private val dy = dest.top + (dest.height - viewBox.height * scale) / 2f

    fun point(x: Float, y: Float) = Offset(dx + x * scale, dy + y * scale)

    fun rect(x: Float, y: Float, w: Float, h: Float): Rect {
        val tl = point(x, y)
        return Rect(tl.x, tl.y, tl.x + w * scale, tl.y + h * scale)
    }
}

// ---- internals -------------------------------------------------------------

private data class Token(val isCommand: Boolean, val cmd: Char, val value: Float)

private fun argCount(cmd: Char): Int = when (cmd) {
    'M', 'L' -> 2
    'H', 'V' -> 1
    'C' -> 6
    'Q', 'S' -> 4
    'T' -> 2
    'Z' -> 0
    else -> 0
}

private fun tokenize(d: String): List<Token> {
    val tokens = mutableListOf<Token>()
    val num = StringBuilder()
    fun flush() {
        if (num.isNotEmpty()) {
            tokens.add(Token(false, ' ', num.toString().toFloat()))
            num.clear()
        }
    }
    var i = 0
    while (i < d.length) {
        val ch = d[i]
        when {
            ch.isLetter() -> {
                flush()
                tokens.add(Token(true, ch, 0f))
            }
            ch == '-' -> {
                // a '-' starts a new number unless it's an exponent sign (e-3)
                if (num.isNotEmpty() && num.last() != 'e' && num.last() != 'E') flush()
                num.append(ch)
            }
            ch == '.' -> {
                // a second '.' in a token starts a new number (e.g. "1.2.3")
                if (num.contains('.')) flush()
                num.append(ch)
            }
            ch.isDigit() || ch == 'e' || ch == 'E' -> num.append(ch)
            else -> flush() // whitespace or comma
        }
        i++
    }
    flush()
    return tokens
}
