package com.moinofactory.moinogeo.core

import com.moinofactory.moinogeo.core.data.buildMoinoGeoDatabase
import com.moinofactory.moinogeo.core.domain.MoinoGeoService

/**
 * Factory for the public [MoinoGeoService].
 *
 * On Android, pass an application `Context`.
 * On JVM, [context] is ignored; the bundled DB is copied from classpath resources.
 */
object MoinoGeo {
    fun create(context: Any? = null): MoinoGeoService {
        return MoinoGeoService(buildMoinoGeoDatabase(context))
    }
}
