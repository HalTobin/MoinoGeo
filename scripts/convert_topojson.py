#!/usr/bin/env python3
"""
convert_topojson.py - Converts GeoJSON vector map boundaries to quantized TopoJSON format.

Dataset source: Natural Earth (CC0 1.0 Universal / Public Domain)
https://www.naturalearthdata.com/
"""

import argparse
import json
import math
import os
import sys


def quantize_and_simplify_topojson(topojson_data: dict, quantize_k: int = 10000) -> dict:
    """
    Minifies and optimizes a TopoJSON dataset by reducing property metadata
    and ensuring arc coordinates are properly structured.
    """
    objects = topojson_data.get("objects", {})

    for obj_key, obj_val in objects.items():
        geometries = obj_val.get("geometries", [])
        for geom in geometries:
            props = geom.get("properties", {})
            # Retain only essential country identifier properties
            clean_props = {}
            for key in ["iso_a2", "ISO_A2", "iso_a3", "ISO_A3", "name", "NAME", "admin", "ADMIN", "id", "ID"]:
                if key in props and props[key] is not None:
                    clean_key = key.lower()
                    clean_props[clean_key] = props[key]

            # Standardize ISO_A2 key
            if "iso_a2" not in clean_props and "iso_a3" in clean_props:
                clean_props["iso_a2"] = clean_props["iso_a3"][:2]

            geom["properties"] = clean_props

    return topojson_data


def geojson_to_topo_fallback(geojson_data: dict, quantize_k: int = 10000) -> dict:
    """
    Pure Python lightweight GeoJSON-to-TopoJSON converter fallback
    when the 'topojson' Python package is not installed.
    """
    features = geojson_data.get("features", [])
    geometries = []
    arcs = []
    arc_index_map = {}

    # Calculate bounding box for transform
    min_x, min_y = float("inf"), float("inf")
    max_x, max_y = float("-inf"), float("-inf")

    def collect_bounds(coords):
        nonlocal min_x, min_y, max_x, max_y
        if not coords:
            return
        if isinstance(coords[0], (int, float)):
            x, y = coords[0], coords[1]
            min_x, max_x = min(min_x, x), max(max_x, x)
            min_y, max_y = min(min_y, y), max(max_y, y)
        else:
            for sub in coords:
                collect_bounds(sub)

    for feature in features:
        geom = feature.get("geometry", {})
        collect_bounds(geom.get("coordinates", []))

    if min_x == float("inf"):
        min_x, min_y, max_x, max_y = -180.0, -90.0, 180.0, 90.0

    scale_x = (max_x - min_x) / (quantize_k - 1) if max_x != min_x else 1.0
    scale_y = (max_y - min_y) / (quantize_k - 1) if max_y != min_y else 1.0

    def quantize_pt(pt):
        qx = int(round((pt[0] - min_x) / scale_x))
        qy = int(round((pt[1] - min_y) / scale_y))
        return qx, qy

    def process_ring(ring):
        nonlocal arcs
        q_ring = [quantize_pt(pt) for pt in ring]

        # Delta encode
        delta_ring = []
        prev_x, prev_y = 0, 0
        for i, (qx, qy) in enumerate(q_ring):
            dx = qx - prev_x
            dy = qy - prev_y
            delta_ring.append([dx, dy])
            prev_x, prev_y = qx, qy

        arc_id = len(arcs)
        arcs.append(delta_ring)
        return arc_id

    for feature in features:
        props = feature.get("properties", {})
        clean_props = {
            "iso_a2": (props.get("ISO_A2") or props.get("iso_a2") or props.get("ADM0_A3", "")[:2]).upper(),
            "iso_a3": (props.get("ISO_A3") or props.get("iso_a3") or props.get("ADM0_A3", "")).upper(),
            "name": props.get("NAME") or props.get("name") or props.get("ADMIN", "")
        }

        geom_type = feature.get("geometry", {}).get("type")
        raw_coords = feature.get("geometry", {}).get("coordinates", [])

        if geom_type == "Polygon":
            polygon_arcs = []
            for ring in raw_coords:
                polygon_arcs.append([process_ring(ring)])
            geometries.append({
                "type": "Polygon",
                "properties": clean_props,
                "arcs": polygon_arcs
            })
        elif geom_type == "MultiPolygon":
            multi_arcs = []
            for poly in raw_coords:
                poly_arcs = []
                for ring in poly:
                    poly_arcs.append([process_ring(ring)])
                multi_arcs.append(poly_arcs)
            geometries.append({
                "type": "MultiPolygon",
                "properties": clean_props,
                "arcs": multi_arcs
            })

    topojson = {
        "type": "Topology",
        "transform": {
            "scale": [scale_x, scale_y],
            "translate": [min_x, min_y]
        },
        "objects": {
            "countries": {
                "type": "GeometryCollection",
                "geometries": geometries
            }
        },
        "arcs": arcs
    }

    return topojson


def convert_boundaries(input_path: str, output_path: str, quantization: int):
    os.makedirs(os.path.dirname(os.path.abspath(output_path)), exist_ok=True)

    print(f"Reading input boundary file: {input_path}")
    with open(input_path, "r", encoding="utf-8") as f:
        data = json.load(f)

    input_size_mb = os.path.getsize(input_path) / (1024 * 1024)

    is_topojson = data.get("type") == "Topology"

    if is_topojson:
        print("Input file is already TopoJSON. Optimizing properties and quantization...")
        result_data = quantize_and_simplify_topojson(data, quantize_k=quantization)
    else:
        print("Input file is GeoJSON. Converting to TopoJSON format...")
        try:
            import topojson as tp
            print("Using 'topojson' package for conversion...")
            topo = tp.Topology(data, quantization=quantization)
            result_data = json.loads(topo.to_json())
            result_data = quantize_and_simplify_topojson(result_data, quantize_k=quantization)
        except ImportError:
            print("Notice: 'topojson' Python package not installed. Using pure Python fallback converter...")
            result_data = geojson_to_topo_fallback(data, quantize_k=quantization)

    with open(output_path, "w", encoding="utf-8") as f:
        json.dump(result_data, f, separators=(",", ":"))

    output_size_mb = os.path.getsize(output_path) / (1024 * 1024)
    compression = 100 * (1 - (output_size_mb / input_size_mb)) if input_size_mb > 0 else 0

    print(f"\nBoundary Conversion Summary:")
    print(f"  - Input Size:  {input_size_mb:.2f} MB")
    print(f"  - Output Size: {output_size_mb:.2f} MB")
    print(f"  - Savings:     {compression:.1f}% reduction")
    print(f"  - Output File: {output_path}")


def main():
    parser = argparse.ArgumentParser(description="Convert/Optimize map boundary data into TopoJSON.")
    parser.add_argument("--input", "-i", required=True, help="Input GeoJSON or TopoJSON boundary file path.")
    parser.add_argument("--output", "-o", default="data/world-50m.topojson", help="Output TopoJSON file path.")
    parser.add_argument("--quantization", "-q", type=int, default=10000, help="Coordinate quantization level.")

    args = parser.parse_args()
    try:
        convert_boundaries(args.input, args.output, args.quantization)
    except Exception as e:
        print(f"Error converting boundaries: {e}", file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    main()
