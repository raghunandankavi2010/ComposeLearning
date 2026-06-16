package com.example.composelearning.arglasses.presentation

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import com.example.composelearning.R

/**
 * A single selectable pair of spectacles in the try-on catalog.
 *
 * The artwork is a real, front-facing transparent PNG (CC0 / public domain, sourced from
 * openclipart.org). [tint], when non-null, is applied to the drawn frame with
 * `BlendMode.SrcAtop` so the same artwork can be re-coloured — that's how the catalog
 * offers "the same glasses in different colours" without shipping a separate image per hue.
 *
 * Lives in the presentation layer (not domain) because it references Android drawable
 * resources and Compose [Color]; the domain stays pure-JVM and ML-Kit-free.
 *
 * @property id          stable identity used by the MVI state / [GlassesCatalog.byId].
 * @property name        label shown under the picker thumbnail.
 * @property drawableRes the transparent PNG artwork.
 * @property tint        optional re-colour applied over the artwork (null = original colour).
 */
@Immutable
data class GlassesStyle(
    val id: String,
    val name: String,
    @DrawableRes val drawableRes: Int,
    val tint: Color? = null,
)

/** The fixed set of spectacles the user can choose from, plus lookup helpers. */
object GlassesCatalog {
    val styles: List<GlassesStyle> = listOf(
        GlassesStyle("rect_black", "Classic", R.drawable.glasses_rectangle),
        GlassesStyle("rect_tortoise", "Tortoise", R.drawable.glasses_rectangle, Color(0xFF6B3F1D)),
        GlassesStyle("rect_navy", "Navy", R.drawable.glasses_rectangle, Color(0xFF1B2A4A)),
        GlassesStyle("round_black", "Round", R.drawable.glasses_round),
        GlassesStyle("round_gold", "Gold round", R.drawable.glasses_round, Color(0xFFC8A24B)),
        GlassesStyle("cateye_black", "Cat-eye", R.drawable.glasses_cateye),
        GlassesStyle("cateye_red", "Ruby cat-eye", R.drawable.glasses_cateye, Color(0xFFB3262A)),
    )

    val default: GlassesStyle = styles.first()

    fun byId(id: String): GlassesStyle = styles.firstOrNull { it.id == id } ?: default
}

/**
 * The [ColorFilter] that re-colours this style's artwork, or null to draw it untouched.
 * `SrcAtop` keeps the artwork's alpha (so transparent lenses stay clear) while tinting the
 * opaque frame to [GlassesStyle.tint].
 */
fun GlassesStyle.tintFilter(): ColorFilter? =
    tint?.let { ColorFilter.tint(it, BlendMode.SrcAtop) }
