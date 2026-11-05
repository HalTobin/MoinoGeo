package com.moinofactory.moinogeo.maps.sphere

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

/**
 * Camera for [SphereMap]. [zoom] maps to camera distance (higher zoom = closer).
 * Gestures orbit the globe; [cameraCenter] is the geographic point under the view center.
 */
class SphereMapState(
    initialCenter: GeoPoint = GeoPoint(0.0, 0.0),
    initialZoom: Float = 1f,
    initialFieldOfView: Float = 45f,
    val minZoom: Float = 0.5f,
    val maxZoom: Float = 8f,
    val minDistance: Float = 1.35f,
    val maxDistance: Float = 6f,
    val globeRadius: Float = 1f,
) : MapCameraController {
    private var _center by mutableStateOf(initialCenter)
    var center: GeoPoint
        get() = _center
        set(value) {
            _center = value.copy(
                latitude = value.latitude.coerceIn(-MaxLat, MaxLat),
                longitude = value.longitude,
            )
        }

    private var _zoom by mutableFloatStateOf(initialZoom.coerceIn(minZoom, maxZoom))
    override var zoom: Float
        get() = _zoom
        set(value) {
            _zoom = value.coerceIn(minZoom, maxZoom)
        }

    var fieldOfView by mutableFloatStateOf(initialFieldOfView.coerceIn(20f, 90f))

    private var _viewportSize by mutableStateOf(Size.Zero)
    internal var viewportSize: Size
        get() = _viewportSize
        set(value) {
            _viewportSize = value
        }

    /** Camera distance from globe origin (world units). */
    val distance: Float
        get() = zoomToDistance(zoom)

    override val cameraCenter: GeoPoint
        get() = center

    private var cameraAnimationJob: Job? = null
    private var doubleTapZoomedIn: Boolean = false
    private var doubleTapBaselineZoom: Float = initialZoom.coerceIn(minZoom, maxZoom)

    fun projection(): SphereProjection =
        SphereProjection(
            size = viewportSize,
            center = center,
            distance = distance,
            fieldOfViewDegrees = fieldOfView,
            globeRadius = globeRadius,
        )

    fun eyePosition(): Float3 = SphereProjection.cameraEye(center, distance)

    /** Orbit by screen-pixel deltas (drag). */
    fun orbitBy(deltaX: Float, deltaY: Float) {
        cancelCameraAnimation()
        val sensitivity = 0.25f * (distance / maxDistance).coerceIn(0.35f, 1.2f)
        // Drag right → content follows finger (look-at longitude increases).
        val dLon = deltaX * sensitivity
        val dLat = deltaY * sensitivity
        center = GeoPoint(
            latitude = (center.latitude + dLat).coerceIn(-MaxLat, MaxLat),
            longitude = center.longitude + dLon,
        )
        doubleTapZoomedIn = false
    }

    fun zoomAt(focalX: Float, focalY: Float, factor: Float) {
        cancelCameraAnimation()
        val before = projection()
        val anchor = before.toGeoPoint(Offset(focalX, focalY))
        zoom = (zoom * factor).coerceIn(minZoom, maxZoom)
        if (anchor != null) {
            // Keep the geographic point under the focal roughly stable by re-centering slightly.
            val after = projection()
            val now = after.toGeoPoint(Offset(focalX, focalY)) ?: return
            val dLat = anchor.latitude - now.latitude
            val dLon = anchor.longitude - now.longitude
            center = GeoPoint(
                latitude = (center.latitude + dLat).coerceIn(-MaxLat, MaxLat),
                longitude = center.longitude + dLon,
            )
        }
        doubleTapZoomedIn = false
    }

    fun geoAt(screen: Offset): GeoPoint? = projection().toGeoPoint(screen)

    suspend fun animateDoubleTapZoom(focalX: Float, focalY: Float) {
        val targetZoom = if (doubleTapZoomedIn) {
            doubleTapBaselineZoom
        } else {
            doubleTapBaselineZoom = zoom
            (zoom * DoubleTapZoomInFactor).coerceIn(minZoom, maxZoom)
        }
        doubleTapZoomedIn = !doubleTapZoomedIn
        val startZoom = zoom
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(DoubleTapZoomMillis),
        ) { t, _ ->
            val previous = zoom
            val next = startZoom + (targetZoom - startZoom) * t
            if (previous > 1e-4f) {
                zoomAt(focalX, focalY, next / previous)
            } else {
                zoom = next
            }
        }
        zoom = targetZoom
    }

    override fun moveTo(point: GeoPoint) = moveTo(point, zoom)

    override fun moveTo(point: GeoPoint, zoom: Float) {
        cancelCameraAnimation()
        applyCamera(point, zoom)
    }

    override suspend fun animateTo(point: GeoPoint) {
        animateTo(point, zoom, DefaultCameraAnimationSpec)
    }

    override suspend fun animateTo(point: GeoPoint, animationSpec: AnimationSpec<Float>) {
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
                val startZoom = this@SphereMapState.zoom
                val endZoom = zoom.coerceIn(minZoom, maxZoom)
                animateCameraTransition(
                    startCenter = startCenter,
                    endCenter = point,
                    startZoom = startZoom,
                    endZoom = endZoom,
                    animationSpec = animationSpec,
                ) { c, z -> applyCamera(c, z) }
            }
            cameraAnimationJob = job
            job.join()
        }
    }

    private fun applyCamera(point: GeoPoint, targetZoom: Float) {
        zoom = targetZoom.coerceIn(minZoom, maxZoom)
        center = point
        doubleTapZoomedIn = false
    }

    private fun cancelCameraAnimation() {
        cameraAnimationJob?.cancel()
        cameraAnimationJob = null
    }

    private fun zoomToDistance(z: Float): Float {
        val t = ((z - minZoom) / (maxZoom - minZoom)).coerceIn(0f, 1f)
        // Zoom in → closer (smaller distance).
        return maxDistance + (minDistance - maxDistance) * t
    }

    private companion object {
        const val MaxLat = 89.5
        const val DoubleTapZoomInFactor = 2f
        const val DoubleTapZoomMillis = 280
    }
}

@Composable
fun rememberSphereMapState(
    initialCenter: GeoPoint = GeoPoint(0.0, 0.0),
    initialZoom: Float = 1f,
    initialFieldOfView: Float = 45f,
    minZoom: Float = 0.5f,
    maxZoom: Float = 8f,
): SphereMapState {
    return remember {
        SphereMapState(
            initialCenter = initialCenter,
            initialZoom = initialZoom,
            initialFieldOfView = initialFieldOfView,
            minZoom = minZoom,
            maxZoom = maxZoom,
        )
    }
}

// Local animate import for double-tap without pulling the full CameraAnimation API into callers.
private suspend fun animate(
    initialValue: Float,
    targetValue: Float,
    animationSpec: AnimationSpec<Float>,
    block: (value: Float, velocity: Float) -> Unit,
) {
    androidx.compose.animation.core.animate(
        initialValue = initialValue,
        targetValue = targetValue,
        animationSpec = animationSpec,
        block = block,
    )
}
