package com.moinofactory.moinogeo.maps.countries

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
internal data class TopoJson(
    val type: String = "Topology",
    val transform: TopoTransform? = null,
    val arcs: List<List<List<Double>>> = emptyList(),
    val objects: JsonObject = JsonObject(emptyMap()),
)

@Serializable
internal data class TopoTransform(
    val scale: List<Double>,
    val translate: List<Double>,
)
