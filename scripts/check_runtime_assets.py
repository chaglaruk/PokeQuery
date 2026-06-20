import os
import glob
import sys

def check_runtime_assets():
    assets_dir = 'app/src/main/res/drawable-nodpi'
    assets = glob.glob(os.path.join(assets_dir, '*.webp')) + glob.glob(os.path.join(assets_dir, '*.png'))
    
    banned_assets = [
        'bg_night_map.webp', 'bg_night_map.png',
        'hero_onboarding_search_shield.webp', 'hero_onboarding_search_shield.png',
        'header_home_map.webp', 'header_home_map.png',
        'header_low_risk.webp', 'header_low_risk.png',
        'header_medium_risk.webp', 'header_medium_risk.png',
        'onboarding_hero_decor.webp', 'onboarding_hero_decor.png',
        'home_map_decor.webp', 'home_map_decor.png',
        'low_risk_decor.webp', 'low_risk_decor.png',
        'medium_risk_decor.webp', 'medium_risk_decor.png',
        'empty_favorites_illustration.webp', 'empty_favorites_illustration.png'
    ]
    
    suspicious_keywords = ['target', 'screenshot', 'full', 'contact', 'mockup', 'screen', 'decor']
    
    print(f"Found {len(assets)} runtime image assets.")
    failed = False
    
    for asset in assets:
        filename = os.path.basename(asset).lower()
        print(f" - {filename}")
        
        if filename in banned_assets:
            print(f"   ERROR: Asset '{filename}' is a banned mockup crop.")
            failed = True
            
        for keyword in suspicious_keywords:
            if keyword in filename:
                print(f"   ERROR: Asset '{filename}' contains suspicious keyword '{keyword}', suggesting it might be derived from a mockup crop.")
                failed = True
                
    if failed:
        print("FAIL: Runtime mockup crops detected!")
        sys.exit(1)
    else:
        print("PASS")
        sys.exit(0)

if __name__ == "__main__":
    check_runtime_assets()
