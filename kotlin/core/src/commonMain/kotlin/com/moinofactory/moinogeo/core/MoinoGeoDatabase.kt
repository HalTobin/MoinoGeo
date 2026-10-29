package com.moinofactory.moinogeo.core

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.moinofactory.moinogeo.core.dao.CityDao
import com.moinofactory.moinogeo.core.dao.CountryDao
import com.moinofactory.moinogeo.core.dao.StateDao
import com.moinofactory.moinogeo.core.model.CityEntity
import com.moinofactory.moinogeo.core.model.CountryEntity
import com.moinofactory.moinogeo.core.model.StateEntity
import com.moinofactory.moinogeo.core.model.TranslationEntity

@Database(
    entities = [CountryEntity::class, StateEntity::class, CityEntity::class, TranslationEntity::class],
    version = 1,
    exportSchema = false
)
@ConstructedBy(MoinoGeoDatabaseConstructor::class)
abstract class MoinoGeoDatabase : RoomDatabase() {
    abstract fun countryDao(): CountryDao
    abstract fun stateDao(): StateDao
    abstract fun cityDao(): CityDao

    companion object {
        const val DATABASE_NAME = "moinogeo.db"
    }
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object MoinoGeoDatabaseConstructor : RoomDatabaseConstructor<MoinoGeoDatabase> {
    override fun initialize(): MoinoGeoDatabase
}

