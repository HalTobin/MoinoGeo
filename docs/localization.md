# Localization & Multi-Language Support

MoinoGeo provides full multi-language translation support for world regions, countries, states/provinces, and cities. Designed to be **easily updatable**, translators and developers can maintain translations in simple source files that automatically compile into the offline SQLite database.

---

## 1. Supported Languages

For launch, MoinoGeo officially targets the following 7 languages:

| ISO Code | Language Name | Native Name |
| :--- | :--- | :--- |
| `en` | English | English |
| `fr` | French | Français |
| `es` | Spanish | Español |
| `it` | Italian | Italiano |
| `de` | German | Deutsch |
| `uk` | Ukrainian | Українська |
| `ru` | Russian | Русский |

---

## 2. Easily Updatable Source File Layout

Translations are maintained outside the compiled database in human- and LLM-friendly JSON files located in `data/translations/`. This structure allows anyone to update or add missing translations without modifying Kotlin, Swift, or SQL code.

```
data/translations/
├── regions/
│   ├── en.json
│   ├── fr.json
│   ├── es.json
│   ├── it.json
│   ├── de.json
│   ├── uk.json
│   └── ru.json
├── countries/
│   ├── en.json
│   ├── fr.json
│   ├── es.json
│   ├── it.json
│   ├── de.json
│   ├── uk.json
│   └── ru.json
└── states/
    ├── fr.json
    ├── es.json
    └── ...
```

### Translation File Formats

#### Countries (`data/translations/countries/fr.json`)
Keyed by ISO 3166-1 alpha-2 code:
```json
{
  "US": "États-Unis",
  "DE": "Allemagne",
  "JP": "Japon",
  "FR": "France",
  "UA": "Ukraine",
  "IT": "Italie",
  "ES": "Espagne"
}
```

#### World Regions (`data/translations/regions/fr.json`)
Keyed by standard English region / subregion name:
```json
{
  "Americas": "Amériques",
  "Europe": "Europe",
  "Asia": "Asie",
  "Africa": "Afrique",
  "Oceania": "Océanie",
  "Northern America": "Amérique du Nord",
  "Western Europe": "Europe de l'Ouest"
}
```

#### States & Regions (`data/translations/states/fr.json`)
Keyed by country ISO2 code and state/province code (`<country_code>.<state_code>` or state ID):
```json
{
  "US.CA": "Californie",
  "US.NY": "État de New York",
  "CA.QC": "Québec",
  "DE.BY": "Bavière"
}
```

---

## 3. Database Translation Architecture

During database compilation via `scripts/build_database.py`, translation files from `data/translations/` are parsed and compiled into the normalized `translations` table in `moinogeo.db`:

```sql
CREATE TABLE translations (
    entity_type TEXT NOT NULL,  -- 'country', 'state', 'city', 'region'
    entity_id   INTEGER NOT NULL,
    lang_code   TEXT NOT NULL,    -- 'en', 'fr', 'es', 'it', 'de', 'uk', 'ru'
    name        TEXT NOT NULL,
    PRIMARY KEY (entity_type, entity_id, lang_code)
);

CREATE INDEX idx_translations_lookup ON translations(entity_type, entity_id, lang_code);
```

In addition, top-level country translations are stored directly in the `countries.translations` JSON column for quick single-row reads when fetching full country records.

---

## 4. Querying Localized Names at Runtime

When executing queries, apps request the localized name with an explicit fallback to default English (`name` column).

### SQL Fallback Pattern
```sql
SELECT 
    c.id,
    c.iso2,
    COALESCE(t.name, c.name) AS name,
    c.capital,
    c.phone_code
FROM countries c
LEFT JOIN translations t 
       ON t.entity_type = 'country' 
      AND t.entity_id = c.id 
      AND t.lang_code = :langCode
WHERE c.iso2 = :iso2;
```

### Kotlin (Android Room) DAO Example
```kotlin
@Query("""
    SELECT 
        c.id,
        c.iso2,
        COALESCE(t.name, c.name) AS name,
        c.capital,
        c.phone_code
    FROM countries c
    LEFT JOIN translations t 
           ON t.entity_type = 'country' 
          AND t.entity_id = c.id 
          AND t.lang_code = :langCode
    WHERE c.region = :region
    ORDER BY name ASC
""")
suspend fun getCountriesByRegionLocalized(region: String, langCode: String): List<LocalizedCountry>
```

### Swift (iOS / macOS SQLite3) Example
```swift
public func fetchCountry(iso2: String, languageCode: String = "en") -> Country? {
    let query = """
        SELECT c.id, COALESCE(t.name, c.name) AS name, c.iso2, c.capital 
        FROM countries c
        LEFT JOIN translations t 
               ON t.entity_type = 'country' 
              AND t.entity_id = c.id 
              AND t.lang_code = ?
        WHERE c.iso2 = ? LIMIT 1;
    """
    // bind languageCode to parameter 1, iso2 to parameter 2
}
```

---

## 5. Workflow for Updating & Adding Translations

1. **Add or Edit Translation Files:**
   Edit the corresponding JSON file under `data/translations/` (e.g., `data/translations/countries/uk.json` or `data/translations/states/es.json`).
2. **Add a New Language:**
   Create new JSON files for the language code in each domain folder (e.g. `data/translations/countries/pt.json`).
3. **Rebuild Database:**
   Run the database compilation script:
   ```bash
   python3 scripts/build_database.py --input data/raw/ --translations data/translations/ --output data/moinogeo.db
   ```
4. **Distribute Database:**
   Copy the updated `moinogeo.db` to platform asset directories (`android/assets/databases/` or `darwin/Resources/`).
