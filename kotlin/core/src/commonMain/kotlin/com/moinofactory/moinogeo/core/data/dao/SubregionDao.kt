package com.moinofactory.moinogeo.core.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.moinofactory.moinogeo.core.data.entity.SubregionEntity

@Dao
internal interface SubregionDao {
    @Query("SELECT * FROM subregions ORDER BY name ASC")
    suspend fun getAll(): List<SubregionEntity>

    @Query("SELECT * FROM subregions WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): SubregionEntity?

    @Query("SELECT * FROM subregions WHERE region_id = :regionId ORDER BY name ASC")
    suspend fun getByRegionId(regionId: Long): List<SubregionEntity>

    @Query(
        """
        SELECT * FROM subregions
        WHERE name LIKE '%' || :query || '%' COLLATE NOCASE
        ORDER BY name ASC
        LIMIT :limit
        """,
    )
    suspend fun searchByName(query: String, limit: Int): List<SubregionEntity>
}
