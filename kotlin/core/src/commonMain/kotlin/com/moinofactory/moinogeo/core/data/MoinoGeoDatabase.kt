package com.moinofactory.moinogeo.core.data

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.moinofactory.moinogeo.core.data.dao.CityDao
import com.moinofactory.moinogeo.core.data.dao.CountryDao
import com.moinofactory.moinogeo.core.data.dao.RegionDao
import com.moinofactory.moinogeo.core.data.dao.StateDao
import com.moinofactory.moinogeo.core.data.dao.SubregionDao
import com.moinofactory.moinogeo.core.data.dao.TranslationDao
import com.moinofactory.moinogeo.core.data.entity.CityEntity
import com.moinofactory.moinogeo.core.data.entity.CountryEntity
import com.moinofactory.moinogeo.core.data.entity.RegionEntity
import com.moinofactory.moinogeo.core.data.entity.StateEntity
import com.moinofactory.moinogeo.core.data.entity.SubregionEntity
import com.moinofactory.moinogeo.core.data.entity.TranslationEntity

@Database(
    entities = [
        RegionEntity::class,
        SubregionEntity::class,
        CountryEntity::class,
        StateEntity::class,
        CityEntity::class,
        TranslationEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@ConstructedBy(MoinoGeoDatabaseConstructor::class)
internal abstract class MoinoGeoDatabase : RoomDatabase() {
    abstract fun regionDao(): RegionDao
    abstract fun subregionDao(): SubregionDao
    abstract fun countryDao(): CountryDao
    abstract fun stateDao(): StateDao
    abstract fun cityDao(): CityDao
    abstract fun translationDao(): TranslationDao

    companion object {
        const val DATABASE_NAME = "moinogeo.db"
        const val ASSET_PATH = "databases/moinogeo.db"
        const val RESOURCE_PATH = "databases/moinogeo.db"
    }
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
internal expect object MoinoGeoDatabaseConstructor : RoomDatabaseConstructor<MoinoGeoDatabase> {
    override fun initialize(): MoinoGeoDatabase
}
