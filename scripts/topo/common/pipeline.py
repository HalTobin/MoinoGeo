"""Build topography + country exports from Natural Earth."""

from __future__ import annotations

import json
from dataclasses import dataclass, field
from pathlib import Path
from typing import Any

import geopandas as gpd

from .catalog import (
    COUNTRIES_LAYER,
    DEFAULT_MIN_AREA,
    DEFAULT_SCALE,
    DEFAULT_SIMPLIFY_TOLERANCE,
    PHYSICAL_LAYERS,
    Scale,
    kotlin_borders_json_dest,
    kotlin_borders_topojson_dest,
    kotlin_topo_dest,
    layer_basename,
    repo_root,
    staging_dir,
)
from .download import ensure_countries_layer, ensure_physical_layers
from .geometry import geom_to_geo_polygons, simplify_geom


@dataclass
class BuildStats:
    topo_features: int = 0
    country_features: int = 0
    outputs: list[Path] = field(default_factory=list)
    errors: list[str] = field(default_factory=list)
    skipped_zones: tuple[str, ...] = (
        "FOREST",
        "GRASSLAND",
        "AGRICULTURE",
        "ROCK",
    )

    def print_summary(self) -> None:
        print("\n=== topo export summary ===")
        print(f"  Topography features: {self.topo_features}")
        print(f"  Country features:    {self.country_features}")
        if self.skipped_zones:
            print(
                "  Zones not in Natural Earth vectors: "
                + ", ".join(self.skipped_zones)
            )
        for path in self.outputs:
            size = path.stat().st_size if path.is_file() else 0
            print(f"  Wrote {path} ({size:,} bytes)")
        for err in self.errors:
            print(f"  ERROR: {err}")


def _feature_name(row: Any, default: str) -> str:
    for key in ("name", "NAME", "Name", "name_en", "NAME_EN"):
        if key in row.index:
            val = row[key]
            if val is not None and str(val).strip() and str(val) != "nan":
                return str(val).strip()
    return default


def _iso_code(row: Any) -> str:
    for key in ("ISO_A2", "iso_a2", "WB_A2", "wb_a2"):
        if key in row.index:
            val = row[key]
            if val is None:
                continue
            code = str(val).strip().upper()
            if code and code not in {"-99", "-1", "NA", "NONE", "NAN"}:
                return code
    for key in ("ADM0_A3", "adm0_a3", "SOV_A3"):
        if key in row.index:
            val = row[key]
            if val is not None and str(val).strip():
                return str(val).strip().upper()
    return "XX"


def _country_name(row: Any) -> str:
    for key in ("NAME", "NAME_EN", "ADMIN", "NAME_LONG", "name"):
        if key in row.index:
            val = row[key]
            if val is not None and str(val).strip() and str(val) != "nan":
                return str(val).strip()
    return "Unknown"


def build_topography(
    *,
    scale: Scale,
    root: Path,
    simplify: float,
    min_area: float,
) -> dict[str, Any]:
    paths = ensure_physical_layers(scale, root)
    # Stable order by priority (specific zones first).
    layers = sorted(PHYSICAL_LAYERS, key=lambda L: L.priority)

    features: list[dict[str, Any]] = []
    for layer in layers:
        basename = layer_basename(layer.name, scale)
        shp = paths.get(basename)
        if shp is None:
            continue
        gdf = gpd.read_file(shp)
        if gdf.crs is not None and gdf.crs.to_epsg() != 4326:
            gdf = gdf.to_crs(epsg=4326)

        for idx, row in gdf.iterrows():
            geom = simplify_geom(row.geometry, simplify)
            polygons = geom_to_geo_polygons(geom, min_area=min_area)
            name = _feature_name(row, default=f"{layer.zone.lower()}_{idx}")
            for pi, poly in enumerate(polygons):
                features.append(
                    {
                        "id": f"{basename}_{idx}_{pi}",
                        "name": name if pi == 0 else f"{name} ({pi + 1})",
                        "zone": layer.zone,
                        "geometry": poly,
                    }
                )

    return {"features": features}


def build_boundaries(
    *,
    scale: Scale,
    root: Path,
    simplify: float,
    min_area: float,
) -> tuple[dict[str, Any], "gpd.GeoDataFrame"]:
    shp = ensure_countries_layer(scale, root)
    gdf = gpd.read_file(shp)
    if gdf.crs is not None and gdf.crs.to_epsg() != 4326:
        gdf = gdf.to_crs(epsg=4326)

    gdf = gdf.copy()
    gdf["geometry"] = gdf["geometry"].apply(lambda g: simplify_geom(g, simplify))

    features: list[dict[str, Any]] = []
    keep_idx: list[Any] = []
    for idx, row in gdf.iterrows():
        polygons = geom_to_geo_polygons(row.geometry, min_area=min_area)
        if not polygons:
            continue
        iso = _iso_code(row)
        name = _country_name(row)
        features.append(
            {
                "id": iso,
                "isoCode": iso,
                "name": name,
                "geometries": polygons,
            }
        )
        keep_idx.append(idx)

    gdf_out = gdf.loc[keep_idx].copy()
    gdf_out["isoCode"] = [_iso_code(gdf_out.loc[i]) for i in keep_idx]
    gdf_out["name"] = [_country_name(gdf_out.loc[i]) for i in keep_idx]
    return {"features": features}, gdf_out


