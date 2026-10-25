#!/usr/bin/env python3
"""
build_database.py - Compiles raw geographic JSON datasets into an optimized SQLite database (moinogeo.db).

Dataset source: dr5hn/countries-states-cities-database (ODbL v1.0)
https://github.com/dr5hn/countries-states-cities-database

Expected input files (either individual or combined):
  - countries.json
  - states.json
  - cities.json
  OR
  - countries+states+cities.json
"""

import argparse
import json
import os
import sqlite3
import sys
import time


def create_schema(cursor: sqlite3.Cursor):
    """Creates tables and indexes for countries, states, and cities."""
    cursor.executescript("""
        -- Countries Table
        CREATE TABLE IF NOT EXISTS countries (
            id              INTEGER PRIMARY KEY AUTOINCREMENT,
            name            TEXT NOT NULL,
            iso2            TEXT NOT NULL UNIQUE,
            iso3            TEXT NOT NULL,
            numeric_code    TEXT,
            phone_code      TEXT,
            capital         TEXT,
            currency        TEXT,
            currency_symbol TEXT,
            tld             TEXT,
            region          TEXT,
            subregion       TEXT,
            latitude        REAL,
            longitude       REAL,
            emoji           TEXT,
            translations    TEXT
        );

        -- States Table
        CREATE TABLE IF NOT EXISTS states (
            id              INTEGER PRIMARY KEY AUTOINCREMENT,
            name            TEXT NOT NULL,
            country_id      INTEGER NOT NULL,
            country_code    TEXT NOT NULL,
            state_code      TEXT,
            type            TEXT,
            latitude        REAL,
            longitude       REAL,
            FOREIGN KEY(country_id) REFERENCES countries(id)
        );

        -- Cities Table
        CREATE TABLE IF NOT EXISTS cities (
            id              INTEGER PRIMARY KEY AUTOINCREMENT,
            name            TEXT NOT NULL,
            state_id        INTEGER NOT NULL,
            state_code      TEXT NOT NULL,
            country_id      INTEGER NOT NULL,
            country_code    TEXT NOT NULL,
            latitude        REAL NOT NULL,
            longitude       REAL NOT NULL,
            wiki_data_id    TEXT,
            FOREIGN KEY(state_id) REFERENCES states(id),
            FOREIGN KEY(country_id) REFERENCES countries(id)
        );
    """)


def create_indexes(cursor: sqlite3.Cursor):
    """Creates B-tree indexes for fast lookup and search performance."""
    cursor.executescript("""
        CREATE UNIQUE INDEX IF NOT EXISTS idx_countries_iso2 ON countries(iso2);
        CREATE INDEX IF NOT EXISTS idx_countries_region ON countries(region);

        CREATE INDEX IF NOT EXISTS idx_states_country_id ON states(country_id);
        CREATE INDEX IF NOT EXISTS idx_states_country_code ON states(country_code);

        CREATE INDEX IF NOT EXISTS idx_cities_state_id ON cities(state_id);
        CREATE INDEX IF NOT EXISTS idx_cities_country_id ON cities(country_id);
        CREATE INDEX IF NOT EXISTS idx_cities_country_code ON cities(country_code);
        CREATE INDEX IF NOT EXISTS idx_cities_name ON cities(name COLLATE NOCASE);
    """)


def safe_float(val):
    if val is None or val == "":
        return None
    try:
        return float(val)
    except (ValueError, TypeError):
        return None


def safe_int(val):
    if val is None or val == "":
        return None
    try:
        return int(val)
    except (ValueError, TypeError):
        return None


def load_json(filepath):
    print(f"Reading {filepath}...")
    with open(filepath, "r", encoding="utf-8") as f:
        return json.load(f)


