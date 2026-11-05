package com.moinofactory.moinogeo.maps.sphere

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.moinofactory.moinogeo.maps.core.GeoPoint
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SphereProjectionTest {
    private val projection = SphereProjection(
        size = Size(800f, 600f),
        center = GeoPoint(0.0, 0.0),
        distance = 3f,
        globeRadius = 1f,
    )

    @Test
    fun geoCartesianRoundTripNearEquator() {
        val point = GeoPoint(10.0, 20.0)
        val cart = projection.toCartesian(point)
        val back = projection.toGeoPoint(cart)
        assertTrue(abs(back.latitude - point.latitude) < 1e-4)
        assertTrue(abs(back.longitude - point.longitude) < 1e-4)
    }

    @Test
    fun centerProjectsNearViewportMiddle() {
        val offset = projection.toOffset(GeoPoint(0.0, 0.0))
        assertNotNull(offset)
        assertTrue(abs(offset.x - 400f) < 8f)
        assertTrue(abs(offset.y - 300f) < 8f)
    }

    @Test
    fun backFaceIsHidden() {
        assertNull(projection.toOffset(GeoPoint(0.0, 180.0), frontFaceOnly = true))
        assertTrue(projection.isFrontFacing(GeoPoint(0.0, 0.0)))
    }

    @Test
    fun rayHitReturnsGeoNearCenter() {
        val hit = projection.toGeoPoint(Offset(400f, 300f))
        assertNotNull(hit)
        assertTrue(abs(hit.latitude) < 5.0)
        assertTrue(abs(hit.longitude) < 5.0)
    }
}
