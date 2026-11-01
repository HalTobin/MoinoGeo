package com.moinofactory.moinogeo.maps.plane

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.moinofactory.moinogeo.maps.countries.DefaultBoundaryProvider
import com.moinofactory.moinogeo.maps.core.BoundaryProvider
import com.moinofactory.moinogeo.maps.core.GeoPoint
import com.moinofactory.moinogeo.maps.core.MapStyle
import com.moinofactory.moinogeo.maps.core.TopoZone
import com.moinofactory.moinogeo.maps.core.TopographyProvider
import com.moinofactory.moinogeo.maps.topography.DefaultTopographyProvider

@Composable
fun PlaneMap(
    modifier: Modifier = Modifier,
    mapStyle: MapStyle = MapStyle.Default,
    boundaryProvider: BoundaryProvider = remember { DefaultBoundaryProvider() },
    topographyProvider: TopographyProvider = remember { DefaultTopographyProvider() },
    state: PlaneMapState = rememberPlaneMapState()
) {
    val boundaryData = remember(boundaryProvider) { boundaryProvider.getBoundaryData() }
    val topoData = remember(topographyProvider) { topographyProvider.getTopographyData() }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoomFactor, _ ->
                        state.zoom = (state.zoom * zoomFactor).coerceIn(0.5f, 5.0f)
                        state.offsetX += pan.x
                        state.offsetY += pan.y
                    }
                }
        ) {
            val width = size.width
            val height = size.height

            // Background fill
            drawRect(color = mapStyle.backgroundColor)

            fun project(point: GeoPoint): Offset {
                val x = (point.longitude + 180.0) / 360.0 * width
                val y = (90.0 - point.latitude) / 180.0 * height
                val cx = width / 2f
                val cy = height / 2f
                val px = (cx + (x - cx + state.offsetX) * state.zoom).toFloat()
                val py = (cy + (y - cy + state.offsetY) * state.zoom).toFloat()
                return Offset(px, py)
            }

            // Topography zones
            if (mapStyle.showTopography) {
                for (feature in topoData.features) {
                    val zoneStyle = mapStyle.zoneStyles[feature.zone]
                        ?: mapStyle.zoneStyles[TopoZone.NEUTRAL]
                    if (zoneStyle != null && feature.geometry.outerBounds.isNotEmpty()) {
                        val path = Path()
                        val pts = feature.geometry.outerBounds
                        val start = project(pts.first())
                        path.moveTo(start.x, start.y)
                        for (i in 1 until pts.size) {
                            val p = project(pts[i])
                            path.lineTo(p.x, p.y)
                        }
                        path.close()
                        drawPath(path = path, color = zoneStyle.fillColor)
                    }
                }
            }

            // Borders
            if (mapStyle.showBorders) {
                for (feature in boundaryData.features) {
                    for (polygon in feature.geometries) {
                        if (polygon.outerBounds.isNotEmpty()) {
                            val path = Path()
                            val pts = polygon.outerBounds
                            val start = project(pts.first())
                            path.moveTo(start.x, start.y)
                            for (i in 1 until pts.size) {
                                val p = project(pts[i])
                                path.lineTo(p.x, p.y)
                            }
                            path.close()
                            drawPath(
                                path = path,
                                color = mapStyle.borderStyle.lineColor,
                                style = Stroke(width = mapStyle.borderStyle.lineWidth * state.zoom)
                            )
                        }
                    }
                }
            }
        }

        // Zoom controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Button(onClick = { state.zoomIn() }) { Text("+") }
            Button(onClick = { state.zoomOut() }) { Text("-") }
            Button(onClick = { state.reset() }) { Text("Reset") }
        }

        Text(
            text = "2D PLANE MAP",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        )
    }
}
