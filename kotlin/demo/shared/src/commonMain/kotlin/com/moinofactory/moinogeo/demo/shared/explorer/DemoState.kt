package com.moinofactory.moinogeo.demo.shared.explorer

import com.moinofactory.moinogeo.core.domain.model.Country
import com.moinofactory.moinogeo.core.domain.model.GeoPlace
import com.moinofactory.moinogeo.demo.shared.flags.FlagLibraryType
import com.moinofactory.moinogeo.demo.shared.flags.FlagViewMode

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
    val inspectedCountry: Country? = null,

    val isSearchOpen: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<GeoPlace> = emptyList(),
    val isSearching: Boolean = false,
    val selectedPlace: GeoPlace? = null,

    /** Settings for [com.moinofactory.moinogeo.maps.plane.PlaneMap]. */
    val mapVerticalScale: Float = 1f,
    val mapCenterLatitude: Float = 0f,
    val mapCenterLongitude: Float = 0f,
    val mapRotationAngle: Float = 0f,
    val mapBordersScaleWithZoom: Boolean = true,
)
