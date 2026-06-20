import os
import glob
from PIL import Image

try:
    from rembg import remove
    REMBG_AVAILABLE = True
except ImportError:
    REMBG_AVAILABLE = False
    print("WARNING: rembg not installed. Fallback to simple color thresholding.")

def remove_background(img_path):
    img = Image.open(img_path).convert("RGBA")
    
    if REMBG_AVAILABLE:
        # Use rembg for robust background removal
        img = remove(img)
    else:
        # Simple thresholding fallback
        data = img.getdata()
        new_data = []
        for item in data:
            if item[0] > 200 and item[1] > 200 and item[2] > 200:
                new_data.append((255, 255, 255, 0))
            else:
                new_data.append(item)
        img.putdata(new_data)
        
    return img

def process_asset(source_path, target_name, is_icon=False):
    target_dir = 'app/src/main/res/drawable-nodpi'
    os.makedirs(target_dir, exist_ok=True)
    target_path = os.path.join(target_dir, target_name)
    
    if not os.path.exists(source_path):
        print(f"File not found: {source_path}")
        return

    print(f"Processing {source_path} -> {target_name}...")
    
    img = Image.open(source_path).convert("RGBA")
    
    if is_icon:
        img = remove_background(source_path)
        
        # Trim
        bbox = img.getbbox()
        if bbox:
            img = img.crop(bbox)
            
        # Resize to 512x512 with transparent padding
        img.thumbnail((480, 480), Image.Resampling.LANCZOS)
        new_img = Image.new("RGBA", (512, 512), (0, 0, 0, 0))
        new_img.paste(img, ((512 - img.width) // 2, (512 - img.height) // 2))
        img = new_img

    img.save(target_path, "PNG")

def main():
    source_dir = r"C:\Users\Caglar\Downloads\a"
    
    mapping = {
        "1": ("onboarding_hero.png", True),
        "2": ("safe_cleanup_header.png", True),
        "3": ("candy_prep_header.png", True),
        "4": ("trade_fodder_header.png", True),
        "5": ("goal_safe_cleanup_icon.png", True),
        "6": ("goal_candy_prep_icon.png", True),
        "7": ("goal_trade_icon.png", True),
        "8": ("goal_hundo_icon.png", True),
        "9": ("goal_tag_icon.png", True),
        "10": ("goal_expert_icon.png", True)
    }

    # Find files like "ChatGPT Image... (1).png"
    files = glob.glob(os.path.join(source_dir, "*.png"))
    for f in files:
        basename = os.path.basename(f)
        # extract the number inside the parentheses
        import re
        match = re.search(r'\((\d+)\)', basename)
        if match:
            num = match.group(1)
            if num in mapping:
                target_name, is_icon = mapping[num]
                process_asset(f, target_name, is_icon)

if __name__ == "__main__":
    main()
