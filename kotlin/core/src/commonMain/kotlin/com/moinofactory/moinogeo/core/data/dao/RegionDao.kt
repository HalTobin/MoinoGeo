package com.moinofactory.moinogeo.core.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.moinofactory.moinogeo.core.data.entity.RegionEntity

@Dao
internal interface RegionDao {
    @Query("SELECT * FROM regions ORDER BY name ASC")
    suspend fun getAll(): List<RegionEntity>

    @Query("SELECT * FROM regions WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): RegionEntity?

    @Query(
        """
        SELECT * FROM regions
        WHERE name LIKE :prefix || '%' COLLATE NOCASE
        ORDER BY name ASC
        LIMIT :limit
        """,
    )
    suspend fun searchByNamePrefix(prefix: String, limit: Int): List<RegionEntity>

    @Query(
        """
        SELECT * FROM regions
        WHERE name LIKE '%' || :query || '%' COLLATE NOCASE
        ORDER BY name ASC
        LIMIT :limit
        """,
    )
    suspend fun searchByName(query: String, limit: Int): List<RegionEntity>
}
