package com.moinofactory.moinogeo.maps.core

import kotlinx.serialization.Serializable

@Serializable
data class TopographyData(
    val features: List<TopoFeature> = emptyList()
)
