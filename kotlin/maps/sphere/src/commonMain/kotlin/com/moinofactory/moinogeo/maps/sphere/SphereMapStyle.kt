package com.moinofactory.moinogeo.maps.sphere

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import com.moinofactory.moinogeo.maps.core.MapStyle

/**
 * Sphere-specific look: shared [MapStyle] for topo/borders, plus globe camera defaults,
 * scene background, and an optional atmospheric aura.
 */
@Immutable
data class SphereMapStyle(
    val mapStyle: MapStyle = MapStyle.Default,
    val globeRadius: Float = 1f,
    val cameraDistance: Float = 3.2f,
    val rotationSpeed: Float = 0f,
    val tiltAngle: Float = 23.5f,
    val enableLighting: Boolean = true,
    val fieldOfView: Float = 45f,
    val background: SphereBackground = SphereBackground.Solid(MapStyle.Default.backgroundColor),
    val aura: SphereAura = SphereAura.Default,
) {
    companion object {
        val Default = SphereMapStyle()

        val Space = SphereMapStyle(
            mapStyle = MapStyle.Satellite,
            background = SphereBackground.Starfield(),
            aura = SphereAura(
                enabled = true,
                color = Color(0x6688CCFF),
                intensity = 0.85f,
                thickness = 0.06f,
            ),
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
        val density: Float = 0.0025f,
        val seed: Int = 42,
    ) : SphereBackground()

    /**
     * Full-bleed image (typically equirectangular HDR/sky art baked to [ImageBitmap]).
     * Filament samples it as a skybox color source in v1 (not a full IBL bake).
     */
    @Immutable
    data class Image(val bitmap: ImageBitmap) : SphereBackground()
}

/** Soft glow shell around the globe (Fresnel-ish transparent sphere + bloom). */
@Immutable
data class SphereAura(
    val enabled: Boolean = true,
    val color: Color = Color(0x6688CCFF),
    val intensity: Float = 0.65f,
    val thickness: Float = 0.05f,
    val bloomStrength: Float = 0.18f,
) {
    companion object {
        val Default = SphereAura()
        val None = SphereAura(enabled = false, bloomStrength = 0f)
    }
}
