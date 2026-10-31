package com.moinofactory.moinogeo.core.domain.model

data class Country(
    val id: Long,
    val name: String,
    val iso2: String,
    val iso3: String,
    val capital: String?,
    val regionId: Long?,
    val subregionId: Long?,
    val latitude: Double?,
    val longitude: Double?,
)
