package com.moinofactory.moinogeo.core.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "countries")
data class CountryEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Long,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "iso2")
    val iso2: String,
    @ColumnInfo(name = "iso3")
    val iso3: String,
    @ColumnInfo(name = "numeric_code")
    val numericCode: String?,
    @ColumnInfo(name = "phone_code")
    val phoneCode: String?,
    @ColumnInfo(name = "capital")
    val capital: String?,
    @ColumnInfo(name = "currency")
    val currency: String?,
    @ColumnInfo(name = "currency_symbol")
    val currencySymbol: String?,
    @ColumnInfo(name = "tld")
    val tld: String?,
    @ColumnInfo(name = "region")
    val region: String?,
    @ColumnInfo(name = "subregion")
    val subregion: String?,
    @ColumnInfo(name = "latitude")
    val latitude: Double?,
    @ColumnInfo(name = "longitude")
    val longitude: Double?,
    @ColumnInfo(name = "emoji")
    val emoji: String?,
    @ColumnInfo(name = "translations")
    val translations: String?
)
