# Architecture & Design Philosophy

MoinoGeo is an offline-first geographic library engineered as a **high-performance, Kotlin- and Swift-optimized wrapper for mobile platforms**.

Rather than acting as a generic database provider or a standalone media library, MoinoGeo wraps geographic datasets (countries, states, cities, localizations) into a clean, platform-native API for Android and Apple platforms. To keep mobile binary size lean and give developers full control over visual presentation, MoinoGeo decouples its visual assets (vector flags and map border polygons) into **optional visual extension modules**.

---

## High-Level Component Diagram

```
+-----------------------------------------------------------------------------------+
|                                MoinoGeo Client                                    |
+-----------------------------------------------------------------------------------+
|   iOS / macOS (Swift)                            Android (Kotlin)                 |
|   - Swift Package Manager                        - Gradle Modules / AARs          |
|   - SQLite3 / GRDB / SwiftData                   - Room / SQLite / Driver         |
+-----------------------------------------------------------------------------------+
                                          |
                      +-------------------+-------------------+
                      |                                       |
                      v                                       v
          +-----------------------+               +-----------------------+
          |  MoinoGeo Core Data   |               |   Visual Extensions   |
          | - 150k+ Cities        |               |   (Optional Modules)  |
          | - States & Countries  |               +-----------------------+
          | - Localizations       |               /                       \
          +-----------------------+              /                         \
                      |                         v                           v
                      |              +--------------------+       +--------------------+
                      |              |  Vector Flags      |       |  Map Boundaries    |
                      |              | - Pluggable Asset  |       | - TopoJSON Decoder |
                      |              |   Provider         |       | - Pluggable        |
                      |              +--------------------+       |   Boundary Provider|
                      |                         |                 +--------------------+
                      |                         v                           |
                      v               [Custom SVG Flag Art]                 v
             [moinogeo.db SQLite]     (Default or User-Provided)  [Custom TopoJSON Asset]
                                                                 (Default or User-Provided)
```

---

## 1. Modular & Abstractable Architecture

MoinoGeo wraps geographic relational data and provides optional visual asset layers that developers can attach or omit based on their app's specific feature set:

### A. Core Dataset & Visual Extension Modules
* **Core Geographic Data (`moinogeo-core`):** The primary relational engine (`moinogeo.db`) containing 150,000+ cities, states, countries, and multi-language localizations (~12.5 MB). Operates completely independently without requiring vector flag icons or map boundary files.
* **Vector Flags Extension (`moinogeo-flags`):** An optional visual module (~1.8 MB) providing UI flag components and asset resolvers mapped to country ISO 3166-1 alpha-2 codes.
* **Map Boundaries Extension (`moinogeo-maps`):** An optional visual module (~2.1 MB) providing TopoJSON arc decoding and polygon map overlay rendering for country boundaries.

### B. Storage Optimization & Zero Storage Waste
Mobile developers care deeply about app binary size, download conversion rates, and cellular download limits. Because MoinoGeo is designed specifically for mobile application development, visual extensions are unbundled to prevent wasting end-user disk space and network bandwidth:

* **Core Data + Flags (No Map Borders):** If an app needs country/city search and flag icons but no map overlays, omitting `world-50m.topojson` **saves 2.1 MB** (13% binary size reduction).
* **Core Data + Map Borders (No Flags):** If an app renders map boundaries but no flag icons, omitting `flags/*.svg` **saves 1.8 MB** (11% binary size reduction).
* **Core Data Only (No Flags or Borders):** If an app only needs search, geocoding, and city lookup, embedding only `moinogeo.db` **saves 3.9 MB** (24% binary size reduction).

| Combination Selected | Modules Included | Total Storage Footprint | Savings vs Full Suite |
| :--- | :--- | :--- | :--- |
| **Full Suite** | Core Data + Flags + Maps | **16.4 MB** | Baseline (Full Feature Set) |
| **Data + Flags** | Core Data + Flags | **14.3 MB** | **Saved 2.1 MB (13%)** — Map borders omitted |
| **Data + Maps** | Core Data + Maps | **14.6 MB** | **Saved 1.8 MB (11%)** — Flag icons omitted |
| **Core Data Only** | Core Data | **12.5 MB** | **Saved 3.9 MB (24%)** — Flags & borders omitted |

