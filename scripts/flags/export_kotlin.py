#!/usr/bin/env python3
"""Export optimized flag SVGs into Kotlin Multiplatform flag modules.

Shared analysis / lossless optimization lives in ``scripts.flags.common``.
This script only maps each upstream pack to its Kotlin resource directory.

Usage (from repo root):

    python3 scripts/flags/export_kotlin.py
    python3 scripts/flags/export_kotlin.py --dry-run
    python3 scripts/flags/export_kotlin.py --only yummygum-flagpack lipis-square
    python3 scripts/flags/export_kotlin.py --clean
"""

from __future__ import annotations

import argparse
import sys
from pathlib import Path

# Allow running as ``python3 scripts/flags/export_kotlin.py`` without install.
_FLAGS_DIR = Path(__file__).resolve().parent
if str(_FLAGS_DIR.parent) not in sys.path:
    sys.path.insert(0, str(_FLAGS_DIR.parent))

from flags.common.catalog import FLAG_SETS, FlagSet, repo_root  # noqa: E402
from flags.common.pipeline import export_flag_set  # noqa: E402

# Kotlin resource destinations relative to the repository root.
KOTLIN_DESTINATIONS: dict[str, str] = {
    "yummygum-flagpack": (
        "kotlin/flags/yummygum-flagpack/src/commonMain/resources/flags-flagpack"
    ),
    "hatscripts-circle": (
        "kotlin/flags/hatscripts-flags-circle/src/commonMain/resources/flags-circle"
    ),
    "lipis-square": (
        "kotlin/flags/lipis-flag-icons-square/src/commonMain/resources/flags-lipis-square"
    ),
    "lipis-rectangle": (
        "kotlin/flags/lipis-flag-icons-rectangle/src/commonMain/resources/flags-lipis-rectangle"
    ),
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
        description="Export ISO-2 flag SVGs from data/flags into kotlin/flags modules."
    )
    parser.add_argument(
        "--root",
        type=Path,
        default=None,
        help="Repository root (default: auto-detect).",
    )
    parser.add_argument(
        "--only",
        nargs="+",
        metavar="SET_ID",
        help="Limit export to one or more flag-set ids.",
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Analyze and optimize in memory without writing files.",
    )
    parser.add_argument(
        "--clean",
        action="store_true",
        help="Delete existing *.svg in each destination before writing.",
    )
    parser.add_argument(
        "--force-optimize",
        action="store_true",
        help="Always run the optimizer even when no comments/metadata are present.",
    )
    args = parser.parse_args(argv)

    root = (args.root or repo_root()).resolve()
    sets = _select_sets(args.only)

    print(f"Repo root: {root}")
    print(f"Exporting {len(sets)} flag set(s) → Kotlin modules")
    if args.dry_run:
        print("(dry run — no files will be written)")

    all_ok = True
    totals = {
        "discovered": 0,
        "written": 0,
        "optimized": 0,
        "bytes_before": 0,
        "bytes_after": 0,
    }

    for flag_set in sets:
        dest_rel = KOTLIN_DESTINATIONS.get(flag_set.id)
        if not dest_rel:
            print(f"\n[{flag_set.id}] No Kotlin destination configured — skipped.")
            continue

        stats = export_flag_set(
            flag_set,
            root / dest_rel,
            root=root,
            dry_run=args.dry_run,
            force_optimize=args.force_optimize,
            clean=args.clean,
        )
        stats.print_summary()
        totals["discovered"] += stats.discovered
        totals["written"] += stats.written
        totals["optimized"] += stats.optimized
        totals["bytes_before"] += stats.bytes_before
        totals["bytes_after"] += stats.bytes_after
        if stats.errors:
            all_ok = False

    saved = totals["bytes_before"] - totals["bytes_after"]
    pct = (100.0 * saved / totals["bytes_before"]) if totals["bytes_before"] else 0.0
    print("\n=== Kotlin export totals ===")
    print(f"  discovered: {totals['discovered']}")
    print(f"  written:    {totals['written']}")
    print(f"  optimized:  {totals['optimized']}")
    print(
        f"  size:       {totals['bytes_before'] / 1024:.1f} KB → "
        f"{totals['bytes_after'] / 1024:.1f} KB ({pct:.1f}% reduction)"
    )

    return 0 if all_ok else 1


if __name__ == "__main__":
    raise SystemExit(main())
