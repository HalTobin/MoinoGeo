package com.moinofactory.moinogeo.maps.topography

import com.moinofactory.moinogeo.maps.core.TopoZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TopographyTest {
    @Test
    fun testDefaultTopographyProvider() {
        val provider = DefaultTopographyProvider()
        val data = provider.getTopographyData()
        assertEquals(5, data.features.size)
        assertTrue(data.features.any { it.zone == TopoZone.WATER })
        assertTrue(data.features.any { it.zone == TopoZone.FOREST })
        assertTrue(data.features.any { it.zone == TopoZone.ROCK })
        assertTrue(data.features.any { it.zone == TopoZone.SNOW })
        assertTrue(data.features.any { it.zone == TopoZone.SAND })
    }
}
