package com.moinofactory.moinogeo.maps.plane

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.moinofactory.moinogeo.maps.core.GeoPoint
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max

/**
 * Equirectangular projection from geographic coordinates to the current
 * [PlaneMap] viewport (size + pan/zoom from [PlaneMapState]).
 *
 * The world wraps **horizontally** (360°) so panning east/west can loop.
 */
@Immutable
internal data class MapProjection(
    val size: Size,
    val zoom: Float,
    val offsetX: Float,
    val offsetY: Float,
) {
    val worldWidth: Float get() = size.width

    /** Screen position of [point] on the horizontal tile nearest the viewport center. */
    fun toOffset(point: GeoPoint): Offset {
        val width = size.width
        val height = size.height
        if (width <= 0f || height <= 0f) return Offset.Zero

        val mapX = ((point.longitude + 180.0) / 360.0 * width).toFloat()
        val mapY = ((90.0 - point.latitude) / 180.0 * height).toFloat()
        val cx = width / 2f
        val cy = height / 2f

        var best = Offset.Zero
        var bestDist = Float.POSITIVE_INFINITY
        for (tx in -1..1) {
            val px = cx + (mapX + tx * width - cx + offsetX) * zoom
            val py = cy + (mapY - cy + offsetY) * zoom
            val dx = px - cx
            val dist = dx * dx
            if (dist < bestDist) {
                bestDist = dist
                best = Offset(px, py)
            }
        }
        return best
    }

    /** Screen position for a horizontal world tile ([tileX]), `(0)` is the primary copy. */
    fun toOffsetOnTile(point: GeoPoint, tileX: Int): Offset {
        val width = size.width
        val height = size.height
        if (width <= 0f || height <= 0f) return Offset.Zero

        val mapX = ((point.longitude + 180.0) / 360.0 * width).toFloat() + tileX * width
        val mapY = ((90.0 - point.latitude) / 180.0 * height).toFloat()
        val cx = width / 2f
        val cy = height / 2f
        return Offset(
            x = cx + (mapX - cx + offsetX) * zoom,
            y = cy + (mapY - cy + offsetY) * zoom,
        )
    }

    fun toGeoPoint(offset: Offset): GeoPoint {
        val width = size.width
        val height = size.height
        if (width <= 0f || height <= 0f) return GeoPoint(0.0, 0.0)

        val cx = width / 2f
        val cy = height / 2f
        var mapX = (offset.x - cx) / zoom - offsetX + cx
        val mapY = (offset.y - cy) / zoom - offsetY + cy
        mapX = wrapInto(mapX, width)
        val longitude = mapX / width * 360.0 - 180.0
        val latitude = (90.0 - mapY / height * 180.0).coerceIn(-90.0, 90.0)
        return GeoPoint(latitude = latitude, longitude = longitude)
    }

    /**
     * Converts a ground distance in meters to viewport pixels at [atLatitude],
     * using an equirectangular approximation (fine for regional circles).
     */
    fun metersToPixels(meters: Double, atLatitude: Double): Float {
        val width = size.width
        if (width <= 0f || meters <= 0.0) return 0f
        val latRad = atLatitude.coerceIn(-89.9, 89.9) * PI / 180.0
        val metersPerDegree = 111_320.0 * cos(latRad)
        val safeMetersPerDegree = max(metersPerDegree, 1.0)
        val degrees = meters / safeMetersPerDegree
        return (degrees / 360.0 * width * zoom).toFloat()
    }

    companion object {
        val Empty = MapProjection(Size.Zero, zoom = 1f, offsetX = 0f, offsetY = 0f)

        /** Horizontal tile offsets drawn for wraparound. */
        val HorizontalWrapTiles: IntRange = -1..1
    }
}

/** Wrap [value] into `[0, period)`. */
internal fun wrapInto(value: Float, period: Float): Float {
    if (period <= 0f) return value
    var v = value % period
    if (v < 0f) v += period
    return v
}

/** Keep horizontal pan near zero while the map visually loops. */
internal fun wrapOffsetCentered(value: Float, period: Float): Float {
    if (period <= 0f) return value
    var v = value % period
    if (v > period / 2f) v -= period
    if (v < -period / 2f) v += period
    return v
}
