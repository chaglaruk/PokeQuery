"""Build docs/screenshots/v053_motion/contact_sheet.png from the 9 numbered shots.

Lays out 3 columns x 3 rows of thumbnails, each labeled with its number and a
short caption describing the motion-polish smoke item it verifies.
Uses only PIL (no numpy dependency).
"""
from PIL import Image, ImageDraw, ImageFont
from pathlib import Path

SHOTS_DIR = Path("docs/screenshots/v053_motion")
OUT = SHOTS_DIR / "contact_sheet.png"

# (filename, caption) — each shot maps to a mandatory smoke item.
SHOTS = [
    ("01_onboarding.png", "01 Onboarding entrance (stagger settles once)"),
    ("02_onboarding_page2.png", "02 Pager transition (smooth, no jank)"),
    ("03_home.png", "03 Home cards entrance (stagger once)"),
    ("04_safe_cleanup_detail.png", "04 Home->Detail crossfade"),
    ("05_risk_warning.png", "05 Copy->Risk Warning flow"),
    ("06_expert_builder.png", "06 Expert Builder entrance + wrap (no h-scroll)"),
    ("07_expert_lucky_traded.png", "07 Expert advisory: copy ENABLED"),
    ("08_settings.png", "08 Settings entrance (no black screen)"),
    ("09_app_language.png", "09 App Language + Visual Density (no crash)"),
]

COLS, ROWS = 3, 3
THUMB_W, THUMB_H = 420, 910  # portrait thumbnails
PAD = 16
LABEL_H = 34
CELL_W = THUMB_W + PAD * 2
CELL_H = THUMB_H + LABEL_H + PAD * 2
MARGIN = 24
TITLE_H = 56

W = MARGIN * 2 + COLS * CELL_W
H = MARGIN * 2 + TITLE_H + ROWS * CELL_H

sheet = Image.new("RGB", (W, H), (24, 28, 40))
draw = ImageDraw.Draw(sheet)


def font(size, bold=False):
    candidates = [
        "C:/Windows/Fonts/arialbd.ttf" if bold else "C:/Windows/Fonts/arial.ttf",
        "C:/Windows/Fonts/segoeui.ttf",
        "arial.ttf",
    ]
    for c in candidates:
        try:
            return ImageFont.truetype(c, size)
        except Exception:
            continue
    return ImageFont.load_default()


title_f = font(28, bold=True)
label_f = font(15, bold=True)
draw.text(
    (MARGIN, MARGIN),
    "PokeQuery v0.5.3  —  Motion Polish Smoke Test Contact Sheet",
    fill=(235, 240, 250),
    font=title_f,
)

for i, (fname, caption) in enumerate(SHOTS):
    col = i % COLS
    row = i // COLS
    x = MARGIN + col * CELL_W + PAD
    y = MARGIN + TITLE_H + row * CELL_H + PAD
    try:
        im = Image.open(SHOTS_DIR / fname).convert("RGB")
    except FileNotFoundError:
        im = Image.new("RGB", (THUMB_W, THUMB_H), (60, 20, 20))
    im.thumbnail((THUMB_W, THUMB_H))
    # paste centered horizontally in the cell
    px = x + (THUMB_W - im.width) // 2
    sheet.paste(im, (px, y))
    draw.rectangle([x - 1, y - 1, x + THUMB_W, y + THUMB_H], outline=(70, 90, 120), width=1)
    draw.text((x, y + THUMB_H + 6), caption, fill=(210, 220, 235), font=label_f)

sheet.save(OUT)
print(f"wrote {OUT}  ({sheet.size})")
