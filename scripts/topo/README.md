# Topography & country boundary export (Natural Earth)

Builds offline map assets from [Natural Earth](https://www.naturalearthdata.com/)
vector themes into the Kotlin maps modules.

| Script | Role |
|--------|------|
| [`common/`](common/) | Download/cache, simplify, zone mapping, JSON/TopoJSON writers |
| [`export_kotlin.py`](export_kotlin.py) | Write into `kotlin/maps/topography` + `kotlin/maps/countries` |
| [`export_darwin.py`](export_darwin.py) | Stub for a future Swift package |

## What Natural Earth covers

Mapped to [`TopoZone`](../../kotlin/maps/core/src/commonMain/kotlin/com/moinofactory/moinogeo/maps/core/TopoZone.kt):

| Zone | Natural Earth layer(s) |
|------|-------------------------|
| `WATER` | ocean, lakes |
| `SNOW` | glaciated areas, Antarctic ice shelves |
| `SAND` | playas (sparse) |
| `NEUTRAL` | land (generic; emitted last) |

**Not available** as NE polygons (left empty for a later land-cover source):
`FOREST`, `GRASSLAND`, `AGRICULTURE`, `ROCK`.

Countries come from `ne_*_admin_0_countries` → `BoundaryData` JSON + TopoJSON
(for [`TopoJsonDecoder`](../../kotlin/maps/countries/src/commonMain/kotlin/com/moinofactory/moinogeo/maps/countries/TopoJsonDecoder.kt)).

## Scale

Default **`50m`** — good fit for ~300–3000 km per inch. Use `110m` for a
tiny proof pack, `10m` if you later need more coastline detail.

## Usage

```bash
pip install -r scripts/requirements.txt

# Download (cached under data/natural_earth/), process, write Kotlin resources
python3 scripts/topo/export_kotlin.py

# Preview counts without writing
python3 scripts/topo/export_kotlin.py --dry-run

# Coarser / finer NE scale
python3 scripts/topo/export_kotlin.py --scale 110m

# Only write data/db/topo/ (inspect before touching modules)
python3 scripts/topo/export_kotlin.py --staging-only

# Skip TopoJSON (flat boundary_data.json only)
python3 scripts/topo/export_kotlin.py --skip-topojson
```

### Outputs

| Path | Content |
|------|---------|
| `data/natural_earth/{scale}/` | Cached NE shapefile zips |
| `data/db/topo/topography_data.json` | Staging copy of `TopographyData` |
| `data/db/topo/boundary_data.json` | Staging copy of `BoundaryData` |
| `data/db/topo/world-{scale}.topojson` | Staging TopoJSON |
| `kotlin/maps/topography/.../topography/topography_data.json` | App resource |
| `kotlin/maps/countries/.../countries/boundary_data.json` | App resource |
| `kotlin/maps/countries/.../countries/world-50m.topojson` | App resource |

Providers still use in-code fallbacks today; wiring resource loading is a
follow-up once you like the file sizes and coverage.

## Tuning

| Flag | Meaning |
|------|---------|
| `--simplify DEG` | Extra Douglas–Peucker in degrees (defaults: 110m=0, 50m=0.02, 10m=0.05) |
| `--min-area SQDEG` | Drop tiny polygon parts |
