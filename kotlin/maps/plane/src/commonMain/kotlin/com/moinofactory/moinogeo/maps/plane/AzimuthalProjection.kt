package com.moinofactory.moinogeo.maps.plane

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import com.moinofactory.moinogeo.maps.core.GeoPoint
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin

/**
 * Azimuthal equidistant projection centered on
 * ([centerLatitude], [centerLongitude]).
 *
 * Angular distance from the origin is preserved, so the whole sphere maps into
 * a disk of radius π (the antipode). With zero pan, that origin sits at the
 * screen center. [rotationAngle] spins the disk around the screen center, and
 * [offsetX] / [offsetY] translate it in screen pixels divided by [zoom].
 *
 * [verticalScale] stretches the projected Y axis before rotation (1f keeps the
 * true equidistant disk). It is an aspect adjustment, not a second projection.
 *
 * The south-polar reduction (φ₀ = −90°) is
 * `c = π/2 + φ`, `x' = c sin(λ − λ₀)`, `y' = c cos(λ − λ₀)`:
 * parallels are concentric circles and meridians radiate from the screen center.
 */
@Immutable
internal data class AzimuthalProjection(
    val size: Size,
    val zoom: Float,
    val offsetX: Float,
    val offsetY: Float,
    val verticalScale: Float = 1f,
    val centerLatitude: Double = 0.0,
    val centerLongitude: Double = 0.0,
    val rotationAngle: Float = 0f,
) {
    val worldWidth: Float get() = size.width
    val worldHeight: Float get() = size.height

    private val yScale: Double
        get() = verticalScale.coerceAtLeast(MinVerticalScale).toDouble()

    private val phi0: Double
        get() = centerLatitude.coerceIn(-90.0, 90.0) * DEG_TO_RAD

    private val lambda0: Double
        get() = centerLongitude * DEG_TO_RAD

    /** Screen position of [point]. Longitude copies are the same sphere point. */
    fun toOffset(point: GeoPoint): Offset {
        if (size.width <= 0f || size.height <= 0f) return Offset.Zero
        val plane = forwardPlane(point.latitude, point.longitude)
        return screenOf(plane.x, plane.y)
    }

    /**
     * [tileX] is retained for call-site compatibility. An azimuthal disk already
     * contains every meridian, so every copy projects to the same screen point.
     */
    fun toOffsetOnTile(point: GeoPoint, tileX: Int): Offset {
        if (tileX == 0) return toOffset(point)
        return toOffset(point.copy(longitude = point.longitude + tileX * 360.0))
    }

    fun toGeoPoint(offset: Offset): GeoPoint {
        val width = size.width
        val height = size.height
        if (width <= 0f || height <= 0f || zoom == 0f) {
            return originPoint()
        }
        val r = radiusPerRadian()
        if (r == 0.0) return originPoint()

        val cx = width / 2.0
        val cy = height / 2.0
        val deltaX = (offset.x - cx - offsetX.toDouble() * zoom) / r
        val deltaY = -(offset.y - cy - offsetY.toDouble() * zoom) / r
        val gamma = rotationAngle.toDouble() * DEG_TO_RAD
        val cosG = cos(gamma)
        val sinG = sin(gamma)
        val xPrime = deltaX * cosG + deltaY * sinG
        val yScaled = -deltaX * sinG + deltaY * cosG
        return inversePlane(xPrime, yScaled / yScale)
    }

    /**
     * Pixel length of [meters] at [atLatitude] on the central meridian.
     * Radial scale is true; the tangential scale factor `c / sin c` is applied
     * so a small circle matches the local stretch of the disk.
     */
    fun metersToPixels(meters: Double, atLatitude: Double): Float {
        if (meters <= 0.0 || zoom <= 0f) return 0f
        val r = radiusPerRadian()
        if (r == 0.0) return 0f
        val c = angularDistance(GeoPoint(atLatitude.coerceIn(-90.0, 90.0), centerLongitude))
        val tangential = if (c < 1e-6) 1.0 else c / sin(c)
        return (meters / EarthRadiusMeters * r * tangential).toFloat()
    }

    /**
     * Bounding rect of the projected disk for the primary copy.
     * Other [tileX] values are empty: the sphere is drawn once.
     */
    fun clipRectForTile(tileX: Int, overlapPx: Float = 1.5f): Rect {
        if (tileX != 0 || size.width <= 0f || size.height <= 0f || zoom <= 0f) {
            return Rect.Zero
        }
        return diskRect(overlapPx)
    }

    /** Great-circle angular distance from the projection origin, in radians. */
    internal fun angularDistance(point: GeoPoint): Double {
        val phi = point.latitude.coerceIn(-90.0, 90.0) * DEG_TO_RAD
        val dLambda = wrapRadians((point.longitude - centerLongitude) * DEG_TO_RAD)
        if (phi0 <= -PI / 2.0 + 1e-10) {
            return (PI / 2.0 + phi).coerceIn(0.0, PI)
        }
        if (phi0 >= PI / 2.0 - 1e-10) {
            return (PI / 2.0 - phi).coerceIn(0.0, PI)
        }
        val cosC = sin(phi0) * sin(phi) + cos(phi0) * cos(phi) * cos(dLambda)
        return acos(cosC.coerceIn(-1.0, 1.0))
    }

    /** Pixels per radian of angular distance at the current zoom (`R`). */
    internal fun radiusPerRadian(): Double {
        val minSide = min(size.width, size.height).toDouble()
        if (minSide <= 0.0 || zoom <= 0f) return 0.0
        return minSide / PI * zoom.toDouble()
    }

    /** Horizontal pixel radius of the full disk (`R · π`). */
    internal fun diskRadiusX(): Float {
        if (zoom <= 0f) return 0f
        return min(size.width, size.height) * zoom
    }

    internal fun diskCenter(): Offset {
        val cx = size.width / 2f
        val cy = size.height / 2f
        return Offset(cx + offsetX * zoom, cy + offsetY * zoom)
    }

    /** Axis-aligned bounds of the rotated, Y-scaled disk. */
    internal fun diskRect(overlapPx: Float = 0f): Rect {
        val center = diskCenter()
        val gamma = rotationAngle.toDouble() * DEG_TO_RAD
        val rx = diskRadiusX().toDouble()
        val ry = rx * yScale
        val halfW = hypot(rx * cos(gamma), ry * sin(gamma)) + overlapPx
        val halfH = hypot(rx * sin(gamma), ry * cos(gamma)) + overlapPx
        return Rect(
            left = (center.x - halfW).toFloat(),
            top = (center.y - halfH).toFloat(),
            right = (center.x + halfW).toFloat(),
            bottom = (center.y + halfH).toFloat(),
        )
    }

    /** Clip of the visible sphere: the disk `c ≤ π`, after scale, rotation, and pan. */
    internal fun diskClipPath(): Path {
        val path = Path()
        if (diskRadiusX() <= 0f) return path
        val c = PI
        for (i in 0..DiskClipSteps) {
            val theta = 2.0 * PI * i / DiskClipSteps
            val p = mapToScreen(
                planeX = (c * sin(theta)).toFloat(),
                planeY = (c * cos(theta)).toFloat(),
            )
            if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
        }
        path.close()
        return path
    }

    /**
     * Pan offsets that project [point] onto ([screenX], [screenY]),
     * leaving the projection origin, zoom, and rotation unchanged.
     */
    internal fun panOffsetsFor(point: GeoPoint, screenX: Float, screenY: Float): Offset {
        if (size.width <= 0f || size.height <= 0f || zoom == 0f) {
            return Offset(offsetX, offsetY)
        }
        val plane = forwardPlane(point.latitude, point.longitude)
        val yScaled = plane.y * yScale
        val gamma = rotationAngle.toDouble() * DEG_TO_RAD
        val cosG = cos(gamma)
        val sinG = sin(gamma)
        val rotX = plane.x * cosG - yScaled * sinG
        val rotY = plane.x * sinG + yScaled * cosG
        val baseR = min(size.width, size.height).toDouble() / PI
        val cx = size.width / 2.0
        val cy = size.height / 2.0
        val ox = (screenX - cx) / zoom - baseR * rotX
        val oy = (screenY - cy) / zoom + baseR * rotY
        return Offset(ox.toFloat(), oy.toFloat())
    }

    /**
     * Applies scale, rotation, and pan to azimuthal plane coordinates
     * (`x'`, `y'` in radians, before [verticalScale]).
     */
    internal fun mapToScreen(planeX: Float, planeY: Float): Offset =
        screenOf(planeX.toDouble(), planeY.toDouble())

    private fun screenOf(planeX: Double, planeY: Double): Offset {
        val cx = size.width / 2.0
        val cy = size.height / 2.0
        val r = radiusPerRadian()
        val yScaled = planeY * yScale
        val gamma = rotationAngle.toDouble() * DEG_TO_RAD
        val cosG = cos(gamma)
        val sinG = sin(gamma)
        val rotX = planeX * cosG - yScaled * sinG
        val rotY = planeX * sinG + yScaled * cosG
        val x = cx + r * rotX + offsetX.toDouble() * zoom
        val y = cy - r * rotY + offsetY.toDouble() * zoom
        return Offset(x.toFloat(), y.toFloat())
    }

    private fun forwardPlane(latitudeDeg: Double, longitudeDeg: Double): PlanePoint {
        val phi = latitudeDeg.coerceIn(-90.0, 90.0) * DEG_TO_RAD
        val dLambda = wrapRadians(longitudeDeg * DEG_TO_RAD - lambda0)
        // Polar aspect. Derived from the oblique formula so the inverse matches:
        // south: y' = +c cos(Δλ); north: y' = −c cos(Δλ). Magnitude is `c`.
        if (phi0 <= -PI / 2.0 + 1e-10) {
            val c = (PI / 2.0 + phi).coerceIn(0.0, PI)
            return clampToDisk(c * sin(dLambda), c * cos(dLambda), c)
        }
        if (phi0 >= PI / 2.0 - 1e-10) {
            val c = (PI / 2.0 - phi).coerceIn(0.0, PI)
            return clampToDisk(c * sin(dLambda), -c * cos(dLambda), c)
        }

        val cosC = sin(phi0) * sin(phi) + cos(phi0) * cos(phi) * cos(dLambda)
        val c = acos(cosC.coerceIn(-1.0, 1.0))
        if (c < 1e-12) return PlanePoint(0.0, 0.0)
        if (c > PI - AntipodeEpsilonRadians) {
            val ux = cos(phi) * sin(dLambda)
            val uy = cos(phi0) * sin(phi) - sin(phi0) * cos(phi) * cos(dLambda)
            return clampToDisk(ux, uy, c)
        }
        val k = c / sin(c)
        val x = k * cos(phi) * sin(dLambda)
        val y = k * (cos(phi0) * sin(phi) - sin(phi0) * cos(phi) * cos(dLambda))
        return PlanePoint(x, y)
    }

    private fun inversePlane(xPrime: Double, yPrime: Double): GeoPoint {
        var x = xPrime
        var y = yPrime
        var rho = hypot(x, y)
        if (rho < 1e-12) return originPoint()
        val maxC = PI - AntipodeEpsilonRadians
        if (rho > maxC) {
            val s = maxC / rho
            x *= s
            y *= s
            rho = maxC
        }
        val c = rho
        val sinC = sin(c)
        val cosC = cos(c)
        val sinPhi0 = sin(phi0)
        val cosPhi0 = cos(phi0)
        val phi = asin(
            (cosC * sinPhi0 + (y * sinC * cosPhi0) / rho).coerceIn(-1.0, 1.0),
        )
        val lambda = lambda0 + atan2(
            x * sinC,
            rho * cosPhi0 * cosC - y * sinPhi0 * sinC,
        )
        return GeoPoint(
            latitude = (phi * RAD_TO_DEG).coerceIn(-90.0, 90.0),
            longitude = wrapLongitudeDegrees(lambda * RAD_TO_DEG),
        )
    }

    private fun clampToDisk(x: Double, y: Double, c: Double): PlanePoint {
        val maxC = PI - AntipodeEpsilonRadians
        if (c <= maxC) return PlanePoint(x, y)
        val mag = hypot(x, y)
        if (mag < 1e-9) return PlanePoint(maxC, 0.0)
        val s = maxC / mag
        return PlanePoint(x * s, y * s)
    }

    private fun originPoint(): GeoPoint = GeoPoint(
        latitude = centerLatitude.coerceIn(-90.0, 90.0),
        longitude = wrapLongitudeDegrees(centerLongitude),
    )

    private data class PlanePoint(val x: Double, val y: Double)

    companion object {
        val Empty = AzimuthalProjection(
            Size.Zero,
            zoom = 1f,
            offsetX = 0f,
            offsetY = 0f,
            verticalScale = 1f,
            centerLatitude = 0.0,
            centerLongitude = 0.0,
            rotationAngle = 0f,
        )

        /**
         * Single copy. The azimuthal disk already shows the whole sphere, so the
         * equirectangular ±1 world-wrap bands are not drawn.
         */
        val HorizontalWrapTiles: IntRange = 0..0

        const val MinVerticalScale = 0.1f

        /** Drop geometry this close to the antipode so strokes cannot cross the disk. */
        const val AntipodeEpsilonRadians = 0.02

        private const val DiskClipSteps = 96

        /** Mean Earth radius consistent with 111_320 m per degree at the equator. */
        private const val EarthRadiusMeters = 111_320.0 * 180.0 / PI

        private const val DEG_TO_RAD = PI / 180.0
        private const val RAD_TO_DEG = 180.0 / PI
    }
}

