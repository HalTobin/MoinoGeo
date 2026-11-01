package com.moinofactory.moinogeo.demo.shared.explorer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moinofactory.moinogeo.core.domain.model.GeoPlace
import com.moinofactory.moinogeo.core.domain.model.GeoPlaceType
import com.moinofactory.moinogeo.maps.countries.DefaultBoundaryProvider
import com.moinofactory.moinogeo.maps.core.BoundaryFeature
import com.moinofactory.moinogeo.maps.core.GeoPoint
import com.moinofactory.moinogeo.maps.core.MapDefaults
import com.moinofactory.moinogeo.maps.plane.PlaneMap
import com.moinofactory.moinogeo.maps.plane.rememberPlaneMapState
import com.moinofactory.moinogeo.maps.topography.DefaultTopographyProvider

private val HighlightFill = Color(0x66FF6D00)
private val HighlightBorder = Color(0xFFFF6D00)
private val MarkerChipBg = Color(0xE6FFFFFF)

/**
 * Demo wrapper around [PlaneMap] using bundled Natural Earth topography + countries.
 * Highlights the selected place (country area and/or lat-lon marker).
 */
@Composable
internal fun DemoMap(
    selectedPlace: GeoPlace?,
    modifier: Modifier = Modifier,
) {
    val boundaryProvider = remember { DefaultBoundaryProvider() }
    val topographyProvider = remember { DefaultTopographyProvider() }
    val boundaryData = remember(boundaryProvider) { boundaryProvider.getBoundaryData() }
    val mapState = rememberPlaneMapState()

    val selectedCountryFeature: BoundaryFeature? = remember(selectedPlace, boundaryData) {
        val iso = selectedPlace?.iso2?.takeIf { it.isNotBlank() } ?: return@remember null
        boundaryData.features.firstOrNull { feature ->
            feature.isoCode.equals(iso, ignoreCase = true)
        }
    }

    val markerPoint: GeoPoint? = remember(selectedPlace) {
        val place = selectedPlace ?: return@remember null
        val lat = place.latitude ?: return@remember null
        val lon = place.longitude ?: return@remember null
        GeoPoint(latitude = lat, longitude = lon)
    }

    Surface(
        modifier = modifier.clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp,
    ) {
        PlaneMap(
            modifier = Modifier.fillMaxSize(),
            mapStyle = MapDefaults.style(
                borderWidth = 1f,
                borderColor = Color(0xFF455A64),
            ),
            boundaryProvider = boundaryProvider,
            topographyProvider = topographyProvider,
            state = mapState,
        ) {
            selectedCountryFeature?.let { feature ->
                Area(
                    geometries = feature.geometries,
                    fillColor = HighlightFill,
                    borderColor = HighlightBorder,
                    borderWidth = 2.5f,
                )
            }

            if (markerPoint != null && selectedPlace != null) {
                Marker(
                    position = markerPoint,
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    PlaceMarkerChip(place = selectedPlace)
                }
            }
        }
    }
}

@Composable
private fun PlaceMarkerChip(place: GeoPlace) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = 4.dp),
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MarkerChipBg)
                .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            Column {
                Text(
                    text = place.name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF212121),
                )
                Text(
                    text = buildString {
                        append(place.type.label())
                        place.iso2?.let { append(" · $it") }
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF616161),
                )
            }
        }
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(HighlightBorder)
                .padding(5.dp),
        )
    }
}

private fun GeoPlaceType.label(): String =
    name.lowercase().replaceFirstChar { it.titlecase() }
