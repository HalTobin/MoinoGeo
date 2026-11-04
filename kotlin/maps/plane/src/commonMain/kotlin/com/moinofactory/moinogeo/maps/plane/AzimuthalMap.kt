package com.moinofactory.moinogeo.maps.plane

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.toSize
import com.moinofactory.moinogeo.maps.countries.DefaultBoundaryProvider
import com.moinofactory.moinogeo.maps.core.BoundaryProvider
import com.moinofactory.moinogeo.maps.core.GeoPoint
import com.moinofactory.moinogeo.maps.core.MapStyle
import com.moinofactory.moinogeo.maps.core.TopoZone
import com.moinofactory.moinogeo.maps.core.TopographyProvider
import com.moinofactory.moinogeo.maps.topography.DefaultTopographyProvider

/**
 * 2D azimuthal equidistant map. Base layers come from [mapStyle] + providers;
 * overlays are declared in [content] via [MapScope] (`Marker`, `Circle`, `Area`).
 *
 * The sphere is drawn once, clipped to the projected disk (`c ≤ π`).
 *
 * @param onMapClick Optional tap callback with the geographic point under the
 * pointer. Omit (or pass `null`) when clicks are not needed.
 */
@Composable
fun AzimuthalMap(
    modifier: Modifier = Modifier,
    mapStyle: MapStyle = MapStyle.Default,
    boundaryProvider: BoundaryProvider = remember { DefaultBoundaryProvider() },
    topographyProvider: TopographyProvider = remember { DefaultTopographyProvider() },
    state: AzimuthalMapState = rememberAzimuthalMapState(),
    onMapClick: ((GeoPoint) -> Unit)? = null,
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
            .mapGestures(state, gestureScope, onMapClick),
    ) {
        val projection = remember(
            state.viewportSize,
            state.zoom,
            state.offsetX,
            state.offsetY,
            state.verticalScale,
            state.centerLatitude,
            state.centerLongitude,
            state.rotationAngle,
        ) {
            AzimuthalProjection(
                size = state.viewportSize,
                zoom = state.zoom,
                offsetX = state.offsetX,
                offsetY = state.offsetY,
                verticalScale = state.verticalScale,
                centerLatitude = state.centerLatitude,
                centerLongitude = state.centerLongitude,
                rotationAngle = state.rotationAngle,
            )
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val proj = AzimuthalProjection(
                size = size,
                zoom = state.zoom,
                offsetX = state.offsetX,
                offsetY = state.offsetY,
                verticalScale = state.verticalScale,
                centerLatitude = state.centerLatitude,
                centerLongitude = state.centerLongitude,
                rotationAngle = state.rotationAngle,
            )

            // Outside the disk is empty background. Ocean fills the disk so
            // missing water polygons never flash the land tint.
            drawRect(color = mapStyle.backgroundColor)

            for (tx in AzimuthalProjection.HorizontalWrapTiles) {
                if (proj.clipRectForTile(tx).isEmpty) continue
                clipPath(proj.diskClipPath()) {
                    drawRect(color = waterColor)
                    if (mapStyle.showTopography) {
                        // Land / ice / etc. Ocean was already painted inside the disk.
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
                        val strokeWidth = if (mapStyle.borderStyle.scaleWithZoom) {
                            mapStyle.borderStyle.lineWidth * state.zoom
                        } else {
                            mapStyle.borderStyle.lineWidth
                        }
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
