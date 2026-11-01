"""Natural Earth sources, zone mapping, and repo paths.

Natural Earth vectors only cover a subset of ``TopoZone``:

- WATER  ← ocean, lakes
- SNOW   ← glaciated areas, Antarctic ice shelves
- SAND   ← playas (sparse)
- NEUTRAL← land (generic fill; keep last so specific zones win in point-in-polygon)

FOREST / GRASSLAND / AGRICULTURE / ROCK are not available as NE polygons.
"""

from __future__ import annotations

from dataclasses import dataclass
from pathlib import Path
from typing import Literal

Scale = Literal["110m", "50m", "10m"]

SCALES: tuple[Scale, ...] = ("110m", "50m", "10m")

# Default for ~300–3000 km/inch offline maps.
DEFAULT_SCALE: Scale = "50m"

# CDN mirrors Natural Earth downloads (stable, no HTML interstitial).
NE_CDN_BASE = "https://naciscdn.org/naturalearth"

# Degrees. Extra Douglas–Peucker after NE's own generalization.
# 50m is already coarse; keep light. Raise for smaller payloads.
DEFAULT_SIMPLIFY_TOLERANCE: dict[Scale, float] = {
    "110m": 0.0,
    "50m": 0.02,
    "10m": 0.05,
}

# Drop rings / parts smaller than this (sq degrees ≈ rough filter).
DEFAULT_MIN_AREA: dict[Scale, float] = {
    "110m": 0.0,
    "50m": 0.002,
    "10m": 0.01,
}


@dataclass(frozen=True)
class PhysicalLayer:
    """One Natural Earth physical shapefile → TopoZone."""

    theme: str  # physical | cultural
    name: str  # e.g. ne_50m_ocean (scale filled in)
    zone: str  # TopoZone name
    # Lower = drawn / matched earlier. Land (NEUTRAL) must be highest.
    priority: int


# ``name`` uses ``{scale}`` placeholder, e.g. "ne_{scale}_ocean".
PHYSICAL_LAYERS: tuple[PhysicalLayer, ...] = (
    PhysicalLayer("physical", "ne_{scale}_lakes", "WATER", priority=10),
    PhysicalLayer("physical", "ne_{scale}_glaciated_areas", "SNOW", priority=20),
    PhysicalLayer(
        "physical", "ne_{scale}_antarctic_ice_shelves", "SNOW", priority=21
    ),
    PhysicalLayer("physical", "ne_{scale}_playas", "SAND", priority=30),
    PhysicalLayer("physical", "ne_{scale}_ocean", "WATER", priority=40),
    # Generic land last so findZoneAt prefers lakes/glaciers/playas.
    PhysicalLayer("physical", "ne_{scale}_land", "NEUTRAL", priority=100),
)

COUNTRIES_LAYER = PhysicalLayer(
    "cultural", "ne_{scale}_admin_0_countries", "", priority=0
)


def repo_root() -> Path:
    """MoinoGeo repository root (parent of ``scripts/``)."""
    return Path(__file__).resolve().parents[3]


def cache_dir(root: Path | None = None, scale: Scale = DEFAULT_SCALE) -> Path:
    return (root or repo_root()) / "data" / "natural_earth" / scale


def staging_dir(root: Path | None = None) -> Path:
    return (root or repo_root()) / "data" / "db" / "topo"


def layer_basename(template: str, scale: Scale) -> str:
    return template.format(scale=scale)


def layer_zip_url(layer: PhysicalLayer, scale: Scale) -> str:
    base = layer_basename(layer.name, scale)
    return f"{NE_CDN_BASE}/{scale}/{layer.theme}/{base}.zip"


def kotlin_topo_dest(root: Path | None = None) -> Path:
    return (
        (root or repo_root())
        / "kotlin/maps/topography/src/commonMain/resources/topography/topography_data.json"
    )


def kotlin_borders_topojson_dest(root: Path | None = None) -> Path:
    return (
        (root or repo_root())
        / "kotlin/maps/countries/src/commonMain/resources/countries/world-50m.topojson"
    )


def kotlin_borders_json_dest(root: Path | None = None) -> Path:
    """Flat BoundaryData JSON (debug / alternate to TopoJSON)."""
    return (
        (root or repo_root())
        / "kotlin/maps/countries/src/commonMain/resources/countries/boundary_data.json"
    )
