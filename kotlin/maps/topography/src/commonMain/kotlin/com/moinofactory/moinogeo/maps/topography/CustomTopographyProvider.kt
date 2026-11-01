package com.moinofactory.moinogeo.maps.topography

import com.moinofactory.moinogeo.maps.core.TopoFeature
import com.moinofactory.moinogeo.maps.core.TopographyData
import com.moinofactory.moinogeo.maps.core.TopographyProvider

class CustomTopographyProvider(
    private val features: List<TopoFeature>
) : TopographyProvider {
    override fun getTopographyData(): TopographyData {
        return TopographyData(features = features)
    }
}
