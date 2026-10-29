# MoinoGeo
A lightweight, offline-first **mobile platform wrapper** for world regions, countries, states/provinces, cities, vector SVG flags, and map border polygons. Currently focused on **Android / Compose Multiplatform (CMP)**, with iOS/Swift support planned for a later phase.

[![Platform](https://img.shields.io/badge/platform-iOS%20%7C%20macOS%20%7C%20Android-blue)](https://github.com)
[![License](https://img.shields.io/badge/license-MIT-green)](LICENSE)

---

## Features

* **Kotlin & Compose Multiplatform:** Native, high-performance API for Android (Kotlin / Room / Jetpack Compose). iOS/Swift support (SQLite3 / GRDB) is planned for a later phase.
* **Comprehensive Geographic Data:** Access structured information on world regions, countries, states/provinces, and over 150,000 cities.
* **Unbundled Visual Extensions:** Vector SVG flags and TopoJSON map border polygons are isolated into optional visual extension modules. Omit unneeded visual assets to keep app binary size lean (e.g. omit map borders to save 2.1 MB, or flags to save 1.8 MB).
* **Swappable Flag Icon Sets:** The flag module uses an abstract provider architecture so icon sets can be easily swapped. Three icon-set modules are supported out of the box:
  * **lipis/flag-icons** — rectangular 4×3 / 1×1 SVG flags
  * **flagpack-core** (Yummygum) — stylized rounded-rectangle flag icons
  * **circle-flags** (HatScripts) — circular flag icons
* **Pluggable Visual Assets & Aesthetic Neutrality:** Swap bundled vector flags with your own custom flag artwork (local SVGs, remote CDN URLs, or alternative regional flag variants) or custom map border datasets (high-res borders, disputed territory adjustments) using abstract provider interfaces.
* **Multi-Language Support:** Offline localization across multiple languages (`en`, `fr`, `es`, `it`, `de`, `uk`, `ru`).
* **Offline-First:** Zero network calls or API keys required. Everything is pre-bundled and optimized for mobile performance.

---

## Project Structure

* **`kotlin/`**: Shared Kotlin code and interface abstractions (`FlagAssetProvider`, `BoundaryProvider`, Room DAOs). Includes a core flags abstraction module and separate icon-set modules for lipis, flagpack, and circle-flags.
* **`darwin/`**: *(Planned — not yet implemented)* Swift Package Manager targets for iOS/macOS.
* **`data/`**: Raw upstream source datasets (JSON country/state/city data, raw SVG flag icons, GeoJSON boundary files, translation files). These are processed by `scripts/` to generate platform-specific outputs.
* **`scripts/`**: Automation scripts to process raw datasets into platform-ready assets (`build_database.py`, `optimize_flags.py`, `convert_topojson.py`).
* **`docs/`**: Technical documentation detailing library architecture, modularity, schemas, and integration guides.

---

## Documentation

For detailed installation guides, data schemas, localization strategy, and API usage, refer to the [Documentation Hub](./docs/README.md):

* [Architecture & Design Philosophy](./docs/architecture.md)
* [Data Schema & ERD](./docs/schema.md)
* [Localization Guide](./docs/localization.md)
* [Vector Flags & Custom Art Guide](./docs/flags.md)
* [TopoJSON Map Boundaries Guide](./docs/maps.md)
* [Android Integration](./docs/integration-android.md)
* [Apple (iOS/macOS) Integration](./docs/integration-apple.md)
* [Licensing & Compliance](./docs/licensing.md)
* [Maintenance & Automation Scripts](./docs/maintenance.md)

---

## License

This project is open source and available under the [MIT License](LICENSE).