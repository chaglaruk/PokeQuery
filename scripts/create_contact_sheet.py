import os
import glob
from PIL import Image

def create_contact_sheet():
    screenshot_dir = os.path.join("docs", "screenshots", "v069_final_user_qa_polish")
    allowed_list = [
        "onboarding_step_1_en.png",
        "onboarding_step_2_en.png",
        "onboarding_step_1_tr.png",
        "onboarding_step_2_tr.png",
        "home_tr.png",
        "detail_or_risk_tr.png",
        "search_assistant_en.png",
        "search_assistant_tr.png",
        "event_guide_en_before_refresh.png",
        "event_guide_en_after_refresh.png",
        "event_guide_tr_before_refresh.png",
        "event_guide_tr_after_refresh.png",
        "event_guide_scrolled_or_expanded_en.png",
        "event_guide_scrolled_or_expanded_tr.png",
        "settings_en.png",
        "settings_tr.png",
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
    
    sheet = Image.new("RGBA", (sheet_w, sheet_h), (8, 13, 28, 255))
    
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
