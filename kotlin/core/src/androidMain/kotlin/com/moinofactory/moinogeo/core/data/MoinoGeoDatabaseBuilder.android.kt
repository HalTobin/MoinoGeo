package com.moinofactory.moinogeo.core.data

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

internal actual fun createMoinoGeoDatabaseBuilder(context: Any?): RoomDatabase.Builder<MoinoGeoDatabase> {
    val appContext = requireNotNull(context as? Context) {
        "Android Context is required to open MoinoGeoDatabase"
    }.applicationContext
    val dbFile = installBundledDatabase(appContext.getDatabasePath(MoinoGeoDatabase.DATABASE_NAME))
    return Room.databaseBuilder<MoinoGeoDatabase>(
        context = appContext,
        name = dbFile.absolutePath,
    )
}

private fun installBundledDatabase(target: File): File {
    if (!target.exists() || target.length() == 0L) {
        target.parentFile?.mkdirs()
        target.writeBytes(requireBundledDatabaseBytes())
    }
    return target
}
