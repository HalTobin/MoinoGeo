package com.moinofactory.moinogeo.maps.sphere

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import com.moinofactory.moinogeo.maps.core.BoundaryData
import com.moinofactory.moinogeo.maps.core.GeoPoint
import com.moinofactory.moinogeo.maps.core.GeoPolygon
import com.moinofactory.moinogeo.maps.core.MapStyle
import com.moinofactory.moinogeo.maps.core.TopoZone
import com.moinofactory.moinogeo.maps.core.TopographyData
import com.moinofactory.moinogeo.maps.core.wrapDeltaDegrees
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * CPU-rasterizes Natural Earth-style polygons into an equirectangular [ImageBitmap]
 * suitable as a Filament albedo on a UV sphere.
 */
object EquirectTextureBaker {
    const val DefaultWidth = 2048
    const val DefaultHeight = 1024

    fun bake(
        topography: TopographyData,
        boundaries: BoundaryData,
        mapStyle: MapStyle,
        width: Int = DefaultWidth,
        height: Int = DefaultHeight,
        highlightPolygons: List<HighlightPolygon> = emptyList(),
    ): ImageBitmap {
        val bitmap = ImageBitmap(width, height)
        val canvas = Canvas(bitmap)
        val water = mapStyle.zoneStyles[TopoZone.WATER]?.fillColor
            ?: mapStyle.backgroundColor

        canvas.drawRect(
            left = 0f,
            top = 0f,
            right = width.toFloat(),
            bottom = height.toFloat(),
            paint = Paint().apply { color = water },
        )

        if (mapStyle.showTopography) {
            val fillPaint = Paint().apply { style = PaintingStyle.Fill }
            for (feature in topography.features) {
                if (feature.zone == TopoZone.WATER) continue
                val zoneStyle = mapStyle.zoneStyles[feature.zone]
                    ?: mapStyle.zoneStyles[TopoZone.NEUTRAL]
                    ?: continue
                fillPaint.color = zoneStyle.fillColor
                val path = feature.geometry.toEquirectPath(width, height) ?: continue
                canvas.drawPath(path, fillPaint)
            }
            val waterStyle = mapStyle.zoneStyles[TopoZone.WATER]
            if (waterStyle != null) {
                fillPaint.color = waterStyle.fillColor
                for (feature in topography.features) {
                    if (feature.zone != TopoZone.WATER) continue
                    if (feature.geometry.isWorldSpanning()) continue
                    val path = feature.geometry.toEquirectPath(width, height) ?: continue
                    canvas.drawPath(path, fillPaint)
                }
            }
        }

        if (mapStyle.showBorders) {
            val stroke = Paint().apply {
                style = PaintingStyle.Stroke
                color = mapStyle.borderStyle.lineColor
                strokeWidth = max(1f, mapStyle.borderStyle.lineWidth)
                strokeCap = StrokeCap.Round
                strokeJoin = StrokeJoin.Round
            }
            for (feature in boundaries.features) {
                for (polygon in feature.geometries) {
                    val path = polygon.toEquirectBorderPath(width, height) ?: continue
                    canvas.drawPath(path, stroke)
                }
            }
        }

        for (highlight in highlightPolygons) {
            val fill = Paint().apply {
                style = PaintingStyle.Fill
                color = highlight.fillColor
            }
            val stroke = Paint().apply {
                style = PaintingStyle.Stroke
                color = highlight.borderColor
                strokeWidth = max(1f, highlight.borderWidth)
            }
            for (polygon in highlight.geometries) {
                polygon.toEquirectPath(width, height)?.let { canvas.drawPath(it, fill) }
                polygon.toEquirectBorderPath(width, height)?.let { canvas.drawPath(it, stroke) }
            }
        }

        return bitmap
    }

