package com.moinofactory.moinogeo.core.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.moinofactory.moinogeo.core.data.entity.CountryEntity

@Dao
internal interface CountryDao {
    @Query("SELECT * FROM countries ORDER BY name ASC")
    suspend fun getAll(): List<CountryEntity>

    @Query("SELECT * FROM countries WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): CountryEntity?

    @Query("SELECT * FROM countries WHERE iso2 = :iso2 LIMIT 1")
    suspend fun getByIso2(iso2: String): CountryEntity?

    @Query("SELECT * FROM countries WHERE iso3 = :iso3 LIMIT 1")
    suspend fun getByIso3(iso3: String): CountryEntity?

    @Query("SELECT * FROM countries WHERE region_id = :regionId ORDER BY name ASC")
    suspend fun getByRegionId(regionId: Long): List<CountryEntity>

    @Query("SELECT * FROM countries WHERE subregion_id = :subregionId ORDER BY name ASC")
    suspend fun getBySubregionId(subregionId: Long): List<CountryEntity>

    @Query(
        """
        SELECT * FROM countries
        WHERE capital LIKE '%' || :query || '%' COLLATE NOCASE
        ORDER BY capital ASC
        LIMIT :limit
        """,
    )
    suspend fun searchByCapital(query: String, limit: Int): List<CountryEntity>

    @Query(
        """
        SELECT * FROM countries
        WHERE name LIKE '%' || :query || '%' COLLATE NOCASE
           OR iso2 LIKE :query COLLATE NOCASE
           OR iso3 LIKE :query COLLATE NOCASE
        ORDER BY name ASC
        LIMIT :limit
        """,
    )
    suspend fun searchByNameOrCode(query: String, limit: Int): List<CountryEntity>

    @Query(
        """
        SELECT c.* FROM countries c
        INNER JOIN translations t
          ON t.entity_type = 'country' AND t.entity_id = c.id
        WHERE (:langCode IS NULL OR t.lang_code = :langCode)
          AND t.name LIKE '%' || :query || '%' COLLATE NOCASE
        ORDER BY t.name ASC
        LIMIT :limit
        """,
    )
    suspend fun searchByTranslation(
        query: String,
        langCode: String?,
        limit: Int,
    ): List<CountryEntity>
}
