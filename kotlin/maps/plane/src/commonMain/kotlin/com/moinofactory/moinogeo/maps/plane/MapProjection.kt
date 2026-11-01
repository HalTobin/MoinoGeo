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
 */
@Immutable
internal data class MapProjection(
    val size: Size,
    val zoom: Float,
    val offsetX: Float,
    val offsetY: Float,
) {
    fun toOffset(point: GeoPoint): Offset {
        val width = size.width
        val height = size.height
        if (width <= 0f || height <= 0f) return Offset.Zero

        val x = ((point.longitude + 180.0) / 360.0 * width).toFloat()
        val y = ((90.0 - point.latitude) / 180.0 * height).toFloat()
        val cx = width / 2f
        val cy = height / 2f
        return Offset(
            x = cx + (x - cx + offsetX) * zoom,
            y = cy + (y - cy + offsetY) * zoom,
        )
    }

    fun toGeoPoint(offset: Offset): GeoPoint {
        val width = size.width
        val height = size.height
        if (width <= 0f || height <= 0f) return GeoPoint(0.0, 0.0)

        val cx = width / 2f
        val cy = height / 2f
        val x = (offset.x - cx) / zoom - offsetX + cx
        val y = (offset.y - cy) / zoom - offsetY + cy
        val longitude = x / width * 360.0 - 180.0
        val latitude = 90.0 - y / height * 180.0
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
    }
}
