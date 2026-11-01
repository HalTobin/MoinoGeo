package com.moinofactory.moinogeo.maps.plane

import kotlin.test.Test
import kotlin.test.assertEquals

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
        val state = PlaneMapState(initialZoom = 2.0f, initialOffsetX = 50f, initialOffsetY = 50f)
        state.reset()
        assertEquals(1.0f, state.zoom)
        assertEquals(0f, state.offsetX)
        assertEquals(0f, state.offsetY)
    }

    @Test
    fun zoomAtKeepsFocalPointStable() {
        val state = PlaneMapState(initialZoom = 1f)
        state.viewportSize = androidx.compose.ui.geometry.Size(400f, 200f)
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
        state.panBy(deltaX = 40f, deltaY = 20f)
        assertEquals(20f, state.offsetX)
        assertEquals(10f, state.offsetY)
    }
}
