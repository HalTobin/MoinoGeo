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
    │   └── moinogeo.db            [Core Module - 12.5 MB]
    ├── geo/
    │   └── world-50m.topojson     [Maps Module - 2.1 MB]   (Optional)
    └── flags-lipis/               [Pick ONE icon set, or omit & use custom provider]
        ├── us.svg                 (or flags-flagpack / flags-circle)
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

## 3. Flag Modules & Custom Asset Providers

Flags are split into a **core** module (no assets) plus optional **icon-set** modules. See [flags.md](flags.md) for the full architecture.

```kotlin
dependencies {
    implementation(projects.flags)            // FlagAssetProvider + FlagIcon
    implementation(projects.flagsLipis)       // or flagsFlagpack / flagsCircle
}
```

### Android assets (bundled SVGs)

```kotlin
import com.moinofactory.moinogeo.flags.FlagIcon
import com.moinofactory.moinogeo.flags.lipis.LipisFlagAssetProvider

@Composable
fun CountryFlag(iso2: String) {
    FlagIcon(
        iso2Code = iso2,
        provider = LipisFlagAssetProvider.androidAssets(),
    )
}
```

### Compose Multiplatform `composeResources`

```kotlin
import com.moinofactory.moinogeo.flags.ComposeResourceFlagAssetProvider
import com.moinofactory.moinogeo.flags.FlagIcon

val provider = ComposeResourceFlagAssetProvider { iso2 ->
    Res.readBytes("files/flags/$iso2.svg")
}

@Composable
fun CountryFlag(iso2: String) {
    FlagIcon(iso2Code = iso2, provider = provider)
}
```

### Custom artwork / CDN (no icon-set module)

```kotlin
import com.moinofactory.moinogeo.flags.FlagAssetProvider
import com.moinofactory.moinogeo.flags.FlagIcon
import com.moinofactory.moinogeo.flags.UriFlagAssetProvider

class CustomFlagProvider(
    private val customBaseUrl: String,
) : FlagAssetProvider by UriFlagAssetProvider(customBaseUrl)

@Composable
fun CustomFlagIcon(iso2Code: String) {
    FlagIcon(
        iso2Code = iso2Code,
        provider = CustomFlagProvider("https://cdn.example.com/flags"),
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

---

## 5. Reference Demo Application Architecture (`kotlin/demo`)

MoinoGeo includes a reference multiplatform demo harness located in `kotlin/demo/` with the following structure:
- **`kotlin/demo/shared`**: Shared Kotlin Multiplatform (KMP) and Compose Multiplatform (CMP) module containing UI screens (`MoinoGeoAppScreen`), platform-agnostic view logic, and cross-platform database helpers (`createDatabase` expect/actual).
- **`kotlin/demo/android`**: Android application target wrapping the shared demo module and bundling Android assets (`assets/databases/moinogeo.db`).
- **`kotlin/demo/jvm`**: JVM / Compose Desktop application target running the demo on desktop environments.

