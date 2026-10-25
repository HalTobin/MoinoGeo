# MoinoGeo
A lightweight, offline-first, **Kotlin- and Swift-optimized mobile platform wrapper** for world regions, countries, states/provinces, cities, vector SVG flags, and map border polygons on iOS and Android.

[![Platform](https://img.shields.io/badge/platform-iOS%20%7C%20macOS%20%7C%20Android-blue)](https://github.com)
[![License](https://img.shields.io/badge/license-MIT-green)](LICENSE)

---

## Features

* **Kotlin & Swift Platform Wrapper:** Native, high-performance API for Android (Kotlin / Room) and Apple platforms (Swift / SQLite3 / GRDB).
* **Comprehensive Geographic Data:** Access structured information on world regions, countries, states/provinces, and over 150,000 cities.
* **Unbundled Visual Extensions:** Vector SVG flags and TopoJSON map border polygons are isolated into optional visual extension modules. Omit unneeded visual assets to keep app binary size lean (e.g. omit map borders to save 2.1 MB, or flags to save 1.8 MB).
* **Pluggable Visual Assets & Aesthetic Neutrality:** Swap bundled vector flags with your own custom flag artwork (local SVGs, remote CDN URLs, or alternative regional flag variants) or custom map border datasets (high-res borders, disputed territory adjustments) using abstract provider interfaces.
* **Multi-Language Support:** Offline localization across multiple languages (`en`, `fr`, `es`, `it`, `de`, `uk`, `ru`).
* **Offline-First:** Zero network calls or API keys required. Everything is pre-bundled and optimized for mobile performance.

---

## Project Structure

* **`kotlin/`**: Shared Kotlin code and interface abstractions (`FlagAssetProvider`, `BoundaryProvider`, Room DAOs).
* **`darwin/`**: Swift Package Manager targets (`MoinoGeoCore`, `MoinoGeoFlags`, `MoinoGeoMaps`, `MoinoGeo`), Swift query engine, and SwiftUI components.
* **`data/`**: Aggregated source datasets (`moinogeo.db`, `flags/*.svg`, `world-50m.topojson`).
* **`scripts/`**: Automation scripts to process datasets (`build_database.py`, `optimize_flags.py`, `convert_topojson.py`).
* **`docs/`**: Technical documentation detailing library architecture, modularity, schemas, and integration guides.

---

## Documentation

For detailed installation guides, data schemas, localization strategy, and API usage, refer to the [Documentation Hub](./docs/README.md):

* 📐 [Architecture & Design Philosophy](./docs/architecture.md)
* 🗄️ [Data Schema & ERD](./docs/schema.md)
* 🌐 [Localization Guide](./docs/localization.md)
* 🚩 [Vector Flags & Custom Art Guide](./docs/flags.md)
* 🗺️ [TopoJSON Map Boundaries Guide](./docs/maps.md)
* 🤖 [Android Integration](./docs/integration-android.md)
* 🍏 [Apple (iOS/macOS) Integration](./docs/integration-apple.md)
* 📜 [Licensing & Compliance](./docs/licensing.md)
* ⚙️ [Maintenance & Automation Scripts](./docs/maintenance.md)

---

## License

This project is open source and available under the [MIT License](LICENSE).