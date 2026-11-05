package com.moinofactory.moinogeo.maps.sphere

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import com.moinofactory.moinogeo.maps.core.MapStyle

/**
 * Sphere-specific look: shared [MapStyle] for topo/borders, plus scene background and aura.
 */
@Immutable
data class SphereMapStyle(
    val mapStyle: MapStyle = MapStyle.Default,
    val globeRadius: Float = 1f,
    val enableLighting: Boolean = true,
    val fieldOfView: Float = 45f,
    val background: SphereBackground = SphereBackground.Starfield(),
    val aura: SphereAura = SphereAura.Default,
) {
    companion object {
        /** Space backdrop with a soft atmospheric aura — the default globe look. */
        val Default = SphereMapStyle()

        val FlatBackdrop = SphereMapStyle(
            background = SphereBackground.Solid(MapStyle.Default.backgroundColor),
            aura = SphereAura.None,
        )
    }
}

/** Backdrop behind the globe. */
@Immutable
sealed class SphereBackground {
    @Immutable
    data class Solid(val color: Color) : SphereBackground()

    @Immutable
    data class Starfield(
        val voidColor: Color = Color(0xFF02040A),
        val starColor: Color = Color(0xFFE8EEFF),
        val density: Float = 0.0035f,
        val seed: Int = 42,
    ) : SphereBackground()

    /**
     * Full-bleed image (typically equirectangular sky art baked to [ImageBitmap]).
     * Rendered as a large textured skydome in v1.
     */
    @Immutable
    data class Image(val bitmap: ImageBitmap) : SphereBackground()
}

/** Soft glow shell around the globe (transparent sphere + bloom). */
@Immutable
data class SphereAura(
    val enabled: Boolean = true,
    val color: Color = Color(0x6688CCFF),
    val intensity: Float = 0.85f,
    val thickness: Float = 0.06f,
    val bloomStrength: Float = 0.18f,
) {
    companion object {
        val Default = SphereAura()
        val None = SphereAura(enabled = false, bloomStrength = 0f)
    }
}