    fun bakeStarfield(
        background: SphereBackground.Starfield,
        width: Int = 1024,
        height: Int = 512,
    ): ImageBitmap {
        val bitmap = ImageBitmap(width, height)
        val canvas = Canvas(bitmap)
        canvas.drawRect(
            left = 0f,
            top = 0f,
            right = width.toFloat(),
            bottom = height.toFloat(),
            paint = Paint().apply { color = background.voidColor },
        )
        val starPaint = Paint().apply {
            style = PaintingStyle.Fill
            color = background.starColor
        }
        val count = (width * height * background.density.coerceIn(0.0005f, 0.02f)).toInt()
        var rng = background.seed.takeIf { it != 0 } ?: 1
        repeat(count) {
            rng = rng * 1103515245 + 12345
            val x = ((rng ushr 16) and 0x7FFF) % width
            rng = rng * 1103515245 + 12345
            val y = ((rng ushr 16) and 0x7FFF) % height
            rng = rng * 1103515245 + 12345
            val size = 0.6f + (((rng ushr 16) and 0x7FFF) % 100) / 100f
            canvas.drawCircle(Offset(x.toFloat(), y.toFloat()), size, starPaint)
        }
        return bitmap
    }
}

data class HighlightPolygon(
    val geometries: List<GeoPolygon>,
    val fillColor: Color,
    val borderColor: Color,
    val borderWidth: Float,
)

private fun GeoPolygon.toEquirectPath(width: Int, height: Int): Path? {
    if (outerBounds.size < 3) return null
    val path = Path().apply { fillType = PathFillType.EvenOdd }
    appendEquirectRing(path, outerBounds, width, height, close = true)
    for (hole in innerHoles) {
        if (hole.size >= 3) appendEquirectRing(path, hole, width, height, close = true)
    }
    return path
}

private fun GeoPolygon.toEquirectBorderPath(width: Int, height: Int): Path? {
    if (outerBounds.size < 2) return null
    val path = Path()
    var any = false
    if (appendEquirectBorderRing(path, outerBounds, width, height)) any = true
    for (hole in innerHoles) {
        if (hole.size >= 2 && appendEquirectBorderRing(path, hole, width, height)) any = true
    }
    return path.takeIf { any }
}

private fun appendEquirectRing(
    path: Path,
    ring: List<GeoPoint>,
    width: Int,
    height: Int,
    close: Boolean,
) {
    val start = ring.first().toEquirectOffset(width, height)
    path.moveTo(start.x, start.y)
    for (i in 1 until ring.size) {
        val p = ring[i].toEquirectOffset(width, height)
        path.lineTo(p.x, p.y)
    }
    if (close) path.close()
}

private fun appendEquirectBorderRing(
    path: Path,
    ring: List<GeoPoint>,
    width: Int,
    height: Int,
): Boolean {
    if (ring.size < 2) return false
    var penDown = false
    var hasEdge = false
    for (i in 1 until ring.size) {
        val a = ring[i - 1]
        val b = ring[i]
        if (isCutEdge(a, b)) {
            penDown = false
            continue
        }
        val pa = a.toEquirectOffset(width, height)
        val pb = b.toEquirectOffset(width, height)
        if (!penDown) {
            path.moveTo(pa.x, pa.y)
            penDown = true
        }
        path.lineTo(pb.x, pb.y)
        hasEdge = true
    }
    return hasEdge
}

private fun isCutEdge(a: GeoPoint, b: GeoPoint): Boolean {
    val dLon = abs(wrapDeltaDegrees(b.longitude - a.longitude))
    if (dLon > 180.0) return true
    val nearAntimeridian =
        (abs(abs(a.longitude) - 180.0) < 1e-3 && abs(abs(b.longitude) - 180.0) < 1e-3)
    val nearPole =
        (abs(abs(a.latitude) - 90.0) < 1e-3 && abs(abs(b.latitude) - 90.0) < 1e-3)
    return nearAntimeridian || nearPole
}

private fun GeoPoint.toEquirectOffset(width: Int, height: Int): Offset {
    val x = ((longitude + 180.0) / 360.0 * width).toFloat()
    val y = ((90.0 - latitude) / 180.0 * height).toFloat()
    return Offset(
        x = min(width.toFloat(), max(0f, x)),
        y = min(height.toFloat(), max(0f, y)),
    )
}

/** Heuristic matching plane map: huge rings that cover most of the world ocean shell. */
private fun GeoPolygon.isWorldSpanning(): Boolean {
    if (outerBounds.size < 4) return false
    var minLon = 180.0
    var maxLon = -180.0
    var minLat = 90.0
    var maxLat = -90.0
    for (p in outerBounds) {
        minLon = min(minLon, p.longitude)
        maxLon = max(maxLon, p.longitude)
        minLat = min(minLat, p.latitude)
        maxLat = max(maxLat, p.latitude)
    }
    return (maxLon - minLon) > 300.0 && (maxLat - minLat) > 140.0
}
