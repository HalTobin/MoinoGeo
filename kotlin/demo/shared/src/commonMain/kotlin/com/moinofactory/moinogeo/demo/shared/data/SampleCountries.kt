package com.moinofactory.moinogeo.demo.shared.data

import com.moinofactory.moinogeo.core.model.CountryEntity

/**
 * Sample fallback dataset when the database is empty or unavailable.
 */
val sampleCountries: List<CountryEntity> = listOf(
    CountryEntity(1, "United States", "US", "USA", "840", "1", "Washington D.C.", "USD", "$", ".us", "Americas", "Northern America", 38.0, -97.0, "🇺🇸", null),
    CountryEntity(2, "France", "FR", "FRA", "250", "33", "Paris", "EUR", "€", ".fr", "Europe", "Western Europe", 46.0, 2.0, "🇫🇷", null),
    CountryEntity(3, "Germany", "DE", "DEU", "276", "49", "Berlin", "EUR", "€", ".de", "Europe", "Western Europe", 51.0, 9.0, "🇩🇪", null),
    CountryEntity(4, "Japan", "JP", "JPN", "392", "81", "Tokyo", "JPY", "¥", ".jp", "Asia", "Eastern Asia", 36.0, 138.0, "🇯🇵", null),
    CountryEntity(5, "Brazil", "BR", "BRA", "076", "55", "Brasília", "BRL", "R$", ".br", "Americas", "South America", -10.0, -55.0, "🇧🇷", null),
    CountryEntity(6, "United Kingdom", "GB", "GBR", "826", "44", "London", "GBP", "£", ".uk", "Europe", "Northern Europe", 54.0, -2.0, "🇬🇧", null),
    CountryEntity(7, "Canada", "CA", "CAN", "124", "1", "Ottawa", "CAD", "$", ".ca", "Americas", "Northern America", 60.0, -95.0, "🇨🇦", null),
    CountryEntity(8, "Australia", "AU", "AUS", "036", "61", "Canberra", "AUD", "$", ".au", "Oceania", "Australia and New Zealand", -27.0, 133.0, "🇦🇺", null),
    CountryEntity(9, "Italy", "IT", "ITA", "380", "39", "Rome", "EUR", "€", ".it", "Europe", "Southern Europe", 42.8, 12.8, "🇮🇹", null),
    CountryEntity(10, "Spain", "ES", "ESP", "724", "34", "Madrid", "EUR", "€", ".es", "Europe", "Southern Europe", 40.0, -4.0, "🇪🇸", null),
    CountryEntity(11, "India", "IN", "IND", "356", "91", "New Delhi", "INR", "₹", ".in", "Asia", "Southern Asia", 20.0, 77.0, "🇮🇳", null),
    CountryEntity(12, "China", "CN", "CHN", "156", "86", "Beijing", "CNY", "¥", ".cn", "Asia", "Eastern Asia", 35.0, 105.0, "🇨🇳", null),
    CountryEntity(13, "South Korea", "KR", "KOR", "410", "82", "Seoul", "KRW", "₩", ".kr", "Asia", "Eastern Asia", 37.0, 127.5, "🇰🇷", null),
    CountryEntity(14, "Mexico", "MX", "MEX", "484", "52", "Mexico City", "MXN", "$", ".mx", "Americas", "Central America", 23.0, -102.0, "🇲🇽", null),
    CountryEntity(15, "South Africa", "ZA", "ZAF", "710", "27", "Pretoria", "ZAR", "R", ".za", "Africa", "Southern Africa", -29.0, 24.0, "🇿🇦", null),
    CountryEntity(16, "Netherlands", "NL", "NLD", "528", "31", "Amsterdam", "EUR", "€", ".nl", "Europe", "Western Europe", 52.5, 5.75, "🇳🇱", null),
    CountryEntity(17, "Switzerland", "CH", "CHE", "756", "41", "Bern", "CHF", "CHF", ".ch", "Europe", "Western Europe", 47.0, 8.0, "🇨🇭", null),
    CountryEntity(18, "Argentina", "AR", "ARG", "032", "54", "Buenos Aires", "ARS", "$", ".ar", "Americas", "South America", -34.0, -64.0, "🇦🇷", null),
    CountryEntity(19, "Sweden", "SE", "SWE", "752", "46", "Stockholm", "SEK", "kr", ".se", "Europe", "Northern Europe", 62.0, 15.0, "🇸🇪", null),
    CountryEntity(20, "Ukraine", "UA", "UKR", "804", "380", "Kyiv", "UAH", "₴", ".ua", "Europe", "Eastern Europe", 49.0, 32.0, "🇺🇦", null),
)
