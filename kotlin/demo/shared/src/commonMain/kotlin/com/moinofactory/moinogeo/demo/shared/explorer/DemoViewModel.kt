package com.moinofactory.moinogeo.demo.shared.explorer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moinofactory.moinogeo.core.domain.MoinoGeoService
import com.moinofactory.moinogeo.demo.shared.data.sampleCountries
import com.moinofactory.moinogeo.demo.shared.flags.FlagLibraryType
import com.moinofactory.moinogeo.demo.shared.flags.FlagViewMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * MVI ViewModel for the geo explorer demo.
 *
 * Exposes a single [state] [StateFlow] and a single [onAction] entry point.
 */
internal class DemoViewModel(
    private val geo: MoinoGeoService,
) : ViewModel() {

    private val _state = MutableStateFlow(DemoState())
    val state: StateFlow<DemoState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadCountries()
    }

    fun onAction(action: DemoAction) {
        when (action) {
            DemoAction.OpenSearch -> _state.update { it.copy(isSearchOpen = true) }
            DemoAction.CloseSearch -> {
                searchJob?.cancel()
                _state.update {
                    it.copy(
                        isSearchOpen = false,
                        isSearching = false,
                    )
                }
            }
            is DemoAction.SearchQueryChanged -> onSearchQueryChanged(action.query)
            DemoAction.SearchSubmit -> onSearchSubmit()
            is DemoAction.SelectPlace -> {
                searchJob?.cancel()
                _state.update {
                    it.copy(
                        selectedPlace = action.place,
                        isSearchOpen = false,
                        isSearching = false,
                        searchQuery = action.place.name,
                    )
                }
            }
            DemoAction.ClearSearchAndSelection -> {
                searchJob?.cancel()
                _state.update {
                    it.copy(
                        searchQuery = "",
                        searchResults = emptyList(),
                        isSearching = false,
                        isSearchOpen = false,
                        selectedPlace = null,
                    )
                }
            }

            DemoAction.OpenSettings -> _state.update { it.copy(isSettingsOpen = true) }
            DemoAction.CloseSettings -> _state.update { it.copy(isSettingsOpen = false) }
            DemoAction.OpenMapSettings -> _state.update { it.copy(isMapSettingsOpen = true) }
            DemoAction.CloseMapSettings -> _state.update { it.copy(isMapSettingsOpen = false) }
            DemoAction.OpenMapStyleSettings -> _state.update { it.copy(isMapStyleSettingsOpen = true) }
            DemoAction.CloseMapStyleSettings -> _state.update { it.copy(isMapStyleSettingsOpen = false) }
            is DemoAction.SelectFlagLibrary -> onSelectFlagLibrary(action.library)
            is DemoAction.SelectViewMode -> _state.update { it.copy(selectedViewMode = action.mode) }

            is DemoAction.SelectMapMode -> _state.update { it.copy(mapMode = action.mode) }
            DemoAction.ResetActiveMapSettings -> updateActiveMap { DemoMapSettings() }
            is DemoAction.SetMapHorizontalScale -> updateActiveMap {
                it.copy(horizontalScale = action.scale.coerceIn(0.5f, 2.5f))
            }
            is DemoAction.SetMapVerticalScale -> updateActiveMap {
                it.copy(verticalScale = action.scale.coerceIn(0.5f, 2.5f))
            }
            is DemoAction.SetMapCenterLatitude -> updateActiveMap {
                it.copy(centerLatitude = action.latitude.coerceIn(-90f, 90f))
            }
            is DemoAction.SetMapCenterLongitude -> updateActiveMap {
                it.copy(centerLongitude = action.longitude.coerceIn(-180f, 180f))
            }
            is DemoAction.SetMapRotationAngle -> updateActiveMap {
                it.copy(rotationAngle = action.angle.coerceIn(-180f, 180f))
            }

            DemoAction.ResetMapStyleSettings -> _state.update {
                it.copy(mapStyleSettings = DemoMapStyleSettings())
            }
            is DemoAction.SelectMapStylePreset -> updateMapStyle {
                it.copy(preset = action.preset)
            }
            is DemoAction.SetMapShowBorders -> updateMapStyle {
                it.copy(showBorders = action.show)
            }
            is DemoAction.SetMapShowTopography -> updateMapStyle {
                it.copy(showTopography = action.show)
            }
            is DemoAction.SetMapBordersScaleWithZoom -> updateMapStyle {
                it.copy(bordersScaleWithZoom = action.scaleWithZoom)
            }

            is DemoAction.InspectCountry -> _state.update { it.copy(inspectedCountry = action.country) }
            DemoAction.DismissCountryDetail -> _state.update { it.copy(inspectedCountry = null) }
        }
    }

    private fun updateActiveMap(transform: (DemoMapSettings) -> DemoMapSettings) {
        _state.update { state ->
            val updated = transform(state.activeMapSettings)
            when (state.mapMode) {
                DemoMapMode.Flat -> state.copy(flatMapSettings = updated)
                DemoMapMode.Azimuthal -> state.copy(azimuthalMapSettings = updated)
                DemoMapMode.Sphere -> state.copy(sphereMapSettings = updated)
            }
        }
    }

    private fun updateMapStyle(transform: (DemoMapStyleSettings) -> DemoMapStyleSettings) {
        _state.update { state ->
            state.copy(mapStyleSettings = transform(state.mapStyleSettings))
        }
    }

    private fun onSelectFlagLibrary(library: FlagLibraryType) {
        _state.update { current ->
            val viewMode = if (current.selectedViewMode == FlagViewMode.COMPARE) {
                FlagViewMode.LIST
            } else {
                current.selectedViewMode
            }
            current.copy(selectedLibrary = library, selectedViewMode = viewMode)
        }
    }

    private fun onSearchQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query) }
        searchJob?.cancel()

        val trimmed = query.trim()
        if (trimmed.length < MIN_QUERY_LENGTH) {
            _state.update { it.copy(searchResults = emptyList(), isSearching = false) }
            return
        }

        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            performSearch(trimmed)
        }
    }

    private fun onSearchSubmit() {
        searchJob?.cancel()
        val trimmed = _state.value.searchQuery.trim()
        if (trimmed.isEmpty()) {
            _state.update { it.copy(searchResults = emptyList(), isSearching = false) }
            return
        }
        searchJob = viewModelScope.launch {
            performSearch(trimmed)
        }
    }

    private suspend fun performSearch(query: String) {
        _state.update { it.copy(isSearching = true) }
        val results = runCatching {
            withContext(Dispatchers.Default) {
                geo.searchPlaces(query = query, limit = SEARCH_LIMIT)
            }
        }.getOrElse { emptyList() }
        _state.update {
            it.copy(
                searchResults = results,
                isSearching = false,
            )
        }
    }

    private fun loadCountries() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingCountries = true) }
            try {
                val dbCountries = withContext(Dispatchers.Default) {
                    geo.getCountries()
                }
                if (dbCountries.isNotEmpty()) {
                    _state.update {
                        it.copy(
                            countries = dbCountries,
                            isLoadingCountries = false,
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            countries = sampleCountries,
                            isLoadingCountries = false,
                        )
                    }
                }
            } catch (_: Exception) {
                _state.update {
                    it.copy(
                        countries = sampleCountries,
                        isLoadingCountries = false,
                    )
                }
            }
        }
    }

    private companion object {
        const val MIN_QUERY_LENGTH = 3
        const val SEARCH_DEBOUNCE_MS = 1_000L
        const val SEARCH_LIMIT = 50
    }
}
