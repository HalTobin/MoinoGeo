# MoinoGeo
A lightweight, offline-first mobile library providing world regions, countries, cities, SVG flags, and map border polygons for iOS and Android.

[![Platform](https://img.shields.io/badge/platform-iOS%20%7C%20macOS%20%7C%20Android-blue)](https://github.com)
[![License](https://img.shields.io/badge/license-MIT-green)](LICENSE)

---

## Features

* **Comprehensive Data:** Access structured information on world regions, countries, states/provinces, and over 150,000 cities.
* **UI-Ready Flags:** Bundled SVG country flags mapped seamlessly via standard ISO codes.
* **Map Boundaries:** Lightweight TopoJSON polygon data for rendering accurate country shapes and borders on a map.
* **Offline-First:** Zero network calls or API keys required. Everything is pre-bundled and optimized for mobile performance.
* **Cross-Platform:** Designed natively for Android, iOS, and macOS applications.

---

## Project Structure

* **`kotlin/`**: Shared Kotlin code that can be used across various targets (such as Android, JVM, etc.).
* **`android/`**: Android-specific code and implementations.
* **`darwin/`**: Code for Apple platforms (iOS, macOS, etc.), targeting exclusively Swift and SwiftUI code where possible.
* **`data/`**: Contains all aggregated data collected from various selected sources.
* **`scripts/`**: Automation scripts to process datasets from `data/` into platform-specific implementations (e.g., generating Kotlin/Swift enums, copying image assets into target resource folders, etc.).
* **`docs/`**: Technical documentation and instructions for both human developers and LLMs to understand library architecture and development workflows.

---

## Documentation

For detailed installation guides, data schemas, code snippets, and API usage, please refer to the [Technical Documentation](./docs/README.md).

---

## License

This project is open source and available under the [MIT License](LICENSE).