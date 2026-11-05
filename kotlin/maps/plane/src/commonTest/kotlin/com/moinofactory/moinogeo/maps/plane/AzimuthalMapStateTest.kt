package com.moinofactory.moinogeo.maps.plane

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.moinofactory.moinogeo.maps.core.GeoPoint
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AzimuthalMapStateTest {
    @Test
    fun testZoomInAndOut() {
        val state = AzimuthalMapState(initialZoom = 1.0f, maxZoom = 5.0f)
        state.zoomIn()
        assertEquals(1.25f, state.zoom)
        state.zoomOut()
        assertEquals(1.0f, state.zoom)
    }

    @Test
    fun testReset() {
        val state = AzimuthalMapState(
            initialZoom = 2.0f,
            initialOffsetX = 50f,
            initialOffsetY = 50f,
            initialVerticalScale = 1.5f,
            initialCenterLatitude = 20.0,
            initialCenterLongitude = 15.0,
            initialRotationAngle = 20f,
        )
        state.viewportSize = Size(400f, 200f)
        state.reset()
        assertEquals(1.0f, state.zoom)
        assertEquals(0f, state.offsetX)
        assertEquals(0f, state.offsetY)
        assertEquals(1.0f, state.verticalScale)
        assertEquals(0.0, state.centerLatitude)
        assertEquals(0.0, state.centerLongitude)
        assertEquals(0f, state.rotationAngle)
    }

    @Test
    fun zoomAtKeepsFocalPointStable() {
        val state = AzimuthalMapState(initialZoom = 1f)
        state.viewportSize = Size(400f, 200f)
        val focalX = 300f
        val focalY = 100f
        val before = state.offsetX
        state.zoomAt(focalX, focalY, factor = 2f)
        assertEquals(2f, state.zoom)
        assertEquals(
            before + (focalX - 200f) * (0.5f - 1f),
            state.offsetX,
            absoluteTolerance = 0.01f,
        )
        assertEquals(0f, state.offsetY, absoluteTolerance = 0.01f)
    }

    @Test
    fun zoomAtKeepsGeographicPointWithRotation() {
        val state = AzimuthalMapState(
            initialZoom = 1f,
            initialRotationAngle = 25f,
            initialCenterLongitude = 10.0,
        )
        state.viewportSize = Size(400f, 300f)
        val focal = Offset(260f, 140f)
        val before = state.projection().toGeoPoint(focal)
        state.zoomAt(focal.x, focal.y, factor = 2f)
        val after = state.projection().toGeoPoint(focal)
        assertEquals(before.latitude, after.latitude, absoluteTolerance = 1e-3)
        assertEquals(before.longitude, after.longitude, absoluteTolerance = 1e-3)
    }

    @Test
    fun panByDividesByZoom() {
        val state = AzimuthalMapState(initialZoom = 2f)
        state.viewportSize = Size(400f, 200f)
        state.panBy(deltaX = 40f, deltaY = 20f)
        assertEquals(20f, state.offsetX)
        assertEquals(10f, state.offsetY)
    }

    @Test
    fun panByUsesZoomForYEvenWithVerticalScale() {
        val state = AzimuthalMapState(initialZoom = 2f, initialVerticalScale = 2f)
        state.viewportSize = Size(400f, 200f)
        state.panBy(deltaX = 0f, deltaY = 40f)
        assertEquals(20f, state.offsetY, absoluteTolerance = 0.01f)
    }

    @Test
    fun clampsVerticalOffsetInsideDisk() {
        val state = AzimuthalMapState(initialZoom = 2f)
        state.viewportSize = Size(400f, 200f)
        state.offsetY = 500f
        state.clampVerticalOffset()
        assertEquals(150f, state.offsetY, absoluteTolerance = 0.01f)

        state.offsetY = -500f
        state.clampVerticalOffset()
        assertEquals(-150f, state.offsetY, absoluteTolerance = 0.01f)
    }

    @Test
    fun zoomOneAllowsVerticalPanInsideDisk() {
        val state = AzimuthalMapState(initialZoom = 1f, initialOffsetY = 40f)
        state.viewportSize = Size(400f, 200f)
        assertEquals(40f, state.offsetY, absoluteTolerance = 0.01f)

        state.offsetY = 500f
        state.clampVerticalOffset()
        assertEquals(100f, state.offsetY, absoluteTolerance = 0.01f)
    }

    @Test
    fun setCenterPlacesProjectionOriginAtScreenCenter() {
        val state = AzimuthalMapState(initialZoom = 1f)
        state.viewportSize = Size(360f, 180f)
        state.offsetX = 12f
        state.offsetY = -4f
        state.setCenter(GeoPoint(latitude = 0.0, longitude = 90.0))
        assertEquals(0f, state.offsetX, absoluteTolerance = 0.01f)
        assertEquals(0f, state.offsetY, absoluteTolerance = 0.01f)
        assertEquals(0.0, state.centerLatitude)
        assertEquals(90.0, state.centerLongitude)
        assertEquals(0.0, state.cameraCenter.latitude, absoluteTolerance = 1e-4)
        assertEquals(90.0, state.cameraCenter.longitude, absoluteTolerance = 1e-4)
        val origin = state.projection().toOffset(GeoPoint(0.0, 90.0))
        assertEquals(180f, origin.x, absoluteTolerance = 0.05f)
        assertEquals(90f, origin.y, absoluteTolerance = 0.05f)
    }

    @Test
    fun setCenterQueuesUntilViewportReady() {
        val state = AzimuthalMapState(initialZoom = 1f)
        state.setCenter(GeoPoint(latitude = 45.0, longitude = 0.0))
        state.viewportSize = Size(360f, 180f)
        assertEquals(45.0, state.cameraCenter.latitude, absoluteTolerance = 1e-3)
        assertEquals(0.0, state.cameraCenter.longitude, absoluteTolerance = 1e-3)
        assertEquals(45.0, state.centerLatitude)
        assertEquals(0.0, state.centerLongitude)
    }

    @Test
    fun moveToSnapsProjectionOrigin() {
        val state = AzimuthalMapState(initialZoom = 1f)
        state.viewportSize = Size(360f, 180f)
        state.offsetX = 8f
        state.moveTo(GeoPoint(latitude = 10.0, longitude = -45.0))
        assertEquals(0f, state.offsetX, absoluteTolerance = 0.01f)
        assertEquals(0f, state.offsetY, absoluteTolerance = 0.01f)
        assertEquals(10.0, state.centerLatitude)
        assertEquals(-45.0, state.centerLongitude)
        assertEquals(1f, state.zoom)
    }

    @Test
    fun moveToWithZoomSnapsOriginAndZoom() {
        val state = AzimuthalMapState(initialZoom = 1f, maxZoom = 8f)
        state.viewportSize = Size(360f, 180f)
        state.moveTo(GeoPoint(latitude = -15.0, longitude = 120.0), zoom = 2.5f)
        assertEquals(-15.0, state.centerLatitude)
        assertEquals(120.0, state.centerLongitude)
        assertEquals(2.5f, state.zoom)
        assertEquals(-15.0, state.cameraCenter.latitude, absoluteTolerance = 1e-3)
        assertEquals(120.0, state.cameraCenter.longitude, absoluteTolerance = 1e-3)
    }

    @Test
    fun verticalScaleStretchesAwayFromProjectionCenter() {
        val state = AzimuthalMapState(
            initialZoom = 1f,
            initialVerticalScale = 2f,
            initialCenterLatitude = 45.0,
        )
        state.viewportSize = Size(360f, 180f)
        val proj = state.projection()
        val pivot = proj.toOffset(GeoPoint(45.0, 0.0))
        val equator = proj.toOffset(GeoPoint(0.0, 0.0))
        assertEquals(180f, pivot.x, absoluteTolerance = 0.05f)
        assertEquals(90f, pivot.y, absoluteTolerance = 0.05f)
        assertEquals(180f, equator.y, absoluteTolerance = 0.15f)
    }

    @Test
    fun rotationAnglePertainsToState() {
        val state = AzimuthalMapState(initialRotationAngle = 90f)
        state.viewportSize = Size(360f, 180f)
        assertEquals(90f, state.rotationAngle)
        assertEquals(90f, state.projection().rotationAngle)
        state.reset()
        assertEquals(0f, state.rotationAngle)
    }

    @Test
    fun verticalScaleChangeReclamps() {
        val state = AzimuthalMapState(initialZoom = 2f)
        state.viewportSize = Size(400f, 200f)
        state.offsetY = 50f
        state.verticalScale = 1f
        assertEquals(50f, state.offsetY, absoluteTolerance = 0.01f)
        state.verticalScale = 2f
        assertTrue(
            state.offsetY <= maxVerticalOffset(
                viewportWidth = 400f,
                viewportHeight = 200f,
                zoom = 2f,
                verticalScale = 2f,
            ),
        )
        state.offsetY = 80f
        state.verticalScale = 0.25f
        assertEquals(0f, state.offsetY, absoluteTolerance = 0.01f)
    }
}
