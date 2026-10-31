package com.moinofactory.moinogeo.demo.shared

import androidx.compose.runtime.Composable
import com.moinofactory.moinogeo.core.domain.MoinoGeoService
import com.moinofactory.moinogeo.demo.shared.explorer.DemoScreen

/**
 * Root demo entry point.
 */
@Composable
fun MoinoGeoAppScreen(geo: MoinoGeoService) {
    DemoScreen(geo = geo)
}