def _write_topojson(gdf: "gpd.GeoDataFrame", dest: Path) -> None:
    """Quantized TopoJSON for CustomBoundaryProvider / TopoJsonDecoder."""
    try:
        import topojson as tp
    except ImportError as exc:
        raise RuntimeError(
            "topojson package required for country TopoJSON export. "
            "pip install -r scripts/requirements.txt"
        ) from exc

    # Keep only properties the decoder looks for.
    slim = gdf[["geometry", "isoCode", "name"]].copy()
    slim["iso_a2"] = slim["isoCode"]
    topo = tp.Topology(slim, prequantize=True)
    # topojson.to_json() returns a JSON string.
    raw = topo.to_json()
    # Ensure object properties use iso_a2 / name (Topology may nest under objects).
    dest.parent.mkdir(parents=True, exist_ok=True)
    dest.write_text(raw, encoding="utf-8")


def _write_json(payload: dict[str, Any], dest: Path) -> None:
    dest.parent.mkdir(parents=True, exist_ok=True)
    dest.write_text(json.dumps(payload, separators=(",", ":")), encoding="utf-8")


def build_exports(
    destinations: dict[str, Path],
    *,
    root: Path | None = None,
    scale: Scale = DEFAULT_SCALE,
    simplify: float | None = None,
    min_area: float | None = None,
    dry_run: bool = False,
    skip_topojson: bool = False,
) -> BuildStats:
    root = (root or repo_root()).resolve()
    simplify = (
        DEFAULT_SIMPLIFY_TOLERANCE[scale] if simplify is None else simplify
    )
    min_area = DEFAULT_MIN_AREA[scale] if min_area is None else min_area
    stats = BuildStats()

    print(f"Scale={scale}  simplify={simplify}  min_area={min_area}")
    print("Building topography from Natural Earth physical vectors…")
    try:
        topo_payload = build_topography(
            scale=scale, root=root, simplify=simplify, min_area=min_area
        )
        stats.topo_features = len(topo_payload["features"])
    except Exception as exc:  # noqa: BLE001
        stats.errors.append(f"topography: {exc}")
        return stats

    print("Building country boundaries…")
    try:
        boundary_payload, countries_gdf = build_boundaries(
            scale=scale, root=root, simplify=simplify, min_area=min_area
        )
        stats.country_features = len(boundary_payload["features"])
    except Exception as exc:  # noqa: BLE001
        stats.errors.append(f"boundaries: {exc}")
        return stats

    # Always stage under data/db/topo for inspection.
    stage = staging_dir(root)
    stage.mkdir(parents=True, exist_ok=True)
    staged = {
        "topography": stage / "topography_data.json",
        "boundaries_json": stage / "boundary_data.json",
        "boundaries_topojson": stage / f"world-{scale}.topojson",
    }

    # Merge explicit destinations with staging defaults (staging always written).
    resolved: dict[str, Path] = dict(staged)
    resolved.update({k: v for k, v in destinations.items() if v is not None})

    if dry_run:
        print("[dry-run] skipping writes")
        print(f"  would write topography features: {stats.topo_features}")
        print(f"  would write country features: {stats.country_features}")
        for key, path in resolved.items():
            print(f"  would write {key} → {path}")
        return stats

    json_writes = [
        ("topography", resolved["topography"], topo_payload),
        ("boundaries_json", resolved["boundaries_json"], boundary_payload),
    ]
    # Also mirror to staging when Kotlin (or other) dest differs.
    written: set[Path] = set()
    for _label, path, payload in json_writes:
        if path not in written:
            _write_json(payload, path)
            stats.outputs.append(path)
            written.add(path)
    for key, payload in (
        ("topography", topo_payload),
        ("boundaries_json", boundary_payload),
    ):
        stage_path = staged[key]
        if stage_path not in written:
            _write_json(payload, stage_path)
            stats.outputs.append(stage_path)
            written.add(stage_path)

    if not skip_topojson:
        topojson_dest = resolved["boundaries_topojson"]
        try:
            for path in {topojson_dest, staged["boundaries_topojson"]}:
                if path in written:
                    continue
                _write_topojson(countries_gdf, path)
                stats.outputs.append(path)
                written.add(path)
        except Exception as exc:  # noqa: BLE001
            stats.errors.append(f"topojson: {exc}")

    return stats


def default_kotlin_destinations(root: Path | None = None) -> dict[str, Path]:
    root = root or repo_root()
    return {
        "topography": kotlin_topo_dest(root),
        "boundaries_json": kotlin_borders_json_dest(root),
        "boundaries_topojson": kotlin_borders_topojson_dest(root),
    }
