package com.moinofactory.moinogeo.maps.plane

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.moinofactory.moinogeo.maps.core.GeoPoint
import com.moinofactory.moinogeo.maps.core.GeoPolygon
import kotlin.math.roundToInt

/** DSL marker mirroring Compose layout scopes (BoxScope, ColumnScope, …). */
@DslMarker
annotation class MapScopeMarker

/**
 * Receiver scope for [PlaneMap] and [AzimuthalMap] content. Overlay APIs take
 * **geographic** coordinates; the map projects them into the current viewport.
 */
@MapScopeMarker
interface MapScope {
    /**
     * Places a simple pin at [position]. Optional [content] is shown in a chip
     * above the pin (use the [MarkerScope] receiver for marker-local UI).
     *
     * ```kotlin
     * Marker(GeoPoint(48.86, 2.35)) {
     *     Text("Paris")
     * }
     * ```
     */
    @Composable
    fun Marker(
        position: GeoPoint,
        onClick: (() -> Unit)? = null,
        content: (@Composable MarkerScope.() -> Unit)? = null,
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
     * Optional [content] is composed at the approximate geographic center
     * via a [Marker]. Optional [onClick] fires when the polygon is tapped.
     */
    @Composable
    fun Area(
        geometries: List<GeoPolygon>,
        fillColor: Color = Color(0x663F51B5),
        borderColor: Color = Color(0xFF3F51B5),
        borderWidth: Float = 2f,
        onClick: (() -> Unit)? = null,
        content: (@Composable MarkerScope.() -> Unit)? = null,
    )
}

/** Content slot for [MapScope.Marker] / [MapScope.Area] center labels. */
@MapScopeMarker
interface MarkerScope

/** Convenience overload for a single polygon. */
@Composable
fun MapScope.Area(
    geometry: GeoPolygon,
    fillColor: Color = Color(0x663F51B5),
    borderColor: Color = Color(0xFF3F51B5),
    borderWidth: Float = 2f,
    onClick: (() -> Unit)? = null,
    content: (@Composable MarkerScope.() -> Unit)? = null,
) {
    Area(
        geometries = listOf(geometry),
        fillColor = fillColor,
        borderColor = borderColor,
        borderWidth = borderWidth,
        onClick = onClick,
        content = content,
    )
}

internal val LocalMapProjection = compositionLocalOf<MapViewport> {
    MapProjection.Empty.viewport()
}

@Immutable
internal object MarkerScopeInstance : MarkerScope

@Immutable
internal object MapScopeInstance : MapScope {
    @Composable
    override fun Marker(
        position: GeoPoint,
        onClick: (() -> Unit)?,
        content: (@Composable MarkerScope.() -> Unit)?,
    ) {
        val proj = LocalMapProjection.current
        val anchor = proj.toOffset(position)
        Box(modifier = Modifier.fillMaxSize()) {
            Layout(
                content = {
                    DefaultMapMarkerPin(
                        onClick = onClick,
                        content = content?.let { block ->
                            { MarkerScopeInstance.block() }
                        },
                    )
                },
                modifier = Modifier.offset {
                    IntOffset(anchor.x.roundToInt(), anchor.y.roundToInt())
                },
            ) { measurables, _ ->
                val placeable = measurables.first().measure(Constraints())
                layout(0, 0) {
                    placeable.place(
                        x = -placeable.width / 2,
                        y = -placeable.height,
                    )
                }
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
        val radiusPx = proj.metersToPixels(radiusMeters, center.latitude)
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (radiusPx <= 0f) return@Canvas
            for (tx in proj.wrapTiles) {
                val screenCenter = proj.toOffsetOnTile(center, tx)
                if (fillColor.alpha > 0f) {
                    drawCircle(color = fillColor, radius = radiusPx, center = screenCenter)
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
    }

    @Composable
    override fun Area(
        geometries: List<GeoPolygon>,
        fillColor: Color,
        borderColor: Color,
        borderWidth: Float,
        onClick: (() -> Unit)?,
        content: (@Composable MarkerScope.() -> Unit)?,
    ) {
        val proj = LocalMapProjection.current
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .tapIfHit(
                    enabled = onClick != null,
                    hitTest = { offset ->
                        val point = proj.toGeoPoint(offset)
                        geometries.any { it.contains(point) }
                    },
                    onTap = { onClick?.invoke() },
                ),
        ) {
            for (tx in proj.wrapTiles) {
                clipToViewport(proj, tx) {
                    for (polygon in geometries) {
                        if (fillColor.alpha > 0f) {
                            proj.fillPath(polygon, tx)?.let { fill ->
                                drawPath(path = fill, color = fillColor)
                            }
                        }
                        if (borderWidth > 0f && borderColor.alpha > 0f) {
                            proj.borderPath(polygon, tx)?.let { stroke ->
                                drawPath(
                                    path = stroke,
                                    color = borderColor,
                                    style = Stroke(width = borderWidth),
                                )
                            }
                        }
                    }
                }
            }
        }
        if (content != null) {
            val center = remember(geometries) { geometries.approximateCenter() } ?: return
            Marker(position = center, content = content)
        }
    }
}

@Composable
private fun DefaultMapMarkerPin(
    onClick: (() -> Unit)?,
    content: (@Composable () -> Unit)?,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = if (onClick != null) {
            Modifier.clickable(onClick = onClick)
        } else {
            Modifier
        },
    ) {
        if (content != null) {
            Box(
                modifier = Modifier
                    .shadow(2.dp, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xF2FFFFFF))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                content()
            }
        }
        Box(
            modifier = Modifier
                .padding(top = if (content != null) 2.dp else 0.dp)
                .size(10.dp)
                .shadow(1.dp, CircleShape)
                .clip(CircleShape)
                .background(Color(0xFFE53935)),
        )
    }
}

/**
 * When the press starts inside the hit region, consumes the gesture so
 * [PlaneMap]'s optional onMapClick does not also fire. Presses outside
 * pass through to map pan / click handlers.
 */
private fun Modifier.tapIfHit(
    enabled: Boolean,
    hitTest: (Offset) -> Boolean,
    onTap: () -> Unit,
): Modifier {
    if (!enabled) return this
    return pointerInput(hitTest, onTap) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = true)
            if (!hitTest(down.position)) return@awaitEachGesture
            down.consume()
            val up = waitForUpOrCancellation() ?: return@awaitEachGesture
            if ((up.position - down.position).getDistance() <= viewConfiguration.touchSlop) {
                onTap()
            }
        }
    }
}

@Composable
internal fun ProvideMapProjection(
    projection: MapProjection,
    content: @Composable () -> Unit,
) {
    ProvideMapViewport(projection.viewport(), content)
}

@Composable
internal fun ProvideMapProjection(
    projection: AzimuthalProjection,
    content: @Composable () -> Unit,
) {
    ProvideMapViewport(projection.viewport(), content)
}

@Composable
private fun ProvideMapViewport(
    viewport: MapViewport,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalMapProjection provides viewport, content = content)
}

private inline fun DrawScope.clipToViewport(
    viewport: MapViewport,
    tileX: Int,
    crossinline block: DrawScope.() -> Unit,
) {
    val shape: Path? = viewport.shapeClipPath()
    if (shape != null) {
        clipPath(shape, block = block)
    } else {
        val band = viewport.clipRectForTile(tileX)
        clipRect(
            left = band.left,
            top = band.top,
            right = band.right,
            bottom = band.bottom,
            block = block,
        )
    }
}
