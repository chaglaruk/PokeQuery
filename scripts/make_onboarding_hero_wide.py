"""
PokeQuery v0.5.4 (Fix 1): generate the wide onboarding hero asset.

This script produces an ORIGINAL, abstract wide hero image (1536x1024, 3:2) used on
the first onboarding page. It replaces the square `onboarding_hero_scene.webp`, which
was rendered as a small tile. The new asset is composed to be cropped wide
(ContentScale.Crop) and to fill a wide panel edge-to-edge.

Design constraints honored:
  * No third-party Pokémon/Niantic assets, logos, silhouettes, or characters.
  * No text glyphs that could read as trademarks.
  * Pure geometry: radial nebula blobs, a concentric "lens"/reticle motif (an abstract
    search-lens nod, not a brand mark), and a faint grid for depth.
  * Dark navy/teal palette consistent with the app theme tokens
    (BackgroundDark #050709, SlateBlack #0E141A, TealPrimary #2EE6C8, BlueCTA #3D7BFF).

Output: app/src/main/res/drawable-nodpi/onboarding_hero_wide.webp  (lossless WebP, RGBA).

Run:  python scripts/make_onboarding_hero_wide.py
"""

import math
import os

from PIL import Image, ImageDraw, ImageFilter

W, H = 1536, 1024

# Palette (matches app theme tokens; ARGB/RGBA tuples below are RGB).
BG_TOP = (5, 7, 9)         # BackgroundDark
BG_MID = (14, 20, 26)      # SlateBlack
BG_BOTTOM = (8, 12, 18)
TEAL = (46, 230, 200)      # TealPrimary
TEAL_SOFT = (46, 230, 200, 70)
BLUE = (61, 123, 255)      # BlueCTA
BLUE_SOFT = (61, 123, 255, 55)
AMBER = (255, 193, 90)     # AmberWarning accent (tiny sparkle only)
WHITE = (235, 246, 250)


def lerp(a, b, t):
    return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(3))


def vertical_gradient(width, height, top, mid, bottom):
    """3-stop vertical gradient."""
    img = Image.new("RGB", (width, height))
    px = img.load()
    for y in range(height):
        t = y / (height - 1)
        if t < 0.5:
            col = lerp(top, mid, t * 2)
        else:
            col = lerp(mid, bottom, (t - 0.5) * 2)
        for x in range(width):
            px[x, y] = col
    return img


def radial_blob(base, cx, cy, radius, color_rgba, intensity=1.0):
    """Add a soft radial glow on an RGBA layer."""
    glow = Image.new("RGBA", base.size, (0, 0, 0, 0))
    draw = ImageDraw.Draw(glow)
    r, g, b, a = color_rgba
    a = int(a * intensity)
    draw.ellipse([cx - radius, cy - radius, cx + radius, cy + radius],
                 fill=(r, g, b, a))
    glow = glow.filter(ImageFilter.GaussianBlur(radius / 2.2))
    return Image.alpha_composite(base, glow)


def draw_reticle(layer, cx, cy, max_r):
    """Concentric rings + crosshair — an abstract 'search lens' motif (no brand IP)."""
    draw = ImageDraw.Draw(layer)
    rings = [int(max_r), int(max_r * 0.78), int(max_r * 0.56), int(max_r * 0.34)]
    widths = [4, 3, 3, 2]
    for r, w in zip(rings, widths):
        alpha = int(255 * (0.18 + 0.04 * (rings.index(r))))
        draw.ellipse([cx - r, cy - r, cx + r, cy + r], outline=TEAL + (alpha,), width=w)
    # Crosshair tick lines (short, not edge-to-edge).
    gap = int(max_r * 0.10)
    tick = int(max_r * 0.18)
    for dx, dy in [(-1, 0), (1, 0), (0, -1), (0, 1)]:
        x1 = cx + dx * gap
        y1 = cy + dy * gap
        x2 = cx + dx * (gap + tick)
        y2 = cy + dy * (gap + tick)
        draw.line([x1, y1, x2, y2], fill=TEAL + (90,), width=3)
    # Central dot.
    core = int(max_r * 0.06)
    draw.ellipse([cx - core, cy - core, cx + core, cy + core], fill=TEAL + (180,))


