# MoinoGeo Documentation Hub

Welcome to the technical documentation for **MoinoGeo**, an offline-first, high-performance geographic library engineered as a **Kotlin- and Swift-optimized wrapper for mobile platforms** (Android, iOS, macOS).

MoinoGeo wraps comprehensive world geographic data—regions, countries, states/provinces, 150,000+ cities, and multi-language localizations—with optional visual extension modules for vector SVG flags and TopoJSON country boundaries.

---

## Architectural Philosophy: Mobile-Optimized Wrapper & Modular Asset Efficiency

MoinoGeo is designed specifically as a platform wrapper to give mobile developers maximum performance and binary size efficiency:
* **Core Wrapper + Optional Visual Extensions:** The core relational engine (`moinogeo.db`) provides offline geographic data and localizations. Vector flags (`flags/`) and map border polygons (`world-50m.topojson`) are unbundled as **optional visual extension modules**.
* **Zero Storage Waste:** Unused visual assets can be omitted to keep mobile binary sizes lean. Omitting map borders **saves 2.1 MB** (13% binary size reduction). Omitting flag vectors **saves 1.8 MB** (11%). Omitting both **saves 3.9 MB** (24%).
* **Pluggable Visual Assets:** For aesthetic, branding, or political/legal compliance reasons, developers can import MoinoGeo's Kotlin/Swift flag and map components while supplying **custom artwork** (local SVGs, remote CDN URLs, or custom TopoJSON/GeoJSON borders) via abstract provider interfaces.

---

## Documentation Structure

To keep documentation clean and token-efficient for both human developers and LLMs, the technical guide is modularized into focused documents:

### Core Architecture & Schema
* **[Architecture & Design Philosophy](file:///Users/alanhart/Documents/Dev/MoinoGeo/docs/architecture.md):** Mobile wrapper design, storage optimization breakdown (~12.5 MB to ~16.4 MB depending on selected modules), aesthetic/geopolitical neutrality, and database indexing.
* **[Data Schema Overview](file:///Users/alanhart/Documents/Dev/MoinoGeo/docs/schema.md):** Entity-relationship diagrams (ERD), table specifications, SQL DDL, and index definitions for `countries`, `states`, and `cities`.
* **[Localization & Multi-Language Support](file:///Users/alanhart/Documents/Dev/MoinoGeo/docs/localization.md):** Strategy, JSON translation file structures, database representation, and automated build pipeline for multi-language support (`en`, `fr`, `es`, `it`, `de`, `uk`, `ru`).

### Platform Integration Guides
* **[Android Integration Guide](file:///Users/alanhart/Documents/Dev/MoinoGeo/docs/integration-android.md):** Gradle setup, selective asset stripping for APK/AAB size optimization, Room pre-populated database configuration, and custom `FlagAssetProvider` / `BoundaryProvider` setup in Kotlin.
* **[Apple Integration Guide (iOS / macOS)](file:///Users/alanhart/Documents/Dev/MoinoGeo/docs/integration-apple.md):** Swift Package Manager (SPM) modular targets (`MoinoGeoCore`, `MoinoGeoFlags`, `MoinoGeoMaps`), app binary size optimization, `Bundle.module` resource handling, and custom `FlagProvider` / `BoundaryProvider` protocols in Swift.

### Media & Map Boundaries
* **[Vector SVG Flags Guide](file:///Users/alanhart/Documents/Dev/MoinoGeo/docs/flags.md):** ISO 3166-1 alpha-2 vector flag assets (~1.8 MB), custom flag artwork injection, minification pipeline, Android Coil-SVG integration, and SwiftUI rendering.
* **[TopoJSON Map Boundaries Guide](file:///Users/alanhart/Documents/Dev/MoinoGeo/docs/maps.md):** Topology-encoded vector maps (`world-50m.topojson`, ~2.1 MB), custom map boundary dataset injection, arc decoding algorithms, and Swift MapKit border rendering.

### Legal & Maintenance
* **[Licensing & Compliance Guide](file:///Users/alanhart/Documents/Dev/MoinoGeo/docs/licensing.md):** ODbL v1.0, MIT, and CC0 licensing guidelines, attribution templates, and commercial compliance rules.
* **[Maintenance & Data Automation](file:///Users/alanhart/Documents/Dev/MoinoGeo/docs/maintenance.md):** Python automation tools (`build_database.py`, `optimize_flags.py`, `convert_topojson.py`) for processing upstream datasets.

---

## Quick Component Modularity & Storage Matrix

| Component Module | Asset File / Target | Size | Primary Function | Optionality & Storage Impact |
| :--- | :--- | :--- | :--- | :--- |
| **Core Data (`moinogeo-core`)** | `moinogeo.db` | ~12.5 MB | Relational search for 150,000+ cities, states, countries, and localizations. | **Core Module:** Complete geographic database. Omit flags & maps to **save 3.9 MB**. |
| **Vector Flags (`moinogeo-flags`)** | `flags/*.svg` | ~1.8 MB | Minified vector SVG country flags mapped to ISO 3166-1 alpha-2 codes. | **Optional Extension:** Omit if app does not render flag icons to **save 1.8 MB**. |
| **Map Boundaries (`moinogeo-maps`)** | `world-50m.topojson` | ~2.1 MB | Quantized arc topology for offline vector country polygon map overlays. | **Optional Extension:** Omit if app does not render map overlays to **save 2.1 MB**. |

---

## Summary Checklist for Developers

- [ ] **Determine Storage Budget:** Select only the required visual extensions (Flags, Map Boundaries) attached to Core Data to optimize app download size.
- [ ] **Database Setup:** Place `moinogeo.db` in app bundle or Android `assets/databases/`.
- [ ] **Flags Setup:** Include default `flags/` SVG directory or implement custom `FlagAssetProvider` for custom artwork.
- [ ] **Boundaries Setup:** Include `world-50m.topojson` or implement custom `BoundaryProvider` for custom map geometry.
- [ ] **Localization Setup:** Configure target locale (`en`, `fr`, `es`, `it`, `de`, `uk`, `ru`) in database query calls.
- [ ] **Legal Compliance:** Add ODbL v1.0 and MIT attributions in your app's "About/Legal" screen.
