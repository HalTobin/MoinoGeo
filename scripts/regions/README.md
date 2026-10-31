# Regions SQLite export scripts

Build a slim search-focused `moinogeo.db` from the nested JSON dump under
[`data/db/regions/`](../../data/db/regions/) and copy it into platform modules.

| Script | Role |
|--------|------|
| [`common/`](common/) | JSON load, slim schema DDL, SQLite build |
| [`export_kotlin.py`](export_kotlin.py) | Android assets + JVM resources in `kotlin/core` |
| [`export_darwin.py`](export_darwin.py) | Stub for a future Swift local package |

## Slim schema

| Table | Columns |
|-------|---------|
| `regions` | id, name |
| `subregions` | id, name, region_id |
| `countries` | id, name, iso2, iso3, capital, region_id, subregion_id, latitude, longitude |
| `states` | id, country_id, name, iso2, iso3166_2, type, latitude, longitude |
| `cities` | id, state_id?, country_id, name, latitude, longitude |
| `translations` | entity_type, entity_id, lang_code, name (countries, all upstream langs) |

Dropped from upstream JSON: currency*, tld, native, nationality, emoji*, gdp,
population, timezones, phone/numeric codes, city timezone / county_id.

## Usage

```bash
# Build and write into kotlin/core
python3 scripts/regions/export_kotlin.py

# Preview without writing
python3 scripts/regions/export_kotlin.py --dry-run

# After the first Room compile, paste the identity hash from MoinoGeoDatabase_Impl
python3 scripts/regions/export_kotlin.py --identity-hash '<hash>'
```

Kotlin destinations:

- `kotlin/core/src/androidMain/assets/databases/moinogeo.db` (`createFromAsset`)
- `kotlin/core/src/jvmMain/resources/databases/moinogeo.db` (copied to disk on open)

## Room identity hash

Room validates prepackaged databases via `room_master_table.identity_hash`.
After entities stabilize, compile `:core` and copy the hash from the generated
`MoinoGeoDatabase_Impl`, then re-run export with `--identity-hash` (also saved to
[`common/room_identity_hash.txt`](common/room_identity_hash.txt)).
