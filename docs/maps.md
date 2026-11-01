# Map & Topography Extension Guide

The MoinoGeo map ecosystem is modularly split into five dedicated Kotlin Multiplatform submodules under `:maps`:

```
kotlin/maps/
├── core/         # Map styling (MapStyle, MapDefaults, MapDefault, ZoneStyle, BorderStyle), topography & border data models (TopoZone, GeoPolygon, GeoPoint, TopoFeature, BoundaryFeature), and provider interfaces (BoundaryProvider, TopographyProvider)
├── borders/      # Default and custom border implementations (TopoJsonDecoder, DefaultBoundaryProvider, CustomBoundaryProvider)
├── topography/   # Default and custom topography implementations (DefaultTopographyProvider with WATER, FOREST, ROCK, SNOW, NEUTRAL zones, TopographyUtils)
├── plane/        # 2D plane map visualization composable (PlaneMap, PlaneMapState with pan/zoom gestures)
└── sphere/       # 3D sphere map placeholder and styling configuration (SphereMap, SphereMapStyle with globe radius, tilt, camera distance, and atmosphere settings)
```

---

## 1. Modular Architecture & Data Injection

Geopolitical borders, territorial claims, and map detail levels vary across application domain requirements and political jurisdictions. MoinoGeo decouples boundary geometry decoding and map rendering from the default `world-50m.topojson` asset.

```
+-----------------------------------------------------------------------+
|                    MoinoGeo TopoJSON Map Extension                    |
+-----------------------------------------------------------------------+
                                   |
                  +----------------+----------------+
                  v                                 v
   +------------------------------+  +------------------------------+
   | Default Boundary Provider    |  | Custom Boundary Provider     |
   | - Loads world-50m.topojson   |  | - Loads high-res TopoJSON    |
   | - Default Natural Earth arcs |  | - Loads custom region boundaries|
   |                              |  | - Loads politically compliant|
   |                              |  |   territorial borders        |
   +------------------------------+  +------------------------------+
```

### Key Modularity Capabilities:
1. **Omit Map Boundaries:** If your application does not display map overlays, omit `world-50m.topojson` to save ~2.1 MB binary footprint.
2. **Custom Map Boundary Datasets:** Inject custom TopoJSON or GeoJSON files into MoinoGeo's arc decoder to support custom boundary demarcations, regional maps, or high-precision spatial overlays.

### Abstract Boundary Provider Examples

#### Kotlin (Android)
```kotlin
fun interface BoundaryProvider {
    fun openBoundaryStream(): java.io.InputStream
}

class DefaultBoundaryProvider(private val context: android.content.Context) : BoundaryProvider {
    override fun openBoundaryStream(): java.io.InputStream {
        return context.assets.open("geo/world-50m.topojson")
    }
}

class CustomBoundaryProvider(private val customFile: java.io.File) : BoundaryProvider {
    override fun openBoundaryStream(): java.io.InputStream {
        return customFile.inputStream()
    }
}
```

#### Swift (iOS / macOS)
```swift
public protocol BoundaryProvider {
    func loadBoundaryData() -> Data?
}

public struct DefaultBoundaryProvider: BoundaryProvider {
    public init() {}
    public func loadBoundaryData() -> Data? {
        guard let url = MoinoGeoBundle.topoJSONURL else { return nil }
        return try? Data(contentsOf: url)
    }
}

public struct CustomBoundaryProvider: BoundaryProvider {
    private let customURL: URL
    public init(customURL: URL) { self.customURL = customURL }
    public func loadBoundaryData() -> Data? {
        try? Data(contentsOf: customURL)
    }
}
```

---

## 2. Why TopoJSON?

GeoJSON repeats coordinate pairs for shared boundaries between adjacent geographic entities (e.g. the border between France and Spain). 

**TopoJSON** encodes topology rather than discrete polygons. Shared boundaries are stored as single shared "arcs", eliminating coordinate redundancy and reducing raw boundary payload size by **70% to 80%** compared to GeoJSON (~2.1 MB total optimized asset footprint).

```
+-----------------------------------------------------------------+
|                       GeoJSON vs TopoJSON                       |
+-----------------------------------------------------------------+
| GeoJSON:                                                        |
| Country A: Polygon [[0,0], [1,1], [1,0], [0,0]]                 |
| Country B: Polygon [[1,1], [2,2], [2,0], [1,1]]                 |
| (Shared boundary [1,1] duplicated in both polygons)             |
|                                                                 |
| TopoJSON:                                                       |
| Arc 0: [[0,0], [1,1]]                                           |
| Arc 1: [[1,1], [1,0], [0,0]]                                    |
| Arc 2: [[1,1], [2,2], [2,0]]                                    |
| Country A: [Arc 0, Arc 1]                                       |
| Country B: [Arc 0 (reversed), Arc 2]                            |
+-----------------------------------------------------------------+
```

---

## 3. TopoJSON Arc-to-Polygon Decoder Algorithm

TopoJSON files represent geographic shapes using quantized delta-encoded arcs. The following conceptual algorithm decodes TopoJSON arcs into standard coordinate polygons:

```
Algorithm: TopoJSON Arc-to-Polygon Decoder

Input: 
  - TopoJSON Data Stream / Bytes
  - Feature Geometry Arc Indices: List of integer arc indices [e.g., [0, 1, -1]]
    (negative index denotes reverse traversal)

Output:
  - List of Geographic Coordinates [(latitude, longitude)]

Steps:
  1. For each arc index in Feature Geometry:
      a. Determine direction: if index < 0, arc_id = ~index (bitwise NOT) and reverse points.
      b. Initialize current_x = 0, current_y = 0.
      c. For each delta point [dx, dy] in Arcs[arc_id]:
          i. current_x += dx
          ii. current_y += dy
          iii. longitude = (current_x * sx) + tx
          iv. latitude = (current_y * sy) + ty
          v. Append (latitude, longitude) to Polygon Coordinate List.
  2. Return Polygon Coordinate List.
```

---

## 4. Swift MapKit Integration Example

Drawing country borders on MapKit using decoded TopoJSON geometry via modular `BoundaryProvider`:

```swift
import Foundation
import MapKit

public struct TopoJSONDecoder {
    public static func loadCountryPolygon(
        iso2: String,
        provider: BoundaryProvider = DefaultBoundaryProvider()
    ) -> [MKPolygon] {
        guard let topoJSONData = provider.loadBoundaryData() else { return [] }

        // 1. Parse TopoJSON JSON structure
        guard let json = try? JSONSerialization.jsonObject(with: topoJSONData) as? [String: Any],
              let objects = json["objects"] as? [String: Any],
              let countriesObj = objects["countries"] as? [String: Any],
              let geometries = countriesObj["geometries"] as? [[String: Any]],
              let transform = json["transform"] as? [String: Any],
              let scale = transform["scale"] as? [Double],
              let translate = transform["translate"] as? [Double],
              let arcs = json["arcs"] as? [[[Int]]] else {
            return []
        }

        // 2. Find target country geometry by ISO code
        guard let countryGeom = geometries.first(where: { 
            (($0["properties"] as? [String: Any])?["iso_a2"] as? String) == iso2.uppercased() 
        }) else {
            return []
        }

        // 3. Decode quantized arcs into geographic coordinates
        var polygons: [MKPolygon] = []
        // Convert decoded coordinates to MKPolygon instances for MapKit display
        return polygons
    }
}
```
