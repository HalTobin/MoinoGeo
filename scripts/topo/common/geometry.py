"""Shapely / GeoJSON → MoinoGeo Kotlin JSON structures."""

from __future__ import annotations

from typing import Any

from shapely.geometry import MultiPolygon, Polygon, mapping, shape
from shapely.geometry.base import BaseGeometry


def _ring_to_points(coords: list[tuple[float, float]]) -> list[dict[str, float]]:
    # Shapely exteriors are (x=lon, y=lat); Kotlin GeoPoint is lat/lon.
    return [{"latitude": float(y), "longitude": float(x)} for x, y in coords]


def polygon_to_geo_polygon(poly: Polygon) -> dict[str, Any] | None:
    if poly.is_empty or not isinstance(poly, Polygon):
        return None
    outer = _ring_to_points(list(poly.exterior.coords))
    if len(outer) < 4:
        return None
    holes = []
    for interior in poly.interiors:
        ring = _ring_to_points(list(interior.coords))
        if len(ring) >= 4:
            holes.append(ring)
    return {"outerBounds": outer, "innerHoles": holes}


def geom_to_geo_polygons(
    geom: BaseGeometry,
    *,
    min_area: float = 0.0,
) -> list[dict[str, Any]]:
    if geom is None or geom.is_empty:
        return []

    polys: list[Polygon]
    if isinstance(geom, Polygon):
        polys = [geom]
    elif isinstance(geom, MultiPolygon):
        polys = list(geom.geoms)
    else:
        # GeometryCollection / others — extract polygonal parts.
        polys = []
        if hasattr(geom, "geoms"):
            for part in geom.geoms:
                polys.extend(
                    p
                    for p in (
                        [part]
                        if isinstance(part, Polygon)
                        else list(part.geoms)
                        if isinstance(part, MultiPolygon)
                        else []
                    )
                    if isinstance(p, Polygon)
                )

    out: list[dict[str, Any]] = []
    for poly in polys:
        if min_area > 0 and poly.area < min_area:
            continue
        converted = polygon_to_geo_polygon(poly)
        if converted is not None:
            out.append(converted)
    return out


def simplify_geom(geom: BaseGeometry, tolerance: float) -> BaseGeometry:
    if tolerance <= 0 or geom is None or geom.is_empty:
        return geom
    return geom.simplify(tolerance, preserve_topology=True)


def geojson_geometry(geom: BaseGeometry) -> dict[str, Any]:
    return mapping(geom)


def shape_from_geojson(obj: dict[str, Any]) -> BaseGeometry:
    return shape(obj)
