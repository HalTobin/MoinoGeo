package com.moinofactory.moinogeo.maps.plane

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Size
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class PlaneMapState(
    initialZoom: Float = 1.0f,
    initialOffsetX: Float = 0f,
    initialOffsetY: Float = 0f,
    // 1f = full world width visible; below that we'd need overlapping tiles (EvenOdd artifacts).
    val minZoom: Float = 1.0f,
    val maxZoom: Float = 12.0f,
) {
    var zoom by mutableFloatStateOf(initialZoom.coerceIn(minZoom, maxZoom))
    var offsetX by mutableFloatStateOf(initialOffsetX)
    var offsetY by mutableFloatStateOf(initialOffsetY)
    internal var viewportSize by mutableStateOf(Size.Zero)

    /** True after an animated double-tap zoom-in; next double-tap zooms back out. */
    private var doubleTapZoomedIn: Boolean = false
    private var doubleTapBaselineZoom: Float = initialZoom.coerceIn(minZoom, maxZoom)
    private var zoomAnimationJob: Job? = null

    fun zoomIn(factor: Float = 1.25f) {
        cancelZoomAnimation()
        zoom = (zoom * factor).coerceIn(minZoom, maxZoom)
        doubleTapZoomedIn = false
    }

    fun zoomOut(factor: Float = 1.25f) {
        cancelZoomAnimation()
        zoom = (zoom / factor).coerceIn(minZoom, maxZoom)
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
        offsetY += deltaY / zoom
        normalizeHorizontalOffset()
    }

    fun reset() {
        cancelZoomAnimation()
        zoom = 1.0f
        offsetX = 0f
        offsetY = 0f
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
        val endOy = startOy + (focalY - cy) * (1f / endZoom - 1f / startZoom)

        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(durationMillis = DoubleTapZoomMillis),
        ) { t, _ ->
            zoom = startZoom + (endZoom - startZoom) * t
            offsetX = startOx + (endOx - startOx) * t
            offsetY = startOy + (endOy - startOy) * t
        }
        normalizeHorizontalOffset()
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
        offsetX += (focalX - cx) * (1f / newZoom - 1f / oldZoom)
        offsetY += (focalY - cy) * (1f / newZoom - 1f / oldZoom)
        zoom = newZoom
        normalizeHorizontalOffset()
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
    minZoom: Float = 1.0f,
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
