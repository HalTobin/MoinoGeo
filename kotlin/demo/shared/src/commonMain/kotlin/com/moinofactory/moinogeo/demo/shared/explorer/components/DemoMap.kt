package com.moinofactory.moinogeo.demo.shared.explorer.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.moinofactory.moinogeo.core.domain.model.GeoPlace
import com.moinofactory.moinogeo.core.domain.model.GeoPlaceType
import com.moinofactory.moinogeo.demo.shared.explorer.DemoMapMode
import com.moinofactory.moinogeo.demo.shared.explorer.DemoMapSettings
import com.moinofactory.moinogeo.demo.shared.explorer.DemoMapStyleSettings
import com.moinofactory.moinogeo.demo.shared.ui.icon.Icons
import com.moinofactory.moinogeo.demo.shared.ui.icon.palette
import com.moinofactory.moinogeo.demo.shared.ui.icon.settings
import com.moinofactory.moinogeo.maps.countries.DefaultBoundaryProvider
import com.moinofactory.moinogeo.maps.core.BoundaryFeature
import com.moinofactory.moinogeo.maps.core.GeoPoint
import com.moinofactory.moinogeo.maps.plane.AzimuthalMap
import com.moinofactory.moinogeo.maps.plane.AzimuthalMapState
import com.moinofactory.moinogeo.maps.plane.MapScope
import com.moinofactory.moinogeo.maps.plane.PlaneMap
import com.moinofactory.moinogeo.maps.plane.PlaneMapState
import com.moinofactory.moinogeo.maps.plane.rememberAzimuthalMapState
import com.moinofactory.moinogeo.maps.plane.rememberPlaneMapState
import com.moinofactory.moinogeo.maps.topography.DefaultTopographyProvider
import kotlin.math.roundToInt

private val HighlightFill = Color(0x66FF6D00)
private val HighlightBorder = Color(0xFFFF6D00)

