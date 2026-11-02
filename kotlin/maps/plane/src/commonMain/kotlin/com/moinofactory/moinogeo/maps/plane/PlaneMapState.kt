package com.moinofactory.moinogeo.maps.plane

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.moinofactory.moinogeo.maps.core.GeoPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class PlaneMapState(
    initialZoom: Float = 1.0f,
    initialOffsetX: Float = 0f,
    initialOffsetY: Float = 0f,
    initialVerticalScale: Float = 1f,
    // 1f = full world width visible; below that we'd need overlapping tiles (EvenOdd artifacts).
    val minZoom: Float = 1.0f,
    val maxZoom: Float = 12.0f,
) {
    var zoom by mutableFloatStateOf(initialZoom.coerceIn(minZoom, maxZoom))
    var offsetX by mutableFloatStateOf(initialOffsetX)
    var offsetY by mutableFloatStateOf(initialOffsetY)

    private var _verticalScale by mutableFloatStateOf(
        initialVerticalScale.coerceAtLeast(MapProjection.MinVerticalScale),
    )

    /**
     * Independent Y-axis stretch (1f = equirectangular aspect). Values above 1
     * expand vertically; below 1 compress.
     */
    var verticalScale: Float
        get() = _verticalScale
        set(value) {
            _verticalScale = value.coerceAtLeast(MapProjection.MinVerticalScale)
            clampVerticalOffset()
        }

    private var _viewportSize by mutableStateOf(Size.Zero)

    internal var viewportSize: Size
        get() = _viewportSize
        set(value) {
            _viewportSize = value
            normalizeHorizontalOffset()
            clampVerticalOffset()
            pendingCenter?.let { applyCenter(it) }
        }

    /** Geographic point currently under the viewport center. */
    val center: GeoPoint
        get() {
            val size = viewportSize
            if (size.width <= 0f || size.height <= 0f) return GeoPoint(0.0, 0.0)
            return projection().toGeoPoint(Offset(size.width / 2f, size.height / 2f))
        }

    /** Applied once [viewportSize] becomes non-zero. */
    private var pendingCenter: GeoPoint? = null

    /** True after an animated double-tap zoom-in; next double-tap zooms back out. */
    private var doubleTapZoomedIn: Boolean = false
    private var doubleTapBaselineZoom: Float = initialZoom.coerceIn(minZoom, maxZoom)
    private var zoomAnimationJob: Job? = null

    fun zoomIn(factor: Float = 1.25f) {
        cancelZoomAnimation()
        zoom = (zoom * factor).coerceIn(minZoom, maxZoom)
        clampVerticalOffset()
        doubleTapZoomedIn = false
    }

    fun zoomOut(factor: Float = 1.25f) {
        cancelZoomAnimation()
        zoom = (zoom / factor).coerceIn(minZoom, maxZoom)
        clampVerticalOffset()
        doubleTapZoomedIn = false
    }

    /**
     * Zoom by [factor] keeping the geographic point under ([focalX], [focalY])
     * (viewport pixels) fixed on screen.
     */
    fun zoomAt(focalX: Float, focalY: Float, factor: Float) {
        cancelZoomAnimation()
        applyZoomAt(focalX, focalY, factor)
        doubleTapZoomedIn = false
    }

    /** Pan in screen pixels (converted to map space). */
    fun panBy(deltaX: Float, deltaY: Float) {
        if (zoom == 0f) return
        offsetX += deltaX / zoom
        val zY = zoom * verticalScale.coerceAtLeast(MapProjection.MinVerticalScale)
        if (zY != 0f) {
            offsetY += deltaY / zY
        }
        normalizeHorizontalOffset()
        clampVerticalOffset()
    }

    /**
     * Centers the viewport on [point]. Horizontal wrap and vertical canvas
     * clamps are applied afterward. If the viewport size is not known yet,
     * the center is applied as soon as it becomes available.
     */
    fun setCenter(point: GeoPoint) {
        cancelZoomAnimation()
        pendingCenter = point
        applyCenter(point)
        doubleTapZoomedIn = false
    }

    private fun applyCenter(point: GeoPoint) {
        val width = viewportSize.width
        val height = viewportSize.height
        if (width <= 0f || height <= 0f) return

        val mapX = MapProjection.longitudeToMapX(point.longitude, width)
        val mapY = MapProjection.latitudeToMapY(point.latitude, height)
        val desiredOffsetY = height / 2f - mapY
        ensureVerticalRoomForOffset(desiredOffsetY)

        offsetX = width / 2f - mapX
        offsetY = desiredOffsetY
        normalizeHorizontalOffset()
        clampVerticalOffset()
        pendingCenter = null
    }

    /**
     * Bumps [zoom] (within [minZoom]..[maxZoom]) so |[desiredOffsetY]| stays
     * inside the vertical canvas clamp. Extreme latitudes near the poles may
     * still be clamped when [maxZoom] is insufficient.
     */
    private fun ensureVerticalRoomForOffset(desiredOffsetY: Float) {
        val height = viewportSize.height
        if (height <= 0f) return
        val cy = height / 2f
        val absDesired = kotlin.math.abs(desiredOffsetY)
        // Need room strictly inside the canvas; poles (|offsetY| == cy) are unreachable.
        if (absDesired < 1e-3f || absDesired >= cy - 1e-3f) return

        val minZY = 1f / (1f - absDesired / cy)
        val scale = verticalScale.coerceAtLeast(MapProjection.MinVerticalScale)
        val neededZoom = (minZY / scale).coerceIn(minZoom, maxZoom)
        if (neededZoom > zoom) {
            zoom = neededZoom
        }
    }

    fun reset() {
        cancelZoomAnimation()
        zoom = 1.0f
        offsetX = 0f
        offsetY = 0f
        verticalScale = 1f
        doubleTapZoomedIn = false
        doubleTapBaselineZoom = 1.0f
    }

    /**
     * Animated double-tap: first tap zooms in toward the point, second returns
     * to the zoom level from before the zoom-in.
     */
    suspend fun animateDoubleTapZoom(focalX: Float, focalY: Float) {
        coroutineScope {
            zoomAnimationJob?.cancel()
            val job = launch {
                val targetZoom = if (doubleTapZoomedIn) {
                    doubleTapBaselineZoom
                } else {
                    doubleTapBaselineZoom = zoom
                    (zoom * DoubleTapZoomInFactor).coerceIn(minZoom, maxZoom)
                }
                val zoomingIn = !doubleTapZoomedIn
                animateZoomTo(focalX, focalY, targetZoom)
                doubleTapZoomedIn = zoomingIn && zoom > doubleTapBaselineZoom + 0.01f
            }
            zoomAnimationJob = job
            job.join()
        }
    }

    private suspend fun animateZoomTo(focalX: Float, focalY: Float, targetZoom: Float) {
        val width = viewportSize.width
        val height = viewportSize.height
        if (width <= 0f || height <= 0f) return

        val startZoom = zoom
        val endZoom = targetZoom.coerceIn(minZoom, maxZoom)
        if (kotlin.math.abs(endZoom - startZoom) < 1e-4f) return

        val cx = width / 2f
        val cy = height / 2f
        val startOx = offsetX
        val startOy = offsetY
        val endOx = startOx + (focalX - cx) * (1f / endZoom - 1f / startZoom)
        val startZY = startZoom * verticalScale.coerceAtLeast(MapProjection.MinVerticalScale)
        val endZY = endZoom * verticalScale.coerceAtLeast(MapProjection.MinVerticalScale)
        val endOy = startOy + (focalY - cy) * (1f / endZY - 1f / startZY)

        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(durationMillis = DoubleTapZoomMillis),
        ) { t, _ ->
            zoom = startZoom + (endZoom - startZoom) * t
            offsetX = startOx + (endOx - startOx) * t
            offsetY = startOy + (endOy - startOy) * t
            clampVerticalOffset()
        }
        normalizeHorizontalOffset()
        clampVerticalOffset()
    }

    private fun applyZoomAt(focalX: Float, focalY: Float, factor: Float) {
        val width = viewportSize.width
        val height = viewportSize.height
        if (width <= 0f || height <= 0f || factor == 1f) return

        val oldZoom = zoom
        val newZoom = (oldZoom * factor).coerceIn(minZoom, maxZoom)
        if (newZoom == oldZoom) return

        val cx = width / 2f
        val cy = height / 2f
        val scale = verticalScale.coerceAtLeast(MapProjection.MinVerticalScale)
        val oldZY = oldZoom * scale
        val newZY = newZoom * scale
        offsetX += (focalX - cx) * (1f / newZoom - 1f / oldZoom)
        offsetY += (focalY - cy) * (1f / newZY - 1f / oldZY)
        zoom = newZoom
        normalizeHorizontalOffset()
        clampVerticalOffset()
    }

    private fun cancelZoomAnimation() {
        zoomAnimationJob?.cancel()
        zoomAnimationJob = null
    }

    internal fun normalizeHorizontalOffset() {
        val period = viewportSize.width
        if (period > 0f) {
            offsetX = wrapOffsetCentered(offsetX, period)
        }
    }

    internal fun clampVerticalOffset() {
        val maxOffset = maxVerticalOffset(
            viewportHeight = viewportSize.height,
            zoom = zoom,
            verticalScale = verticalScale,
        )
        offsetY = offsetY.coerceIn(-maxOffset, maxOffset)
    }

    internal fun projection(): MapProjection =
        MapProjection(
            size = viewportSize,
            zoom = zoom,
            offsetX = offsetX,
            offsetY = offsetY,
            verticalScale = verticalScale,
        )

    private companion object {
        const val DoubleTapZoomInFactor = 2.0f
        const val DoubleTapZoomMillis = 280
    }
}

@Composable
fun rememberPlaneMapState(
    initialZoom: Float = 1.0f,
    initialOffsetX: Float = 0f,
    initialOffsetY: Float = 0f,
    initialVerticalScale: Float = 1f,
    minZoom: Float = 1.0f,
    maxZoom: Float = 12.0f,
): PlaneMapState {
    return remember {
        PlaneMapState(
            initialZoom = initialZoom,
            initialOffsetX = initialOffsetX,
            initialOffsetY = initialOffsetY,
            initialVerticalScale = initialVerticalScale,
            minZoom = minZoom,
            maxZoom = maxZoom,
        )
    }
}
