package com.moinofactory.moinogeo.maps.sphere

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import com.moinofactory.moinogeo.maps.core.GeoPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private const val WheelZoomFactor = 1.12f

/**
 * Orbit (drag), pinch/wheel zoom, double-tap zoom, and optional tap → geo callback.
 */
internal fun Modifier.sphereGestures(
    state: SphereMapState,
    scope: CoroutineScope,
    onMapClick: ((GeoPoint) -> Unit)? = null,
): Modifier =
    this
        .pointerInput(state) {
            detectTransformGestures { centroid, pan, zoomFactor, _ ->
                if (zoomFactor != 1f) {
                    state.zoomAt(centroid.x, centroid.y, zoomFactor)
                }
                if (pan.x != 0f || pan.y != 0f) {
                    state.orbitBy(pan.x, pan.y)
                }
            }
        }
        .pointerInput(state, scope, onMapClick) {
            detectTapGestures(
                onTap = onMapClick?.let { callback ->
                    { pos ->
                        state.geoAt(pos)?.let(callback)
                    }
                },
                onDoubleTap = { pos ->
                    scope.launch {
                        state.animateDoubleTapZoom(pos.x, pos.y)
                    }
                },
            )
        }
        .pointerInput(state) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Main)
                    if (event.type != PointerEventType.Scroll) continue
                    val change = event.changes.firstOrNull() ?: continue
                    val scrollY = change.scrollDelta.y
                    if (scrollY == 0f) continue
                    val factor = if (scrollY < 0f) WheelZoomFactor else 1f / WheelZoomFactor
                    state.zoomAt(change.position.x, change.position.y, factor)
                    change.consume()
                }
            }
        }
