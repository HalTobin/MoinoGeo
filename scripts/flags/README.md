# Flag SVG export scripts

Shared tooling to copy flag SVGs from [`data/flags/`](../../data/flags/) into platform modules, with **lossless** cleanup (comments, XML prolog, DOCTYPE, `<metadata>`, inter-tag whitespace only — no path/precision changes).

| Script | Role |
|--------|------|
| [`common/`](common/) | Catalog, analyzer, optimizer, export pipeline |
| [`export_kotlin.py`](export_kotlin.py) | Write into `kotlin/flags/*/resources/` |
| [`export_darwin.py`](export_darwin.py) | Stub for a future Swift local package |

## Flag sets

| Id | Source | Kotlin destination |
|----|--------|--------------------|
| `yummygum-flagpack` | `data/flags/Yummygum_flagpack-core/svg_l` | `…/flags-flagpack/` |
| `hatscripts-circle` | `data/flags/HatScripts_circle-flags/flags` | `…/flags-circle/` |
| `lipis-square` | `data/flags/lipis_flag-icons/flags/1x1` | `…/flags-lipis-square/` |
| `lipis-rectangle` | `data/flags/lipis_flag-icons/flags/4x3` | `…/flags-lipis-rectangle/` |

All top-level `*.svg` basenames matching `[A-Za-z0-9_-]+.svg` are exported:

- ISO 3166-1 alpha-2 (`fr.svg`)
- Regional / subdivision variants (`gb-eng.svg`, `BQ-BO.svg`, `us-ca.svg`)
- Named / organization flags (`arab.svg`, `european_union.svg`)

Nested folders (`flags/language/`, `flags/other/`) are skipped so their basenames do not collide with country codes when flattened into the destination.

## Usage

```bash
# Export all sets into Kotlin modules
python3 scripts/flags/export_kotlin.py

# Preview without writing
python3 scripts/flags/export_kotlin.py --dry-run

# One or more packs only
python3 scripts/flags/export_kotlin.py --only yummygum-flagpack lipis-square

# Replace existing SVGs in destinations
python3 scripts/flags/export_kotlin.py --clean
```

Filenames are normalized to lowercase (`FR.svg` → `fr.svg`, `GB-ENG.svg` → `gb-eng.svg`).
Providers resolve codes the same way — pass `gb-eng`, `bq-bo`, etc. to `getFlagData`.

## Adding iOS later

1. Create the Swift package resource folders.
2. Update `IOS_DESTINATIONS` in [`export_darwin.py`](export_darwin.py).
3. Run `python3 scripts/flags/export_ios.py`.

Optimization stays in `common/`; only the destination map is platform-specific.
