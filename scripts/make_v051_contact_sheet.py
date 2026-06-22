"""v0.5.1 UI polish contact sheet generator.

Tiling screenshots into a single dark-background image (matching app theme) for review.
"""
import os
import glob
from PIL import Image, ImageDraw, ImageFont

SRC = "docs/screenshots/v051_ui_polish"
OUT = os.path.join(SRC, "contact_sheet.png")
COLS = 3
THUMB_W = 540  # half-res thumbnails
PAD = 16
LABEL_H = 28
BG = (10, 14, 26)  # BackgroundDark #0A0E1A
LABEL_BG = (18, 26, 46)  # CardPremium
LABEL_FG = (176, 190, 197)  # TextSecondary

screens = sorted(glob.glob(os.path.join(SRC, "*.png")))
screens = [s for s in screens if "contact_sheet" not in os.path.basename(s)]

if not screens:
    raise SystemExit("No screenshots found in " + SRC)

thumbs = []
for s in screens:
    im = Image.open(s).convert("RGB")
    ratio = THUMB_W / im.width
    th = int(im.height * ratio)
    im = im.resize((THUMB_W, th), Image.LANCZOS)
    thumbs.append((os.path.splitext(os.path.basename(s))[0], im))

rows = (len(thumbs) + COLS - 1) // COLS
cell_w = THUMB_W + PAD * 2
cell_h = max(t.height for _, t in thumbs) + LABEL_H + PAD * 2
sheet_w = COLS * cell_w + PAD
sheet_h = rows * cell_h + PAD

sheet = Image.new("RGB", (sheet_w, sheet_h), BG)
draw = ImageDraw.Draw(sheet)
try:
    font = ImageFont.truetype("arial.ttf", 16)
except OSError:
    font = ImageFont.load_default()

for i, (name, im) in enumerate(thumbs):
    r = i // COLS
    c = i % COLS
    x = PAD + c * cell_w + PAD
    y = PAD + r * cell_h + PAD
    draw.rectangle([x - 1, y - 1, x + THUMB_W, y + LABEL_H], fill=LABEL_BG)
    draw.text((x + 8, y + 5), name, fill=LABEL_FG, font=font)
    sheet.paste(im, (x, y + LABEL_H))

sheet.save(OUT, optimize=True)
print(f"Saved {OUT}: {sheet.size}, {len(thumbs)} screens")