/**
 * Demo wrapper around [PlaneMap] and [AzimuthalMap] using bundled Natural Earth
 * topography and countries. Each mode keeps its own camera. The map canvas uses a
 * determined aspect ratio from [DemoMapSettings] stretch values instead of
 * stretching to fill the parent.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DemoMap(
    selectedPlace: GeoPlace?,
    mode: DemoMapMode,
    flatSettings: DemoMapSettings,
    azimuthalSettings: DemoMapSettings,
    styleSettings: DemoMapStyleSettings,
    onSelectMode: (DemoMapMode) -> Unit,
    modifier: Modifier = Modifier,
    onOpenMapSettings: (() -> Unit)? = null,
    onOpenMapStyleSettings: (() -> Unit)? = null,
) {
    val boundaryProvider = remember { DefaultBoundaryProvider() }
    val topographyProvider = remember { DefaultTopographyProvider() }
    val boundaryData = remember(boundaryProvider) { boundaryProvider.getBoundaryData() }
    val planeState = rememberPlaneMapState()
    val azimuthalState = rememberAzimuthalMapState()
    var clickedPoint by remember { mutableStateOf<GeoPoint?>(null) }
    var overlayClickLabel by remember { mutableStateOf<String?>(null) }

    BindPlaneCamera(planeState, flatSettings)
    BindAzimuthalCamera(azimuthalState, azimuthalSettings)

    val selectedCountryFeature: BoundaryFeature? = remember(selectedPlace, boundaryData) {
        val place = selectedPlace ?: return@remember null
        if (place.type != GeoPlaceType.COUNTRY) return@remember null
        val iso = place.iso2?.takeIf { it.isNotBlank() } ?: return@remember null
        boundaryData.features.firstOrNull { feature ->
            feature.isoCode.equals(iso, ignoreCase = true)
        }
    }

    val markerPoint: GeoPoint? = remember(selectedPlace, selectedCountryFeature) {
        if (selectedCountryFeature != null) return@remember null
        val place = selectedPlace ?: return@remember null
        val lat = place.latitude ?: return@remember null
        val lon = place.longitude ?: return@remember null
        GeoPoint(latitude = lat, longitude = lon)
    }

    val activeSettings = when (mode) {
        DemoMapMode.Flat -> flatSettings
        DemoMapMode.Azimuthal -> azimuthalSettings
    }
    val mapAspectRatio = activeSettings.aspectRatio(mode.baseAspectRatio)
    val mapStyle = styleSettings.toMapStyle()

    Column(modifier = modifier) {
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            val containerAspect = if (maxHeight > 0.dp) {
                maxWidth / maxHeight
            } else {
                mapAspectRatio
            }
            val mapSizeModifier = if (containerAspect > mapAspectRatio) {
                Modifier
                    .fillMaxHeight()
                    .aspectRatio(mapAspectRatio)
            } else {
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(mapAspectRatio)
            }
            Surface(
                modifier = mapSizeModifier.clip(RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 1.dp,
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    when (mode) {
                        DemoMapMode.Flat -> PlaneMap(
                            modifier = Modifier.fillMaxSize(),
                            mapStyle = mapStyle,
                            boundaryProvider = boundaryProvider,
                            topographyProvider = topographyProvider,
                            state = planeState,
                            onMapClick = { point ->
                                clickedPoint = point
                                overlayClickLabel = null
                            },
                        ) {
                            PlaceOverlays(
                                selectedPlace = selectedPlace,
                                selectedCountryFeature = selectedCountryFeature,
                                markerPoint = markerPoint,
                                onOverlayClick = { label, point ->
                                    overlayClickLabel = label
                                    clickedPoint = point
                                },
                            )
                        }

                        DemoMapMode.Azimuthal -> AzimuthalMap(
                            modifier = Modifier.fillMaxSize(),
                            mapStyle = mapStyle,
                            boundaryProvider = boundaryProvider,
                            topographyProvider = topographyProvider,
                            state = azimuthalState,
                            onMapClick = { point ->
                                clickedPoint = point
                                overlayClickLabel = null
                            },
                        ) {
                            PlaceOverlays(
                                selectedPlace = selectedPlace,
                                selectedCountryFeature = selectedCountryFeature,
                                markerPoint = markerPoint,
                                onOverlayClick = { label, point ->
                                    overlayClickLabel = label
                                    clickedPoint = point
                                },
                            )
                        }
                    }

                    if (onOpenMapStyleSettings != null) {
                        MapOverlayIconButton(
                            onClick = onOpenMapStyleSettings,
                            contentDescription = "Map Style",
                            imageVector = Icons.palette,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(8.dp),
                        )
                    }

                    MapModeSelector(
                        selected = mode,
                        onSelect = onSelectMode,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 8.dp)
                            .widthIn(max = 280.dp)
                            .padding(horizontal = 56.dp),
                    )

                    if (onOpenMapSettings != null) {
                        MapOverlayIconButton(
                            onClick = onOpenMapSettings,
                            contentDescription = "Map Settings",
                            imageVector = Icons.settings,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp),
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = when {
                overlayClickLabel != null -> "Clicked: $overlayClickLabel"
                clickedPoint != null -> {
                    val lat = (clickedPoint!!.latitude * 10000).roundToInt() / 10000.0
                    val lon = (clickedPoint!!.longitude * 10000).roundToInt() / 10000.0
                    "Clicked: $lat°, $lon°"
                }
                else -> "Tap the map to inspect a location"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
    }
}

@Composable
private fun MapOverlayIconButton(
    onClick: () -> Unit,
    contentDescription: String,
    imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
            tonalElevation = 3.dp,
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription,
                modifier = Modifier.padding(8.dp),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MapModeSelector(
    selected: DemoMapMode,
    onSelect: (DemoMapMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        tonalElevation = 3.dp,
    ) {
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.padding(4.dp),
        ) {
            DemoMapMode.entries.forEachIndexed { index, mode ->
                SegmentedButton(
                    selected = selected == mode,
                    onClick = { onSelect(mode) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = DemoMapMode.entries.size,
                    ),
                    label = { Text(mode.title) },
                )
            }
        }
    }
}

@Composable
private fun BindPlaneCamera(state: PlaneMapState, settings: DemoMapSettings) {
    // Stretch is applied via the demo canvas aspect ratio, not projection scale.
    LaunchedEffect(Unit) {
        state.verticalScale = 1f
    }
    LaunchedEffect(settings.rotationAngle) {
        state.rotationAngle = settings.rotationAngle
    }
    LaunchedEffect(settings.centerLatitude, settings.centerLongitude) {
        state.centerLatitude = settings.centerLatitude.toDouble()
        state.setCenter(
            GeoPoint(
                latitude = settings.centerLatitude.toDouble(),
                longitude = settings.centerLongitude.toDouble(),
            ),
        )
    }
}

@Composable
private fun BindAzimuthalCamera(state: AzimuthalMapState, settings: DemoMapSettings) {
    // Stretch is applied via the demo canvas aspect ratio, not projection scale.
    LaunchedEffect(Unit) {
        state.verticalScale = 1f
    }
    LaunchedEffect(settings.rotationAngle) {
        state.rotationAngle = settings.rotationAngle
    }
    LaunchedEffect(settings.centerLatitude, settings.centerLongitude) {
        state.setCenter(
            GeoPoint(
                latitude = settings.centerLatitude.toDouble(),
                longitude = settings.centerLongitude.toDouble(),
            ),
        )
    }
}

@Composable
private fun MapScope.PlaceOverlays(
    selectedPlace: GeoPlace?,
    selectedCountryFeature: BoundaryFeature?,
    markerPoint: GeoPoint?,
    onOverlayClick: (label: String, point: GeoPoint?) -> Unit,
) {
    selectedCountryFeature?.let { feature ->
        Area(
            geometries = feature.geometries,
            fillColor = HighlightFill,
            borderColor = HighlightBorder,
            borderWidth = 2.5f,
            onClick = {
                onOverlayClick(selectedPlace?.name ?: feature.isoCode, null)
            },
        )
    }

    if (markerPoint != null && selectedPlace != null) {
        Marker(
            position = markerPoint,
            onClick = {
                onOverlayClick(selectedPlace.name, markerPoint)
            },
        ) {
            Text(
                text = selectedPlace.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF212121),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
