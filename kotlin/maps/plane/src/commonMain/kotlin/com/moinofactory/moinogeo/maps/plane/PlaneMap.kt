package com.moinofactory.moinogeo.maps.plane

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.toSize
import com.moinofactory.moinogeo.maps.countries.DefaultBoundaryProvider
import com.moinofactory.moinogeo.maps.core.BoundaryProvider
import com.moinofactory.moinogeo.maps.core.MapStyle
import com.moinofactory.moinogeo.maps.core.TopoZone
import com.moinofactory.moinogeo.maps.core.TopographyProvider
import com.moinofactory.moinogeo.maps.topography.DefaultTopographyProvider

/**
 * 2D equirectangular map. Base layers come from [mapStyle] + providers;
 * overlays are declared in [content] via [MapScope] (`Marker`, `Circle`, `Area`).
 *
 * Horizontal looping draws three clipped world copies so oceans / Antarctica
 * keep stable fills (no longitude-unwrap of wide polygons).
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
    val gestureScope = rememberCoroutineScope()
    val waterColor = mapStyle.zoneStyles[TopoZone.WATER]?.fillColor ?: mapStyle.backgroundColor

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { state.viewportSize = it.toSize() }
            .mapGestures(state, gestureScope),
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

            // Ocean is the canvas base so missing/clipped water polygons never
            // flash the land-tint background when looping.
            drawRect(color = waterColor)

            for (tx in MapProjection.HorizontalWrapTiles) {
                val band = proj.clipRectForTile(tx)
                clipRect(
                    left = band.left,
                    top = band.top,
                    right = band.right,
                    bottom = band.bottom,
                ) {
                    if (mapStyle.showTopography) {
                        // Land / ice / etc. (ocean is canvas background).
                        for (feature in topoData.features) {
                            if (feature.zone == TopoZone.WATER) continue
                            val zoneStyle = mapStyle.zoneStyles[feature.zone]
                                ?: mapStyle.zoneStyles[TopoZone.NEUTRAL]
                                ?: continue
                            val path = feature.geometry.toPath(proj, tx) ?: continue
                            drawPath(path = path, color = zoneStyle.fillColor)
                        }
                        // Lakes only — skip the world ocean shell (holes would
                        // paint over continents; canvas already provides ocean).
                        val waterStyle = mapStyle.zoneStyles[TopoZone.WATER]
                        if (waterStyle != null) {
                            for (feature in topoData.features) {
                                if (feature.zone != TopoZone.WATER) continue
                                if (feature.geometry.isWorldSpanning()) continue
                                val path = feature.geometry.toPath(proj, tx) ?: continue
                                drawPath(path = path, color = waterStyle.fillColor)
                            }
                        }
                    }

                    if (mapStyle.showBorders) {
                        val strokeWidth = mapStyle.borderStyle.lineWidth * state.zoom
                        for (feature in boundaryData.features) {
                            for (polygon in feature.geometries) {
                                val path = polygon.toBorderPath(proj, tx) ?: continue
                                drawPath(
                                    path = path,
                                    color = mapStyle.borderStyle.lineColor,
                                    style = Stroke(width = strokeWidth),
                                )
                            }
                        }
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
