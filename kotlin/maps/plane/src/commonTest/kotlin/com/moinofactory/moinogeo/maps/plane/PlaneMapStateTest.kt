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
}
