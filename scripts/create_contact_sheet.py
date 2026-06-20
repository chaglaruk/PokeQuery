import os
import glob
from PIL import Image

def create_contact_sheet():
    screenshot_dir = os.path.join("docs", "screenshots")
    allowed_list = [
        "1_onboarding_step_1.png",
        "2_onboarding_step_2.png",
        "3_onboarding_step_3.png",
        "4_home.png",
        "5_safe_cleanup_detail.png",
        "6_candy_prep_detail.png",
        "7_trade_fodder_detail.png",
        "8_nundo_detail.png",
        "9_pvp_detail.png",
        "10_lucky_trade_detail.png",
        "11_popular_presets.png",
        "12_knowledge_search.png",
        "13_knowledge_expanded.png",
        "14_favorites.png",
        "15_history.png",
        "16_settings.png"
    ]
    
    files = []
    for filename in allowed_list:
        path = os.path.join(screenshot_dir, filename)
        if not os.path.exists(path):
            print(f"ERROR: Missing required screenshot: {filename}")
            exit(1)
        files.append(path)
        
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
