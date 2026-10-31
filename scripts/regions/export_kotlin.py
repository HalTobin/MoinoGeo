#!/usr/bin/env python3
"""Export the slim regions SQLite DB into the Kotlin core module.

Shared JSON→SQLite logic lives in ``scripts.regions.common``.
This script writes a single shared resource under ``commonMain/resources``.

Usage (from repo root):

    python3 scripts/regions/export_kotlin.py
    python3 scripts/regions/export_kotlin.py --dry-run
    python3 scripts/regions/export_kotlin.py --identity-hash <room_hash>
"""

from __future__ import annotations

import argparse
import sys
from pathlib import Path

_REGIONS_DIR = Path(__file__).resolve().parent
if str(_REGIONS_DIR.parent) not in sys.path:
    sys.path.insert(0, str(_REGIONS_DIR.parent))

from regions.common.catalog import identity_hash_path, repo_root  # noqa: E402
from regions.common.pipeline import build_database  # noqa: E402

# Single shared destination (same pattern as kotlin/flags/*/commonMain/resources).
KOTLIN_DESTINATIONS: tuple[str, ...] = (
    "kotlin/core/src/commonMain/resources/databases/moinogeo.db",
)

# Legacy per-target copies — removed when present so we do not ship duplicates.
LEGACY_DESTINATIONS: tuple[str, ...] = (
    "kotlin/core/src/androidMain/assets/databases/moinogeo.db",
    "kotlin/core/src/jvmMain/resources/databases/moinogeo.db",
)


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(
        description=(
            "Build moinogeo.db from data/db/regions JSON and copy it into "
            "kotlin/core commonMain resources."
        )
    )
    parser.add_argument(
        "--root",
        type=Path,
        default=None,
        help="Repository root (default: auto-detect).",
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Build in a temp dir without writing destinations.",
    )
    parser.add_argument(
        "--identity-hash",
        default=None,
        help=(
            "Room identity_hash for room_master_table. "
            "Also persisted to scripts/regions/common/room_identity_hash.txt."
        ),
    )
    args = parser.parse_args(argv)

    root = (args.root or repo_root()).resolve()
    destinations = [root / rel for rel in KOTLIN_DESTINATIONS]

    print(f"Repo root: {root}")
    print(f"Destinations ({len(destinations)}):")
    for dest in destinations:
        print(f"  - {dest.relative_to(root)}")

    if args.identity_hash:
        hash_file = identity_hash_path(root)
        hash_file.parent.mkdir(parents=True, exist_ok=True)
        hash_file.write_text(args.identity_hash.strip() + "\n", encoding="utf-8")
        print(f"Saved identity hash → {hash_file.relative_to(root)}")

    stats = build_database(
        destinations,
        root=root,
        dry_run=args.dry_run,
        identity_hash=args.identity_hash,
    )

    if not args.dry_run and not stats.errors:
        for rel in LEGACY_DESTINATIONS:
            legacy = root / rel
            if legacy.is_file():
                legacy.unlink()
                print(f"Removed legacy copy: {rel}")
                parent = legacy.parent
                # Drop empty databases/ folders left behind.
                if parent.is_dir() and not any(parent.iterdir()):
                    parent.rmdir()

    stats.print_summary()
    return 0 if not stats.errors else 1


if __name__ == "__main__":
    raise SystemExit(main())
