package com.moinofactory.moinogeo.maps.sphere

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.moinofactory.moinogeo.maps.core.GeoPoint
import com.moinofactory.moinogeo.maps.core.GeoPolygon
import kotlin.math.roundToInt

/** DSL marker mirroring Compose layout scopes. */
@DslMarker
annotation class SphereMapScopeMarker

/**
 * Receiver scope for [SphereMap] content. Overlay APIs take geographic coordinates;
 * the map projects them into the current viewport (front-face only for markers).
 */
@SphereMapScopeMarker
interface SphereMapScope {
    @Composable
    fun Marker(
        position: GeoPoint,
        onClick: (() -> Unit)? = null,
        content: (@Composable SphereMarkerScope.() -> Unit)? = null,
    )

    @Composable
    fun Circle(
        center: GeoPoint,
        radiusMeters: Double,
        fillColor: Color = Color.Transparent,
        borderColor: Color = Color.Black,
        borderWidth: Float = 2f,
    )

    @Composable
    fun Area(
        geometries: List<GeoPolygon>,
        fillColor: Color = Color(0x663F51B5),
        borderColor: Color = Color(0xFF3F51B5),
        borderWidth: Float = 2f,
        onClick: (() -> Unit)? = null,
        content: (@Composable SphereMarkerScope.() -> Unit)? = null,
    )
}

@SphereMapScopeMarker
interface SphereMarkerScope

internal val LocalSphereProjection = compositionLocalOf<SphereProjection> {
    error("SphereProjection not provided")
}

internal val LocalSphereHighlightRegistrar = compositionLocalOf<SphereHighlightRegistrar> {
    error("SphereHighlightRegistrar not provided")
}

internal fun interface SphereHighlightRegistrar {
    /** Returns an unregister callback. */
    fun register(highlight: HighlightPolygon): () -> Unit
}

@Composable
internal fun ProvideSphereMapLocals(
    projection: SphereProjection,
    highlightRegistrar: SphereHighlightRegistrar,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalSphereProjection provides projection,
        LocalSphereHighlightRegistrar provides highlightRegistrar,
    ) {
        content()
    }
}

internal object SphereMapScopeInstance : SphereMapScope {
    @Composable
    override fun Marker(
        position: GeoPoint,
        onClick: (() -> Unit)?,
        content: (@Composable SphereMarkerScope.() -> Unit)?,
    ) {
        val projection = LocalSphereProjection.current
        val offset = projection.toOffset(position) ?: return
        Box(
            modifier = Modifier
                .offset { IntOffset(offset.x.roundToInt() - 12, offset.y.roundToInt() - 24) }
                .size(width = 24.dp, height = 36.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (content != null) {
                    Box(
                        modifier = Modifier
                            .shadow(2.dp, RoundedCornerShape(6.dp))
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White.copy(alpha = 0.92f))
                            .then(
                                if (onClick != null) Modifier.clickable(onClick = onClick)
                                else Modifier,
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        SphereMarkerScopeInstance.content()
                    }
                }
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .shadow(2.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color(0xFFE53935))
                        .then(
                            if (onClick != null && content == null) {
                                Modifier.clickable(onClick = onClick)
                            } else {
                                Modifier
                            },
                        ),
                )
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
        val projection = LocalSphereProjection.current
        val offset = projection.toOffset(center) ?: return
        val radiusPx = projection.metersToPixels(radiusMeters, center.latitude)
        Canvas(
            modifier = Modifier
                .offset {
                    IntOffset(
                        (offset.x - radiusPx).roundToInt(),
                        (offset.y - radiusPx).roundToInt(),
                    )
                }
                .size((radiusPx * 2f).dp),
        ) {
            drawCircle(color = fillColor)
            drawCircle(color = borderColor, style = Stroke(width = borderWidth))
        }
    }

    @Composable
    override fun Area(
        geometries: List<GeoPolygon>,
        fillColor: Color,
        borderColor: Color,
        borderWidth: Float,
        onClick: (() -> Unit)?,
        content: (@Composable SphereMarkerScope.() -> Unit)?,
    ) {
        val registrar = LocalSphereHighlightRegistrar.current
        androidx.compose.runtime.DisposableEffect(
            geometries,
            fillColor,
            borderColor,
            borderWidth,
        ) {
            val unregister = registrar.register(
                HighlightPolygon(
                    geometries = geometries,
                    fillColor = fillColor,
                    borderColor = borderColor,
                    borderWidth = borderWidth,
                ),
            )
            onDispose(unregister)
        }

        val centroid = remember(geometries) { approximateCentroid(geometries) }
        if (centroid != null && (content != null || onClick != null)) {
            Marker(position = centroid, onClick = onClick, content = content)
        }
    }
}

private object SphereMarkerScopeInstance : SphereMarkerScope

private fun approximateCentroid(geometries: List<GeoPolygon>): GeoPoint? {
    var count = 0
    var lat = 0.0
    var lon = 0.0
    for (polygon in geometries) {
        for (p in polygon.outerBounds) {
            lat += p.latitude
            lon += p.longitude
            count++
        }
    }
    if (count == 0) return null
    return GeoPoint(latitude = lat / count, longitude = lon / count)
}
