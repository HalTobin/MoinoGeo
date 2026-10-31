#!/usr/bin/env python3
"""Deprecated entry point — use ``scripts/flags/export_kotlin.py`` instead.

Kept so older docs / muscle memory still work. Forwards to the Kotlin exporter
when invoked with no args; otherwise prints migration help.
"""

from __future__ import annotations

import sys
from pathlib import Path


def main() -> int:
    print(
        "optimize_flags.py is deprecated.\n"
        "Use the shared flag tooling under scripts/flags/:\n"
        "  python3 scripts/flags/export_kotlin.py\n"
        "  python3 scripts/flags/export_ios.py   # Swift package (placeholder)\n"
        "See scripts/flags/README.md.\n",
        file=sys.stderr,
    )

    # No legacy CLI flags → run the Kotlin exporter for convenience.
    if len(sys.argv) == 1:
        flags_dir = Path(__file__).resolve().parent
        if str(flags_dir) not in sys.path:
            sys.path.insert(0, str(flags_dir))
        from flags.export_kotlin import main as export_kotlin_main

        return export_kotlin_main([])

    print(
        "Legacy --input/--output flags are no longer supported.\n"
        "Pass options to export_kotlin.py (e.g. --dry-run, --only, --clean).",
        file=sys.stderr,
    )
    return 2


if __name__ == "__main__":
    raise SystemExit(main())
