package com.example.composelearning.server

import androidx.compose.remote.core.operations.paint.PaintBundle
import androidx.compose.remote.creation.JvmRcPlatformServices
import androidx.compose.remote.creation.RemoteComposeContext
import java.awt.Color as AwtColor
import java.awt.GradientPaint
import java.awt.Polygon
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Builds RemoteCompose documents **on the server** and returns their bytes.
 *
 * The point of the demo: the UI is authored here in plain JVM Kotlin with the
 * `androidx.compose.remote` creation API — NOT in the Android app — encoded to a
 * portable binary buffer, and shipped over HTTP. The Android client only *plays*
 * it; it has no idea whether it will get a list, an image, a dropdown or a card.
 *
 * Each variant is a structurally different layout, which is what makes this
 * genuinely server-driven: the server decides the entire screen.
 */
object RemoteUiDocument {

    private const val W = 400
    private const val H = 620
    private const val WIDTH = W.toFloat()
    private const val HEIGHT = H.toFloat()

    private const val ACCENT = 0xFF6C5CE7.toInt()
    private const val WHITE = 0xFFFFFFFF.toInt()
    private const val BG = 0xFF14151D.toInt()

    private val variantNames = listOf("List", "Image", "Dropdown", "Card")
    fun variantCount(): Int = variantNames.size
    fun variantName(v: Int): String = variantNames[norm(v)]

    private fun norm(v: Int) = ((v % variantNames.size) + variantNames.size) % variantNames.size

    fun build(variant: Int): ByteArray {
        val now = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        return when (norm(variant)) {
            0 -> listDocument(now)
            1 -> imageDocument(now)
            2 -> dropdownDocument(now)
            else -> cardDocument(now)
        }
    }

    // ── Variant 0: a list ───────────────────────────────────────────────────
    private fun listDocument(now: String): ByteArray = document("Server-driven list") {
        fill(BG); drawRect(0f, 0f, WIDTH, HEIGHT)
        fill(ACCENT); drawRect(0f, 0f, WIDTH, 96f)
        text("Server Inbox", 24f, 58f, 32f, WHITE, panX = -1f)

        data class Row(val name: String, val sub: String, val dot: Int)
        val rows = listOf(
            Row("Ada Lovelace", "Notes on the Analytical Engine", 0xFFF2A65A.toInt()),
            Row("Grace Hopper", "Compiler review at 2pm", 0xFF38A3D1.toInt()),
            Row("Margaret Hamilton", "Apollo guidance code", 0xFFE5573F.toInt()),
            Row("Katherine Johnson", "Trajectory numbers", 0xFF2ECC71.toInt()),
            Row("Dijkstra", "Re: shortest paths", 0xFF9B59B6.toInt())
        )
        var y = 150f
        rows.forEach { r ->
            fill(r.dot); drawCircle(48f, y + 4f, 18f)
            text(r.name, 84f, y, 24f, WHITE, panX = -1f)
            text(r.sub, 84f, y + 30f, 17f, 0xFFAFAFC2.toInt(), panX = -1f)
            stroke(0xFF2A2B36.toInt(), 1.5f); drawLine(84f, y + 56f, WIDTH - 24f, y + 56f)
            y += 88f
        }
        footer(now, 0)
    }

    // ── Variant 1: a real raster image embedded in the document ─────────────
    private fun imageDocument(now: String): ByteArray = document("Server-driven image") {
        fill(0xFF101018.toInt()); drawRect(0f, 0f, WIDTH, HEIGHT)
        text("A bitmap, embedded server-side", WIDTH / 2f, 70f, 24f, WHITE)

        val imageId = addBitmap(renderScene())
        // drawBitmap(id, left, top, right, bottom, description)
        drawBitmap(imageId, 32f, 120f, WIDTH - 32f, 350f, "Generated scene")

        text("Drawn with java.awt on the server,", WIDTH / 2f, 410f, 20f, 0xFFCFCFE0.toInt())
        text("PNG-encoded into the doc bytes.", WIDTH / 2f, 440f, 20f, 0xFFCFCFE0.toInt())
        footer(now, 1)
    }

