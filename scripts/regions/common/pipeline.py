"""Build a slim SQLite database from the nested countries+states+cities JSON."""

from __future__ import annotations

import json
import shutil
import sqlite3
import tempfile
from dataclasses import dataclass, field
from pathlib import Path

from .catalog import ROOM_DATABASE_VERSION, identity_hash_path, source_json_path
from .schema import SCHEMA_SQL


def _as_float(value: object) -> float | None:
    if value is None or value == "":
        return None
    try:
        return float(value)  # type: ignore[arg-type]
    except (TypeError, ValueError):
        return None


@dataclass
class BuildStats:
    countries: int = 0
    states: int = 0
    cities: int = 0
    translations: int = 0
    regions: int = 0
    subregions: int = 0
    bytes_out: int = 0
    destinations: list[str] = field(default_factory=list)
    identity_hash: str | None = None
    errors: list[str] = field(default_factory=list)

    def print_summary(self) -> None:
        print("\n=== Regions DB build ===")
        print(f"  regions:      {self.regions}")
        print(f"  subregions:   {self.subregions}")
        print(f"  countries:    {self.countries}")
        print(f"  states:       {self.states}")
        print(f"  cities:       {self.cities}")
        print(f"  translations: {self.translations}")
        if self.identity_hash:
            print(f"  identity:     {self.identity_hash}")
        else:
            print("  identity:     (not set — pass --identity-hash after Room compile)")
        print(f"  size:         {self.bytes_out / 1024 / 1024:.2f} MB")
        for dest in self.destinations:
            print(f"  wrote:        {dest}")
        if self.errors:
            print("  errors:")
            for err in self.errors:
                print(f"    - {err}")


def _load_identity_hash(root: Path, explicit: str | None) -> str | None:
    if explicit:
        return explicit.strip() or None
    path = identity_hash_path(root)
    if path.is_file():
        text = path.read_text(encoding="utf-8").strip()
        return text or None
    return None


def build_database(
    destinations: list[Path],
    *,
    root: Path,
    dry_run: bool = False,
    identity_hash: str | None = None,
) -> BuildStats:
    stats = BuildStats()
    json_path = source_json_path(root)
    if not json_path.is_file():
        stats.errors.append(f"Source JSON not found: {json_path}")
        return stats

    print(f"Loading {json_path} …")
    with json_path.open(encoding="utf-8") as fh:
        countries_json = json.load(fh)
    if not isinstance(countries_json, list):
        stats.errors.append("Expected top-level JSON array of countries")
        return stats

    hash_value = _load_identity_hash(root, identity_hash)
    stats.identity_hash = hash_value

    with tempfile.TemporaryDirectory(prefix="moinogeo-regions-") as tmp:
        db_path = Path(tmp) / "moinogeo.db"
        conn = sqlite3.connect(db_path)
        try:
            conn.executescript(SCHEMA_SQL)

            regions: dict[int, str] = {}
            subregions: dict[int, tuple[str, int]] = {}
            country_rows: list[tuple] = []
            state_rows: list[tuple] = []
            city_rows: list[tuple] = []
            translation_rows: list[tuple] = []

            for country in countries_json:
                cid = int(country["id"])
                region_id = country.get("region_id")
                subregion_id = country.get("subregion_id")
                region_name = country.get("region")
                subregion_name = country.get("subregion")

                if region_id is not None and region_name:
                    regions[int(region_id)] = str(region_name)
                if subregion_id is not None and subregion_name and region_id is not None:
                    subregions[int(subregion_id)] = (str(subregion_name), int(region_id))

                country_rows.append(
                    (
                        cid,
                        str(country["name"]),
                        str(country["iso2"]),
                        str(country["iso3"]),
                        country.get("capital") or None,
                        int(region_id) if region_id is not None else None,
                        int(subregion_id) if subregion_id is not None else None,
                        _as_float(country.get("latitude")),
                        _as_float(country.get("longitude")),
                    )
                )

                for lang, translated in (country.get("translations") or {}).items():
                    if translated:
                        translation_rows.append(
                            ("country", cid, str(lang), str(translated))
                        )

                for state in country.get("states") or []:
                    sid = int(state["id"])
                    state_rows.append(
                        (
                            sid,
                            cid,
                            str(state["name"]),
                            state.get("iso2") or None,
                            state.get("iso3166_2") or None,
                            state.get("type") or None,
                            _as_float(state.get("latitude")),
                            _as_float(state.get("longitude")),
                        )
                    )
                    for city in state.get("cities") or []:
                        city_rows.append(
                            (
                                int(city["id"]),
                                sid,
                                cid,
                                str(city["name"]),
                                _as_float(city.get("latitude")),
                                _as_float(city.get("longitude")),
                            )
                        )

                # Rare: cities attached directly on the country (e.g. Western Sahara).
                for city in country.get("cities") or []:
                    city_rows.append(
                        (
                            int(city["id"]),
                            None,
                            cid,
                            str(city["name"]),
                            _as_float(city.get("latitude")),
                            _as_float(city.get("longitude")),
                        )
                    )

            conn.executemany(
                "INSERT INTO regions (id, name) VALUES (?, ?)",
                sorted(regions.items()),
            )
            conn.executemany(
                "INSERT INTO subregions (id, name, region_id) VALUES (?, ?, ?)",
                [(i, name, rid) for i, (name, rid) in sorted(subregions.items())],
            )
            conn.executemany(
                "INSERT INTO countries (id, name, iso2, iso3, capital, region_id, "
                "subregion_id, latitude, longitude) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                country_rows,
            )
            conn.executemany(
                "INSERT INTO states (id, country_id, name, iso2, iso3166_2, type, "
                "latitude, longitude) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                state_rows,
            )
            conn.executemany(
                "INSERT INTO cities (id, state_id, country_id, name, latitude, longitude) "
                "VALUES (?, ?, ?, ?, ?, ?)",
                city_rows,
            )
            conn.executemany(
                "INSERT INTO translations (entity_type, entity_id, lang_code, name) "
                "VALUES (?, ?, ?, ?)",
                translation_rows,
            )

            # Room version pragma (must match @Database version).
            conn.execute(f"PRAGMA user_version = {ROOM_DATABASE_VERSION}")

            if hash_value:
                conn.execute(
                    "CREATE TABLE IF NOT EXISTS room_master_table "
                    "(id INTEGER PRIMARY KEY, identity_hash TEXT)"
                )
                conn.execute(
                    "INSERT OR REPLACE INTO room_master_table (id, identity_hash) "
                    "VALUES (42, ?)",
                    (hash_value,),
                )

            conn.commit()
            conn.execute("VACUUM")
            conn.close()
            conn = None  # type: ignore[assignment]

            stats.regions = len(regions)
            stats.subregions = len(subregions)
            stats.countries = len(country_rows)
            stats.states = len(state_rows)
            stats.cities = len(city_rows)
            stats.translations = len(translation_rows)
            stats.bytes_out = db_path.stat().st_size

            if dry_run:
                print("(dry run — database built in temp, not copied)")
                stats.destinations = [str(d) + " (skipped)" for d in destinations]
                return stats

            for dest in destinations:
                dest.parent.mkdir(parents=True, exist_ok=True)
                shutil.copy2(db_path, dest)
                stats.destinations.append(str(dest.relative_to(root) if dest.is_relative_to(root) else dest))
        finally:
            if conn is not None:
                conn.close()

    return stats
