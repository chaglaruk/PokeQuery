#!/usr/bin/env python3
"""
v0.5.2 (Fix 1): rasterize the refreshed adaptive launcher icon to the legacy
mipmap-* densities (used on API 24-25 where adaptive icons are not supported).

Draws the SAME concept as the adaptive vectors: deep-navy radial background + navy
"query shield" with a cyan magnifier over a 4-dot collection grid + spark accents.
Original artwork, brand palette only.

Run:  python scripts/render_v052_launcher.py
Writes: app/src/main/res/mipmap-<density>/ic_launcher.webp + ic_launcher_round.webp
"""
import math
import os
from PIL import Image, ImageDraw, ImageFilter

VIEWPORT = 108  # matches the vector viewport; we render at high res then downscale
RENDER = 432    # 4x supersample for crisp edges

# Brand palette
NAVY_DEEP = (7, 11, 22, 255)
NAVY_MID = (9, 26, 48, 255)
NAVY_HI = (14, 42, 77, 255)
SHIELD = (11, 37, 69, 255)
SHIELD_HI = (15, 61, 107, 255)
CYAN = (0, 229, 255, 255)
CYAN_BRIGHT = (29, 233, 255, 255)
GOLD = (255, 201, 40, 255)
CYAN_DIM_GLOW = (0, 229, 255, 24)


def shield_path_points(cx, cy, w, h):
    # Rounded-top shield: rectangle top, pointed/curved bottom.
    # Returns a polygon approximating the vector shield (top corners, sides taper to a point).
    top = cy - h / 2
    bot = cy + h / 2
    left = cx - w / 2
    right = cx + w / 2
    shoulder = h * 0.16
    waist = h * 0.62
    return [
        (left, top + shoulder),
        (cx - w * 0.02, top),
        (cx + w * 0.02, top),
        (right, top + shoulder),
        (right, top + waist),
        (cx, bot),
        (left, top + waist),
    ]


def radial_bg(size):
    img = Image.new("RGBA", (size, size), NAVY_DEEP)
    cx = cy = size / 2
    max_r = size * 0.74 / 2 * 2
    # Build a radial gradient by stacking translucent navy circles.
    grad = Image.new("RGBA", (size, size), NAVY_DEEP)
    gd = ImageDraw.Draw(grad)
    steps = 40
    for i in range(steps, 0, -1):
        r = max_r * (i / steps)
        t = 1 - (i / steps)
        # interpolate NAVY_HI -> NAVY_MID -> NAVY_DEEP from center outward
        if t < 0.5:
            f = t / 0.5
            c = lerp(NAVY_HI, NAVY_MID, f)
        else:
            f = (t - 0.5) / 0.5
            c = lerp(NAVY_MID, NAVY_DEEP, f)
        gd.ellipse([cx - r, cy - r, cx + r, cy + r], fill=c)
    img.alpha_composite(grad)
    # cyan glow halo
    halo = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    hd = ImageDraw.Draw(halo)
    hr = size * 0.37
    hd.ellipse([cx - hr, cy - hr, cx + hr, cy + hr], fill=(0, 229, 255, 28))
    hr2 = size * 0.27
    hd.ellipse([cx - hr2, cy - hr2, cx + hr2, cy + hr2], fill=(0, 229, 255, 18))
    halo = halo.filter(ImageFilter.GaussianBlur(size * 0.04))
    img.alpha_composite(halo)
    return img


def lerp(a, b, t):
    return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(4))


def star(draw, cx, cy, r, color, points=4):
    # 4-point sparkle
    pts = []
    for i in range(points * 2):
        ang = math.pi / 2 + i * math.pi / points
        rad = r if i % 2 == 0 else r * 0.34
        pts.append((cx + math.cos(ang) * rad, cy - math.sin(ang) * rad))
    draw.polygon(pts, fill=color)


def render_icon(size):
    s = RENDER
    scale = s / VIEWPORT
    img = radial_bg(s)
    draw = ImageDraw.Draw(img)
    cx = cy = s / 2

    # vignette ring
    draw.ellipse([cx - 37 * scale, cy - 37 * scale, cx + 37 * scale, cy + 37 * scale],
                 outline=(0, 229, 255, 32), width=int(2 * scale))

    # Shield (outline + fill + inner)
    w, h = 40 * scale, 57 * scale
    pts = shield_path_points(cx, cy + 1.5 * scale, w, h)
    draw.polygon(pts, fill=SHIELD, outline=(0, 229, 255, 120))
    # inner highlight (slightly smaller)
    pts2 = shield_path_points(cx, cy + 1.5 * scale, w * 0.88, h * 0.86)
    draw.polygon(pts2, fill=SHIELD_HI)

    # Collection grid: 4 cyan dots
    gx, gy = cx, cy + 3 * scale
    dr = 2.6 * scale
    off = 9 * scale
    for dx in (-off, off):
        for dy in (-off, off):
            draw.ellipse([gx + dx - dr, gy + dy - dr, gx + dx + dr, gy + dy + dr], fill=CYAN)

    # Magnifier ring + handle
    mr = 12 * scale
    mx, my = cx, cy + 2 * scale
    draw.ellipse([mx - mr, my - mr, mx + mr, my + mr],
                 outline=CYAN_BRIGHT, width=int(3.4 * scale))
    # handle (thick line down-right)
    h1 = (mx + mr * 0.72, my + mr * 0.72)
    h2 = (mx + mr * 1.5, my + mr * 1.5)
    draw.line([h1, h2], fill=CYAN_BRIGHT, width=int(4.4 * scale))

    # Spark accents
    star(draw, cx + 16 * scale, cy - 16 * scale, 6.4 * scale, GOLD)
    star(draw, cx - 16 * scale, cy - 16 * scale, 4.0 * scale, CYAN)

    return img.resize((size, size), Image.LANCZOS)


def main():
    densities = {
        "mdpi": 48,
        "hdpi": 72,
        "xhdpi": 96,
        "xxhdpi": 144,
        "xxxhdpi": 192,
    }
    res = os.path.join("app", "src", "main", "res")
    for density, px in densities.items():
        d = os.path.join(res, f"mipmap-{density}")
        os.makedirs(d, exist_ok=True)
        icon = render_icon(px)
        # round = same art; the launcher mask crops it to a circle on API 25.
        icon.save(os.path.join(d, "ic_launcher.webp"), "WEBP", quality=95)
        icon.save(os.path.join(d, "ic_launcher_round.webp"), "WEBP", quality=95)
        print(f"  wrote mipmap-{density}/ic_launcher{{,_round}}.webp ({px}px)")
    print("done")


if __name__ == "__main__":
    main()
