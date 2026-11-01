#!/usr/bin/env python3
"""Export Natural Earth topography + countries into Kotlin maps modules.

Shared download/simplify logic lives in ``scripts.topo.common``.
This script writes resources under ``kotlin/maps/topography`` and
``kotlin/maps/countries``.

Usage (from repo root):

    python3 scripts/topo/export_kotlin.py
    python3 scripts/topo/export_kotlin.py --dry-run
    python3 scripts/topo/export_kotlin.py --scale 110m
    python3 scripts/topo/export_kotlin.py --skip-topojson
"""

from __future__ import annotations

import argparse
import sys
from pathlib import Path

_TOPO_DIR = Path(__file__).resolve().parent
if str(_TOPO_DIR.parent) not in sys.path:
    sys.path.insert(0, str(_TOPO_DIR.parent))

from topo.common.catalog import (  # noqa: E402
    DEFAULT_SCALE,
    SCALES,
    repo_root,
)
from topo.common.pipeline import (  # noqa: E402
    build_exports,
    default_kotlin_destinations,
)


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(
        description=(
            "Download Natural Earth vectors (cached under data/natural_earth/), "
            "simplify for offline mobile maps, and write Kotlin resources."
        )
    )
    parser.add_argument(
        "--root",
        type=Path,
        default=None,
        help="Repository root (default: auto-detect).",
    )
    parser.add_argument(
        "--scale",
        choices=SCALES,
        default=DEFAULT_SCALE,
        help="Natural Earth scale (default: 50m).",
    )
    parser.add_argument(
        "--simplify",
        type=float,
        default=None,
        help="Douglas–Peucker tolerance in degrees (default depends on scale).",
    )
    parser.add_argument(
        "--min-area",
        type=float,
        default=None,
        help="Drop polygon parts smaller than this area in sq degrees.",
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Download + process without writing destinations.",
    )
    parser.add_argument(
        "--skip-topojson",
        action="store_true",
        help="Only write flat JSON (skip TopoJSON for borders).",
    )
    parser.add_argument(
        "--staging-only",
        action="store_true",
        help="Write under data/db/topo only (do not touch Kotlin modules).",
    )
    args = parser.parse_args(argv)

    root = (args.root or repo_root()).resolve()
    if args.staging_only:
        destinations: dict[str, Path] = {}
    else:
        destinations = default_kotlin_destinations(root)

    print(f"Repo root: {root}")
    if destinations:
        print("Kotlin destinations:")
        for key, dest in destinations.items():
            print(f"  - {key}: {dest.relative_to(root)}")
    else:
        print("Staging only → data/db/topo/")

    stats = build_exports(
        destinations,
        root=root,
        scale=args.scale,
        simplify=args.simplify,
        min_area=args.min_area,
        dry_run=args.dry_run,
        skip_topojson=args.skip_topojson,
    )
    stats.print_summary()
    return 0 if not stats.errors else 1


if __name__ == "__main__":
    raise SystemExit(main())
