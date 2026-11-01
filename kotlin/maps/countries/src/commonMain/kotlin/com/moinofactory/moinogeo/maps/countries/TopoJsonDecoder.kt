package com.moinofactory.moinogeo.maps.countries

import com.moinofactory.moinogeo.maps.core.BoundaryData
import com.moinofactory.moinogeo.maps.core.BoundaryFeature
import com.moinofactory.moinogeo.maps.core.GeoPoint
import com.moinofactory.moinogeo.maps.core.GeoPolygon
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object TopoJsonDecoder {
    private val jsonInstance = Json { ignoreUnknownKeys = true }

    fun decodeTopoJson(topoJsonString: String): BoundaryData {
        if (topoJsonString.isBlank()) return BoundaryData()
        return try {
            val topoJson = jsonInstance.decodeFromString<TopoJson>(topoJsonString)
            decodeTopoJsonModel(topoJson)
        } catch (e: Exception) {
            BoundaryData()
        }
    }

    fun decodeTopoJsonModel(topoJson: TopoJson): BoundaryData {
        val transform = topoJson.transform
        val scaleX = transform?.scale?.getOrNull(0) ?: 1.0
        val scaleY = transform?.scale?.getOrNull(1) ?: 1.0
        val translateX = transform?.translate?.getOrNull(0) ?: 0.0
        val translateY = transform?.translate?.getOrNull(1) ?: 0.0

        val decodedArcs = topoJson.arcs.map { arcPoints ->
            var currentX = 0.0
            var currentY = 0.0
            arcPoints.map { delta ->
                val dx = delta.getOrNull(0) ?: 0.0
                val dy = delta.getOrNull(1) ?: 0.0
                currentX += dx
                currentY += dy
                val lon = (currentX * scaleX) + translateX
                val lat = (currentY * scaleY) + translateY
                GeoPoint(latitude = lat, longitude = lon)
            }
        }

        val features = mutableListOf<BoundaryFeature>()
        val objects = topoJson.objects

        for ((objectKey, objectElement) in objects) {
            val obj = objectElement.jsonObject
            val geometries = obj["geometries"]?.jsonArray

            if (geometries != null) {
                for (geomElem in geometries) {
                    val geomObj = geomElem.jsonObject
                    val props = geomObj["properties"]?.jsonObject
                    val id = geomObj["id"]?.jsonPrimitive?.content ?: props?.get("iso_a2")?.jsonPrimitive?.content ?: "unknown"
                    val name = props?.get("name")?.jsonPrimitive?.content ?: id
                    val iso = props?.get("iso_a2")?.jsonPrimitive?.content ?: id

                    val parsedPolygons = parseGeomArcs(geomObj, decodedArcs)
                    if (parsedPolygons.isNotEmpty()) {
                        features.add(BoundaryFeature(id = id, isoCode = iso, name = name, geometries = parsedPolygons))
                    }
                }
            } else {
                val props = obj["properties"]?.jsonObject
                val name = props?.get("name")?.jsonPrimitive?.content ?: objectKey
                val parsedPolygons = parseGeomArcs(obj, decodedArcs)
                if (parsedPolygons.isNotEmpty()) {
                    features.add(BoundaryFeature(id = objectKey, isoCode = objectKey, name = name, geometries = parsedPolygons))
                }
            }
        }

        return BoundaryData(features = features)
    }

    private fun parseGeomArcs(geomObj: JsonObject, decodedArcs: List<List<GeoPoint>>): List<GeoPolygon> {
        val polygons = mutableListOf<GeoPolygon>()
        val arcsArray = geomObj["arcs"]?.jsonArray ?: return emptyList()

        for (polyElem in arcsArray) {
            val ringPoints = mutableListOf<GeoPoint>()
            val polyArcs = polyElem.jsonArray
            for (arcIdxElem in polyArcs) {
                if (arcIdxElem.jsonArray != null) {
                    for (subArcElem in arcIdxElem.jsonArray) {
                        val rawIndex = subArcElem.jsonPrimitive.content.toIntOrNull() ?: continue
                        ringPoints.addAll(getPointsForArcIndex(rawIndex, decodedArcs))
                    }
                } else {
                    val rawIndex = arcIdxElem.jsonPrimitive.content.toIntOrNull() ?: continue
                    ringPoints.addAll(getPointsForArcIndex(rawIndex, decodedArcs))
                }
            }
            if (ringPoints.isNotEmpty()) {
                polygons.add(GeoPolygon(outerBounds = ringPoints))
            }
        }

        return polygons
    }

    private fun getPointsForArcIndex(index: Int, decodedArcs: List<List<GeoPoint>>): List<GeoPoint> {
        val arcId = if (index < 0) index.inv() else index
        val arcPoints = decodedArcs.getOrNull(arcId) ?: return emptyList()
        return if (index < 0) arcPoints.reversed() else arcPoints
    }
}
