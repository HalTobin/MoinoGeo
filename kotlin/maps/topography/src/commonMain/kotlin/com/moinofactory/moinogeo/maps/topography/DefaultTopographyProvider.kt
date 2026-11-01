package com.moinofactory.moinogeo.maps.topography

import com.moinofactory.moinogeo.maps.core.GeoPoint
import com.moinofactory.moinogeo.maps.core.GeoPolygon
import com.moinofactory.moinogeo.maps.core.TopoFeature
import com.moinofactory.moinogeo.maps.core.TopoZone
import com.moinofactory.moinogeo.maps.core.TopographyData
import com.moinofactory.moinogeo.maps.core.TopographyProvider

class DefaultTopographyProvider : TopographyProvider {
    override fun getTopographyData(): TopographyData {
        return TopographyData(
            features = listOf(
                TopoFeature(
                    id = "water_pacific",
                    name = "Pacific Ocean",
                    zone = TopoZone.WATER,
                    geometry = GeoPolygon(
                        outerBounds = listOf(
                            GeoPoint(60.0, -180.0),
                            GeoPoint(60.0, -100.0),
                            GeoPoint(-60.0, -100.0),
                            GeoPoint(-60.0, 150.0),
                            GeoPoint(60.0, 150.0),
                            GeoPoint(60.0, 180.0)
                        )
                    )
                ),
                TopoFeature(
                    id = "forest_amazon",
                    name = "Amazon Rainforest",
                    zone = TopoZone.FOREST,
                    geometry = GeoPolygon(
                        outerBounds = listOf(
                            GeoPoint(2.0, -75.0),
                            GeoPoint(4.0, -50.0),
                            GeoPoint(-15.0, -48.0),
                            GeoPoint(-12.0, -77.0),
                            GeoPoint(2.0, -75.0)
                        )
                    )
                ),
                TopoFeature(
                    id = "rock_himalayas",
                    name = "Himalayas Mountain Range",
                    zone = TopoZone.ROCK,
                    geometry = GeoPolygon(
                        outerBounds = listOf(
                            GeoPoint(35.0, 70.0),
                            GeoPoint(36.0, 95.0),
                            GeoPoint(26.0, 97.0),
                            GeoPoint(27.0, 72.0),
                            GeoPoint(35.0, 70.0)
                        )
                    )
                ),
                TopoFeature(
                    id = "snow_antarctica",
                    name = "Antarctica Ice Sheet",
                    zone = TopoZone.SNOW,
                    geometry = GeoPolygon(
                        outerBounds = listOf(
                            GeoPoint(-60.0, -180.0),
                            GeoPoint(-60.0, 180.0),
                            GeoPoint(-90.0, 180.0),
                            GeoPoint(-90.0, -180.0),
                            GeoPoint(-60.0, -180.0)
                        )
                    )
                ),
                TopoFeature(
                    id = "neutral_sahara",
                    name = "Sahara Plateau",
                    zone = TopoZone.NEUTRAL,
                    geometry = GeoPolygon(
                        outerBounds = listOf(
                            GeoPoint(35.0, -15.0),
                            GeoPoint(32.0, 35.0),
                            GeoPoint(15.0, 38.0),
                            GeoPoint(12.0, -16.0),
                            GeoPoint(35.0, -15.0)
                        )
                    )
                )
            )
        )
    }
}
