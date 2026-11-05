package com.moinofactory.moinogeo.maps.core

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween

/**
 * Programmatic camera control shared by plane, azimuthal, and sphere maps.
 *
 * Instant helpers snap immediately; [animateTo] variants interpolate center
 * (and optionally zoom) with any Compose [AnimationSpec] — e.g.
 * `tween(500, easing = LinearEasing)` or `tween(800, easing = FastOutSlowInEasing)`.
 */
interface MapCameraController {
    /** Geographic point currently under the viewport center. */
    val cameraCenter: GeoPoint

    /** Current map zoom. */
    val zoom: Float

    /** Instantly centers the camera on [point], keeping the current zoom. */
    fun moveTo(point: GeoPoint)

    /** Instantly centers the camera on [point] at [zoom]. */
    fun moveTo(point: GeoPoint, zoom: Float)

    /**
     * Animates the camera so [point] sits under the viewport center.
     * Zoom is held at its current value. Uses [DefaultCameraAnimationSpec].
     */
    suspend fun animateTo(point: GeoPoint)

    /**
     * Animates the camera so [point] sits under the viewport center.
     * Zoom is held at its current value.
     */
    suspend fun animateTo(point: GeoPoint, animationSpec: AnimationSpec<Float>)

    /**
     * Animates the camera to center on [point] at [zoom].
     * Uses [DefaultCameraAnimationSpec].
     */
    suspend fun animateTo(point: GeoPoint, zoom: Float)

    /** Animates the camera to center on [point] at [zoom]. */
    suspend fun animateTo(
        point: GeoPoint,
        zoom: Float,
        animationSpec: AnimationSpec<Float>,
    )
}

/** Default ease-in-out camera transition (~350ms). */
val DefaultCameraAnimationSpec: AnimationSpec<Float> = tween(
    durationMillis = 350,
    easing = FastOutSlowInEasing,
)
