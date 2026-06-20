import os
import sys

try:
    from PIL import Image, ImageDraw, ImageFont, ImageChops
except ImportError:
    import subprocess
    subprocess.check_call([sys.executable, "-m", "pip", "install", "Pillow"])
    from PIL import Image, ImageDraw, ImageFont, ImageChops

files = [
    "docs/screenshots/1_Onboarding.png",
    "docs/screenshots/2_Home.png",
    "docs/screenshots/3_GuidedQuestions.png",
    "docs/screenshots/4_SafeCleanup_Preview.png",
    "docs/screenshots/5_CandyPrep_Preview.png",
    "docs/screenshots/6_TradeFodder_Preview.png",
    "docs/screenshots/7_KnowledgeBase.png",
    "docs/screenshots/8_ExpertBuilder.png",
    "docs/screenshots/9_Favorites.png",
    "docs/screenshots/10_Settings.png",
]

# Open images
images = []
labels = []
for f in files:
    try:
        img = Image.open(f)
        images.append(img)
        labels.append(os.path.basename(f))
    except Exception as e:
        print(f"Failed to open {f}: {e}")

if not images:
    print("No images found.")
    sys.exit(1)

w, h = images[0].size
padding = 60
cell_w = w
cell_h = h + padding

cols = 2
rows = 5
sheet_w = cols * cell_w
sheet_h = rows * cell_h

contact_sheet = Image.new('RGB', (sheet_w, sheet_h), color=(255, 255, 255))
draw = ImageDraw.Draw(contact_sheet)

try:
    font = ImageFont.truetype("arial.ttf", 40)
except IOError:
    font = ImageFont.load_default()

for idx, (img, label) in enumerate(zip(images, labels)):
    row = idx // cols
    col = idx % cols
    x = col * cell_w
    y = row * cell_h
    
    draw.text((x + 10, y + 10), label, fill=(0, 0, 0), font=font)
    contact_sheet.paste(img, (x, y + padding))

contact_sheet.save("docs/screenshots/contact_sheet.png")

# Check if they are visually distinct
distinct = True
if len(images) > 1:
    first = images[0].convert('RGB')
    identical_count = 0
    for i in range(1, len(images)):
        diff = ImageChops.difference(first, images[i].convert('RGB'))
        if diff.getbbox() is None:
            identical_count += 1
    
    # If all other 9 images match the 1st one exactly
    if identical_count == len(images) - 1:
        distinct = False

if distinct:
    print("RESULT: Screenshots are VISUALLY DISTINCT")
else:
    print("RESULT: Screenshots are MOSTLY IDENTICAL")
