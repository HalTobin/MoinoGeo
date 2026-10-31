package com.moinofactory.moinogeo.core.data

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

internal actual fun createMoinoGeoDatabaseBuilder(context: Any?): RoomDatabase.Builder<MoinoGeoDatabase> {
    val dbFile = installBundledDatabase(
        File(System.getProperty("user.home"), ".moinogeo/${MoinoGeoDatabase.DATABASE_NAME}"),
    )
    return Room.databaseBuilder<MoinoGeoDatabase>(name = dbFile.absolutePath)
}

private fun installBundledDatabase(target: File): File {
    if (!target.exists() || target.length() == 0L) {
        target.parentFile?.mkdirs()
        target.writeBytes(requireBundledDatabaseBytes())
    }
    return target
}
