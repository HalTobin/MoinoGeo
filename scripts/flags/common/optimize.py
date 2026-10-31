"""Lossless SVG cleanup shared by Kotlin / iOS flag exporters.

Only removes non-rendering payload (comments, XML prolog, DOCTYPE, metadata)
and collapses whitespace *between* tags. Geometry, attributes, and text content
are left untouched so visual quality is preserved.
"""

from __future__ import annotations

import re
from dataclasses import dataclass

_COMMENT_RE = re.compile(r"<!--.*?-->", re.DOTALL)
_XML_DECL_RE = re.compile(r"<\?xml.*?\?>", re.DOTALL | re.IGNORECASE)
_DOCTYPE_RE = re.compile(r"<!DOCTYPE.*?>", re.DOTALL | re.IGNORECASE)
_METADATA_RE = re.compile(r"<metadata\b[^>]*>.*?</metadata>", re.DOTALL | re.IGNORECASE)
_INTER_TAG_WS_RE = re.compile(r">\s+<")


@dataclass(frozen=True)
class OptimizationResult:
    """Outcome of a lossless SVG pass."""

    content: str
    changed: bool
    had_comments: bool
    had_metadata: bool
    had_prolog: bool
    bytes_before: int
    bytes_after: int

    @property
    def bytes_saved(self) -> int:
        return self.bytes_before - self.bytes_after


def analyze_svg(svg_text: str) -> dict[str, bool]:
    """Detect removable non-visual payload in an SVG string."""
    return {
        "comments": bool(_COMMENT_RE.search(svg_text)),
        "metadata": bool(_METADATA_RE.search(svg_text)),
        "prolog": bool(_XML_DECL_RE.search(svg_text) or _DOCTYPE_RE.search(svg_text)),
        "inter_tag_whitespace": bool(_INTER_TAG_WS_RE.search(svg_text)),
    }


def optimize_svg(svg_text: str, *, force: bool = False) -> OptimizationResult:
    """Return a quality-preserving optimized SVG.

    When *force* is False (default), the SVG is rewritten only if it contains
    comments, metadata, an XML/DOCTYPE prolog, or inter-tag whitespace.
    Already-compact files are returned unchanged.
    """
    original = svg_text
    info = analyze_svg(original)
    needs_work = any(info.values())

    if not force and not needs_work:
        size = len(original.encode("utf-8"))
        return OptimizationResult(
            content=original,
            changed=False,
            had_comments=False,
            had_metadata=False,
            had_prolog=False,
            bytes_before=size,
            bytes_after=size,
        )

    cleaned = original
    cleaned = _COMMENT_RE.sub("", cleaned)
    cleaned = _XML_DECL_RE.sub("", cleaned)
    cleaned = _DOCTYPE_RE.sub("", cleaned)
    cleaned = _METADATA_RE.sub("", cleaned)
    cleaned = _INTER_TAG_WS_RE.sub("><", cleaned)
    cleaned = cleaned.strip()

    before = len(original.encode("utf-8"))
    after = len(cleaned.encode("utf-8"))
    return OptimizationResult(
        content=cleaned,
        changed=cleaned != original,
        had_comments=info["comments"],
        had_metadata=info["metadata"],
        had_prolog=info["prolog"],
        bytes_before=before,
        bytes_after=after,
    )
