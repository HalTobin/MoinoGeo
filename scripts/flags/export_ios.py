#!/usr/bin/env python3
"""Export optimized flag SVGs into a future Swift local package.

Uses the same shared catalog / optimizer as the Kotlin exporter. Destination
paths are placeholders until the iOS package layout is finalized.

Usage (from repo root):

    python3 scripts/flags/export_ios.py --dry-run
"""

from __future__ import annotations

import argparse
import sys
from pathlib import Path

_FLAGS_DIR = Path(__file__).resolve().parent
if str(_FLAGS_DIR.parent) not in sys.path:
    sys.path.insert(0, str(_FLAGS_DIR.parent))

from flags.common.catalog import FLAG_SETS, FlagSet, repo_root  # noqa: E402
from flags.common.pipeline import export_flag_set  # noqa: E402

# TODO: point these at the Swift package resource folders once created.
IOS_DESTINATIONS: dict[str, str] = {
    "yummygum-flagpack": "ios/Flags/Sources/Flagpack/Resources",
    "hatscripts-circle": "ios/Flags/Sources/CircleFlags/Resources",
    "lipis-square": "ios/Flags/Sources/LipisSquare/Resources",
    "lipis-rectangle": "ios/Flags/Sources/LipisRectangle/Resources",
}


def _select_sets(only: list[str] | None) -> list[FlagSet]:
    if not only:
        return list(FLAG_SETS)
    wanted = set(only)
    unknown = wanted - {fs.id for fs in FLAG_SETS}
    if unknown:
        known = ", ".join(fs.id for fs in FLAG_SETS)
        raise SystemExit(f"Unknown flag set(s): {', '.join(sorted(unknown))}. Known: {known}")
    return [fs for fs in FLAG_SETS if fs.id in wanted]


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(
        description=(
            "Export ISO-2 flag SVGs from data/flags into an iOS Swift package "
            "(destinations are placeholders until the package exists)."
        )
    )
    parser.add_argument("--root", type=Path, default=None)
    parser.add_argument("--only", nargs="+", metavar="SET_ID")
    parser.add_argument("--dry-run", action="store_true")
    parser.add_argument("--clean", action="store_true")
    parser.add_argument("--force-optimize", action="store_true")
    args = parser.parse_args(argv)

    root = (args.root or repo_root()).resolve()
    sets = _select_sets(args.only)

    print(f"Repo root: {root}")
    print(
        "iOS destinations are placeholders — update IOS_DESTINATIONS in "
        "export_ios.py when the Swift package lands."
    )
    if args.dry_run:
        print("(dry run — no files will be written)")

    all_ok = True
    for flag_set in sets:
        dest_rel = IOS_DESTINATIONS.get(flag_set.id)
        if not dest_rel:
            print(f"\n[{flag_set.id}] No iOS destination configured — skipped.")
            continue

        dest = root / dest_rel
        if not args.dry_run and not dest.parent.exists():
            print(
                f"\n[{flag_set.id}] Skipping write — parent path does not exist yet: "
                f"{dest.parent}"
            )
            print("  Re-run after creating the Swift package, or use --dry-run.")
            continue

        stats = export_flag_set(
            flag_set,
            dest,
            root=root,
            dry_run=args.dry_run,
            force_optimize=args.force_optimize,
            clean=args.clean,
        )
        stats.print_summary()
        if stats.errors:
            all_ok = False

    return 0 if all_ok else 1


if __name__ == "__main__":
    raise SystemExit(main())
