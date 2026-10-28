package com.moinofactory.moinogeo.core.dao

import androidx.room.Dao
import androidx.room.Query
import com.moinofactory.moinogeo.core.model.StateEntity

@Dao
interface StateDao {
    @Query("SELECT * FROM states WHERE country_code = :countryCode ORDER BY name ASC")
    suspend fun getStatesByCountryCode(countryCode: String): List<StateEntity>

    @Query("SELECT * FROM states WHERE country_id = :countryId ORDER BY name ASC")
    suspend fun getStatesByCountryId(countryId: Long): List<StateEntity>

    @Query("SELECT * FROM states WHERE id = :stateId LIMIT 1")
    suspend fun getStateById(stateId: Long): StateEntity?
}
