package com.moinofactory.moinogeo.maps.core

import kotlinx.serialization.Serializable

@Serializable
data class GeoPolygon(
    val outerBounds: List<GeoPoint>,
    val innerHoles: List<List<GeoPoint>> = emptyList()
)
