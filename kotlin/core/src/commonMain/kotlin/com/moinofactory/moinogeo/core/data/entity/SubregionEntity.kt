package com.moinofactory.moinogeo.core.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "subregions",
    indices = [Index(value = ["region_id"])],
)
internal data class SubregionEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Long,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "region_id")
    val regionId: Long,
)
