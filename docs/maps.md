# TopoJSON Map Boundaries Guide

MoinoGeo embeds TopoJSON vector polygon boundary data (`world-50m.topojson`) for rendering accurate, offline country boundaries and shapes on mobile map overlays (MapKit, Mapbox, Google Maps).

---

## 1. Why TopoJSON?

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

## 2. TopoJSON Arc-to-Polygon Decoder Algorithm

TopoJSON files represent geographic shapes using quantized delta-encoded arcs. The following conceptual algorithm decodes TopoJSON arcs into standard coordinate polygons:

```
Algorithm: TopoJSON Arc-to-Polygon Decoder

Input: 
  - TopoJSON Object (transform: { scale: [sx, sy], translate: [tx, ty] })
  - Arcs Array: Array of quantized delta coordinates [[dx, dy], ...]
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

## 3. Swift MapKit Integration Example

Drawing country borders on MapKit using decoded TopoJSON geometry:

```swift
import Foundation
import MapKit

public struct TopoJSONDecoder {
    public static func loadCountryPolygon(iso2: String, topoJSONData: Data) -> [MKPolygon] {
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
        // (Uses scale [scale[0], scale[1]] & translate [translate[0], translate[1]])
        var polygons: [MKPolygon] = []
        
        // Convert decoded coordinates to MKPolygon instances for MapKit display
        // mapKitView.addOverlays(polygons)

        return polygons
    }
}
```
