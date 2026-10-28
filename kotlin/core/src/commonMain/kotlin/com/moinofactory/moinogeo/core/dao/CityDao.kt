package com.moinofactory.moinogeo.core.dao

import androidx.room.Dao
import androidx.room.Query
import com.moinofactory.moinogeo.core.model.CityEntity

@Dao
interface CityDao {
    @Query("SELECT * FROM cities WHERE country_code = :countryCode ORDER BY name ASC LIMIT :limit OFFSET :offset")
    suspend fun getCitiesByCountryCode(countryCode: String, limit: Int = 100, offset: Int = 0): List<CityEntity>

    @Query("SELECT * FROM cities WHERE state_id = :stateId ORDER BY name ASC")
    suspend fun getCitiesByStateId(stateId: Long): List<CityEntity>

    @Query("SELECT * FROM cities WHERE country_code = :countryCode AND name LIKE :query || '%' ORDER BY name ASC LIMIT :limit")
    suspend fun searchCitiesInCountry(countryCode: String, query: String, limit: Int = 50): List<CityEntity>

    @Query("SELECT * FROM cities WHERE name LIKE :query || '%' ORDER BY name ASC LIMIT :limit")
    suspend fun searchCities(query: String, limit: Int = 50): List<CityEntity>
}
