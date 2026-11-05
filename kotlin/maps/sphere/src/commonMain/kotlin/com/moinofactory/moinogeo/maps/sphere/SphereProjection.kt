package com.moinofactory.moinogeo.maps.sphere

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.moinofactory.moinogeo.maps.core.GeoPoint
import com.moinofactory.moinogeo.maps.core.wrapLongitudeDegrees
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * Perspective projection of a unit-ish globe centered at the origin.
 * Camera looks at the origin from a point derived from [center] + [distance].
 */
class SphereProjection(
    val size: Size,
    val center: GeoPoint,
    val distance: Float,
    val fieldOfViewDegrees: Float = 45f,
    val globeRadius: Float = 1f,
) {
    private val eye: Float3 = cameraEye(center, distance)
    private val fovYRad = fieldOfViewDegrees * DegToRad
    private val aspect = if (size.height > 0f) size.width / size.height else 1f

    fun toCartesian(point: GeoPoint, radius: Float = globeRadius): Float3 {
        val lat = point.latitude * DegToRad
        val lon = point.longitude * DegToRad
        val cosLat = cos(lat)
        return Float3(
            x = (radius * cosLat * cos(lon)).toFloat(),
            y = (radius * sin(lat)).toFloat(),
            z = (radius * cosLat * sin(lon)).toFloat(),
        )
    }

    fun toGeoPoint(position: Float3): GeoPoint {
        val r = max(1e-6f, length(position))
        val lat = asin((position.y / r).toDouble()).coerceIn(-PI / 2, PI / 2)
        val lon = atan2(position.z.toDouble(), position.x.toDouble())
        return GeoPoint(
            latitude = lat * RadToDeg,
            longitude = wrapLongitudeDegrees(lon * RadToDeg),
        )
    }

    /** Screen position of [point], or null when behind the camera / off-sphere backface. */
    fun toOffset(point: GeoPoint, frontFaceOnly: Boolean = true): Offset? {
        if (size.width <= 0f || size.height <= 0f) return null
        val world = toCartesian(point)
        if (frontFaceOnly && !isFrontFacing(world)) return null
        return project(world)
    }

    fun isFrontFacing(point: GeoPoint): Boolean = isFrontFacing(toCartesian(point))

    fun isFrontFacing(world: Float3): Boolean {
        val toEye = normalize(Float3(eye.x - world.x, eye.y - world.y, eye.z - world.z))
        val normal = normalize(world)
        return dot(normal, toEye) > 0.05f
    }

    /**
     * Ray–sphere intersection for a viewport pixel. Returns the nearer front hit as geo,
     * or null when the ray misses the globe.
     */
    fun toGeoPoint(offset: Offset): GeoPoint? {
        if (size.width <= 0f || size.height <= 0f) return null
        val ndcX = (2f * offset.x / size.width) - 1f
        val ndcY = 1f - (2f * offset.y / size.height)
        val tanHalf = tan(fovYRad * 0.5).toFloat()
        val dirCam = normalize(
            Float3(
                x = ndcX * tanHalf * aspect,
                y = ndcY * tanHalf,
                // +Z matches [project]'s positive look-direction depth.
                z = 1f,
            ),
        )
        // Camera looks toward origin; build a simple look-at basis.
        val forward = normalize(Float3(-eye.x, -eye.y, -eye.z))
        val worldUp = Float3(0f, 1f, 0f)
        val right = normalize(cross(forward, worldUp))
        val up = cross(right, forward)
        val dir = normalize(
            Float3(
                x = right.x * dirCam.x + up.x * dirCam.y + forward.x * dirCam.z,
                y = right.y * dirCam.x + up.y * dirCam.y + forward.y * dirCam.z,
                z = right.z * dirCam.x + up.z * dirCam.y + forward.z * dirCam.z,
            ),
        )
        val hit = raySphere(eye, dir, globeRadius) ?: return null
        return toGeoPoint(hit)
    }

    fun metersToPixels(meters: Double, atLatitude: Double): Float {
        if (size.height <= 0f || distance <= 0f) return 0f
        val angular = meters / EarthRadiusMeters
        val worldArc = (angular * globeRadius).toFloat()
        val projected = project(Float3(0f, worldArc, -distance + globeRadius))
            ?: return 0f
        val centerPx = Offset(size.width / 2f, size.height / 2f)
        return (projected - centerPx).getDistance().coerceAtLeast(0.5f) *
            cos(atLatitude * DegToRad).toFloat().coerceAtLeast(0.2f)
    }

    private fun project(world: Float3): Offset? {
        val forward = normalize(Float3(-eye.x, -eye.y, -eye.z))
        val worldUp = Float3(0f, 1f, 0f)
        val right = normalize(cross(forward, worldUp))
        val up = cross(right, forward)
        val rel = Float3(world.x - eye.x, world.y - eye.y, world.z - eye.z)
        // Positive depth along the look direction (eye → target).
        val depth = dot(rel, forward)
        if (depth <= 1e-4f) return null
        val camX = dot(rel, right)
        val camY = dot(rel, up)
        val tanHalf = tan(fovYRad * 0.5).toFloat()
        val ndcX = camX / (depth * tanHalf * aspect)
        val ndcY = camY / (depth * tanHalf)
        return Offset(
            x = (ndcX + 1f) * 0.5f * size.width,
            y = (1f - ndcY) * 0.5f * size.height,
        )
    }

    companion object {
        private const val DegToRad = (PI / 180.0).toFloat()
        private const val RadToDeg = 180.0 / PI
        private const val EarthRadiusMeters = 6_371_000.0

        fun cameraEye(center: GeoPoint, distance: Float): Float3 {
            val lat = center.latitude * DegToRad
            val lon = center.longitude * DegToRad
            val cosLat = cos(lat)
            return Float3(
                x = (distance * cosLat * cos(lon)).toFloat(),
                y = (distance * sin(lat)).toFloat(),
                z = (distance * cosLat * sin(lon)).toFloat(),
            )
        }

        private fun raySphere(origin: Float3, dir: Float3, radius: Float): Float3? {
            val b = 2f * dot(origin, dir)
            val c = dot(origin, origin) - radius * radius
            val disc = b * b - 4f * c
            if (disc < 0f) return null
            val sqrtDisc = sqrt(disc)
            val t0 = (-b - sqrtDisc) * 0.5f
            val t1 = (-b + sqrtDisc) * 0.5f
            val t = when {
                t0 > 1e-4f -> t0
                t1 > 1e-4f -> t1
                else -> return null
            }
            return Float3(origin.x + dir.x * t, origin.y + dir.y * t, origin.z + dir.z * t)
        }
    }
}

data class Float3(val x: Float, val y: Float, val z: Float)

internal fun length(v: Float3): Float = sqrt(v.x * v.x + v.y * v.y + v.z * v.z)

internal fun normalize(v: Float3): Float3 {
    val len = length(v).coerceAtLeast(1e-8f)
    return Float3(v.x / len, v.y / len, v.z / len)
}

internal fun dot(a: Float3, b: Float3): Float = a.x * b.x + a.y * b.y + a.z * b.z

internal fun cross(a: Float3, b: Float3): Float3 =
    Float3(
        x = a.y * b.z - a.z * b.y,
        y = a.z * b.x - a.x * b.z,
        z = a.x * b.y - a.y * b.x,
    )
