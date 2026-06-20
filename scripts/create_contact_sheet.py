import os
import glob
from PIL import Image

def create_contact_sheet():
    screenshot_dir = os.path.join("docs", "screenshots")
    files = sorted(glob.glob(os.path.join(screenshot_dir, "*.png")))
    files = [f for f in files if "contact_sheet" not in f and "design_vs_actual" not in f]
    
    if not files:
        print("No screenshots found.")
        return
        
    images = [Image.open(f) for f in files]
    
    # Grid layout (e.g., 5x2 for 10 images)
    cols = min(5, len(images))
    rows = (len(images) + cols - 1) // cols
    
    w, h = images[0].size
    padding = 20
    
    sheet_w = cols * w + (cols + 1) * padding
    sheet_h = rows * h + (rows + 1) * padding
    
    sheet = Image.new("RGBA", (sheet_w, sheet_h), (255, 255, 255, 255))
    
    for idx, img in enumerate(images):
        col = idx % cols
        row = idx // cols
        x = padding + col * (w + padding)
        y = padding + row * (h + padding)
        sheet.paste(img, (x, y))
        
    target = os.path.join(screenshot_dir, "contact_sheet.png")
    sheet.save(target)
    print(f"Created {target}")

if __name__ == "__main__":
    create_contact_sheet()
