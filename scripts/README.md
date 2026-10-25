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

## 2. Flag Icon Minifier (`optimize_flags.py`)

Minifies SVG vector flag icons from [`lipis/flag-icons`](https://github.com/lipis/flag-icons) and standardizes filenames to lowercase ISO 3166-1 alpha-2 codes.

### Features
- Strips XML metadata, comments, doctypes, and redundant formatting whitespace.
- Standardizes output paths (`data/flags/us.svg`, `data/flags/de.svg`, etc.).
- Achieves ~70%+ file size reduction per vector SVG.

### Usage
```bash
python3 scripts/optimize_flags.py --input path/to/lipis_flags/ --output data/flags/
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
