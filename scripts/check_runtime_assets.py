import os
import glob
import sys
from PIL import Image

def check_runtime_assets():
    assets_dir = 'app/src/main/res/drawable-nodpi'
    assets = glob.glob(os.path.join(assets_dir, '*.webp')) + glob.glob(os.path.join(assets_dir, '*.png')) + glob.glob(os.path.join(assets_dir, '*.jpg'))

    # Package 6: runtime assets were converted PNG->WebP. Allowlist uses the .webp names.
    # Names match R.drawable refs (extension is stripped in code), so refs did not change.
    allowed_assets = {
        'app_icon_source.webp',
        'candy_prep_header.webp',
        'detail_header_blue.webp',
        'detail_header_gold.webp',
        'empty_favorites.webp',
        'goal_candy_prep_icon.webp',
        'goal_expert_icon.webp',
        'goal_hundo_icon.webp',
        'goal_safe_cleanup_icon.webp',
        'goal_tag_icon.webp',
        'goal_trade_icon.webp',
        'home_header_bg.webp',
        'logo_wordmark_source.webp',
        'lucky_trade_header.webp',
        'nundo_header.webp',
        'onboarding_hero.webp',
        'onboarding_hero_scene.webp',
        'onboarding_hero_wide.webp',
        'pokequery_wordmark.png',
        'pvp_header.webp',
        'safe_cleanup_header.webp',
        'trade_fodder_header.webp',
    }
    intentional_raster_icons = set()

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

        # Check icon properties (applies to both .png and .webp icons — not weakened by the conversion).
        if "icon" in filename and filename != 'app_icon_source.webp' and filename != 'app_icon_source.png' and filename not in intentional_raster_icons:
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
                if ratio > 0.99 and not filename.startswith('icon_'):
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
