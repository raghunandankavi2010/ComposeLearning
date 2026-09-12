package com.example.composelearning.globe.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.math.PI

/**
 * One admin-0 unit of the Natural Earth 1:50m dataset (sovereign states plus dependencies).
 *
 * @param id 1..241, the value stored in the index raster; 0 means ocean.
 * @param lon label anchor longitude in degrees — the pole of inaccessibility, not the centroid.
 * @param areaSr spherical area in steradians, used for label priority (GLOBE.md §13).
 */
@Serializable
data class Country(
    val id: Int,
    val name: String,
    val lon: Float,
    val lat: Float,
    val areaSr: Float,
    val areaKm2: Long,
    @SerialName("colour") val colourClass: Int
)

@Serializable
private data class CountriesFile(
    val version: Int,
    val textureWidth: Int,
    val textureHeight: Int,
    val colourClasses: Int,
    val countries: List<Country>
)

/**
 * The baked world: an equirectangular raster of country ids plus the country table.
 * See `GLOBE.md` §7 and §14 — everything here is produced by
 * `tools/globe/build_globe_assets.py`.
 */
class WorldAtlas(
    val countries: List<Country>,
    val indexBitmap: Bitmap,
    val colourClasses: Int
) {
    val texWidth: Int get() = indexBitmap.width
    val texHeight: Int get() = indexBitmap.height

    private val byId = arrayOfNulls<Country>(MaxIds)

    init {
        countries.forEach { byId[it.id] = it }
    }

    fun country(id: Int): Country? = if (id in 1 until MaxIds) byId[id] else null

    /**
     * The equirectangular lookup of GLOBE.md §7, on the CPU — the same function the shader
     * uses to colour that pixel, which is what makes hit-testing agree with what is on screen.
     */
    fun idAt(lonRad: Float, latRad: Float): Int {
        val s = ((lonRad / (2.0 * PI) + 0.5) * texWidth).toInt().mod(texWidth)
        val t = ((0.5 - latRad / PI) * texHeight).toInt().coerceIn(0, texHeight - 1)
        return (indexBitmap.getPixel(s, t) shr 16) and 0xFF // R channel holds the id
    }

    /**
     * The 256×1 colour lookup of GLOBE.md §7. Country colour = its four-colouring class,
     * nudged by a per-id hash so that same-class countries that are *not* neighbours still
     * differ slightly. Rebuilding this is a 1 KB upload, so re-theming never re-bakes.
     */
    fun buildPalette(classColours: List<Color>, oceanColour: Color): Bitmap {
        val bmp = Bitmap.createBitmap(MaxIds, 1, Bitmap.Config.ARGB_8888)
        bmp.setPixel(0, 0, oceanColour.toArgb())
        for (c in countries) {
            val base = classColours[c.colourClass % classColours.size]
            val jitter = ((c.id.toLong() * 2654435761L) % 1000L) / 1000f
            val k = 0.87f + 0.26f * jitter
            bmp.setPixel(
                c.id, 0,
                Color(
                    red = (base.red * k).coerceIn(0f, 1f),
                    green = (base.green * k).coerceIn(0f, 1f),
                    blue = (base.blue * k).coerceIn(0f, 1f)
                ).toArgb()
            )
        }
        return bmp
    }

    companion object {
        const val MaxIds = 256

        suspend fun load(context: Context): WorldAtlas = withContext(Dispatchers.IO) {
            val json = context.assets.open("globe/countries.json")
                .bufferedReader().use { it.readText() }
            val parsed = Json { ignoreUnknownKeys = true }
                .decodeFromString<CountriesFile>(json)

            // inScaled = false: this is data, not artwork — density scaling would resample ids.
            val options = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
                inScaled = false
            }
            val bitmap = context.assets.open("globe/world_index.png").use {
                BitmapFactory.decodeStream(it, null, options)
            } ?: error("globe/world_index.png missing — run tools/globe/build_globe_assets.py")

            WorldAtlas(parsed.countries, bitmap, parsed.colourClasses)
        }
    }
}

