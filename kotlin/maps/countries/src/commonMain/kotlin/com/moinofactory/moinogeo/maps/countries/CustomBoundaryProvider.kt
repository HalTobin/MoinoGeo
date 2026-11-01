package com.moinofactory.moinogeo.maps.countries

import com.moinofactory.moinogeo.maps.core.BoundaryData
import com.moinofactory.moinogeo.maps.core.BoundaryProvider

class CustomBoundaryProvider(
    private val topoJsonContent: String
) : BoundaryProvider {
    override fun getBoundaryData(): BoundaryData {
        return TopoJsonDecoder.decodeTopoJson(topoJsonContent)
    }
}
