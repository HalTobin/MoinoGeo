package com.moinofactory.moinogeo.core.data

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

/**
 * Platform-specific [RoomDatabase.Builder] for the bundled regions database.
 *
 * The SQLite file is shipped once under `commonMain/resources` ([MoinoGeoDatabase.RESOURCE_PATH]).
 * Android and JVM copy it to a writable path on first open; Apple wiring comes later.
 */
internal expect fun createMoinoGeoDatabaseBuilder(context: Any? = null): RoomDatabase.Builder<MoinoGeoDatabase>

internal fun buildMoinoGeoDatabase(context: Any? = null): MoinoGeoDatabase {
    return createMoinoGeoDatabaseBuilder(context)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .fallbackToDestructiveMigration(true)
        .build()
}
