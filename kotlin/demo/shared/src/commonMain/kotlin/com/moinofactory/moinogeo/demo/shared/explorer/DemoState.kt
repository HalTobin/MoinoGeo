package com.moinofactory.moinogeo.demo.shared.explorer

import androidx.compose.ui.graphics.Color
import com.moinofactory.moinogeo.core.domain.model.Country
import com.moinofactory.moinogeo.core.domain.model.GeoPlace
import com.moinofactory.moinogeo.core.domain.model.GeoPlaceType
import com.moinofactory.moinogeo.demo.shared.flags.FlagLibraryType
import com.moinofactory.moinogeo.demo.shared.flags.FlagViewMode
import com.moinofactory.moinogeo.maps.core.GeoPoint
import com.moinofactory.moinogeo.maps.core.MapStyle
import com.moinofactory.moinogeo.maps.core.TopoZone
import com.moinofactory.moinogeo.maps.core.ZoneStyle
import com.moinofactory.moinogeo.maps.sphere.SphereAura
import com.moinofactory.moinogeo.maps.sphere.SphereBackground
import com.moinofactory.moinogeo.maps.sphere.SphereMapStyle

/**
 * Immutable UI state for [DemoScreen].
 */
internal data class DemoState(
    val countries: List<Country> = emptyList(),
    val isLoadingCountries: Boolean = true,

    val selectedLibrary: FlagLibraryType = FlagLibraryType.LIPIS_RECTANGLE,
    val selectedViewMode: FlagViewMode = FlagViewMode.LIST,

    val isSettingsOpen: Boolean = false,
    val isMapSettingsOpen: Boolean = false,
    val isMapStyleSettingsOpen: Boolean = false,
    val inspectedCountry: Country? = null,

    val isSearchOpen: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<GeoPlace> = emptyList(),
    val isSearching: Boolean = false,
    val selectedPlace: GeoPlace? = null,

    /** One-shot fly-to consumed by [com.moinofactory.moinogeo.demo.shared.explorer.components.DemoMap]. */
    val cameraRequest: MapCameraRequest? = null,

    val mapMode: DemoMapMode = DemoMapMode.Flat,
    val flatMapSettings: DemoMapSettings = DemoMapSettings.Standard,
    val azimuthalMapSettings: DemoMapSettings = DemoMapSettings.Linear,
    val sphereSettings: DemoSphereSettings = DemoSphereSettings(),
    val mapStyleSettings: DemoMapStyleSettings = DemoMapStyleSettings(),
) {
    val activeMapSettings: DemoMapSettings
        get() = when (mapMode) {
            DemoMapMode.Flat -> flatMapSettings
            DemoMapMode.Azimuthal -> azimuthalMapSettings
            DemoMapMode.Sphere -> DemoMapSettings.Linear
        }
}

/** Animated camera fly-to issued by search / country Go To. */
internal data class MapCameraRequest(
    val id: Long,
    val point: GeoPoint,
    val zoom: Float,
)

internal fun GeoPlaceType.defaultCameraZoom(): Float = when (this) {
    GeoPlaceType.REGION, GeoPlaceType.SUBREGION -> 2f
    GeoPlaceType.COUNTRY -> 3.5f
    GeoPlaceType.STATE -> 5.5f
    GeoPlaceType.CITY -> 7.5f
}

internal fun GeoPlace.toCameraRequest(id: Long): MapCameraRequest? {
    val lat = latitude ?: return null
    val lon = longitude ?: return null
    return MapCameraRequest(
        id = id,
        point = GeoPoint(latitude = lat, longitude = lon),
        zoom = type.defaultCameraZoom(),
    )
}

internal fun Country.toGeoPlace(): GeoPlace = GeoPlace(
    type = GeoPlaceType.COUNTRY,
    id = id,
    name = name,
    iso2 = iso2,
    iso3 = iso3,
    latitude = latitude,
    longitude = longitude,
    regionId = regionId,
    subregionId = subregionId,
    countryId = id,
)

internal fun Country.hasCoordinates(): Boolean =
    latitude != null && longitude != null

internal enum class DemoMapMode(
    val title: String,
    /** Natural geographic width/height before horizontal/vertical stretch. */
    val baseAspectRatio: Float,
) {
    Flat("Flat", baseAspectRatio = 2f),
    Azimuthal("Azimuthal", baseAspectRatio = 1f),
    Sphere("Sphere", baseAspectRatio = 1f),
}

