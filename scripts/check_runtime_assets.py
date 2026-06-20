import os
import glob

def check_runtime_assets():
    assets_dir = 'app/src/main/res/drawable-nodpi'
    assets = glob.glob(os.path.join(assets_dir, '*.webp')) + glob.glob(os.path.join(assets_dir, '*.png'))
    
    suspicious_keywords = ['target', 'screenshot', 'full', 'contact', 'mockup', 'screen']
    
    print(f"Found {len(assets)} runtime image assets.")
    for asset in assets:
        filename = os.path.basename(asset).lower()
        print(f" - {filename}")
        
        for keyword in suspicious_keywords:
            if keyword in filename:
                print(f"   WARNING: Asset '{filename}' contains suspicious keyword '{keyword}', suggesting it might be a baked UI crop.")

if __name__ == "__main__":
    check_runtime_assets()
