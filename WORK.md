# MoinoGeo Development Roadmap & Work Items

This document outlines all findings, discrepancies, bugs, and pending implementation work identified during the review of the initial code draft. It is structured into actionable, prioritized work packages for developer agents.

---

## 📋 Table of Contents
1. [Executive Summary & Status](#1-executive-summary--status)
2. [Critical Issues & Immediate Blockers](#2-critical-issues--immediate-blockers)
3. [Work Package 1: Data Pipeline & Build Scripts (`scripts/`, `data/`)](#3-work-package-1-data-pipeline--build-scripts)
4. [Work Package 2: Kotlin Multiplatform & Android Core (`kotlin/core`)](#4-work-package-2-kotlin-multiplatform--android-core)
5. [Work Package 3: Flags Extension Module (`kotlin/flags`)](#5-work-package-3-flags-extension-module)
6. [Work Package 4: Maps & TopoJSON Extension Module (`kotlin/maps`)](#6-work-package-4-maps--topojson-extension-module)
7. [Work Package 5: Swift Package & Darwin Target (`darwin/`)](#7-work-package-5-swift-package--darwin-target)
8. [Work Package 6: Android Demo / Harness App (`android/app`)](#8-work-package-6-android-demo--harness-app)
9. [Work Package 7: Testing, CI/CD & Build Validation](#9-work-package-7-testing-cicd--build-validation)

---

## 1. Executive Summary & Status

The repository provides documentation and initial skeleton code across Kotlin multiplatform modules, Android test app, and Python dataset processing scripts. However, there are significant gaps between the architectural documentation (`docs/*.md`) and the actual code:
* **`darwin/` directory is completely empty:** No Swift Package (`Package.swift`), no GRDB/SQLite3 query engine, no SwiftUI components, and no tests despite being a primary stated deliverable.
* **`data/` directory is empty:** Neither `moinogeo.db`, `flags/*.svg`, `world-50m.topojson`, nor `data/translations/` exist in the repository.
* **Schema & DAO Mismatches:** The `translations` table exists in Room entities (`TranslationEntity`), but `scripts/build_database.py` never creates or populates it, and no `TranslationDao` exists.
* **Incomplete Features in Visual Modules:**
  * `kotlin/flags`: Missing SVG decoder configuration (only uses Coil without `SvgDecoder.Factory()`), missing size/caching parameters, and non-working default asset path.
  * `kotlin/maps`: `BoundaryProvider` only provides a single stub string method; no arc decoding algorithm, no polygon extraction, no projection handling, and no Compose canvas rendering.
* **Untracked Core Files:** Gradle root configuration and Kotlin modules are untracked by Git.

---

## 2. Critical Issues & Immediate Blockers

| ID | Module / Area | Severity | Summary |
| :--- | :--- | :--- | :--- |
| **CRIT-01** | `darwin/` | High | Swift Package Manager target completely missing (no `Package.swift`, no Swift code). |
| **CRIT-02** | `data/` & `scripts/` | High | `scripts/build_database.py` does not create `translations` table nor load `data/translations/` JSON files into normalized rows. |
| **CRIT-03** | `kotlin/core` | High | No DAO or query methods exist for translations, nor unified localized query wrappers. |
| **CRIT-04** | `kotlin/maps` | Medium-High | TopoJSON model is minimal (`TopoJsonModel.kt`); lacks arc dequantization and polygon conversion logic described in `docs/maps.md`. |
| **CRIT-05** | `kotlin/flags` | Medium | `FlagIcon` uses Coil `AsyncImage` without registering `SvgDecoder.Factory()` or handling vector lifecycle; default asset path `file:///android_asset/flags` fails unless bundled. |
| **CRIT-06** | `build.gradle.kts` | Medium | Gradle daemon JVM requires Java 17+ configured; untracked files need version-controlled baseline. |

---

## 3. Work Package 1: Data Pipeline & Build Scripts

**Goal:** Ensure the database generation script creates all tables specified in `docs/schema.md` and `docs/localization.md`, including normalized translation records.

### Tasks:
- [ ] **WP1.1: Add `translations` Table to `scripts/build_database.py`:**
  - Update `create_schema()` in `scripts/build_database.py` to add:
    ```sql
    CREATE TABLE IF NOT EXISTS translations (
        entity_type  TEXT NOT NULL,
        entity_id    INTEGER NOT NULL,
        lang_code    TEXT NOT NULL,
        name         TEXT NOT NULL,
        PRIMARY KEY (entity_type, entity_id, lang_code)
    );
    CREATE INDEX IF NOT EXISTS idx_translations_lookup ON translations(entity_type, entity_id, lang_code);
    ```
- [ ] **WP1.2: Implement `data/translations/` parsing in `scripts/build_database.py`:**
  - Parse existing `translations` JSON column on countries and insert normalized rows into `translations` table with `entity_type = 'country'`.
  - Add support for reading external translation files from `data/translations/{regions,countries,states}/*.json` (`en`, `fr`, `es`, `it`, `de`, `uk`, `ru`).
- [ ] **WP1.3: Update `scripts/convert_topojson.py`:**
  - Ensure the fallback conversion preserves `id`, `iso_a2`, `iso_a3`, `name` properties.
  - Add validation step checking that geometry arcs are properly delta-encoded.
- [ ] **WP1.4: Add Database Verification / Smoke Test Script:**
  - Create `scripts/verify_database.py` to assert row counts, foreign key integrity (`PRAGMA foreign_key_check`), and translation lookup benchmarks.

---

## 4. Work Package 2: Kotlin Multiplatform & Android Core (`kotlin/core`)

**Goal:** Provide a complete Room / SQLite query engine with multi-language search and paging capabilities.

### Tasks:
- [ ] **WP2.1: Implement `TranslationDao`:**
  - Create `kotlin/core/src/commonMain/kotlin/com/moinofactory/moinogeo/core/dao/TranslationDao.kt`:
    - `getTranslation(entityType: String, entityId: Long, langCode: String): String?`
    - `getTranslationsForEntity(entityType: String, entityId: Long): List<TranslationEntity>`
    - `getTranslationsByLanguage(entityType: String, langCode: String): List<TranslationEntity>`
  - Register `translationDao()` in `MoinoGeoDatabase.kt`.
- [ ] **WP2.2: Add Localized Queries & Joins to Existing DAOs:**
  - `CountryDao`:
    - Add localized country lookup joining `translations` table by `lang_code` with fallback to English `name`.
    - Add pagination support or return Flow / Coroutine streams.
    - Add `getCountryById(id: Long): CountryEntity?` and `getCountryByIso3(iso3: String): CountryEntity?`.
  - `StateDao`:
    - Add `searchStates(query: String, countryCode: String?): List<StateEntity>`.
  - `CityDao`:
    - Add indexed coordinate bounding box search:
      `getCitiesInBoundingBox(minLat: Double, maxLat: Double, minLng: Double, maxLng: Double, limit: Int): List<CityEntity>`.
- [ ] **WP2.3: Repository Layer Abstraction:**
  - Provide a clean, idiomatic `MoinoGeoRepository` or `MoinoGeoEngine` interface that wraps raw Room DAOs, handles language fallbacks automatically, and manages database initialization across Android and iOS (KMP).

---

## 5. Work Package 3: Flags Extension Module (`kotlin/flags`)

**Goal:** Ensure SVG vector flags load reliably in Compose Multiplatform / Android with caching and pluggable providers.

### Tasks:
- [ ] **WP3.1: Configure SVG Decoder for Coil:**
  - In `kotlin/flags`, ensure Coil's `SvgDecoder` is properly configured or provided via `ImageLoader` / `LocalPlatformContext`.
- [ ] **WP3.2: Enhance `FlagAssetProvider`:**
  - Support fallback flags (e.g. unknown/placeholder flag for missing ISO codes).
  - Provide standard implementations:
    - `DefaultAssetFlagProvider(assetSubdirectory: String = "flags")`
    - `RemoteCdnFlagProvider(baseUrl: String)`
    - `ResourceFlagProvider(resIdMap: Map<String, Int>)`
- [ ] **WP3.3: Composable Flag Enhancements:**
  - Support custom sizing, aspect ratio constraints (standard 4:3 or 3:2), shape clipping (rounded, circular, flag badge), and placeholder/error composable slots.

---

## 6. Work Package 4: Maps & TopoJSON Extension Module (`kotlin/maps`)

**Goal:** Implement full TopoJSON decoding and polygon extraction in Kotlin Multiplatform.

### Tasks:
- [ ] **WP4.1: Expand TopoJSON Model:**
  - Support Geometry objects (`Polygon`, `MultiPolygon`, `GeometryCollection`).
  - Add property models (extracting `iso_a2`, `iso_a3`, `name`).
- [ ] **WP4.2: Implement Arc Dequantization & Delta Decoder:**
  - Port the arc-to-coordinates algorithm specified in `docs/maps.md` (section 3) to pure Kotlin in `commonMain`:
    - Decode bitwise inverted arc indices (negative arc indices).
    - Apply delta decoding: `x += dx`, `y += dy`.
    - Apply scale and translate transforms: `lng = x * sx + tx`, `lat = y * sy + ty`.
- [ ] **WP4.3: Implement `BoundaryProvider` & Repository:**
  - Support reading from stream/asset (`openBoundaryStream()`).
  - Cache decoded country boundary polygons in memory for instant map overlay queries.
- [ ] **WP4.4: Add Basic Compose Canvas Renderer / GeoPath:**
  - Add a lightweight Compose Multiplatform component to render country polygons onto a Compose `Canvas` or export as Android/Compose `Path`.

---

## 7. Work Package 5: Swift Package & Darwin Target (`darwin/`)

**Goal:** Create the native Apple / Swift implementation following `docs/integration-apple.md`.

### Tasks:
- [ ] **WP5.1: Create `darwin/Package.swift`:**
  - Define products: `MoinoGeo` (umbrella), `MoinoGeoCore`, `MoinoGeoFlags`, `MoinoGeoMaps`.
  - Target iOS 15+ and macOS 12+.
  - Integrate GRDB or native `SQLite3`.
- [ ] **WP5.2: Implement `MoinoGeoCore` (Swift):**
  - Database manager with `moinogeo.db` bundle resolution (`MoinoGeoBundle.swift`).
  - Record models: `Country`, `State`, `City`, `Translation`.
  - Swift query API with async/await and localized lookups.
- [ ] **WP5.3: Implement `MoinoGeoFlags` (Swift):**
  - `FlagProvider` protocol and `DefaultFlagProvider` reading SVGs from bundle.
  - SwiftUI `FlagView` component using native `Image` / vector rendering or SVG parser.
- [ ] **WP5.4: Implement `MoinoGeoMaps` (Swift):**
  - TopoJSON arc decoder and polygon extraction in Swift.
  - MapKit integration (`loadCountryPolygon(iso2:) -> [MKPolygon]`).
- [ ] **WP5.5: Add Unit Tests for Swift Targets:**
  - Validate database read performance, ISO2/ISO3 lookups, and TopoJSON polygon decoding.

---

## 8. Work Package 6: Android Demo / Harness App (`android/app`)

**Goal:** Update `MainActivity.kt` into a functional showcase for all three modules.

### Tasks:
- [ ] **WP6.1: Update Database Builder & Fallback:**
  - Handle missing asset gracefully with informative UI error state.
  - Use `MoinoGeoDatabase.getInstance(context)`.
- [ ] **WP6.2: Showcase Visual Extensions:**
  - Add tabs/screens:
    1. Country / State / City Explorer with Flag Icons.
    2. Interactive Map Boundary Preview using `kotlin/maps`.
    3. Multi-language toggle (`en`, `fr`, `es`, `de`, `it`, `uk`, `ru`) to demonstrate localized country/city names.

---

## 9. Work Package 7: Testing, CI/CD & Build Validation

**Goal:** Ensure automated testing and CI verification across all modules.

### Tasks:
- [ ] **WP7.1: Git Hygiene & Tracking:**
  - Add untracked files (`kotlin/`, `android/`, `build.gradle.kts`, `settings.gradle.kts`, `gradle/libs.versions.toml`) to git staging.
  - Update `.gitignore` to exclude build artifacts, `.gradle`, `.idea`, and local properties while keeping source code and Gradle wrappers tracked.
- [ ] **WP7.2: Unit Tests for Kotlin Modules:**
  - Add tests in `kotlin/core/src/commonTest` for entities, serializations, and mock DAO queries.
  - Add tests in `kotlin/maps/src/commonTest` verifying TopoJSON arc decoding against known test coordinates.
- [ ] **WP7.3: CI Pipeline Setup:**
  - Create `.github/workflows/ci.yml` running:
    - Kotlin/Android: `./gradlew check test`
    - Swift: `swift test` (inside `darwin/`)
    - Python: `pytest` or syntax check on `scripts/`.