private fun wrapRadians(radians: Double): Double = atan2(sin(radians), cos(radians))

internal fun wrapLongitudeDegrees(longitude: Double): Double {
    var v = longitude % 360.0
    if (v > 180.0) v -= 360.0
    if (v < -180.0) v += 360.0
    return v
}

internal fun wrapDeltaDegrees(delta: Double): Double {
    var d = delta % 360.0
    if (d > 180.0) d -= 360.0
    if (d <= -180.0) d += 360.0
    return d
}

/**
 * Allowed [AzimuthalMapState.offsetY] so the disk's vertical extent still covers
 * the viewport. [rotationAngle] is degrees.
 */
internal fun verticalOffsetBounds(
    viewportWidth: Float,
    viewportHeight: Float,
    zoom: Float,
    verticalScale: Float,
    rotationAngle: Float = 0f,
): ClosedFloatingPointRange<Float> {
    val extent = diskHalfExtent(
        viewportWidth = viewportWidth,
        viewportHeight = viewportHeight,
        zoom = zoom,
        verticalScale = verticalScale,
        rotationAngle = rotationAngle,
        vertical = true,
    ) ?: return 0f..0f
    val limit = (extent - viewportHeight / 2f) / zoom
    if (limit <= 0f) return 0f..0f
    return -limit..limit
}

