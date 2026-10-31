package com.moinofactory.moinogeo.demo.shared

import android.content.Context
import androidx.room.Room
import com.moinofactory.moinogeo.core.MoinoGeoDatabase

actual fun createDatabase(context: Any?): MoinoGeoDatabase {
    val ctx = requireNotNull(context as? Context) { "Context is required for Android Room database creation" }
    return Room.databaseBuilder<MoinoGeoDatabase>(
        context = ctx,
        name = MoinoGeoDatabase.DATABASE_NAME
    )
    .createFromAsset("databases/moinogeo.db")
    .fallbackToDestructiveMigration(true)
    .build()
}
