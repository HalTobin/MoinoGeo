package com.moinofactory.moinogeo.maps.sphere

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SphereMapStyleTest {
    @Test
    fun testDefaultSphereMapStyle() {
        val style = SphereMapStyle.Default
        assertEquals(200f, style.globeRadius)
        assertEquals(1000f, style.cameraDistance)
        assertEquals(23.5f, style.tiltAngle)
        assertTrue(style.enableLighting)
    }
}
