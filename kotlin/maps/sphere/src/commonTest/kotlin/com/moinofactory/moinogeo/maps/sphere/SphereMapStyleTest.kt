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
        assertTrue(style.enableLighting)
        assertTrue(style.aura.enabled)
        assertIs<SphereBackground.Starfield>(style.background)
    }

    @Test
    fun testDefaultAuraUsesSpaceTint() {
        val aura = SphereAura.Default
        assertEquals(Color(0x6688CCFF), aura.color)
        assertTrue(aura.intensity > 0f)
        assertTrue(aura.thickness > 0f)
    }
}
