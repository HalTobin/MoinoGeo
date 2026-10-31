package com.moinofactory.moinogeo.core.data

import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

/**
 * Apple (iOS / macOS) builder stub — bundled asset wiring lands with the Darwin export.
 * Opens an on-disk database path for now so native targets compile.
 */
@OptIn(ExperimentalForeignApi::class)
internal actual fun createMoinoGeoDatabaseBuilder(context: Any?): RoomDatabase.Builder<MoinoGeoDatabase> {
    val docUrl = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    val path = requireNotNull(docUrl?.path) { "Unable to resolve NSDocumentDirectory" }
    return Room.databaseBuilder<MoinoGeoDatabase>(name = "$path/${MoinoGeoDatabase.DATABASE_NAME}")
}
