"""
PokeQuery v0.5.5: build a contact sheet (montage) of the audit-hardening smoke screenshots.

Reads every *.png in docs/screenshots/v055_audit_hardening/ and tiles them into a single
grid image saved alongside them as contact_sheet.png. Deterministic order = sorted filename.

Run:  python scripts/v055_contact_sheet.py
"""

import os
import sys

from PIL import Image

SHOT_DIR = os.path.join("docs", "screenshots", "v055_audit_hardening")
OUT_PATH = os.path.join(SHOT_DIR, "contact_sheet.png")

# Tile geometry.
THUMB_W = 480          # keep portrait phone shots readable
THUMB_H = 1040
PADDING = 16
BG = (12, 16, 22)      # SlateBlack, matches theme
COLS = 3


def build():
    if not os.path.isdir(SHOT_DIR):
        print(f"ERROR: screenshot dir not found: {SHOT_DIR}")
        sys.exit(1)

    shots = sorted(
        f for f in os.listdir(SHOT_DIR)
        if f.lower().endswith(".png") and f != "contact_sheet.png"
    )
    if not shots:
        print("ERROR: no screenshots found to tile.")
        sys.exit(1)

    rows = (len(shots) + COLS - 1) // COLS
    sheet_w = COLS * THUMB_W + (COLS + 1) * PADDING
    sheet_h = rows * THUMB_H + (rows + 1) * PADDING

    sheet = Image.new("RGB", (sheet_w, sheet_h), BG)

    for i, name in enumerate(shots):
        path = os.path.join(SHOT_DIR, name)
        try:
            img = Image.open(path).convert("RGB")
        except Exception as e:
            print(f"  skip {name}: {e}")
            continue
        img.thumbnail((THUMB_W, THUMB_H), Image.LANCZOS)
        # Center the (possibly narrower) thumb in its cell.
        col = i % COLS
        row = i // COLS
        cell_x = PADDING + col * (THUMB_W + PADDING)
        cell_y = PADDING + row * (THUMB_H + PADDING)
        x = cell_x + (THUMB_W - img.size[0]) // 2
        y = cell_y + (THUMB_H - img.size[1]) // 2
        sheet.paste(img, (x, y))
        print(f"  + {name}")

    sheet.save(OUT_PATH, format="PNG")
    print(f"Wrote {OUT_PATH} ({sheet.size[0]}x{sheet.size[1]}, {len(shots)} tiles)")


if __name__ == "__main__":
    build()
