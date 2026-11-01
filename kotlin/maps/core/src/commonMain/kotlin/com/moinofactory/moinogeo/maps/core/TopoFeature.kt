package com.moinofactory.moinogeo.maps.core

import kotlinx.serialization.Serializable

@Serializable
data class TopoFeature(
    val id: String,
    val name: String,
    val zone: TopoZone,
    val geometry: GeoPolygon
)
