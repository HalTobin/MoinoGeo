package com.moinofactory.moinogeo.core.data.mapper

import com.moinofactory.moinogeo.core.data.entity.CityEntity
import com.moinofactory.moinogeo.core.data.entity.CountryEntity
import com.moinofactory.moinogeo.core.data.entity.RegionEntity
import com.moinofactory.moinogeo.core.data.entity.StateEntity
import com.moinofactory.moinogeo.core.data.entity.SubregionEntity
import com.moinofactory.moinogeo.core.domain.model.City
import com.moinofactory.moinogeo.core.domain.model.Country
import com.moinofactory.moinogeo.core.domain.model.GeoPlace
import com.moinofactory.moinogeo.core.domain.model.GeoPlaceType
import com.moinofactory.moinogeo.core.domain.model.Region
import com.moinofactory.moinogeo.core.domain.model.State
import com.moinofactory.moinogeo.core.domain.model.Subregion

internal fun RegionEntity.toDomain() = Region(id = id, name = name)

internal fun SubregionEntity.toDomain() = Subregion(
    id = id,
    name = name,
    regionId = regionId,
)

internal fun CountryEntity.toDomain() = Country(
    id = id,
    name = name,
    iso2 = iso2,
    iso3 = iso3,
    capital = capital,
    regionId = regionId,
    subregionId = subregionId,
    latitude = latitude,
    longitude = longitude,
)

internal fun StateEntity.toDomain() = State(
    id = id,
    countryId = countryId,
    name = name,
    iso2 = iso2,
    iso3166_2 = iso3166_2,
    type = type,
    latitude = latitude,
    longitude = longitude,
)

internal fun CityEntity.toDomain() = City(
    id = id,
    stateId = stateId,
    countryId = countryId,
    name = name,
    latitude = latitude,
    longitude = longitude,
)

internal fun RegionEntity.toGeoPlace() = GeoPlace(
    type = GeoPlaceType.REGION,
    id = id,
    name = name,
    regionId = id,
)

internal fun SubregionEntity.toGeoPlace() = GeoPlace(
    type = GeoPlaceType.SUBREGION,
    id = id,
    name = name,
    regionId = regionId,
    subregionId = id,
)

internal fun CountryEntity.toGeoPlace() = GeoPlace(
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

internal fun StateEntity.toGeoPlace() = GeoPlace(
    type = GeoPlaceType.STATE,
    id = id,
    name = name,
    iso2 = iso2,
    latitude = latitude,
    longitude = longitude,
    countryId = countryId,
    stateId = id,
)

internal fun CityEntity.toGeoPlace() = GeoPlace(
    type = GeoPlaceType.CITY,
    id = id,
    name = name,
    latitude = latitude,
    longitude = longitude,
    countryId = countryId,
    stateId = stateId,
)
