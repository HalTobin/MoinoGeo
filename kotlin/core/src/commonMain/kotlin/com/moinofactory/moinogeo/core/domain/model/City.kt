package com.moinofactory.moinogeo.core.domain.model

data class City(
    val id: Long,
    val stateId: Long?,
    val countryId: Long,
    val name: String,
    val latitude: Double?,
    val longitude: Double?,
)
