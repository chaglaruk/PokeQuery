from pathlib import Path
from PIL import Image

root = Path(".")
mockup = root / "ChatGPT Image 20 Haz 2026 01_53_00 (1).png"

if not mockup.exists():
    candidates = list(root.glob("ChatGPT Image*.png")) + list(root.glob("*mockup*.png"))
    if not candidates:
        raise FileNotFoundError("Mockup image not found. Put the selected UI image in the PokeQuery folder.")
    mockup = candidates[0]

(root / "design_targets").mkdir(exist_ok=True)
(root / "docs").mkdir(exist_ok=True)
(root / "prompts").mkdir(exist_ok=True)

img = Image.open(mockup).convert("RGB")
w, h = img.size
phone_w = w // 4

targets = [
    ("01_onboarding_target.png", 0),
    ("02_home_target.png", 1),
    ("03_safe_cleanup_preview_target.png", 2),
    ("04_candy_prep_preview_target.png", 3),
]

for name, i in targets:
    left = i * phone_w
    right = (i + 1) * phone_w if i < 3 else w
    img.crop((left, 0, right, h)).save(root / "design_targets" / name)

img.save(root / "reference_full_mockup.png")

(root / "docs" / "DESIGN_LOCK.md").write_text("""# PokeQuery UI Design Lock

The mockup is a visual reference only. It must not be used as a runtime background or baked UI screenshot.

Hard rules:
- All text must be real Jetpack Compose text.
- All buttons, cards, chips, app bars, bottom nav, and search panels must be real Compose components.
- Target crops are reference-only.
- Runtime assets must contain no baked text, no buttons, no app bars, no bottom nav, no full-screen mockup crops.
- Do not use official Pokémon/Niantic/Nintendo assets for release.

Target style:
- dark navy / midnight blue
- Pokémon GO-like blue CTA
- teal/green low-risk
- amber/yellow medium-risk
- rounded premium cards
- map / route / waypoint / exploration mood
- mature game companion utility, not hacker/cyberpunk/crypto

Screen requirements:
- Onboarding: title, subtitle, large hero, No login / Offline-first / Copy-only, blue Start button, no bottom nav.
- Home: app bar, map header, “What do you want to find?”, 2-column 6-card grid, bottom nav.
- Preview: app bar, risk header, search string panel, copy button, explanation/warning, protected chips.
- Knowledge/Expert/Favorites/Settings must be distinct real screens.
""", encoding="utf-8")

(root / "docs" / "UI_ASSET_CONTRACT.md").write_text("""# PokeQuery Runtime UI Asset Contract

Allowed runtime assets only:

- onboarding_hero.png
- home_header_bg.png
- safe_cleanup_header.png
- candy_prep_header.png
- trade_fodder_header.png
- empty_favorites.png
- goal_safe_cleanup_icon.png
- goal_candy_prep_icon.png
- goal_trade_icon.png
- goal_hundo_icon.png
- goal_tag_icon.png
- goal_expert_icon.png

Rules:
- No baked UI text.
- No buttons.
- No app bars.
- No bottom navigation.
- No full-screen screenshot crops.
- No target/mockup/contact/screen/crop/full filenames in runtime drawable assets.
- App must work with simple placeholders if final art is missing.
""", encoding="utf-8")

(root / "prompts" / "codex_ui_prompt.txt").write_text(r"""/goal
Continue PokeQuery in:

C:\Users\Caglar\Desktop\PokeQuery

Do not start over.
Do not change the search-string engine.
This is UI polish only.

Read:
docs/DESIGN_LOCK.md
docs/UI_ASSET_CONTRACT.md

Use these as visual references only:
reference_full_mockup.png
design_targets/01_onboarding_target.png
design_targets/02_home_target.png
design_targets/03_safe_cleanup_preview_target.png
design_targets/04_candy_prep_preview_target.png

Important:
Do NOT use target crops or the full mockup as runtime drawables.
Do NOT crop UI-containing sections into the app.
All UI text/buttons/cards/chips/nav/appbars must be real Compose.

Task:
1. Verify build:
   ./gradlew clean test assembleDebug --no-daemon --console=plain

2. Prepare allowed clean asset slots under app/src/main/res/drawable-nodpi/.
   Use simple placeholders if final art is unavailable.
   Allowed names only:
   onboarding_hero.png
   home_header_bg.png
   safe_cleanup_header.png
   candy_prep_header.png
   trade_fodder_header.png
   empty_favorites.png
   goal_safe_cleanup_icon.png
   goal_candy_prep_icon.png
   goal_trade_icon.png
   goal_hundo_icon.png
   goal_tag_icon.png
   goal_expert_icon.png

3. Rebuild UI composition to match target crops:
   - Onboarding: large title, subtitle, hero, trust indicators, blue CTA, no bottom nav.
   - Home: app bar, map header, 2-column 6-card goal grid, bottom nav.
   - Safe Cleanup Preview: Low Risk header, string panel, blue Copy button, explanation, chips.
   - Candy/Trade Preview: Medium Risk header, string panel, amber Copy button, warning/tip, chips.
   - Knowledge/Expert/Favorites/Settings: polish with same card system.

4. Update scripts/check_runtime_assets.py:
   fail if runtime drawable filename contains target, mockup, screenshot, contact, screen, crop, full.
   allow only filenames from docs/UI_ASSET_CONTRACT.md.

5. Run:
   ./gradlew clean test assembleDebug --no-daemon --console=plain
   python scripts/check_runtime_assets.py

6. Capture screenshots and generate:
   docs/screenshots/contact_sheet.png
   docs/screenshots/design_vs_actual.png

7. Update docs/VISUAL_ACCEPTANCE_REPORT.md with honest YES/NO checklist.

8. Commit and push:
   git status --short
   git add .
   git commit -m "polish UI using strict design handoff assets"
   git push

Final response:
- latest commit hash
- build/test result
- asset guard result
- APK path
- contact sheet path
- design_vs_actual path
- runtime asset list
- remaining visual limitations
""", encoding="utf-8")

print("Created UI handoff files:")
print("- reference_full_mockup.png")
print("- design_targets/*.png")
print("- docs/DESIGN_LOCK.md")
print("- docs/UI_ASSET_CONTRACT.md")
print("- prompts/codex_ui_prompt.txt")
