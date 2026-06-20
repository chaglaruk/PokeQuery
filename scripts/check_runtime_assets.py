import os
import glob
import sys

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
        'goal_expert_icon.png'
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
                
    if failed:
        print("FAIL: Invalid runtime assets detected!")
        sys.exit(1)
    else:
        print("PASS")
        sys.exit(0)

if __name__ == "__main__":
    check_runtime_assets()
