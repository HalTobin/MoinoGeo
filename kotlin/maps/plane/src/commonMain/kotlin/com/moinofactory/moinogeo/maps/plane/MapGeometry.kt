package com.moinofactory.moinogeo.maps.plane

import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import com.moinofactory.moinogeo.maps.core.GeoPoint
import com.moinofactory.moinogeo.maps.core.GeoPolygon
import kotlin.math.abs

/**
 * Builds a filled path for world copy [tileX].
 *
 * Polygons are mapped to tile [tileX] in equirectangular coordinates.
 * Caller clips to the tile's horizontal band via [MapProjection.clipRectForTile].
 */
internal fun GeoPolygon.toPath(projection: MapProjection, tileX: Int): Path? {
    if (outerBounds.size < 3) return null
    val path = Path().apply { fillType = PathFillType.EvenOdd }
    appendFillRing(path, outerBounds, projection, tileX)
    for (hole in innerHoles) {
        if (hole.size >= 3) {
            appendFillRing(path, hole, projection, tileX)
        }
    }
    return path
}

private fun appendFillRing(
    path: Path,
    ring: List<GeoPoint>,
    projection: MapProjection,
    tileX: Int,
) {
    val start = projection.toOffsetOnTile(ring.first(), tileX)
    path.moveTo(start.x, start.y)
    for (i in 1 until ring.size) {
        val p = projection.toOffsetOnTile(ring[i], tileX)
        path.lineTo(p.x, p.y)
    }
    path.close()
}

/**
 * Stroked outline for world copy [tileX]. Skips artificial boundary cut edges:
 * - Antimeridian cuts along ±180° longitude (e.g. Chukotka / Antarctica splits)
 * - Polar cut lines along ±90° latitude (e.g. South Pole closing edge)
 * - Wrap-around jumps across the map (>180° longitude span)
 */
internal fun GeoPolygon.toBorderPath(projection: MapProjection, tileX: Int): Path? {
    if (outerBounds.size < 2) return null
    val worldWidth = projection.worldWidth
    val worldHeight = projection.worldHeight
    if (worldWidth <= 0f || worldHeight <= 0f) return null

    val path = Path()
    var hasCommands = false

    if (appendBorderRing(path, outerBounds, projection, tileX)) {
        hasCommands = true
    }
    for (hole in innerHoles) {
        if (hole.size >= 2) {
            if (appendBorderRing(path, hole, projection, tileX)) {
                hasCommands = true
            }
        }
    }
    return if (hasCommands) path else null
}

private fun appendBorderRing(
    path: Path,
    ring: List<GeoPoint>,
    projection: MapProjection,
    tileX: Int,
): Boolean {
    if (ring.size < 2) return false
    var penDown = false
    var hasEdge = false

    for (i in 1 until ring.size) {
        val from = ring[i - 1]
        val to = ring[i]
        if (isArtificialBorderEdge(from, to)) {
            penDown = false
            continue
        }
        val fromScreen = projection.toOffsetOnTile(from, tileX)
        val toScreen = projection.toOffsetOnTile(to, tileX)
        if (!penDown) {
            path.moveTo(fromScreen.x, fromScreen.y)
            penDown = true
        }
        path.lineTo(toScreen.x, toScreen.y)
        hasEdge = true
    }

    if (penDown && !sameGeo(ring.first(), ring.last())) {
        val from = ring.last()
        val to = ring.first()
        if (!isArtificialBorderEdge(from, to)) {
            val start = projection.toOffsetOnTile(to, tileX)
            path.lineTo(start.x, start.y)
            hasEdge = true
        }
    }

    return hasEdge
}

/**
 * Returns true if the segment between [from] and [to] is an artificial cut line
 * inserted by GIS data producers (such as Natural Earth) rather than a real border.
 */
internal fun isArtificialBorderEdge(from: GeoPoint, to: GeoPoint): Boolean {
    if (isAntimeridianEdge(from, to)) return true
    if (abs(from.latitude) >= 89.9 && abs(to.latitude) >= 89.9) return true
    return abs(from.longitude - to.longitude) > 180.0
}

/** Natural Earth cut edge along ±180° (draws as a vertical line on equirectangular). */
internal fun isAntimeridianEdge(a: GeoPoint, b: GeoPoint, epsilonDeg: Double = 1e-3): Boolean {
    val aOn = abs(abs(a.longitude) - 180.0) <= epsilonDeg
    val bOn = abs(abs(b.longitude) - 180.0) <= epsilonDeg
    return aOn && bOn
}

private fun sameGeo(a: GeoPoint, b: GeoPoint): Boolean =
    abs(a.latitude - b.latitude) < 1e-9 && abs(a.longitude - b.longitude) < 1e-9

internal fun List<GeoPolygon>.approximateCenter(): GeoPoint? {
    var lat = 0.0
    var lon = 0.0
    var count = 0
    for (polygon in this) {
        for (point in polygon.outerBounds) {
            lat += point.latitude
            lon += point.longitude
            count++
        }
    }
    if (count == 0) return null
    return GeoPoint(latitude = lat / count, longitude = lon / count)
}

/** True when a ring spans (nearly) the full globe — ocean shells, not lakes. */
internal fun GeoPolygon.isWorldSpanning(thresholdDegrees: Double = 300.0): Boolean {
    if (outerBounds.isEmpty()) return false
    var minLon = Double.POSITIVE_INFINITY
    var maxLon = Double.NEGATIVE_INFINITY
    for (p in outerBounds) {
        if (p.longitude < minLon) minLon = p.longitude
        if (p.longitude > maxLon) maxLon = p.longitude
    }
    return (maxLon - minLon) >= thresholdDegrees
}
