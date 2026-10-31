package com.moinofactory.moinogeo.demo.shared

import androidx.room.Room
import com.moinofactory.moinogeo.core.MoinoGeoDatabase
import java.io.File

actual fun createDatabase(context: Any?): MoinoGeoDatabase {
    val dbFile = File("moinogeo.db")
    return Room.databaseBuilder<MoinoGeoDatabase>(
        name = dbFile.absolutePath
    )
    .fallbackToDestructiveMigration(true)
    .build()
}
