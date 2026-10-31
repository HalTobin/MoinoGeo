"""Regions data source paths."""

from __future__ import annotations

from pathlib import Path

# Nested upstream dump (folder name includes a literal space after the underscore).
SOURCE_JSON_RELATIVE = (
    "data/db/regions/dr5hn_ countries-states-cities-database/"
    "json/countries+states+cities.json"
)

# Room @Database version — keep in sync with MoinoGeoDatabase.
ROOM_DATABASE_VERSION = 1

# Written by export after the first Room compile fills in the real hash.
# Until then an empty string skips inserting room_master_table (Room may still
# accept a matching schema on createFromAsset / open, or re-run export with --identity-hash).
DEFAULT_IDENTITY_HASH_FILE = "scripts/regions/common/room_identity_hash.txt"


def repo_root() -> Path:
    """MoinoGeo repository root (parent of ``scripts/``)."""
    return Path(__file__).resolve().parents[3]


def source_json_path(root: Path | None = None) -> Path:
    return (root or repo_root()) / SOURCE_JSON_RELATIVE


def identity_hash_path(root: Path | None = None) -> Path:
    return (root or repo_root()) / DEFAULT_IDENTITY_HASH_FILE
