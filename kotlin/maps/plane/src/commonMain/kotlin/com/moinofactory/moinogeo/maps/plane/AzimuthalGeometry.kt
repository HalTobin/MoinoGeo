package com.moinofactory.moinogeo.maps.plane

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import com.moinofactory.moinogeo.maps.core.GeoPoint
import com.moinofactory.moinogeo.maps.core.GeoPolygon
import com.moinofactory.moinogeo.maps.core.wrapDeltaDegrees
import com.moinofactory.moinogeo.maps.core.wrapLongitudeDegrees
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.hypot
import kotlin.math.max

/**
 * Builds a filled path on the azimuthal disk.
 *
 * Long lat/lon edges are subdivided so parallels and meridians become arcs
 * instead of chords. Vertices past the antipode (`c > π − ε`) break the
 * contour so a segment cannot jump across the back of the sphere.
 */
internal fun GeoPolygon.toPath(projection: AzimuthalProjection, tileX: Int): Path? {
    if (tileX != 0 || outerBounds.size < 3) return null
    val path = Path().apply { fillType = PathFillType.EvenOdd }
    val outer = projectRing(
        path = path,
        ring = outerBounds,
        projection = projection,
        close = true,
        skipArtificial = false,
    )
    var holes = false
    for (hole in innerHoles) {
        if (hole.size >= 3 &&
            projectRing(path, hole, projection, close = true, skipArtificial = false)
        ) {
            holes = true
        }
    }
    if (!outer && !holes) return null
    return path
}

/**
 * Stroked outline. Skips artificial GIS cut edges (antimeridian, polar
 * closing line, and longitude jumps longer than 180°) and drops antipodal spans.
 */
internal fun GeoPolygon.toBorderPath(projection: AzimuthalProjection, tileX: Int): Path? {
    if (tileX != 0 || outerBounds.size < 2) return null
    if (projection.worldWidth <= 0f || projection.worldHeight <= 0f) return null

    val path = Path()
    var hasCommands = false
    if (projectRing(path, outerBounds, projection, close = false, skipArtificial = true)) {
        hasCommands = true
    }
    for (hole in innerHoles) {
        if (hole.size >= 2 &&
            projectRing(path, hole, projection, close = false, skipArtificial = true)
        ) {
            hasCommands = true
        }
    }
    return if (hasCommands) path else null
}

/**
 * Points along the short lat/lon arc from [from] to [to], excluding [from]
 * and including [to]. Step size tracks zoom so each segment is a few pixels
 * near the projection center.
 */
internal fun densifySegment(
    from: GeoPoint,
    to: GeoPoint,
    projection: AzimuthalProjection,
): List<GeoPoint> {
    val dLon = wrapDeltaDegrees(to.longitude - from.longitude)
    val dLat = to.latitude - from.latitude
    val span = max(abs(dLat), abs(dLon))
    if (span < 1e-8) return listOf(to)

    val radius = projection.radiusPerRadian()
    val stepDeg = if (radius > 1.0) {
        (8.0 / radius * (180.0 / PI)).coerceIn(0.15, 8.0)
    } else {
        5.0
    }
    val steps = ceil(span / stepDeg).toInt().coerceIn(1, 96)
    val out = ArrayList<GeoPoint>(steps)
    for (i in 1..steps) {
        val t = i.toDouble() / steps
        out.add(
            GeoPoint(
                latitude = (from.latitude + dLat * t).coerceIn(-90.0, 90.0),
                longitude = wrapLongitudeDegrees(from.longitude + dLon * t),
            ),
        )
    }
    return out
}

private fun projectRing(
    path: Path,
    ring: List<GeoPoint>,
    projection: AzimuthalProjection,
    close: Boolean,
    skipArtificial: Boolean,
): Boolean {
    if (ring.size < 2) return false
    val maxC = PI - AzimuthalProjection.AntipodeEpsilonRadians
    val maxJump = projection.diskRadiusX() * 1.25f
    var penDown = false
    var hasEdge = false
    var lastScreen: Offset? = null

    fun breakPen() {
        if (close && penDown) path.close()
        penDown = false
        lastScreen = null
    }

    fun lineTo(screen: Offset) {
        val previous = lastScreen
        if (!penDown || previous == null) {
            path.moveTo(screen.x, screen.y)
            penDown = true
            lastScreen = screen
            return
        }
        val jump = hypot(
            (screen.x - previous.x).toDouble(),
            (screen.y - previous.y).toDouble(),
        )
        if (maxJump > 0f && jump > maxJump) {
            breakPen()
            path.moveTo(screen.x, screen.y)
            penDown = true
            lastScreen = screen
            return
        }
        path.lineTo(screen.x, screen.y)
        hasEdge = true
        lastScreen = screen
    }

    fun emit(point: GeoPoint) {
        if (projection.angularDistance(point) > maxC) {
            breakPen()
            return
        }
        lineTo(projection.toOffset(point))
    }

    fun emitEdge(from: GeoPoint, to: GeoPoint) {
        if (!penDown) emit(from)
        for (point in densifySegment(from, to, projection)) {
            emit(point)
        }
    }

    for (i in 1 until ring.size) {
        val from = ring[i - 1]
        val to = ring[i]
        if (skipArtificial && isArtificialBorderEdge(from, to)) {
            breakPen()
            continue
        }
        emitEdge(from, to)
    }

    if (!sameGeo(ring.first(), ring.last())) {
        val from = ring.last()
        val to = ring.first()
        if (!(skipArtificial && isArtificialBorderEdge(from, to))) {
            emitEdge(from, to)
        }
    } else if (close && penDown) {
        path.close()
    }
    return hasEdge
}

private fun sameGeo(a: GeoPoint, b: GeoPoint): Boolean =
    abs(a.latitude - b.latitude) < 1e-9 && abs(a.longitude - b.longitude) < 1e-9
