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
7. [Work Package 5: Swift Package & Darwin Target (`darwin/`) — ⏸️ DEFERRED](#7-work-package-5-swift-package--darwin-target--️-deferred)
8. [Work Package 6: Android Demo / Harness App (`android/app`)](#8-work-package-6-android-demo--harness-app)
9. [Work Package 7: Testing, CI/CD & Build Validation](#9-work-package-7-testing-cicd--build-validation)

---

## 1. Executive Summary & Status

The repository provides documentation and initial skeleton code across Kotlin multiplatform modules, Android test app, and Python dataset processing scripts. However, there are significant gaps between the architectural documentation (`docs/*.md`) and the actual code:
* **`darwin/` directory is empty (expected — deferred):** Swift/iOS implementation is intentionally postponed. Current focus is Android / Compose Multiplatform (CMP).
* **`data/` directory is empty:** This directory is intended to store **raw external source datasets** (e.g. upstream JSON dumps from `dr5hn/countries-states-cities-database`, raw SVGs from flag icon repositories, GeoJSON boundary files from Natural Earth). The build scripts in `scripts/` then process these raw sources to generate platform-specific outputs (Room `.db`, Swift code, Kotlin code, copied and optimized assets for Android/CMP/iOS). The raw source files have not been committed yet.
* **Schema & DAO Mismatches:** The `translations` table exists in Room entities (`TranslationEntity`), but `scripts/build_database.py` never creates or populates it, and no `TranslationDao` exists.
* **Flag Icon Sets — Swappable Module Architecture Missing:** The flags module needs to support **easily swappable icon sets**. There should be a core flags abstraction module plus separate, independently selectable icon-set modules for:
  * **lipis/flag-icons** (`lipis`) — rectangular 4×3 / 1×1 SVG flags
  * **flagpack-core** (`Yummygum`) — stylized rounded-rectangle flag icons
  * **circle-flags** (`HatScripts`) — circular flag icons
* **Incomplete Features in Visual Modules:**
  * `kotlin/flags`: Missing SVG decoder configuration (only uses Coil without `SvgDecoder.Factory()`), missing size/caching parameters, and non-working default asset path.
  * `kotlin/maps`: `BoundaryProvider` only provides a single stub string method; no arc decoding algorithm, no polygon extraction, no projection handling, and no Compose canvas rendering.
* **Untracked Core Files:** Gradle root configuration and Kotlin modules are untracked by Git.

---

## 2. Critical Issues & Immediate Blockers

| ID | Module / Area | Severity | Summary |
| :--- | :--- | :--- | :--- |
| **CRIT-01** | `kotlin/flags` | High | Flag module needs a **swappable icon-set architecture**: a core abstraction layer plus separate modules for lipis/flag-icons, flagpack-core (Yummygum), and circle-flags (HatScripts). Currently only a single hardcoded provider exists. |
| **CRIT-02** | `data/` & `scripts/` | High | `scripts/build_database.py` does not create `translations` table nor load `data/translations/` JSON files into normalized rows. The `data/` directory (meant for raw upstream source datasets) is empty and needs to be populated before scripts can run. |
| **CRIT-03** | `kotlin/core` | High | No DAO or query methods exist for translations, nor unified localized query wrappers. |
| **CRIT-04** | `kotlin/maps` | Medium-High | TopoJSON model is minimal (`TopoJsonModel.kt`); lacks arc dequantization and polygon conversion logic described in `docs/maps.md`. |
| **CRIT-05** | `kotlin/flags` | Medium | `FlagIcon` uses Coil `AsyncImage` without registering `SvgDecoder.Factory()` or handling vector lifecycle; default asset path `file:///android_asset/flags` fails unless bundled. |
| **CRIT-06** | `build.gradle.kts` | Medium | Gradle daemon JVM requires Java 17+ configured; untracked files need version-controlled baseline. |
| ~~CRIT-07~~ | `darwin/` | Deferred | Swift/iOS target is intentionally postponed. Focus is on Android / CMP first. |

---

## 3. Work Package 1: Data Pipeline & Build Scripts

**Goal:** Populate `data/` with raw upstream source datasets and ensure the build scripts process them into platform-specific outputs (Room `.db`, optimized SVG assets, TopoJSON files) ready for Android/CMP integration.

> **Architecture note:** `data/` is the **raw source data directory** — it stores upstream JSON dumps, raw SVG flag icons, and GeoJSON boundary files. The `scripts/` pipeline processes these raw sources to generate:
> - `moinogeo.db` (SQLite / Room database for Android/CMP)
> - Optimized `flags/*.svg` assets ready for bundling
> - Quantized `world-50m.topojson` for map overlays
> - (Future) Swift-native code/assets for iOS

### Tasks:
- [ ] **WP1.0: Populate `data/` with Raw Upstream Sources:**
  - Download and commit raw source datasets into `data/`:
    - `data/countries+states+cities.json` (or separate `countries.json`, `states.json`, `cities.json`) from `dr5hn/countries-states-cities-database`.
    - `data/raw-flags/` — raw SVG flag files from the upstream flag icon repositories (lipis, flagpack, circle-flags).
    - `data/raw-boundaries/` — raw GeoJSON boundary file from Natural Earth.
    - `data/translations/` — translation JSON files organized by entity type and language code.
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

**Goal:** Architect a **swappable flag icon-set system** where a core abstraction module defines the provider interface and Compose components, and separate icon-set modules supply the actual flag assets. Developers pick one (or more) icon-set modules at dependency time; the core renders whichever set is provided.

### Module Structure:
```
kotlin/flags/          → core abstraction (FlagAssetProvider interface, FlagIcon composable, Coil SVG config)
kotlin/flags-lipis/    → icon-set module: lipis/flag-icons (rectangular 4×3 / 1×1 SVGs)
kotlin/flags-flagpack/ → icon-set module: flagpack-core by Yummygum (stylized rounded-rectangle icons)
kotlin/flags-circle/   → icon-set module: circle-flags by HatScripts (circular flag icons)
```

### Tasks:
- [ ] **WP3.1: Configure SVG Decoder for Coil:**
  - In `kotlin/flags` (core), ensure Coil's `SvgDecoder` is properly configured or provided via `ImageLoader` / `LocalPlatformContext`.
- [ ] **WP3.2: Refactor `FlagAssetProvider` into Core Abstraction:**
  - `kotlin/flags` provides the `FlagAssetProvider` interface, `FlagIcon` composable, and default error/placeholder handling.
  - Support fallback flags (e.g. unknown/placeholder flag for missing ISO codes).
  - The core module must **not** bundle any flag assets itself — assets come from icon-set modules.
- [ ] **WP3.3: Create Icon-Set Module — lipis/flag-icons (`kotlin/flags-lipis`):**
  - New Gradle module `kotlin/flags-lipis` providing `LipisFlagAssetProvider` that resolves bundled SVGs from `lipis/flag-icons`.
  - Add `scripts/` step (or extend `optimize_flags.py`) to download/process lipis SVGs into `data/raw-flags/lipis/` → optimized output.
  - Register in `settings.gradle.kts` as `:flags-lipis`.
- [ ] **WP3.4: Create Icon-Set Module — flagpack-core (`kotlin/flags-flagpack`):**
  - New Gradle module `kotlin/flags-flagpack` providing `FlagpackAssetProvider` resolving Yummygum's flagpack-core icons.
  - Register in `settings.gradle.kts` as `:flags-flagpack`.
- [ ] **WP3.5: Create Icon-Set Module — circle-flags (`kotlin/flags-circle`):**
  - New Gradle module `kotlin/flags-circle` providing `CircleFlagAssetProvider` resolving HatScripts' circle-flags icons.
  - Register in `settings.gradle.kts` as `:flags-circle`.
- [ ] **WP3.6: Composable Flag Enhancements:**
  - Support custom sizing, aspect ratio constraints (standard 4:3 or 3:2 or 1:1 circle), shape clipping (rounded, circular, flag badge), and placeholder/error composable slots.

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

## 7. Work Package 5: Swift Package & Darwin Target (`darwin/`) — ⏸️ DEFERRED

**Status:** Intentionally deferred. Current focus is Android / Compose Multiplatform (CMP). This work package is kept for future reference but is **not in scope** for the current development phase.

**Goal (future):** Create the native Apple / Swift implementation following `docs/integration-apple.md`.

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
    1. Country / State / City Explorer with Flag Icons (demonstrate switching between lipis, flagpack, and circle-flags icon sets at runtime).
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
    - Python: `pytest` or syntax check on `scripts/`.
    - (Future, when Darwin is in scope) Swift: `swift test` inside `darwin/`.
