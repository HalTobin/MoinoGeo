package com.moinofactory.moinogeo.maps.plane

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Size

class PlaneMapState(
    initialZoom: Float = 1.0f,
    initialOffsetX: Float = 0f,
    initialOffsetY: Float = 0f,
    val minZoom: Float = 0.5f,
    val maxZoom: Float = 8.0f,
) {
    var zoom by mutableFloatStateOf(initialZoom.coerceIn(minZoom, maxZoom))
    var offsetX by mutableFloatStateOf(initialOffsetX)
    var offsetY by mutableFloatStateOf(initialOffsetY)
    var viewportSize by mutableStateOf(Size.Zero)
        internal set

    fun zoomIn(factor: Float = 1.25f) {
        zoom = (zoom * factor).coerceIn(minZoom, maxZoom)
    }

    fun zoomOut(factor: Float = 1.25f) {
        zoom = (zoom / factor).coerceIn(minZoom, maxZoom)
    }

    fun reset() {
        zoom = 1.0f
        offsetX = 0f
        offsetY = 0f
    }

    /** Current projection for the last measured viewport. */
    fun projection(): MapProjection = MapProjection(
        size = viewportSize,
        zoom = zoom,
        offsetX = offsetX,
        offsetY = offsetY,
    )
}

@Composable
fun rememberPlaneMapState(
    initialZoom: Float = 1.0f,
    initialOffsetX: Float = 0f,
    initialOffsetY: Float = 0f,
    minZoom: Float = 0.5f,
    maxZoom: Float = 8.0f,
): PlaneMapState {
    return remember {
        PlaneMapState(
            initialZoom = initialZoom,
            initialOffsetX = initialOffsetX,
            initialOffsetY = initialOffsetY,
            minZoom = minZoom,
            maxZoom = maxZoom,
        )
    }
}
