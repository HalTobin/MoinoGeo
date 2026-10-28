package com.moinofactory.moinogeo.core.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "translations", primaryKeys = ["entity_type", "entity_id", "lang_code"])
data class TranslationEntity(
    @ColumnInfo(name = "entity_type")
    val entityType: String,
    @ColumnInfo(name = "entity_id")
    val entityId: Long,
    @ColumnInfo(name = "lang_code")
    val langCode: String,
    @ColumnInfo(name = "name")
    val name: String
)