def process_combined_data(data, cursor):
    """Processes combined countries+states+cities structure."""
    print("Processing combined dataset...")
    countries_count = 0
    states_count = 0
    cities_count = 0

    for country in data:
        c_id = country.get("id")
        c_name = country.get("name", "")
        c_iso2 = country.get("iso2", "")
        c_iso3 = country.get("iso3", "")
        c_num = country.get("numeric_code")
        c_phone = country.get("phone_code")
        c_capital = country.get("capital")
        c_currency = country.get("currency")
        c_currency_sym = country.get("currency_symbol")
        c_tld = country.get("tld")
        c_region = country.get("region")
        c_subregion = country.get("subregion")
        c_lat = safe_float(country.get("latitude"))
        c_lng = safe_float(country.get("longitude"))
        c_emoji = country.get("emoji")
        c_trans = json.dumps(country.get("translations", {}), ensure_ascii=False)

        cursor.execute("""
            INSERT INTO countries (id, name, iso2, iso3, numeric_code, phone_code, capital,
                                  currency, currency_symbol, tld, region, subregion,
                                  latitude, longitude, emoji, translations)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """, (c_id, c_name, c_iso2, c_iso3, c_num, c_phone, c_capital, c_currency,
              c_currency_sym, c_tld, c_region, c_subregion, c_lat, c_lng, c_emoji, c_trans))
        countries_count += 1

        for state in country.get("states", []):
            s_id = state.get("id")
            s_name = state.get("name", "")
            s_country_id = c_id
            s_country_code = state.get("country_code", c_iso2)
            s_state_code = state.get("state_code")
            s_type = state.get("type")
            s_lat = safe_float(state.get("latitude"))
            s_lng = safe_float(state.get("longitude"))

            cursor.execute("""
                INSERT INTO states (id, name, country_id, country_code, state_code, type, latitude, longitude)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """, (s_id, s_name, s_country_id, s_country_code, s_state_code, s_type, s_lat, s_lng))
            states_count += 1

            for city in state.get("cities", []):
                ct_id = city.get("id")
                ct_name = city.get("name", "")
                ct_state_id = s_id
                ct_state_code = city.get("state_code", s_state_code or "")
                ct_country_id = c_id
                ct_country_code = city.get("country_code", c_iso2)
                ct_lat = safe_float(city.get("latitude")) or 0.0
                ct_lng = safe_float(city.get("longitude")) or 0.0
                ct_wiki = city.get("wikiDataId")

                cursor.execute("""
                    INSERT INTO cities (id, name, state_id, state_code, country_id, country_code, latitude, longitude, wiki_data_id)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, (ct_id, ct_name, ct_state_id, ct_state_code, ct_country_id, ct_country_code, ct_lat, ct_lng, ct_wiki))
                cities_count += 1

    return countries_count, states_count, cities_count


def process_separate_files(input_dir, cursor):
    """Processes separate countries.json, states.json, cities.json files."""
    countries_file = os.path.join(input_dir, "countries.json")
    states_file = os.path.join(input_dir, "states.json")
    cities_file = os.path.join(input_dir, "cities.json")

    if not os.path.exists(countries_file):
        raise FileNotFoundError(f"Missing required file: {countries_file}")

    countries = load_json(countries_file)
    print(f"Importing {len(countries)} countries...")
    for c in countries:
        c_trans = json.dumps(c.get("translations", {}), ensure_ascii=False)
        cursor.execute("""
            INSERT INTO countries (id, name, iso2, iso3, numeric_code, phone_code, capital,
                                  currency, currency_symbol, tld, region, subregion,
                                  latitude, longitude, emoji, translations)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """, (c.get("id"), c.get("name"), c.get("iso2"), c.get("iso3"), c.get("numeric_code"),
              c.get("phone_code"), c.get("capital"), c.get("currency"), c.get("currency_symbol"),
              c.get("tld"), c.get("region"), c.get("subregion"), safe_float(c.get("latitude")),
              safe_float(c.get("longitude")), c.get("emoji"), c_trans))

    states_count = 0
    if os.path.exists(states_file):
        states = load_json(states_file)
        print(f"Importing {len(states)} states...")
        for s in states:
            cursor.execute("""
                INSERT INTO states (id, name, country_id, country_code, state_code, type, latitude, longitude)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """, (s.get("id"), s.get("name"), s.get("country_id"), s.get("country_code"),
                  s.get("state_code"), s.get("type"), safe_float(s.get("latitude")), safe_float(s.get("longitude"))))
            states_count += 1

    cities_count = 0
    if os.path.exists(cities_file):
        cities = load_json(cities_file)
        print(f"Importing {len(cities)} cities...")
        for ct in cities:
            cursor.execute("""
                INSERT INTO cities (id, name, state_id, state_code, country_id, country_code, latitude, longitude, wiki_data_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """, (ct.get("id"), ct.get("name"), ct.get("state_id"), ct.get("state_code", ""),
                  ct.get("country_id"), ct.get("country_code", ""), safe_float(ct.get("latitude")) or 0.0,
                  safe_float(ct.get("longitude")) or 0.0, ct.get("wikiDataId")))
            cities_count += 1

    return len(countries), states_count, cities_count


def build_database(input_path: str, output_db: str):
    start_time = time.time()
    os.makedirs(os.path.dirname(os.path.abspath(output_db)), exist_ok=True)

    if os.path.exists(output_db):
        print(f"Removing existing database file: {output_db}")
        os.remove(output_db)

    print(f"Creating database at {output_db}...")
    conn = sqlite3.connect(output_db)
    cursor = conn.cursor()

    # Performance pragmas for bulk load
    cursor.execute("PRAGMA page_size = 4096;")
    cursor.execute("PRAGMA journal_mode = MEMORY;")
    cursor.execute("PRAGMA synchronous = OFF;")

    create_schema(cursor)

    if os.path.isfile(input_path) and input_path.endswith(".json"):
        data = load_json(input_path)
        c_count, s_count, ct_count = process_combined_data(data, cursor)
    elif os.path.isdir(input_path):
        combined_file = os.path.join(input_path, "countries+states+cities.json")
        if os.path.exists(combined_file):
            data = load_json(combined_file)
            c_count, s_count, ct_count = process_combined_data(data, cursor)
        else:
            c_count, s_count, ct_count = process_separate_files(input_path, cursor)
    else:
        raise ValueError(f"Invalid input path: {input_path}")

    print("Building database indexes...")
    create_indexes(cursor)

    conn.commit()

    print("Optimizing database storage (VACUUM & ANALYZE)...")
    cursor.execute("PRAGMA journal_mode = DELETE;")
    cursor.execute("PRAGMA synchronous = NORMAL;")
    cursor.execute("ANALYZE;")
    cursor.execute("VACUUM;")

    conn.close()

    db_size_mb = os.path.getsize(output_db) / (1024 * 1024)
    elapsed = time.time() - start_time

    print(f"\nSuccessfully built database!")
    print(f"  - Countries: {c_count}")
    print(f"  - States:    {s_count}")
    print(f"  - Cities:    {ct_count}")
    print(f"  - File Size: {db_size_mb:.2f} MB")
    print(f"  - Duration:  {elapsed:.2f} seconds")


def main():
    parser = argparse.ArgumentParser(description="Build MoinoGeo SQLite database from raw JSON source.")
    parser.add_argument("--input", "-i", required=True, help="Input directory or combined JSON file path.")
    parser.add_argument("--output", "-o", default="data/moinogeo.db", help="Output SQLite database path.")

    args = parser.parse_args()
    try:
        build_database(args.input, args.output)
    except Exception as e:
        print(f"Error building database: {e}", file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    main()
