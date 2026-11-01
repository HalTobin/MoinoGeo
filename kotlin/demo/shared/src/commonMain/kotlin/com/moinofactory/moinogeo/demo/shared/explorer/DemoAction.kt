package com.moinofactory.moinogeo.demo.shared.explorer

import com.moinofactory.moinogeo.core.domain.model.Country
import com.moinofactory.moinogeo.core.domain.model.GeoPlace
import com.moinofactory.moinogeo.demo.shared.flags.FlagLibraryType
import com.moinofactory.moinogeo.demo.shared.flags.FlagViewMode

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
    data class SelectFlagLibrary(val library: FlagLibraryType) : DemoAction()
    data class SelectViewMode(val mode: FlagViewMode) : DemoAction()

    data class InspectCountry(val country: Country) : DemoAction()
    data object DismissCountryDetail : DemoAction()
}
