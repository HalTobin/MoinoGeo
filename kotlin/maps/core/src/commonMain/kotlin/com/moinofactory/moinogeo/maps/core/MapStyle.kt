package com.moinofactory.moinogeo.maps.core

import androidx.compose.ui.graphics.Color

data class MapStyle(
    val backgroundColor: Color = Color(0xFFEBF4FA),
    val zoneStyles: Map<TopoZone, ZoneStyle> = defaultZoneStyles(),
    val borderStyle: BorderStyle = BorderStyle(),
    val showBorders: Boolean = true,
    val showTopography: Boolean = true
) {
    companion object {
        fun defaultZoneStyles(): Map<TopoZone, ZoneStyle> = mapOf(
            TopoZone.WATER to ZoneStyle(fillColor = Color(0xFF4A90E2)),
            TopoZone.FOREST to ZoneStyle(fillColor = Color(0xFF2E7D32)),
            TopoZone.GRASSLAND to ZoneStyle(fillColor = Color(0xFF4CAF50)),
            TopoZone.AGRICULTURE to ZoneStyle(fillColor = Color(0xFF8BC34A)),
            TopoZone.ROCK to ZoneStyle(fillColor = Color(0xFF795548)),
            TopoZone.SNOW to ZoneStyle(fillColor = Color(0xFFE0E0E0)),
            TopoZone.SAND to ZoneStyle(fillColor = Color(0xFFE9C46A)),
            TopoZone.NEUTRAL to ZoneStyle(fillColor = Color(0xFFD7CCC8))
        )

        val Default = MapStyle()

        val Light = MapStyle(
            backgroundColor = Color(0xFFF5F5F5),
            borderStyle = BorderStyle(lineColor = Color(0xFF666666), lineWidth = 1f)
        )

        val Dark = MapStyle(
            backgroundColor = Color(0xFF121212),
            zoneStyles = mapOf(
                TopoZone.WATER to ZoneStyle(fillColor = Color(0xFF1565C0)),
                TopoZone.FOREST to ZoneStyle(fillColor = Color(0xFF1B5E20)),
                TopoZone.GRASSLAND to ZoneStyle(fillColor = Color(0xFF2E7D32)),
                TopoZone.AGRICULTURE to ZoneStyle(fillColor = Color(0xFF558B2F)),
                TopoZone.ROCK to ZoneStyle(fillColor = Color(0xFF4E342E)),
                TopoZone.SNOW to ZoneStyle(fillColor = Color(0xFFB0BEC5)),
                TopoZone.SAND to ZoneStyle(fillColor = Color(0xFFC29B38)),
                TopoZone.NEUTRAL to ZoneStyle(fillColor = Color(0xFF3E2723))
            ),
            borderStyle = BorderStyle(lineColor = Color(0xFF888888), lineWidth = 1f)
        )

        val Satellite = MapStyle(
            backgroundColor = Color(0xFF0B1D3A),
            zoneStyles = mapOf(
                TopoZone.WATER to ZoneStyle(fillColor = Color(0xFF0D47A1)),
                TopoZone.FOREST to ZoneStyle(fillColor = Color(0xFF33691E)),
                TopoZone.GRASSLAND to ZoneStyle(fillColor = Color(0xFF33691E)),
                TopoZone.AGRICULTURE to ZoneStyle(fillColor = Color(0xFF689F38)),
                TopoZone.ROCK to ZoneStyle(fillColor = Color(0xFF5D4037)),
                TopoZone.SNOW to ZoneStyle(fillColor = Color(0xFFECEFF1)),
                TopoZone.SAND to ZoneStyle(fillColor = Color(0xFFD4A373)),
                TopoZone.NEUTRAL to ZoneStyle(fillColor = Color(0xFF424242))
            ),
            borderStyle = BorderStyle(lineColor = Color(0xFFFFD54F), lineWidth = 1.2f)
        )
    }
}
