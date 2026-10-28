package com.moinofactory.moinogeo.maps

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class TopoJson(
    val type: String,
    val transform: TopoTransform? = null,
    val arcs: List<List<List<Double>>> = emptyList(),
    val objects: JsonObject = JsonObject(emptyMap())
)

@Serializable
data class TopoTransform(
    val scale: List<Double>,
    val translate: List<Double>
)
