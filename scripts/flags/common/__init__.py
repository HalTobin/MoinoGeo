"""Shared flag analysis, optimization, and export helpers."""

from .catalog import (
    FlagSet,
    discover_flag_svgs,
    discover_iso2_svgs,
    normalized_flag_filename,
    normalized_iso2_filename,
    repo_root,
)
from .optimize import OptimizationResult, optimize_svg
from .pipeline import ExportStats, export_flag_set

__all__ = [
    "ExportStats",
    "FlagSet",
    "OptimizationResult",
    "discover_flag_svgs",
    "discover_iso2_svgs",
    "export_flag_set",
    "normalized_flag_filename",
    "normalized_iso2_filename",
    "optimize_svg",
    "repo_root",
]
