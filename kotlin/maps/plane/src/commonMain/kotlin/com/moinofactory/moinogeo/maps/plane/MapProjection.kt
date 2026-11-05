package com.moinofactory.moinogeo.maps.plane

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import com.moinofactory.moinogeo.maps.core.GeoPoint
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

/**
 * Equirectangular (Plate Carrée) projection.
 *
 * Horizontal looping is implemented by drawing three world copies (tileX = -1..1)
 * with **horizontal clip bands** — not by unwrapping wide polygons (oceans /
 * Antarctica), which caused fill inversions and missing geometry.
 *
 * The primary tile's antimeridian stays fixed at ±180° so Natural Earth polygons
 * (pre-split on that meridian) remain valid. Camera centering uses [offsetX] /
 * [offsetY] via [PlaneMapState.setCenter], not a shifted central meridian.
 *
 * [verticalScale] is an aspect-ratio adjustment along Y (1f = classic
 * equirectangular with equal degrees-per-pixel on X and Y). Canvas aspect does
 * **not** distort geography: [geographicYFactor] compensates so a square or tall
 * viewport keeps the same proportions as a 2:1 one. It is **not** spherical
 * distortion centering: meridians stay parallel, and east-west stretch still
 * grows as sec(φ) toward the poles. [centerLatitude] only picks the latitude
 * that the Y stretch pivots around.
 */
@Immutable
internal data class MapProjection(
    val size: Size,
    val zoom: Float,
    val offsetX: Float,
    val offsetY: Float,
    val verticalScale: Float = 1f,
    val centerLatitude: Double = 0.0,
    val rotationAngle: Float = 0f,
) {
    val worldWidth: Float get() = size.width
    val worldHeight: Float get() = size.height

    /** Effective Y zoom for distances away from [centerLatitude]. */
    val zoomY: Float
        get() = zoom * verticalScale.coerceAtLeast(MinVerticalScale) *
            geographicYFactor(size.width, size.height)

    /** Map-Y of the vertical aspect-ratio pivot ([centerLatitude]). */
    val pivotY: Float
        get() = latitudeToMapY(centerLatitude, size.height)

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
        val pivot = pivotY
        val zY = zoomY

        var screenX = offset.x
        var screenY = offset.y

        if (rotationAngle != 0f) {
            val rad = rotationAngle * (PI / 180.0).toFloat()
            val cosR = cos(rad)
            val sinR = sin(rad)
            val dx = offset.x - cx
            val dy = offset.y - cy
            screenX = cx + dx * cosR + dy * sinR
            screenY = cy - dx * sinR + dy * cosR
        }

        var mapX = (screenX - cx) / zoom - offsetX + cx
        val mapY = pivot +
            (screenY - cy - offsetY * zoom) / zY
        mapX = wrapInto(mapX, width)
        val longitude = mapXToLongitude(mapX, width)
        val latitude = mapYToLatitude(mapY, height).coerceIn(-90.0, 90.0)
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

    /**
     * Vertical stretch pivots around [pivotY] ([centerLatitude]):
     * distances from the pivot use [zoomY], while the pivot itself tracks
     * with [zoom] + [offsetY] like the horizontal axis.
     */
    internal fun mapToScreen(mapX: Float, mapY: Float): Offset {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val pivot = pivotY
        val zY = zoomY

        val uX = cx + (mapX - cx + offsetX) * zoom
        val uY = cy + offsetY * zoom + (mapY - pivot) * zY

        if (rotationAngle == 0f) {
            return Offset(uX, uY)
        }

        val rad = rotationAngle * (PI / 180.0).toFloat()
        val cosR = cos(rad)
        val sinR = sin(rad)
        val dx = uX - cx
        val dy = uY - cy
        return Offset(
            x = cx + dx * cosR - dy * sinR,
            y = cy + dx * sinR + dy * cosR,
        )
    }

    companion object {
        val Empty = MapProjection(
            Size.Zero,
            zoom = 1f,
            offsetX = 0f,
            offsetY = 0f,
            verticalScale = 1f,
            centerLatitude = 0.0,
            rotationAngle = 0f,
        )

        val HorizontalWrapTiles: IntRange = -1..1

        const val MinVerticalScale = 0.1f

        /** Natural equirectangular width:height (360°:180°). */
        const val GeographicAspectRatio = 2f

        /**
         * Y compensation so equal degrees-per-pixel holds on a viewport of
         * [viewportWidth]×[viewportHeight] when user vertical scale is 1.
         */
        fun geographicYFactor(viewportWidth: Float, viewportHeight: Float): Float {
            if (viewportWidth <= 0f || viewportHeight <= 0f) return 1f
            return (viewportWidth / GeographicAspectRatio) / viewportHeight
        }

        /** Longitude → map X with the primary tile fixed on ±180° (Greenwich at center). */
        fun longitudeToMapX(longitude: Double, worldWidth: Float): Float =
            ((longitude + 180.0) / 360.0 * worldWidth).toFloat()

        fun mapXToLongitude(mapX: Float, worldWidth: Float): Double {
            if (worldWidth <= 0f) return 0.0
            return mapX / worldWidth * 360.0 - 180.0
        }

        fun latitudeToMapY(latitude: Double, worldHeight: Float): Float =
            ((90.0 - latitude) / 180.0 * worldHeight).toFloat()

        fun mapYToLatitude(mapY: Float, worldHeight: Float): Double {
            if (worldHeight <= 0f) return 0.0
            return 90.0 - mapY / worldHeight * 180.0
        }
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

/**
 * Allowed [PlaneMapState.offsetY] range so the viewport stays inside the map
 * canvas vertically, given the aspect-ratio pivot at [centerLatitude].
 *
 * [viewportWidth] participates only through [MapProjection.geographicYFactor]
 * so clamps stay consistent with canvas-independent proportions.
 */
internal fun verticalOffsetBounds(
    viewportWidth: Float,
    viewportHeight: Float,
    zoom: Float,
    verticalScale: Float,
    centerLatitude: Double,
): ClosedFloatingPointRange<Float> {
    if (viewportWidth <= 0f || viewportHeight <= 0f || zoom <= 0f) return 0f..0f
    val cy = viewportHeight / 2f
    val pivot = MapProjection.latitudeToMapY(centerLatitude, viewportHeight)
    val scale = verticalScale.coerceAtLeast(MapProjection.MinVerticalScale) *
        MapProjection.geographicYFactor(viewportWidth, viewportHeight)
    val zoomY = zoom * scale
    val h = viewportHeight

    val upper = (pivot * zoomY - cy) / zoom
    val lower = (h - cy - (h - pivot) * zoomY) / zoom
    val lo = minOf(lower, upper)
    val hi = maxOf(lower, upper)
    return lo..hi
}

internal fun maxVerticalOffset(
    viewportWidth: Float,
    viewportHeight: Float,
    zoom: Float,
    verticalScale: Float,
    centerLatitude: Double,
): Float {
    val bounds = verticalOffsetBounds(
        viewportWidth,
        viewportHeight,
        zoom,
        verticalScale,
        centerLatitude,
    )
    return maxOf(0f, -bounds.start, bounds.endInclusive)
}
