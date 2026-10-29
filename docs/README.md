# MoinoGeo Documentation Hub

Welcome to the technical documentation for **MoinoGeo**, an offline-first, high-performance geographic library engineered as a **mobile platform wrapper**. Currently focused on **Android / Compose Multiplatform (CMP)**, with iOS/Swift support planned for a later phase.

MoinoGeo wraps comprehensive world geographic data—regions, countries, states/provinces, 150,000+ cities, and multi-language localizations—with optional visual extension modules for vector SVG flags and TopoJSON country boundaries.

---

## Architectural Philosophy: Mobile-Optimized Wrapper & Modular Asset Efficiency

MoinoGeo is designed specifically as a platform wrapper to give mobile developers maximum performance and binary size efficiency:
* **Core Wrapper + Optional Visual Extensions:** The core relational engine (`moinogeo.db`) provides offline geographic data and localizations. Vector flags (`flags/`) and map border polygons (`world-50m.topojson`) are unbundled as **optional visual extension modules**.
* **Zero Storage Waste:** Unused visual assets can be omitted to keep mobile binary sizes lean. Omitting map borders **saves 2.1 MB** (13% binary size reduction). Omitting flag vectors **saves 1.8 MB** (11%). Omitting both **saves 3.9 MB** (24%).
* **Pluggable Visual Assets:** For aesthetic, branding, or political/legal compliance reasons, developers can import MoinoGeo's Kotlin flag and map components while supplying **custom artwork** (local SVGs, remote CDN URLs, or custom TopoJSON/GeoJSON borders) via abstract provider interfaces.
* **Swappable Flag Icon Sets:** The flag module is designed around a core abstraction with **interchangeable icon-set modules**. Three sets are supported out of the box: **lipis/flag-icons** (rectangular), **flagpack-core** by Yummygum (rounded-rectangle), and **circle-flags** by HatScripts (circular).

---

## Documentation Structure

To keep documentation clean and token-efficient for both human developers and LLMs, the technical guide is modularized into focused documents:

### Core Architecture & Schema
* **[Architecture & Design Philosophy](file:///Users/alanhart/Documents/Dev/MoinoGeo/docs/architecture.md):** Mobile wrapper design, storage optimization breakdown (~12.5 MB to ~16.4 MB depending on selected modules), aesthetic/geopolitical neutrality, and database indexing.
* **[Data Schema Overview](file:///Users/alanhart/Documents/Dev/MoinoGeo/docs/schema.md):** Entity-relationship diagrams (ERD), table specifications, SQL DDL, and index definitions for `countries`, `states`, and `cities`.
* **[Localization & Multi-Language Support](file:///Users/alanhart/Documents/Dev/MoinoGeo/docs/localization.md):** Strategy, JSON translation file structures, database representation, and automated build pipeline for multi-language support (`en`, `fr`, `es`, `it`, `de`, `uk`, `ru`).

### Platform Integration Guides
* **[Android & CMP Integration Guide](file:///Users/alanhart/Documents/Dev/MoinoGeo/docs/integration-android.md):** Gradle setup, Room pre-populated database configuration, and swappable `FlagAssetProvider` / `BoundaryProvider` setup in Kotlin / Compose Multiplatform.
* **[Apple Integration Guide (iOS / macOS)](file:///Users/alanhart/Documents/Dev/MoinoGeo/docs/integration-apple.md):** *(Planned — deferred)* Swift Package Manager (SPM) architecture, `Bundle.module` resource handling, and custom Swift providers.

### Media & Map Boundaries
* **[Vector SVG Flags Guide](file:///Users/alanhart/Documents/Dev/MoinoGeo/docs/flags.md):** Swappable flag icon-set architecture (`lipis`, `flagpack-core`, `circle-flags`), core abstraction module, custom flag artwork injection, minification pipeline, and Compose Multiplatform integration.
* **[TopoJSON Map Boundaries Guide](file:///Users/alanhart/Documents/Dev/MoinoGeo/docs/maps.md):** Topology-encoded vector maps (`world-50m.topojson`, ~2.1 MB), custom map boundary dataset injection, arc decoding algorithms, and vector border rendering.

### Legal & Maintenance
* **[Licensing & Compliance Guide](file:///Users/alanhart/Documents/Dev/MoinoGeo/docs/licensing.md):** ODbL v1.0, MIT, and CC0 licensing guidelines for data, flag icon sets (`lipis`, `flagpack`, `circle-flags`), and boundaries.
* **[Maintenance & Data Automation](file:///Users/alanhart/Documents/Dev/MoinoGeo/docs/maintenance.md):** Python automation tools (`build_database.py`, `optimize_flags.py`, `convert_topojson.py`) for processing raw upstream source datasets in `data/` into platform-ready assets.

---

## Quick Component Modularity & Storage Matrix

| Component Module | Source / Target Asset | Size | Primary Function | Optionality & Storage Impact |
| :--- | :--- | :--- | :--- | :--- |
| **Core Data (`moinogeo-core`)** | `moinogeo.db` | ~12.5 MB | Relational search for 150,000+ cities, states, countries, and localizations. | **Core Module:** Complete geographic database. Omit flags & maps to **save storage**. |
| **Flags Core (`moinogeo-flags`)** | Interfaces & Composables | <50 KB | Core abstractions (`FlagAssetProvider`, `FlagIcon`), Coil SVG setup. No bundled assets. | **Core Flags Module:** Required if using any flag icons. |
| **Icon Set: Lipis (`moinogeo-flags-lipis`)** | `flags-lipis/*.svg` | ~1.8 MB | Rectangular 4×3 and 1×1 vector SVG flags from `lipis/flag-icons`. | **Swappable Option:** Choose this, flagpack, or circle-flags. |
| **Icon Set: Flagpack (`moinogeo-flags-flagpack`)** | `flags-flagpack/*.svg` | ~1.5 MB | Stylized rounded-rectangle vector flag icons from `Yummygum`. | **Swappable Option:** Choose this, lipis, or circle-flags. |
| **Icon Set: Circle Flags (`moinogeo-flags-circle`)** | `flags-circle/*.svg` | ~1.6 MB | Circular vector flag icons from `HatScripts`. | **Swappable Option:** Choose this, lipis, or flagpack. |
| **Map Boundaries (`moinogeo-maps`)** | `world-50m.topojson` | ~2.1 MB | Quantized arc topology for offline vector country polygon map overlays. | **Optional Extension:** Omit if app does not render map overlays. |

---

## Summary Checklist for Developers

- [ ] **Raw Sources in `data/`:** Ensure raw external source datasets (JSON dumps, raw flag SVGs, GeoJSON boundaries) are present in `data/` before running scripts.
- [ ] **Generate Assets:** Run `scripts/` to generate `moinogeo.db`, optimized flag SVGs, and quantized TopoJSON.
- [ ] **Database Setup:** Place generated `moinogeo.db` in Android `assets/databases/`.
- [ ] **Select Flag Icon Set:** Pick one of the swappable flag modules (`flags-lipis`, `flags-flagpack`, or `flags-circle`) or implement a custom `FlagAssetProvider`.
- [ ] **Boundaries Setup:** Include `world-50m.topojson` or implement custom `BoundaryProvider` for custom map geometry.
- [ ] **Localization Setup:** Configure target locale (`en`, `fr`, `es`, `it`, `de`, `uk`, `ru`) in database query calls.
- [ ] **Legal Compliance:** Add ODbL v1.0 and MIT attributions in your app's "About/Legal" screen.