    /** Paints a small landscape into a BufferedImage that becomes a real PNG in the doc. */
    private fun renderScene(): BufferedImage {
        val w = 320
        val h = 200
        val img = BufferedImage(w, h, BufferedImage.TYPE_INT_RGB)
        val g = img.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.paint = GradientPaint(0f, 0f, AwtColor(0x2E6FB0), 0f, h.toFloat(), AwtColor(0xBFE0F5))
        g.fillRect(0, 0, w, h)
        g.color = AwtColor(0xFFE08A)
        g.fillOval(w - 90, 24, 64, 64) // sun
        g.color = AwtColor(0x3A5A8A)
        g.fillPolygon(Polygon(intArrayOf(0, 90, 190, 0), intArrayOf(h, 120, h, h), 4))
        g.color = AwtColor(0x2F4A39)
        g.fillPolygon(Polygon(intArrayOf(60, 200, w, w, 60), intArrayOf(h, 130, 160, h, h), 5))
        g.dispose()
        return img
    }

    // ── Variant 2: a dropdown (shown expanded) ──────────────────────────────
    private fun dropdownDocument(now: String): ByteArray = document("Server-driven dropdown") {
        fill(BG); drawRect(0f, 0f, WIDTH, HEIGHT)
        text("Choose a city", WIDTH / 2f, 70f, 28f, WHITE)

        // Header showing the current selection + chevron.
        fill(0xFF23242F.toInt()); drawRoundRect(40f, 110f, WIDTH - 40f, 174f, 16f, 16f)
        text("San Francisco", 64f, 150f, 24f, WHITE, panX = -1f)
        stroke(WHITE, 4f)
        drawLine(316f, 138f, 332f, 154f)
        drawLine(332f, 154f, 348f, 138f)

        // Expanded options; the selected one is highlighted.
        val options = listOf("San Francisco", "London", "Tokyo", "Sydney", "Nairobi")
        var y = 196f
        options.forEachIndexed { i, city ->
            if (i == 0) {
                fill(ACCENT); drawRoundRect(40f, y, WIDTH - 40f, y + 48f, 12f, 12f)
                text(city, 64f, y + 32f, 22f, WHITE, panX = -1f)
            } else {
                text(city, 64f, y + 32f, 22f, 0xFFCFCFDE.toInt(), panX = -1f)
            }
            y += 56f
        }
        footer(now, 2)
    }

    // ── Variant 3: a card ───────────────────────────────────────────────────
    private fun cardDocument(now: String): ByteArray = document("Server-driven card") {
        fill(0xFF0B1026.toInt()); drawRect(0f, 0f, WIDTH, HEIGHT)
        listOf(40f to 70f, 120f to 40f, 300f to 60f, 350f to 120f, 70f to 150f).forEach { (sx, sy) ->
            fill(WHITE); drawCircle(sx, sy, 3f)
        }
        val accent = 0xFF7E8BE0.toInt()
        fill(accent); drawRoundRect(24f, 36f, WIDTH - 24f, 168f, 28f, 28f)
        drawCircle(WIDTH / 2f, 320f, 76f)
        fill(0xFF0B1026.toInt()); drawCircle(WIDTH / 2f + 30f, 300f, 64f) // crescent
        text("Starry Night", WIDTH / 2f, 112f, 44f, WHITE)
        text("A single hero card", WIDTH / 2f, 440f, 26f, 0xFFE6E6F0.toInt())
        footer(now, 3)
    }

    // ---- shared bits ----

    private fun RemoteComposeContext.footer(now: String, variant: Int) {
        text("variant $variant (${variantName(variant)}) · served $now", WIDTH / 2f, HEIGHT - 56f, 20f, ACCENT)
        text("androidx.compose.remote", WIDTH / 2f, HEIGHT - 28f, 17f, 0xFF8A8AA0.toInt())
    }

    private fun document(desc: String, content: RemoteComposeContext.() -> Unit): ByteArray =
        RemoteComposeContext(W, H, desc, JvmRcPlatformServices(), content).buffer()

    private fun RemoteComposeContext.fill(color: Int) {
        buffer.addPaint(PaintBundle().apply { setColor(color); setStyle(PaintBundle.STYLE_FILL); setAntiAlias(true) })
    }

    private fun RemoteComposeContext.stroke(color: Int, width: Float) {
        buffer.addPaint(
            PaintBundle().apply {
                setColor(color); setStyle(PaintBundle.STYLE_STROKE); setStrokeWidth(width); setAntiAlias(true)
            }
        )
    }

    /** [panX] = 0 centers on [x]; -1 left-anchors; +1 right-anchors. */
    private fun RemoteComposeContext.text(s: String, x: Float, y: Float, size: Float, color: Int, panX: Float = 0f) {
        buffer.addPaint(PaintBundle().apply { setColor(color); setTextSize(size); setAntiAlias(true) })
        drawTextAnchored(s, x, y, panX, 0f, 0)
    }
}
