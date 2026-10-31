package com.moinofactory.moinogeo.core.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "countries",
    indices = [
        Index(value = ["iso2"]),
        Index(value = ["iso3"]),
        Index(value = ["name"]),
        Index(value = ["capital"]),
        Index(value = ["region_id"]),
        Index(value = ["subregion_id"]),
    ],
)
internal data class CountryEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Long,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "iso2")
    val iso2: String,
    @ColumnInfo(name = "iso3")
    val iso3: String,
    @ColumnInfo(name = "capital")
    val capital: String?,
    @ColumnInfo(name = "region_id")
    val regionId: Long?,
    @ColumnInfo(name = "subregion_id")
    val subregionId: Long?,
    @ColumnInfo(name = "latitude")
    val latitude: Double?,
    @ColumnInfo(name = "longitude")
    val longitude: Double?,
)
