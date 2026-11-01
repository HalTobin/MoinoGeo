package com.moinofactory.moinogeo.maps.plane

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.toSize
import com.moinofactory.moinogeo.maps.countries.DefaultBoundaryProvider
import com.moinofactory.moinogeo.maps.core.BoundaryProvider
import com.moinofactory.moinogeo.maps.core.GeoPolygon
import com.moinofactory.moinogeo.maps.core.MapStyle
import com.moinofactory.moinogeo.maps.core.TopoZone
import com.moinofactory.moinogeo.maps.core.TopographyProvider
import com.moinofactory.moinogeo.maps.topography.DefaultTopographyProvider

/**
 * 2D equirectangular map. Base layers come from [mapStyle] + providers;
 * overlays are declared in [content] via [MapScope] (`Marker`, `Circle`, `Area`).
 *
 * ```kotlin
 * PlaneMap(mapStyle = MapDefaults.style()) {
 *     Marker(GeoPoint(48.8566, 2.3522)) {
 *         Text("Paris")
 *     }
 *     Circle(
 *         center = GeoPoint(48.8566, 2.3522),
 *         radiusMeters = 50_000.0,
 *         fillColor = Color(0x3300BCD4),
 *         borderColor = Color.Cyan,
 *     )
 *     Area(
 *         geometries = france.geometries,
 *         fillColor = Color(0x66FF5722),
 *         borderColor = Color(0xFFFF5722),
 *         borderWidth = 3f,
 *     ) {
 *         Text("FR")
 *     }
 * }
 * ```
 */
@Composable
fun PlaneMap(
    modifier: Modifier = Modifier,
    mapStyle: MapStyle = MapStyle.Default,
    boundaryProvider: BoundaryProvider = remember { DefaultBoundaryProvider() },
    topographyProvider: TopographyProvider = remember { DefaultTopographyProvider() },
    state: PlaneMapState = rememberPlaneMapState(),
    content: @Composable MapScope.() -> Unit = {},
) {
    val boundaryData = remember(boundaryProvider) { boundaryProvider.getBoundaryData() }
    val topoData = remember(topographyProvider) { topographyProvider.getTopographyData() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { state.viewportSize = it.toSize() }
            .pointerInput(state) {
                detectTransformGestures { _, pan, zoomFactor, _ ->
                    state.zoom = (state.zoom * zoomFactor).coerceIn(state.minZoom, state.maxZoom)
                    state.offsetX += pan.x
                    state.offsetY += pan.y
                }
            },
    ) {
        val projection = remember(
            state.viewportSize,
            state.zoom,
            state.offsetX,
            state.offsetY,
        ) {
            MapProjection(
                size = state.viewportSize,
                zoom = state.zoom,
                offsetX = state.offsetX,
                offsetY = state.offsetY,
            )
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val proj = MapProjection(
                size = size,
                zoom = state.zoom,
                offsetX = state.offsetX,
                offsetY = state.offsetY,
            )

            drawRect(color = mapStyle.backgroundColor)

            if (mapStyle.showTopography) {
                for (feature in topoData.features) {
                    val zoneStyle = mapStyle.zoneStyles[feature.zone]
                        ?: mapStyle.zoneStyles[TopoZone.NEUTRAL]
                        ?: continue
                    val path = feature.geometry.toPath(proj) ?: continue
                    drawPath(path = path, color = zoneStyle.fillColor)
                    val stroke = zoneStyle.strokeColor
                    if (stroke != null && zoneStyle.strokeWidth > 0f) {
                        drawPath(
                            path = path,
                            color = stroke,
                            style = Stroke(width = zoneStyle.strokeWidth),
                        )
                    }
                }
            }

            if (mapStyle.showBorders) {
                val strokeWidth = mapStyle.borderStyle.lineWidth * state.zoom
                for (feature in boundaryData.features) {
                    for (polygon in feature.geometries) {
                        val path = polygon.toBorderPath(proj) ?: continue
                        drawPath(
                            path = path,
                            color = mapStyle.borderStyle.lineColor,
                            style = Stroke(width = strokeWidth),
                        )
                    }
                }
            }
        }

        ProvideMapProjection(projection) {
            Box(modifier = Modifier.fillMaxSize()) {
                MapScopeInstance.content()
            }
        }
    }
}

/** Stroke-only path (no fill / holes) for country borders. */
private fun GeoPolygon.toBorderPath(projection: MapProjection): Path? {
    if (outerBounds.size < 2) return null
    val path = Path()
    val start = projection.toOffset(outerBounds.first())
    path.moveTo(start.x, start.y)
    for (i in 1 until outerBounds.size) {
        val p = projection.toOffset(outerBounds[i])
        path.lineTo(p.x, p.y)
    }
    path.close()
    return path
}
