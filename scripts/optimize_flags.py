#!/usr/bin/env python3
"""
optimize_flags.py - Minifies and standardizes SVG country flag icons.

Dataset source: lipis/flag-icons (MIT License)
https://github.com/lipis/flag-icons

Keying strategy: Lowercase ISO 3166-1 alpha-2 code (e.g., us.svg, de.svg, jp.svg)
"""

import argparse
import os
import re
import sys
import xml.etree.ElementTree as ET


def clean_svg_string(svg_text: str) -> str:
    """Minifies SVG content by removing metadata, comments, and unneeded attributes."""
    # Remove XML processing instructions and DOCTYPE declarations
    svg_text = re.sub(r"<\?xml.*?\?>", "", svg_text, flags=re.DOTALL)
    svg_text = re.sub(r"<!DOCTYPE.*?>", "", svg_text, flags=re.DOTALL)

    # Remove XML comments
    svg_text = re.sub(r"<!--.*?-->", "", svg_text, flags=re.DOTALL)

    # Remove metadata tags
    svg_text = re.sub(r"<metadata>.*?</metadata>", "", svg_text, flags=re.DOTALL)

    # Parse XML to clean namespaces and unneeded attributes
    try:
        # Register namespaces to prevent ns0: prefixes
        namespaces = dict([node for _, node in ET.iterparse(
            re.sub(r"<!--.*?-->", "", svg_text, flags=re.DOTALL),
            events=["start-ns"]
        )])
        for ns_prefix, ns_uri in namespaces.items():
            ET.register_namespace(ns_prefix, ns_uri)
    except Exception:
        pass

    # Normalize whitespace
    svg_text = re.sub(r"\s+", " ", svg_text)
    svg_text = re.sub(r">\s+<", "><", svg_text)

    return svg_text.strip()


def process_flags(input_dir: str, output_dir: str):
    os.makedirs(output_dir, exist_ok=True)

    input_files = []
    for root, _, files in os.walk(input_dir):
        for f in files:
            if f.endswith(".svg"):
                input_files.append(os.path.join(root, f))

    if not input_files:
        print(f"No SVG files found in {input_dir}")
        return

    print(f"Found {len(input_files)} SVG files in {input_dir}")

    total_input_bytes = 0
    total_output_bytes = 0
    processed_count = 0

    for filepath in sorted(input_files):
        filename = os.path.basename(filepath)
        # Extract ISO code (assumes filename starts with 2-letter ISO code e.g. "us.svg" or "US.svg")
        iso_match = re.search(r"([a-zA-Z]{2})\.svg$", filename)
        if not iso_match:
            continue

        iso_code = iso_match.group(1).lower()
        output_filename = f"{iso_code}.svg"
        output_path = os.path.join(output_dir, output_filename)

        with open(filepath, "r", encoding="utf-8", errors="ignore") as f:
            raw_svg = f.read()

        total_input_bytes += len(raw_svg.encode("utf-8"))
        optimized_svg = clean_svg_string(raw_svg)
        total_output_bytes += len(optimized_svg.encode("utf-8"))

        with open(output_path, "w", encoding="utf-8") as f:
            f.write(optimized_svg)

        processed_count += 1

    input_kb = total_input_bytes / 1024
    output_kb = total_output_bytes / 1024
    savings = 100 * (1 - (total_output_bytes / total_input_bytes)) if total_input_bytes > 0 else 0

    print(f"\nFlag Optimization Summary:")
    print(f"  - Processed Flags: {processed_count}")
    print(f"  - Raw Size:       {input_kb:.2f} KB")
    print(f"  - Optimized Size: {output_kb:.2f} KB")
    print(f"  - Compression:    {savings:.1f}% reduction")
    print(f"  - Output Dir:     {output_dir}")


def main():
    parser = argparse.ArgumentParser(description="Optimize SVG flag icons for MoinoGeo.")
    parser.add_argument("--input", "-i", required=True, help="Input directory containing SVG flags.")
    parser.add_argument("--output", "-o", default="data/flags", help="Output directory for minified SVG flags.")

    args = parser.parse_args()
    try:
        process_flags(args.input, args.output)
    except Exception as e:
        print(f"Error optimizing flags: {e}", file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    main()
