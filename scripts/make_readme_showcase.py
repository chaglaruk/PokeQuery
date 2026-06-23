"""Build a compact README showcase contact sheet from curated v0.5.2 screenshots.

Layout: 3 columns x 2 rows = 6 phone screenshots on a dark-navy background,
with a small caption under each. Output: docs/readme/pokequery_readme_showcase.png
"""
from PIL import Image, ImageDraw, ImageFont
import os

SRC = os.path.join("docs", "readme", "screenshots")
OUT = os.path.join("docs", "readme", "pokequery_readme_showcase.png")

# (filename, caption) — 6 latest v0.5.2 screens
SHOTS = [
    ("01_home.png", "Home"),
    ("02_onboarding.png", "Onboarding"),
    ("03_safe_cleanup_risk_warning.png", "Safe Cleanup · Risk Warning"),
    ("04_popular_presets.png", "Popular Presets"),
    ("05_knowledge_base.png", "Knowledge Base"),
    ("06_settings.png", "Settings · Language Safety"),
]

BG = (10, 20, 38)
PANEL = (16, 30, 56)
CAPTION = (210, 226, 246)
CYAN = (0, 224, 255)

COLS = 3
ROWS = 2
THUMB_W = 360
PAD = 28
CAPTION_H = 44
TITLE_H = 90

# scale thumbnails
def load_thumb(path, target_w):
    im = Image.open(path).convert("RGB")
    w, h = im.size
    th = int(h * target_w / w)
    return im.resize((target_w, th), Image.LANCZOS), th

first, first_h = load_thumb(os.path.join(SRC, SHOTS[0][0]), THUMB_W)
THUMB_H = first_h

cell_w = THUMB_W
cell_h = THUMB_H + CAPTION_H
W = PAD * (COLS + 1) + cell_w * COLS
H = TITLE_H + PAD * (ROWS + 1) + cell_h * ROWS

sheet = Image.new("RGB", (W, H), BG)
draw = ImageDraw.Draw(sheet)

f_title = ImageFont.truetype(r"C:\Windows\Fonts\arialbd.ttf", 40)
f_cap = ImageFont.truetype(r"C:\Windows\Fonts\segoeui.ttf", 24)

title = "PokeQuery"
tw = draw.textlength(title, font=f_title)
draw.text((PAD, 26), title, font=f_title, fill=(244, 248, 255))
draw.text((PAD + tw + 18, 38), "Build safer Pokémon GO search strings · offline-first, no tracking",
          font=f_cap, fill=(150, 176, 210))
draw.rounded_rectangle([PAD, 30, PAD + 4, 30 + 40], radius=2, fill=CYAN)

idx = 0
for r in range(ROWS):
    for c in range(COLS):
        fname, cap = SHOTS[idx]
        idx += 1
        x = PAD + c * (cell_w + PAD)
        y = TITLE_H + PAD + r * (cell_h + PAD)
        # panel background
        draw.rounded_rectangle([x, y, x + cell_w, y + THUMB_H], radius=14,
                               fill=PANEL, outline=(40, 70, 110))
        thumb, _ = load_thumb(os.path.join(SRC, fname), THUMB_W - 16)
        tx = x + 8
        ty = y + 8
        sheet.paste(thumb, (tx, ty))
        # caption
        cw = draw.textlength(cap, font=f_cap)
        draw.text((x + (cell_w - cw) // 2, y + THUMB_H + 10), cap, font=f_cap, fill=CAPTION)

os.makedirs(os.path.dirname(OUT), exist_ok=True)
sheet.save(OUT, "PNG", optimize=True)
print("wrote", OUT, os.path.getsize(OUT), "bytes", sheet.size)
