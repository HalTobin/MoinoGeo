package com.moinofactory.moinogeo.maps.plane

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animate
import com.moinofactory.moinogeo.maps.core.GeoPoint
import com.moinofactory.moinogeo.maps.core.isNear
import com.moinofactory.moinogeo.maps.core.lerpToward
import kotlin.math.abs

/**
 * Runs a single-progress camera transition from ([startCenter], [startZoom]) to
 * ([endCenter], [endZoom]), invoking [apply] on every frame.
 */
internal suspend fun animateCameraTransition(
    startCenter: GeoPoint,
    endCenter: GeoPoint,
    startZoom: Float,
    endZoom: Float,
    animationSpec: AnimationSpec<Float>,
    apply: (center: GeoPoint, zoom: Float) -> Unit,
) {
    val zoomUnchanged = abs(endZoom - startZoom) < 1e-4f
    val centerUnchanged = startCenter.isNear(endCenter)
    if (zoomUnchanged && centerUnchanged) {
        apply(endCenter, endZoom)
        return
    }

    animate(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = animationSpec,
    ) { t, _ ->
        val center = if (centerUnchanged) {
            endCenter
        } else {
            startCenter.lerpToward(endCenter, t)
        }
        val zoom = if (zoomUnchanged) {
            endZoom
        } else {
            startZoom + (endZoom - startZoom) * t
        }
        apply(center, zoom)
    }
    apply(endCenter, endZoom)
}
