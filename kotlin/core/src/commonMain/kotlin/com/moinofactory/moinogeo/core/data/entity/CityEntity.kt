package com.moinofactory.moinogeo.core.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cities",
    indices = [
        Index(value = ["country_id"]),
        Index(value = ["state_id"]),
        Index(value = ["name"]),
    ],
)
internal data class CityEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Long,
    @ColumnInfo(name = "state_id")
    val stateId: Long?,
    @ColumnInfo(name = "country_id")
    val countryId: Long,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "latitude")
    val latitude: Double?,
    @ColumnInfo(name = "longitude")
    val longitude: Double?,
)
