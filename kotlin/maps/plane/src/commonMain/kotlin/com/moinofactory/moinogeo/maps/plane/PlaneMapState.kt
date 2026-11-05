package com.moinofactory.moinogeo.maps.plane

import androidx.compose.animation.core.AnimationSpec
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
import com.moinofactory.moinogeo.maps.core.DefaultCameraAnimationSpec
import com.moinofactory.moinogeo.maps.core.GeoPoint
import com.moinofactory.moinogeo.maps.core.MapCameraController
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class PlaneMapState(
    initialZoom: Float = 1.0f,
    initialOffsetX: Float = 0f,
    initialOffsetY: Float = 0f,
    initialVerticalScale: Float = 1f,
    initialVerticalWarp: Float = 0f,
    initialCenterLatitude: Double = 0.0,
    initialRotationAngle: Float = 0f,
    // 1f = full world width visible; below that we'd need overlapping tiles (EvenOdd artifacts).
    val minZoom: Float = 1.0f,
    val maxZoom: Float = 12.0f,
) : MapCamera, MapCameraController {
    override var zoom by mutableFloatStateOf(initialZoom.coerceIn(minZoom, maxZoom))
    var offsetX by mutableFloatStateOf(initialOffsetX)
    var offsetY by mutableFloatStateOf(initialOffsetY)
    override var rotationAngle by mutableFloatStateOf(initialRotationAngle)

    private var _verticalScale by mutableFloatStateOf(
        initialVerticalScale.coerceAtLeast(MapProjection.MinVerticalScale),
    )

    /**
     * Independent Y-axis aspect-ratio stretch (1f = equirectangular). Expansion
     * pivots around [centerLatitude]. This does not change spherical distortion.
     */
    var verticalScale: Float
        get() = _verticalScale
        set(value) {
            _verticalScale = value.coerceAtLeast(MapProjection.MinVerticalScale)
            clampVerticalOffset()
        }

    private var _verticalWarp by mutableFloatStateOf(
        initialVerticalWarp.coerceIn(
            MapProjection.MinVerticalWarp,
            MapProjection.MaxVerticalWarp,
        ),
    )

    /**
     * Non-linear vertical falloff from [centerLatitude]. `0` is linear;
     * positive values stretch exponentially toward the poles.
     */
    var verticalWarp: Float
        get() = _verticalWarp
        set(value) {
            _verticalWarp = value.coerceIn(
                MapProjection.MinVerticalWarp,
                MapProjection.MaxVerticalWarp,
            )
            clampVerticalOffset()
        }

    private var _centerLatitude by mutableDoubleStateOf(
        initialCenterLatitude.coerceIn(-90.0, 90.0),
    )

    /**
     * Geographic latitude that [verticalScale] expands from (0 = equator).
     * Camera position is controlled separately via [setCenter] / [offsetX]/[offsetY].
     */
    var centerLatitude: Double
        get() = _centerLatitude
        set(value) {
            _centerLatitude = value.coerceIn(-90.0, 90.0)
            clampVerticalOffset()
        }

    private var _viewportSize by mutableStateOf(Size.Zero)

    internal var viewportSize: Size
        get() = _viewportSize
        set(value) {
            _viewportSize = value
            normalizeHorizontalOffset()
            clampVerticalOffset()
            pendingCenter?.let { applyCameraCenter(it) }
        }

    /** Geographic point currently under the viewport center (camera). */
    override val cameraCenter: GeoPoint
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
    private var cameraAnimationJob: Job? = null

    fun zoomIn(factor: Float = 1.25f) {
        cancelCameraAnimation()
        zoom = (zoom * factor).coerceIn(minZoom, maxZoom)
        clampVerticalOffset()
        doubleTapZoomedIn = false
    }

    fun zoomOut(factor: Float = 1.25f) {
        cancelCameraAnimation()
        zoom = (zoom / factor).coerceIn(minZoom, maxZoom)
        clampVerticalOffset()
        doubleTapZoomedIn = false
    }

    /**
     * Zoom by [factor] keeping the geographic point under ([focalX], [focalY])
     * (viewport pixels) fixed on screen.
     */
    override fun zoomAt(focalX: Float, focalY: Float, factor: Float) {
        cancelCameraAnimation()
        applyZoomAt(focalX, focalY, factor)
        doubleTapZoomedIn = false
    }

    /** Pan in screen pixels (converted to map space). */
    override fun panBy(deltaX: Float, deltaY: Float) {
        cancelCameraAnimation()
        if (zoom == 0f) return
        // offsetY is scaled by [zoom] in the projection (pivot frame), not zoomY.
        offsetX += deltaX / zoom
        offsetY += deltaY / zoom
        normalizeHorizontalOffset()
        clampVerticalOffset()
    }

    /**
     * Pans the camera so [point] sits under the viewport center via [offsetX] /
     * [offsetY]. Does **not** rematerialize polygons around a new central meridian
     * (that would break Natural Earth seams). If the viewport size is not known
     * yet, the camera center is applied as soon as it becomes available.
     */
    override fun moveTo(point: GeoPoint) {
        moveTo(point, zoom)
    }

    override fun moveTo(point: GeoPoint, zoom: Float) {
        cancelCameraAnimation()
        applyCamera(point, zoom)
    }

    /** @see moveTo */
    fun setCenter(point: GeoPoint) = moveTo(point)

    override suspend fun animateTo(point: GeoPoint) {
        animateTo(point, zoom, DefaultCameraAnimationSpec)
    }

    override suspend fun animateTo(
        point: GeoPoint,
        animationSpec: AnimationSpec<Float>,
    ) {
        animateTo(point, zoom, animationSpec)
    }

    override suspend fun animateTo(point: GeoPoint, zoom: Float) {
        animateTo(point, zoom, DefaultCameraAnimationSpec)
    }

    override suspend fun animateTo(
        point: GeoPoint,
        zoom: Float,
        animationSpec: AnimationSpec<Float>,
    ) {
        coroutineScope {
            cameraAnimationJob?.cancel()
            val job = launch {
                val startCenter = cameraCenter
                val startZoom = this@PlaneMapState.zoom
                val endZoom = zoom.coerceIn(minZoom, maxZoom)
                animateCameraTransition(
                    startCenter = startCenter,
                    endCenter = point,
                    startZoom = startZoom,
                    endZoom = endZoom,
                    animationSpec = animationSpec,
                ) { center, z -> applyCamera(center, z) }
            }
            cameraAnimationJob = job
            job.join()
        }
    }

    private fun applyCamera(point: GeoPoint, targetZoom: Float) {
        this.zoom = targetZoom.coerceIn(minZoom, maxZoom)
        pendingCenter = point
        applyCameraCenter(point)
        doubleTapZoomedIn = false
    }

    private fun applyCameraCenter(point: GeoPoint) {
        val width = viewportSize.width
        val height = viewportSize.height
        if (width <= 0f || height <= 0f) return

        val cx = width / 2f
        val mapX = MapProjection.longitudeToMapX(point.longitude, width)
        val mapY = MapProjection.latitudeToMapY(point.latitude, height)
        val pivot = MapProjection.latitudeToMapY(centerLatitude, height)
        val scale = verticalScale.coerceAtLeast(MapProjection.MinVerticalScale) *
            MapProjection.geographicYFactor(width, height)
        val warpRef = maxOf(pivot, height - pivot).coerceAtLeast(1f)
        val warpedDelta = MapProjection.warpVerticalDelta(
            delta = mapY - pivot,
            warp = verticalWarp,
            reference = warpRef,
        )

        offsetX = cx - mapX
        var desiredOffsetY = -warpedDelta * scale
        ensureVerticalRoomForOffset(desiredOffsetY)
        desiredOffsetY = -warpedDelta * scale
        offsetY = desiredOffsetY
        normalizeHorizontalOffset()
        clampVerticalOffset()
        pendingCenter = null
    }

    /**
     * Bumps [zoom] so [desiredOffsetY] stays inside the vertical canvas clamp.
     */
    private fun ensureVerticalRoomForOffset(desiredOffsetY: Float) {
        val width = viewportSize.width
        val height = viewportSize.height
        if (width <= 0f || height <= 0f) return
        // Grow zoom until the desired offset fits or maxZoom is hit.
        var guard = 0
        while (guard++ < 24) {
            val bounds = verticalOffsetBounds(
                viewportWidth = width,
                viewportHeight = height,
                zoom = zoom,
                verticalScale = verticalScale,
                centerLatitude = centerLatitude,
            )
            if (desiredOffsetY in bounds) return
            if (zoom >= maxZoom - 1e-4f) return
            zoom = (zoom * 1.15f).coerceAtMost(maxZoom)
        }
    }

    fun reset() {
        cancelCameraAnimation()
        zoom = 1.0f
        offsetX = 0f
        offsetY = 0f
        verticalScale = 1f
        verticalWarp = 0f
        centerLatitude = 0.0
        rotationAngle = 0f
        doubleTapZoomedIn = false
        doubleTapBaselineZoom = 1.0f
    }

    /**
     * Animated double-tap: first tap zooms in toward the point, second returns
     * to the zoom level from before the zoom-in.
     */
    override suspend fun animateDoubleTapZoom(focalX: Float, focalY: Float) {
        coroutineScope {
            cameraAnimationJob?.cancel()
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
            cameraAnimationJob = job
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

        val focalGeo = projection().toGeoPoint(Offset(focalX, focalY))
        zoom = newZoom
        keepGeoAtScreen(focalGeo, focalX, focalY)
        normalizeHorizontalOffset()
        clampVerticalOffset()
    }

    /** Adjusts pan offsets so [geo] projects to ([screenX], [screenY]). */
    private fun keepGeoAtScreen(geo: GeoPoint, screenX: Float, screenY: Float) {
        val width = viewportSize.width
        val height = viewportSize.height
        if (width <= 0f || height <= 0f || zoom == 0f) return

        val cx = width / 2f
        val cy = height / 2f
        val mapX = MapProjection.longitudeToMapX(geo.longitude, width)
        val mapY = MapProjection.latitudeToMapY(geo.latitude, height)
        val pivot = MapProjection.latitudeToMapY(centerLatitude, height)
        val scale = verticalScale.coerceAtLeast(MapProjection.MinVerticalScale) *
            MapProjection.geographicYFactor(width, height)
        val warpRef = maxOf(pivot, height - pivot).coerceAtLeast(1f)
        val warpedDelta = MapProjection.warpVerticalDelta(
            delta = mapY - pivot,
            warp = verticalWarp,
            reference = warpRef,
        )

        var sX = screenX
        var sY = screenY
        if (rotationAngle != 0f) {
            val rad = rotationAngle * (PI / 180.0).toFloat()
            val cosR = cos(rad)
            val sinR = sin(rad)
            val dx = screenX - cx
            val dy = screenY - cy
            sX = cx + dx * cosR + dy * sinR
            sY = cy - dx * sinR + dy * cosR
        }

        offsetX = (sX - cx) / zoom - mapX + cx
        offsetY = (sY - cy - warpedDelta * zoom * scale) / zoom
    }

    private fun cancelCameraAnimation() {
        cameraAnimationJob?.cancel()
        cameraAnimationJob = null
    }

    internal fun normalizeHorizontalOffset() {
        val period = viewportSize.width
        if (period > 0f) {
            offsetX = wrapOffsetCentered(offsetX, period)
        }
    }

    internal fun clampVerticalOffset() {
        val bounds = verticalOffsetBounds(
            viewportWidth = viewportSize.width,
            viewportHeight = viewportSize.height,
            zoom = zoom,
            verticalScale = verticalScale,
            centerLatitude = centerLatitude,
        )
        offsetY = offsetY.coerceIn(bounds.start, bounds.endInclusive)
    }

    override fun geoAt(screen: Offset): GeoPoint = projection().toGeoPoint(screen)

    internal fun projection(): MapProjection =
        MapProjection(
            size = viewportSize,
            zoom = zoom,
            offsetX = offsetX,
            offsetY = offsetY,
            verticalScale = verticalScale,
            verticalWarp = verticalWarp,
            centerLatitude = centerLatitude,
            rotationAngle = rotationAngle,
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
    initialVerticalWarp: Float = 0f,
    initialCenterLatitude: Double = 0.0,
    initialRotationAngle: Float = 0f,
    minZoom: Float = 1.0f,
    maxZoom: Float = 12.0f,
): PlaneMapState {
    return remember {
        PlaneMapState(
            initialZoom = initialZoom,
            initialOffsetX = initialOffsetX,
            initialOffsetY = initialOffsetY,
            initialVerticalScale = initialVerticalScale,
            initialVerticalWarp = initialVerticalWarp,
            initialCenterLatitude = initialCenterLatitude,
            initialRotationAngle = initialRotationAngle,
            minZoom = minZoom,
            maxZoom = maxZoom,
        )
    }
}
