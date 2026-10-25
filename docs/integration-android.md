# Android Integration Guide

This guide details how to integrate MoinoGeo into Android applications using Kotlin, Gradle, and Android Room Persistence Library.

---

## 1. Directory Layout & Asset Setup

Place the pre-compiled database and assets into `app/src/main/assets/`:

```
app/src/main/
└── assets/
    ├── databases/
    │   └── moinogeo.db
    ├── geo/
    │   └── world-50m.topojson
    └── flags/
        ├── us.svg
        ├── de.svg
        ├── fr.svg
        └── ...
```

---

## 2. Room Database Setup

Pre-populate Android Room database from bundled `assets/databases/moinogeo.db`:

```kotlin
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [CountryEntity::class, StateEntity::class, CityEntity::class, TranslationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MoinoGeoDatabase : RoomDatabase() {
    abstract fun countryDao(): CountryDao
    abstract fun stateDao(): StateDao
    abstract fun cityDao(): CityDao

    companion object {
        @Volatile
        private var INSTANCE: MoinoGeoDatabase? = null

        fun getInstance(context: Context): MoinoGeoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MoinoGeoDatabase::class.java,
                    "moinogeo.db"
                )
                .createFromAsset("databases/moinogeo.db")
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```

---

## 3. Query Examples with DAOs

### CountryDao & CityDao Definitions
```kotlin
// CountryDao.kt
@Dao
interface CountryDao {
    @Query("SELECT * FROM countries WHERE iso2 = :iso2 LIMIT 1")
    suspend fun getCountryByIso2(iso2: String): CountryEntity?

    @Query("""
        SELECT c.id, c.iso2, COALESCE(t.name, c.name) AS name, c.capital, c.phone_code 
        FROM countries c 
        LEFT JOIN translations t 
               ON t.entity_type = 'country' AND t.entity_id = c.id AND t.lang_code = :langCode 
        WHERE c.region = :region 
        ORDER BY name ASC
    """)
    suspend fun getCountriesByRegionLocalized(region: String, langCode: String): List<LocalizedCountry>
}

// CityDao.kt
@Dao
interface CityDao {
    @Query("""
        SELECT * FROM cities 
        WHERE country_code = :countryCode AND name LIKE :query || '%' 
        ORDER BY name ASC 
        LIMIT :limit OFFSET :offset
    """)
    suspend fun searchCitiesInCountry(
        countryCode: String, 
        query: String, 
        limit: Int = 20, 
        offset: Int = 0
    ): List<CityEntity>
}
```

### Usage in Repository
```kotlin
suspend fun loadJapaneseCities(context: Context) {
    val db = MoinoGeoDatabase.getInstance(context)
    val japan = db.countryDao().getCountryByIso2("JP")
    println("Country: ${japan?.name}, Capital: ${japan?.capital}, Dial Code: ${japan?.phoneCode}")

    val tokioCities = db.cityDao().searchCitiesInCountry(countryCode = "JP", query = "Tok")
    tokioCities.forEach { city ->
        println("City: ${city.name} (Lat: ${city.latitude}, Lng: ${city.longitude})")
    }
}
```
