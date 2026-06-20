import os
import glob
import sys
from PIL import Image

def check_runtime_assets():
    assets_dir = 'app/src/main/res/drawable-nodpi'
    assets = glob.glob(os.path.join(assets_dir, '*.webp')) + glob.glob(os.path.join(assets_dir, '*.png')) + glob.glob(os.path.join(assets_dir, '*.jpg'))
    
    allowed_assets = [
        'onboarding_hero.png',
        'home_header_bg.png',
        'safe_cleanup_header.png',
        'candy_prep_header.png',
        'trade_fodder_header.png',
        'empty_favorites.png',
        'goal_safe_cleanup_icon.png',
        'goal_candy_prep_icon.png',
        'goal_trade_icon.png',
        'goal_hundo_icon.png',
        'goal_tag_icon.png',
        'goal_expert_icon.png',
        'v033_app_icon_source.png',
        'v033_onboarding_hero.png',
        'v033_home_header_bg.png',
        'v033_safe_cleanup_header.png',
        'v033_candy_prep_header.png',
        'v033_trade_fodder_header.png',
        'v033_lucky_trade_header.png',
        'v033_pvp_header.png',
        'v033_nundo_header.png'
    ]
    
    suspicious_keywords = ['target', 'mockup', 'screenshot', 'contact', 'screen', 'crop', 'full']
    
    print(f"Found {len(assets)} runtime image assets.")
    failed = False
    
    for asset in assets:
        filename = os.path.basename(asset).lower()
        print(f" - {filename}")
        
        if filename not in allowed_assets:
            print(f"   ERROR: Asset '{filename}' is NOT in the UI_ASSET_CONTRACT allowed list.")
            failed = True
            
        for keyword in suspicious_keywords:
            if keyword in filename:
                print(f"   ERROR: Asset '{filename}' contains suspicious keyword '{keyword}'.")
                failed = True
                
        # Check icon properties
        if "icon" in filename and filename.endswith(".png") and filename != 'v033_app_icon_source.png':
            try:
                img = Image.open(asset).convert("RGBA")
                w, h = img.size
                if w != h:
                    print(f"   ERROR: Icon '{filename}' is not square ({w}x{h}).")
                    failed = True
                
                # Check transparency (if too opaque, it might be a white/checkerboard box)
                alpha = img.getchannel('A')
                non_transparent = sum(1 for a in alpha.getdata() if a > 0)
                total = w * h
                ratio = non_transparent / total
                if ratio > 0.95:
                    print(f"   ERROR: Icon '{filename}' is almost fully opaque ({ratio*100:.1f}%). It likely has a white/checkerboard background.")
                    failed = True
            except Exception as e:
                print(f"   WARNING: Could not process {filename} for transparency check: {e}")
                
    if failed:
        print("FAIL: Invalid runtime assets detected!")
        sys.exit(1)
    else:
        print("PASS")
        sys.exit(0)

if __name__ == "__main__":
    check_runtime_assets()
