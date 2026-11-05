package com.moinofactory.moinogeo.demo.shared.explorer

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moinofactory.moinogeo.core.domain.MoinoGeoService
import com.moinofactory.moinogeo.demo.shared.explorer.components.CountryListPane
import com.moinofactory.moinogeo.demo.shared.explorer.components.DemoMap
import com.moinofactory.moinogeo.demo.shared.explorer.components.FlagSettingsDialog
import com.moinofactory.moinogeo.demo.shared.explorer.components.MapSettingsDialog
import com.moinofactory.moinogeo.demo.shared.explorer.components.MapStyleDialog
import com.moinofactory.moinogeo.demo.shared.explorer.components.PlaceSearchBar
import com.moinofactory.moinogeo.demo.shared.explorer.components.PlaceSearchDialog
import com.moinofactory.moinogeo.demo.shared.flags.FlagDetailDialog
import com.moinofactory.moinogeo.demo.shared.ui.icon.Icons
import com.moinofactory.moinogeo.demo.shared.ui.icon.settings

private val ExpandedWidthBreakpoint = 840.dp

/**
 * Adaptive geo explorer demo: map + search + country list on large screens,
 * search + map on compact screens.
 */
@Composable
internal fun DemoScreen(geo: MoinoGeoService) {
    val demoViewModel: DemoViewModel = viewModel { DemoViewModel(geo) }
    val state by demoViewModel.state.collectAsStateWithLifecycle()
    DemoScreenContent(
        state = state,
        onAction = demoViewModel::onAction,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DemoScreenContent(
    state: DemoState,
    onAction: (DemoAction) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "MoinoGeo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Demo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onAction(DemoAction.OpenSettings) }
                    ) {
                        Icon(
                            imageVector = Icons.settings,
                            contentDescription = "App Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            )
        },
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
        ) {
            val isExpanded = maxWidth >= ExpandedWidthBreakpoint

            Column(modifier = Modifier.fillMaxSize()) {
                if (isExpanded) {
                    ExpandedDemoLayout(state = state, onAction = onAction)
                } else {
                    CompactDemoLayout(state = state, onAction = onAction)
                }
            }
        }
    }

    if (state.isSearchOpen) {
        PlaceSearchDialog(
            query = state.searchQuery,
            results = state.searchResults,
            isSearching = state.isSearching,
            onQueryChange = { onAction(DemoAction.SearchQueryChanged(it)) },
            onSubmit = { onAction(DemoAction.SearchSubmit) },
            onPlaceSelected = { onAction(DemoAction.SelectPlace(it)) },
            onDismiss = { onAction(DemoAction.CloseSearch) },
        )
    }

    if (state.isSettingsOpen) {
        FlagSettingsDialog(
            selectedLibrary = state.selectedLibrary,
            selectedViewMode = state.selectedViewMode,
            onLibrarySelected = { onAction(DemoAction.SelectFlagLibrary(it)) },
            onViewModeSelected = { onAction(DemoAction.SelectViewMode(it)) },
            onDismiss = { onAction(DemoAction.CloseSettings) },
        )
    }

    if (state.isMapStyleSettingsOpen) {
        MapStyleDialog(
            settings = state.mapStyleSettings,
            onPresetSelected = { onAction(DemoAction.SelectMapStylePreset(it)) },
            onShowBordersChange = { onAction(DemoAction.SetMapShowBorders(it)) },
            onShowTopographyChange = { onAction(DemoAction.SetMapShowTopography(it)) },
            onBordersScaleWithZoomChange = {
                onAction(DemoAction.SetMapBordersScaleWithZoom(it))
            },
            onZoneColorChange = { zone, color ->
                onAction(DemoAction.SetMapZoneColor(zone, color))
            },
            onReset = { onAction(DemoAction.ResetMapStyleSettings) },
            onDismiss = { onAction(DemoAction.CloseMapStyleSettings) },
        )
    }

    if (state.isMapSettingsOpen) {
        MapSettingsDialog(
            mode = state.mapMode,
            settings = state.activeMapSettings,
            sphereSettings = state.sphereSettings,
            onMapHorizontalScaleChange = { onAction(DemoAction.SetMapHorizontalScale(it)) },
            onMapVerticalScaleChange = { onAction(DemoAction.SetMapVerticalScale(it)) },
            onMapVerticalWarpChange = { onAction(DemoAction.SetMapVerticalWarp(it)) },
            onMapCenterLatitudeChange = { onAction(DemoAction.SetMapCenterLatitude(it)) },
            onMapCenterLongitudeChange = { onAction(DemoAction.SetMapCenterLongitude(it)) },
            onMapRotationAngleChange = { onAction(DemoAction.SetMapRotationAngle(it)) },
            onApplyPreset = { onAction(DemoAction.ApplyMapSettingsPreset(it)) },
            onSphereBackgroundKindChange = { onAction(DemoAction.SetSphereBackgroundKind(it)) },
            onSphereSolidColorChange = { onAction(DemoAction.SetSphereSolidColor(it)) },
            onSphereAuraEnabledChange = { onAction(DemoAction.SetSphereAuraEnabled(it)) },
            onSphereAuraColorChange = { onAction(DemoAction.SetSphereAuraColor(it)) },
            onSphereAuraIntensityChange = { onAction(DemoAction.SetSphereAuraIntensity(it)) },
            onSphereAuraThicknessChange = { onAction(DemoAction.SetSphereAuraThickness(it)) },
            onSphereAuraBloomChange = { onAction(DemoAction.SetSphereAuraBloom(it)) },
            onResetSphereSettings = { onAction(DemoAction.ResetSphereSettings) },
            onDismiss = { onAction(DemoAction.CloseMapSettings) },
        )
    }

    state.inspectedCountry?.let { country ->
        FlagDetailDialog(
            country = country,
            onDismiss = { onAction(DemoAction.DismissCountryDetail) },
            onGoTo = { onAction(DemoAction.GoToCountry(country)) },
        )
    }
}

