"""Flag source catalog and ISO-2 SVG discovery."""

from __future__ import annotations

import re
from dataclasses import dataclass
from pathlib import Path

_ISO2_SVG_RE = re.compile(r"^[A-Za-z]{2}\.svg$")


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


def discover_iso2_svgs(source_dir: Path, *, recursive: bool = False) -> list[Path]:
    """List SVG files whose basename is a 2-letter ISO code (e.g. ``fr.svg``).

    Regional variants (``gb-eng.svg``), multi-part codes (``BQ-BO.svg``), and
    non-ISO names under nested folders (e.g. ``flags/other/``) are skipped.
    """
    if not source_dir.is_dir():
        raise FileNotFoundError(f"Flag source directory not found: {source_dir}")

    files: list[Path] = []
    iterator = source_dir.rglob("*.svg") if recursive else source_dir.glob("*.svg")
    for path in iterator:
        if not path.is_file():
            continue
        if not _ISO2_SVG_RE.match(path.name):
            continue
        # Non-recursive: ignore anything under a subdirectory (e.g. other/).
        if not recursive and path.parent != source_dir:
            continue
        files.append(path)

    return sorted(files, key=lambda p: p.name.lower())


def normalized_iso2_filename(path: Path) -> str:
    """Return lowercase ``xx.svg`` for an ISO-2 source path."""
    return path.name.lower()
