package com.moinofactory.moinogeo.maps.plane

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class PlaneMapState(
    initialZoom: Float = 1.0f,
    initialOffsetX: Float = 0f,
    initialOffsetY: Float = 0f
) {
    var zoom by mutableFloatStateOf(initialZoom.coerceIn(0.5f, 5.0f))
    var offsetX by mutableFloatStateOf(initialOffsetX)
    var offsetY by mutableFloatStateOf(initialOffsetY)

    fun zoomIn() {
        zoom = (zoom * 1.25f).coerceIn(0.5f, 5.0f)
    }

    fun zoomOut() {
        zoom = (zoom / 1.25f).coerceIn(0.5f, 5.0f)
    }

    fun reset() {
        zoom = 1.0f
        offsetX = 0f
        offsetY = 0f
    }
}

@Composable
fun rememberPlaneMapState(
    initialZoom: Float = 1.0f,
    initialOffsetX: Float = 0f,
    initialOffsetY: Float = 0f
): PlaneMapState {
    return remember { PlaneMapState(initialZoom, initialOffsetX, initialOffsetY) }
}
