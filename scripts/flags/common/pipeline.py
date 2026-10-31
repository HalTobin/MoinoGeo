"""Shared export pipeline: discover → optimize → write."""

from __future__ import annotations

from dataclasses import dataclass, field
from pathlib import Path

from .catalog import FlagSet, discover_iso2_svgs, normalized_iso2_filename
from .optimize import optimize_svg


@dataclass
class ExportStats:
    """Aggregate stats for one flag-set export."""

    flag_set_id: str
    source_dir: Path
    dest_dir: Path
    discovered: int = 0
    written: int = 0
    optimized: int = 0
    copied_as_is: int = 0
    skipped_unchanged: int = 0
    bytes_before: int = 0
    bytes_after: int = 0
    errors: list[str] = field(default_factory=list)

    def print_summary(self) -> None:
        saved = self.bytes_before - self.bytes_after
        pct = (100.0 * saved / self.bytes_before) if self.bytes_before else 0.0
        print(f"\n[{self.flag_set_id}]")
        print(f"  source:     {self.source_dir}")
        print(f"  dest:       {self.dest_dir}")
        print(f"  discovered: {self.discovered}")
        print(f"  written:    {self.written}")
        print(f"  optimized:  {self.optimized}")
        print(f"  as-is:      {self.copied_as_is}")
        print(f"  unchanged:  {self.skipped_unchanged}")
        print(
            f"  size:       {self.bytes_before / 1024:.1f} KB → "
            f"{self.bytes_after / 1024:.1f} KB "
            f"({pct:.1f}% reduction)"
        )
        if self.errors:
            print(f"  errors:     {len(self.errors)}")
            for err in self.errors[:10]:
                print(f"    - {err}")


def export_flag_set(
    flag_set: FlagSet,
    dest_dir: Path,
    *,
    root: Path | None = None,
    dry_run: bool = False,
    force_optimize: bool = False,
    clean: bool = False,
) -> ExportStats:
    """Export one flag set into *dest_dir* with shared lossless optimization.

    Parameters
    ----------
    clean:
        If True, remove existing ``*.svg`` files in *dest_dir* before writing
        (keeps non-SVG files such as ``.gitkeep`` / LICENSE).
    """
    source_dir = flag_set.source_dir(root)
    stats = ExportStats(
        flag_set_id=flag_set.id,
        source_dir=source_dir,
        dest_dir=dest_dir,
    )

    try:
        sources = discover_iso2_svgs(source_dir, recursive=False)
    except FileNotFoundError as exc:
        stats.errors.append(str(exc))
        return stats

    stats.discovered = len(sources)

    if not dry_run:
        dest_dir.mkdir(parents=True, exist_ok=True)
        if clean:
            for stale in dest_dir.glob("*.svg"):
                stale.unlink()

    for src in sources:
        out_name = normalized_iso2_filename(src)
        dest = dest_dir / out_name
        try:
            raw = src.read_text(encoding="utf-8")
            result = optimize_svg(raw, force=force_optimize)
            stats.bytes_before += result.bytes_before
            stats.bytes_after += result.bytes_after

            if result.changed:
                stats.optimized += 1
            else:
                stats.copied_as_is += 1

            if dry_run:
                stats.written += 1
                continue

            if dest.is_file() and dest.read_text(encoding="utf-8") == result.content:
                stats.skipped_unchanged += 1
                continue

            dest.write_text(result.content, encoding="utf-8")
            stats.written += 1
        except Exception as exc:  # noqa: BLE001 — collect and continue
            stats.errors.append(f"{src.name}: {exc}")

    return stats
