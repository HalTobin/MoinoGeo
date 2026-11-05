package com.moinofactory.moinogeo.demo.shared.explorer

import com.moinofactory.moinogeo.core.domain.model.Country
import com.moinofactory.moinogeo.core.domain.model.GeoPlace
import com.moinofactory.moinogeo.demo.shared.flags.FlagLibraryType
import com.moinofactory.moinogeo.demo.shared.flags.FlagViewMode
import com.moinofactory.moinogeo.maps.core.MapStyle

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

    val mapMode: DemoMapMode = DemoMapMode.Flat,
    val flatMapSettings: DemoMapSettings = DemoMapSettings(),
    val azimuthalMapSettings: DemoMapSettings = DemoMapSettings(),
    val mapStyleSettings: DemoMapStyleSettings = DemoMapStyleSettings(),
) {
    val activeMapSettings: DemoMapSettings
        get() = when (mapMode) {
            DemoMapMode.Flat -> flatMapSettings
            DemoMapMode.Azimuthal -> azimuthalMapSettings
        }
}

internal enum class DemoMapMode(
    val title: String,
    /** Natural map canvas width/height before horizontal/vertical stretch. */
    val baseAspectRatio: Float,
) {
    Flat("Flat", baseAspectRatio = 2f),
    Azimuthal("Azimuthal", baseAspectRatio = 1f),
}

/** Camera controls for one map mode. */
internal data class DemoMapSettings(
    val horizontalScale: Float = 1f,
    val verticalScale: Float = 1f,
    val centerLatitude: Float = 0f,
    val centerLongitude: Float = 0f,
    val rotationAngle: Float = 0f,
) {
    /** Canvas aspect ratio after stretch (does not follow the parent layout). */
    fun aspectRatio(baseAspectRatio: Float): Float {
        val horizontal = horizontalScale.coerceAtLeast(MinStretch)
        val vertical = verticalScale.coerceAtLeast(MinStretch)
        return (baseAspectRatio * horizontal / vertical).coerceIn(MinAspectRatio, MaxAspectRatio)
    }

    private companion object {
        const val MinStretch = 0.5f
        const val MinAspectRatio = 0.35f
        const val MaxAspectRatio = 4f
    }
}

/** Shared map style options (apply to every map mode). */
internal data class DemoMapStyleSettings(
    val preset: DemoMapStylePreset = DemoMapStylePreset.Default,
    val showBorders: Boolean = true,
    val showTopography: Boolean = true,
    val bordersScaleWithZoom: Boolean = true,
) {
    fun toMapStyle(): MapStyle {
        val base = preset.style
        return base.copy(
            showBorders = showBorders,
            showTopography = showTopography,
            borderStyle = base.borderStyle.copy(
                lineWidth = 1f,
                scaleWithZoom = bordersScaleWithZoom,
            ),
        )
    }
}

internal enum class DemoMapStylePreset(val title: String, val style: MapStyle) {
    Default("Default", MapStyle.Default),
    Light("Light", MapStyle.Light),
    Dark("Dark", MapStyle.Dark),
    Satellite("Satellite", MapStyle.Satellite),
}
