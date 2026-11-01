package com.moinofactory.moinogeo.maps.plane

import androidx.compose.ui.geometry.Size
import com.moinofactory.moinogeo.maps.core.GeoPoint
import com.moinofactory.moinogeo.maps.core.GeoPolygon
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class MapProjectionTest {
    private val projection = MapProjection(
        size = Size(360f, 180f),
        zoom = 1f,
        offsetX = 0f,
        offsetY = 0f,
    )

    @Test
    fun projectsEquatorPrimeMeridianToCenter() {
        val offset = projection.toOffset(GeoPoint(latitude = 0.0, longitude = 0.0))
        assertEquals(180f, offset.x, absoluteTolerance = 0.01f)
        assertEquals(90f, offset.y, absoluteTolerance = 0.01f)
    }

    @Test
    fun roundTripsGeoPoint() {
        val original = GeoPoint(latitude = 48.8566, longitude = 2.3522)
        val back = projection.toGeoPoint(projection.toOffset(original))
        assertEquals(original.latitude, back.latitude, absoluteTolerance = 1e-4)
        assertEquals(original.longitude, back.longitude, absoluteTolerance = 1e-4)
    }

    @Test
    fun metersToPixelsScalesWithZoom() {
        val atZoom1 = projection.metersToPixels(111_320.0, atLatitude = 0.0)
        val zoomed = projection.copy(zoom = 2f).metersToPixels(111_320.0, atLatitude = 0.0)
        // ~1° of longitude at equator on a 360px-wide map ≈ 1px at zoom 1
        assertEquals(1f, atZoom1, absoluteTolerance = 0.05f)
        assertEquals(2f, zoomed, absoluteTolerance = 0.05f)
    }

    @Test
    fun approximateCenterAveragesVertices() {
        val polygon = GeoPolygon(
            outerBounds = listOf(
                GeoPoint(0.0, 0.0),
                GeoPoint(0.0, 2.0),
                GeoPoint(2.0, 2.0),
                GeoPoint(2.0, 0.0),
                GeoPoint(0.0, 0.0),
            ),
        )
        val center = assertNotNull(listOf(polygon).approximateCenter())
        assertEquals(0.8, center.latitude, absoluteTolerance = 1e-6)
        assertEquals(0.8, center.longitude, absoluteTolerance = 1e-6)
    }
}