### C. Aesthetic & Geopolitical Neutrality
Geopolitical boundaries, territorial demarcations, and national flags carry significant political, regional, and legal implications. Furthermore, mobile apps often require custom visual aesthetics (e.g., circular flags, custom brand palettes, 3D flags, or regional flag sets).

MoinoGeo provides **pluggable asset interfaces** so developers are never locked into default visual assets:
1. **Custom Flag Art Injection:** Developers can use MoinoGeo's Kotlin/Swift flag components while supplying their own custom asset repository (local SVG assets, custom vector sets, remote CDN URLs, or politically localized variants).
2. **Custom Boundary Injection:** Developers can swap the default `world-50m.topojson` with custom TopoJSON/GeoJSON boundary files (e.g., higher precision borders, disputed territory adjustments, or regionally compliant map boundaries).

---

## 2. Why SQLite + TopoJSON + SVG?

1. **Relational Data via SQLite:**
   - Querying 150,000+ cities and multi-level administrative hierarchies (Country → State → City) requires complex relational joins, spatial coordinate bounding queries, and full-text searches.
   - SQLite provides zero-configuration, cross-platform, embedded SQL evaluation with native C-bindings on both Apple (`libsqlite3`) and Android (`sqlite3`/Framework) operating systems.

2. **Map Boundaries via TopoJSON:**
   - GeoJSON repeats coordinate pairs for shared boundaries between adjacent geographic entities (e.g., the border between France and Spain).
   - **TopoJSON** encodes topology rather than discrete polygons. Shared boundaries are stored as single shared "arcs", eliminating coordinate redundancy and reducing raw boundary payload size by **70% to 80%** compared to GeoJSON.

3. **Flags via Vector SVG:**
   - Raster images (PNG/WEBP) require multiple density variants (`@2x`, `@3x`, `hdpi`, `xxhdpi`) or suffer from scaling artifacts on high-DPI retina mobile screens.
   - Vector SVG flags scale losslessly to any resolution while keeping the total asset footprint per flag to ~1–3 KB.

---

## 3. Storage Optimization Breakdown

Mobile binary size budgets dictate strict limits on bundled assets. MoinoGeo employs three targeted optimization strategies to keep the overall storage footprint between **12.5 MB and 16.4 MB** depending on selected modules:

| Component | Raw Unoptimized Size | MoinoGeo Optimized Size | Optimization Technique Applied | Optionality |
| :--- | :--- | :--- | :--- | :--- |
| **Relational Data** | ~85 MB (Raw JSON) | **12.5 MB** (`moinogeo.db`) | SQLite page packing, Integer PK foreign keys, VACUUM compaction, B-tree indexes. | **Core Module** |
| **Map Boundaries** | ~12 MB (GeoJSON) | **2.1 MB** (`world-50m.topojson`) | Arc topology encoding, 10,000-point coordinate quantization, delta encoding. | **Optional Extension** |
| **Flags** | ~6.5 MB (Raw SVGs) | **1.8 MB** (`flags/*.svg`) | SVGO minification: removal of XML metadata, comments, hidden elements, precision reduction. | **Optional Extension** |
| **Total Asset Footprint** | **~103.5 MB** | **~16.4 MB** | **~84% Total Compression Ratio** | Unbundlable by feature need |

---

## 4. Database Indexing Strategy

To ensure sub-millisecond query execution on mobile CPUs, B-tree indexes are built during offline database compilation:
- `idx_countries_iso2`: Direct lookup by ISO 3166-1 alpha-2 code (`iso2`).
- `idx_states_country`: Foreign key lookup retrieving all states for a specific country.
- `idx_cities_state`: Foreign key lookup retrieving cities by state.
- `idx_cities_country`: Foreign key lookup retrieving cities directly by country.
- `idx_cities_name_prefix`: Prefix index supporting autocomplete search inputs.
- `idx_translations_lookup`: Index supporting fast multi-language entity lookup by entity type, entity ID, and language code.
