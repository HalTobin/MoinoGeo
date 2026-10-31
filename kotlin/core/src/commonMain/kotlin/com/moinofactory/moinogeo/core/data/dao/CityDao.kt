package com.moinofactory.moinogeo.core.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.moinofactory.moinogeo.core.data.entity.CityEntity

@Dao
internal interface CityDao {
    @Query("SELECT * FROM cities WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): CityEntity?

    @Query(
        """
        SELECT * FROM cities
        WHERE country_id = :countryId
        ORDER BY name ASC
        LIMIT :limit OFFSET :offset
        """,
    )
    suspend fun getByCountryId(countryId: Long, limit: Int, offset: Int): List<CityEntity>

    @Query("SELECT * FROM cities WHERE state_id = :stateId ORDER BY name ASC")
    suspend fun getByStateId(stateId: Long): List<CityEntity>

    @Query(
        """
        SELECT * FROM cities
        WHERE name LIKE '%' || :query || '%' COLLATE NOCASE
        ORDER BY name ASC
        LIMIT :limit
        """,
    )
    suspend fun searchByName(query: String, limit: Int): List<CityEntity>

    @Query(
        """
        SELECT * FROM cities
        WHERE country_id = :countryId
          AND name LIKE '%' || :query || '%' COLLATE NOCASE
        ORDER BY name ASC
        LIMIT :limit
        """,
    )
    suspend fun searchInCountry(countryId: Long, query: String, limit: Int): List<CityEntity>

    @Query(
        """
        SELECT * FROM cities
        WHERE state_id = :stateId
          AND name LIKE '%' || :query || '%' COLLATE NOCASE
        ORDER BY name ASC
        LIMIT :limit
        """,
    )
    suspend fun searchInState(stateId: Long, query: String, limit: Int): List<CityEntity>
}
