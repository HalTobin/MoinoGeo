package com.moinofactory.moinogeo.core.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.moinofactory.moinogeo.core.data.entity.TranslationEntity

@Dao
internal interface TranslationDao {
    @Query(
        """
        SELECT * FROM translations
        WHERE entity_type = :entityType AND entity_id = :entityId
        ORDER BY lang_code ASC
        """,
    )
    suspend fun getForEntity(entityType: String, entityId: Long): List<TranslationEntity>

    @Query(
        """
        SELECT * FROM translations
        WHERE entity_type = :entityType
          AND entity_id = :entityId
          AND lang_code = :langCode
        LIMIT 1
        """,
    )
    suspend fun getOne(entityType: String, entityId: Long, langCode: String): TranslationEntity?
}
