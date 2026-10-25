# Architecture & Design Philosophy

MoinoGeo is engineered around the principle of **zero-latency, offline-first execution**. Traditional geographic and geocoding services rely on REST APIs, introduce network latency, enforce rate limits, require API keys, and present privacy risks. MoinoGeo embeds optimized datasets directly into client application binaries.

---

## High-Level Component Diagram

```
+-------------------------------------------------------------------------+
|                            MoinoGeo Client                              |
+-------------------------------------------------------------------------+
|   iOS / macOS (Swift)                     Android (Kotlin)              |
|   - Swift Package Manager                 - Gradle Module / AAR         |
|   - SQLite3 / GRDB / SwiftData            - Room / SQLite / Driver      |
+-------------------------------------------------------------------------+
                                    |
          +-------------------------+-------------------------+
          |                         |                         |
          v                         v                         v
+-------------------+     +-------------------+     +-------------------+
| SQLite Database   |     | TopoJSON Maps     |     | Vector SVG Flags  |
| - 150k+ Cities    |     | - Natural Earth   |     | - ISO 3166-1 α-2  |
| - Localizations   |     | - Quantized Arcs  |     | - Minified SVGs   |
| - B-Tree Indexed  |     |                   |     |                   |
+-------------------+     +-------------------+     +-------------------+
```

---

## 1. Why SQLite + TopoJSON + SVG?

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

## 2. Storage Optimization Breakdown

Mobile binary size budgets dictate strict limits on bundled assets. MoinoGeo employs three targeted optimization strategies to keep the overall storage footprint between **15 MB and 18.5 MB**:

| Component | Raw Unoptimized Size | MoinoGeo Optimized Size | Optimization Technique Applied |
| :--- | :--- | :--- | :--- |
| **Relational Data** | ~85 MB (Raw JSON) | **12.5 MB** (`moinogeo.db`) | SQLite page packing, Integer PK foreign keys, VACUUM compaction, B-tree indexes on `iso2`, `country_id`, `state_id`, `name`. |
| **Map Boundaries** | ~12 MB (GeoJSON) | **2.1 MB** (`world-50m.topojson`) | Arc topology encoding, 10,000-point coordinate quantization, floating-point delta encoding. |
| **Flags** | ~6.5 MB (Raw SVGs) | **1.8 MB** (`flags/*.svg`) | SVGO minification: removal of XML metadata, comments, hidden elements, and coordinate precision reduction. |
| **Total Asset Footprint** | **~103.5 MB** | **~16.4 MB** | **~84% Total Compression Ratio** |

---

## 3. Database Indexing Strategy

To ensure sub-millisecond query execution on mobile CPUs, B-tree indexes are built during offline database compilation:
- `idx_countries_iso2`: Direct lookup by ISO 3166-1 alpha-2 code (`iso2`).
- `idx_states_country`: Foreign key lookup retrieving all states for a specific country.
- `idx_cities_state`: Foreign key lookup retrieving cities by state.
- `idx_cities_country`: Foreign key lookup retrieving cities directly by country.
- `idx_cities_name_prefix`: Prefix index supporting autocomplete search inputs.
- `idx_translations_lookup`: Index supporting fast multi-language entity lookup by entity type, entity ID, and language code.
