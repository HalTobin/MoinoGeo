# MoinoGeo Documentation Hub

Welcome to the technical documentation for **MoinoGeo**, an offline-first, high-performance geography library for iOS, macOS, and Android.

MoinoGeo delivers comprehensive world geographic data—including regions, countries, states/provinces, over 150,000 cities, multi-language localization, vector SVG flags, and TopoJSON country boundaries—without requiring network requests or external API keys.

---

## Documentation Structure

To keep documentation clean and token-efficient for both human developers and LLMs, the technical guide is modularized into focused documents:

### Core Architecture & Schema
* **[Architecture & Design Philosophy](file:///Users/alanhart/Documents/Dev/MoinoGeo/docs/architecture.md):** Storage optimization strategies, compression benchmarks (~16.4 MB total footprint), and indexing principles.
* **[Data Schema Overview](file:///Users/alanhart/Documents/Dev/MoinoGeo/docs/schema.md):** Entity-relationship diagrams (ERD), table specifications, SQL DDL, and index definitions for `countries`, `states`, and `cities`.
* **[Localization & Multi-Language Support](file:///Users/alanhart/Documents/Dev/MoinoGeo/docs/localization.md):** Strategy, JSON translation file structures, database representation, and automated build pipeline for multi-language support (`en`, `fr`, `es`, `it`, `de`, `uk`, `ru`).

### Platform Integration Guides
* **[Android Integration Guide](file:///Users/alanhart/Documents/Dev/MoinoGeo/docs/integration-android.md):** Gradle setup, Room pre-populated database configuration, and Kotlin asynchronous DAOs.
* **[Apple Integration Guide (iOS / macOS)](file:///Users/alanhart/Documents/Dev/MoinoGeo/docs/integration-apple.md):** Swift Package Manager (SPM) setup, `Bundle.module` resource handling, and SQLite3 / GRDB query engine in Swift.

### Media & Map Boundaries
* **[Vector SVG Flags Guide](file:///Users/alanhart/Documents/Dev/MoinoGeo/docs/flags.md):** ISO 3166-1 alpha-2 vector flag assets, minification pipeline, Android Coil-SVG integration, and SwiftUI rendering.
* **[TopoJSON Map Boundaries Guide](file:///Users/alanhart/Documents/Dev/MoinoGeo/docs/maps.md):** Topology-encoded vector maps (`world-50m.topojson`), arc decoding algorithms, and Swift MapKit border rendering.

### Legal & Maintenance
* **[Licensing & Compliance Guide](file:///Users/alanhart/Documents/Dev/MoinoGeo/docs/licensing.md):** ODbL v1.0, MIT, and CC0 licensing guidelines, attribution templates, and commercial compliance rules.
* **[Maintenance & Data Automation](file:///Users/alanhart/Documents/Dev/MoinoGeo/docs/maintenance.md):** Python automation tools (`build_database.py`, `optimize_flags.py`, `convert_topojson.py`) for processing upstream datasets.

---

## Quick Component Summary

| Component | Asset File | Size | Primary Function |
| :--- | :--- | :--- | :--- |
| **Relational DB** | `moinogeo.db` | ~12.5 MB | Relational search for 150,000+ cities, states, countries, and multi-language translations. |
| **Map Boundaries** | `world-50m.topojson` | ~2.1 MB | Quantized arc topology for offline vector country polygon map overlays. |
| **Flags Assets** | `flags/*.svg` | ~1.8 MB | Minified vector SVG country flags keyed by ISO 3166-1 alpha-2 codes. |

---

## Summary Checklist for Developers

- [ ] **Database Setup:** Place `moinogeo.db` in app bundle or Android `assets/databases/`.
- [ ] **Flags Setup:** Include `flags/` SVG directory in assets or SPM module resources.
- [ ] **Boundaries Setup:** Include `world-50m.topojson` for vector map rendering.
- [ ] **Localization Setup:** Configure target locale (`en`, `fr`, `es`, `it`, `de`, `uk`, `ru`) in database query calls.
- [ ] **Legal Compliance:** Add ODbL v1.0 and MIT attributions in your app's "About/Legal" screen.
