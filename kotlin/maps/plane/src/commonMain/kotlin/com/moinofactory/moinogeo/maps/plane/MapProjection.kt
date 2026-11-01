package com.moinofactory.moinogeo.maps.plane

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import com.moinofactory.moinogeo.maps.core.GeoPoint
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max

/**
 * Equirectangular projection.
 *
 * Horizontal looping is implemented by drawing three world copies (tileX = -1..1)
 * with **horizontal clip bands** — not by unwrapping wide polygons (oceans /
 * Antarctica), which caused fill inversions and missing geometry.
 */
@Immutable
internal data class MapProjection(
    val size: Size,
    val zoom: Float,
    val offsetX: Float,
    val offsetY: Float,
) {
    val worldWidth: Float get() = size.width
    val worldHeight: Float get() = size.height

    /** Screen position for [point] on world copy [tileX] (`0` = primary). */
    fun toOffsetOnTile(point: GeoPoint, tileX: Int): Offset {
        val width = size.width
        val height = size.height
        if (width <= 0f || height <= 0f) return Offset.Zero

        val mapX = longitudeToMapX(point.longitude, width) + tileX * width
        val mapY = latitudeToMapY(point.latitude, height)
        return mapToScreen(mapX, mapY)
    }

    /**
     * Marker / hit-test helper: pick the horizontal copy whose screen X is
     * closest to the viewport center.
     */
    fun toOffset(point: GeoPoint): Offset {
        val cx = size.width / 2f
        var best = Offset.Zero
        var bestDist = Float.POSITIVE_INFINITY
        for (tx in HorizontalWrapTiles) {
            val p = toOffsetOnTile(point, tx)
            val d = (p.x - cx) * (p.x - cx)
            if (d < bestDist) {
                bestDist = d
                best = p
            }
        }
        return best
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

    fun metersToPixels(meters: Double, atLatitude: Double): Float {
        val width = size.width
        if (width <= 0f || meters <= 0.0) return 0f
        val latRad = atLatitude.coerceIn(-89.9, 89.9) * PI / 180.0
        val metersPerDegree = 111_320.0 * cos(latRad)
        val safeMetersPerDegree = max(metersPerDegree, 1.0)
        val degrees = meters / safeMetersPerDegree
        return (degrees / 360.0 * width * zoom).toFloat()
    }

    /**
     * Horizontal screen band for world copy [tileX], expanded by [overlapPx]
     * so adjacent clips meet without a hairline gap.
     */
    fun clipRectForTile(tileX: Int, overlapPx: Float = 1.5f): Rect {
        val width = size.width
        val height = size.height
        val left = mapToScreen(tileX * width, 0f).x - overlapPx
        val right = mapToScreen((tileX + 1) * width, 0f).x + overlapPx
        return Rect(
            left = minOf(left, right),
            top = -overlapPx,
            right = maxOf(left, right),
            bottom = height + overlapPx,
        )
    }

    internal fun mapToScreen(mapX: Float, mapY: Float): Offset {
        val cx = size.width / 2f
        val cy = size.height / 2f
        return Offset(
            x = cx + (mapX - cx + offsetX) * zoom,
            y = cy + (mapY - cy + offsetY) * zoom,
        )
    }

    companion object {
        val Empty = MapProjection(Size.Zero, zoom = 1f, offsetX = 0f, offsetY = 0f)

        val HorizontalWrapTiles: IntRange = -1..1

        fun longitudeToMapX(longitude: Double, worldWidth: Float): Float =
            ((longitude + 180.0) / 360.0 * worldWidth).toFloat()

        fun latitudeToMapY(latitude: Double, worldHeight: Float): Float =
            ((90.0 - latitude) / 180.0 * worldHeight).toFloat()
    }
}

internal fun wrapInto(value: Float, period: Float): Float {
    if (period <= 0f) return value
    var v = value % period
    if (v < 0f) v += period
    return v
}

internal fun wrapOffsetCentered(value: Float, period: Float): Float {
    if (period <= 0f) return value
    var v = value % period
    if (v > period / 2f) v -= period
    if (v < -period / 2f) v += period
    return v
}
