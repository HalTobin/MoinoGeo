package com.moinofactory.moinogeo.maps.core

import kotlinx.serialization.Serializable

@Serializable
data class BoundaryFeature(
    val id: String,
    val isoCode: String,
    val name: String,
    val geometries: List<GeoPolygon> = emptyList()
)
