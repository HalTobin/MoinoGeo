package com.moinofactory.moinogeo.core.domain.model

data class State(
    val id: Long,
    val countryId: Long,
    val name: String,
    val iso2: String?,
    val iso3166_2: String?,
    val type: String?,
    val latitude: Double?,
    val longitude: Double?,
)
