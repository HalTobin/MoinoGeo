# Android Integration Guide

This guide details how to integrate MoinoGeo into Android applications using Kotlin, Gradle, Jetpack Compose, and Android Room.

MoinoGeo is designed to be **fully modular**. You can integrate core database queries, vector SVG flags, and TopoJSON map boundaries together or selectively as independent components.

---

## 1. Directory Layout & Modular Asset Options

Place pre-compiled database assets in `app/src/main/assets/`. You can include all assets or omit any unneeded directory to minimize APK size:

```
app/src/main/
└── assets/
    ├── databases/
    │   └── moinogeo.db       [Core Module - 12.5 MB] (Optional)
    ├── geo/
    │   └── world-50m.topojson [Maps Module - 2.1 MB]   (Optional)
    └── flags/
        ├── us.svg            [Flags Module - 1.8 MB]  (Optional: swap or omit)
        ├── de.svg
        └── ...
```

### Selective Asset Stripping in `build.gradle.kts`
If integrating via an AAR/library module that includes all assets by default, you can strip unneeded assets in your app's Gradle file:

```kotlin
// app/build.gradle.kts
android {
    // Exclude flags or map boundaries if not needed in your app build
    androidResources {
        noCompress += "db"
        noCompress += "topojson"
        // ignoreAssetsPattern = "!*.svg" // Example: ignore flag assets if supplying custom artwork
    }
}
```

---

## 2. Core Database Setup (Room)

Pre-populate Android Room database from bundled `assets/databases/moinogeo.db`. The core database runs independently without flags or maps:

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

## 3. Custom Flag Asset Provider Setup

To use MoinoGeo's flag rendering core with custom artwork or alternative asset locations:

```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import coil.compose.AsyncImage

// Abstract Provider
fun interface FlagAssetProvider {
    fun getFlagData(iso2Code: String): Any
}

// Custom Provider pointing to custom flag SVGs (e.g. custom directory, drawable res, or remote CDN)
class CustomFlagProvider(private val customBaseUrl: String) : FlagAssetProvider {
    override fun getFlagData(iso2Code: String): Any {
        return "$customBaseUrl/${iso2Code.lowercase()}.svg"
    }
}

// Usage in Jetpack Compose
@Composable
fun CustomFlagIcon(
    iso2Code: String,
    modifier: Modifier = Modifier,
    provider: FlagAssetProvider = CustomFlagProvider("https://cdn.example.com/flags")
) {
    AsyncImage(
        model = provider.getFlagData(iso2Code),
        contentDescription = "Flag for $iso2Code",
        modifier = modifier
    )
}
```

---

## 4. Query Examples with DAOs

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
