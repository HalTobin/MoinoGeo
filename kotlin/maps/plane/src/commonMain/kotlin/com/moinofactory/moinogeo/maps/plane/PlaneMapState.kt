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
    val maxZoom: Float = 12.0f,
) {
    var zoom by mutableFloatStateOf(initialZoom.coerceIn(minZoom, maxZoom))
    var offsetX by mutableFloatStateOf(initialOffsetX)
    var offsetY by mutableFloatStateOf(initialOffsetY)
    internal var viewportSize by mutableStateOf(Size.Zero)

    fun zoomIn(factor: Float = 1.25f) {
        zoom = (zoom * factor).coerceIn(minZoom, maxZoom)
    }

    fun zoomOut(factor: Float = 1.25f) {
        zoom = (zoom / factor).coerceIn(minZoom, maxZoom)
    }

    /**
     * Zoom by [factor] keeping the geographic point under ([focalX], [focalY])
     * (viewport pixels) fixed on screen.
     */
    fun zoomAt(focalX: Float, focalY: Float, factor: Float) {
        val width = viewportSize.width
        val height = viewportSize.height
        if (width <= 0f || height <= 0f || factor == 1f) return

        val oldZoom = zoom
        val newZoom = (oldZoom * factor).coerceIn(minZoom, maxZoom)
        if (newZoom == oldZoom) return

        val cx = width / 2f
        val cy = height / 2f
        offsetX += (focalX - cx) * (1f / newZoom - 1f / oldZoom)
        offsetY += (focalY - cy) * (1f / newZoom - 1f / oldZoom)
        zoom = newZoom
        normalizeHorizontalOffset()
    }

    /** Pan in screen pixels (converted to map space). */
    fun panBy(deltaX: Float, deltaY: Float) {
        if (zoom == 0f) return
        offsetX += deltaX / zoom
        offsetY += deltaY / zoom
        normalizeHorizontalOffset()
    }

    fun reset() {
        zoom = 1.0f
        offsetX = 0f
        offsetY = 0f
    }

    internal fun normalizeHorizontalOffset() {
        val period = viewportSize.width
        if (period > 0f) {
            offsetX = wrapOffsetCentered(offsetX, period)
        }
    }
}

@Composable
fun rememberPlaneMapState(
    initialZoom: Float = 1.0f,
    initialOffsetX: Float = 0f,
    initialOffsetY: Float = 0f,
    minZoom: Float = 0.5f,
    maxZoom: Float = 12.0f,
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
