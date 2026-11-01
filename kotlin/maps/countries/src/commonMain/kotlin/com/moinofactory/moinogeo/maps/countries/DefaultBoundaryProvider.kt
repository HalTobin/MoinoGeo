package com.moinofactory.moinogeo.maps.countries

import com.moinofactory.moinogeo.maps.core.BoundaryData
import com.moinofactory.moinogeo.maps.core.BoundaryFeature
import com.moinofactory.moinogeo.maps.core.BoundaryProvider
import com.moinofactory.moinogeo.maps.core.GeoPoint
import com.moinofactory.moinogeo.maps.core.GeoPolygon

class DefaultBoundaryProvider(
    private val customTopoJson: String? = null
) : BoundaryProvider {

    override fun getBoundaryData(): BoundaryData {
        if (!customTopoJson.isNullOrBlank()) {
            return TopoJsonDecoder.decodeTopoJson(customTopoJson)
        }
        return createFallbackWorldBorders()
    }

    private fun createFallbackWorldBorders(): BoundaryData {
        return BoundaryData(
            features = listOf(
                BoundaryFeature(
                    id = "US",
                    isoCode = "US",
                    name = "United States",
                    geometries = listOf(
                        GeoPolygon(
                            outerBounds = listOf(
                                GeoPoint(49.0, -125.0),
                                GeoPoint(49.0, -67.0),
                                GeoPoint(25.0, -80.0),
                                GeoPoint(31.0, -111.0),
                                GeoPoint(49.0, -125.0)
                            )
                        )
                    )
                ),
                BoundaryFeature(
                    id = "FR",
                    isoCode = "FR",
                    name = "France",
                    geometries = listOf(
                        GeoPolygon(
                            outerBounds = listOf(
                                GeoPoint(51.0, 2.5),
                                GeoPoint(48.9, 8.2),
                                GeoPoint(43.7, 7.5),
                                GeoPoint(42.5, 3.1),
                                GeoPoint(43.5, -1.8),
                                GeoPoint(48.4, -4.8),
                                GeoPoint(51.0, 2.5)
                            )
                        )
                    )
                ),
                BoundaryFeature(
                    id = "JP",
                    isoCode = "JP",
                    name = "Japan",
                    geometries = listOf(
                        GeoPolygon(
                            outerBounds = listOf(
                                GeoPoint(45.5, 141.9),
                                GeoPoint(43.3, 145.8),
                                GeoPoint(35.1, 140.0),
                                GeoPoint(31.0, 130.5),
                                GeoPoint(34.0, 131.0),
                                GeoPoint(41.5, 140.0),
                                GeoPoint(45.5, 141.9)
                            )
                        )
                    )
                ),
                BoundaryFeature(
                    id = "BR",
                    isoCode = "BR",
                    name = "Brazil",
                    geometries = listOf(
                        GeoPolygon(
                            outerBounds = listOf(
                                GeoPoint(5.0, -60.0),
                                GeoPoint(-5.0, -35.0),
                                GeoPoint(-33.0, -53.0),
                                GeoPoint(-20.0, -58.0),
                                GeoPoint(-10.0, -70.0),
                                GeoPoint(5.0, -60.0)
                            )
                        )
                    )
                )
            )
        )
    }
}
