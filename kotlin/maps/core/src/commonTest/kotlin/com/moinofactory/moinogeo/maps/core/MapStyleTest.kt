package com.moinofactory.moinogeo.maps.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class MapStyleTest {
    @Test
    fun testDefaultMapStyle() {
        val style = MapStyle.Default
        assertNotNull(style.backgroundColor)
        assertEquals(5, style.zoneStyles.size)
        assertEquals(true, style.showBorders)
        assertEquals(true, style.showTopography)
    }

    @Test
    fun testTopoZoneEnumValues() {
        val zones = TopoZone.entries
        assertEquals(5, zones.size)
    }
}
