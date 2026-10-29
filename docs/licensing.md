# Licensing & Commercial Compliance

MoinoGeo integrates open-source datasets and icon libraries. Each upstream component retains its respective license.

---

## 1. Data & Asset Attribution Table

| Asset Type | Source Project | Upstream Repository / Source | License | Usage in MoinoGeo |
| :--- | :--- | :--- | :--- | :--- |
| **Relational Geographic Data** | Countries States Cities Database | [`dr5hn/countries-states-cities-database`](https://github.com/dr5hn/countries-states-cities-database) | **ODbL v1.0** (Open Database License) | Stored raw in `data/`, compiled into SQLite database schema (`moinogeo.db`). |
| **Vector Flags: Rectangular** | Flag Icons | [`lipis/flag-icons`](https://github.com/lipis/flag-icons) | **MIT License** | Processed into `moinogeo-flags-lipis` module. |
| **Vector Flags: Rounded** | Flagpack Core | [`Yummygum/flagpack-core`](https://github.com/Yummygum/flagpack-core) | **MIT License** | Processed into `moinogeo-flags-flagpack` module. |
| **Vector Flags: Circular** | Circle Flags | [`HatScripts/circle-flags`](https://github.com/HatScripts/circle-flags) | **MIT License** | Processed into `moinogeo-flags-circle` module. |
| **Vector Map Boundaries** | Natural Earth | [`naturalearthdata.com`](https://www.naturalearthdata.com/) | **CC0 1.0 Universal** (Public Domain) | Quantized TopoJSON vector polygons (`world-50m.topojson`). |

---

## 2. Commercial Compliance Guidelines

Integrating MoinoGeo into commercial applications requires adherence to the licenses of its underlying datasets:

### 1. Open Database License (ODbL v1.0) Compliance
- **Attribution:** Commercial apps using MoinoGeo must include an attribution notice in their application (e.g., in an "About" or "Legal Notices" screen):
  > *"Geographic data provided by MoinoGeo, derived from the Countries States Cities Database by dr5hn, licensed under ODbL v1.0."*
- **Share-Alike:** Modifying or extending the underlying database schema and redistributing the database file itself requires making those database modifications available under the ODbL. Bundling the unmodified `moinogeo.db` file within a proprietary application does **not** force your proprietary application code to become open-source.

### 2. MIT License Compliance (Flags)
- Retain the MIT copyright notice in your legal documentation or license bundle.

### 3. CC0 Public Domain (Natural Earth Map Data)
- Free to use, modify, and distribute commercially without restriction or mandatory attribution.
