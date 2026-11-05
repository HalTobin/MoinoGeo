package com.moinofactory.moinogeo.demo.shared.explorer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moinofactory.moinogeo.core.domain.MoinoGeoService
import com.moinofactory.moinogeo.core.domain.model.Country
import com.moinofactory.moinogeo.core.domain.model.GeoPlace
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
    private var nextCameraRequestId = 0L

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
                        cameraRequest = nextCameraRequest(action.place),
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
                        cameraRequest = null,
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
            is DemoAction.ApplyMapSettingsPreset -> updateActiveMap { action.settings }
            is DemoAction.SetMapHorizontalScale -> updateActiveMap {
                it.copy(horizontalScale = action.scale.coerceIn(0.5f, 2.5f))
            }
            is DemoAction.SetMapVerticalScale -> updateActiveMap {
                it.copy(verticalScale = action.scale.coerceIn(0.5f, 2.5f))
            }
            is DemoAction.SetMapVerticalWarp -> updateActiveMap {
                it.copy(verticalWarp = action.warp.coerceIn(-3f, 3f))
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

            is DemoAction.SetSphereBackgroundKind -> updateSphere {
                it.copy(backgroundKind = action.kind)
            }
            is DemoAction.SetSphereSolidColor -> updateSphere {
                it.copy(solidColor = action.color)
            }
            is DemoAction.SetSphereAuraEnabled -> updateSphere {
                it.copy(auraEnabled = action.enabled)
            }
            is DemoAction.SetSphereAuraColor -> updateSphere {
                it.copy(auraColor = action.color)
            }
            is DemoAction.SetSphereAuraIntensity -> updateSphere {
                it.copy(auraIntensity = action.intensity.coerceIn(0f, 1f))
            }
            is DemoAction.SetSphereAuraThickness -> updateSphere {
                it.copy(auraThickness = action.thickness.coerceIn(0.01f, 0.2f))
            }
            is DemoAction.SetSphereAuraBloom -> updateSphere {
                it.copy(auraBloom = action.bloom.coerceIn(0f, 0.6f))
            }
            DemoAction.ResetSphereSettings -> _state.update {
                it.copy(sphereSettings = DemoSphereSettings.Default)
            }

            DemoAction.ResetMapStyleSettings -> _state.update {
                it.copy(mapStyleSettings = DemoMapStyleSettings())
            }
            is DemoAction.SelectMapStylePreset -> updateMapStyle {
                it.copy(
                    preset = action.preset,
                    zoneColors = defaultZoneColors(action.preset.style),
                )
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
            is DemoAction.SetMapZoneColor -> updateMapStyle {
                it.copy(zoneColors = it.zoneColors + (action.zone to action.color))
            }

            is DemoAction.InspectCountry -> _state.update { it.copy(inspectedCountry = action.country) }
            DemoAction.DismissCountryDetail -> _state.update { it.copy(inspectedCountry = null) }
            is DemoAction.GoToCountry -> goToCountry(action.country)
            DemoAction.ConsumeCameraRequest -> _state.update { it.copy(cameraRequest = null) }
        }
    }

    private fun goToCountry(country: Country) {
        val place = country.toGeoPlace()
        _state.update {
            it.copy(
                selectedPlace = place,
                searchQuery = country.name,
                isSearchOpen = false,
                inspectedCountry = null,
                cameraRequest = nextCameraRequest(place),
            )
        }
    }

    private fun nextCameraRequest(place: GeoPlace): MapCameraRequest? {
        val request = place.toCameraRequest(++nextCameraRequestId) ?: return null
        return request
    }

    private fun updateActiveMap(transform: (DemoMapSettings) -> DemoMapSettings) {
        _state.update { state ->
            val updated = transform(state.activeMapSettings)
            when (state.mapMode) {
                DemoMapMode.Flat -> state.copy(flatMapSettings = updated)
                DemoMapMode.Azimuthal -> state.copy(azimuthalMapSettings = updated)
                DemoMapMode.Sphere -> state
            }
        }
    }

    private fun updateSphere(transform: (DemoSphereSettings) -> DemoSphereSettings) {
        _state.update { state ->
            state.copy(sphereSettings = transform(state.sphereSettings))
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
