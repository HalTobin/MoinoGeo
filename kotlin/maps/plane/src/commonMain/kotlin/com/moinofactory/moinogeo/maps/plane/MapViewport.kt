package com.moinofactory.moinogeo.maps.plane

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import com.moinofactory.moinogeo.maps.core.GeoPoint
import com.moinofactory.moinogeo.maps.core.GeoPolygon

/**
 * Screen projection used by [MapScope] overlays. [PlaneMap] and [AzimuthalMap]
 * each adapt their own projection into this.
 */
internal interface MapViewport {
    fun toOffset(point: GeoPoint): Offset
    fun toOffsetOnTile(point: GeoPoint, tileX: Int): Offset
    fun toGeoPoint(offset: Offset): GeoPoint
    fun metersToPixels(meters: Double, atLatitude: Double): Float
    fun clipRectForTile(tileX: Int, overlapPx: Float = 1.5f): Rect
    val wrapTiles: IntRange

    /** Disk clip for the azimuthal map. Null keeps the rectangular tile bands. */
    fun shapeClipPath(): Path?

    fun fillPath(polygon: GeoPolygon, tileX: Int): Path?
    fun borderPath(polygon: GeoPolygon, tileX: Int): Path?
}

internal fun MapProjection.viewport(): MapViewport = EquirectViewport(this)

internal fun AzimuthalProjection.viewport(): MapViewport = AzimuthViewport(this)

private class EquirectViewport(
    private val projection: MapProjection,
) : MapViewport {
    override fun toOffset(point: GeoPoint): Offset = projection.toOffset(point)

    override fun toOffsetOnTile(point: GeoPoint, tileX: Int): Offset =
        projection.toOffsetOnTile(point, tileX)

    override fun toGeoPoint(offset: Offset): GeoPoint = projection.toGeoPoint(offset)

    override fun metersToPixels(meters: Double, atLatitude: Double): Float =
        projection.metersToPixels(meters, atLatitude)

    override fun clipRectForTile(tileX: Int, overlapPx: Float): Rect =
        projection.clipRectForTile(tileX, overlapPx)

    override val wrapTiles: IntRange = MapProjection.HorizontalWrapTiles

    override fun shapeClipPath(): Path? = null

    override fun fillPath(polygon: GeoPolygon, tileX: Int): Path? =
        polygon.toPath(projection, tileX)

    override fun borderPath(polygon: GeoPolygon, tileX: Int): Path? =
        polygon.toBorderPath(projection, tileX)
}

private class AzimuthViewport(
    private val projection: AzimuthalProjection,
) : MapViewport {
    override fun toOffset(point: GeoPoint): Offset = projection.toOffset(point)

    override fun toOffsetOnTile(point: GeoPoint, tileX: Int): Offset =
        projection.toOffsetOnTile(point, tileX)

    override fun toGeoPoint(offset: Offset): GeoPoint = projection.toGeoPoint(offset)

    override fun metersToPixels(meters: Double, atLatitude: Double): Float =
        projection.metersToPixels(meters, atLatitude)

    override fun clipRectForTile(tileX: Int, overlapPx: Float): Rect =
        projection.clipRectForTile(tileX, overlapPx)

    override val wrapTiles: IntRange = AzimuthalProjection.HorizontalWrapTiles

    override fun shapeClipPath(): Path = projection.diskClipPath()

    override fun fillPath(polygon: GeoPolygon, tileX: Int): Path? =
        polygon.toPath(projection, tileX)

    override fun borderPath(polygon: GeoPolygon, tileX: Int): Path? =
        polygon.toBorderPath(projection, tileX)
}
