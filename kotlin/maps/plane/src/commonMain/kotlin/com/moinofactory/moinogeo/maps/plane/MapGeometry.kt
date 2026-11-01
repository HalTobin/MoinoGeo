package com.moinofactory.moinogeo.maps.plane

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import com.moinofactory.moinogeo.maps.core.GeoPoint
import com.moinofactory.moinogeo.maps.core.GeoPolygon
import kotlin.math.abs
import kotlin.math.floor

/**
 * Builds a filled path for world copy [tileX].
 *
 * Longitudes are unwrapped in map space so antimeridian / polar rings stay
 * continuous. South-polar continents close along the bottom of the map.
 * Draw [MapProjection.HorizontalWrapTiles] shifted copies without per-tile X
 * clip bands — unwrapped polar geometry often extends past one world width and
 * needs the neighbouring copy to paint inside the primary viewport.
 */
internal fun GeoPolygon.toPath(projection: MapProjection, tileX: Int): Path? {
    if (outerBounds.size < 3) return null
    val path = Path().apply { fillType = PathFillType.NonZero }
    if (!appendFillRing(path, outerBounds, projection, tileX)) return null
    for (hole in innerHoles) {
        if (hole.size >= 3) {
            appendFillRing(path, hole.asReversed(), projection, tileX)
        }
    }
    return path
}

/**
 * Stroked outline for [tileX]. Skips artificial antimeridian cut edges (the
 * vertical lines Natural Earth inserts where a country was split at ±180°).
 */
internal fun GeoPolygon.toBorderPath(projection: MapProjection, tileX: Int): Path? {
    if (outerBounds.size < 2) return null
    val worldWidth = projection.worldWidth
    val worldHeight = projection.worldHeight
    if (worldWidth <= 0f || worldHeight <= 0f) return null

    val ring = outerBounds
    val mapPts = unwrapRingToMap(ring, worldWidth, worldHeight)
    if (mapPts.size < 2) return null

    // mapPts[i] corresponds to ring[i] (duplicate close vertex already dropped).
    val shiftX = tileX * worldWidth
    val path = Path()
    var penDown = false

    fun screenAt(i: Int): Offset =
        projection.mapToScreen(mapPts[i].x + shiftX, mapPts[i].y)

    for (i in mapPts.indices) {
        if (i > 0 && isAntimeridianEdge(ring[i - 1], ring[i])) {
            penDown = false
            continue
        }
        val screen = screenAt(i)
        if (!penDown) {
            path.moveTo(screen.x, screen.y)
            penDown = true
        } else {
            path.lineTo(screen.x, screen.y)
        }
    }

    // Re-close short rings unless the closing edge is the dateline cut.
    if (penDown && sameGeo(ring.first(), ring.last())) {
        val from = ring[mapPts.lastIndex]
        val to = ring.first()
        if (!isAntimeridianEdge(from, to)) {
            val start = screenAt(0)
            path.lineTo(start.x, start.y)
        }
    }
    return path
}

private fun appendFillRing(
    path: Path,
    ring: List<GeoPoint>,
    projection: MapProjection,
    tileX: Int,
): Boolean {
    val worldWidth = projection.worldWidth
    val worldHeight = projection.worldHeight
    val mapPts = unwrapRingToMap(ring, worldWidth, worldHeight)
    if (mapPts.size < 2) return false

    val shiftX = tileX * worldWidth
    fun screen(pt: Offset): Offset =
        projection.mapToScreen(pt.x + shiftX, pt.y)

    val first = mapPts.first()
    val last = mapPts.last()
    path.moveTo(screen(first).x, screen(first).y)
    for (i in 1 until mapPts.size) {
        val p = screen(mapPts[i])
        path.lineTo(p.x, p.y)
    }

    val xGap = last.x - first.x
    val wrapsFar = abs(xGap) > worldWidth * 0.5f
    when {
        wrapsFar && isSouthPolarRing(ring) -> {
            val south = screen(Offset(last.x, worldHeight))
            path.lineTo(south.x, south.y)
            val southFirst = screen(Offset(first.x, worldHeight))
            path.lineTo(southFirst.x, southFirst.y)
            val back = screen(first)
            path.lineTo(back.x, back.y)
        }
        wrapsFar && isNorthPolarRing(ring) -> {
            val north = screen(Offset(last.x, 0f))
            path.lineTo(north.x, north.y)
            val northFirst = screen(Offset(first.x, 0f))
            path.lineTo(northFirst.x, northFirst.y)
            val back = screen(first)
            path.lineTo(back.x, back.y)
        }
        wrapsFar -> {
            val image = screen(Offset(first.x + xGap, first.y))
            path.lineTo(image.x, image.y)
        }
        else -> path.close()
    }
    return true
}

