package com.moinofactory.moinogeo.core.data

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

/**
 * Platform-specific [RoomDatabase.Builder] for the bundled regions database.
 *
 * - Android: createFromAsset with [MoinoGeoDatabase.ASSET_PATH]
 * - JVM: copies [MoinoGeoDatabase.RESOURCE_PATH] from the classpath to a local file
 * - Apple: on-disk path stub until Darwin export lands
 */
internal expect fun createMoinoGeoDatabaseBuilder(context: Any? = null): RoomDatabase.Builder<MoinoGeoDatabase>

internal fun buildMoinoGeoDatabase(context: Any? = null): MoinoGeoDatabase {
    return createMoinoGeoDatabaseBuilder(context)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .fallbackToDestructiveMigration(true)
        .build()
}
