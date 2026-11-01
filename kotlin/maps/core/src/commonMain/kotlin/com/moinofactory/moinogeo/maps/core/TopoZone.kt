package com.moinofactory.moinogeo.maps.core

import kotlinx.serialization.Serializable

@Serializable
enum class TopoZone {
    WATER,
    FOREST,
    GRASSLAND,
    AGRICULTURE,
    ROCK,
    SNOW,
    SAND,
    NEUTRAL
}
