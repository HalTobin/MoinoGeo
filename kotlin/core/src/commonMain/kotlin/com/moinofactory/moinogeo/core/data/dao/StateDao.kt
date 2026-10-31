package com.moinofactory.moinogeo.core.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.moinofactory.moinogeo.core.data.entity.StateEntity

@Dao
internal interface StateDao {
    @Query("SELECT * FROM states WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): StateEntity?

    @Query("SELECT * FROM states WHERE country_id = :countryId ORDER BY name ASC")
    suspend fun getByCountryId(countryId: Long): List<StateEntity>

    @Query(
        """
        SELECT * FROM states
        WHERE name LIKE '%' || :query || '%' COLLATE NOCASE
           OR iso2 LIKE :query COLLATE NOCASE
           OR iso3166_2 LIKE :query COLLATE NOCASE
        ORDER BY name ASC
        LIMIT :limit
        """,
    )
    suspend fun searchByNameOrCode(query: String, limit: Int): List<StateEntity>

    @Query(
        """
        SELECT * FROM states
        WHERE country_id = :countryId
          AND (
            name LIKE '%' || :query || '%' COLLATE NOCASE
            OR iso2 LIKE :query COLLATE NOCASE
            OR iso3166_2 LIKE :query COLLATE NOCASE
          )
        ORDER BY name ASC
        LIMIT :limit
        """,
    )
    suspend fun searchInCountry(countryId: Long, query: String, limit: Int): List<StateEntity>
}
