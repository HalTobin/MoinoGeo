package com.moinofactory.moinogeo.demo.shared.data

import com.moinofactory.moinogeo.core.domain.model.Country

/**
 * Sample fallback dataset when the database is empty or unavailable.
 */
internal val sampleCountries: List<Country> = listOf(
    Country(1, "United States", "US", "USA", "Washington D.C.", 2, 6, 38.0, -97.0),
    Country(2, "France", "FR", "FRA", "Paris", 4, 17, 46.0, 2.0),
    Country(3, "Germany", "DE", "DEU", "Berlin", 4, 17, 51.0, 9.0),
    Country(4, "Japan", "JP", "JPN", "Tokyo", 3, 12, 36.0, 138.0),
    Country(5, "Brazil", "BR", "BRA", "Brasília", 2, 8, -10.0, -55.0),
    Country(6, "United Kingdom", "GB", "GBR", "London", 4, 18, 54.0, -2.0),
    Country(7, "Canada", "CA", "CAN", "Ottawa", 2, 6, 60.0, -95.0),
    Country(8, "Australia", "AU", "AUS", "Canberra", 5, 19, -27.0, 133.0),
    Country(9, "Italy", "IT", "ITA", "Rome", 4, 16, 42.8, 12.8),
    Country(10, "Spain", "ES", "ESP", "Madrid", 4, 16, 40.0, -4.0),
    Country(11, "India", "IN", "IND", "New Delhi", 3, 14, 20.0, 77.0),
    Country(12, "China", "CN", "CHN", "Beijing", 3, 12, 35.0, 105.0),
    Country(13, "South Korea", "KR", "KOR", "Seoul", 3, 12, 37.0, 127.5),
    Country(14, "Mexico", "MX", "MEX", "Mexico City", 2, 9, 23.0, -102.0),
    Country(15, "South Africa", "ZA", "ZAF", "Pretoria", 1, 5, -29.0, 24.0),
    Country(16, "Netherlands", "NL", "NLD", "Amsterdam", 4, 17, 52.5, 5.75),
    Country(17, "Switzerland", "CH", "CHE", "Bern", 4, 17, 47.0, 8.0),
    Country(18, "Argentina", "AR", "ARG", "Buenos Aires", 2, 8, -34.0, -64.0),
    Country(19, "Sweden", "SE", "SWE", "Stockholm", 4, 18, 62.0, 15.0),
    Country(20, "Ukraine", "UA", "UKR", "Kyiv", 4, 15, 49.0, 32.0),
)