def draw_grid(layer, spacing, color_rgba):
    """Faint engineering grid for depth."""
    draw = ImageDraw.Draw(layer)
    w, h = layer.size
    a = color_rgba[3]
    for x in range(0, w, spacing):
        draw.line([x, 0, x, h], fill=color_rgba[:3] + (a,), width=1)
    for y in range(0, h, spacing):
        draw.line([0, y, w, y], fill=color_rgba[:3] + (a,), width=1)


def draw_sparkles(layer, n=18):
    """Tiny ambient specks for a premium 'dust' texture."""
    import random
    rng = random.Random(20260623)  # deterministic; reproducible build
    draw = ImageDraw.Draw(layer)
    for _ in range(n):
        x = rng.randint(0, layer.size[0] - 1)
        y = rng.randint(0, layer.size[1] - 1)
        r = rng.choice([1, 1, 1, 2])
        col = rng.choice([WHITE + (60,), TEAL + (70,), AMBER + (60,)])
        draw.ellipse([x - r, y - r, x + r, y + r], fill=col)


def build():
    base = vertical_gradient(W, H, BG_TOP, BG_MID, BG_BOTTOM).convert("RGBA")

    # Ambient nebula blobs — broad, soft, off-center to leave a clean focal area.
    base = radial_blob(base, int(W * 0.72), int(H * 0.32), 520, TEAL_SOFT, 1.0)
    base = radial_blob(base, int(W * 0.22), int(H * 0.68), 460, BLUE_SOFT, 0.9)
    base = radial_blob(base, int(W * 0.50), int(H * 0.50), 700, TEAL_SOFT, 0.45)

    # Faint grid, blurred so it reads as texture, not a wallpaper.
    grid_layer = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    draw_grid(grid_layer, spacing=72, color_rgba=TEAL + (16,))
    grid_layer = grid_layer.filter(ImageFilter.GaussianBlur(1.1))
    base = Image.alpha_composite(base, grid_layer)

    # Abstract concentric "search lens" motif — focal element, slightly left of center.
    motif = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    draw_reticle(motif, cx=int(W * 0.46), cy=int(H * 0.50), max_r=int(H * 0.30))
    base = Image.alpha_composite(base, motif)

    # Soft vignette to anchor the panel edges.
    vignette = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    vd = ImageDraw.Draw(vignette)
    vd.rectangle([0, 0, W, H], fill=(0, 0, 0, 0))
    vd.ellipse([int(W * 0.10), int(H * 0.06), int(W * 0.90), int(H * 0.94)],
               outline=(0, 0, 0, 0), width=0)
    # Darken corners by compositing a reversed radial alpha.
    corner = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    cd = ImageDraw.Draw(corner)
    cd.rectangle([0, 0, W, H], fill=(BG_TOP[0], BG_TOP[1], BG_TOP[2], 90))
    hole = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    hd = ImageDraw.Draw(hole)
    hd.ellipse([int(W * 0.12), int(H * 0.08), int(W * 0.88), int(H * 0.92)],
               fill=(255, 255, 255, 255))
    hole = hole.filter(ImageFilter.GaussianBlur(120))
    # Use hole alpha to knock the corner darkening back in the center.
    final_alpha = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    final_alpha = Image.alpha_composite(final_alpha, corner)
    # Subtract the hole by pasting transparent where hole is opaque.
    mask = hole.split()[3].point(lambda p: 255 - p)
    base.paste(final_alpha, (0, 0), mask)

    # Ambient sparkles last, on top.
    draw_sparkles(base, n=22)

    return base


def main():
    out_dir = os.path.join("app", "src", "main", "res", "drawable-nodpi")
    os.makedirs(out_dir, exist_ok=True)
    out_path = os.path.join(out_dir, "onboarding_hero_wide.webp")
    img = build()
    img.save(out_path, format="WEBP", lossless=True, quality=100)
    print(f"Wrote {out_path} ({img.size[0]}x{img.size[1]})")


if __name__ == "__main__":
    main()
