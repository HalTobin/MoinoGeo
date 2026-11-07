package com.moinofactory.moinogeo.maps.sphere

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import com.moinofactory.moinogeo.maps.core.GeoPoint
import com.moinofactory.moinogeo.maps.core.MapStyle
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Globe illumination. Modes are mutually exclusive: either flat/unlit colour, or a single
 * directional sun for day/night simulation.
 */
@Immutable
sealed class SphereLighting {
    /**
     * Uniform self-illumination: the baked map texture is shown as-is with no shading,
     * terminator, or shadows.
     */
    @Immutable
    data object Uniform : SphereLighting()

    /**
     * Directional sunlight for day/night cycles. [directionX]/[directionY]/[directionZ] is the
     * world-space travel direction of the light (Filament convention; need not be unit length).
     *
     * Prefer [over] when driving the sun from a geographic subsolar point.
     *
     * @param intensity Relative sun brightness (`1` ≈ daylight).
     * @param ambient Soft fill on the night side (`0` = pure black night, `1` ≈ fully lit).
     */
    @Immutable
    data class Directional(
        val directionX: Float = 0.35f,
        val directionY: Float = -0.85f,
        val directionZ: Float = -0.4f,
        val intensity: Float = 1f,
        val ambient: Float = 0.12f,
    ) : SphereLighting() {
        companion object {
            /**
             * Sun directly overhead at [point] (degrees). Light travels from that side of the
             * globe toward the origin, lighting the hemisphere around the subsolar point.
             */
            fun over(
                point: GeoPoint,
                intensity: Float = 1f,
                ambient: Float = 0.12f,
            ): Directional = over(
                latitude = point.latitude,
                longitude = point.longitude,
                intensity = intensity,
                ambient = ambient,
            )

            /** Sun directly overhead at [latitude]/[longitude] (degrees). */
            fun over(
                latitude: Double,
                longitude: Double,
                intensity: Float = 1f,
                ambient: Float = 0.12f,
            ): Directional {
                val lat = latitude * DegToRad
                val lon = longitude * DegToRad
                val cosLat = cos(lat)
                // Subsolar cartesian on the unit sphere (matches [SphereProjection.toCartesian]).
                val px = (cosLat * cos(lon)).toFloat()
                val py = sin(lat).toFloat()
                val pz = (cosLat * sin(lon)).toFloat()
                // Sunlight travels from the sun toward the globe centre.
                return Directional(
                    directionX = -px,
                    directionY = -py,
                    directionZ = -pz,
                    intensity = intensity,
                    ambient = ambient,
                )
            }
        }
    }

    companion object {
        val Default: SphereLighting = Uniform
    }
}

private const val DegToRad = PI / 180.0

/**
 * Sphere-specific look: shared [MapStyle] for topo/borders, plus scene background and aura.
 */
@Immutable
data class SphereMapStyle(
    val mapStyle: MapStyle = MapStyle.Default,
    val globeRadius: Float = 1f,
    val fieldOfView: Float = 45f,
    val background: SphereBackground = SphereBackground.Starfield(),
    val aura: SphereAura = SphereAura.Default,
    /** Exactly one lighting mode — uniform (default) or directional sun. Last for ABI safety. */
    val lighting: SphereLighting = SphereLighting.Uniform,
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

/**
 * Atmospheric glowing aura surrounding the globe at its edges:
 * an outer halo radiating outward into space and an inner Fresnel rim along the silhouette,
 * while leaving the face of the globe clean so map details remain sharp and visible.
 *
 * [thickness] controls the width/reach of the halo and rim falloff;
 * [intensity] scales emission brightness;
 * [bloomStrength] controls post-processing bloom intensity.
 */
@Immutable
data class SphereAura(
    val enabled: Boolean = true,
    val color: Color = Color(0xFF5EC8FF),
    val intensity: Float = 1f,
    val thickness: Float = 0.04f,
    val bloomStrength: Float = 0.05f,
) {
    companion object {
        val Default = SphereAura()
        val None = SphereAura(enabled = false, bloomStrength = 0f)
    }
}
