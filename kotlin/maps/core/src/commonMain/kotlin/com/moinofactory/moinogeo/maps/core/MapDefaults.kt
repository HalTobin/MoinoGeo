package com.moinofactory.moinogeo.maps.core

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color

/**
 * Contains default values and builder functions for map styling.
 */
object MapDefaults {

    /**
     * Creates a [MapStyle] with customizable colors for topography zones (WATER, FOREST, ROCK, SNOW, SAND, NEUTRAL),
     * border color, border width, and background.
     *
     * Usage matching Compose component conventions (similar to `ButtonDefaults.colors()`):
     * ```kotlin
     * val customStyle = MapDefaults.style(
     *     forestColor = Color.Green,
     *     sandColor = Color.Yellow,
     *     borderColor = Color.Black,
     *     borderWidth = 2.0f
     * )
     * ```
     *
     * @param backgroundColor Color for the map background/canvas.
     * @param waterColor Color for [TopoZone.WATER] regions.
     * @param forestColor Color for [TopoZone.FOREST] regions.
     * @param rockColor Color for [TopoZone.ROCK] regions.
     * @param snowColor Color for [TopoZone.SNOW] regions.
     * @param sandColor Color for [TopoZone.SAND] regions.
     * @param neutralColor Color for [TopoZone.NEUTRAL] regions.
     * @param borderColor Color for map boundary/border lines.
     * @param borderWidth Width of map boundary/border lines in pixels.
     * @param showBorders Whether boundary lines should be displayed.
     * @param showTopography Whether topography zone fills should be displayed.
     */
    @Stable
    fun style(
        backgroundColor: Color = Color(0xFFEBF4FA),
        waterColor: Color = Color(0xFF4A90E2),
        forestColor: Color = Color(0xFF2E7D32),
        rockColor: Color = Color(0xFF795548),
        snowColor: Color = Color(0xFFE0E0E0),
        sandColor: Color = Color(0xFFE9C46A),
        neutralColor: Color = Color(0xFFD7CCC8),
        borderColor: Color = Color(0xFF333333),
        borderWidth: Float = 1.5f,
        showBorders: Boolean = true,
        showTopography: Boolean = true
    ): MapStyle {
        return MapStyle(
            backgroundColor = backgroundColor,
            zoneStyles = mapOf(
                TopoZone.WATER to ZoneStyle(fillColor = waterColor),
                TopoZone.FOREST to ZoneStyle(fillColor = forestColor),
                TopoZone.ROCK to ZoneStyle(fillColor = rockColor),
                TopoZone.SNOW to ZoneStyle(fillColor = snowColor),
                TopoZone.SAND to ZoneStyle(fillColor = sandColor),
                TopoZone.NEUTRAL to ZoneStyle(fillColor = neutralColor)
            ),
            borderStyle = BorderStyle(
                lineColor = borderColor,
                lineWidth = borderWidth
            ),
            showBorders = showBorders,
            showTopography = showTopography
        )
    }
}

/**
 * Alias allowing [MapDefault.style()] usage matching user query syntax.
 */
typealias MapDefault = MapDefaults
