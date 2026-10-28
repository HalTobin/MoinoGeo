package com.moinofactory.moinogeo.core.dao

import androidx.room.Dao
import androidx.room.Query
import com.moinofactory.moinogeo.core.model.CountryEntity

@Dao
interface CountryDao {
    @Query("SELECT * FROM countries ORDER BY name ASC")
    suspend fun getAllCountries(): List<CountryEntity>

    @Query("SELECT * FROM countries WHERE iso2 = :iso2 LIMIT 1")
    suspend fun getCountryByIso2(iso2: String): CountryEntity?

    @Query("SELECT * FROM countries WHERE region = :region ORDER BY name ASC")
    suspend fun getCountriesByRegion(region: String): List<CountryEntity>

    @Query("SELECT DISTINCT region FROM countries WHERE region IS NOT NULL ORDER BY region ASC")
    suspend fun getAllRegions(): List<String>
}
