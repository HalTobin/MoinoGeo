# MoinoGeo Build & Maintenance Scripts

This directory contains Python automation scripts for processing, compiling, minifying, and optimizing geographic datasets into local assets for the MoinoGeo library.

---

## 1. Database Compiler (`build_database.py`)

Processes raw JSON datasets from [`dr5hn/countries-states-cities-database`](https://github.com/dr5hn/countries-states-cities-database) into an optimized, indexed SQLite database file (`moinogeo.db`).

### Features
- Compiles `countries`, `states`, and `cities` into normalized SQLite tables.
- Builds B-Tree indexes on ISO codes, country IDs, state IDs, region names, and city names.
- Executes `VACUUM` and `ANALYZE` for maximum database compaction (~12.5 MB final size).

### Usage
```bash
python3 scripts/build_database.py --input path/to/raw_json_dir/ --output data/moinogeo.db
```

Or using a single combined file (`countries+states+cities.json`):
```bash
python3 scripts/build_database.py --input path/to/countries+states+cities.json --output data/moinogeo.db
```

---

## 2. Flag SVG exporters (`scripts/flags/`)

Copies ISO 3166-1 alpha-2 flag SVGs from [`data/flags/`](../data/flags/) into Kotlin (and later iOS) modules. Shared lossless cleanup lives in `scripts/flags/common/`; destinations are platform-specific.

See [`scripts/flags/README.md`](flags/README.md) for flag-set mapping and options.

### Usage
```bash
python3 scripts/flags/export_kotlin.py
python3 scripts/flags/export_kotlin.py --dry-run
python3 scripts/flags/export_kotlin.py --only yummygum-flagpack lipis-square
```

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

## Setup & Dependencies

All scripts are compatible with Python 3.8+ and use standard library modules by default. Optional packages can be installed via:

```bash
pip install -r scripts/requirements.txt
```
