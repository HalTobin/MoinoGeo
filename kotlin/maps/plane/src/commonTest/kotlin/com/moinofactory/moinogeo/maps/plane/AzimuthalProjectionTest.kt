package com.moinofactory.moinogeo.maps.plane

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.moinofactory.moinogeo.maps.core.GeoPoint
import com.moinofactory.moinogeo.maps.core.GeoPolygon
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AzimuthalProjectionTest {
    private fun assertLongitude(expected: Double, actual: Double, tolerance: Double = 1e-3) {
        assertEquals(
            0.0,
            abs(wrapDeltaDegrees(actual - expected)),
            absoluteTolerance = tolerance,
        )
    }

    private val projection = AzimuthalProjection(
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
        val base = projection.toOffset(point)
        val scaled = stretched.toOffset(point)
        assertEquals(base.x, scaled.x, absoluteTolerance = 0.01f)
        assertTrue(scaled.y < base.y)
        val back = stretched.toGeoPoint(scaled)
        assertEquals(point.latitude, back.latitude, absoluteTolerance = 1e-4)
        assertEquals(point.longitude, back.longitude, absoluteTolerance = 1e-4)
    }

    @Test
    fun projectionOriginSitsAtScreenCenter() {
        val pivoted = projection.copy(centerLatitude = 45.0, centerLongitude = 10.0)
        val origin = pivoted.toOffset(GeoPoint(45.0, 10.0))
        assertEquals(180f, origin.x, absoluteTolerance = 0.01f)
        assertEquals(90f, origin.y, absoluteTolerance = 0.01f)
        val equator = pivoted.copy(verticalScale = 2f).toOffset(GeoPoint(0.0, 10.0))
        assertEquals(180f, equator.y, absoluteTolerance = 0.15f)
        val back = pivoted.toGeoPoint(origin)
        assertEquals(45.0, back.latitude, absoluteTolerance = 1e-4)
        assertEquals(10.0, back.longitude, absoluteTolerance = 1e-4)
    }

    @Test
    fun southPoleProjectsToCenterAndEquatorIsCircle() {
        val proj = projection.copy(centerLatitude = -90.0, centerLongitude = 0.0)
        val pole = proj.toOffset(GeoPoint(latitude = -90.0, longitude = 45.0))
        assertEquals(180f, pole.x, absoluteTolerance = 0.05f)
        assertEquals(90f, pole.y, absoluteTolerance = 0.05f)

        val longitudes = listOf(0.0, 45.0, 90.0, 135.0, 180.0, -90.0, -135.0)
        for (lon in longitudes) {
            val p = proj.toOffset(GeoPoint(0.0, lon))
            val radius = hypot((p.x - 180f).toDouble(), (p.y - 90f).toDouble())
            assertEquals(90.0, radius, absoluteTolerance = 0.15)
            val back = proj.toGeoPoint(p)
            assertEquals(0.0, back.latitude, absoluteTolerance = 1e-3)
            assertLongitude(lon, back.longitude)
        }

        val prime = proj.toOffset(GeoPoint(0.0, 0.0))
        val east = proj.toOffset(GeoPoint(0.0, 90.0))
        assertEquals(180f, prime.x, absoluteTolerance = 0.15f)
        assertTrue(prime.y < 90f)
        assertEquals(90f, east.y, absoluteTolerance = 0.15f)
        assertTrue(east.x > 180f)
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
    fun roundTripsNonZeroCenterAndRotation() {
        val proj = projection.copy(
            centerLatitude = 35.0,
            centerLongitude = -120.0,
            rotationAngle = 33f,
            offsetX = 12f,
            offsetY = -6f,
            zoom = 1.5f,
        )
        val samples = listOf(
            GeoPoint(35.0, -120.0),
            GeoPoint(48.8566, 2.3522),
            GeoPoint(-33.9, 18.4),
            GeoPoint(0.0, 180.0),
            GeoPoint(64.0, -150.0),
        )
        for (original in samples) {
            val back = proj.toGeoPoint(proj.toOffset(original))
            assertEquals(original.latitude, back.latitude, absoluteTolerance = 1e-3)
            assertLongitude(original.longitude, back.longitude)
        }
    }

    @Test
    fun roundTripsWithVerticalPivotAndStretch() {
        val proj = projection.copy(
            verticalScale = 1.5f,
            centerLatitude = -15.0,
            centerLongitude = 40.0,
        )
        val original = GeoPoint(latitude = -23.5, longitude = -46.6)
        val back = proj.toGeoPoint(proj.toOffset(original))
        assertEquals(original.latitude, back.latitude, absoluteTolerance = 1e-4)
        assertEquals(original.longitude, back.longitude, absoluteTolerance = 1e-4)
    }

    @Test
    fun angularDistanceReachesPiAtAntipode() {
        val proj = projection.copy(centerLatitude = 20.0, centerLongitude = -30.0)
        assertEquals(
            0.0,
            proj.angularDistance(GeoPoint(20.0, -30.0)),
            absoluteTolerance = 1e-6,
        )
        assertEquals(
            PI,
            proj.angularDistance(GeoPoint(-20.0, 150.0)),
            absoluteTolerance = 1e-6,
        )
    }

    @Test
    fun equatorArcCurvesAroundSouthPole() {
        val proj = projection.copy(centerLatitude = -90.0)
        val start = GeoPoint(0.0, 0.0)
        val end = GeoPoint(0.0, 90.0)
        val sample = densifySegment(start, end, proj).minBy { abs(it.longitude - 45.0) }
        assertEquals(0.0, sample.latitude, absoluteTolerance = 1e-6)
        val screen = proj.toOffset(sample)
        val chord = Offset(
            (proj.toOffset(start).x + proj.toOffset(end).x) / 2f,
            (proj.toOffset(start).y + proj.toOffset(end).y) / 2f,
        )
        val screenR = hypot((screen.x - 180f).toDouble(), (screen.y - 90f).toDouble())
        val chordR = hypot((chord.x - 180f).toDouble(), (chord.y - 90f).toDouble())
        assertEquals(90.0, screenR, absoluteTolerance = 1.0)
        assertTrue(screenR > chordR + 10.0)
    }

    @Test
    fun densifyTakesTheShortLongitudeArc() {
        val points = densifySegment(
            GeoPoint(0.0, 170.0),
            GeoPoint(0.0, -170.0),
            projection,
        )
        assertTrue(points.isNotEmpty())
        assertTrue(points.all { abs(it.longitude) >= 170.0 - 0.01 })
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
    fun worldCopiesCollapseOnTheSphere() {
        val point = GeoPoint(latitude = 10.0, longitude = 20.0)
        val primary = projection.toOffsetOnTile(point, tileX = 0)
        val east = projection.toOffsetOnTile(point, tileX = 1)
        assertEquals(primary.x, east.x, absoluteTolerance = 0.01f)
        assertEquals(primary.y, east.y, absoluteTolerance = 0.01f)
        assertEquals(0..0, AzimuthalProjection.HorizontalWrapTiles)
    }

    @Test
    fun clipRectBoundsTheProjectionDisk() {
        val rect = projection.clipRectForTile(0, overlapPx = 0f)
        assertEquals(0f, rect.left, absoluteTolerance = 0.05f)
        assertEquals(360f, rect.right, absoluteTolerance = 0.05f)
        assertEquals(-90f, rect.top, absoluteTolerance = 0.05f)
        assertEquals(270f, rect.bottom, absoluteTolerance = 0.05f)
        assertTrue(projection.clipRectForTile(1).isEmpty)
        assertTrue(projection.clipRectForTile(-1).isEmpty)
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
                GeoPoint(70.0, 180.0),
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
    fun verticalPanLimitFollowsDisk() {
        assertEquals(
            100f,
            maxVerticalOffset(
                viewportWidth = 200f,
                viewportHeight = 200f,
                zoom = 1f,
                verticalScale = 1f,
            ),
            absoluteTolerance = 0.01f,
        )
        assertEquals(
            150f,
            maxVerticalOffset(
                viewportWidth = 200f,
                viewportHeight = 200f,
                zoom = 2f,
                verticalScale = 1f,
            ),
            absoluteTolerance = 0.01f,
        )
    }
}
