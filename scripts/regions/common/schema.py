"""SQLite DDL matching the Room entities in kotlin/core."""

from __future__ import annotations

# Must stay aligned with:
#   com.moinofactory.moinogeo.core.data.entity.*
#   MoinoGeoDatabase version
SCHEMA_SQL = """
PRAGMA foreign_keys = OFF;

CREATE TABLE IF NOT EXISTS regions (
    id INTEGER NOT NULL,
    name TEXT NOT NULL,
    PRIMARY KEY(id)
);

CREATE TABLE IF NOT EXISTS subregions (
    id INTEGER NOT NULL,
    name TEXT NOT NULL,
    region_id INTEGER NOT NULL,
    PRIMARY KEY(id)
);

CREATE TABLE IF NOT EXISTS countries (
    id INTEGER NOT NULL,
    name TEXT NOT NULL,
    iso2 TEXT NOT NULL,
    iso3 TEXT NOT NULL,
    capital TEXT,
    region_id INTEGER,
    subregion_id INTEGER,
    latitude REAL,
    longitude REAL,
    PRIMARY KEY(id)
);

CREATE TABLE IF NOT EXISTS states (
    id INTEGER NOT NULL,
    country_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    iso2 TEXT,
    iso3166_2 TEXT,
    type TEXT,
    latitude REAL,
    longitude REAL,
    PRIMARY KEY(id)
);

CREATE TABLE IF NOT EXISTS cities (
    id INTEGER NOT NULL,
    state_id INTEGER,
    country_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    latitude REAL,
    longitude REAL,
    PRIMARY KEY(id)
);

CREATE TABLE IF NOT EXISTS translations (
    entity_type TEXT NOT NULL,
    entity_id INTEGER NOT NULL,
    lang_code TEXT NOT NULL,
    name TEXT NOT NULL,
    PRIMARY KEY(entity_type, entity_id, lang_code)
);

CREATE INDEX IF NOT EXISTS index_countries_iso2 ON countries(iso2);
CREATE INDEX IF NOT EXISTS index_countries_iso3 ON countries(iso3);
CREATE INDEX IF NOT EXISTS index_countries_name ON countries(name);
CREATE INDEX IF NOT EXISTS index_countries_capital ON countries(capital);
CREATE INDEX IF NOT EXISTS index_countries_region_id ON countries(region_id);
CREATE INDEX IF NOT EXISTS index_countries_subregion_id ON countries(subregion_id);
CREATE INDEX IF NOT EXISTS index_states_country_id ON states(country_id);
CREATE INDEX IF NOT EXISTS index_states_name ON states(name);
CREATE INDEX IF NOT EXISTS index_cities_country_id ON cities(country_id);
CREATE INDEX IF NOT EXISTS index_cities_state_id ON cities(state_id);
CREATE INDEX IF NOT EXISTS index_cities_name ON cities(name);
CREATE INDEX IF NOT EXISTS index_translations_lang_code_name ON translations(lang_code, name);
CREATE INDEX IF NOT EXISTS index_translations_name ON translations(name);
CREATE INDEX IF NOT EXISTS index_subregions_region_id ON subregions(region_id);
"""
