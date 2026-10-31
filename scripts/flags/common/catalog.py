"""Flag source catalog and SVG discovery."""

from __future__ import annotations

import re
from dataclasses import dataclass
from pathlib import Path

# ISO-2 (``fr.svg``), regional / subdivision (``gb-eng.svg``, ``BQ-BO.svg``),
# and named / org flags (``arab.svg``, ``european_union.svg``).
_FLAG_SVG_RE = re.compile(r"^[A-Za-z0-9_-]+\.svg$")


def repo_root() -> Path:
    """MoinoGeo repository root (parent of ``scripts/``)."""
    return Path(__file__).resolve().parents[3]


@dataclass(frozen=True)
class FlagSet:
    """One upstream flag pack mapped to a platform resource directory."""

    id: str
    source_relative: str
    description: str

    def source_dir(self, root: Path | None = None) -> Path:
        return (root or repo_root()) / self.source_relative


# Upstream packs under data/flags/ (shared by all platform exporters).
FLAG_SETS: tuple[FlagSet, ...] = (
    FlagSet(
        id="yummygum-flagpack",
        source_relative="data/flags/Yummygum_flagpack-core/svg_l",
        description="Yummygum flagpack-core (stylized rounded rectangles)",
    ),
    FlagSet(
        id="hatscripts-circle",
        source_relative="data/flags/HatScripts_circle-flags/flags",
        description="HatScripts circle-flags",
    ),
    FlagSet(
        id="lipis-square",
        source_relative="data/flags/lipis_flag-icons/flags/1x1",
        description="lipis/flag-icons square (1×1)",
    ),
    FlagSet(
        id="lipis-rectangle",
        source_relative="data/flags/lipis_flag-icons/flags/4x3",
        description="lipis/flag-icons rectangle (4×3)",
    ),
)


def discover_flag_svgs(source_dir: Path, *, recursive: bool = False) -> list[Path]:
    """List exportable flag SVG files under *source_dir*.

    Includes ISO 3166-1 alpha-2 codes (``fr.svg``), regional / subdivision
    variants (``gb-eng.svg``, ``BQ-BO.svg``), and named / organization flags
    (``arab.svg``, ``european_union.svg``).

    Nested folders (e.g. ``flags/language/``, ``flags/other/``) are skipped
    unless *recursive* is True, to avoid flattening basename collisions with
    country codes.
    """
    if not source_dir.is_dir():
        raise FileNotFoundError(f"Flag source directory not found: {source_dir}")

    files: list[Path] = []
    iterator = source_dir.rglob("*.svg") if recursive else source_dir.glob("*.svg")
    for path in iterator:
        if not path.is_file():
            continue
        if not _FLAG_SVG_RE.match(path.name):
            continue
        # Non-recursive: ignore anything under a subdirectory (e.g. other/).
        if not recursive and path.parent != source_dir:
            continue
        files.append(path)

    return sorted(files, key=lambda p: p.name.lower())


def discover_iso2_svgs(source_dir: Path, *, recursive: bool = False) -> list[Path]:
    """Alias for [discover_flag_svgs] — kept for older call sites."""
    return discover_flag_svgs(source_dir, recursive=recursive)


def normalized_flag_filename(path: Path) -> str:
    """Return lowercase ``code.svg`` for a flag source path."""
    return path.name.lower()


def normalized_iso2_filename(path: Path) -> str:
    """Alias for [normalized_flag_filename] — kept for older call sites."""
    return normalized_flag_filename(path)
