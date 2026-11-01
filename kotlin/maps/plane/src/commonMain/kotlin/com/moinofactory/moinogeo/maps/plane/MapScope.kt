package com.moinofactory.moinogeo.maps.plane

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.moinofactory.moinogeo.maps.core.GeoPoint
import com.moinofactory.moinogeo.maps.core.GeoPolygon
import kotlin.math.roundToInt

/** DSL marker mirroring Compose layout scopes (BoxScope, ColumnScope, …). */
@DslMarker
annotation class MapScopeMarker

/**
 * Receiver scope for [PlaneMap] content. Overlay APIs take **geographic**
 * coordinates; the map projects them into the current viewport.
 */
@MapScopeMarker
interface MapScope {
    /**
     * Places [content] at [position]. [contentAlignment] anchors the content
     * relative to the projected point (e.g. [Alignment.BottomCenter] for a pin).
     */
    @Composable
    fun Marker(
        position: GeoPoint,
        contentAlignment: Alignment = Alignment.BottomCenter,
        content: @Composable () -> Unit,
    )

    /**
     * Filled/stroked circle centered on [center] with a real-world [radiusMeters].
     */
    @Composable
    fun Circle(
        center: GeoPoint,
        radiusMeters: Double,
        fillColor: Color = Color.Transparent,
        borderColor: Color = Color.Black,
        borderWidth: Float = 2f,
    )

    /**
     * Highlights one or more polygons (country, city, custom region).
     * Optional [content] is composed at the approximate geographic center.
     */
    @Composable
    fun Area(
        geometries: List<GeoPolygon>,
        fillColor: Color = Color(0x663F51B5),
        borderColor: Color = Color(0xFF3F51B5),
        borderWidth: Float = 2f,
        contentAlignment: Alignment = Alignment.Center,
        content: (@Composable () -> Unit)? = null,
    )
}

/** Convenience overload for a single polygon. */
@Composable
fun MapScope.Area(
    geometry: GeoPolygon,
    fillColor: Color = Color(0x663F51B5),
    borderColor: Color = Color(0xFF3F51B5),
    borderWidth: Float = 2f,
    contentAlignment: Alignment = Alignment.Center,
    content: (@Composable () -> Unit)? = null,
) {
    Area(
        geometries = listOf(geometry),
        fillColor = fillColor,
        borderColor = borderColor,
        borderWidth = borderWidth,
        contentAlignment = contentAlignment,
        content = content,
    )
}

internal val LocalMapProjection = compositionLocalOf { MapProjection.Empty }

@Immutable
internal object MapScopeInstance : MapScope {
    @Composable
    override fun Marker(
        position: GeoPoint,
        contentAlignment: Alignment,
        content: @Composable () -> Unit,
    ) {
        val proj = LocalMapProjection.current
        val offset = proj.toOffset(position)
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
                    .size(0.dp),
                contentAlignment = contentAlignment,
            ) {
                content()
            }
        }
    }

    @Composable
    override fun Circle(
        center: GeoPoint,
        radiusMeters: Double,
        fillColor: Color,
        borderColor: Color,
        borderWidth: Float,
    ) {
        val proj = LocalMapProjection.current
        val screenCenter = proj.toOffset(center)
        val radiusPx = proj.metersToPixels(radiusMeters, center.latitude)
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (radiusPx <= 0f) return@Canvas
            if (fillColor.alpha > 0f) {
                drawCircle(
                    color = fillColor,
                    radius = radiusPx,
                    center = screenCenter,
                )
            }
            if (borderWidth > 0f && borderColor.alpha > 0f) {
                drawCircle(
                    color = borderColor,
                    radius = radiusPx,
                    center = screenCenter,
                    style = Stroke(width = borderWidth),
                )
            }
        }
    }

    @Composable
    override fun Area(
        geometries: List<GeoPolygon>,
        fillColor: Color,
        borderColor: Color,
        borderWidth: Float,
        contentAlignment: Alignment,
        content: (@Composable () -> Unit)?,
    ) {
        val proj = LocalMapProjection.current
        Canvas(modifier = Modifier.fillMaxSize()) {
            for (polygon in geometries) {
                val path = polygon.toPath(proj) ?: continue
                if (fillColor.alpha > 0f) {
                    drawPath(path = path, color = fillColor)
                }
                if (borderWidth > 0f && borderColor.alpha > 0f) {
                    drawPath(
                        path = path,
                        color = borderColor,
                        style = Stroke(width = borderWidth),
                    )
                }
            }
        }
        if (content != null) {
            val center = remember(geometries) { geometries.approximateCenter() } ?: return
            Marker(position = center, contentAlignment = contentAlignment, content = content)
        }
    }
}

@Composable
internal fun ProvideMapProjection(
    projection: MapProjection,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalMapProjection provides projection, content = content)
}

internal fun GeoPolygon.toPath(projection: MapProjection): Path? {
    if (outerBounds.size < 3) return null
    val path = Path().apply { fillType = PathFillType.EvenOdd }
    appendRing(path, outerBounds, projection)
    for (hole in innerHoles) {
        if (hole.size >= 3) appendRing(path, hole, projection)
    }
    return path
}

private fun appendRing(
    path: Path,
    ring: List<GeoPoint>,
    projection: MapProjection,
) {
    val start = projection.toOffset(ring.first())
    path.moveTo(start.x, start.y)
    for (i in 1 until ring.size) {
        val p = projection.toOffset(ring[i])
        path.lineTo(p.x, p.y)
    }
    path.close()
}

/** Simple mean of all outer-ring vertices (good enough for overlay anchors). */
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
