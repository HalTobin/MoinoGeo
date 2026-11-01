package com.moinofactory.moinogeo.maps.core

import kotlinx.serialization.Serializable

@Serializable
enum class TopoZone {
    WATER,
    FOREST,
    ROCK,
    SNOW,
    NEUTRAL
}
