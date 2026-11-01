"""Download and cache Natural Earth shapefile zips."""

from __future__ import annotations

import io
import urllib.request
import zipfile
from pathlib import Path

from .catalog import (
    COUNTRIES_LAYER,
    PHYSICAL_LAYERS,
    PhysicalLayer,
    Scale,
    cache_dir,
    layer_basename,
    layer_zip_url,
)


def _download_zip(url: str, dest_dir: Path) -> Path:
    dest_dir.mkdir(parents=True, exist_ok=True)
    marker = dest_dir / ".extracted"
    if marker.is_file():
        return dest_dir

    print(f"  Downloading {url}")
    with urllib.request.urlopen(url, timeout=120) as resp:
        data = resp.read()

    with zipfile.ZipFile(io.BytesIO(data)) as zf:
        zf.extractall(dest_dir)
    marker.write_text(url + "\n", encoding="utf-8")
    return dest_dir


def shapefile_path(cache: Path, basename: str) -> Path:
    """Find ``basename.shp`` under the layer cache folder."""
    direct = cache / f"{basename}.shp"
    if direct.is_file():
        return direct
    matches = list(cache.rglob(f"{basename}.shp"))
    if not matches:
        raise FileNotFoundError(f"No shapefile for {basename} under {cache}")
    return matches[0]


def ensure_layer(layer: PhysicalLayer, scale: Scale, root: Path) -> Path:
    basename = layer_basename(layer.name, scale)
    layer_cache = cache_dir(root, scale) / basename
    _download_zip(layer_zip_url(layer, scale), layer_cache)
    return shapefile_path(layer_cache, basename)


def ensure_physical_layers(scale: Scale, root: Path) -> dict[str, Path]:
    paths: dict[str, Path] = {}
    for layer in PHYSICAL_LAYERS:
        basename = layer_basename(layer.name, scale)
        try:
            paths[basename] = ensure_layer(layer, scale, root)
        except Exception as exc:  # noqa: BLE001 — report and skip optional layers
            print(f"  WARN: skip {basename}: {exc}")
    return paths


def ensure_countries_layer(scale: Scale, root: Path) -> Path:
    return ensure_layer(COUNTRIES_LAYER, scale, root)
