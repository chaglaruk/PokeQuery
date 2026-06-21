#!/usr/bin/env python3
"""Convert runtime PNG assets to WebP (Package 6 size reduction).

- Icons / logo / source (alpha + sharp edges): lossless WebP.
- Large opaque headers / heroes: lossy WebP at quality 85 (visually near-identical,
  much smaller on dark UI backgrounds).

Preserves base filenames so R.drawable refs in code stay valid (extension is stripped).
The asset guard allowlist is updated separately.
"""
import glob
import os
from PIL import Image

DRAWABLE = "app/src/main/res/drawable-nodpi"

# Lossless set: small icons with transparency and sharp vector-like edges,
# plus the wordmark/app-icon sources (must stay crisp).
LOSSLESS = {
    "goal_candy_prep_icon.png",
    "goal_expert_icon.png",
    "goal_hundo_icon.png",
    "goal_safe_cleanup_icon.png",
    "goal_tag_icon.png",
    "goal_trade_icon.png",
    "logo_wordmark_source.png",
    "app_icon_source.png",
    "empty_favorites.png",
}


def convert(path: str) -> None:
    name = os.path.basename(path)
    out = os.path.splitext(path)[0] + ".webp"
    img = Image.open(path)
    lossless = name in LOSSLESS
    if lossless:
        img.save(out, "WEBP", lossless=True, quality=100, method=6)
    else:
        # Large dark hero/header images: lossy q85 keeps them visually clean.
        if img.mode != "RGBA":
            img = img.convert("RGBA")
        img.save(out, "WEBP", quality=85, method=6)
    before = os.path.getsize(path)
    after = os.path.getsize(out)
    print(f"  {name:36s} {before:>9d} -> {after:>9d}  ({100*after/before:5.1f}%) {'[lossless]' if lossless else '[q85]'}")


def main() -> None:
    pngs = sorted(glob.glob(os.path.join(DRAWABLE, "*.png")))
    print(f"Converting {len(pngs)} PNGs to WebP...")
    total_before = sum(os.path.getsize(p) for p in pngs)
    for p in pngs:
        convert(p)
    webps = sorted(glob.glob(os.path.join(DRAWABLE, "*.webp")))
    total_after = sum(os.path.getsize(p) for p in webps)
    print(f"\nTOTAL: {total_before} -> {total_after} bytes ({100*total_after/total_before:.1f}%)")
    print(f"Removing original PNGs...")
    for p in pngs:
        os.remove(p)
    print(f"Removed {len(pngs)} PNGs.")


if __name__ == "__main__":
    main()