@Composable
private fun ExpandedDemoLayout(
    state: DemoState,
    onAction: (DemoAction) -> Unit,
) {
    Row(modifier = Modifier.fillMaxSize()) {
        DemoMap(
            selectedPlace = state.selectedPlace,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            mode = state.mapMode,
            flatSettings = state.flatMapSettings,
            azimuthalSettings = state.azimuthalMapSettings,
            sphereSettings = state.sphereSettings,
            styleSettings = state.mapStyleSettings,
            cameraRequest = state.cameraRequest,
            onCameraRequestConsumed = { onAction(DemoAction.ConsumeCameraRequest) },
            onSelectMode = { onAction(DemoAction.SelectMapMode(it)) },
            onOpenMapSettings = { onAction(DemoAction.OpenMapSettings) },
            onOpenMapStyleSettings = { onAction(DemoAction.OpenMapStyleSettings) },
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        ) {
            PlaceSearchBar(
                query = state.searchQuery,
                onOpenSearch = { onAction(DemoAction.OpenSearch) },
                onClear = if (state.searchQuery.isNotEmpty() || state.selectedPlace != null) {
                    { onAction(DemoAction.ClearSearchAndSelection) }
                } else null,
            )
            Spacer(modifier = Modifier.height(12.dp))
            CountryListPane(
                countries = state.countries,
                isLoading = state.isLoadingCountries,
                selectedLibrary = state.selectedLibrary,
                selectedViewMode = state.selectedViewMode,
                onCountryClick = { onAction(DemoAction.InspectCountry(it)) },
                onCountryGoTo = { onAction(DemoAction.GoToCountry(it)) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun CompactDemoLayout(
    state: DemoState,
    onAction: (DemoAction) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        PlaceSearchBar(
            query = state.searchQuery,
            onOpenSearch = { onAction(DemoAction.OpenSearch) },
            onClear = if (state.searchQuery.isNotEmpty() || state.selectedPlace != null) {
                { onAction(DemoAction.ClearSearchAndSelection) }
            } else null,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(12.dp))
        DemoMap(
            selectedPlace = state.selectedPlace,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            mode = state.mapMode,
            flatSettings = state.flatMapSettings,
            azimuthalSettings = state.azimuthalMapSettings,
            sphereSettings = state.sphereSettings,
            styleSettings = state.mapStyleSettings,
            cameraRequest = state.cameraRequest,
            onCameraRequestConsumed = { onAction(DemoAction.ConsumeCameraRequest) },
            onSelectMode = { onAction(DemoAction.SelectMapMode(it)) },
            onOpenMapSettings = { onAction(DemoAction.OpenMapSettings) },
            onOpenMapStyleSettings = { onAction(DemoAction.OpenMapStyleSettings) },
        )
    }
}
