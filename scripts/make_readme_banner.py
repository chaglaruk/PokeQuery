"""Generate an original PokeQuery repo banner (GitHub social preview size).

Original artwork only: dark-navy gradient, cyan glow, abstract grid/search/shield motif.
NO Poké Ball, NO creatures, NO official logos or fonts.
"""
from PIL import Image, ImageDraw, ImageFont, ImageFilter
import math
import os

W, H = 1280, 640
OUT = os.path.join("docs", "readme", "pokequery_repo_banner.png")

FONT_BOLD = r"C:\Windows\Fonts\arialbd.ttf"
FONT_REG = r"C:\Windows\Fonts\segoeui.ttf"
FONT_LIGHT = r"C:\Windows\Fonts\segoeuil.ttf"

# --- palette --------------------------------------------------------------
NAVY_DEEP = (8, 16, 32)        # #081020
NAVY = (12, 28, 56)            # #0c1c38
NAVY_HI = (20, 44, 84)         # #142c54
CYAN = (0, 224, 255)           # #00e0ff
CYAN_DIM = (0, 150, 190)
WHITE = (244, 248, 255)
MUTED = (140, 168, 204)
AMBER = (255, 196, 90)         # spark accent

img = Image.new("RGB", (W, H), NAVY_DEEP)
px = img.load()


def lerp(a, b, t):
    return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(3))


# Vertical gradient background (deep -> navy)
for y in range(H):
    t = y / (H - 1)
    c = lerp(NAVY_DEEP, NAVY, min(1.0, t * 1.4))
    for x in range(W):
        px[x, y] = c

draw = ImageDraw.Draw(img, "RGBA")

# --- radial glow behind wordmark ------------------------------------------
glow = Image.new("RGBA", (W, H), (0, 0, 0, 0))
gdraw = ImageDraw.Draw(glow)
cx, cy = W // 2 - 60, H // 2 - 30
for r in range(560, 40, -20):
    a = int(60 * (1 - r / 560))
    gdraw.ellipse([cx - r, cy - r, cx + r, cy + r], fill=(0, 120, 160, a))
glow = glow.filter(ImageFilter.GaussianBlur(60))
img.paste(glow, (0, 0), glow)

draw = ImageDraw.Draw(img, "RGBA")

# --- abstract grid (search/query motif) -----------------------------------
grid_alpha = 26
grid_color = (90, 150, 200, grid_alpha)
step = 64
for x in range(0, W, step):
    draw.line([(x, 0), (x, H)], fill=grid_color, width=1)
for y in range(0, H, step):
    draw.line([(0, y), (W, y)], fill=grid_color, width=1)