/**
 * Equirectangular map coordinates with consecutive X unwrapped so edges stay
 * short. Closed rings drop the duplicate last vertex. Result is shifted so
 * `minX ∈ [0, worldWidth)` to keep polar rings overlapping the primary world.
 */
internal fun unwrapRingToMap(
    ring: List<GeoPoint>,
    worldWidth: Float,
    worldHeight: Float,
): List<Offset> {
    if (ring.isEmpty() || worldWidth <= 0f || worldHeight <= 0f) return emptyList()

    val half = worldWidth / 2f
    val out = ArrayList<Offset>(ring.size)
    var prevX = MapProjection.longitudeToMapX(ring[0].longitude, worldWidth)
    out.add(
        Offset(
            prevX,
            MapProjection.latitudeToMapY(ring[0].latitude, worldHeight),
        ),
    )

    for (i in 1 until ring.size) {
        val lat = ring[i].latitude
        var x = MapProjection.longitudeToMapX(ring[i].longitude, worldWidth)
        val y = MapProjection.latitudeToMapY(lat, worldHeight)

        val prevLat = ring[i - 1].latitude
        if (abs(prevLat) >= 89.9 && abs(lat) >= 89.9) {
            // Pole singularity — don't draw a full-width mid-ring edge.
            x = prevX
        } else {
            while (x - prevX > half) x -= worldWidth
            while (x - prevX < -half) x += worldWidth
        }

        out.add(Offset(x, y))
        prevX = x
    }

    if (out.size >= 2 && sameGeo(ring.first(), ring.last())) {
        out.removeAt(out.lastIndex)
    }
    if (out.isEmpty()) return out

    val minX = out.minOf { it.x }
    val normalize = floor(minX / worldWidth.toDouble()).toFloat() * worldWidth
    if (normalize != 0f) {
        for (i in out.indices) {
            out[i] = Offset(out[i].x - normalize, out[i].y)
        }
    }
    return out
}

/** Natural Earth cut edge along ±180° (draws as a vertical line on equirectangular). */
internal fun isAntimeridianEdge(a: GeoPoint, b: GeoPoint, epsilonDeg: Double = 1e-3): Boolean {
    val aOn = abs(abs(a.longitude) - 180.0) <= epsilonDeg
    val bOn = abs(abs(b.longitude) - 180.0) <= epsilonDeg
    return aOn && bOn
}

private fun sameGeo(a: GeoPoint, b: GeoPoint): Boolean =
    abs(a.latitude - b.latitude) < 1e-9 && abs(a.longitude - b.longitude) < 1e-9

private fun isSouthPolarRing(ring: List<GeoPoint>): Boolean {
    if (ring.isEmpty()) return false
    var minLat = Double.POSITIVE_INFINITY
    var maxLat = Double.NEGATIVE_INFINITY
    for (p in ring) {
        if (p.latitude < minLat) minLat = p.latitude
        if (p.latitude > maxLat) maxLat = p.latitude
    }
    return minLat <= -89.0 && maxLat <= -50.0
}

private fun isNorthPolarRing(ring: List<GeoPoint>): Boolean {
    if (ring.isEmpty()) return false
    var minLat = Double.POSITIVE_INFINITY
    var maxLat = Double.NEGATIVE_INFINITY
    for (p in ring) {
        if (p.latitude < minLat) minLat = p.latitude
        if (p.latitude > maxLat) maxLat = p.latitude
    }
    return maxLat >= 89.0 && minLat >= 50.0
}

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
