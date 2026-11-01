package com.moinofactory.moinogeo.maps.topography

import com.moinofactory.moinogeo.maps.core.GeoPoint
import com.moinofactory.moinogeo.maps.core.TopoZone
import com.moinofactory.moinogeo.maps.core.TopographyData

object TopographyUtils {
    fun findZoneAt(point: GeoPoint, topographyData: TopographyData): TopoZone {
        for (feature in topographyData.features) {
            if (isPointInPolygon(point, feature.geometry.outerBounds)) {
                return feature.zone
            }
        }
        return TopoZone.NEUTRAL
    }

    private fun isPointInPolygon(point: GeoPoint, polygon: List<GeoPoint>): Boolean {
        if (polygon.size < 3) return false
        var inside = false
        var j = polygon.size - 1
        for (i in polygon.indices) {
            val pi = polygon[i]
            val pj = polygon[j]
            if ((pi.longitude > point.longitude) != (pj.longitude > point.longitude) &&
                (point.latitude < (pj.latitude - pi.latitude) * (point.longitude - pi.longitude) / (pj.longitude - pi.longitude) + pi.latitude)
            ) {
                inside = !inside
            }
            j = i
        }
        return inside
    }
}
