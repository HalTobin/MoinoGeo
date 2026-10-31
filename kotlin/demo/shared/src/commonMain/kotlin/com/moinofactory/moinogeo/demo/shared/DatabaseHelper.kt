package com.moinofactory.moinogeo.demo.shared

import com.moinofactory.moinogeo.core.MoinoGeo
import com.moinofactory.moinogeo.core.domain.MoinoGeoService

fun createMoinoGeo(context: Any? = null): MoinoGeoService = MoinoGeo.create(context)
