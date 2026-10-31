package com.moinofactory.moinogeo.core.domain.model

/**
 * Lightweight listing / search hit shared by every geographic level.
 *
 * Use the typed models ([Region], [Country], …) when you need level-specific fields.
 */
data class GeoPlace(
    val type: GeoPlaceType,
    val id: Long,
    val name: String,
    val iso2: String? = null,
    val iso3: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val regionId: Long? = null,
    val subregionId: Long? = null,
    val countryId: Long? = null,
    val stateId: Long? = null,
)
