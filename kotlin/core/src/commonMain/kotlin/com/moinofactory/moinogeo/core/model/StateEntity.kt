package com.moinofactory.moinogeo.core.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "states")
data class StateEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Long,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "country_id")
    val countryId: Long,
    @ColumnInfo(name = "country_code")
    val countryCode: String,
    @ColumnInfo(name = "state_code")
    val stateCode: String?,
    @ColumnInfo(name = "type")
    val type: String?,
    @ColumnInfo(name = "latitude")
    val latitude: Double?,
    @ColumnInfo(name = "longitude")
    val longitude: Double?
)
