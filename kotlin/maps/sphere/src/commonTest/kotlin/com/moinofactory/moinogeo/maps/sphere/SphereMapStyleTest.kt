package com.moinofactory.moinogeo.maps.sphere

import androidx.compose.ui.graphics.Color
import com.moinofactory.moinogeo.maps.core.GeoPoint
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SphereMapStyleTest {
    @Test
    fun testDefaultSphereMapStyle() {
        val style = SphereMapStyle.Default
        assertEquals(1f, style.globeRadius)
        assertIs<SphereLighting.Uniform>(style.lighting)
        assertTrue(style.aura.enabled)
        assertIs<SphereBackground.Starfield>(style.background)
    }

    @Test
    fun testDirectionalLightingFromSubsolarPoint() {
        val lighting = SphereLighting.Directional.over(
            point = GeoPoint(latitude = 0.0, longitude = 0.0),
        )
        // Sun over (0,0) → light travels along −X toward the origin.
        assertEquals(-1f, lighting.directionX, absoluteTolerance = 1e-5f)
        assertEquals(0f, lighting.directionY, absoluteTolerance = 1e-5f)
        assertEquals(0f, lighting.directionZ, absoluteTolerance = 1e-5f)
    }

    @Test
    fun testDefaultAuraUsesSpaceTint() {
        val aura = SphereAura.Default
        assertEquals(Color(0xFF5EC8FF), aura.color)
        assertTrue(aura.intensity > 0f)
        assertTrue(aura.thickness > 0f)
        assertTrue(InnerAuraMaterial.payload().isNotEmpty())
        assertTrue(OuterAuraMaterial.payload().isNotEmpty())
        assertTrue(UnlitTexturedMaterial.payload().isNotEmpty())
    }
}
