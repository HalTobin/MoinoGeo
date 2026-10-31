package com.moinofactory.moinogeo.demo.shared

import androidx.compose.runtime.Composable
import com.moinofactory.moinogeo.core.MoinoGeoDatabase
import com.moinofactory.moinogeo.demo.shared.flags.FlagsDemoScreen

/**
 * Root demo entry point.
 *
 * Currently hosts the flags showcase. Later this can become a section shell
 * for map and database exploration demos as well.
 */
@Composable
fun MoinoGeoAppScreen(db: MoinoGeoDatabase) {
    FlagsDemoScreen(db = db)
}
