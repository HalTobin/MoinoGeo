# Apple Integration Guide (iOS / macOS)

This guide details how to integrate MoinoGeo into iOS and macOS applications using Swift Package Manager (SPM), Swift, and SQLite3 / GRDB.

---

## 1. Swift Package Manager Setup

Bundling MoinoGeo assets (`moinogeo.db`, `world-50m.topojson`, and SVG flags) in `Package.swift`:

```swift
// Package.swift
import PackageDescription

let package = Package(
    name: "MoinoGeo",
    platforms: [
        .iOS(.v15),
        .macOS(.v12)
    ],
    products: [
        .library(name: "MoinoGeo", targets: ["MoinoGeo"])
    ],
    targets: [
        .target(
            name: "MoinoGeo",
            resources: [
                .copy("Resources/moinogeo.db"),
                .copy("Resources/world-50m.topojson"),
                .copy("Resources/flags")
            ]
        )
    ]
)
```

---

## 2. Bundle Resource Access Helper

Ensure asset loaders resolve file URLs correctly via `Bundle.module` (for SPM) or target bundle:

```swift
import Foundation

public final class MoinoGeoBundle {
    public static var bundle: Bundle {
        #if SWIFT_PACKAGE
        return Bundle.module
        #else
        return Bundle(for: MoinoGeoBundle.self)
        #endif
    }
    
    public static var databaseURL: URL? {
        bundle.url(forResource: "moinogeo", withExtension: "db")
    }
    
    public static var topoJSONURL: URL? {
        bundle.url(forResource: "world-50m", withExtension: "topojson")
    }
    
    public static func flagURL(for iso2: String) -> URL? {
        bundle.url(forResource: iso2.lowercased(), withExtension: "svg", subdirectory: "flags")
    }
}
```

---

## 3. Query Engine Example (SQLite3 C-API)

Querying `moinogeo.db` in Swift with multi-language fallback support:

```swift
import Foundation
import SQLite3

public struct Country {
    public let id: Int
    public let name: String
    public let iso2: String
    public let capital: String
    public let phoneCode: String
}

public final class MoinoGeoQueryEngine {
    private var db: OpaquePointer?

    public init(databaseURL: URL) throws {
        if sqlite3_open_v2(databaseURL.path, &db, SQLITE_OPEN_READONLY, nil) != SQLITE_OK {
            throw NSError(domain: "MoinoGeo", code: 1, userInfo: [NSLocalizedDescriptionKey: "Failed to open DB"])
        }
    }

    deinit {
        sqlite3_close(db)
    }

    public func fetchCountry(iso2: String, langCode: String = "en") -> Country? {
        let query = """
            SELECT c.id, COALESCE(t.name, c.name) AS name, c.iso2, c.capital, c.phone_code 
            FROM countries c 
            LEFT JOIN translations t 
                   ON t.entity_type = 'country' AND t.entity_id = c.id AND t.lang_code = ? 
            WHERE c.iso2 = ? LIMIT 1;
        """
        var statement: OpaquePointer?

        guard sqlite3_prepare_v2(db, query, -1, &statement, nil) == SQLITE_OK else { return nil }
        defer { sqlite3_finalize(statement) }

        sqlite3_bind_text(statement, 1, (langCode as NSString).utf8String, -1, nil)
        sqlite3_bind_text(statement, 2, (iso2 as NSString).utf8String, -1, nil)

        if sqlite3_step(statement) == SQLITE_ROW {
            let id = Int(sqlite3_column_int(statement, 0))
            let name = String(cString: sqlite3_column_text(statement, 1))
            let iso2 = String(cString: sqlite3_column_text(statement, 2))
            let capital = String(cString: sqlite3_column_text(statement, 3))
            let phoneCode = String(cString: sqlite3_column_text(statement, 4))
            return Country(id: id, name: name, iso2: iso2, capital: capital, phoneCode: phoneCode)
        }
        return nil
    }
}
```
