package com.moinofactory.moinogeo.core.data

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

internal actual fun createMoinoGeoDatabaseBuilder(context: Any?): RoomDatabase.Builder<MoinoGeoDatabase> {
    val appContext = requireNotNull(context as? Context) {
        "Android Context is required to open MoinoGeoDatabase"
    }.applicationContext
    return Room.databaseBuilder<MoinoGeoDatabase>(
        context = appContext,
        name = MoinoGeoDatabase.DATABASE_NAME,
    ).createFromAsset(MoinoGeoDatabase.ASSET_PATH)
}
