package com.moinofactory.moinogeo.maps.plane

import androidx.compose.ui.geometry.Size
import com.moinofactory.moinogeo.maps.core.GeoPoint
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PlaneMapStateTest {
    @Test
    fun testZoomInAndOut() {
        val state = PlaneMapState(initialZoom = 1.0f, maxZoom = 5.0f)
        state.zoomIn()
        assertEquals(1.25f, state.zoom)
        state.zoomOut()
        assertEquals(1.0f, state.zoom)
    }

    @Test
    fun testReset() {
        val state = PlaneMapState(
            initialZoom = 2.0f,
            initialOffsetX = 50f,
            initialOffsetY = 50f,
            initialVerticalScale = 1.5f,
        )
        state.viewportSize = Size(400f, 200f)
        state.reset()
        assertEquals(1.0f, state.zoom)
        assertEquals(0f, state.offsetX)
        assertEquals(0f, state.offsetY)
        assertEquals(1.0f, state.verticalScale)
    }

    @Test
    fun zoomAtKeepsFocalPointStable() {
        val state = PlaneMapState(initialZoom = 1f)
        state.viewportSize = Size(400f, 200f)
        val focalX = 300f
        val focalY = 100f
        val before = state.offsetX
        state.zoomAt(focalX, focalY, factor = 2f)
        assertEquals(2f, state.zoom)
        // offset shifts so the map point under the focal stays put
        assertEquals(
            before + (focalX - 200f) * (0.5f - 1f),
            state.offsetX,
            absoluteTolerance = 0.01f,
        )
        assertEquals(0f, state.offsetY, absoluteTolerance = 0.01f)
    }

    @Test
    fun panByDividesByZoom() {
        val state = PlaneMapState(initialZoom = 2f)
        state.viewportSize = Size(400f, 200f)
        state.panBy(deltaX = 40f, deltaY = 20f)
        assertEquals(20f, state.offsetX)
        assertEquals(10f, state.offsetY)
    }

    @Test
    fun panByUsesVerticalScaleForY() {
        val state = PlaneMapState(initialZoom = 2f, initialVerticalScale = 2f)
        state.viewportSize = Size(400f, 200f)
        state.panBy(deltaX = 0f, deltaY = 40f)
        // deltaY / (zoom * verticalScale) = 40 / 4 = 10
        assertEquals(10f, state.offsetY, absoluteTolerance = 0.01f)
    }

    @Test
    fun clampsVerticalOffsetInsideCanvas() {
        val state = PlaneMapState(initialZoom = 2f)
        state.viewportSize = Size(400f, 200f)
        // maxOffsetY = 100 * (1 - 1/2) = 50
        state.offsetY = 500f
        state.clampVerticalOffset()
        assertEquals(50f, state.offsetY, absoluteTolerance = 0.01f)

        state.offsetY = -500f
        state.clampVerticalOffset()
        assertEquals(-50f, state.offsetY, absoluteTolerance = 0.01f)
    }

    @Test
    fun minZoomKeepsVerticalOffsetCentered() {
        val state = PlaneMapState(initialZoom = 1f, initialOffsetY = 40f)
        state.viewportSize = Size(400f, 200f)
        assertEquals(0f, state.offsetY, absoluteTolerance = 0.01f)
    }

    @Test
    fun setCenterMovesViewport() {
        val state = PlaneMapState(initialZoom = 1f)
        state.viewportSize = Size(360f, 180f)
        state.setCenter(GeoPoint(latitude = 0.0, longitude = 90.0))
        // mapX for 90° = 270; offsetX = 180 - 270 = -90
        assertEquals(-90f, state.offsetX, absoluteTolerance = 0.01f)
        assertEquals(0f, state.offsetY, absoluteTolerance = 0.01f)
        assertEquals(0.0, state.center.latitude, absoluteTolerance = 1e-4)
        assertEquals(90.0, state.center.longitude, absoluteTolerance = 1e-4)
    }

    @Test
    fun setCenterQueuesUntilViewportReady() {
        val state = PlaneMapState(initialZoom = 1f)
        state.setCenter(GeoPoint(latitude = 45.0, longitude = 0.0))
        state.viewportSize = Size(360f, 180f)
        assertEquals(45.0, state.center.latitude, absoluteTolerance = 1e-4)
        assertEquals(0.0, state.center.longitude, absoluteTolerance = 1e-4)
    }

    @Test
    fun verticalScaleChangeReclamps() {
        val state = PlaneMapState(initialZoom = 2f)
        state.viewportSize = Size(400f, 200f)
        state.offsetY = 50f
        // At zoom=2, scale=1: max=50 — ok
        state.verticalScale = 1f
        assertEquals(50f, state.offsetY, absoluteTolerance = 0.01f)
        // Raising scale to 2 → zY=4 → max = 100*(1-1/4)=75, still ok
        state.verticalScale = 2f
        assertTrue(state.offsetY <= 75f)
        // Dropping back toward fit shrinks the allowed range
        state.verticalScale = 0.5f // zY = 1 → max = 0
        assertEquals(0f, state.offsetY, absoluteTolerance = 0.01f)
    }
}
