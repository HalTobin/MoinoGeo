package com.moinofactory.moinogeo.core.data

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File
import java.nio.file.Files

internal actual fun createMoinoGeoDatabaseBuilder(context: Any?): RoomDatabase.Builder<MoinoGeoDatabase> {
    val dbFile = resolveJvmDatabaseFile()
    return Room.databaseBuilder<MoinoGeoDatabase>(name = dbFile.absolutePath)
}

private fun resolveJvmDatabaseFile(): File {
    val target = File(
        System.getProperty("user.home"),
        ".moinogeo/${MoinoGeoDatabase.DATABASE_NAME}",
    )
    if (!target.exists() || target.length() == 0L) {
        target.parentFile?.mkdirs()
        val resource = MoinoGeoDatabase::class.java.classLoader
            .getResourceAsStream(MoinoGeoDatabase.RESOURCE_PATH)
            ?: error(
                "Bundled database not found on classpath: ${MoinoGeoDatabase.RESOURCE_PATH}. " +
                    "Run: python3 scripts/regions/export_kotlin.py",
            )
        resource.use { input ->
            Files.copy(input, target.toPath())
        }
    }
    return target
}
