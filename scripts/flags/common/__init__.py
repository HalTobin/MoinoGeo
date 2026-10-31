"""Shared flag analysis, optimization, and export helpers."""

from .catalog import FlagSet, discover_iso2_svgs, repo_root
from .optimize import OptimizationResult, optimize_svg
from .pipeline import ExportStats, export_flag_set

__all__ = [
    "ExportStats",
    "FlagSet",
    "OptimizationResult",
    "discover_iso2_svgs",
    "export_flag_set",
    "optimize_svg",
    "repo_root",
]
