package com.moinofactory.moinogeo.maps.core

import kotlinx.serialization.Serializable

@Serializable
data class BoundaryData(
    val features: List<BoundaryFeature> = emptyList()
)
