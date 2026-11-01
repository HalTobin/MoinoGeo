package com.moinofactory.moinogeo.maps.plane

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import com.moinofactory.moinogeo.maps.core.GeoPoint
import com.moinofactory.moinogeo.maps.core.GeoPolygon
import kotlin.math.abs

/**
 * Builds a filled path for world copy [tileX].
 *
 * Ring longitudes are unwrapped in map space so antimeridian edges stay short.
 * South-polar continents (Antarctica) close along the bottom of the map instead
 * of across the Southern Ocean. Caller clips to the tile's horizontal band.
 */
internal fun GeoPolygon.toPath(projection: MapProjection, tileX: Int): Path? {
    if (outerBounds.size < 3) return null
    val path = Path().apply { fillType = PathFillType.NonZero }
    if (!appendRing(path, outerBounds, projection, tileX, closeForFill = true)) return null
    for (hole in innerHoles) {
        if (hole.size >= 3) {
            appendRing(path, hole.asReversed(), projection, tileX, closeForFill = true)
        }
    }
    return path
}

internal fun GeoPolygon.toBorderPath(projection: MapProjection, tileX: Int): Path? {
    if (outerBounds.size < 2) return null
    val path = Path()
    // Strokes follow the coast only — no synthetic polar closing edge.
    if (!appendRing(path, outerBounds, projection, tileX, closeForFill = false)) return null
    return path
}

private fun appendRing(
    path: Path,
    ring: List<GeoPoint>,
    projection: MapProjection,
    tileX: Int,
    closeForFill: Boolean,
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
        closeForFill && wrapsFar && isSouthPolarRing(ring) -> {
            // Close via the south edge so fill is the polar continent, not a
            // band across the Southern Ocean.
            val south = screen(Offset(last.x, worldHeight))
            path.lineTo(south.x, south.y)
            val southFirst = screen(Offset(first.x, worldHeight))
            path.lineTo(southFirst.x, southFirst.y)
            val back = screen(first)
            path.lineTo(back.x, back.y)
        }
        closeForFill && wrapsFar && isNorthPolarRing(ring) -> {
            val north = screen(Offset(last.x, 0f))
            path.lineTo(north.x, north.y)
            val northFirst = screen(Offset(first.x, 0f))
            path.lineTo(northFirst.x, northFirst.y)
            val back = screen(first)
            path.lineTo(back.x, back.y)
        }
        closeForFill && wrapsFar -> {
            // Non-polar wrap: close to the unwrapped image of the start point.
            val image = screen(Offset(first.x + xGap, first.y))
            path.lineTo(image.x, image.y)
        }
        !wrapsFar -> path.close()
        // Stroke of a dateline-crossing ring: leave open — a close() edge
        // would draw a false border across the ocean.
    }
    return true
}

/**
 * Equirectangular map coordinates with consecutive X unwrapped so edges stay
 * short. Closed rings drop the duplicate last vertex.
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

        // Pole singularity: all longitudes meet — keep X continuous instead of
        // drawing a full-width edge along the top/bottom of the map mid-ring.
        val prevLat = ring[i - 1].latitude
        if (abs(prevLat) >= 89.9 && abs(lat) >= 89.9) {
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
    return out
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
