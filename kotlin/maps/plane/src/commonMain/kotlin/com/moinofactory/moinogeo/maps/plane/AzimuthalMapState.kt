package com.moinofactory.moinogeo.maps.plane

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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

class AzimuthalMapState(
    initialZoom: Float = 1.0f,
    initialOffsetX: Float = 0f,
    initialOffsetY: Float = 0f,
    initialVerticalScale: Float = 1f,
    initialCenterLatitude: Double = 0.0,
    initialCenterLongitude: Double = 0.0,
    initialRotationAngle: Float = 0f,
    // 1f fits the projected disk across the shorter viewport side.
    val minZoom: Float = 1.0f,
    val maxZoom: Float = 12.0f,
) : MapCamera {
    var zoom by mutableFloatStateOf(initialZoom.coerceIn(minZoom, maxZoom))
    var offsetX by mutableFloatStateOf(initialOffsetX)
    var offsetY by mutableFloatStateOf(initialOffsetY)

    private var _rotationAngle by mutableFloatStateOf(initialRotationAngle)

    /** Disk rotation in degrees, applied around the screen center before pan. */
    override var rotationAngle: Float
        get() = _rotationAngle
        set(value) {
            _rotationAngle = value
            clampHorizontalOffset()
            clampVerticalOffset()
        }

    private var _verticalScale by mutableFloatStateOf(
        initialVerticalScale.coerceAtLeast(AzimuthalProjection.MinVerticalScale),
    )

    /**
     * Y-axis stretch applied in the azimuthal plane before rotation
     * (1f = true equidistant disk). Expansion is around the projection origin.
     */
    var verticalScale: Float
        get() = _verticalScale
        set(value) {
            _verticalScale = value.coerceAtLeast(AzimuthalProjection.MinVerticalScale)
            clampHorizontalOffset()
            clampVerticalOffset()
        }

    private var _centerLatitude by mutableDoubleStateOf(
        initialCenterLatitude.coerceIn(-90.0, 90.0),
    )

    /** Latitude of the azimuthal origin. With zero pan this point is at screen center. */
    var centerLatitude: Double
        get() = _centerLatitude
        set(value) {
            _centerLatitude = value.coerceIn(-90.0, 90.0)
        }

    private var _centerLongitude by mutableDoubleStateOf(
        wrapLongitudeDegrees(initialCenterLongitude),
    )

    /** Longitude of the azimuthal origin. With zero pan this point is at screen center. */
    var centerLongitude: Double
        get() = _centerLongitude
        set(value) {
            _centerLongitude = wrapLongitudeDegrees(value)
        }

    private var _viewportSize by mutableStateOf(Size.Zero)

    internal var viewportSize: Size
        get() = _viewportSize
        set(value) {
            _viewportSize = value
            clampHorizontalOffset()
            clampVerticalOffset()
            pendingCenter?.let { applyCameraCenter(it) }
        }

    /** Geographic point currently under the viewport center (camera). */
    val cameraCenter: GeoPoint
        get() {
            val size = viewportSize
            if (size.width <= 0f || size.height <= 0f) return GeoPoint(0.0, 0.0)
            return projection().toGeoPoint(Offset(size.width / 2f, size.height / 2f))
        }

    @Deprecated("Use cameraCenter")
    val center: GeoPoint get() = cameraCenter

    /** Applied once [viewportSize] becomes non-zero. */
    private var pendingCenter: GeoPoint? = null

    /** True after an animated double-tap zoom-in; next double-tap zooms back out. */
    private var doubleTapZoomedIn: Boolean = false
    private var doubleTapBaselineZoom: Float = initialZoom.coerceIn(minZoom, maxZoom)
    private var zoomAnimationJob: Job? = null

    fun zoomIn(factor: Float = 1.25f) {
        cancelZoomAnimation()
        zoom = (zoom * factor).coerceIn(minZoom, maxZoom)
        clampHorizontalOffset()
        clampVerticalOffset()
        doubleTapZoomedIn = false
    }

    fun zoomOut(factor: Float = 1.25f) {
        cancelZoomAnimation()
        zoom = (zoom / factor).coerceIn(minZoom, maxZoom)
        clampHorizontalOffset()
        clampVerticalOffset()
        doubleTapZoomedIn = false
    }

    /**
     * Zoom by [factor] keeping the geographic point under ([focalX], [focalY])
     * (viewport pixels) fixed on screen.
     */
    override fun zoomAt(focalX: Float, focalY: Float, factor: Float) {
        cancelZoomAnimation()
        applyZoomAt(focalX, focalY, factor)
        doubleTapZoomedIn = false
    }

    /** Pan in screen pixels. Offsets are in pre-zoom map space (`pixels / zoom`). */
    override fun panBy(deltaX: Float, deltaY: Float) {
        if (zoom == 0f) return
        offsetX += deltaX / zoom
        offsetY += deltaY / zoom
        clampHorizontalOffset()
        clampVerticalOffset()
    }

    /**
     * Recenters the azimuthal origin on [point] and clears pan, so
     * (φ₀, λ₀) lies on the screen center whenever the offsets are zero.
     * If the viewport size is not known yet, the camera is applied as soon
     * as it becomes available.
     */
    fun setCenter(point: GeoPoint) {
        cancelZoomAnimation()
        pendingCenter = point
        applyCameraCenter(point)
        doubleTapZoomedIn = false
    }

    private fun applyCameraCenter(point: GeoPoint) {
        _centerLatitude = point.latitude.coerceIn(-90.0, 90.0)
        _centerLongitude = wrapLongitudeDegrees(point.longitude)
        offsetX = 0f
        offsetY = 0f
        if (viewportSize.width <= 0f || viewportSize.height <= 0f) return
        pendingCenter = null
    }

    fun reset() {
        cancelZoomAnimation()
        zoom = 1.0f
        offsetX = 0f
        offsetY = 0f
        verticalScale = 1f
        centerLatitude = 0.0
        centerLongitude = 0.0
        rotationAngle = 0f
        pendingCenter = null
        doubleTapZoomedIn = false
        doubleTapBaselineZoom = 1.0f
    }

    /**
     * Animated double-tap: first tap zooms in toward the point, second returns
     * to the zoom level from before the zoom-in.
     */
    override suspend fun animateDoubleTapZoom(focalX: Float, focalY: Float) {
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

        val focalGeo = projection().toGeoPoint(Offset(focalX, focalY))

        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(durationMillis = DoubleTapZoomMillis),
        ) { t, _ ->
            zoom = startZoom + (endZoom - startZoom) * t
            keepGeoAtScreen(focalGeo, focalX, focalY)
            clampHorizontalOffset()
            clampVerticalOffset()
        }
        clampHorizontalOffset()
        clampVerticalOffset()
    }

    private fun applyZoomAt(focalX: Float, focalY: Float, factor: Float) {
        val width = viewportSize.width
        val height = viewportSize.height
        if (width <= 0f || height <= 0f || factor == 1f) return

        val oldZoom = zoom
        val newZoom = (oldZoom * factor).coerceIn(minZoom, maxZoom)
        if (newZoom == oldZoom) return

        val focalGeo = projection().toGeoPoint(Offset(focalX, focalY))
        zoom = newZoom
        keepGeoAtScreen(focalGeo, focalX, focalY)
        clampHorizontalOffset()
        clampVerticalOffset()
    }

    /** Adjusts pan offsets so [geo] projects to ([screenX], [screenY]). */
    private fun keepGeoAtScreen(geo: GeoPoint, screenX: Float, screenY: Float) {
        if (viewportSize.width <= 0f || viewportSize.height <= 0f || zoom == 0f) return
        val offsets = projection().panOffsetsFor(geo, screenX, screenY)
        offsetX = offsets.x
        offsetY = offsets.y
    }

    private fun cancelZoomAnimation() {
        zoomAnimationJob?.cancel()
        zoomAnimationJob = null
    }

    internal fun clampHorizontalOffset() {
        if (viewportSize.width <= 0f || viewportSize.height <= 0f) return
        val bounds = horizontalOffsetBounds(
            viewportWidth = viewportSize.width,
            viewportHeight = viewportSize.height,
            zoom = zoom,
            verticalScale = verticalScale,
            rotationAngle = rotationAngle,
        )
        offsetX = offsetX.coerceIn(bounds.start, bounds.endInclusive)
    }

    internal fun clampVerticalOffset() {
        if (viewportSize.width <= 0f || viewportSize.height <= 0f) return
        val bounds = verticalOffsetBounds(
            viewportWidth = viewportSize.width,
            viewportHeight = viewportSize.height,
            zoom = zoom,
            verticalScale = verticalScale,
            rotationAngle = rotationAngle,
        )
        offsetY = offsetY.coerceIn(bounds.start, bounds.endInclusive)
    }

    override fun geoAt(screen: Offset): GeoPoint = projection().toGeoPoint(screen)

    internal fun projection(): AzimuthalProjection =
        AzimuthalProjection(
            size = viewportSize,
            zoom = zoom,
            offsetX = offsetX,
            offsetY = offsetY,
            verticalScale = verticalScale,
            centerLatitude = centerLatitude,
            centerLongitude = centerLongitude,
            rotationAngle = rotationAngle,
        )

    private companion object {
        const val DoubleTapZoomInFactor = 2.0f
        const val DoubleTapZoomMillis = 280
    }
}

@Composable
fun rememberAzimuthalMapState(
    initialZoom: Float = 1.0f,
    initialOffsetX: Float = 0f,
    initialOffsetY: Float = 0f,
    initialVerticalScale: Float = 1f,
    initialCenterLatitude: Double = 0.0,
    initialCenterLongitude: Double = 0.0,
    initialRotationAngle: Float = 0f,
    minZoom: Float = 1.0f,
    maxZoom: Float = 12.0f,
): AzimuthalMapState {
    return remember {
        AzimuthalMapState(
            initialZoom = initialZoom,
            initialOffsetX = initialOffsetX,
            initialOffsetY = initialOffsetY,
            initialVerticalScale = initialVerticalScale,
            initialCenterLatitude = initialCenterLatitude,
            initialCenterLongitude = initialCenterLongitude,
            initialRotationAngle = initialRotationAngle,
            minZoom = minZoom,
            maxZoom = maxZoom,
        )
    }
}
