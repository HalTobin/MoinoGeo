package com.moinofactory.moinogeo.core.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "states",
    indices = [
        Index(value = ["country_id"]),
        Index(value = ["name"]),
    ],
)
internal data class StateEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Long,
    @ColumnInfo(name = "country_id")
    val countryId: Long,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "iso2")
    val iso2: String?,
    @ColumnInfo(name = "iso3166_2")
    val iso3166_2: String?,
    @ColumnInfo(name = "type")
    val type: String?,
    @ColumnInfo(name = "latitude")
    val latitude: Double?,
    @ColumnInfo(name = "longitude")
    val longitude: Double?,
)
