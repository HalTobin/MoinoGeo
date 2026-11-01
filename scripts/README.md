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

## 3. Regions SQLite export (`scripts/regions/`)

Builds a slim search-focused `moinogeo.db` from nested JSON under `data/db/regions/`.
See [`scripts/regions/README.md`](regions/README.md).

```bash
python3 scripts/regions/export_kotlin.py
python3 scripts/regions/export_kotlin.py --dry-run
```

---

## 4. Topography & country boundaries (`scripts/topo/`)

Downloads [Natural Earth](https://www.naturalearthdata.com/) physical + admin-0 vectors
(cached under `data/natural_earth/`), simplifies for offline mobile maps, and writes:

- `TopographyData` JSON → `kotlin/maps/topography/.../resources/`
- `BoundaryData` JSON + TopoJSON → `kotlin/maps/countries/.../resources/`

NE vectors cover `WATER` / `SNOW` / `SAND` / `NEUTRAL` only — not forest, grassland,
agriculture, or rock. See [`scripts/topo/README.md`](topo/README.md).

```bash
pip install -r scripts/requirements.txt
python3 scripts/topo/export_kotlin.py
python3 scripts/topo/export_kotlin.py --dry-run
python3 scripts/topo/export_kotlin.py --scale 110m --staging-only
```

---

## Setup & Dependencies

Flag/region scripts lean on the standard library. Topo export needs GeoPandas +
Shapely + topojson:

```bash
pip install -r scripts/requirements.txt
```
