package com.moinofactory.moinogeo.demo.shared.explorer.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.moinofactory.moinogeo.demo.shared.ui.icon.Icons
import com.moinofactory.moinogeo.demo.shared.ui.icon.settings
import com.moinofactory.moinogeo.maps.countries.DefaultBoundaryProvider
import com.moinofactory.moinogeo.maps.core.BoundaryFeature
import com.moinofactory.moinogeo.maps.core.GeoPoint
import com.moinofactory.moinogeo.maps.core.MapDefaults
import com.moinofactory.moinogeo.maps.plane.PlaneMap
import com.moinofactory.moinogeo.maps.plane.rememberPlaneMapState
import com.moinofactory.moinogeo.maps.topography.DefaultTopographyProvider
import kotlin.math.roundToInt

private val HighlightFill = Color(0x66FF6D00)
private val HighlightBorder = Color(0xFFFF6D00)

/**
 * Demo wrapper around [PlaneMap] using bundled Natural Earth topography + countries.
 * Countries get an [Area] highlight; other places get a simple [Marker].
 */
@Composable
internal fun DemoMap(
    selectedPlace: GeoPlace?,
    modifier: Modifier = Modifier,
    verticalScale: Float = 1f,
    centerLatitude: Float = 0f,
    centerLongitude: Float = 0f,
    rotationAngle: Float = 0f,
    bordersScaleWithZoom: Boolean = true,
    onOpenMapSettings: (() -> Unit)? = null,
) {
    val boundaryProvider = remember { DefaultBoundaryProvider() }
    val topographyProvider = remember { DefaultTopographyProvider() }
    val boundaryData = remember(boundaryProvider) { boundaryProvider.getBoundaryData() }
    val mapState = rememberPlaneMapState()
    var clickedPoint by remember { mutableStateOf<GeoPoint?>(null) }
    var overlayClickLabel by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(verticalScale) {
        mapState.verticalScale = verticalScale
    }
    LaunchedEffect(rotationAngle) {
        mapState.rotationAngle = rotationAngle
    }
    LaunchedEffect(centerLatitude, centerLongitude) {
        mapState.centerLatitude = centerLatitude.toDouble()
        mapState.setCenter(
            GeoPoint(
                latitude = centerLatitude.toDouble(),
                longitude = centerLongitude.toDouble(),
            ),
        )
    }

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

    Column(modifier = modifier) {
        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 1.dp,
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                PlaneMap(
                    modifier = Modifier.fillMaxSize(),
                    mapStyle = MapDefaults.style(
                        borderWidth = 1f,
                        borderColor = Color(0xFF455A64),
                        borderScaleWithZoom = bordersScaleWithZoom,
                    ),
                    boundaryProvider = boundaryProvider,
                    topographyProvider = topographyProvider,
                    state = mapState,
                    onMapClick = { point ->
                        clickedPoint = point
                        overlayClickLabel = null
                    },
                ) {
                    selectedCountryFeature?.let { feature ->
                        Area(
                            geometries = feature.geometries,
                            fillColor = HighlightFill,
                            borderColor = HighlightBorder,
                            borderWidth = 2.5f,
                            onClick = {
                                overlayClickLabel = selectedPlace?.name ?: feature.isoCode
                                clickedPoint = null
                            },
                        )
                    }

                    if (markerPoint != null && selectedPlace != null) {
                        Marker(
                            position = markerPoint,
                            onClick = {
                                overlayClickLabel = selectedPlace.name
                                clickedPoint = markerPoint
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

                if (onOpenMapSettings != null) {
                    IconButton(
                        onClick = onOpenMapSettings,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                            tonalElevation = 3.dp,
                        ) {
                            Icon(
                                imageVector = Icons.settings,
                                contentDescription = "Map Settings",
                                modifier = Modifier.padding(8.dp),
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
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
