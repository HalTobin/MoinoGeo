#!/usr/bin/env python3
"""Stub: export Natural Earth map assets into a future Swift package.

Shared pipeline lives in ``scripts.topo.common``. Destination paths are
placeholders until the iOS package layout is finalized.

Usage (from repo root):

    python3 scripts/topo/export_darwin.py --dry-run
"""

from __future__ import annotations

import argparse
import sys
from pathlib import Path

_TOPO_DIR = Path(__file__).resolve().parent
if str(_TOPO_DIR.parent) not in sys.path:
    sys.path.insert(0, str(_TOPO_DIR.parent))

from topo.common.catalog import DEFAULT_SCALE, SCALES, repo_root  # noqa: E402
from topo.common.pipeline import build_exports  # noqa: E402

# TODO: point these at Swift package resource folders once created.
IOS_DESTINATIONS: dict[str, str] = {
    "topography": "ios/Maps/Sources/Topography/Resources/topography_data.json",
    "boundaries_json": "ios/Maps/Sources/Countries/Resources/boundary_data.json",
    "boundaries_topojson": "ios/Maps/Sources/Countries/Resources/world-50m.topojson",
}


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(
        description="Export Natural Earth map assets for a future Darwin package."
    )
    parser.add_argument("--root", type=Path, default=None)
    parser.add_argument("--scale", choices=SCALES, default=DEFAULT_SCALE)
    parser.add_argument("--simplify", type=float, default=None)
    parser.add_argument("--min-area", type=float, default=None)
    parser.add_argument("--dry-run", action="store_true")
    parser.add_argument("--skip-topojson", action="store_true")
    args = parser.parse_args(argv)

    root = (args.root or repo_root()).resolve()
    destinations = {key: root / rel for key, rel in IOS_DESTINATIONS.items()}

    print(f"Repo root: {root}")
    print("Darwin destinations (placeholders):")
    for key, dest in destinations.items():
        print(f"  - {key}: {dest.relative_to(root)}")

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
