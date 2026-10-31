package com.moinofactory.moinogeo.core.domain

import com.moinofactory.moinogeo.core.data.MoinoGeoDatabase
import com.moinofactory.moinogeo.core.data.mapper.toDomain
import com.moinofactory.moinogeo.core.data.mapper.toGeoPlace
import com.moinofactory.moinogeo.core.domain.model.City
import com.moinofactory.moinogeo.core.domain.model.Country
import com.moinofactory.moinogeo.core.domain.model.GeoPlace
import com.moinofactory.moinogeo.core.domain.model.GeoPlaceType
import com.moinofactory.moinogeo.core.domain.model.Region
import com.moinofactory.moinogeo.core.domain.model.State
import com.moinofactory.moinogeo.core.domain.model.Subregion

/**
 * Public entry point for geographic lookups against the bundled regions database.
 */
class MoinoGeoService internal constructor(
    private val db: MoinoGeoDatabase,
) {
    private val regions get() = db.regionDao()
    private val subregions get() = db.subregionDao()
    private val countries get() = db.countryDao()
    private val states get() = db.stateDao()
    private val cities get() = db.cityDao()
    private val translations get() = db.translationDao()

    suspend fun getRegions(): List<Region> = regions.getAll().map { it.toDomain() }

    suspend fun getRegion(id: Long): Region? = regions.getById(id)?.toDomain()

    suspend fun getSubregions(regionId: Long? = null): List<Subregion> {
        val rows = if (regionId == null) {
            subregions.getAll()
        } else {
            subregions.getByRegionId(regionId)
        }
        return rows.map { it.toDomain() }
    }

    suspend fun getSubregion(id: Long): Subregion? = subregions.getById(id)?.toDomain()

    suspend fun getCountries(): List<Country> = countries.getAll().map { it.toDomain() }

    suspend fun getCountry(id: Long): Country? = countries.getById(id)?.toDomain()

    suspend fun getCountryByIso2(iso2: String): Country? =
        countries.getByIso2(iso2.uppercase()).toDomainOrNull()

    suspend fun getCountryByIso3(iso3: String): Country? =
        countries.getByIso3(iso3.uppercase()).toDomainOrNull()

    suspend fun getCountriesByRegion(regionId: Long): List<Country> =
        countries.getByRegionId(regionId).map { it.toDomain() }

    suspend fun getCountriesBySubregion(subregionId: Long): List<Country> =
        countries.getBySubregionId(subregionId).map { it.toDomain() }

    suspend fun getState(id: Long): State? = states.getById(id)?.toDomain()

    suspend fun getStatesByCountry(countryId: Long): List<State> =
        states.getByCountryId(countryId).map { it.toDomain() }

    suspend fun getCity(id: Long): City? = cities.getById(id)?.toDomain()

    suspend fun getCitiesByState(stateId: Long): List<City> =
        cities.getByStateId(stateId).map { it.toDomain() }

    suspend fun getCitiesByCountry(
        countryId: Long,
        limit: Int = 100,
        offset: Int = 0,
    ): List<City> = cities.getByCountryId(countryId, limit, offset).map { it.toDomain() }

    suspend fun getCountryTranslations(countryId: Long): Map<String, String> =
        translations.getForEntity(ENTITY_COUNTRY, countryId).associate { it.langCode to it.name }

    suspend fun getCountryTranslation(countryId: Long, langCode: String): String? =
        translations.getOne(ENTITY_COUNTRY, countryId, langCode)?.name

    /**
     * Search countries by English name, ISO codes, and optional translated names.
     */
    suspend fun searchCountries(
        query: String,
        langCode: String? = null,
        limit: Int = 50,
    ): List<Country> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return emptyList()

        val byName = countries.searchByNameOrCode(trimmed, limit).map { it.toDomain() }
        if (langCode == null && byName.size >= limit) return byName

        val byTranslation = countries.searchByTranslation(trimmed, langCode, limit)
            .map { it.toDomain() }

        return (byName + byTranslation)
            .distinctBy { it.id }
            .take(limit)
    }

    suspend fun searchCountriesByCapital(query: String, limit: Int = 50): List<Country> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return emptyList()
        return countries.searchByCapital(trimmed, limit).map { it.toDomain() }
    }

    suspend fun searchStates(
        query: String,
        countryId: Long? = null,
        limit: Int = 50,
    ): List<State> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return emptyList()
        val rows = if (countryId == null) {
            states.searchByNameOrCode(trimmed, limit)
        } else {
            states.searchInCountry(countryId, trimmed, limit)
        }
        return rows.map { it.toDomain() }
    }

    suspend fun searchCities(
        query: String,
        countryId: Long? = null,
        stateId: Long? = null,
        limit: Int = 50,
    ): List<City> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return emptyList()
        val rows = when {
            stateId != null -> cities.searchInState(stateId, trimmed, limit)
            countryId != null -> cities.searchInCountry(countryId, trimmed, limit)
            else -> cities.searchByName(trimmed, limit)
        }
        return rows.map { it.toDomain() }
    }

    /**
     * Cross-type search returning [GeoPlace] hits for pickers and typeahead UIs.
     */
    suspend fun searchPlaces(
        query: String,
        types: Set<GeoPlaceType> = GeoPlaceType.entries.toSet(),
        langCode: String? = null,
        limit: Int = 50,
    ): List<GeoPlace> {
        val trimmed = query.trim()
        if (trimmed.isEmpty() || types.isEmpty()) return emptyList()

        val perType = (limit / types.size).coerceAtLeast(5)
        val results = mutableListOf<GeoPlace>()

        if (GeoPlaceType.REGION in types) {
            results += regions.searchByName(trimmed, perType).map { it.toGeoPlace() }
        }
        if (GeoPlaceType.SUBREGION in types) {
            results += subregions.searchByName(trimmed, perType).map { it.toGeoPlace() }
        }
        if (GeoPlaceType.COUNTRY in types) {
            results += searchCountries(trimmed, langCode, perType).map { it.toGeoPlace() }
        }
        if (GeoPlaceType.STATE in types) {
            results += searchStates(trimmed, limit = perType).map { it.toGeoPlace() }
        }
        if (GeoPlaceType.CITY in types) {
            results += searchCities(trimmed, limit = perType).map { it.toGeoPlace() }
        }

        return results.take(limit)
    }

    private fun com.moinofactory.moinogeo.core.data.entity.CountryEntity?.toDomainOrNull() =
        this?.toDomain()

    private fun Country.toGeoPlace() = GeoPlace(
        type = GeoPlaceType.COUNTRY,
        id = id,
        name = name,
        iso2 = iso2,
        iso3 = iso3,
        latitude = latitude,
        longitude = longitude,
        regionId = regionId,
        subregionId = subregionId,
        countryId = id,
    )

    private fun State.toGeoPlace() = GeoPlace(
        type = GeoPlaceType.STATE,
        id = id,
        name = name,
        iso2 = iso2,
        latitude = latitude,
        longitude = longitude,
        countryId = countryId,
        stateId = id,
    )

    private fun City.toGeoPlace() = GeoPlace(
        type = GeoPlaceType.CITY,
        id = id,
        name = name,
        latitude = latitude,
        longitude = longitude,
        countryId = countryId,
        stateId = stateId,
    )

    private companion object {
        const val ENTITY_COUNTRY = "country"
    }
}
