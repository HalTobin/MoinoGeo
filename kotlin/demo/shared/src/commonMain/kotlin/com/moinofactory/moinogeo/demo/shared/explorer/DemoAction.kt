package com.moinofactory.moinogeo.demo.shared.explorer

import androidx.compose.ui.graphics.Color
import com.moinofactory.moinogeo.core.domain.model.Country
import com.moinofactory.moinogeo.core.domain.model.GeoPlace
import com.moinofactory.moinogeo.demo.shared.flags.FlagLibraryType
import com.moinofactory.moinogeo.demo.shared.flags.FlagViewMode
import com.moinofactory.moinogeo.maps.core.TopoZone

/**
 * User intents for [DemoScreen]. Handled exclusively via [DemoViewModel.onAction].
 */
internal sealed class DemoAction {
    data object OpenSearch : DemoAction()
    data object CloseSearch : DemoAction()
    data class SearchQueryChanged(val query: String) : DemoAction()
    data object SearchSubmit : DemoAction()
    data class SelectPlace(val place: GeoPlace) : DemoAction()
    data object ClearSearchAndSelection : DemoAction()

    data object OpenSettings : DemoAction()
    data object CloseSettings : DemoAction()
    data object OpenMapSettings : DemoAction()
    data object CloseMapSettings : DemoAction()
    data object OpenMapStyleSettings : DemoAction()
    data object CloseMapStyleSettings : DemoAction()
    data class SelectFlagLibrary(val library: FlagLibraryType) : DemoAction()
    data class SelectViewMode(val mode: FlagViewMode) : DemoAction()

    data class SelectMapMode(val mode: DemoMapMode) : DemoAction()
    data class ApplyMapSettingsPreset(val settings: DemoMapSettings) : DemoAction()
    data class SetMapHorizontalScale(val scale: Float) : DemoAction()
    data class SetMapVerticalScale(val scale: Float) : DemoAction()
    data class SetMapVerticalWarp(val warp: Float) : DemoAction()
    data class SetMapCenterLatitude(val latitude: Float) : DemoAction()
    data class SetMapCenterLongitude(val longitude: Float) : DemoAction()
    data class SetMapRotationAngle(val angle: Float) : DemoAction()

    data object ResetMapStyleSettings : DemoAction()
    data class SelectMapStylePreset(val preset: DemoMapStylePreset) : DemoAction()
    data class SetMapShowBorders(val show: Boolean) : DemoAction()
    data class SetMapShowTopography(val show: Boolean) : DemoAction()
    data class SetMapBordersScaleWithZoom(val scaleWithZoom: Boolean) : DemoAction()
    data class SetMapZoneColor(val zone: TopoZone, val color: Color) : DemoAction()

    data class InspectCountry(val country: Country) : DemoAction()
    data object DismissCountryDetail : DemoAction()
}
