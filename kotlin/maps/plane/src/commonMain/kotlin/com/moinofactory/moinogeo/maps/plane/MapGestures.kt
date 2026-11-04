package com.moinofactory.moinogeo.maps.plane

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import com.moinofactory.moinogeo.maps.core.GeoPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private const val WheelZoomFactor = 1.12f

/**
 * Pan (drag / pinch-pan), pinch-zoom, mouse-wheel zoom, animated double-tap
 * zoom toggle, and optional single-tap [onMapClick] — all focused on the
 * pointer / gesture centroid.
 */
internal fun Modifier.mapGestures(
    state: PlaneMapState,
    scope: CoroutineScope,
    onMapClick: ((GeoPoint) -> Unit)? = null,
): Modifier =
    this
        .pointerInput(state) {
            detectTransformGestures { centroid, pan, zoomFactor, rotation ->
                if (zoomFactor != 1f) {
                    state.zoomAt(centroid.x, centroid.y, zoomFactor)
                }
                if (rotation != 0f) {
                    state.rotationAngle += rotation
                }
                if (pan != Offset.Zero) {
                    state.panBy(pan.x, pan.y)
                }
            }
        }
        .pointerInput(state, scope, onMapClick) {
            detectTapGestures(
                onTap = onMapClick?.let { callback ->
                    { pos ->
                        callback(state.projection().toGeoPoint(pos))
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
