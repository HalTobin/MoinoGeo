package com.moinofactory.moinogeo.demo.shared

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.moinofactory.moinogeo.core.MoinoGeoDatabase
import kotlinx.coroutines.Dispatchers
import java.io.File

actual fun createDatabase(context: Any?): MoinoGeoDatabase {
    val dbFile = File("moinogeo.db")
    return Room.databaseBuilder<MoinoGeoDatabase>(
        name = dbFile.absolutePath
    )
    .setDriver(BundledSQLiteDriver())
    .setQueryCoroutineContext(Dispatchers.IO)
    .fallbackToDestructiveMigration(true)
    .build()
}
