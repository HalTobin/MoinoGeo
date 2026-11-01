package com.moinofactory.moinogeo.demo.shared.explorer.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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

/**
 * Demo wrapper around [PlaneMap] using bundled Natural Earth topography + countries.
 * Countries get an [Area] highlight; other places get a simple [Marker].
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
                Marker(position = markerPoint) {
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
    }
}
