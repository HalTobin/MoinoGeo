package com.moinofactory.moinogeo.core.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "cities")
data class CityEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Long,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "state_id")
    val stateId: Long,
    @ColumnInfo(name = "state_code")
    val stateCode: String?,
    @ColumnInfo(name = "country_id")
    val countryId: Long,
    @ColumnInfo(name = "country_code")
    val countryCode: String,
    @ColumnInfo(name = "latitude")
    val latitude: Double?,
    @ColumnInfo(name = "longitude")
    val longitude: Double?,
    @ColumnInfo(name = "wiki_data_id")
    val wikiDataId: String?
)
