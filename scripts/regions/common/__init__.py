"""Shared regions JSON → SQLite helpers."""

from .catalog import SOURCE_JSON_RELATIVE, repo_root, source_json_path
from .pipeline import BuildStats, build_database

__all__ = [
    "BuildStats",
    "SOURCE_JSON_RELATIVE",
    "build_database",
    "repo_root",
    "source_json_path",
]
