package com.moinofactory.moinogeo.maps.core

import kotlin.math.abs

/** Wraps [longitude] into (−180°, 180°]. */
fun wrapLongitudeDegrees(longitude: Double): Double {
    var v = longitude % 360.0
    if (v > 180.0) v -= 360.0
    if (v < -180.0) v += 360.0
    return v
}

/** Shortest signed delta in degrees, in (−180°, 180°]. */
fun wrapDeltaDegrees(delta: Double): Double {
    var d = delta % 360.0
    if (d > 180.0) d -= 360.0
    if (d <= -180.0) d += 360.0
    return d
}

/**
 * Linearly interpolates toward [target] along the shortest longitude arc.
 * [fraction] is typically in \[0, 1\] but is not clamped.
 */
fun GeoPoint.lerpToward(target: GeoPoint, fraction: Float): GeoPoint {
    val t = fraction.toDouble()
    val dLon = wrapDeltaDegrees(target.longitude - longitude)
    return GeoPoint(
        latitude = (latitude + (target.latitude - latitude) * t).coerceIn(-90.0, 90.0),
        longitude = wrapLongitudeDegrees(longitude + dLon * t),
    )
}

/** True when [other] is within [tolerance] degrees in both axes (longitude wrapped). */
fun GeoPoint.isNear(other: GeoPoint, tolerance: Double = 1e-4): Boolean {
    if (abs(latitude - other.latitude) > tolerance) return false
    return abs(wrapDeltaDegrees(longitude - other.longitude)) <= tolerance
}
