# Maintenance & Data Automation Scripts

This guide describes how to regenerate or update geographic datasets, translations, flags, and map boundaries using the automation scripts located in [scripts/](file:///Users/alanhart/Documents/Dev/MoinoGeo/scripts/README.md).

---

## 1. Database Compiler (`build_database.py`)

Processes raw JSON datasets and translation files into an optimized, indexed SQLite database file (`moinogeo.db`).

### Features
- Compiles `countries`, `states`, `cities`, and multi-language `translations` (`en`, `fr`, `es`, `it`, `de`, `uk`, `ru`) into normalized SQLite tables.
- Builds B-Tree indexes on ISO codes, country IDs, state IDs, region names, city names, and translation lookup keys.
- Executes `VACUUM` and `ANALYZE` for maximum database compaction (~12.5 MB final size).

### Usage
```bash
python3 scripts/build_database.py \
  --input data/raw/ \
  --translations data/translations/ \
  --output data/moinogeo.db
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

## Setup & Script Dependencies

All scripts are compatible with Python 3.8+ and use standard library modules by default. Optional packages can be installed via:

```bash
pip install -r scripts/requirements.txt
```
