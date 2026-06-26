from PIL import Image, ImageDraw, ImageFont
import os, math, sys
sys.stdout.reconfigure(encoding='utf-8')

folder = r'C:\Users\Caglar\Desktop\PokeQuery\docs\screenshots\v061_workflows_surface_context\missing_qa\device_fresh'
out = os.path.join(folder, 'contact_sheet.png')

# All 15 screenshots (sorted by filename)
files = sorted([
    f for f in os.listdir(folder) 
    if f.endswith('.png') and f != 'contact_sheet.png' and not f.startswith('00_')
])

n = len(files)
print(f'Including {n} screenshots')

cols = 5
rows = math.ceil(n / cols)
thumb_w = 220
label_h = 26
gap = 6
pad = 10

first = Image.open(os.path.join(folder, files[0]))
aspect = first.height / first.width
thumb_h = int(thumb_w * aspect)

cell_w = thumb_w + gap * 2
cell_h = thumb_h + label_h + gap * 2
width = cols * cell_w + pad * 2
height = rows * cell_h + pad * 2

canvas = Image.new('RGB', (width, height), (240, 240, 240))
draw = ImageDraw.Draw(canvas)

try:
    font = ImageFont.truetype('arial.ttf', 11)
except:
    font = ImageFont.load_default()

for idx, fname in enumerate(files):
    col = idx % cols
    row = idx // cols
    cx = pad + col * cell_w
    cy = pad + row * cell_h
    
    img = Image.open(os.path.join(folder, fname))
    img.thumbnail((thumb_w, thumb_h), Image.LANCZOS)
    
    ox = cx + (cell_w - img.width) // 2
    oy = cy + gap
    canvas.paste(img, (ox, oy))
    
    label = fname.replace('.png', '')
    lx = cx + cell_w // 2
    ly = cy + cell_h - gap - label_h // 2
    draw.text((lx, ly), label, fill=(0, 0, 0), font=font, anchor='mt')

canvas.save(out, 'PNG')
print(f'Saved: {out}')
print(f'Size: {canvas.size}')
