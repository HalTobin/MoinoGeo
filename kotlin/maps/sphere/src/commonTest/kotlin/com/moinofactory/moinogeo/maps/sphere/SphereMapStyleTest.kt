package com.moinofactory.moinogeo.maps.sphere

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SphereMapStyleTest {
    @Test
    fun testDefaultSphereMapStyle() {
        val style = SphereMapStyle.Default
        assertEquals(1f, style.globeRadius)
        assertEquals(3.2f, style.cameraDistance)
        assertEquals(23.5f, style.tiltAngle)
        assertTrue(style.enableLighting)
        assertTrue(style.aura.enabled)
        assertIs<SphereBackground.Solid>(style.background)
    }

    @Test
    fun testSpacePresetUsesStarfieldAndAura() {
        val style = SphereMapStyle.Space
        assertIs<SphereBackground.Starfield>(style.background)
        assertTrue(style.aura.enabled)
        assertEquals(Color(0x6688CCFF), style.aura.color)
    }
}
