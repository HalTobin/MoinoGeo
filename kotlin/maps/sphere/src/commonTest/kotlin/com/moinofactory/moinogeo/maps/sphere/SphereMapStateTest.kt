package com.moinofactory.moinogeo.maps.sphere

import com.moinofactory.moinogeo.maps.core.GeoPoint
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SphereMapStateTest {
    @Test
    fun zoomIsClamped() {
        val state = SphereMapState(initialZoom = 1f, minZoom = 0.5f, maxZoom = 4f)
        state.zoom = 99f
        assertEquals(4f, state.zoom)
        state.zoom = 0.1f
        assertEquals(0.5f, state.zoom)
    }

    @Test
    fun moveToUpdatesCenter() {
        val state = SphereMapState()
        state.moveTo(GeoPoint(48.8, 2.3), zoom = 2f)
        assertEquals(48.8, state.cameraCenter.latitude, absoluteTolerance = 1e-6)
        assertEquals(2.3, state.cameraCenter.longitude, absoluteTolerance = 1e-6)
        assertEquals(2f, state.zoom)
    }

    @Test
    fun higherZoomMeansCloserCamera() {
        val state = SphereMapState(minZoom = 0.5f, maxZoom = 8f)
        state.zoom = 0.5f
        val far = state.distance
        state.zoom = 8f
        val near = state.distance
        assertTrue(near < far)
    }

    @Test
    fun orbitByChangesLongitude() {
        val state = SphereMapState(initialCenter = GeoPoint(0.0, 0.0))
        state.orbitBy(deltaX = 40f, deltaY = 0f)
        assertTrue(abs(state.center.longitude) > 1e-3)
    }
}
