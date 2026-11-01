package com.moinofactory.moinogeo.maps.core

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class MapStyleTest {
    @Test
    fun testDefaultMapStyle() {
        val style = MapStyle.Default
        assertNotNull(style.backgroundColor)
        assertEquals(6, style.zoneStyles.size)
        assertEquals(true, style.showBorders)
        assertEquals(true, style.showTopography)
    }

    @Test
    fun testTopoZoneEnumValues() {
        val zones = TopoZone.entries
        assertEquals(6, zones.size)
    }

    @Test
    fun testMapDefaultsStyle() {
        val customStyle = MapDefaults.style(
            forestColor = Color.Green,
            sandColor = Color.Yellow,
            borderColor = Color.Red,
            borderWidth = 3.0f
        )
        assertEquals(Color.Green, customStyle.zoneStyles[TopoZone.FOREST]?.fillColor)
        assertEquals(Color.Yellow, customStyle.zoneStyles[TopoZone.SAND]?.fillColor)
        assertEquals(Color.Red, customStyle.borderStyle.lineColor)
        assertEquals(3.0f, customStyle.borderStyle.lineWidth)
    }

    @Test
    fun testMapDefaultAlias() {
        val styleFromAlias = MapDefault.style(
            snowColor = Color.White
        )
        assertEquals(Color.White, styleFromAlias.zoneStyles[TopoZone.SNOW]?.fillColor)
    }
}