/** Camera controls for one map mode. */
internal data class DemoMapSettings(
    val horizontalScale: Float = 1f,
    val verticalScale: Float = 1f,
    /** Non-linear vertical falloff from center; `0` = linear (flat map only). */
    val verticalWarp: Float = 0f,
    val centerLatitude: Float = 0f,
    val centerLongitude: Float = 0f,
    val rotationAngle: Float = 0f,
) {
    /**
     * Relative Y stretch for the map projection. Horizontal stretch is folded in
     * so only the ratio matters; canvas size stays independent.
     */
    fun projectionVerticalScale(): Float {
        val horizontal = horizontalScale.coerceAtLeast(MinStretch)
        val vertical = verticalScale.coerceAtLeast(MinStretch)
        return (vertical / horizontal).coerceIn(MinProjectionScale, MaxProjectionScale)
    }

    /** Effective geographic aspect after stretch (informational). */
    fun aspectRatio(baseAspectRatio: Float): Float {
        val horizontal = horizontalScale.coerceAtLeast(MinStretch)
        val vertical = verticalScale.coerceAtLeast(MinStretch)
        return (baseAspectRatio * horizontal / vertical).coerceIn(MinAspectRatio, MaxAspectRatio)
    }

    companion object {
        /** Neutral equirectangular: uniform 1× scale, no falloff. */
        val Linear = DemoMapSettings()

        /**
         * Cartographic flat look: slight vertical stretch plus polar falloff,
         * closer to common printed/web world maps.
         */
        val Standard = DemoMapSettings(
            verticalScale = 1.6f,
            verticalWarp = 1.9f,
        )

        private const val MinStretch = 0.5f
        private const val MinAspectRatio = 0.35f
        private const val MaxAspectRatio = 4f
        private const val MinProjectionScale = 0.2f
        private const val MaxProjectionScale = 5f
    }
}

/** Shared map style options (apply to every map mode). */
internal data class DemoMapStyleSettings(
    val preset: DemoMapStylePreset = DemoMapStylePreset.Default,
    val showBorders: Boolean = true,
    val showTopography: Boolean = true,
    val bordersScaleWithZoom: Boolean = true,
    val zoneColors: Map<TopoZone, Color> = defaultZoneColors(),
) {
    fun toMapStyle(): MapStyle {
        val base = preset.style
        return base.copy(
            showBorders = showBorders,
            showTopography = showTopography,
            zoneStyles = zoneColors.mapValues { (_, color) -> ZoneStyle(fillColor = color) },
            borderStyle = base.borderStyle.copy(
                lineWidth = 1f,
                scaleWithZoom = bordersScaleWithZoom,
            ),
        )
    }
}

internal fun defaultZoneColors(
    style: MapStyle = MapStyle.Default,
): Map<TopoZone, Color> = style.zoneStyles.mapValues { it.value.fillColor }

internal fun TopoZone.displayName(): String =
    name.lowercase().replaceFirstChar { it.titlecase() }

internal enum class DemoMapStylePreset(val title: String, val style: MapStyle) {
    Default("Default", MapStyle.Default),
    Light("Light", MapStyle.Light),
    Dark("Dark", MapStyle.Dark),
    Satellite("Satellite", MapStyle.Satellite),
}

internal enum class DemoSphereBackgroundKind(val title: String) {
    Starfield("Stars"),
    Solid("Solid color"),
}

/** Sphere-only look controls (aura + backdrop). No stretch / center / rotation. */
internal data class DemoSphereSettings(
    val backgroundKind: DemoSphereBackgroundKind = DemoSphereBackgroundKind.Starfield,
    val solidColor: Color = Color(0xFF02040A),
    val auraEnabled: Boolean = true,
    val auraColor: Color = Color(0x6688CCFF),
    val auraIntensity: Float = 0.85f,
    val auraThickness: Float = 0.06f,
    val auraBloom: Float = 0.18f,
) {
    fun toSphereMapStyle(mapStyle: MapStyle): SphereMapStyle {
        val background = when (backgroundKind) {
            DemoSphereBackgroundKind.Starfield -> SphereBackground.Starfield()
            DemoSphereBackgroundKind.Solid -> SphereBackground.Solid(solidColor)
        }
        return SphereMapStyle(
            mapStyle = mapStyle,
            background = background,
            aura = SphereAura(
                enabled = auraEnabled,
                color = auraColor,
                intensity = auraIntensity,
                thickness = auraThickness,
                bloomStrength = auraBloom,
            ),
        )
    }

    companion object {
        val Default = DemoSphereSettings()
    }
}
