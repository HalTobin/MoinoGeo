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

## 2. Flag Icon Minifier (`optimize_flags.py`)

Minifies SVG vector flag icons from upstream icon repositories (**`lipis/flag-icons`**, **`flagpack-core`**, **`circle-flags`**) and standardizes filenames to lowercase ISO 3166-1 alpha-2 codes.

### Features
- Strips XML metadata, comments, doctypes, and redundant formatting whitespace.
- Standardizes output paths for each swappable icon module (`flags-lipis/`, `flags-flagpack/`, `flags-circle/`).
- Achieves ~70%+ file size reduction per vector SVG.

### Usage
```bash
# Minify lipis flags
python3 scripts/optimize_flags.py --input data/raw-flags/lipis/ --output kotlin/flags-lipis/src/commonMain/resources/flags-lipis/

# Minify flagpack flags
python3 scripts/optimize_flags.py --input data/raw-flags/flagpack/ --output kotlin/flags-flagpack/src/commonMain/resources/flags-flagpack/

# Minify circle flags
python3 scripts/optimize_flags.py --input data/raw-flags/circle/ --output kotlin/flags-circle/src/commonMain/resources/flags-circle/
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

## Setup & Script Dependencies

All scripts are compatible with Python 3.8+ and use standard library modules by default. Optional packages can be installed via:

```bash
pip install -r scripts/requirements.txt
```
