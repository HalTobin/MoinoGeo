#!/usr/bin/env python3
"""Export the slim regions SQLite DB into a future Swift / Darwin package.

Destinations are placeholders until the Darwin package layout is finalized.

Usage (from repo root):

    python3 scripts/regions/export_darwin.py --dry-run
"""

from __future__ import annotations

import argparse
import sys
from pathlib import Path

_REGIONS_DIR = Path(__file__).resolve().parent
if str(_REGIONS_DIR.parent) not in sys.path:
    sys.path.insert(0, str(_REGIONS_DIR.parent))

from regions.common.catalog import repo_root  # noqa: E402
from regions.common.pipeline import build_database  # noqa: E402

# TODO: point at the Swift package resource folder once created.
DARWIN_DESTINATIONS: tuple[str, ...] = (
    "darwin/MoinoGeoCore/Sources/MoinoGeoCore/Resources/databases/moinogeo.db",
)


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(
        description=(
            "Build moinogeo.db for a future Darwin/Swift package "
            "(destinations are placeholders)."
        )
    )
    parser.add_argument("--root", type=Path, default=None)
    parser.add_argument("--dry-run", action="store_true")
    parser.add_argument("--identity-hash", default=None)
    args = parser.parse_args(argv)

    root = (args.root or repo_root()).resolve()
    destinations = [root / rel for rel in DARWIN_DESTINATIONS]

    print(f"Repo root: {root}")
    print(
        "Darwin destinations are placeholders — update DARWIN_DESTINATIONS in "
        "export_darwin.py when the Swift package lands."
    )
    if args.dry_run:
        print("(dry run — no files will be written)")

    writable: list[Path] = []
    for dest in destinations:
        if args.dry_run or dest.parent.exists() or dest.parent.parent.exists():
            writable.append(dest)
        else:
            print(
                f"Skipping write — parent path does not exist yet: {dest.parent}\n"
                "  Re-run after creating the Darwin package, or use --dry-run."
            )

    if not writable and not args.dry_run:
        return 0

    stats = build_database(
        writable or destinations,
        root=root,
        dry_run=args.dry_run or not writable,
        identity_hash=args.identity_hash,
    )
    stats.print_summary()
    return 0 if not stats.errors else 1


if __name__ == "__main__":
    raise SystemExit(main())