# --- shield motif (right side) --------------------------------------------
def shield(cx, cy, w, h, color, width=4):
    """Abstract shield outline — original geometric form."""
    pts = [
        (cx, cy - h // 2),
        (cx + w // 2, cy - h // 2 + h // 6),
        (cx + w // 2, cy + h // 8),
        (cx, cy + h // 2),
        (cx - w // 2, cy + h // 8),
        (cx - w // 2, cy - h // 2 + h // 6),
    ]
    draw.line(pts + [pts[0]], fill=color, width=width)
    # inner search-query glyph: a stylized "{" + "}" + dot, like a query token
    inner = CYAN
    qy = cy + 6
    draw.arc([cx - 34, qy - 26, cx - 2, qy + 6], start=0, end=270, fill=inner, width=5)
    draw.arc([cx + 2, qy - 26, cx + 34, qy + 6], start=270, end=540, fill=inner, width=5)
    draw.line([(cx - 18, qy + 6), (cx + 18, qy + 6)], fill=inner, width=5)
    draw.ellipse([cx + 14, qy + 18, cx + 30, qy + 34], fill=AMBER)

shield(W - 200, H // 2, 300, 360, CYAN_DIM, width=4)

# soft cyan glow around shield
sglow = Image.new("RGBA", (W, H), (0, 0, 0, 0))
sdraw = ImageDraw.Draw(sglow)
sx, sy = W - 200, H // 2
for r in range(260, 60, -16):
    a = int(34 * (1 - r / 260))
    sdraw.ellipse([sx - r, sy - r, sx + r, sy + r], fill=(0, 200, 240, a))
sglow = sglow.filter(ImageFilter.GaussianBlur(28))
img.paste(sglow, (0, 0), sglow)

draw = ImageDraw.Draw(img, "RGBA")

# --- spark accent (four-point star) ---------------------------------------
def spark(cx, cy, size, color):
    pts = [
        (cx, cy - size),
        (cx + size * 0.28, cy - size * 0.28),
        (cx + size, cy),
        (cx + size * 0.28, cy + size * 0.28),
        (cx, cy + size),
        (cx - size * 0.28, cy + size * 0.28),
        (cx - size, cy),
        (cx - size * 0.28, cy - size * 0.28),
    ]
    draw.polygon(pts, fill=color)

spark(250, 150, 16, AMBER)
spark(1020, 120, 10, CYAN)
spark(980, 540, 12, WHITE)

# --- wordmark: "Poke" white + "Query" cyan with glow ----------------------
def draw_glow_text(base, xy, text, font, fill, glow_color, glow_radius=8, layers=6):
    """Render text with a colored glow halo."""
    glayer = Image.new("RGBA", base.size, (0, 0, 0, 0))
    gd = ImageDraw.Draw(glayer)
    for i in range(layers, 0, -1):
        a = int(180 * (i / layers) ** 2)
        offset = int(glow_radius * i / layers)
        gd.text(xy, text, font=font, fill=glow_color + (a // 4,))
    glayer = glayer.filter(ImageFilter.GaussianBlur(glow_radius))
    base.paste(glayer, (0, 0), glayer)
    d = ImageDraw.Draw(base, "RGBA")
    d.text(xy, text, font=font, fill=fill)

# Measure to center the combined wordmark
f_word = ImageFont.truetype(FONT_BOLD, 168)
poke_w = draw.textlength("Poke", font=f_word)
query_w = draw.textlength("Query", font=f_word)
gap = 6
total_w = poke_w + gap + query_w
start_x = (W - total_w) // 2 - 40
wy = 200

draw_glow_text(img, (start_x, wy), "Poke", f_word, WHITE, (120, 160, 200), 9, 5)
draw_glow_text(img, (start_x + poke_w + gap, wy), "Query", f_word, CYAN, CYAN, 12, 7)

# --- tagline --------------------------------------------------------------
f_tag = ImageFont.truetype(FONT_REG, 46)
tagline = "Build safer Pokémon GO search strings."
tw = draw.textlength(tagline, font=f_tag)
draw.text(((W - tw) // 2 - 40, wy + 200), tagline, font=f_tag, fill=MUTED)

# --- small underline accent ----------------------------------------------
ux = (W - tw) // 2 - 40
uy = wy + 200 + 70
draw.rounded_rectangle([ux + tw // 2 - 80, uy, ux + tw // 2 + 80, uy + 4], radius=2, fill=CYAN)

# --- footer chips: offline / no-tracking / no-login -----------------------
f_chip = ImageFont.truetype(FONT_LIGHT, 26)
chips = ["OFFLINE-FIRST", "NO TRACKING", "NO LOGIN", "NO ADS"]
chip_y = H - 70
cx_cursor = (W - sum(draw.textlength(c + "   ", font=f_chip) for c in chips)) // 2 - 40
for c in chips:
    dot_r = 5
    draw.ellipse([cx_cursor, chip_y + 12, cx_cursor + dot_r * 2, chip_y + 12 + dot_r * 2], fill=CYAN)
    draw.text((cx_cursor + 16, chip_y), c, font=f_chip, fill=MUTED)
    cx_cursor += draw.textlength(c, font=f_chip) + 38

os.makedirs(os.path.dirname(OUT), exist_ok=True)
img.save(OUT, "PNG", optimize=True)
print("wrote", OUT, os.path.getsize(OUT), "bytes")
