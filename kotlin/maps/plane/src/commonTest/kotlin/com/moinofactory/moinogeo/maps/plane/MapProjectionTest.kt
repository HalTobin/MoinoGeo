package com.moinofactory.moinogeo.maps.plane

import androidx.compose.ui.geometry.Size
import com.moinofactory.moinogeo.maps.core.GeoPoint
import com.moinofactory.moinogeo.maps.core.GeoPolygon
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
    fun verticalScaleStretchesYOnly() {
        val stretched = projection.copy(verticalScale = 2f)
        val point = GeoPoint(latitude = 45.0, longitude = 0.0)
        val base = projection.toOffsetOnTile(point, tileX = 0)
        val scaled = stretched.toOffsetOnTile(point, tileX = 0)
        assertEquals(base.x, scaled.x, absoluteTolerance = 0.01f)
        // 45°N is above the equator pivot; stretch pushes it farther north on screen
        assertTrue(scaled.y < base.y)
        val back = stretched.toGeoPoint(scaled)
        assertEquals(point.latitude, back.latitude, absoluteTolerance = 1e-4)
        assertEquals(point.longitude, back.longitude, absoluteTolerance = 1e-4)
    }

    @Test
    fun canvasAspectDoesNotDistortEquirectangularProportions() {
        val square = MapProjection(
            size = Size(360f, 360f),
            zoom = 1f,
            offsetX = 0f,
            offsetY = 0f,
        )
        val equator = square.toOffsetOnTile(GeoPoint(0.0, 0.0), tileX = 0)
        val north45 = square.toOffsetOnTile(GeoPoint(45.0, 0.0), tileX = 0)
        val east45 = square.toOffsetOnTile(GeoPoint(0.0, 45.0), tileX = 0)
        // Same angular span keeps the same pixel length on a non-2:1 canvas
        assertEquals(
            kotlin.math.abs(equator.y - north45.y),
            kotlin.math.abs(east45.x - equator.x),
            absoluteTolerance = 0.01f,
        )
    }

    @Test
    fun verticalScalePivotsAroundCenterLatitude() {
        val pivoted = projection.copy(verticalScale = 2f, centerLatitude = 45.0)
        val pivot = pivoted.toOffsetOnTile(GeoPoint(45.0, 0.0), tileX = 0)
        val equator = pivoted.toOffsetOnTile(GeoPoint(0.0, 0.0), tileX = 0)
        assertEquals(90f, pivot.y, absoluteTolerance = 0.01f)
        assertEquals(180f, equator.y, absoluteTolerance = 0.01f)
        val back = pivoted.toGeoPoint(equator)
        assertEquals(0.0, back.latitude, absoluteTolerance = 1e-4)
    }

    @Test
    fun roundTripsWithRotation() {
        val proj = projection.copy(
            rotationAngle = 45f,
            centerLatitude = -90.0,
            verticalScale = 1.2f,
        )
        val original = GeoPoint(latitude = -75.0, longitude = 45.0)
        val offset = proj.toOffset(original)
        val back = proj.toGeoPoint(offset)
        assertEquals(original.latitude, back.latitude, absoluteTolerance = 1e-4)
        assertEquals(original.longitude, back.longitude, absoluteTolerance = 1e-4)
    }

    @Test
    fun roundTripsGeoPoint() {
        val original = GeoPoint(latitude = 48.8566, longitude = 2.3522)
        val back = projection.toGeoPoint(projection.toOffset(original))
        assertEquals(original.latitude, back.latitude, absoluteTolerance = 1e-4)
        assertEquals(original.longitude, back.longitude, absoluteTolerance = 1e-4)
    }

    @Test
    fun roundTripsWithVerticalPivotAndStretch() {
        val proj = projection.copy(
            verticalScale = 1.5f,
            centerLatitude = -15.0,
        )
        val original = GeoPoint(latitude = -23.5, longitude = -46.6)
        val back = proj.toGeoPoint(proj.toOffset(original))
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
    fun isArtificialBorderEdgeDetected() {
        // Antimeridian cuts (e.g. Russia split at 180° or Antarctica cut down to South Pole)
        assertTrue(
            isArtificialBorderEdge(
                GeoPoint(65.0, 180.0),
                GeoPoint(70.0, 180.0),
            ),
        )
        assertTrue(
            isArtificialBorderEdge(
                GeoPoint(65.0, -180.0),
                GeoPoint(70.0, -180.0),
            ),
        )
        // South pole and north pole edges
        assertTrue(
            isArtificialBorderEdge(
                GeoPoint(-90.0, 180.0),
                GeoPoint(-90.0, -180.0),
            ),
        )
        assertTrue(
            isArtificialBorderEdge(
                GeoPoint(90.0, 0.0),
                GeoPoint(90.0, 100.0),
            ),
        )
        // Dateline wrap jumps
        assertTrue(
            isArtificialBorderEdge(
                GeoPoint(10.0, 179.0),
                GeoPoint(10.0, -179.0),
            ),
        )
        // Normal geographic borders should NOT be flagged
        assertFalse(
            isArtificialBorderEdge(
                GeoPoint(48.0, 2.0),
                GeoPoint(49.0, 3.0),
            ),
        )
        assertFalse(
            isArtificialBorderEdge(
                GeoPoint(65.0, 179.0),
                GeoPoint(70.0, 178.0),
            ),
        )
    }

    @Test
    fun antarcticaRingIdentifiesArtificialEdges() {
        val antarcticaRing = listOf(
            GeoPoint(-84.71, 180.0),
            GeoPoint(-90.0, 180.0),
            GeoPoint(-90.0, -180.0),
            GeoPoint(-84.71, -180.0),
            GeoPoint(-70.0, -90.0),
            GeoPoint(-70.0, 0.0),
            GeoPoint(-70.0, 90.0),
            GeoPoint(-84.47, 178.28),
            GeoPoint(-84.71, 180.0),
        )
        // 0->1: antimeridian cut
        assertTrue(isArtificialBorderEdge(antarcticaRing[0], antarcticaRing[1]))
        // 1->2: South Pole cut
        assertTrue(isArtificialBorderEdge(antarcticaRing[1], antarcticaRing[2]))
        // 2->3: antimeridian cut
        assertTrue(isArtificialBorderEdge(antarcticaRing[2], antarcticaRing[3]))
        // 3->4: real coastline
        assertFalse(isArtificialBorderEdge(antarcticaRing[3], antarcticaRing[4]))
        // 7->8: real coastline ending at 180°
        assertFalse(isArtificialBorderEdge(antarcticaRing[7], antarcticaRing[8]))
    }

    @Test
    fun antimeridianEdgeDetected() {
        assertTrue(
            isAntimeridianEdge(
                GeoPoint(65.0, 180.0),
                GeoPoint(70.0, 180.0),
            ),
        )
        assertTrue(
            isAntimeridianEdge(
                GeoPoint(65.0, -180.0),
                GeoPoint(70.0, 180.0),
            ),
        )
        assertFalse(
            isAntimeridianEdge(
                GeoPoint(65.0, 179.0),
                GeoPoint(70.0, 178.0),
            ),
        )
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

    @Test
    fun polygonContainsPointAndHoles() {
        val square = GeoPolygon(
            outerBounds = listOf(
                GeoPoint(0.0, 0.0),
                GeoPoint(0.0, 10.0),
                GeoPoint(10.0, 10.0),
                GeoPoint(10.0, 0.0),
                GeoPoint(0.0, 0.0),
            ),
            innerHoles = listOf(
                listOf(
                    GeoPoint(3.0, 3.0),
                    GeoPoint(3.0, 6.0),
                    GeoPoint(6.0, 6.0),
                    GeoPoint(6.0, 3.0),
                    GeoPoint(3.0, 3.0),
                ),
            ),
        )
        assertTrue(square.contains(GeoPoint(1.0, 1.0)))
        assertFalse(square.contains(GeoPoint(4.0, 4.0))) // hole
        assertFalse(square.contains(GeoPoint(20.0, 20.0)))
    }

    @Test
    fun maxVerticalOffsetAtFitIsZero() {
        // 400×200 is geographic 2:1, so zoom 1 fits exactly
        assertEquals(
            0f,
            maxVerticalOffset(
                viewportWidth = 400f,
                viewportHeight = 200f,
                zoom = 1f,
                verticalScale = 1f,
                centerLatitude = 0.0,
            ),
        )
        assertEquals(
            50f,
            maxVerticalOffset(
                viewportWidth = 400f,
                viewportHeight = 200f,
                zoom = 2f,
                verticalScale = 1f,
                centerLatitude = 0.0,
            ),
            absoluteTolerance = 0.01f,
        )
    }
}
