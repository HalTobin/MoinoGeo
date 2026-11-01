package com.moinofactory.moinogeo.maps.plane

import androidx.compose.ui.geometry.Size
import com.moinofactory.moinogeo.maps.core.GeoPoint
import com.moinofactory.moinogeo.maps.core.GeoPolygon
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MapProjectionTest {
    private val projection = MapProjection(
        size = Size(360f, 180f),
        zoom = 1f,
        offsetX = 0f,
        offsetY = 0f,
    )

    @Test
    fun projectsEquatorPrimeMeridianToCenter() {
        val offset = projection.toOffsetOnTile(GeoPoint(latitude = 0.0, longitude = 0.0), tileX = 0)
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

    @Test
    fun horizontalTileShiftsByWorldWidth() {
        val point = GeoPoint(latitude = 10.0, longitude = 20.0)
        val primary = projection.toOffsetOnTile(point, tileX = 0)
        val east = projection.toOffsetOnTile(point, tileX = 1)
        assertEquals(primary.x + projection.worldWidth, east.x, absoluteTolerance = 0.01f)
        assertEquals(primary.y, east.y, absoluteTolerance = 0.01f)
    }

    @Test
    fun clipBandsMeetAtSeam() {
        val left = projection.clipRectForTile(-1, overlapPx = 0f)
        val mid = projection.clipRectForTile(0, overlapPx = 0f)
        assertEquals(left.right, mid.left, absoluteTolerance = 0.05f)
        assertTrue(mid.right > mid.left)
    }

    @Test
    fun wrapOffsetCenteredKeepsHorizontalPanBounded() {
        assertEquals(10f, wrapOffsetCentered(10f, period = 360f), absoluteTolerance = 0.01f)
        assertEquals(-10f, wrapOffsetCentered(350f, period = 360f), absoluteTolerance = 0.01f)
    }

    @Test
    fun unwrapKeepsAntimeridianEdgesShort() {
        val ring = listOf(
            GeoPoint(latitude = -80.0, longitude = 170.0),
            GeoPoint(latitude = -90.0, longitude = 180.0),
            GeoPoint(latitude = -90.0, longitude = -180.0),
            GeoPoint(latitude = -80.0, longitude = -170.0),
            GeoPoint(latitude = -80.0, longitude = 170.0),
        )
        val map = unwrapRingToMap(ring, worldWidth = 360f, worldHeight = 180f)
        assertTrue(map.size >= 3)
        for (i in 1 until map.size) {
            assertTrue(
                abs(map[i].x - map[i - 1].x) <= 180f + 0.01f,
                "edge $i jumped by ${abs(map[i].x - map[i - 1].x)}",
            )
        }
        // Duplicate close vertex dropped; first/last may sit ~360° apart in
        // unwrapped space (polar continent) — that is handled at fill time.
        assertFalse(sameApprox(map.first(), map.last()))
    }

    @Test
    fun antarcticaUnwrapStaysSouthernAndWide() {
        val ring = listOf(
            GeoPoint(-70.0, -60.0),
            GeoPoint(-75.0, 0.0),
            GeoPoint(-70.0, 120.0),
            GeoPoint(-90.0, 180.0),
            GeoPoint(-90.0, -180.0),
            GeoPoint(-80.0, -120.0),
            GeoPoint(-70.0, -60.0),
        )
        val map = unwrapRingToMap(ring, worldWidth = 360f, worldHeight = 180f)
        // All coast samples south of ~60S → map Y below equator (y=90).
        assertTrue(map.all { it.y >= 90f - 1f })
        // Unwrapped ring spans ~a full world so fill can close via south edge.
        val span = map.maxOf { it.x } - map.minOf { it.x }
        assertTrue(span > 180f, "expected wide polar ring, span=$span")
    }

    @Test
    fun worldSpanningDetectsOceanShell() {
        val ocean = GeoPolygon(
            outerBounds = listOf(
                GeoPoint(0.0, -180.0),
                GeoPoint(0.0, 180.0),
                GeoPoint(10.0, 180.0),
                GeoPoint(10.0, -180.0),
                GeoPoint(0.0, -180.0),
            ),
        )
        val lake = GeoPolygon(
            outerBounds = listOf(
                GeoPoint(0.0, 0.0),
                GeoPoint(0.0, 1.0),
                GeoPoint(1.0, 1.0),
                GeoPoint(1.0, 0.0),
                GeoPoint(0.0, 0.0),
            ),
        )
        assertTrue(ocean.isWorldSpanning())
        assertFalse(lake.isWorldSpanning())
    }

    private fun sameApprox(a: androidx.compose.ui.geometry.Offset, b: androidx.compose.ui.geometry.Offset): Boolean =
        abs(a.x - b.x) < 1f && abs(a.y - b.y) < 1f
}