internal fun horizontalOffsetBounds(
    viewportWidth: Float,
    viewportHeight: Float,
    zoom: Float,
    verticalScale: Float,
    rotationAngle: Float = 0f,
): ClosedFloatingPointRange<Float> {
    val extent = diskHalfExtent(
        viewportWidth = viewportWidth,
        viewportHeight = viewportHeight,
        zoom = zoom,
        verticalScale = verticalScale,
        rotationAngle = rotationAngle,
        vertical = false,
    ) ?: return 0f..0f
    val limit = (extent - viewportWidth / 2f) / zoom
    if (limit <= 0f) return 0f..0f
    return -limit..limit
}

internal fun maxVerticalOffset(
    viewportWidth: Float,
    viewportHeight: Float,
    zoom: Float,
    verticalScale: Float,
    rotationAngle: Float = 0f,
): Float {
    val bounds = verticalOffsetBounds(
        viewportWidth,
        viewportHeight,
        zoom,
        verticalScale,
        rotationAngle,
    )
    return maxOf(0f, -bounds.start, bounds.endInclusive)
}

private fun diskHalfExtent(
    viewportWidth: Float,
    viewportHeight: Float,
    zoom: Float,
    verticalScale: Float,
    rotationAngle: Float,
    vertical: Boolean,
): Float? {
    if (viewportWidth <= 0f || viewportHeight <= 0f || zoom <= 0f) return null
    val rx = min(viewportWidth, viewportHeight) * zoom
    val ry = rx * verticalScale.coerceAtLeast(AzimuthalProjection.MinVerticalScale)
    val gamma = rotationAngle.toDouble() * PI / 180.0
    val extent = if (vertical) {
        hypot(rx.toDouble() * sin(gamma), ry.toDouble() * cos(gamma))
    } else {
        hypot(rx.toDouble() * cos(gamma), ry.toDouble() * sin(gamma))
    }
    return extent.toFloat()
}
