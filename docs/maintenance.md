# Maintenance & Data Automation Scripts

This guide describes how to process raw upstream geographic datasets, translations, flags, and map boundaries using the automation scripts located in [scripts/](file:///Users/alanhart/Documents/Dev/MoinoGeo/scripts/README.md).

> **Data Pipeline Concept:**  
> The `data/` directory stores **raw upstream source files** (JSON dumps, raw SVGs, GeoJSON). The scripts in `scripts/` process and transform these sources into optimized artifacts (`moinogeo.db`, minified SVG sets, quantized TopoJSON) for consumption by Android, Compose Multiplatform, or iOS.

---

## 1. Database Compiler (`build_database.py`)

Processes raw JSON datasets and translation files from `data/` into an optimized, indexed SQLite / Room database file (`moinogeo.db`).

### Features
- Compiles `countries`, `states`, `cities`, and multi-language `translations` (`en`, `fr`, `es`, `it`, `de`, `uk`, `ru`) into normalized SQLite tables.
- Builds B-Tree indexes on ISO codes, country IDs, state IDs, region names, city names, and translation lookup keys.
- Executes `VACUUM` and `ANALYZE` for maximum database compaction (~12.5 MB final size).

### Usage
```bash
python3 scripts/build_database.py \
  --input data/countries+states+cities.json \
  --output data/moinogeo.db
```

---

## 2. Flag SVG exporters (`scripts/flags/`)

Exports ISO 3166-1 alpha-2 flag SVGs from `data/flags/` into the Kotlin flag modules (shared lossless optimizer; iOS stub ready for a Swift package).

### Features
- Strips comments, XML prolog, DOCTYPE, `<metadata>`, and inter-tag whitespace only (no geometry / precision changes).
- Writes into each swappable module’s `commonMain/resources/` folder (`flags-flagpack/`, `flags-circle/`, `flags-lipis-square/`, `flags-lipis-rectangle/`).
- Normalizes filenames to lowercase `xx.svg`.

### Usage
```bash
python3 scripts/flags/export_kotlin.py
python3 scripts/flags/export_kotlin.py --dry-run
python3 scripts/flags/export_kotlin.py --only yummygum-flagpack lipis-square --clean
```

See [`scripts/flags/README.md`](../scripts/flags/README.md).

---

## 3. TopoJSON Map Boundary Converter (`convert_topojson.py`)

Converts GeoJSON boundaries from Natural Earth or simplifies existing TopoJSON files into quantized, delta-encoded TopoJSON assets (`world-50m.topojson`).

### Features
- Quantizes arc coordinates to 10,000-point resolution.
- Filters out unneeded property attributes to minimize file payload.
- Includes pure Python fallback if the `topojson` package is not installed.

### Usage
```bash
python3 scripts/convert_topojson.py --input path/to/ne_50m_admin_0_countries.geojson --output data/world-50m.topojson
```

---

## Setup & Script Dependencies

All scripts are compatible with Python 3.8+ and use standard library modules by default. Optional packages can be installed via:

```bash
pip install -r scripts/requirements.txt
```
