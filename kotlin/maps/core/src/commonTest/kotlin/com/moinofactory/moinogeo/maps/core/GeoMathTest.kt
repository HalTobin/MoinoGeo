package com.moinofactory.moinogeo.maps.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GeoMathTest {
    @Test
    fun wrapLongitudeDegreesNormalizes() {
        assertEquals(170.0, wrapLongitudeDegrees(170.0), absoluteTolerance = 1e-9)
        assertEquals(-170.0, wrapLongitudeDegrees(-170.0), absoluteTolerance = 1e-9)
        assertEquals(-170.0, wrapLongitudeDegrees(190.0), absoluteTolerance = 1e-9)
        assertEquals(170.0, wrapLongitudeDegrees(-190.0), absoluteTolerance = 1e-9)
    }

    @Test
    fun lerpTowardTakesShortLongitudeArc() {
        val from = GeoPoint(0.0, 170.0)
        val to = GeoPoint(0.0, -170.0)
        val mid = from.lerpToward(to, 0.5f)
        assertEquals(0.0, mid.latitude, absoluteTolerance = 1e-9)
        // Short path is +20° total, so halfway is 180° / -180°
        assertEquals(180.0, kotlin.math.abs(mid.longitude), absoluteTolerance = 1e-6)
    }

    @Test
    fun isNearAccountsForLongitudeWrap() {
        assertTrue(GeoPoint(0.0, 179.9).isNear(GeoPoint(0.0, -179.9), tolerance = 0.3))
    }
}
